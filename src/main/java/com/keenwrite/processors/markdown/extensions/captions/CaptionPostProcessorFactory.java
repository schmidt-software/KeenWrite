/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.keenwrite.processors.markdown.extensions.common.MarkdownPostProcessorFactory;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.util.ast.Document;

class CaptionPostProcessorFactory extends MarkdownPostProcessorFactory {
  CaptionPostProcessorFactory() {
    // The argument isn't used by the Markdown parsing library.
    super( false );

    addNodes( CaptionBlock.class );
  }

  @Override
  protected NodePostProcessor createPostProcessor( final Document document ) {
    return new CaptionPostProcessor();
  }
}
