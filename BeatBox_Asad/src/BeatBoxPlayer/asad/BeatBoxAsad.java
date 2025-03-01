package BeatBoxPlayer.asad;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.io.File;
import static javax.sound.midi.ShortMessage.*;


public class BeatBoxAsad {
	
	private String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
			"Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
			"High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
			"Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
			"Open Hi Conga"};
	private int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};
	private Sequencer sequencer;
	private Sequence seq;
	private Track track;
	private ArrayList<JCheckBox> cbList;
	private JFrame frame;
	
	public static void main(String[] args){
		BeatBoxAsad obj = new BeatBoxAsad();
		obj.buildGUI();
	}
	public void buildGUI() {
		frame = new JFrame("Cyber BeatBox");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
	    panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
		BorderLayout border = new BorderLayout();
		panel.setLayout(border);
		MyDraw nPanel = new MyDraw();
		nPanel.setPreferredSize(new Dimension(100,100));
		frame.add(BorderLayout.NORTH, nPanel);
	    Box boxContainer = new Box(BoxLayout.Y_AXIS);

		JButton b1 = new JButton("Start");
		b1.setBackground(Color.ORANGE);
		b1.addActionListener(e -> start());
		JButton b2 = new JButton("Stop");
		b2.setBackground(Color.ORANGE);
		b2.addActionListener(e -> sequencer.stop());
		JButton b3 = new JButton("Tempo Up");
		b3.setBackground(Color.ORANGE);
		b3.addActionListener(e -> changeTempo(1.03f));
		JButton b4 = new JButton("Tempo Down");
		b4.setBackground(Color.ORANGE);
		b4.addActionListener(e -> changeTempo(0.97f));
		JButton b5 = new JButton("Serialize it");
		b5.setBackground(Color.ORANGE);
		b5.addActionListener(e-> saveIt());
		JButton b6 = new JButton("Restore");
		b6.setBackground(Color.ORANGE);
		b6.addActionListener(e-> restore());;
		boxContainer.add(b1);
		boxContainer.add(b2);
		boxContainer.add(b3);
		boxContainer.add(b4);
		boxContainer.add(b5);
		boxContainer.add(b6);
		
		frame.add(BorderLayout.EAST, boxContainer);
		
		Box boxNames = new Box(BoxLayout.Y_AXIS);
		
		for(String instru : instrumentNames) {
			JLabel label = new JLabel(instru);
			label.setBorder(BorderFactory.createEmptyBorder(9,20,2,8));
			boxNames.add(label);
			
		}
		panel.add(BorderLayout.WEST, boxNames);
		
		GridLayout grid = new GridLayout(16,16);
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(grid);
		checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
		
		cbList = new ArrayList<>();
		for(int i = 0; i<256; i++) {
			JCheckBox cb = new JCheckBox();
			cb.setSelected(false);
			cbList.add(cb);
			checkBoxPanel.add(cb);
		}
		panel.add(BorderLayout.CENTER, checkBoxPanel);
		setupMIDI();
		
		frame.getContentPane().add(panel);
        frame.setSize(800,600);
		frame.setVisible(true);
		frame.setResizable(false);
}
	class MyDraw extends JPanel{
		public void paintComponent(Graphics g){
			g.setColor(Color.YELLOW);
			g.fillRect(0, 0, this.getWidth(),this.getHeight());
			g.setColor(Color.RED);
			g.fillRect(0,0, 100, 100);
			g.setColor(Color.RED);
			g.fillRect(this.getWidth()-100,0, 100, 100);
			g.setColor(Color.BLUE);
			g.setFont(new Font("serif",Font.ITALIC+Font.BOLD,60));
			g.drawString("BeatBox Player",200, 60);
			g.setFont(new Font("arial",Font.ITALIC+Font.BOLD,18));
			g.setColor(Color.MAGENTA);
			g.drawString("By- Asad Khan", 550,93);
	}
	}	
	public void setupMIDI() {
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			seq = new Sequence(Sequence.PPQ, 4);
			track = seq.createTrack();
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public MidiEvent makeEvent(int cmmd, int chnnl, int one, int two, int time) {
		MidiEvent midi = null;
		try {
			ShortMessage msg = new ShortMessage();
			msg.setMessage(cmmd,chnnl,one,two);
			midi = new MidiEvent(msg, time);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return midi;
	}
	public void start() {
		
		
		seq.deleteTrack(track);
	    track = seq.createTrack();
		for(int i = 0; i<16; i++) {
			int note = instruments[i];
		    
			for(int j = 0; j<16; j++) {
				JCheckBox c = cbList.get(16*i+j);
				if(c.isSelected()) {
					track.add(makeEvent(NOTE_ON,9,note,100,j));
					track.add(makeEvent(NOTE_OFF,9,note,100,j+1));
				}
				
}
			track.add(makeEvent(PROGRAM_CHANGE,9,1,0,16));
		}
		try {
			sequencer.setSequence(seq);
			sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
		}catch(Exception e) {
			e.printStackTrace();
		}
		

}
	public void changeTempo(float tempoGot) {
		float tempo = sequencer.getTempoFactor();
		sequencer.setTempoFactor(tempo * tempoGot);
	}
	public void saveIt() {
		JFileChooser save = new JFileChooser();
		save.showSaveDialog(frame);
		saveIn(save.getSelectedFile());
		
	}
	public void saveIn(File file) {
		boolean[] checkBox = new boolean[256];
		for(int i = 0; i<256; i++) {
			if(cbList.get(i).isSelected()) {
				checkBox[i] = true;
				cbList.get(i).setSelected(false);
			}
		}
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
			os.writeObject(checkBox);
			os.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		sequencer.stop();
	}
	public void restore() {
		JFileChooser open = new JFileChooser();
		open.showOpenDialog(frame);
		openFrom(open.getSelectedFile());
	}
	public void openFrom(File file){
		  boolean[] checkBox = null;
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
			checkBox = (boolean[]) is.readObject();
			is.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		for(int i = 0; i<256; i++) {
			if(checkBox[i]) {
				cbList.get(i).setSelected(true);
			}else {
				cbList.get(i).setSelected(false);
			}
		}
	}
}
