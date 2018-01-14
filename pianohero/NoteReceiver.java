package pianohero;

public interface NoteReceiver 
{
   public void noteOff(int pKeyNumber);
   public void noteOn(int pKeyNumber);
}
