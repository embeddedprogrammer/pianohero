package pianohero;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;

public class Staff
{
   Pane mPane;
   
   //Image i;
   List<Line> lines;
   List<Note> notes;
   double mMinX;
   double mMinY;
   double mMaxX;
   double mMaxY;
   
   // (note position from middle C, higher note means higher value)
   int mMaxNotePosition; 
   int mMinNotePosition;
   public final static int TREBLE_MIN_NOTE_POSITION = 37; 
   public final static int BASS_MIN_NOTE_POSITION = -10 + 7;
   public final static double MIDDLE_A_FREQ = 440;
   
   public Staff(double pMinX, double pMinY, double pMaxX, double pMaxY, Pane pPane, int pMinPitch)
   {
      mPane = pPane;
      mMinX = pMinX;
      mMinY = pMinY;
      mMaxX = pMaxX;
      mMaxY = pMaxY;
      mMinNotePosition = pMinPitch;
      lines = new ArrayList<Line>();
      notes = new ArrayList<Note>();
      for(int i = 0; i < 5; i++)
      {
         Line l = new Line();
         double y = mMinY + (mMaxY - mMinY) / 4 * i;
         l.setStartX(mMinX);
         l.setStartY(y);
         l.setEndX(mMaxX);
         l.setEndY(y);
         mPane.getChildren().add(l);
         lines.add(l);
      }
      /*mPane.setOnMouseClicked(new EventHandler<MouseEvent>()
      {
         @Override
         public void handle(MouseEvent me)
         {
            Circle circle = new Circle();
            circle.setRadius(5);
            circle.setTranslateX(me.getX());
            circle.setTranslateY(me.getY());
            mPane.getChildren().add(circle);
         }
      });*/
   }
   
   public void addNote(double pX,  int pKeyNumber)
   {
      Note n = new Note(pKeyNumber);
      int nNote = pKeyNumber % 12;
      int nOctave = pKeyNumber / 12;
      int notePosition = (nNote + ((nNote >= 5) ? 1 : 0)) / 2 + (nOctave * 7);
      boolean isBlack = (nNote == 1) || (nNote == 3) || (nNote == 6) || (nNote == 8) || (nNote == 10);
      boolean sharp = isBlack;
      System.out.println(((char)(65 + ((notePosition + 2) % 7))) + (sharp ? "#" : ""));


      double y = mMaxY - (mMaxY - mMinY) / 8 * (n.getStaffPosition() - mMinNotePosition);
      Ellipse circle = new Ellipse();
      circle.setFill(Color.BLACK);
      //circle.setStroke(Color.BLACK);
      circle.setRadiusX((mMaxY - mMinY) / 8 * 1.1);
      circle.setRadiusY((mMaxY - mMinY) / 8);
      circle.setTranslateX(mMinX + pX);
      circle.setTranslateY(y);
      System.out.println(mMinX + pX);
      System.out.println(y);
      mPane.getChildren().add(circle);
      //notes.add(circle);
      

   }
   
   public void clearNotes()
   {
      for(int i = 0; i < mPane.getChildren().size(); i++)
      {
         Node n =  mPane.getChildren().get(i);
         if(n instanceof Ellipse)
         {
            mPane.getChildren().remove(n);
            i--;
         }
      }
   }
   
   /*public void addNote2(double pX, double pY)
   {
      Note n = new Note();
      n.getNode().setTranslateX(mMinX + pX);
      n.getNode().setTranslateY(mMinY + pY);
      mPane.getChildren().add(n.getNode());
      notes.add(n);
   }*/
}