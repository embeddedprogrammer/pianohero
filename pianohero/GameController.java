package pianohero;

import java.util.Random;

public class GameController implements NoteReceiver
{
   Staff mStaff;
   
   // Shows results 
   public GameController(Staff pStaff)
   {
      r = new Random();
      mStaff = pStaff;
   }
   
   int note;
   Random r;
   
   public void displayRandomNote()
   {
      note = r.nextInt(12) + 60;
      System.out.println("note #" + note);
      mStaff.addNote(20, note);
   }
   
   @Override
   public void noteOff(int pKeyNumber)
   {
      if(pKeyNumber == note)
      {
         mStaff.clearNotes();
         displayRandomNote();
         // correct
      }
      else
      {
         // incorrect
         System.out.println("Incorrect. Key was " + note + ". You pressed key " + pKeyNumber);
      }
      // TODO Auto-generated method stub
      
   }

   @Override
   public void noteOn(int pKeyNumber)
   {
      // No need to do anything      
   }

   public void startGame()
   {
      displayRandomNote();
   }

}
