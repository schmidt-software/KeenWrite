package com.keenwrite.ui.outline;

import com.keenwrite.events.Bus;
import com.keenwrite.events.CaretNavigationEvent;
import com.keenwrite.events.ParseHeadingEvent;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.greenrobot.eventbus.Subscribe;

import static com.keenwrite.events.Bus.register;
import static com.keenwrite.ui.fonts.IconFactory.createGraphic;
import static javafx.application.Platform.runLater;
import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

public class DocumentOutline extends TreeView<ParseHeadingEvent> {
  private TreeItem<ParseHeadingEvent> mCurrent;

  /**
   * Registers with the {@link Bus}.
   */
  public DocumentOutline() {
    // Override double-click to issue a caret navigation event.
    setCellFactory( new Callback<>() {
      @Override
      public TreeCell<ParseHeadingEvent> call(
        TreeView<ParseHeadingEvent> treeView ) {
        TreeCell<ParseHeadingEvent> cell = new TreeCell<>() {
          @Override
          protected void updateItem( ParseHeadingEvent item, boolean empty ) {
            super.updateItem( item, empty );
            if( empty || item == null ) {
              setText( null );
              setGraphic( null );
            }
            else {
              setText( item.toString() );
              setGraphic( createIcon() );
            }
          }
        };

        cell.addEventFilter( MOUSE_PRESSED, event -> {
          if( event.getButton() == PRIMARY && event.getClickCount() % 2 == 0 ) {
            CaretNavigationEvent.fire( cell.getItem().getOffset() );
            event.consume();
          }
        } );

        return cell;
      }
    } );

    register( this );
  }

  /**
   * Updates the {@link TreeView} with the given event data. This method will
   * track the most recently added {@link TreeItem} so that the nesting
   * hierarchy reflects the document hierarchy.
   *
   * @param event Represents a document heading to add to the tree.
   */
  @Subscribe
  public void handle( final ParseHeadingEvent event ) {
    runLater(
      () -> mCurrent = event.isNewOutline() ? clear( event ) : addItem( event )
    );
  }

  private TreeItem<ParseHeadingEvent> clear( final ParseHeadingEvent event ) {
    final var root = createTreeItem( event );
    setRoot( root );
    setShowRoot( false );
    return root;
  }

  /**
   * This method is called once for every heading in the document. The event
   * data directly corresponds to the sequence of headings in the document.
   * The given event data contains a level that is relative to the last
   * item in the tree.
   *
   * @param next Contains a level value to indicate heading depth.
   */
  private TreeItem<ParseHeadingEvent> addItem( final ParseHeadingEvent next ) {
    var parent = mCurrent;
    final var item = createTreeItem( next );
    final var curr = parent.getValue();
    final var currLevel = curr.getLevel();
    final var nextLevel = next.getLevel();
    var deltaLevel = currLevel - nextLevel + 1;

    while( deltaLevel > 0 && parent != null ) {
      parent = parent.getParent();
      deltaLevel--;
    }

    if( parent == null ) {
      parent = getRoot();
    }

    parent.getChildren().add( item );

    return item;
  }

  private TreeItem<ParseHeadingEvent> createTreeItem(
    final ParseHeadingEvent event ) {
    final var item = new TreeItem<>( event, createIcon() );
    item.setExpanded( true );
    return item;
  }

  private Node createIcon() {
    return createGraphic( "BOOKMARK" );
  }
}
