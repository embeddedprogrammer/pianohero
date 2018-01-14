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
   
   public void send(MidiMessage message, long lTimeStamp)
   {
      if (message instanceof ShortMessage)
      {
         Platform.runLater(new RunnableMessage((ShortMessage)message)
         {
            @Override
            public void run()
            {
               if(shortMessage.getCommand() == 0x80 || 
                     (shortMessage.getCommand() == 0x90) && (shortMessage.getData2() == 0))
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
