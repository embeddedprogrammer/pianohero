package pianohero;

import javax.sound.midi.ShortMessage;

public class RunnableMessage implements Runnable 
{
   ShortMessage shortMessage;
   
   public RunnableMessage(ShortMessage pShortMessage)
   {
      shortMessage = pShortMessage;
   }
   
   @Override
   public void run()
   {
      
   }
}
