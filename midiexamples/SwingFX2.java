package midiexamples;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.javafx.application.PlatformImpl;

public class SwingFX2 extends JPanel // It's a Swing JPanel 
{  
   private JFXPanel jfxPanel;          // The JavaFX component(s)
   private JButton swingButton;        // The Swing component

   public SwingFX2()
   { 
       initComponents(); 
   } 
  
   public static void main(String ...args)
   { 
       SwingUtilities.invokeLater(new Runnable() { 
           public void run() 
           { 
               // Create a Swing Frame
               final JFrame frame = new JFrame(); 
               frame.setMinimumSize(new Dimension(640, 480)); 
               frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
                 
               // Add the Swing JPanel and make visible
               frame.getContentPane().add(new SwingFX2()); 
               frame.setVisible(true); 
           } 
       });    
   } 
     
   private void initComponents()
   {
       // The JavaFX 2.x JFXPanel makes the Swing integration seamless
       jfxPanel = new JFXPanel();

       // Create the JavaFX Scene
       createScene();
         
       setLayout(new BorderLayout()); 
       add(jfxPanel, BorderLayout.CENTER);

       // Swing
       swingButton = new JButton(); 
       swingButton.addActionListener(
           new ActionListener()
           { 
               @Override
               public void actionPerformed(java.awt.event.ActionEvent ae)
               {
                   System.exit(0);
               }
           }); 
       swingButton.setText("Close");         
       add(swingButton, BorderLayout.SOUTH); 
   }    
     
   private void createScene() 
   { 
       // The Scene needs to be created on "FX user thread", NOT on the
       // AWT Event Thread
       PlatformImpl.startup(
           new Runnable() {
               public void run() { 
                   Group root = new Group(); 
                   Scene scene = new Scene(root, 80, 20); 
                   SizeView sizeview = createSizeView(scene);
                   root.getChildren().add(sizeview);
                   jfxPanel.setScene(scene); 
               } 
           }); 
   }
   
   private SizeView createSizeView(Scene scene) 
   {
       double HBOX_WIDTH = scene.getWidth() - 10 - 10;

       final SizeView sizeview = new SizeView();
       sizeview.setLayoutX(10);
       sizeview.setLayoutY(10);
       sizeview.setPrefWidth(HBOX_WIDTH);
        
       Node[] controls = new Node[5];
       for ( int l = 0; l < controls.length; l++ ) {
           controls[l] = new ListView();
       }

       sizeview.getChildren().setAll(controls);
        
       /*scene.widthProperty().addListener(
           new ChangeListener() {
               public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                   Double w = (Double)newValue;
                   sizeview.setPrefWidth( w - 20 );
                   sizeview.layout();
               }
           });*/
        
       return sizeview;
   }
}