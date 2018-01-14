package midiexamples;

import java.util.List;
import java.util.Vector;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author ericjbruno
 */
public class Divider extends Rectangle {
    // The width of the invisible divider between the controls
    static final double SEP_WIDTH = 2;

    // A static counter for IDs
    static int DIVIDER_ID_INDEX = 1;
    
    // The unique ID of this divider
    public int id = DIVIDER_ID_INDEX++;

    
    // The node this divider controls
    public final Control control;
    
    // All nodes left of our control
    Vector<Control> leftControls = new Vector<Control>();
    
    // All nodes right of our control
    Vector<Control> rightControls = new Vector<Control>();

    // The parent HBox container
    HBox parent;
    
    // When true, will never resize any controls except for the one directly
    // to the left of the divider (the controlling node)
    boolean preserveResize = false; 
    
    // When true, will maintain size of left and right controls, except when
    // dragging right, where all right most controls will be reset to share space
    boolean preserveAdajacent = true; 
    
    // Set to true when user drags to the right
    boolean draggingRight = false; 
    
    // This is a flag that is reset with each mouse click to indicate
    // first time processing each time the user clicks on a divider
    boolean oneTimeProc = false;
    
    public Divider(HBox parent, Control c) {
        this.control = c;
        this.parent = parent;

        setWidth(SEP_WIDTH);
        setStroke(Color.TRANSPARENT);
        setFill(Color.TRANSPARENT);
        
        setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                setCursor(Cursor.W_RESIZE);
            }
        });
        
        setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                setCursor(Cursor.DEFAULT);
            }
        });
        
        setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                processChildren();
                
                // Lock the lock components
                for ( Control control: leftControls ) {
                    double w = control.getWidth();
                    control.setMinWidth(w);
                    control.setMaxWidth(w);
                }
                
                oneTimeProc = false;
            }
        });
        
        setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                draggingRight = false;
                
                // A positive relative X position indicates the user 
                // is dragging to the right. Do this once per click
                if ( oneTimeProc == false && me.getX() >= 1 ) {
                    draggingRight = true;
                    oneTimeProc = true;
                }
                
                if ( draggingRight && ! preserveResize ) {
                    for ( Control control: rightControls ) {
                        double w = control.getWidth();
                        control.setMinWidth(Control.USE_COMPUTED_SIZE);
                        control.setMaxWidth(Control.USE_COMPUTED_SIZE);
                    }
                }
                
                double newWidth = control.getWidth() + me.getX();
                if ( newWidth > control.getWidth() ) {
                    control.setMaxWidth(newWidth);
                    control.setMinWidth(newWidth);
                }
                else {
                    control.setMinWidth(newWidth);
                    control.setMaxWidth(newWidth);
                }
            }
        });
        
        setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                draggingRight = false;
            }
        });
        
        // Set this divider's height to always equal its parent's height
        parent.heightProperty().addListener(
            new ChangeListener() {
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    Double height = (Double)newValue - 1;
                    setHeight(height);
                }
            });
    }
    
    private void processChildren() {
        boolean addLock = true;
        List<Node> children = parent.getChildren();
        for ( Node node: children ) {
            if ( node == control ) {
                addLock = false;
            }
            else if ( node instanceof Control ) {
                if ( addLock )
                    leftControls.add((Control)node);
                else
                    rightControls.add((Control)node);
            }
        }
      
    }
}
