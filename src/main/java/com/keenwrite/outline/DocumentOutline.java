package com.keenwrite.outline;

import com.keenwrite.events.Bus;
import com.keenwrite.events.ParseHeadingEvent;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.greenrobot.eventbus.Subscribe;

import static com.keenwrite.events.Bus.register;

public class DocumentOutline extends TreeView<String> {
  /**
   * Registers with the {@link Bus}.
   */
  public DocumentOutline() {
    register( this );
  }

  @Subscribe
  public void handle( final ParseHeadingEvent event ) {
    if( event.isNewOutline() ) {
      clear();
    }
    else {
      addItem( event );
    }
  }

  private void clear() {
    setRoot( new TreeItem<>( "Document" ) );
  }

  private void addItem( final ParseHeadingEvent event ) {
    getRoot().getChildren().add( new TreeItem<>( event.toString() ) );
  }
}
