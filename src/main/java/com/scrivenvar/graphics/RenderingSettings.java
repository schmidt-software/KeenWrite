/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivenvar.graphics;

import java.util.Map;

import static java.awt.RenderingHints.*;

/**
 * Responsible for supplying consistent rendering hints throughout the
 * application, such as image rendering for {@link SVGRasterizer}.
 */
public class RenderingSettings {

  /**
   * Shared hints for high-quality rendering.
   */
  public final static Map<Object, Object> RENDERING_HINTS = Map.of(
      KEY_ANTIALIASING,
      VALUE_ANTIALIAS_ON,
      KEY_ALPHA_INTERPOLATION,
      VALUE_ALPHA_INTERPOLATION_QUALITY,
      KEY_COLOR_RENDERING,
      VALUE_COLOR_RENDER_QUALITY,
      KEY_DITHERING,
      VALUE_DITHER_DISABLE,
      KEY_FRACTIONALMETRICS,
      VALUE_FRACTIONALMETRICS_ON,
      KEY_INTERPOLATION,
      VALUE_INTERPOLATION_BICUBIC,
      KEY_RENDERING,
      VALUE_RENDER_QUALITY,
      KEY_STROKE_CONTROL,
      VALUE_STROKE_PURE,
      KEY_TEXT_ANTIALIASING,
      VALUE_TEXT_ANTIALIAS_ON
  );

  /**
   * Prevent instantiation as per Joshua Bloch's recommendation.
   */
  private RenderingSettings() {
  }
}
