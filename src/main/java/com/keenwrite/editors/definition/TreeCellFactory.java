/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.definition;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Objects;

import static com.keenwrite.io.MediaType.APP_JAVA_OBJECT;
import static javafx.scene.input.TransferMode.MOVE;

/**
 * Responsible for producing {@link TreeCell} instances that can be edited
 * and respond to drag and drop functionality.
 */
public class TreeCellFactory
    implements Callback<TreeView<String>, TreeCell<String>> {
  private static final String STYLE_CLASS_DROP_TARGET = "drop-target";
  private static final DataFormat JAVA_FORMAT =
      new DataFormat( APP_JAVA_OBJECT.toString() );

  private TreeItem<String> mDraggedTreeItem;
  private TreeCell<String> mTargetCell;

  /**
   * Constructs a new {@link TreeCell} manufacturing facility called when
   * a new {@link TreeItem} is added to one of the editor's {@link TreeView}s.
   */
  public TreeCellFactory() {
  }

  @Override
  public TreeCell<String> call( final TreeView<String> treeView ) {
    final var cell = createTreeCell();

    cell.setOnDragDetected( event -> dragDetected( event, cell ) );
    cell.setOnDragOver( event -> dragOver( event, cell ) );
    cell.setOnDragDropped( event -> dragDropped( event, cell, treeView ) );
    cell.setOnDragDone( event -> dragClear() );

    return cell;
  }

  private TreeCell<String> createTreeCell() {
    return new FocusAwareTextFieldTreeCell( createStringConverter() ) {
      @Override
      public void commitEdit( final String newValue ) {
        super.commitEdit( newValue );
        //mEditor.select( getTreeItem() );
        requestFocus();
      }
    };
  }

  private StringConverter<String> createStringConverter() {
    return new StringConverter<>() {
      @Override
      public String toString( final String object ) {
        return sanitize( object );
      }

      @Override
      public String fromString( final String string ) {
        return sanitize( string );
      }

      private String sanitize( final String string ) {
        return string == null ? "" : string;
      }
    };
  }

  /**
   * Drag start.
   *
   * @param event    The drag start {@link MouseEvent}.
   * @param treeCell The cell being dragged.
   */
  private void dragDetected(
      final MouseEvent event, final TreeCell<String> treeCell ) {
    final var sourceItem = treeCell.getTreeItem();

    // Prevent dragging the root item.
    if( sourceItem != null && sourceItem.getParent() != null ) {
      final var dragboard = treeCell.startDragAndDrop( MOVE );
      final var clipboard = new ClipboardContent();
      clipboard.put( JAVA_FORMAT, sourceItem.getValue() );
      dragboard.setContent( clipboard );
      dragboard.setDragView( treeCell.snapshot( null, null ) );
      event.consume();

      mDraggedTreeItem = sourceItem;
    }
  }

  /**
   * Drag over another {@link TreeCell} instance.
   *
   * @param event    The drag over {@link DragEvent}.
   * @param treeCell The cell dragged over.
   * @throws IllegalStateException Drag transfer "move" mode denied.
   */
  private void dragOver(
      final DragEvent event, final TreeCell<String> treeCell ) {
    if( event.getDragboard().hasContent( JAVA_FORMAT ) ) {
      final var thisItem = treeCell.getTreeItem();

      if( mDraggedTreeItem == null ||
          thisItem == null ||
          thisItem == mDraggedTreeItem ) {
        return;
      }

      // Ignore dragging over the root item.
      if( mDraggedTreeItem.getParent() == null ) {
        dragClear();
        return;
      }

      event.acceptTransferModes( MOVE );

      if( !Objects.equals( mTargetCell, treeCell ) ) {
        dragClear();
        mTargetCell = treeCell;
        mTargetCell.getStyleClass().add( STYLE_CLASS_DROP_TARGET );
      }
    }
  }

  /**
   * Dragged item is dropped
   *
   * @param event    The drag dropped {@link DragEvent}.
   * @param treeCell The cell dropped onto.
   */
  private void dragDropped( final DragEvent event,
                            final TreeCell<String> treeCell,
                            final TreeView<String> treeView ) {
    if( !event.getDragboard().hasContent( JAVA_FORMAT ) ) {
      return;
    }

    final var sourceItem = mDraggedTreeItem;
    final var sourceItemParent = mDraggedTreeItem.getParent();
    final var targetItem = treeCell.getTreeItem();
    final var targetItemParent = targetItem.getParent();

    sourceItemParent.getChildren().remove( sourceItem );

    final ObservableList<TreeItem<String>> children;
    final int index;

    // Dropping onto a parent node makes the source item the first child.
    if( Objects.equals( sourceItemParent, targetItem ) ) {
      children = targetItem.getChildren();
      index = 0;
    }
    else if( targetItemParent != null) {
      children = targetItemParent.getChildren();
      index = children.indexOf( targetItem ) + 1;
    }
    else {
      children = sourceItemParent.getChildren();
      index = 0;
    }

    children.add( index, sourceItem );

    treeView.getSelectionModel().clearSelection();
    treeView.getSelectionModel().select( sourceItem );

    // TODO: Notify a listener of the old and new tree item position.

    event.setDropCompleted( true );
  }

  private void dragClear() {
    final var targetCell = mTargetCell;

    if( targetCell != null ) {
      targetCell.getStyleClass().remove( STYLE_CLASS_DROP_TARGET );
    }
  }
}
