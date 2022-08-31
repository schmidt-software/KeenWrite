/*
 * Copyright 2013, Morten Nobel-Joergensen
 *
 * License: The BSD 3-Clause License
 * http://opensource.org/licenses/BSD-3-Clause
 */
package com.keenwrite.preview.images;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import static com.keenwrite.preview.images.ConstrainedDimension.createAbsolutionDimension;
import static java.awt.image.BufferedImage.*;
import static java.awt.image.DataBuffer.TYPE_USHORT;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;

/**
 * Based on <a href="http://schmidt.devlib.org/jiu/">Java Image Util</a>.
 * <p>
 * Note that the filter method is not thread-safe.
 * </p>
 *
 * @author Morten Nobel-Joergensen
 * @author Heinz Doerr
 */
public class ResampleOp extends AdvancedResizeOp {
  private static final int MAX_CHANNEL_VALUE = 255;

  private int nrChannels;
  private int srcWidth;
  private int srcHeight;
  private int dstWidth;
  private int dstHeight;

  static class SubSamplingData {
    // individual - per row or per column - nr of contributions
    private final int[] arrN;
    // 2Dim: [wid or hei][contrib]
    private final int[] arrPixel;
    // 2Dim: [wid or hei][contrib]
    private final float[] arrWeight;
    // the primary index length for the 2Dim arrays : arrPixel and arrWeight
    private final int numContributors;

    private SubSamplingData( int[] arrN, int[] arrPixel, float[] arrWeight,
                             int numContributors ) {
      this.arrN = arrN;
      this.arrPixel = arrPixel;
      this.arrWeight = arrWeight;
      this.numContributors = numContributors;
    }

    public int[] getArrN() {
      return arrN;
    }
  }

  private SubSamplingData horizontalSubsamplingData;
  private SubSamplingData verticalSubsamplingData;

  private final int threadCount = getRuntime().availableProcessors();
  private final AtomicInteger multipleInvocationLock = new AtomicInteger();
  private final ResampleFilter mFilter;

  public ResampleOp(
    final ResampleFilter filter, final int destWidth, final int destHeight ) {
    this( filter,
          createAbsolutionDimension( destWidth, destHeight ) );
  }

  public ResampleOp(
    final ResampleFilter filter, ConstrainedDimension dimensionConstrain ) {
    super( dimensionConstrain );
    mFilter = filter;
  }

  public BufferedImage doFilter(
    BufferedImage srcImg, BufferedImage dest, int dstWidth, int dstHeight ) {
    this.dstWidth = dstWidth;
    this.dstHeight = dstHeight;

    if( dstWidth < 3 || dstHeight < 3 ) {
      throw new IllegalArgumentException( "Target must be at least 3x3." );
    }

    assert multipleInvocationLock.incrementAndGet() == 1 :
      "Multiple concurrent invocations detected";

    final var srcType = srcImg.getType();

    if( srcType == TYPE_BYTE_BINARY ||
      srcType == TYPE_BYTE_INDEXED ||
      srcType == TYPE_CUSTOM ) {
      srcImg = ImageUtils.convert(
        srcImg,
        srcImg.getColorModel().hasAlpha() ? TYPE_4BYTE_ABGR : TYPE_3BYTE_BGR );
    }

    this.nrChannels = ImageUtils.nrChannels( srcImg );
    assert nrChannels > 0;
    this.srcWidth = srcImg.getWidth();
    this.srcHeight = srcImg.getHeight();

    byte[][] workPixels = new byte[ srcHeight ][ dstWidth * nrChannels ];

    // Pre-calculate  sub-sampling
    horizontalSubsamplingData = createSubSampling(
      mFilter, srcWidth, dstWidth );
    verticalSubsamplingData = createSubSampling(
      mFilter, srcHeight, dstHeight );

    final BufferedImage scrImgCopy = srcImg;
    final byte[][] workPixelsCopy = workPixels;
    final Thread[] threads = new Thread[ threadCount - 1 ];

    for( int i = 1; i < threadCount; i++ ) {
      final int finalI = i;
      threads[ i - 1 ] = new Thread( () -> horizontallyFromSrcToWork(
        scrImgCopy, workPixelsCopy, finalI, threadCount ) );
      threads[ i - 1 ].start();
    }

    horizontallyFromSrcToWork( scrImgCopy, workPixelsCopy, 0, threadCount );
    waitForAllThreads( threads );

    byte[] outPixels = new byte[ dstWidth * dstHeight * nrChannels ];

    // --------------------------------------------------
    // Apply filter to sample vertically from Work to Dst
    // --------------------------------------------------
    final byte[] outPixelsCopy = outPixels;
    for( int i = 1; i < threadCount; i++ ) {
      final int finalI = i;
      threads[ i - 1 ] = new Thread( () -> verticalFromWorkToDst(
        workPixelsCopy, outPixelsCopy, finalI, threadCount ) );
      threads[ i - 1 ].start();
    }
    verticalFromWorkToDst( workPixelsCopy, outPixelsCopy, 0, threadCount );
    waitForAllThreads( threads );

    //noinspection UnusedAssignment
    workPixels = null; // free memory
    final BufferedImage out;
    if( dest != null && dstWidth == dest.getWidth() && dstHeight == dest.getHeight() ) {
      out = dest;
      int nrDestChannels = ImageUtils.nrChannels( dest );
      if( nrDestChannels != nrChannels ) {
        final var errorMgs = format(
          "Destination image must be compatible width source image. Source " +
            "image had %d channels destination image had %d channels",
          nrChannels, nrDestChannels );
        throw new RuntimeException( errorMgs );
      }
    }
    else {
      out = new BufferedImage(
        dstWidth, dstHeight, getResultBufferedImageType( srcImg ) );
    }

    ImageUtils.setBGRPixels( outPixels, out, 0, 0, dstWidth, dstHeight );

    assert multipleInvocationLock.decrementAndGet() == 0 : "Multiple " +
      "concurrent invocations detected";

    return out;
  }

  private void waitForAllThreads( final Thread[] threads ) {
    try {
      for( final Thread thread : threads ) {
        thread.join( Long.MAX_VALUE );
      }
    } catch( final InterruptedException e ) {
      currentThread().interrupt();
      throw new RuntimeException( e );
    }
  }

  static SubSamplingData createSubSampling(
    ResampleFilter filter, int srcSize, int dstSize ) {
    final float scale = (float) dstSize / (float) srcSize;
    final int[] arrN = new int[ dstSize ];
    final int numContributors;
    final float[] arrWeight;
    final int[] arrPixel;

    final float fwidth = filter.getSamplingRadius();

    float centerOffset = 0.5f / scale;

    if( scale < 1.0f ) {
      final float width = fwidth / scale;
      // Add 2 to be safe with the ceiling
      numContributors = (int) (width * 2.0f + 2);
      arrWeight = new float[ dstSize * numContributors ];
      arrPixel = new int[ dstSize * numContributors ];

      final float fNormFac = (float) (1f / (Math.ceil( width ) / fwidth));

      for( int i = 0; i < dstSize; i++ ) {
        final int subindex = i * numContributors;
        float center = i / scale + centerOffset;
        int left = (int) Math.floor( center - width );
        int right = (int) Math.ceil( center + width );
        for( int j = left; j <= right; j++ ) {
          float weight;
          weight = filter.apply( (center - j) * fNormFac );

          if( weight == 0.0f ) {
            continue;
          }
          int n;
          if( j < 0 ) {
            n = -j;
          }
          else if( j >= srcSize ) {
            n = srcSize - j + srcSize - 1;
          }
          else {
            n = j;
          }
          int k = arrN[ i ];
          //assert k == j-left:String.format("%s = %s %s", k,j,left);
          arrN[ i ]++;
          if( n < 0 || n >= srcSize ) {
            weight = 0.0f;// Flag that cell should not be used
          }
          arrPixel[ subindex + k ] = n;
          arrWeight[ subindex + k ] = weight;
        }
        // normalize the filter's weight's so the sum equals to 1.0, very
        // important for avoiding box type of artifacts
        final int max = arrN[ i ];
        float tot = 0;
        for( int k = 0; k < max; k++ ) { tot += arrWeight[ subindex + k ]; }
        if( tot != 0f ) { // 0 should never happen except bug in filter
          for( int k = 0; k < max; k++ ) { arrWeight[ subindex + k ] /= tot; }
        }
      }
    }
    else {
      // super-sampling
      // Scales from smaller to bigger height
      numContributors = (int) (fwidth * 2.0f + 1);
      arrWeight = new float[ dstSize * numContributors ];
      arrPixel = new int[ dstSize * numContributors ];
      //
      for( int i = 0; i < dstSize; i++ ) {
        final int subindex = i * numContributors;
        float center = i / scale + centerOffset;
        int left = (int) Math.floor( center - fwidth );
        int right = (int) Math.ceil( center + fwidth );
        for( int j = left; j <= right; j++ ) {
          float weight = filter.apply( center - j );
          if( weight == 0.0f ) {
            continue;
          }
          int n;
          if( j < 0 ) {
            n = -j;
          }
          else if( j >= srcSize ) {
            n = srcSize - j + srcSize - 1;
          }
          else {
            n = j;
          }
          int k = arrN[ i ];
          arrN[ i ]++;
          if( n < 0 || n >= srcSize ) {
            weight = 0.0f;// Flag that cell should not be used
          }
          arrPixel[ subindex + k ] = n;
          arrWeight[ subindex + k ] = weight;
        }
        // normalize the filter's weight's so the sum equals to 1.0, very
        // important for avoiding box type of artifacts
        final int max = arrN[ i ];
        float tot = 0;
        for( int k = 0; k < max; k++ ) { tot += arrWeight[ subindex + k ]; }
        assert tot != 0 : "should never happen except bug in filter";
        if( tot != 0f ) {
          for( int k = 0; k < max; k++ ) { arrWeight[ subindex + k ] /= tot; }
        }
      }
    }
    return new SubSamplingData( arrN, arrPixel, arrWeight, numContributors );
  }

  private void verticalFromWorkToDst( byte[][] workPixels, byte[] outPixels,
                                      int start, int delta ) {
    if( nrChannels == 1 ) {
      verticalFromWorkToDstGray(
        workPixels, outPixels, start, threadCount );
      return;
    }
    boolean useChannel3 = nrChannels > 3;
    for( int x = start; x < dstWidth; x += delta ) {
      final int xLocation = x * nrChannels;
      for( int y = dstHeight - 1; y >= 0; y-- ) {
        final int yTimesNumContributors =
          y * verticalSubsamplingData.numContributors;
        final int max = verticalSubsamplingData.arrN[ y ];
        final int sampleLocation = (y * dstWidth + x) * nrChannels;

        float sample0 = 0.0f;
        float sample1 = 0.0f;
        float sample2 = 0.0f;
        float sample3 = 0.0f;
        int index = yTimesNumContributors;
        for( int j = max - 1; j >= 0; j-- ) {
          int valueLocation = verticalSubsamplingData.arrPixel[ index ];
          float arrWeight = verticalSubsamplingData.arrWeight[ index ];
          sample0 += (workPixels[ valueLocation ][ xLocation ] & 0xff) * arrWeight;
          sample1 += (workPixels[ valueLocation ][ xLocation + 1 ] & 0xff) * arrWeight;
          sample2 += (workPixels[ valueLocation ][ xLocation + 2 ] & 0xff) * arrWeight;
          if( useChannel3 ) {
            sample3 += (workPixels[ valueLocation ][ xLocation + 3 ] & 0xff) * arrWeight;
          }

          index++;
        }

        outPixels[ sampleLocation ] = toByte( sample0 );
        outPixels[ sampleLocation + 1 ] = toByte( sample1 );
        outPixels[ sampleLocation + 2 ] = toByte( sample2 );

        if( useChannel3 ) {
          outPixels[ sampleLocation + 3 ] = toByte( sample3 );
        }
      }
    }
  }

  private void verticalFromWorkToDstGray(
    byte[][] workPixels, byte[] outPixels, int start, int delta ) {
    for( int x = start; x < dstWidth; x += delta ) {
      for( int y = dstHeight - 1; y >= 0; y-- ) {
        final int yTimesNumContributors =
          y * verticalSubsamplingData.numContributors;
        final int max = verticalSubsamplingData.arrN[ y ];
        final int sampleLocation = y * dstWidth + x;
        float sample0 = 0.0f;
        int index = yTimesNumContributors;

        for( int j = max - 1; j >= 0; j-- ) {
          int valueLocation = verticalSubsamplingData.arrPixel[ index ];
          float arrWeight = verticalSubsamplingData.arrWeight[ index ];
          sample0 += (workPixels[ valueLocation ][ x ] & 0xff) * arrWeight;

          index++;
        }

        outPixels[ sampleLocation ] = toByte( sample0 );
      }
    }
  }

  /**
   * Apply filter to sample horizontally from Src to Work
   */
  private void horizontallyFromSrcToWork(
    BufferedImage srcImg, byte[][] workPixels, int start, int delta ) {
    if( nrChannels == 1 ) {
      horizontallyFromSrcToWorkGray( srcImg, workPixels, start, delta );
      return;
    }

    // Used if we work on int based bitmaps, later used to keep channel values
    final int[] tempPixels = new int[ srcWidth ];
    // create reusable row to minimize memory overhead
    final byte[] srcPixels = new byte[ srcWidth * nrChannels ];
    final boolean useChannel3 = nrChannels > 3;

    for( int k = start; k < srcHeight; k = k + delta ) {
      ImageUtils.getPixelsBGR( srcImg, k, srcWidth, srcPixels, tempPixels );

      for( int i = dstWidth - 1; i >= 0; i-- ) {
        int sampleLocation = i * nrChannels;
        final int max = horizontalSubsamplingData.arrN[ i ];

        float sample0 = 0.0f;
        float sample1 = 0.0f;
        float sample2 = 0.0f;
        float sample3 = 0.0f;
        int index = i * horizontalSubsamplingData.numContributors;
        for( int j = max - 1; j >= 0; j-- ) {
          float arrWeight = horizontalSubsamplingData.arrWeight[ index ];
          int pixelIndex =
            horizontalSubsamplingData.arrPixel[ index ] * nrChannels;

          sample0 += (srcPixels[ pixelIndex ] & 0xff) * arrWeight;
          sample1 += (srcPixels[ pixelIndex + 1 ] & 0xff) * arrWeight;
          sample2 += (srcPixels[ pixelIndex + 2 ] & 0xff) * arrWeight;
          if( useChannel3 ) {
            sample3 += (srcPixels[ pixelIndex + 3 ] & 0xff) * arrWeight;
          }
          index++;
        }

        workPixels[ k ][ sampleLocation ] = toByte( sample0 );
        workPixels[ k ][ sampleLocation + 1 ] = toByte( sample1 );
        workPixels[ k ][ sampleLocation + 2 ] = toByte( sample2 );
        if( useChannel3 ) {
          workPixels[ k ][ sampleLocation + 3 ] = toByte( sample3 );
        }
      }
    }
  }

  /**
   * Apply filter to sample horizontally from Src to Work
   */
  private void horizontallyFromSrcToWorkGray(
    BufferedImage srcImg, byte[][] workPixels, int start, int delta ) {
    // Used if we work on int based bitmaps, later used to keep channel values
    final int[] tempPixels = new int[ srcWidth ];
    // create reusable row to minimize memory overhead
    final byte[] srcPixels = new byte[ srcWidth ];

    for( int k = start; k < srcHeight; k = k + delta ) {
      ImageUtils.getPixelsBGR( srcImg, k, srcWidth, srcPixels, tempPixels );

      for( int i = dstWidth - 1; i >= 0; i-- ) {
        final int max = horizontalSubsamplingData.arrN[ i ];

        float sample0 = 0.0f;
        int index = i * horizontalSubsamplingData.numContributors;
        for( int j = max - 1; j >= 0; j-- ) {
          float arrWeight = horizontalSubsamplingData.arrWeight[ index ];
          int pixelIndex = horizontalSubsamplingData.arrPixel[ index ];

          sample0 += (srcPixels[ pixelIndex ] & 0xff) * arrWeight;
          index++;
        }

        workPixels[ k ][ i ] = toByte( sample0 );
      }
    }
  }

  private static byte toByte( final float f ) {
    if( f < 0 ) {
      return 0;
    }

    return (byte) (f > MAX_CHANNEL_VALUE ? MAX_CHANNEL_VALUE : f + 0.5f);
  }

  protected int getResultBufferedImageType( BufferedImage srcImg ) {
    return nrChannels == 3
      ? TYPE_3BYTE_BGR
      : nrChannels == 4
      ? TYPE_4BYTE_ABGR
      : srcImg.getSampleModel().getDataType() == TYPE_USHORT
      ? TYPE_USHORT_GRAY
      : TYPE_BYTE_GRAY;
  }
}
