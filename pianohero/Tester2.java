package pianohero;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import com.sun.javafx.application.PlatformImpl;
import com.sun.org.apache.bcel.internal.generic.StoreInstruction;

import abc.notation.Accidental;
import abc.notation.BarLine;
import abc.notation.EndOfStaffLine;
import abc.notation.KeySignature;
import abc.notation.MultiNote;
import abc.notation.MusicElement;
import abc.notation.MusicElementReference;
import abc.notation.NoteAbstract;
import abc.notation.NotesSeparator;
import abc.notation.TimeSignature;
import abc.notation.Tune;
import abc.notation.Note;
import abc.notation.Voice;
import abc.parser.TuneParser;
import abc.ui.swing.Engraver;
import abc.ui.swing.JGroupOfNotes;
import abc.ui.swing.JNote;
import abc.ui.swing.JNoteElementAbstract;
import abc.ui.swing.JScoreComponent;
import abc.ui.swing.JScoreElement;
import abc.ui.scoretemplates.DefaultScoreTemplate;
import abc.ui.scoretemplates.ScoreAttribute;
import abc.ui.swing.ScoreTemplate;
import abc.ui.swing.JScoreElementAbstract;

// This should really be called MainClass or PianoHero or something instead of Tester2
public class Tester2 implements NoteReceiver
{
	private JFXPanel jfxPianoPanel; // The JavaFX component(s)

	public Tester2(JPanel panel)
	{
		instance = this;
		jpanel = panel;
		initComponents();
	}

	private static Tester2 instance;

	public static Tester2 getInstance()
	{
		return instance;
	}

	private static JFrame frame;

	public static void main(String... args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// Create a Swing Frame
				frame = new JFrame();
				frame.setMinimumSize(new Dimension(1040 + 20, 470));
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setTitle("Piano Hero");

				// Add the Swing JPanel and make visible
				JPanel jp = new JPanel();
				frame.getContentPane().add(jp);
				Tester2 tester = new Tester2(jp);
				frame.setVisible(true);
			}
		});
	}

	// ****************************** INIT METHODS ******************************

	Tune trebleTune;
	Tune bassTune;
	boolean useSharps;
	JScoreComponent trebleScoreUI;
	JScoreComponent bassScoreUI;
	ScoreTemplate scoreTemplate;

	JPanel jpanel;

	private void initComponents()
	{
		initScores();

		jfxPianoPanel = new JFXPanel();
		// The Scene needs to be created on "FX user thread", NOT on the
		// AWT Event Thread
		PlatformImpl.startup(new Runnable()
		{
			public void run()
			{
				initializePianoPanel();
			}
		});

		// Layout
		jpanel.setBackground(Color.WHITE);
		FlowLayout fl = new FlowLayout();
		fl.setVgap(0);
		jpanel.setLayout(fl);

		jpanel.add(new ControlBar());
		addNewLine();
		jpanel.add(trebleScoreUI);
		addNewLine();
		jpanel.add(bassScoreUI);
		addNewLine();
		jpanel.add(jfxPianoPanel);
	}

	public void initScores()
	{
		// Set Attributes
		scoreTemplate = new DefaultScoreTemplate();
		scoreTemplate.setAttributeSize(ScoreAttribute.NOTATION_SIZE, 80);
		// scoreTemplate.setAttributeSize(ScoreAttribute.NOTE_SPACING, 10f);
		scoreTemplate.getEngraver().setMode(Engraver.DEFAULT);
		// scoreTemplate.setAttributeSize(ScoreAttribute.MARGIN_TOP, 100);
		// scoreTemplate.setAttributeSize(ScoreAttribute.MARGIN_BOTTOM, 100);

		trebleScoreUI = new JScoreComponent();
		bassScoreUI = new JScoreComponent();
		// Swing
		// String tuneAsString =
		// "X:0\nT:A simple scale exercise\nK:D\nCDEFGABcdefggfedcBAGFEDC\n";
		// String tuneAsString =
		// "X:0\nT:A simple scale exercise\nM:4/4\nK:D\n([^CD][EF]|G)A Bc|de fg-|gf ed|cB A(G|FE DC)\n";
		// =
		// "X:0\nT:A simple scale exercise\nM:4/4\nK:D clef=bass\n(^CD EF|G)A Bc|de fg-|gf ed|cB A(G|FE DC)\n";
		// String tuneAsString =
		// "X:0\nT:A simple scale exercise\nM:4/4\nK:D\nabc\nK:D clef=bass\nM:4/4\nABC\n";
		// tuneAsString = "X:0\nT:Music\nM:4/4\nK:D \nabcxd";
		// tuneAsString =
		// "M:4/4\nK:D clef=treble\n(^CD EF|G)A Bc|de fg-|gf ed|cB A(G|FE DC)";

		loadTuneFromString(Staff.TREBLE, "M:4/4\nK:Bb \nb[_aA] f^f|cxd\n");
		loadTuneFromString(Staff.BASS, "M:4/4\nK:Bb clef=bass\n[A,C,]B, F,G,|C,xD,\n");
		magicMakeStaffsLineUp();

		trebleScoreUI.addMouseListener(new MouseInputAdapter()
		{
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e)
			{
				clickStaff(Staff.TREBLE, e);
			}
		});
		bassScoreUI.addMouseListener(new MouseInputAdapter()
		{
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e)
			{
				clickStaff(Staff.BASS, e);
			}
		});
		frame.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent ke)
			{
				keyPress(ke);
			}
		});
	}

	// ****************************** EVENTS ******************************

	File currentFile;

	public void openFile()
	{
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(jpanel);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			currentFile = fileChooser.getSelectedFile();
			readScoreFromFile();
		}
	}

	public void saveFile()
	{
		if (currentFile != null)
			writeScoreToFile();
		else
			saveFileAs();
	}

	public void saveFileAs()
	{
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(jpanel);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			currentFile = fileChooser.getSelectedFile();
			writeScoreToFile();
		}
	}
	
	public void writeScoreToFile()
	{
		try
		{
			FileWriter fileWriter = new FileWriter(currentFile);
			fileWriter.write(saveTuneToString(Staff.TREBLE) + "\r" + saveTuneToString(Staff.BASS) + "\r");
			fileWriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void readScoreFromFile()
	{
		try
		{
			Scanner scanner = new Scanner(currentFile).useDelimiter("\r");
			loadTuneFromString(Staff.TREBLE, scanner.next());
			loadTuneFromString(Staff.BASS, scanner.next());
			scanner.close();
			magicMakeStaffsLineUp();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static final int RND_SEQUENCE_LENGTH = 15;
	private static final int RND_NOTES_PER_CHORD = 1;
	private static final int TREBLE_CENTER_LINE = 70;
	private static final int BASS_CENTER_LINE = 50;
	private static final int MAX_RND_DIST_FROM_CENTER = 14;

	public int getCenterLine(Staff staff)
	{
		return (staff == Staff.TREBLE) ? TREBLE_CENTER_LINE : BASS_CENTER_LINE;
	}

	public void generateRandomScore()
	{
		print("Generating random score");
		//if (getRandomBool())
		//generateRandomNotes();
		//else
		//	generateRandomChords();
		generateRandomScales();
	}

	public void generateRandomNotes()
	{
		clearRndScore();
		for(int i = 0; i < RND_SEQUENCE_LENGTH; i++)
		{
			for(int j = 0; j < RND_NOTES_PER_CHORD; j++)
			{
				Staff staff = getRandomBool() ? Staff.TREBLE : Staff.BASS;
				int pKeyNumber = getRandom(
						getCenterLine(staff) - MAX_RND_DIST_FROM_CENTER, 
						getCenterLine(staff) + MAX_RND_DIST_FROM_CENTER);
				if(j == 0 ||
					(findNote(getVoice(Staff.TREBLE).size() - 2, pKeyNumber, Staff.TREBLE) == null &&
					findNote(getVoice(Staff.BASS).size() - 2, pKeyNumber, Staff.BASS) == null))
					addNoteToEndOfTune(pKeyNumber, staff, j > 0);
			}
		}
		magicMakeStaffsLineUp();
	}

	private static final int[] CHORD_MAJOR = { 0, 4, 7 };
	private static final int[] CHORD_MINOR = { 0, 3, 7 };
	private static final int[] CHORD_AUG = { 0, 4, 8 };
	private static final int[] CHORD_DIM = { 0, 3, 6 };
	private static final int[] CHORD_6TH = { 0, 4, 7, 9 };
	private static final int[] CHORD_7TH = { 0, 4, 7, 10 };
	private static final int[][] CHORDS = { CHORD_MAJOR, CHORD_MINOR, CHORD_AUG, CHORD_DIM, CHORD_6TH, CHORD_7TH };
	private static final String[] SHARP_KEYS = { "C", "G", "D", "A"};//, "E", "B" };    // , "F#", "C#"};
	private static final String[] FLAT_KEYS = { "C", "F", "Bb", "Eb"};//, "Ab", "Db" }; // , "Gb", "Cb"};

	//
	// add octave
	// drop note

	public void clearRndScore()
	{
		useSharps = getRandomBool();
		if (useSharps)
			clearScore(SHARP_KEYS[getRandom(0, SHARP_KEYS.length - 1)]);
		else
			clearScore(FLAT_KEYS[getRandom(0, FLAT_KEYS.length - 1)]);

	}

	public void generateRandomChords()
	{
		clearRndScore();
		for (int i = 0; i < RND_SEQUENCE_LENGTH; i++)
			addRandomChord();
		magicMakeStaffsLineUp();
	}

	public void addRandomChord()
	{
		int[] chord = generateRandomChord();
		Staff staff = getRandomBool() ? Staff.TREBLE : Staff.BASS;
		int chordStartingNote = getRandom(getCenterLine(staff)
				- MAX_RND_DIST_FROM_CENTER, getCenterLine(staff)
				+ MAX_RND_DIST_FROM_CENTER - chord[chord.length - 1]);
		for (int j = 0; j < chord.length; j++)
		{
			int pKeyNumber = chordStartingNote + chord[j];
			addNoteToEndOfTune(pKeyNumber, staff, j > 0);
		}
	}

	public int[] generateRandomChord()
	{
		int[] chord = CHORDS[getRandom(0, CHORDS.length - 1)].clone();
		if (getRandomBool()) // Optionally invert
			chord = invertChord(chord, getRandom(0, 3));
		if (getRandomBool()) // Optionally add octave
			chord = addElement(chord, 12);
		if (getRandomBool() || (chord.length > 4)) // Optionally drop note
			chord = removeElement(chord, getRandom(0, chord.length - 1));
		return chord;
	}

	public String printChord(int[] chord)
	{
		String text = "";
		for (int i = 0; i < chord.length; i++)
			text += chord[i] + " ";
		return text;
	}

	// TODO: I'm pretty sure this isn't called inverting the chord, it is called something else.
	public int[] invertChord(int[] chord, int numOfTimes)
	{
		// for each specified note, raise by an octave
		numOfTimes = (numOfTimes % chord.length);
		for (int i = 0; i < numOfTimes; i++)
			chord[i] += 12;
		int[] newChord = new int[chord.length];

		// shift chord and subtract so that the lowest note is still 0.
		// I suppose these chords are still generic; they aren't yet assigned
		// to a specific note.
		for (int i = 0; i < chord.length; i++)
			newChord[i] = chord[(i + numOfTimes) % chord.length];
		int lowestNote = newChord[0];
		for (int i = 0; i < chord.length; i++)
			newChord[i] -= lowestNote;
		return newChord;
	}

	public int[] removeElement(int[] input, int position)
	{
		int[] result = new int[input.length - 1];
		int j = 0;
		for (int i = 0; i < input.length; i++)
			if (i != position)
				result[j++] = input[i];
		return result;
	}

	public int[] addElement(int[] input, int element)
	{
		int[] result = new int[input.length + 1];
		for (int i = 0; i < input.length; i++)
			result[i] = input[i];
		result[input.length] = element;
		return result;
	}

	private static final int[] SCALE_MAJOR = { 0, 2, 4, 5, 7, 9, 11, 12 };

	public void generateRandomScales()
	{
		clearRndScore();
		for (int i = 0; i < RND_SEQUENCE_LENGTH / 7; i++)
		{
			int[] chord = SCALE_MAJOR;
			Staff staff = getRandomBool() ? Staff.TREBLE : Staff.BASS;
			int chordStartingNote = getRandom(
					getCenterLine(staff) - MAX_RND_DIST_FROM_CENTER, 
					getCenterLine(staff) + MAX_RND_DIST_FROM_CENTER - chord[chord.length - 1]);
			for(int j = 0; j < chord.length; j++)
			{
				int pKeyNumber = chordStartingNote + chord[j];
				addNoteToEndOfTune(pKeyNumber, staff, false);
			}
		}
		magicMakeStaffsLineUp();
	}

	public int getRandom(int min, int max)
	{
		return (int) (min + (Math.random() * (max - min + 1)));
	}

	public boolean getRandomBool()
	{
		return (Math.random() > .5);
	}

	// ****************************** EVENTS ******************************

	public void keyPress(KeyEvent ke)
	{
		if (ControlBar.getInstance().selectedButton == ControlBar.getInstance().editButton)
		{
			if (ke.getKeyCode() == KeyEvent.VK_DELETE)
			{
				deleteSelectedNote();
			}
			else if (ke.getKeyCode() == KeyEvent.VK_UP)
			{
				moveSelectedNoteVertically(1);
			}
			else if (ke.getKeyCode() == KeyEvent.VK_DOWN)
			{
				moveSelectedNoteVertically(-1);
			}
			else if (ke.getKeyCode() == KeyEvent.VK_RIGHT)
			{
				moveSelectedNoteHorizontally(1);
			}
			else if (ke.getKeyCode() == KeyEvent.VK_LEFT)
			{
				moveSelectedNoteHorizontally(-1);
			}
		}
		else if (ControlBar.getInstance().selectedButton == ControlBar
				.getInstance().recordButton)
		{

		}
		else if (ControlBar.getInstance().selectedButton == ControlBar
				.getInstance().gameButton)
		{

		}

	}

	MusicElement selectedMusicElement = null;
	MusicElement selectedMultiMusicElement = null;
	JScoreElement selectedJScoreElement = null;

	enum Staff
	{
		TREBLE, BASS
	};

	Staff selectedStaff;

	public void m2()
	{
		print(getScoreUI(Staff.TREBLE).getJTune().getScoreElements().toString());
		print("Positions:");
		for (int i = 0; i < getVoice(Staff.TREBLE).size(); i++)
		{
			MusicElement me = (MusicElement) getVoice(Staff.TREBLE).get(i);
			JScoreElementAbstract jme = (JScoreElementAbstract) findJScoreElement(
					me, Staff.TREBLE);
			if (jme == null)
				print("element " + me.toString() + " with ref " + me.getReference() + " not found");
			else
			{
				print("element " + me.toString() + " with ref " + me.getReference() 	+ " found:");
				print("    " + jme.toString() + " at " + jme.getBase());
			}

		}
	}

	public void magicMakeStaffsLineUp()
	{
		double lastx1 = 0;
		double lastx2 = 0;
		double x = 0;
		for (int i = 2; i < getVoice(Staff.TREBLE).size(); i++)
		{
			MusicElement me1 = (MusicElement)getVoice(Staff.TREBLE).get(i);
			MusicElement me2 = (MusicElement)getVoice(Staff.BASS).get(i);
			JScoreElementAbstract jme1 = (JScoreElementAbstract)findJScoreElement(me1, Staff.TREBLE);
			JScoreElementAbstract jme2 = (JScoreElementAbstract)findJScoreElement(me2, Staff.BASS);
			if(jme1 != null && jme2 != null)
			{
				Point2D b1 = jme1.getBase();
				Point2D b2 = jme2.getBase();
				// double curx1 = b1.getX();
				// double curx2 = b2.getX();
				// double wid1 = curx1 - lastx1;
				// double wid2 = curx2 - lastx2;
				// x = Math.max(Math.max(curx1, curx2),
				// Math.max(lastx1 + wid1, lastx2 + wid2));
				// lastx1 = curx1;
				// lastx2 = curx2;
				if (i == 2)
					x = Math.max(b1.getX(), b2.getX());
				b1.setLocation(x, b1.getY());
				b2.setLocation(x, b2.getY());
				jme1.setBase(b1);
				jme2.setBase(b2);
				x += Math.max(jme1.getWidth(), jme2.getWidth()) + scoreTemplate.getAttributeSize(ScoreAttribute.NOTE_SPACING);
			}
		}
		getScoreUI(Staff.TREBLE).setWidthWithMargin(x);
		getScoreUI(Staff.BASS).setWidthWithMargin(x);
		frame.revalidate();
	}

	public void clickStaff(Staff staff, java.awt.event.MouseEvent e)
	{
		if (e.getButton() == 3)
		{
			magicMakeStaffsLineUp();
			// Point2D base = selectedJScoreElement.getBase();
			// base.setLocation(e.getPoint().x, base.getY());
			// ((JScoreElementAbstract)selectedJScoreElement).setBase(base);
			// getScoreUI(staff).setBufferedImageOutdated(true);
			// getScoreUI(staff).repaint();

		}
		print(e.getPoint());
		if (ControlBar.getInstance().selectedButton == ControlBar.getInstance().editButton)
		{
			if (selectedJScoreElement != null)
			{
				selectedJScoreElement.setColor(Color.BLACK);
				getSelectedScoreUI().repaint();
			}
			selectedStaff = staff;
			selectedJScoreElement = getSelectedScoreUI().getScoreElementAt(
					e.getPoint());
			if (selectedJScoreElement != null)
			{
				print(selectedJScoreElement);
				selectedJScoreElement.setColor(Color.GREEN);
				// We grab these references now, because if the notes are selected and
				// dragged, we want to rely on the music element, not the outdated JScoreElement.
				selectedMusicElement = getOriginalMusicElement(selectedJScoreElement);
				selectedMultiMusicElement = getOriginalMultiMusicElement(selectedJScoreElement);
				print("Index: " + getMusicElementIndex(selectedMusicElement)); 
				if(selectedMusicElement instanceof Note && !((Note)selectedMusicElement).isRest())
					print("Key Number:" + getKeyNumber((Note)selectedMusicElement, getMusicElementIndex(selectedMusicElement)));
				getSelectedScoreUI().repaint();
				ControlBar.getInstance().ptsLabel.setText("element at " + selectedJScoreElement.getBase());
			}
			frame.requestFocus();
		}
	}

	public void buttonToggle(ControlBar.CustomToggleButton button)
	{
		gameIndexWithinScore = 2;
		reloadBothScores();
		if (button == ControlBar.getInstance().gameButton)
		{
			pts = 0;
			timeStartedPlaying = new Date().getTime();
			wrong = 0;
			right = 0;
			addToPts(0);
		}
	}

	@Override
	public void noteOff(int pKeyNumber)
	{

	}

	@Override
	public void noteOn(int pKeyNumber)
	{
		// print(pKeyNumber);
		long timeStamp = new Date().getTime();
		boolean combineWithLast = timeStamp - lastTimePressed < syncThresholdInMs;
		if (ControlBar.getInstance().selectedButton == ControlBar.getInstance().recordButton)
		{
//			Note n = getNoteFromKeyNumber(pKeyNumber);
//			Staff staff = (pKeyNumber > 60) ? Staff.TREBLE : Staff.BASS;
//			int index;
//			// if(selectedMultiMusicElement == null)
//			index = getVoice(staff).size() - 2;
//			// else
//			// index = getSelectedElementX();
//			addNoteToTune(staff, n, index, combineWithLast);
//			if (!combineWithLast)
//				addRest(getOppositeStaff(staff), index + 1);
			addNoteToEndOfTune(pKeyNumber, (pKeyNumber > 60) ? Staff.TREBLE : Staff.BASS, combineWithLast);
			lastTimePressed = timeStamp;
		}
		else if (ControlBar.getInstance().selectedButton == ControlBar.getInstance().editButton)
		{
			if (selectedMusicElement == null || !(selectedMusicElement instanceof Note))
				return;

			Voice v = getSelectedVoice();
			Note note = (Note) selectedMusicElement;
			if (note.isRest())
				return;

			setNoteKeyNumber(note, pKeyNumber, getMusicElementIndex(selectedMusicElement));
			reloadScoreUI(selectedStaff);
			magicMakeStaffsLineUp();
		}
		else if (ControlBar.getInstance().selectedButton == ControlBar.getInstance().gameButton)
		{
			// If user presses a key in less than 100ms, they are probably playing a
			// note already played in the measure a second time!
			if (combineWithLast)
				return;
			int ptsDiff = addResults(
					findNoteAndAct(gameIndexWithinScore, pKeyNumber, Staff.TREBLE),
					findNoteAndAct(gameIndexWithinScore, pKeyNumber, Staff.BASS));
			if (ptsDiff == -1)
				soundNote(pKeyNumber);
			addToPts(ptsDiff);

			while (isGroupPlayed(gameIndexWithinScore, Staff.TREBLE)
					&& isGroupPlayed(gameIndexWithinScore, Staff.BASS))
			{
				gameIndexWithinScore++;
				if (gameIndexWithinScore >= getVoice(Staff.TREBLE).size()
						|| gameIndexWithinScore >= getVoice(Staff.BASS).size())
					advanceScore();
				lastTimePressed = timeStamp;
			}
		}
	}

	public void soundNote(int pKeyNumber)
	{
		try
		{
			final int SAMPLE_RATE = 16 * 1024; // ~16KHz
			final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
			SourceDataLine line = AudioSystem.getSourceDataLine(af);

			line.open(af, SAMPLE_RATE);
			line.start();

			int ms = 500;
			int length = SAMPLE_RATE * ms / 1000;

			byte[] data = new byte[length];

			double exp = ((double) pKeyNumber - 60 + 3 - 12) / 12d;
			double hz = 440d * Math.pow(2d, exp);
			double period = (double) SAMPLE_RATE / hz;
			print("note: " + hz + " cps, " + period + " ms period");
			for (int i = 0; i < length; i++)
			{
				double angle = 2.0 * Math.PI * i / period;
				int amplitude = (int) (Math.sin(angle) * 16f); // Before was 127f
																				// (quite loud)
				amplitude = amplitude % 8; // add distortion
				data[i] = (byte) (amplitude);
			}

			int count = line.write(data, 0, length);
			line.drain();
			line.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void advanceScore()
	{
		generateRandomScore();
	}

	int pts;
	long timeStartedPlaying;
	int wrong;
	int right;

	public void addToPts(int diff)
	{
		pts += diff;
		if (diff == 1)
			right++;
		else if (diff == -1)
			wrong++;
		ControlBar.getInstance().setPts(pts);
		ControlBar.getInstance().setAccuracy(
				((double) right) / (((double) wrong) + ((double) right)));
		double timespanInMin = ((double) (new Date().getTime() - timeStartedPlaying)) / 60 / 1000;
		ControlBar.getInstance().setTime(timespanInMin);
		ControlBar.getInstance().setSpeed(((double) right) / timespanInMin);
	}

	List<String> trebleScoreList;
	List<String> bassScoreList;

	int gameIndexWithinScore;
	int gameIndexOfScore;

	public void moveToLineInMusic(int newIndex, Staff staff)
	{
		getScoreList(staff).set(gameIndexOfScore, saveTuneToString(staff));
		gameIndexOfScore = newIndex;
		loadTuneFromString(staff, getScoreList(staff).get(newIndex));
	}

	public List<String> getScoreList(Staff staff)
	{
		return (staff == Staff.TREBLE) ? trebleScoreList : bassScoreList;
	}

	public int addResults(Result r1, Result r2)
	{
		if (r1 == Result.RIGHT || r2 == Result.RIGHT)
			return 1;
		else if (r1 == Result.WRONG && r2 == Result.WRONG)
			return -1;
		else
			return 0;
	}

	enum Result
	{
		WRONG, RIGHT, ALREADY_PLAYED
	}

	public Result findNoteAndAct(int index, int pKeyNumber, Staff staff)
	{
		Note note = findNote(index, pKeyNumber, staff);
		if (note == null)
			return Result.WRONG;
		if (!note.isPlayed())
		{
			note.setPlayed(true);
			findJNote(note, staff).setColor(Color.GREEN);
			getScoreUI(staff).repaint();
			return Result.RIGHT;
		}
		return Result.ALREADY_PLAYED;
	}

	public boolean isGroupPlayed(int index, Staff staff)
	{
		MusicElement me = (MusicElement) getVoice(staff).get(index);
		if (me instanceof Note)
			return ((Note) me).isPlayed() || ((Note) me).isRest();
		if (me instanceof MultiNote)
			for (Note n : (Vector<Note>) ((MultiNote) me).getNotes())
				if (!n.isPlayed() && !n.isRest())
					return false;
		return true;
	}

	public JNote findJNote(Note note, Staff staff)
	{
		// print(note.getReference());
		return (JNote) getScoreUI(staff).getJTune().getRenditionObjectFor(
				note.getReference());
	}

	public JScoreElement findJScoreElement(MusicElement me, Staff staff)
	{
		// print(note.getReference());
		return getScoreUI(staff).getJTune().getRenditionObjectFor(
				me.getReference());
	}

	public Note findNote(int index, int pKeyNumber, Staff staff)
	{
		MusicElement me = (MusicElement) getVoice(staff).get(index);
		if (me instanceof Note)
		{
			if (!((Note) me).isRest() && getKeyNumber((Note) me, index) == pKeyNumber)
				return (Note) me;
		}
		else if (me instanceof MultiNote)
		{
			for (Note n : (Vector<Note>) ((MultiNote) me).getNotes())
			{
				if (getKeyNumber(n, index) == pKeyNumber)
					return n;
			}
		}
		return null;
	}

	// ****************************** PRINT FUNCTIONS ******************************

	public void print(Object s)
	{
		System.out.println(s);
	}

	public <E> void printJTune(Collection<E> c)
	{
		print(c.getClass() + " with " + c.size() + " items");
		for (Object o : c)
		{
			if (o instanceof Collection)
			{
				printCollection((Collection) o);
			}
			else if (o instanceof JGroupOfNotes)
			{
				printArray(((JGroupOfNotes) o).getJNotes());
			}
			else
			{
				print(o.getClass() + " - " + o.toString());
			}
		}
	}

	public <E> void printArray(Object[] c)
	{
		print(c.getClass() + " with " + c.length + " items");
		for (Object o : c)
		{
			if (o instanceof Collection)
			{
				printCollection((Collection) o);
			}
			else
			{
				print(o.getClass() + " - " + o.toString());
			}
		}
	}

	public <E> void printCollection(Collection<E> c)
	{
		print(c.getClass() + " with " + c.size() + " items");
		for (Object o : c)
		{
			if (o instanceof Collection)
			{
				printCollection((Collection) o);
			}
			else
			{
				print(o.getClass() + " - " + o.toString());
			}
		}
	}

	public void printVector(Vector<MusicElement> v)
	{
		print(v.getClass() + " with " + v.size() + " items");
		for (Object o : v)
		{
			if (o instanceof Vector<?>)
			{
				printVector((Vector<MusicElement>) o);
			}
			else if (o instanceof MusicElement)
			{
				print(o.getClass() + " - " + o.toString() + " - "
						+ ((MusicElement) o).getReference());
			}
		}
	}

	// Add a wide invisible line to the applet to force all the components onto
	// the next row.
	private void addNewLine()
	{
		Canvas line = new Canvas();
		line.setSize(10000, 1); // 10000 wide so it will always be wider than the
										// applet and force the line onto the next row.
		line.setBackground(jpanel.getBackground());
		// line.setBackground(Color.BLUE);
		jpanel.add(line);
	}

	public void initializePianoPanel()
	{
		jfxPianoPanel.setScene(PianoKeyboard.createDefaultScene());

		// Connect to it to show events
		PianoKeyboard.getInstance().addReciever(Tester2.getInstance());

		// GameController gc = new GameController(staff);
		// pk.addReciever(gc); //Connect this to play game.
		// gc.startGame();

		// MidiCommon.listDevicesAndExit(true, false);
		try
		{
			MidiConnection mc = new MidiConnection(1);
			mc.addReceiver(PianoKeyboard.getInstance());
			System.out.println("Running");
		}
		catch (MidiUnavailableException e)
		{
			// e.printStackTrace();
			System.out.println("Failed to get device.");
		}
	}

	// String tuneAsString = "X:0\nT:Keep playing...\nK:D\n";
	String trebleScore = "";
	String bassScore = "";

	long lastTimePressed;
	final long syncThresholdInMs = 150;
	final boolean REST_INVISIBLE = true;

	public Staff getOppositeStaff(Staff staff)
	{
		return Staff.values()[1 - staff.ordinal()];
	}

	// *************************** HIGH-LEVEL EDITING METHODS ***************************

	public void loadTuneFromString(Staff staff, String tuneAsString)
	{
		Tune tune = new TuneParser().parse(tuneAsString);
		if (staff == Staff.TREBLE)
			trebleTune = tune;
		else
			bassTune = tune;
		getScoreUI(staff).setTuneAndTemplate(tune, scoreTemplate);
		gameIndexWithinScore = 2;
	}

	public String saveTuneToString(Staff staff)
	{
		String score = "";
		for (MusicElement me : (Vector<MusicElement>) getVoice(staff))
		{
			score += getMusicElementRepresentation(me, staff);
		}
		return score;
	}

	public void deleteSelectedNote()
	{
		if (selectedMusicElement == null)
			return;
		else if (selectedMusicElement instanceof Note)
			removeNoteAtIndex(getSelectedVoice(), (Note) selectedMusicElement,
					getSelectedElementIndex());
		else
			removeElementAtIndex(getSelectedVoice(), getSelectedElementIndex());
		reloadScoreUI(selectedStaff);
		magicMakeStaffsLineUp();
		selectedMusicElement = null;
		selectedMultiMusicElement = null;
		selectedJScoreElement = null;
	}
	
	// TODO: When the note is moved between two consecutive locations,
	// the accidental of the note at it's previous location and the accidental
	// of the new note could interfere. Not sure what the best way to deal with
	// this is. For now we are just displaying a warning. 
	public void moveSelectedNoteVertically(int offset)
	{
		if (selectedMusicElement == null
				|| !(selectedMusicElement instanceof Note))
			return;

		Voice v = getSelectedVoice();
		Note note = (Note) selectedMusicElement;
		if (note.isRest())
			return;

		int pKeyNumber = getKeyNumber(note, getMusicElementIndex(selectedMusicElement));
		int idx = getSelectedElementIndex();
		pKeyNumber += offset;
		setNoteKeyNumber(note, pKeyNumber, getMusicElementIndex(selectedMusicElement));
		reloadScoreUI(selectedStaff);
		magicMakeStaffsLineUp();
	}

	public void moveSelectedNoteHorizontally(int offset)
	{
		if (selectedMusicElement == null
				|| !(selectedMusicElement instanceof Note))
		{
			print("exit 1");
			return;
		}
		Voice v = getSelectedVoice();
		MusicElement me = selectedMultiMusicElement;
		int idx = getSelectedElementIndex();
		removeElementAtIndex(v, idx);
		insertElementAtIndex(v, me, idx + offset);
		printVector(v);
		reloadScoreUI(selectedStaff);
		magicMakeStaffsLineUp();
	}

	public void addNoteToEndOfTune(int pKeyNumber, Staff staff, boolean combineWithLast)
	{
		if (!combineWithLast)
		{
			int counts = countCounts(staff);
			if (counts % 4 == 0 && counts > 0)
			{
				//print("" + counts + " -> bar line");
				addBarLine();
			}
			else if (counts % 2 == 0 && counts > 0)
			{
				// This keeps notes from joining together
				//print("" + counts + " -> separator");
				addNotesSeparator();
			}
		}

		int index = getVoice(staff).size() - (combineWithLast ? 2 : 1);
		Note n = getNoteFromKeyNumber(pKeyNumber, index);
		addNoteToTune(staff, n, index, combineWithLast);
		if (!combineWithLast)
			addRest(getOppositeStaff(staff));
		magicMakeStaffsLineUp();
	}

	public int countCounts(Staff staff)
	{
		int count = 0;
		for (MusicElement me : (Vector<MusicElement>) getVoice(staff))
		{
			if (!(me instanceof BarLine || me instanceof KeySignature
					|| me instanceof TimeSignature || me instanceof EndOfStaffLine || me instanceof NotesSeparator))
			{
				// print(me.getClass());
				count++;
			}
		}
		return count;
	}

	public void addNoteToTune(Staff staff, Note note, int index, boolean combineNote)
	{
		Voice v = getVoice(staff);
		if (combineNote && v.get(index) instanceof NoteAbstract)
			addNoteAtIndex(v, note, index);
		else
			insertElementAtIndex(v, note, index);
		reloadScoreUI(staff);
	}

	public void addRest(Staff staff)
	{
		Note n = new Note(Note.REST);
		n.setInvisibleRest(REST_INVISIBLE);
		appendElement(staff, n);
		reloadScoreUI(staff);
	}

	public void appendElement(Staff staff, MusicElement me)
	{
		insertElementAtIndex(getVoice(staff), me, getVoice(staff).size() - 1);
	}

	public void clearScore()
	{
		clearScore("D");
	}

	public void clearScore(String key)
	{
		loadTuneFromString(Staff.TREBLE, "M:4/4\nK:" + key + "\n");
		loadTuneFromString(Staff.BASS, "M:4/4\nK:" + key + " clef=bass\n");
		insertElementAtIndex(getVoice(Staff.TREBLE), new EndOfStaffLine(),
				getVoice(Staff.TREBLE).size());
		insertElementAtIndex(getVoice(Staff.BASS), new EndOfStaffLine(),
				getVoice(Staff.BASS).size());
		reloadBothScores();
	}

	public void addBarLine()
	{
		appendElement(Staff.TREBLE, new BarLine());
		appendElement(Staff.BASS, new BarLine());
		reloadBothScores();
	}

	public void addNotesSeparator()
	{
		appendElement(Staff.TREBLE, new NotesSeparator());
		appendElement(Staff.BASS, new NotesSeparator());
	}

	// *************************** LOW-LEVEL NOTE EDITING METHODS ***************************

	// Removes note at specified index. If no note is present, replaces with a rest.
	public void removeNoteAtIndex(Voice v, Note note, int index)
	{
		MusicElement me = (MusicElement) v.get(index);
		me = removeNote(me, note);
		replaceElementAtIndex(v, me, index);
	}

	public void addNoteAtIndex(Voice v, Note note, int index)
	{
		MusicElement me = (MusicElement) v.get(index);
		me = combineNote(me, note);
		replaceElementAtIndex(v, me, index);
	}

	// Removes note at specified index. If after the deletion, no
	// note remains, the note is replaced with a rest.
	public MusicElement removeNote(MusicElement me, Note note)
	{
		Vector<Note> notes;
		if (me instanceof MultiNote)
		{
			notes = ((MultiNote) me).getNotes();
			notes.remove(note);
			if (notes.size() == 1)
				return notes.get(0);
			else
				return new MultiNote(notes);
		}
		else if (me instanceof Note)
		{
			note = new Note(Note.REST);
			note.setInvisibleRest(REST_INVISIBLE);
			return note;
		}
		else
		{
			print("Error! Cannot remove note from class " + me.getClass());
			return null;
		}
	}

	public MusicElement combineNote(MusicElement me, Note note)
	{
		Vector<Note> notes;
		if (me instanceof MultiNote)
		{
			notes = ((MultiNote) me).getNotes();
			notes.add(note);
			return new MultiNote(notes);
		}
		else if (me instanceof Note)
		{
			Note originalNote = (Note) me;
			if (!originalNote.isRest())
			{
				notes = new Vector<Note>(2);
				notes.add(originalNote);
				notes.add(note);
				return new MultiNote(notes);
			}
			else
				return note;
		}
		else
		{
			print("Error! Cannot create a multinote from class " + me.getClass());
			return null;
		}
	}

	public void replaceElementAtIndex(Voice v, MusicElement newElement, int index)
	{
		v.remove(index);
		v.insertElementAt(newElement, index);
		newElement.getReference().setX((short) index);
		updateRef(newElement, index);
	}

	public void insertElementAtIndex(Voice v, MusicElement element, int index)
	{
		v.insertElementAt(element, index);
		for (int i = index; i < v.size(); i++)
			updateRef((MusicElement) v.get(i), i);
	}

	public void removeElementAtIndex(Voice v, int index)
	{
		v.remove(index);
		for (int i = index; i < v.size(); i++)
			updateRef((MusicElement) v.get(i), i);
	}

	public void updateRef(MusicElement element, int index)
	{
		element.getReference().setX((short) index);
		element.getReference().setY((byte) -1);
		if (element instanceof MultiNote)
		{
			Vector<Note> notes = ((MultiNote) element).getNotes();
			for (int y = 0; y < notes.size(); y++)
			{
				notes.get(y).getReference().setX((short) index);
				notes.get(y).getReference().setY((byte) (y + 1));
			}
		}
	}

	// ************************ MUSIC ELEMENT AND STAFF REFERENCE METHODS ************************

	public void reloadBothScores()
	{
		reloadScoreUI(Staff.TREBLE);
		reloadScoreUI(Staff.BASS);
		magicMakeStaffsLineUp();
	}

	public void reloadScoreUI(Staff staff)
	{
		getScoreUI(staff).setTuneAndTemplate(getTune(staff), scoreTemplate);
	}

	public int getSelectedElementIndex()
	{
		return selectedMusicElement.getReference().getX();
	}

	public int getSelectedElementY()
	{
		return selectedMusicElement.getReference().getY();
	}

	// Returns actual MusicElement, not a clone!
	public MusicElement getOriginalMultiMusicElement(JScoreElement element)
	{
		MusicElementReference ref = element.getMusicElement().getReference();
		int x = ref.getX();
		if (x == -1)
			return null;
		return (MusicElement) getSelectedVoice().get(x);
	}
	
	// Returns actual note, not a clone!
	public MusicElement getOriginalMusicElement(JScoreElement element)
	{
		MusicElementReference ref = element.getMusicElement().getReference();
		int x = ref.getX();
		if (x == -1)
			return null;
		MusicElement me = (MusicElement) getSelectedVoice().get(x);
		int y = ref.getY();
		if (me instanceof MultiNote && y != -1)
			return (MusicElement) ((MultiNote) me).getNotes().get(y - 1);
		else
			return me;
	}
	
	public int getMusicElementIndex(JScoreElement element)
	{
		return element.getMusicElement().getReference().getX();
	}
	
	public int getMusicElementIndex(MusicElement element)
	{
		return element.getReference().getX();
	}	

	public JScoreComponent getSelectedScoreUI()
	{
		return getScoreUI(selectedStaff);
	}

	public JScoreComponent getScoreUI(Staff staff)
	{
		if (staff == Staff.TREBLE)
			return trebleScoreUI;
		else
			return bassScoreUI;
	}

	public Tune getTune(Staff staff)
	{
		if (staff == Staff.TREBLE)
			return trebleTune;
		else
			return bassTune;
	}

	public Voice getSelectedVoice()
	{
		return getVoice(selectedStaff);
	}

	public Voice getVoice(Staff staff)
	{
		if (staff == Staff.TREBLE)
			return getVoice(trebleTune);
		else
			return getVoice(bassTune);
	}

	public Voice getVoice(Tune tune)
	{
		return (Voice) ((Vector) tune.getMusic().getVoices()).get(0);
	}

	// *************************** NOTATION CONVERSION METHODS ***************************

	// Create an abc4j Note music element from the midi key number.
	public Note getNoteFromKeyNumber(int pKeyNumber, int index)
	{
		Note n = new Note();
		setNoteKeyNumber(n, pKeyNumber, index);
		return n;
	}

	// Sets the abc4j Note height using the midi key number.
	public void setNoteKeyNumber(Note note, int pKeyNumber, int index)
	{
		// Note: For now we are assuming that the score will never have both sharps and flats.
		// This is true of most music scores. Having both sharps and flats is only desirable
		// if a note is played frequently both as a natural and an accidental. In this case
		// it is sometimes simpler to reach the accidental from the opposite side.
		// However, playing with these sorts of possibilities can sometimes paint you into a corner.
		// For example, consider the case where the G flat and A sharp have been played in the same
		// measure, but then G sharp (A flat) now needs to be played. This would require reversing
		// one of the accidentals and then applying a new one in the opposite direction.
		// Of course, this can be easily avoided by reverting to the previous assumption of using
		// only flats or sharps. After all, there are more white keys than black keys.
		// In fact, the only time this scenario could ever occur is with the G and A keys.
		// In short, this is an optimization problem that gets a bit complicated to implement.
		// It is much simpler to not allow such possibilities.
		byte height;
		byte relHeight;
		if(useSharps)
		{
			height = getNoteHeightFloor(pKeyNumber);
			relHeight = getRelativeNoteHeightFloor(pKeyNumber);
		}
		else
		{
			height = getNoteHeightCiel(pKeyNumber);
			relHeight = getRelativeNoteHeightCeil(pKeyNumber);
		}
		
		// Get accidental at position
		Accidental aMeasure = getKeyAndMeasureAccidental(relHeight, index);
		Accidental aNote = Accidental.NONE;
		if (isNoteSharp(pKeyNumber))
		{
			if (useSharps ? aMeasure.isSharp() : aMeasure.isFlat())
				aNote = Accidental.NONE;
			else if (aMeasure.isNatural() || aMeasure.isNotDefined())
				aNote = useSharps ? Accidental.SHARP : Accidental.FLAT;
			else
				print("Warning: Attempting to use a SHARP and a FLAT at the same note height.");
		}
		else
		{
			if (aMeasure.isSharp() || aMeasure.isFlat())
				aNote = Accidental.NATURAL;
		}
		
		// Make sure the accidental at the current index doesn't conflict with the one we 
		// are trying to add to it.
		Accidental aCurr = getCurrAccidental(relHeight, index);
		if(aCurr.isDefined() && !aCurr.equals(aNote))
			print("Warning: Attempting to add a conflicting accidental at current index: " + index + " Key: " + pKeyNumber);
		note.setHeight(height);
		note.setAccidental(aNote);
	}
	
	// return all accidentals
	public Accidental getCumAccidental(Note n, int index)
	{
		// Note: For the current accidental, we are using the note,
		// rather than checking the current index in the music score.
		// This allows conflicting accidentals to exist at the same index
		// temporarily when editing.
		byte relHeight = getNoteRelHeight(n);
		Accidental a1 = getKeyAccidental(relHeight);
		Accidental a2 = getMeasureAccidental(relHeight, index);
		Accidental a3 = n.getAccidental();

		// Assume accidentals override each other. This ignores the possibility of
		// - double sharps or double flats
		// - math (ie. sharp + sharp + flat = ?)
		if (a3.isDefined())
			return a3;
		else if (a2.isDefined())
			return a2;
		else
			return a1;
	}	

	// return key and measure accidental
	public Accidental getKeyAndMeasureAccidental(byte relHeight, int index)
	{
		Accidental a1 = getKeyAccidental(relHeight);
		Accidental a2 = getMeasureAccidental(relHeight, index);

		// Assume accidentals override each other. This ignores the possibility of
		// - double sharps or double flats
		// - math (ie. sharp + sharp + flat = ?)
		if (a2.isDefined())
			return a2;
		else
			return a1;
	}

	public Accidental getKeyAccidental(byte relHeight)
	{
		return trebleTune.getKey().getAccidentalFor(relHeight);
	}

	// Returns the accidental (if any) used at previous indices in this measure
	// at the given relative height
	// Does not return the accidental of the key.
	public Accidental getMeasureAccidental(byte relHeight, int index)
	{
		Accidental a = Accidental.NONE;
		Vector<MusicElement> musicElements1 = (Vector<MusicElement>) getVoice(Staff.TREBLE);
		Vector<MusicElement> musicElements2 = (Vector<MusicElement>) getVoice(Staff.BASS);
		assert (index - 1 < musicElements1.size());
		assert (index - 1 < musicElements2.size());
		for (int i = 0; i < index; i++)
		{
			MusicElement me1 = musicElements1.get(i);
			MusicElement me2 = musicElements2.get(i);
			if (me1 instanceof BarLine || me2 instanceof BarLine)
				a = Accidental.NONE;
			else
			{
				// getMusicElementAccidental permits elements other than notes to be
				// passed in
				Accidental a1 = getMusicElementAccidental(me1, relHeight);
				Accidental a2 = getMusicElementAccidental(me2, relHeight);
				if (a1.isDefined()) // in other words, if a1 is not Accidental.None
					a = a1;
				else if (a2.isDefined())
					a = a2;
			}
		}
		return a;
	}

	// Returns the accidental (if any) used at the current index at the given
	// relative height
	// Does not return the accidental of the key.
	public Accidental getCurrAccidental(byte relHeight, int index)
	{
		MusicElement me1 = (MusicElement) getVoice(Staff.TREBLE).get(index);
		MusicElement me2 = (MusicElement) getVoice(Staff.BASS).get(index);
		Accidental a1 = getMusicElementAccidental(me1, relHeight);
		Accidental a2 = getMusicElementAccidental(me2, relHeight);
		if (a1.isDefined()) // in other words, if a1 is not Accidental.None
			return a1;
		else
			return a2;
	}

	public Accidental getMusicElementAccidental(MusicElement me, byte relHeight)
	{
		// Note: This assumes that there is only one accidental at the relative
		// height.
		// If there are more than one (ie. two separate C notes at different
		// octaves,
		// one sharp and one flat), then we have a problem.
		// I'm pretty sure this is a big no-no in music.
		if (me instanceof Note)
		{
			Note n = (Note) me;
			if (!n.isRest() && getNoteRelHeight(n) == relHeight)
				return n.getAccidental();
		}
		else if (me instanceof MultiNote)
		{
			for (Note n : (Vector<Note>) ((MultiNote) me).getNotes())
			{
				if (getNoteRelHeight(n) == relHeight)
					return n.getAccidental();
			}
		}
		return Accidental.NONE;
	}

	// Returns the relative position of the note within it's
	// octave measured in half steps from A.
	public int getNoteRelToOctave(int pKeyNumber)
	{
		return pKeyNumber % 12;
	}

	// Returns the octave of the note, starting with A.
	public int getOctave(int pKeyNumber)
	{
		return pKeyNumber / 12;
	}

	// Returns the absolute position of the note measured in whole steps,
	// relative to the C1. Value returned ranges from 0 to 100 or so.
	// Result rounded down, cutting off half-steps.
	public int getNotePositionRelC(int pKeyNumber)
	{
		int nNote = getNoteRelToOctave(pKeyNumber);
		int nOctave = getOctave(pKeyNumber);
		return (nNote + ((nNote >= 5) ? 1 : 0)) / 2 + (nOctave * 7);
	}

	// Returns the relative position of the note within it's octave scale of C,
	// measured in whole steps. Use this method for character representation
	// (ie. use 65 + n). Value is always between 0 (for A) and 7 (for G).
	public int getNotePositionRelA(int pKeyNumber)
	{
		return (getNotePositionRelC(pKeyNumber) + 2) % 7;
	}

	// Returns true if the note is sharp.
	public boolean isNoteSharp(int pKeyNumber)
	{
		int nNote = getNoteRelToOctave(pKeyNumber);
		return (nNote == 1) || (nNote == 3) || (nNote == 6) || (nNote == 8)
				|| (nNote == 10);
	}

	// Returns the note height, used for abc4j staff positioning.
	// Rounds down if the note is sharp.
	public byte getNoteHeightFloor(int pKeyNumber)
	{
		return (byte) (pKeyNumber - 60 - (isNoteSharp(pKeyNumber) ? 1 : 0));
	}

	// Returns the note height, used for abc4j staff positioning.
	// Rounds up if the note is sharp.
	public byte getNoteHeightCiel(int pKeyNumber)
	{
		return (byte) (pKeyNumber - 60 + (isNoteSharp(pKeyNumber) ? 1 : 0));
	}

	// Returns the note height measured from A.
	// Returns a number between 0 and 12
	public byte getRelativeNoteHeightFloor(int pKeyNumber)
	{
		return (byte) ((pKeyNumber - (isNoteSharp(pKeyNumber) ? 1 : 0)) % 12);
	}

	// Returns the note height measured from A.
	// Returns a number between 0 and 12
	public byte getRelativeNoteHeightCeil(int pKeyNumber)
	{
		return (byte) ((pKeyNumber + (isNoteSharp(pKeyNumber) ? 1 : 0)) % 12);
	}

	// Returns the midi number of the key from it's abc4j note height and
	// possible accidentals
	public int getKeyNumber(byte noteHeight, Accidental acc)
	{
		if (acc.isFlat())
			noteHeight--;
		else if (acc.isSharp())
			noteHeight++;
		return noteHeight + 60;
	}

	// Returns the midi number of the key from it's abc4j Note object,
	// using the key of the treble cleff.
	public int getKeyNumber(Note n, int index)
	{
		return getKeyNumber(n.getHeight(), getCumAccidental(n, index));
	}

	public byte getNoteRelHeight(Note n)
	{
		return (byte) ((n.getHeight() + 60) % 12);
	}

	// Returns the midi number of the key from it's abc4j Note object,
	// using the key of the treble cleff.
	public int getKeyNumberWithoutAccidentals(Note n)
	{
		return getKeyNumber(n.getHeight(), Accidental.NONE);
	}

	public String getMusicElementRepresentation(MusicElement me, Staff staff)
	{
		if (me instanceof NotesSeparator)
			return " ";
		else if (me instanceof KeySignature)
		{
			return "K: " + ((KeySignature) me).toLitteralNotation()
					+ ((staff == Staff.BASS) ? " clef=bass\n" : "\n");
		}
		else if (me instanceof TimeSignature)
			return "M: " + me.toString() + "\n";
		else if (me instanceof EndOfStaffLine)
			return "\n";
		else if (me instanceof MultiNote)
			return getMultiNoteRepresentation((MultiNote) me);
		else if (me instanceof Note)
			return getNoteRepresentation((Note) me);
		else
			return me.toString();
	}

	public String getMultiNoteRepresentation(MultiNote mn)
	{
		String score = "";
		for (Note note : (Vector<Note>) mn.getNotes())
		{
			score += getNoteRepresentation(note);
		}
		return "[" + score + "]";
	}

	public String getNoteRepresentation(Note note)
	{
		if (note.isRest())
		{
			if (note.isRestInvisible())
				return "x";
			else
				return "z";
		}
		String score = "";
		if (note.getAccidental().isSharp())
			score = "^";
		else if (note.getAccidental().isFlat())
			score = "_";
		else if (note.getAccidental().isNatural())
			score = "=";
		int pKeyNumber = getKeyNumberWithoutAccidentals(note);
		int notePosition = getNotePositionRelA(pKeyNumber);
		int nOctave = getOctave(pKeyNumber);
		if (nOctave <= 5)
			score += (char) ('A' + notePosition);
		else
			score += (char) ('a' + notePosition);
		while (nOctave < 5)
		{
			score += ",";
			nOctave += 1;
		}
		while (nOctave > 6)
		{
			score += "'";
			nOctave -= 1;
		}
		return score;
	}

	public String addNoteToScore(String score, String noteToAdd,
			boolean combineWithLast)
	{
		if (combineWithLast)
		{
			if (score.charAt(score.length() - 1) == ']')
			{
				score = score.substring(0, score.length() - 1) + noteToAdd + "]";
			}
			else
			{
				int lastNoteLength = 1;
				while (score.charAt(score.length() - lastNoteLength) == ','
						|| score.charAt(score.length() - lastNoteLength) == '\'')
					lastNoteLength++;
				if (lastNoteLength < score.length()
						&& (score.charAt(score.length() - lastNoteLength - 1) == '_'
								|| score.charAt(score.length() - lastNoteLength - 1) == '=' || score
								.charAt(score.length() - lastNoteLength - 1) == '^'))
					lastNoteLength++;
				print("did it");
				score = score.substring(0, score.length() - lastNoteLength)
						+ "["
						+ score.substring(score.length() - lastNoteLength,
								score.length()) + noteToAdd + "]";
			}
		}
		else
		{
			score = score + noteToAdd;
		}
		return score;
	}
}