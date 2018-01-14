package pianohero;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class PianoKeyboard implements NoteReceiver
{
   final int FIRST_NOTE = 21;
   final int LAST_NOTE = 108;
   private KeyboardKey[] keys;
   List<NoteReceiver> mReceiverList;
	
   public PianoKeyboard(Pane pPane, double offsetY)
   {
      mReceiverList = new ArrayList<NoteReceiver>();
      keys = new KeyboardKey[LAST_NOTE + 1];
      //Do first key to set relative positioning
      keys[0] = new KeyboardKey(FIRST_NOTE, pPane, offsetY, 0);
      double offsetX = -keys[0].mKeyNode.getTranslateX();
      keys[0].mKeyNode.setTranslateX(0);      
      for(int nKeyNumber = FIRST_NOTE + 1; nKeyNumber <= LAST_NOTE; nKeyNumber++)
      {
         keys[nKeyNumber] = new KeyboardKey(nKeyNumber, pPane, offsetY, offsetX);
      }
   }
   
   // Use this for java.awt integration. Returns the default scene,
   // with standard midi setup. This can be integrated into a JFXPanel.  
	public static Scene createDefaultScene()
	{
		Group root = new Group();
		Scene scene = new Scene(root, 1040, 80);
		Pane pane = new Pane();
		instance = new PianoKeyboard(pane, 0);
		root.getChildren().add(pane);
		return scene;
	}
	
   private static PianoKeyboard instance;
	
	public static PianoKeyboard getInstance()
	{
		return instance;
	}
   
   public double getWidth()
   {
      return keys[LAST_NOTE].mKeyNode.getTranslateX() + keys[LAST_NOTE].mKeyNode.getWidth();
   }
   
   public double getHeight()
   {
      return KeyboardKey.WHITE_KEY_LENGTH;
   }
   
   public void addReciever(NoteReceiver pReceiver)
   {
      mReceiverList.add(pReceiver);      
   }
   
   public void noteOff(int pKeyNumber)
   {
      keys[pKeyNumber].noteOff();
   }
   
   public void noteOn(int pKeyNumber)
   {
      keys[pKeyNumber].noteOn();
   }
   
   public class KeyboardKey
   {
      static final double WHITE_KEY_LENGTH = 80;
      static final double BLACK_KEY_LENGTH = 50;
      static final double WHITE_KEY_WIDTH = 20;
      static final double BLACK_KEY_WIDTH = 12;
      static final double BLACK_KEY_OFFSET = 2;
   	
      private Rectangle mKeyNode;
      private int mKeyNumber;
      private boolean mIsBlack;
      
      public KeyboardKey(int pKeyNumber, Pane pPane, double offsetY, double offsetX)
      {
         mKeyNumber = pKeyNumber;
         int nNote = mKeyNumber % 12;
         int nOctave = mKeyNumber / 12;
         //return sm_astrKeyNames[nNote] + (nOctave - 1);
         mIsBlack = (nNote == 1) || (nNote == 3) || (nNote == 6) || (nNote == 8) || (nNote == 10);
         double keyWidth = mIsBlack ? BLACK_KEY_WIDTH : WHITE_KEY_WIDTH;
         double keyLength = mIsBlack ? BLACK_KEY_LENGTH : WHITE_KEY_LENGTH;
         Color keyColor = mIsBlack ? Color.BLACK : Color.WHITE;
         double keyPosition = ((double)(nNote + ((nNote >= 5) ? 1 : 0))) / 2 + (nOctave * 7);
         double keyOffsetX = mIsBlack ? ((double)((nNote >= 5) ? (nNote - 8) : (nNote - 2))) / 2 * BLACK_KEY_OFFSET : 0;
         double keyCenterX = (keyPosition + .5) * WHITE_KEY_WIDTH + keyOffsetX;
         mKeyNode = new Rectangle();
         mKeyNode.setWidth(keyWidth);
         mKeyNode.setHeight(keyLength);
         mKeyNode.setFill(keyColor);
         mKeyNode.setStroke(pKeyNumber == 60 ? Color.BLUE : Color.BLACK);
         mKeyNode.setTranslateX(keyCenterX - keyWidth / 2 + offsetX);
         mKeyNode.setTranslateY(offsetY);
         mKeyNode.setId("" + mKeyNumber);
         pPane.getChildren().add(mKeyNode);
         if(mIsBlack)
            mKeyNode.toFront();
         else
            mKeyNode.toBack();
         mKeyNode.setOnMousePressed(new EventHandler<MouseEvent>()
         {
            @Override
            public void handle(MouseEvent arg0)
            {
               noteOn();
            }
         });
         mKeyNode.setOnMouseReleased(new EventHandler<MouseEvent>()
         {
            @Override
            public void handle(MouseEvent arg0)
            {
               noteOff();
            }
         });
      }
      
      public void noteOff()
      {
         mKeyNode.setFill(mIsBlack ? Color.BLACK : Color.WHITE);
         for(NoteReceiver r : mReceiverList)
         {
            r.noteOff(mKeyNumber);
         }
      }
      
      public void noteOn()
      {
         mKeyNode.setFill(Color.BLUE);
         for(NoteReceiver r : mReceiverList)
         {
            r.noteOn(mKeyNumber);
         }
      }
   }   
}
