package pianohero;

import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;

import javax.imageio.ImageIO;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;

import midiexamples.MidiCommon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

import javax.swing.JFrame;

import abc.notation.Tune;
import abc.parser.TuneParser;
import abc.ui.swing.JScoreComponent;


/**
 * A sample that demonstrates various mouse and scroll events and their usage.
 * Click the circles and drag them across the screen. Scroll the whole screen.
 * All events are logged to the console.
 * 
 * @see javafx.scene.Cursor
 * @see javafx.scene.input.MouseEvent
 * @see javafx.event.EventHandler
 */
public class TestStaff extends Application implements NoteReceiver {

   private Pane pane;
   
   private Staff staff;
   
   MidiConnection mc;
 
   private void init(Stage primaryStage)
   {
      pane = new Pane();
//      pane.setOnKeyPressed(new EventHandler<KeyEvent>()
//      {
//         @Override public void handle(KeyEvent ke)
//         {
//            System.out.println(MouseInfo.getPointerInfo().getLocation());
//         }
//      });
      PianoKeyboard pk = new PianoKeyboard(pane, 100);
      pk.addReciever(this); //Connect to it to show events
      
      staff = new Staff(20, 20, 420, 60, pane, Staff.TREBLE_MIN_NOTE_POSITION);
      /*staff.addNote(20, 2);
      staff.addNote(40, 3);
      staff.addNote(60, 4);
      staff.addNote(20, 5);
      staff.addNote(40, 6);
      staff.addNote(60, 7);
      staff.addNote(20, 8);
      staff.addNote(40, 9);
      staff.addNote(60, 10);*/
      primaryStage.setResizable(true);
      primaryStage.setScene(new Scene(pane, 500, 500, true));
//      score = "";
//      Thread t = new Thread()
//      {
//         public void run()
//         {
//            while(true)
//            {
//               try
//               {
//                  Thread.sleep(300);
//               }
//               catch(Exception e)
//               {
//                  
//               }
//               score = score + " ";
//               x += 10;
//            }
//         }
//      };
//      t.start();
      pane.requestFocus();
      
      //GameController gc =  new GameController(staff);
      //pk.addReciever(gc); //Connect this to play game.
      //gc.startGame();
      
      //MidiCommon.listDevicesAndExit(true, false);
      try 
      {
         mc = new MidiConnection(1);
         mc.addReceiver(pk);
         System.out.println("Running");
      }
      catch (MidiUnavailableException e)
      {
         //e.printStackTrace();
         System.out.println("Failed to get device.");
      }
      primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
         @Override
         public void handle(WindowEvent event)
         {
            System.out.println("Close request handled.");
            if(mc != null)
            {
               mc.close();
               System.out.println("Midi connection closed");
            }
            Platform.exit();
         }
     });
      
      showNotes();
   }
   
   @Override
   public void noteOff(int pKeyNumber)
   {
      // TODO Auto-generated method stub
      
   }
   
   double x = 10;
   String score;

   @Override
   public void noteOn(int pKeyNumber)
   {
      /*
      staff.addNote(x, pKeyNumber);
      
      int nNote = pKeyNumber % 12;
      int nOctave = pKeyNumber / 12;
      int notePosition = (nNote + ((nNote >= 5) ? 1 : 0)) / 2 + (nOctave * 7);
      boolean isBlack = (nNote == 1) || (nNote == 3) || (nNote == 6) || (nNote == 8) || (nNote == 10);
      boolean sharp = isBlack;
      score = score + ((char)(65 + ((notePosition + 2) % 7))) + (sharp ? "#" : "");
      
      */
      
      // TODO Auto-generated method stub
      //showNotes();
   }
   
   public void showNotes()
   {
        String tuneAsString = "X:0\nT:A simple scale exercise\nK:D\nCDEFGABcdefggfedcBAGFEDC\n";
        Tune tune = new TuneParser().parse(tuneAsString);
        JScoreComponent scoreUI = new JScoreComponent();
        scoreUI.setTune(tune);
        JFrame j = new JFrame();
        j.add(scoreUI);
        j.pack();
        j.setVisible(true);
   }

   @Override
   public void start(Stage primaryStage) throws Exception {
      init(primaryStage);
      primaryStage.show();
   }

   public static void main(String[] args) {
      launch(args);
   }

}

