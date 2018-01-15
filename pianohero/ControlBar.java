package pianohero;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javafx.scene.control.ToggleGroup;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar.Separator;

public class ControlBar extends JPanel
{
	public CustomButton newButton;
	public CustomButton openButton;
	public CustomButton saveButton;
	public CustomButton saveAsButton;
	public CustomToggleButton recordButton;
	public CustomToggleButton editButton;
	public CustomToggleButton gameButton;
	public CustomButton settingsButton;
	public CustomButton randomButton;
	public BorderedLabel ptsLabel;
	public BorderedLabel accuracyLabel;
	public BorderedLabel timeLabel;
	public BorderedLabel speedLabel;
	
	public ControlBar() 
	{
      FlowLayout fl = new FlowLayout();
      this.setLayout(fl);
		instance = this;
		newButton = new CustomButton("New")
		{
			@Override
			public void click()
			{
				Tester2.getInstance().clearScore();
			}
		};
		openButton = new CustomButton("Open")
		{
			@Override
			public void click()
			{
				Tester2.getInstance().openFile();
			}
		};
		saveButton = new CustomButton("Save")
		{
			@Override
			public void click()
			{
				Tester2.getInstance().saveFile();
			}
		};
		saveAsButton = new CustomButton("Save As")
		{
			@Override
			public void click()
			{
				Tester2.getInstance().saveFileAs();
			}
		};		
		recordButton = new CustomToggleButton("Record");
		editButton = new CustomToggleButton("Edit");
		gameButton = new CustomToggleButton("Game");
		settingsButton = new CustomButton("Settings")
		{
			@Override
			public void click()
			{
				
//				Tester2.getInstance().openFile();
			}
		};
		randomButton = new CustomButton("Random")
		{
			@Override
			public void click()
			{
				Tester2.getInstance().generateRandomScore();
			}
		};
		ptsLabel = new BorderedLabel("Points: 0");
		accuracyLabel = new BorderedLabel("Accuracy: 0%");
		timeLabel = new BorderedLabel("Time: 0");
		speedLabel = new BorderedLabel("Speed: 0");
		
		add(newButton);
		add(openButton);
		add(saveButton);
		add(saveAsButton);
		add(recordButton);
		add(editButton);
		add(gameButton);
		add(settingsButton);
		add(randomButton);
		add(ptsLabel);
		add(accuracyLabel);
		add(timeLabel);
		add(speedLabel);
	}
	
	public void setPts(int pts)
	{
		ptsLabel.setText("Points: " + pts);
	}
	

	public void setAccuracy(double accuracy)
	{
		accuracyLabel.setText("Accuracy: " + String.format("%.1f", accuracy * 100) + "%");
	}
	
	public void setTime(double time)
	{
		timeLabel.setText("Time: " + String.format("%.1f", time));
	}

	public void setSpeed(double speed)
	{
		speedLabel.setText("Speed: " + String.format("%.1f", speed));
	}
	
   private static ControlBar instance;
	
	public static ControlBar getInstance()
	{
		return instance;
	}
	
   public CustomToggleButton selectedButton;
	
	// Wrapper class to allow visual formatting of control buttons
	public class CustomToggleButton extends JToggleButton
	{
	   CustomToggleButton(String pName)
	   {
	   	super(pName);
	      this.addItemListener(new ItemListener()
	      {
	      	public void itemStateChanged(ItemEvent ie) 
	      	{
	      		toggle(isSelected());
	      	}
	      });     
	   }

	   public void toggle(boolean selected)
	   {
	   	if(selected)
	   	{
	   		if(selectedButton != null && selectedButton != this)
	   			ControlBar.getInstance().selectedButton.setSelected(false);
	   		selectedButton = this;
	   	}
	   	else
	   		selectedButton = null;
			Tester2.getInstance().buttonToggle(selectedButton);
	   }
	}
	
	// Wrapper class to allow visual formatting of control buttons
	public class CustomButton extends Button
	{
		CustomButton(String pName)
	   {
	   	super(pName);
	      this.addActionListener(new ActionListener()
	      {
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					click();
				}
	      });     
	   }

	   public void click()
	   {
	   	System.out.println("clicked");
	   }
	}
	
	public class BorderedLabel extends JPanel
	{
		Label label;
		
		public BorderedLabel(String text)
		{
			super();
			label = new Label(text);
			add(label);
			((FlowLayout)getLayout()).setVgap(0);
			((FlowLayout)getLayout()).setHgap(0);
			setBorder(BorderFactory.createLineBorder(Color.black));
		}
		
		public void setText(String text)
		{
			label.setText(text);;
		}
	}
}
