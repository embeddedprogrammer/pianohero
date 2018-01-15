package pianohero;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import midiexamples.MidiCommon;

public class MidiConnection implements Receiver
{
   MidiDevice inputDevice;
   
   public MidiConnection(int pIndex) throws MidiUnavailableException
   {
      this(MidiCommon.getMidiDeviceInfo(1));
   }
   
   public MidiConnection(MidiDevice.Info info) throws MidiUnavailableException
   {
      mReceiverList = new ArrayList<NoteReceiver>();
      inputDevice = MidiSystem.getMidiDevice(info);
      inputDevice.open();
      Transmitter tr = inputDevice.getTransmitter();
      tr.setReceiver(this);
   }
   
   List<NoteReceiver> mReceiverList;
   
   public void addReceiver(NoteReceiver pReceiver)
   {
      mReceiverList.add(pReceiver);      
   }
   
	public static final int NOTE_ON = 0x90;
	public static final int NOTE_OFF = 0x80;
	public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
   
   public void send(MidiMessage message, long lTimeStamp)
   {
      if (message instanceof ShortMessage)
      {
         Platform.runLater(new RunnableMessage((ShortMessage)message)
         {
            @Override
            public void run()
            {
            	// Print stuff
//					ShortMessage sm = shortMessage;
//					System.out.print("Channel: " + sm.getChannel() + " ");
//					if (sm.getCommand() == NOTE_ON) {
//					    int key = sm.getData1();
//					    int octave = (key / 12)-1;
//					    int note = key % 12;
//					    String noteName = NOTE_NAMES[note];
//					    int velocity = sm.getData2();
//					    System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
//					} else if (sm.getCommand() == NOTE_OFF) {
//					    int key = sm.getData1();
//					    int octave = (key / 12)-1;
//					    int note = key % 12;
//					    String noteName = NOTE_NAMES[note];
//					    int velocity = sm.getData2();
//					    System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
//					} else {
//					    System.out.println("Command:" + sm.getCommand());
//					}
            	
            	// Do stuff
               if(shortMessage.getCommand() == 0x80 || 
                     //(shortMessage.getCommand() == 0x90) && (shortMessage.getData2() == 0))               		
                     (shortMessage.getCommand() == 0x90) && (shortMessage.getData2() == 64)) //64 is a hack to make it work on our old piano
               {
                  noteOff(shortMessage.getData1());
               }
               else if (shortMessage.getCommand() == 0x90)
   
               {
                  noteOn(shortMessage.getData1());
               }
            }
         });
      }
		else
		{
			System.out.println("Other message: " + message.getClass());
		}
	}
   
   public void noteOff(int mKeyNumber)
   {
      for(NoteReceiver r : mReceiverList)
      {
         r.noteOff(mKeyNumber);
      }
   }
   
   public void noteOn(int mKeyNumber)
   {
      for(NoteReceiver r : mReceiverList)
      {
         r.noteOn(mKeyNumber);
      }
   }
   
   @Override
   public void close()
   {
      if(inputDevice != null)
      {
         inputDevice.close();
         System.out.println("Closed.");
      }
   }
}
