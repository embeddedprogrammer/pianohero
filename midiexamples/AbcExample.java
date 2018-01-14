package midiexamples;

import javax.swing.JFrame;

import abc.notation.Tune;
import abc.parser.TuneParser;
import abc.ui.swing.JScoreComponent;

public class AbcExample
{
   public static void main (String[] arg)
   {
        String tuneAsString = "X:0\nT:A simple scale exercise\nK:D\nCDEFGABcdefggfedcBAGFEDC\n";
        Tune tune = new TuneParser().parse(tuneAsString);
        JScoreComponent scoreUI =new JScoreComponent();
        scoreUI.setTune(tune);
        JFrame j = new JFrame();
        j.add(scoreUI);
        j.pack();
        j.setVisible(true);
   }
}
