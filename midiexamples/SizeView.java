package midiexamples;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 *
 * @author ericjbruno
 */
public class SizeView extends HBox {
    boolean ignoreChanges = false;
        
    public SizeView() {
        setManaged(true);
        
        // Listen for changes to the pane's child nodes
        ObservableList<Node> children =  this.getChildren();
        children.addListener(new ListChangeListener<Node>() {
            public void onChanged(ListChangeListener.Change change) {
                if ( ignoreChanges )
                    return;
                       
                ObservableList<Node> c = getChildren();
                if ( c != null && c.size() > 0 ) {
                    Control[] cs = new Control[ c.size() ];
                    cs = (Control[])c.toArray(cs);
                    divide(cs);
                }
            }
        });
    }
    
    protected void divide(Control[] controls) {
        // There needs to be at least one control in the array
        if ( controls.length == 0 )
            return;
        
        ignoreChanges = true;

        // Place each control (with a divider) into the hbox
        for ( int i = controls.length-1; i > 0;  i-- ) {
            Control control = controls[i];
            if ( i > 0 ) {
                // Add a divider first, and assign it the control
                // to the left to resize when moved
                Divider div = new Divider( this, controls[i-1] );
                div.setHeight( this.getHeight() );
                getChildren().add( i, div );
            }

            HBox.setHgrow(control, Priority.ALWAYS);
        }
        
        ignoreChanges = false;
    }
}
