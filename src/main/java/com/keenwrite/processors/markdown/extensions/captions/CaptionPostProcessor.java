/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.keenwrite.processors.markdown.extensions.fences.ClosingDivBlock;
import com.keenwrite.processors.markdown.extensions.fences.OpeningDivBlock;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import org.jetbrains.annotations.NotNull;

/**
 * Captions are written most naturally <em>after</em>> the element that they
 * apply to, regardless of whether they are figures, tables, code listings,
 * algorithms, or equations. The typesetting software uses event-based parsing
 * of XML elements, meaning the DOM isn't fully loaded into memory. This means
 * that captions must come <em>before</em> the item being captioned.
 * <p>
 * To reconcile this UX conundrum, we swap captions with the previous node.
 */
class CaptionPostProcessor extends NodePostProcessor {
  @Override
  public void process(
    @NotNull final NodeTracker state,
    @NotNull final Node caption ) {

    var previous = caption.getPrevious();

    if( previous != null ) {
      swap( previous, caption );
    }
  }

  private void swap( final Node previous, final Node caption ) {
    assert previous != null;
    assert caption != null;

    var swap = previous;
    boolean found = true;

    if( swap.isOrDescendantOfType( ClosingDivBlock.class ) ) {
      found = false;

      while( !found && swap != null ) {
        if( swap.isOrDescendantOfType( OpeningDivBlock.class ) ) {
          found = true;
        }
        else {
          swap = swap.getPrevious();
        }
      }
    }

    if( found ) {
      swap.insertBefore( caption );
    }
  }
}
