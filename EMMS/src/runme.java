import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Time;
import java.util.Arrays;
import java.util.EventListener;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Draws GUI
 * @author Zachery Holsinger
 *
 */
@SuppressWarnings("serial")
// Multithreading to refresh in the background and not block code
class backgroundTasks implements Runnable {
	private Thread t;
	private String threadName;
	public runme GUI;
	private JPanel mainPanel;
	backgroundTasks(String name) {
		threadName = name;
	}
	// Executes the background task
	public void start(runme GUImain, JPanel panel ) {
		GUI = GUImain;
		mainPanel = panel;
		if (t == null) {
			System.out.println("Starting " +  threadName );
			t = new Thread (this, threadName);
			t.start ();
		}
	}
	// Commands to execute in the background
	public void run() {
		while(true) {
			if(threadName.contains("meterScan")) {
				try {
					String activeMeters[] = Communication.startUp();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(threadName.contains("updateGUI")) {
				GUI.refreshGUI(Communication.ReturnFileValues());
				GUI.repaintAll(mainPanel);
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}

public class runme extends JFrame {



	// All these are global to the class to make it super easy to switch info out as new meters are
	// Active

	// This handles more data collection
	private static JButton[][] listMeters;
	private static int numMeters = 10;
	private JLabel listMetersCurrentName;
	private JLabel listMetersCurrentNum;
	private JLabel listMetersCurrentLoc;
	private JPanel listMetersNamePanel;
	private JPanel listMetersNumberPanel;
	private JPanel listMetersIPPanel;



	// This is a check label 
	private String activeMeterName = "No Meter Selected";
	private String activeMeterNum = "null";
	private String activeMeterLoc = "null";
	private int activeMeterNumber = 0;

	// More general column breakdown before it gets too crazy
	JPanel leftColumn = new JPanel(new BorderLayout());
	JPanel rightColumn = new JPanel(new BorderLayout());
	JPanel centerColumn = new JPanel(new BorderLayout());
	JPanel bottomButtonContainer = new JPanel(new GridLayout(0,4));
	static JPanel mainPanel = new JPanel(new BorderLayout());


	// Variables to pass info on (self explanitory)
	JPanel pictureContainer = null;
	ImageIcon currMeterPicture = null;
	JLabel picture = null;
	JLabel pictureName = null;
	JLabel pictureLocation = null;
	JLabel ip = null;
	JLabel MAC = null;
	JLabel remaining = null;
	JLabel currentLoad = null;
	JLabel timeLeft = null;
	JLabel loadLeft = null;
	JPanel infoContainer = null;
	JButton pulse;
	JButton showOff;
	JButton lButton;
	JButton rButton;

	// Make it for the center
	JPanel mainBoxContainer = null;
	JPanel mainBoxFill = null;

	private int activeMeterIndex = 1; // set this via active meter function
	// Actual meter data coming in
	static String meter[][];
	JProgressBar activeProgressBar;

	public runme(String[][] meters) {
		// Name of Program
		super("EMMS Control Panel V0.0");

		// array of meters
		meter = meters;
		numMeters = meters.length;

		// Sets active stats
		this.setTitle("EMMS Control Panel v0.0 | " + numMeters + " Total Meters Found | No Meter Selected");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		add(GUILayout(meters));
		setSize(1800, 1000);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		this.setUndecorated(true);
		this.setVisible(true);
		setLocationRelativeTo(null);

		// Tried to get key events working but it does NOT work currently
		KeyListen key = new KeyListen();
		this.addKeyListener(key);


		// Prevents smallness making things wonkey
		setMinimumSize(new Dimension(1200, 600));
		//		this.pack();
		setVisible(true);


		//		while(true) {
		//			Thread.sleep(25);
		//			int x = this.getWidth() / 4;
		//			int y = this.getHeight() / 4;
		//			leftColumn.setPreferredSize(new Dimension(x,y));
		//			
		//		}
	}

	void repaintAll(JPanel mainPanel) {
		mainPanel.repaint();
	}

	private JPanel GUILayout(String[][] meter) {
		int activeMeters = 0;
		listMeters = new JButton[3][numMeters + 1];

		/// LEFT HAND OF PAGE ///
		/// BEGIN ///

		//		listMetersNamePanel = new JPanel(new GridLayout(numMeters + 1,0));
		//		listMetersNumberPanel = new JPanel(new GridLayout(numMeters + 1,0));
		//		listMetersIPPanel = new JPanel(new GridLayout(numMeters + 1,0));

		listMetersNamePanel = new JPanel(new GridLayout(40,0));
		listMetersNumberPanel = new JPanel(new GridLayout(40,0));
		listMetersIPPanel = new JPanel(new GridLayout(40,0));

		listMetersNamePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,0));
		listMetersNumberPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
		listMetersIPPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,10));

		// Something happens if item in list is clicked
		Listener clickList = new Listener();
		listMeters[0][0] = new JButton("Name:");
		listMeters[1][0] = new JButton("#:");
		listMeters[2][0] = new JButton("Location:");

		listMeters[1][0].setPreferredSize(new Dimension(50, 25));

		listMetersNamePanel.add(listMeters[0][0]);
		listMetersNumberPanel.add(listMeters[1][0]);
		listMetersIPPanel.add(listMeters[2][0]);

		// fix weird column label issue in the future when there's 10+ meters on the network



		listMetersNamePanel.setBackground(Color.white);
		listMetersNumberPanel.setBackground(Color.white);
		listMetersIPPanel.setBackground(Color.white);
		for( int i = 1; i <= numMeters; i++) {
			// Add in section to check if meter is in index
			// hide / show depending on condition
			boolean active;
			if( meter[i - 1][12].contains("TRUE")) {
				active = true; 
				activeMeters++;
			} else { 
				active = false; 
			}

			listMeters[0][i] = new JButton(meter[i - 1][4]);
			//			listMeters[0][i].setPreferredSize(new Dimension(150, 25));
			listMeters[0][i].addActionListener(clickList);
			listMeters[0][i].setEnabled(active);
			listMeters[0][i].setBorder(BorderFactory.createLineBorder(Color.white));
			listMetersNamePanel.add(listMeters[0][i]);

			listMeters[1][i] = new JButton(meter[i - 1 ][5]);
			listMeters[1][i].setPreferredSize(new Dimension(50, 25));
			listMeters[1][i].addActionListener(clickList);
			listMeters[1][i].setEnabled(active);
			listMeters[1][i].setBorder(BorderFactory.createLineBorder(Color.white));
			listMetersNumberPanel.add(listMeters[1][i]);

			listMeters[2][i] = new JButton(meter[i - 1][6]);
			//			listMeters[2][i].setPreferredSize(new Dimension(150, 25));
			listMeters[2][i].addActionListener(clickList);
			listMeters[2][i].setEnabled(active);
			listMeters[2][i].setBorder(BorderFactory.createLineBorder(Color.white));
			listMetersIPPanel.add(listMeters[2][i]);	
			for(int j = 0; j < 3; j ++) {
				listMeters[j][i].setBackground(Color.white);

			}
		}


		// Adding the bottom section for current stats
		JPanel listMetersCurrentPanelLabel = new JPanel(new GridLayout(1,2));
		JPanel listMetersCurrentPanel = new JPanel(new GridLayout(1,2));

		JLabel listMetersNameLabel = new JLabel("Name:", SwingConstants.CENTER);
		JLabel listMetersNumLabel = new JLabel("Number:", SwingConstants.CENTER);
		listMetersNumLabel.setPreferredSize(new Dimension(50,25));
		JLabel listMetersLocLabel = new JLabel("Location:", SwingConstants.CENTER);


		listMetersCurrentPanelLabel.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
		listMetersCurrentPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

		listMetersCurrentName = new JLabel(activeMeterName, SwingConstants.CENTER);
		listMetersCurrentNum = new JLabel(activeMeterNum, SwingConstants.CENTER);
		listMetersCurrentNum.setPreferredSize(new Dimension(50,25));
		listMetersCurrentLoc = new JLabel(activeMeterLoc, SwingConstants.CENTER);
		listMetersCurrentPanelLabel.add(listMetersNameLabel);
		listMetersCurrentPanelLabel.add(listMetersNumLabel);
		listMetersCurrentPanelLabel.add(listMetersLocLabel);


		listMetersCurrentPanel.add(listMetersCurrentName,1,0);
		listMetersCurrentPanel.add(listMetersCurrentNum,1,1);
		listMetersCurrentPanel.add(listMetersCurrentLoc,1,2);


		// Puts together the grid layout of meters and locations
		JPanel listMetersPanel = new JPanel(new BorderLayout());
		JPanel currentMeterStats = new JPanel(new BorderLayout());

		// Puts together active Stats
		currentMeterStats.add(listMetersCurrentPanelLabel, BorderLayout.NORTH);
		currentMeterStats.add(listMetersCurrentPanel, BorderLayout.SOUTH);
		currentMeterStats.setBorder(BorderFactory.createLineBorder(Color.black));

		// Puts left-hand column together
		listMetersPanel.add(listMetersNamePanel, BorderLayout.WEST);
		listMetersPanel.add(listMetersNumberPanel, BorderLayout.CENTER);
		listMetersPanel.add(listMetersIPPanel, BorderLayout.EAST);
		JScrollPane leftSideBar = new JScrollPane(listMetersPanel);
		leftSideBar.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
		leftSideBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		leftSideBar.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		leftColumn.add(currentMeterStats, BorderLayout.SOUTH);
		leftColumn.add(leftSideBar);
		leftColumn.setBackground(Color.white);
		leftColumn.setPreferredSize(new Dimension(720, 400));
		leftColumn.setBorder(BorderFactory.createLineBorder(Color.black));



		/// Left Hand Page END ///
		/// END ///




		/// CENTER SPOT PAGE BEGIN ///
		/// BEGIN ///
		System.out.println(activeMeters);
		int meterColumns = (int) Math.ceil((float) activeMeters / 3);
		JPanel centerContainer = new JPanel(new GridLayout(meterColumns, 3));
		centerContainer.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		centerContainer.setBorder(BorderFactory.createLineBorder(Color.black));
		//		centerContainer.setSize(new Dimension(400,800));
		centerContainer.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));




		JPanel[][] meterGrid = new JPanel[meterColumns][3];
		int meter_accumulator = 0;
		for (int i = 0; i < meterColumns; i++) {
			for( int j = 0; j < 3; j++) {
				if(meter_accumulator < activeMeters) {
					//					System.out.println(i + " " + j + " " + meter_accumulator);
					meterGrid[i][j] = new JPanel(new BorderLayout());
					meterGrid[i][j].setBackground(Color.white);
					meterGrid[i][j].setPreferredSize(new Dimension(100,100));
					meterGrid[i][j].setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
					meterGrid[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
					meterGrid[i][j].addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							System.out.println("I was Clicked!");
						}
					});
					centerContainer.add(meterGrid[i][j]);
					meter_accumulator++;
				}
			}
		}
		centerColumn.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		centerColumn.add(centerContainer, BorderLayout.NORTH);		
		centerColumn.setBackground(Color.white);


		///CENTER SPOT PAGE END ///
		/// END /// 

		/// Right Hand Page Begin ///

		// BEGIN ///		
		rightColumn = new JPanel(new BorderLayout());
		rightColumn.setPreferredSize(new Dimension( 800, 800));
		rightColumn.setBackground(Color.white);
		rightColumn.setBorder(BorderFactory.createLineBorder(Color.black));

		// Handles upper picture stuff
		pictureContainer = new JPanel(new BorderLayout());
		pictureContainer.setPreferredSize(new Dimension(400,450));
		pictureContainer.setBorder(BorderFactory.createLineBorder(Color.black));

		currMeterPicture = grabImage("/none.png", 400);
		picture = new JLabel(currMeterPicture,JLabel.CENTER);
		picture.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		pictureName = new JLabel("No Meter Selected", SwingConstants.CENTER);
		pictureName.setFont(new Font(pictureName.getFont().getName(), Font.PLAIN, 26));
		pictureLocation = new JLabel("No Meter Selected", SwingConstants.CENTER);
		pictureContainer.add(pictureName, BorderLayout.CENTER);
		pictureContainer.add(pictureLocation, BorderLayout.SOUTH);

		// Info on bottom-right side for stuff
		infoContainer = new JPanel(new GridLayout(5, 1));
		infoContainer.setPreferredSize(new Dimension(800,100));
		infoContainer.setBackground(Color.white);
		infoContainer.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		ip = new JLabel("No IP");
		MAC = new JLabel("No MAC");
		remaining = new JLabel("No Load");
		currentLoad = new JLabel("None Remaining");
		timeLeft = new JLabel("No Time Left");
		loadLeft = new JLabel("No Load Left");
		infoContainer.add(ip);
		infoContainer.add(MAC);
		infoContainer.add(remaining);
		infoContainer.add(currentLoad);
		infoContainer.add(timeLeft);
		infoContainer.add(loadLeft);


		// Set up for progress Bar
		mainBoxContainer = new JPanel(new BorderLayout());
		mainBoxContainer.setPreferredSize(new Dimension(100,20));
		mainBoxContainer.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mainBoxContainer.setBorder(BorderFactory.createLineBorder(Color.black));

		activeProgressBar = new JProgressBar(0, 100);
		activeProgressBar.setValue(50);
		activeProgressBar.setStringPainted(true);
		mainBoxContainer.add(activeProgressBar);

		//		mainBoxFill = new JPanel(new BorderLayout());
		//		mainBoxFill.setPreferredSize(new Dimension(200,20));
		//		mainBoxFill.setBackground(Color.black);
		//		mainBoxContainer.add(mainBoxFill, BorderLayout.WEST);



		// Add ButtonS
		PulseListen pulseTrigger = new PulseListen();
		pulse = new JButton("Debug: null");
		pulse.setFont(new Font(pictureName.getFont().getName(), Font.PLAIN, 10));

		lButton = new JButton("<--");
		lButton.addActionListener(pulseTrigger);

		rButton = new JButton("-->");
		rButton.addActionListener(pulseTrigger);

		showOff = new JButton("Reset");
		showOff.addActionListener(pulseTrigger);

		pulse.addActionListener(pulseTrigger);
		pulse.setToolTipText("no Ip Yet");



		bottomButtonContainer.add(lButton);
		bottomButtonContainer.add(pulse);
		bottomButtonContainer.add(showOff);
		bottomButtonContainer.add(rButton);
		bottomButtonContainer.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));



		// progress bar stuff
		JPanel ProgressBar = new JPanel(new BorderLayout());
		ProgressBar.add(mainBoxContainer, BorderLayout.NORTH);
		ProgressBar.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		infoPanel.add(infoContainer, BorderLayout.NORTH);
		infoPanel.add(ProgressBar, BorderLayout.CENTER);
		// Putting it together in the right column
		pictureContainer.add(picture, BorderLayout.NORTH);
		rightColumn.add(pictureContainer,BorderLayout.NORTH);
		rightColumn.add(infoPanel, BorderLayout.CENTER);
		rightColumn.add(bottomButtonContainer, BorderLayout.SOUTH);

		/// RIGHT HAND COLUMN END///

		// main panel setup
		mainPanel.add(leftColumn, BorderLayout.WEST);
		mainPanel.add(rightColumn, BorderLayout.EAST);
		mainPanel.add(centerColumn, BorderLayout.CENTER);

		// get windowresize event
		windowResize onResize = new windowResize();
		mainPanel.addComponentListener(onResize);
		//		Window onRestore = new windowRestore();
		//	mainPanel.addComponentListener(onRestore);


		return mainPanel;
	}

	public void setActiveMeter(int indexMeterArray) {

//		System.out.println(meter[indexMeterArray - 1][12]); //<-- Gets stat of on / off debug mode, change to active and use to disallow arrows to change active meter
		for( int i = 1; i <= numMeters; i++ ) {
			for( int j = 0; j < 3; j++ ) {
				listMeters[j][i].setBackground(Color.white);
			}
		}
//		if(meter[indexMeterArray - 1][12].contains("TRUE")) {
		UIManager.put("Button.font", new javax.swing.plaf.FontUIResource("Serif",Font.ITALIC,12));
		UIManager.put("Label.font", new javax.swing.plaf.FontUIResource("Serif",Font.ITALIC,8));
		pulse.setFont(new Font(pictureName.getFont().getName(), Font.PLAIN, 10));
		pulse.setText("Debug: " + meter[indexMeterArray - 1][13]);
		activeMeterIndex = indexMeterArray;
		listMetersCurrentName.setText(listMeters[0][indexMeterArray].getText());
		listMetersCurrentNum.setText(listMeters[1][indexMeterArray].getText());
		listMetersCurrentLoc.setText(listMeters[2][indexMeterArray].getText());
		listMeters[0][indexMeterArray].setBackground(Color.LIGHT_GRAY);
		listMeters[1][indexMeterArray].setBackground(Color.LIGHT_GRAY);
		listMeters[2][indexMeterArray].setBackground(Color.LIGHT_GRAY);


		// Picture stuff
		String URLcurrentMeterImg = "/" + listMeters[1][indexMeterArray].getText() + ".png";
		ImageIcon tempimg = grabImage(URLcurrentMeterImg,350);
		currMeterPicture.setImage(tempimg.getImage());
		pictureName.setText("Name: " + listMeters[0][indexMeterArray].getText() + " | " + listMeters[1][indexMeterArray].getText());
		pictureLocation.setText("Location: " + listMeters[2][indexMeterArray].getText());
		picture.repaint();


		// Stats Stuff
		ip.setText("IP: " + meter[indexMeterArray - 1][3]);
		MAC.setText("MAC: " + meter[indexMeterArray - 1][11]);
		remaining.setText("Energy Used: " + meter[indexMeterArray - 1][7] + "WHr");
		currentLoad.setText("Current Load: " + meter[indexMeterArray - 1][8] + " W");
		loadLeft.setText("Allocation:: " + meter[indexMeterArray - 1][10] + " WHr");
		timeLeft.setText("Time Left: " + meter[indexMeterArray - 1][9] + " mins");
		String[] loadInfo = meter[indexMeterArray - 1][10].split("/");
		if(loadInfo.length == 2) {
			activeProgressBar.setMaximum(Integer.parseInt(loadInfo[1]));
			activeProgressBar.setValue(Integer.parseInt(loadInfo[0]));
		} else {
			activeProgressBar.setMaximum(100);
			activeProgressBar.setValue(100 - Integer.parseInt(meter[indexMeterArray - 1][7]));
		}
		activeProgressBar.setStringPainted(true);
		pulse.setToolTipText(meter[indexMeterArray - 1][3]);
		showOff.setToolTipText(meter[indexMeterArray - 1][3]);
		infoContainer.repaint();

		// Change % values for box
		//		mainBoxFill.setSize(Integer.parseInt(meter[indexMeterArray - 1][7]),20);
		// change color based on 

		rightColumn.repaint();
		setTitle("EMMS Control Panel v0.0 | " + numMeters + " Total Meters Found | " + listMeters[0][indexMeterArray].getText() + " | " + listMeters[1][indexMeterArray].getText() + " | " + listMeters[2][indexMeterArray].getText() + " | ");
//	} else if(numMeters != 0) {
//		activeMeterIndex--;
//		if( activeMeterIndex < 1) {
//			activeMeterIndex = numMeters;
//		}
//		setActiveMeter(activeMeterIndex);
//	}
	}

	public interface WindowStateListener extends EventListener{

		default void windowStateChanged(WindowEvent e) {

		}

	}

	// Resizes images
	private ImageIcon grabImage(String filePath, int imgDimension) {
		ImageIcon imageIcon = null;

		try {
			imageIcon = new ImageIcon(getClass().getResource(filePath));
		} catch(NullPointerException e) {
			imageIcon = new ImageIcon(getClass().getResource("/none.png"));
		}
		Image image = getScaledImage(imageIcon.getImage(), imgDimension, imgDimension);
		imageIcon.setImage(image);
		return imageIcon;

	}
	// resizes images but scaled
	private Image getScaledImage(Image srcImg, int w, int h){
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}


	// adjust everything when resized, prevents it being too smol
	public class windowResize implements ComponentListener {

		@Override
		public void componentMoved(ComponentEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void componentResized(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			JPanel panel = (JPanel) arg0.getSource();
			int xL = panel.getWidth() / 5;
			int xR = panel.getWidth() / 3;
			leftColumn.setPreferredSize(new Dimension(xL,1200));
			rightColumn.setPreferredSize(new Dimension(xR,1200));


		}

		@Override
		public void componentShown(ComponentEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void componentHidden(ComponentEvent e) {
			// TODO Auto-generated method stub

		}
	}

	public class Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			// this happens when button is clicked
			JButton button = (JButton) event.getSource();

			for( int i = 1; i <= numMeters; i++ ) {
				for( int j = 0; j < 3; j++ ) {
					if(listMeters[j][i] == button ) {
						setActiveMeter(i);
					}
				}
			}
		}

	}

	// doesnt work yet
	public class KeyListen implements KeyListener {

		// method doesnt currently work


		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println(arg0.getKeyChar());
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			System.out.println(e.getKeyChar());

		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			System.out.println(e.getKeyChar());

		}

	}
	// does things for bottom buttons.
	public class PulseListen implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			// this happens when button is clicked
			JButton button = (JButton) event.getSource();
			String ip = button.getToolTipText();
			String name = button.getText();
			if( name.contains("Debug: OFF")) {
				Client client = new Client();
				// Turns ON debug Mode
				String response = client.Communicate(ip, 80, "!Set;Lights;On*");
				try {
					Communication.updateCSVInfoNetwork(ip);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
				setActiveMeter(activeMeterIndex);

			}
			if( name.contains("Debug: ON")) {
				Client client = new Client();
				// Turns OFF debug Mode
				client.Communicate(ip, 80, "!Set;Lights;Off*");
				try {
					Communication.updateCSVInfoNetwork(ip);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setActiveMeter(activeMeterIndex);
			}

			if( name.contains("Reset")) {
				System.out.println("Resetting " + ip);
				Client client = new Client();
				client.Communicate(ip, 80, "!MOD;RESET*");
				//				Communication.updateCSVInfoNetwork(ip);
			}
			if( name.contains("-->")) {
				// rotates selection up
				if (activeMeterIndex == numMeters) {
					activeMeterIndex = 1;
				} else {
					activeMeterIndex++;
				}
				setActiveMeter(activeMeterIndex);
			}
			if( name.contains("<--")) {
				if (activeMeterIndex == 1) {
					activeMeterIndex = numMeters;
				} else {
					activeMeterIndex--;
				}

				// rotates selection down
				setActiveMeter(activeMeterIndex);
			}
		}

	}
	void refreshGUI(String[][] meters) {
		//		System.out.println(Arrays.deepToString(meter));
		meter = meters;
		//		System.out.println(Arrays.deepToString(meter));
		if (meter.length == numMeters) {
			// Wont refresh array list unless it needs too
			int activeMeters = 0;
			for( int i = 1; i <= numMeters; i++) {
				// Add in section to check if meter is in index
				// hide / show depending on condition
				boolean active;
				if( meter[i - 1][12].contains("TRUE")) {
					active = true; 
					activeMeters++;
				} else { 
					active = false; 
				}
				listMeters[0][i].setText(meter[i - 1][4]);
				//			listMeters[0][i].setPreferredSize(new Dimension(150, 25));
				listMeters[0][i].setEnabled(active);
				listMeters[0][i].setBorder(BorderFactory.createLineBorder(Color.white));


				listMeters[1][i].setText(meter[i - 1 ][5]);
				listMeters[1][i].setPreferredSize(new Dimension(50, 25));
				listMeters[1][i].setEnabled(active);
				listMeters[1][i].setBorder(BorderFactory.createLineBorder(Color.white));

				listMeters[2][i].setText(meter[i - 1][6]);
				//			listMeters[2][i].setPreferredSize(new Dimension(150, 25));
				listMeters[2][i].setEnabled(active);
				listMeters[2][i].setBorder(BorderFactory.createLineBorder(Color.white));
				for(int j = 0; j < 3; j ++) {
					listMeters[j][i].setBackground(Color.white);

				}
			}
		} else{
			/// Chunk adjusts to new meternumber///////////////
			//			if (meter.length >= numMeters) {
			//				for (int i = meter.length; i > numMeters; i--) {
			//					numMeters = i;
			//				}
			//			} else {
			//				for (int i = meter.length; i < numMeters; i++) {
			//					numMeters = i;
			//				}
			//			}

			numMeters = meter.length; // Haha this is so much easier
			////////////////////////////////////////////////////

			/////// Re-Updates and Adds / Removes Everything //////

			listMetersNamePanel.removeAll();
			listMetersNumberPanel.removeAll();
			listMetersIPPanel.removeAll();

			listMetersNamePanel.revalidate();
			listMetersNumberPanel.revalidate();
			listMetersIPPanel.revalidate();

			// Adds all the stuff back on
			listMeters = new JButton[3][numMeters + 1];
			int activeMeters = 0;
			Listener clickList = new Listener();
			listMeters[0][0] = new JButton("Name:");
			listMeters[1][0] = new JButton("#:");
			listMeters[2][0] = new JButton("Location:");

			listMeters[1][0].setPreferredSize(new Dimension(50, 25));

			listMetersNamePanel.add(listMeters[0][0]);
			listMetersNumberPanel.add(listMeters[1][0]);
			listMetersIPPanel.add(listMeters[2][0]);

			for( int i = 1; i <= numMeters; i++) {
				// Add in section to check if meter is in index
				// hide / show depending on condition
				boolean active;
				if( meter[i - 1][12].contains("TRUE")) {
					active = true; 
					activeMeters++;
				} else { 
					active = false; 
				}
				//				System.out.println(meter[i - 1][4]);
				listMeters[0][i] = new JButton(meter[i - 1][4]);
				//			listMeters[0][i].setPreferredSize(new Dimension(150, 25));
				listMeters[0][i].setEnabled(active);
				listMeters[0][i].addActionListener(clickList);
				listMeters[0][i].setBorder(BorderFactory.createLineBorder(Color.white));
				listMetersNamePanel.add(listMeters[0][i]);

				listMeters[1][i] = new JButton(meter[i - 1 ][5]);
				listMeters[1][i].setPreferredSize(new Dimension(50, 25));
				listMeters[1][i].setEnabled(active);
				listMeters[1][i].addActionListener(clickList);
				listMeters[1][i].setBorder(BorderFactory.createLineBorder(Color.white));
				listMetersNumberPanel.add(listMeters[1][i]);


				listMeters[2][i] = new JButton(meter[i - 1][6]);
				//			listMeters[2][i].setPreferredSize(new Dimension(150, 25));
				listMeters[2][i].setEnabled(active);
				listMeters[2][i].addActionListener(clickList);
				listMeters[2][i].setBorder(BorderFactory.createLineBorder(Color.white));
				listMetersIPPanel.add(listMeters[2][i]);

				for(int j = 0; j < 3; j ++) {
					listMeters[j][i].setBackground(Color.white);
				}
			}
			//////////////////////////////////////////////////////////
		}
		listMetersNamePanel.revalidate();
		listMetersNumberPanel.revalidate();
		listMetersIPPanel.revalidate();

		listMetersNamePanel.repaint();
		listMetersNumberPanel.repaint();
		listMetersIPPanel.repaint();
		// Makes Sure Active Meter is always selected
		if (activeMeterIndex <= numMeters && !listMetersCurrentNum.getText().equals("null")) {
			setActiveMeter(activeMeterIndex);
		} else if (activeMeterIndex > numMeters) {
			activeMeterIndex = 1;
		}
		mainPanel.repaint();
	}

	public static void main(String[] args) throws InterruptedException {
		// This also sets fonts to make sure nothing is improperly set
		UIManager.put("Button.font", new javax.swing.plaf.FontUIResource("Serif",Font.ITALIC,12));
		UIManager.put("Label.font", new javax.swing.plaf.FontUIResource("Serif",Font.ITALIC,12));
		String[][] meterData = Communication.ReturnFileValues();
		// MESSIAH ACKNOLWEDGE         DATE         VERSION         IPV4          NAME       #     Location   			 	Remaining %    Curr Load     Time Left      Allocation(Formerly load left)       MAC Addr   Online(?)  debug(ON/OFF)
		runme GUI = new runme(meterData);
		//		run backgroundThread = new run(GUI);
		backgroundTasks meterScan = new backgroundTasks("meterScan");
		backgroundTasks updateGUI = new backgroundTasks("updateGUI");
		meterScan.start(GUI, mainPanel);
		updateGUI.start(GUI, mainPanel);
	}
}
