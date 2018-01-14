package pianohero;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Note
{
   private int mKeyNumber;
   private int mNote;
   private int mOctave;
   private boolean mIsBlack;
   private double mKeyboardPosition;
   private int mStaffPosition;
   private Node mNode;
   private Staff mStaff;
   
   public Note(int pKeyNumber)
   {
      mKeyNumber = pKeyNumber;
      mNote = mKeyNumber % 12;
      mOctave = mKeyNumber / 12;
      mIsBlack = (mNote == 1) || (mNote == 3) || (mNote == 6) || (mNote == 8) || (mNote == 10);
      mStaffPosition = (mNote + ((mNote >= 5) ? 1 : 0)) / 2 + (mOctave * 7);
      mKeyboardPosition = ((double)(mNote + ((mNote >= 5) ? 1 : 0))) / 2 + (mOctave * 7);
   }
   
   public int getStaffPosition()
   {
      return mStaffPosition;
   }
   
   public double getKeyboardPosition()
   {
      return mKeyboardPosition;
   }
   
   public boolean getIsBlack()
   {
      return mIsBlack;
   }
   
   public int getmNote()
   {
      return mNote;
   } 
   
   public int getmOctave()
   {
      return mOctave;
   }
   
   public Node getNode()
   {
      return mNode;
   }
   
   public void setNode(Node pNode)
   {
      mNode = pNode;
   }
   
   public Staff getStaff()
   {
      return mStaff;
   }
   
   public void setStaff(Staff pStaff)
   {
      mStaff = pStaff;
   }
}
