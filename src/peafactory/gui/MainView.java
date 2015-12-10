package cologne.eck.peafactory.gui;

/*
 * Peafactory - Production of Password Encryption Archives
 * Copyright (C) 2015  Axel von dem Bruch
 * 
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the License, 
 * or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * See:  http://www.gnu.org/licenses/gpl-2.0.html
 * You should have received a copy of the GNU General Public License 
 * along with this library.
 */

/**
 * Main frame for the production of any Password Encryption Archive (pea).
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.peagen.*;
import cologne.eck.peafactory.peas.editor_pea.EditorType;
import cologne.eck.peafactory.peas.file_pea.FileType;
import cologne.eck.peafactory.peas.file_pea.FileTypePanel;
import cologne.eck.peafactory.peas.gui.CharTable;
import cologne.eck.peafactory.peas.image_pea.ImageType;
import cologne.eck.peafactory.peas.image_pea.ImageTypePanel;
import cologne.eck.peafactory.peas.note_pea.NotesType;
import cologne.eck.peafactory.tools.EntropyPool;
import cologne.eck.peafactory.tools.KeyRandomCollector;
import cologne.eck.peafactory.tools.MouseRandomCollector;
import cologne.eck.peafactory.tools.PasswordQualityCheck;
import cologne.eck.peafactory.tools.Zeroizer;


@SuppressWarnings("serial")
public final class MainView extends JFrame implements ActionListener {

	private static MainView frame; 
	
	// file to encrypt: 
	// if null, this indicates that resource is saved beside jar archive in same directory
	private static String openedFileName = null;

	// screen: 
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	double screenWidth = screenSize.getWidth();
	double screenHeight = screenSize.getHeight();
	
	private JLabel projectLabel; // Notebook, Image...
	
	private static FileTypePanel filePanel;
	
	// MainView is several times disposed and new created, so
	// password fields must be static
	private static JPasswordField pswField;
	private static JPasswordField pswField2;
	
	private JLabel qualityLabel;
	private JLabel qualityLabelStars;

	private JTextField jarNameField;
	private JTextField labelTextField;	

	protected JLabel pathLabel;
	public static JProgressBar progressBar;
	
	private JCheckBox scriptCheck;	
	private static boolean unixScript = false;
	
	private static boolean blankPea = false;
	
	protected static String language; 
	protected static ResourceBundle languageBundle;
	
	private static Image image = new ImageIcon("resources" + File.separator + "pea-lock.png").getImage();

	private MainView () {
		
		frame = this;
		this.setTitle("Pea Factory");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		language = PeaFactory.getLanguage();
		languageBundle = PeaFactory.getLanguagesBundle();
				
		DataType dataType = DataType.getCurrentType();

		//Image icon = new ImageIcon("resources" + File.separator + "pea-lock.png").getImage();
	    this.setIconImage(image);

		JPanel contentPane = (JPanel)this.getContentPane();
		contentPane.setBorder(new EmptyBorder(5, 15, 5, 15));
		// use frame to collect random values: 
		contentPane.addMouseMotionListener(new MouseRandomCollector() );
		JScrollPane frameScrollPane = new JScrollPane(contentPane);
		this.setContentPane(frameScrollPane);
		
		BoxLayout boxLayout = new BoxLayout(contentPane, BoxLayout.Y_AXIS);
		contentPane.setLayout(boxLayout);
		
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));		
		// Menu:
		Menu menu = new Menu(languageBundle);
		JLabel languageLabel = new JLabel(language + "  ");
		LanguageMenu languageMenu = new LanguageMenu();
		languageMenu.setToolTipText(languageBundle.getString("tooltip_language"));

		menuPanel.add(menu);		
		menuPanel.add(Box.createHorizontalGlue() );
		menuPanel.add(languageLabel);
		menuPanel.add(languageMenu);

		contentPane.add(menuPanel);
		

		projectLabel = new JLabel();
		projectLabel.setText(dataType.getDescription());
		projectLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		projectLabel.setBorder(new LineBorder(Color.GRAY, 1));
		contentPane.add(Box.createVerticalStrut(10));
		contentPane.add(projectLabel);
		contentPane.add(Box.createVerticalStrut(10));
		
		if (blankPea == false) {		
			
			if (dataType instanceof NotesType || // text
					dataType instanceof EditorType ){	
				
				TextTypePanel textTypePanel = TextTypePanel.getInstance(languageBundle);
				contentPane.add(textTypePanel);			

			} else if (dataType instanceof ImageType) {	
				
				ImageTypePanel imageTypePanel = ImageTypePanel.getInstance(languageBundle);
				contentPane.add(imageTypePanel);
			
			} else if (dataType instanceof FileType) {	
							
				filePanel = new FileTypePanel(300, 120, true);
		
				contentPane.add(Box.createVerticalStrut(20));
				contentPane.add(filePanel);				
			}
			
			JPanel pathPanel = new JPanel();
			pathPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
			pathLabel = new JLabel();

			if (openedFileName != null) {
				pathLabel.setText(openedFileName);
				pathLabel.setBorder(new LineBorder(Color.BLACK,1));
				pathPanel.add(pathLabel);
			}
			contentPane.add(pathPanel);

			
			JPanel pswPanel = new JPanel();		
			pswPanel.setLayout(new BoxLayout(pswPanel, BoxLayout.Y_AXIS));

			JPanel pswLabelPanel = new JPanel();
			pswLabelPanel.setLayout(new BoxLayout(pswLabelPanel, BoxLayout.X_AXIS));
			JLabel pswLabel = new JLabel(languageBundle.getString("psw_label"));
			pswLabelPanel.add(pswLabel);
			
			pswLabelPanel.add(Box.createHorizontalStrut(20));		
			
			qualityLabel = new JLabel();
			qualityLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
			pswLabelPanel.add(qualityLabel);
			
			qualityLabelStars = new JLabel();
			qualityLabelStars.setOpaque(true);
			qualityLabelStars.setBackground(Color.WHITE);
			qualityLabelStars.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
			pswLabelPanel.add(qualityLabelStars);
			
			pswLabelPanel.add(Box.createHorizontalGlue());
			
			JButton charTableButton = new JButton(languageBundle.getString("char_table"));
			charTableButton.addActionListener(this);
			charTableButton.setActionCommand("charTable1");
			pswLabelPanel.add(charTableButton);
			pswPanel.add(pswLabelPanel);

			pswField = new JPasswordField(30);
			pswField.setToolTipText(languageBundle.getString("tooltip_psw"));
			// collect random values from key events:
			pswField.addKeyListener(new KeyRandomCollector() );
			pswField.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent arg0) {}
				@Override
				public void keyReleased(KeyEvent arg0) {}
				@Override
				public void keyTyped(KeyEvent arg0) {
					updatePasswordCheck();				
				}			
			});
			pswPanel.add(pswField);
			
			pswPanel.add(Box.createVerticalStrut(10));
			
			JPanel pswLabelPanel2 = new JPanel();
			pswLabelPanel2.setLayout(new BoxLayout(pswLabelPanel2, BoxLayout.X_AXIS));
			JLabel pswLabel2 = new JLabel(languageBundle.getString("psw_label_2"));
			pswLabel2.setPreferredSize(new Dimension(300,30));
			pswLabelPanel2.add(pswLabel2);
			pswLabelPanel2.add(Box.createHorizontalGlue());
			JButton charTableButton2 = new JButton(languageBundle.getString("char_table"));
			charTableButton2.addActionListener(this);
			charTableButton2.setActionCommand("charTable2");
			pswLabelPanel2.add(charTableButton2);
			pswPanel.add(pswLabelPanel2);


			pswField2 = new JPasswordField(30);
			pswField2.setToolTipText(languageBundle.getString("tooltip_psw2"));
			// collect random values from key events:
			pswField2.addKeyListener(new KeyRandomCollector() );
			pswPanel.add(pswField2);
			
			contentPane.add(Box.createVerticalStrut(10));
			
			contentPane.add(pswPanel);
		
		} // end if blankPea == false

		
		JPanel jarNamePanel = new JPanel();		
		jarNamePanel.setLayout(new BoxLayout(jarNamePanel, BoxLayout.Y_AXIS));
		
		JLabel jarNameLabel = new JLabel(languageBundle.getString("jar_name_label"));
		jarNameLabel.setPreferredSize(new Dimension(300,30));
		jarNamePanel.add(jarNameLabel);
		
		jarNameField = new JTextField(30);
		jarNameField.setToolTipText(languageBundle.getString("tooltip_jarname"));
		// collect random values from key events:
		jarNameField.addKeyListener(new KeyRandomCollector() );

		jarNamePanel.add(jarNameField);
		contentPane.add(Box.createVerticalStrut(10));
		contentPane.add(jarNamePanel);
	
		
		JPanel labelTextPanel = new JPanel();		
		labelTextPanel.setLayout(new BoxLayout(labelTextPanel, BoxLayout.Y_AXIS));		
		JLabel abovePswfieldLabel = new JLabel(languageBundle.getString("above_pswfield_label"));
		abovePswfieldLabel.setPreferredSize(new Dimension(300,30));
		labelTextPanel.add(abovePswfieldLabel);		
		labelTextField = new JTextField(30);
		labelTextField.setToolTipText(languageBundle.getString("tooltip_text_above"));
		// collect random values from key events:
		labelTextField.addKeyListener(new KeyRandomCollector() );
		labelTextPanel.add(labelTextField);
		contentPane.add(Box.createVerticalStrut(10));
		contentPane.add(labelTextPanel);

		
		JPanel newProjectPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JButton newProjectButton = new JButton(languageBundle.getString("new_project"));
		newProjectButton.setActionCommand("newProject");
		newProjectButton.addActionListener(this);
		newProjectPanel.add(newProjectButton);
		contentPane.add(Box.createVerticalStrut(10));
		contentPane.add(newProjectPanel);

		
		JPanel createFilePanel = new JPanel();
		createFilePanel.setLayout(new BoxLayout(createFilePanel, BoxLayout.X_AXIS));
		
		progressBar = new JProgressBar(0,100);
		progressBar.setValue(0);
		progressBar.setStringPainted(false);
		progressBar.setPreferredSize(new Dimension(200, 30));
		progressBar.setBorderPainted(false);// not visible if no task
		createFilePanel.add(progressBar);
		createFilePanel.add(Box.createHorizontalStrut(50));		
/*		
		blankCheck = new JCheckBox(languageBundle.getString("blank_check"), false);
		blankCheck.setToolTipText(languageBundle.getString("tooltip_blank_check"));
		blankCheck.setSelected(false);
		blankCheck.addActionListener(this);
		blankCheck.setActionCommand("blankCheck");
		contentPane.add(blankCheck);
		
		boundCheck = new JCheckBox(languageBundle.getString("bound_check"), false);
		boundCheck.setToolTipText(languageBundle.getString("tooltip_bound_check"));
		boundCheck.setSelected(true);
		boundCheck.addActionListener(this);
		boundCheck.setActionCommand("boundCheck");
		contentPane.add(boundCheck);
*/		
		scriptCheck = new JCheckBox(languageBundle.getString("script_check"), false);
		scriptCheck.setToolTipText(languageBundle.getString("tooltip_script_check"));
		contentPane.add(scriptCheck);
		
		JButton createFileButton = new JButton(languageBundle.getString("create_jarfile_button"));
		createFileButton.setActionCommand("create");
		createFileButton.addActionListener(this);
		createFileButton.setToolTipText(languageBundle.getString("tooltip_create"));
		createFilePanel.add(createFileButton);
		contentPane.add(Box.createVerticalStrut(10));
		contentPane.add(createFilePanel);

		setLocation(100, 10);		
	
		int prefHeight = (int) this.getLayout().preferredLayoutSize(contentPane).getHeight();
		int prefWidth = (int) this.getLayout().preferredLayoutSize(contentPane).getWidth();

		// size of frame depends on screen size: 
		if ( (prefHeight + 120) > screenHeight) { // width + 50, because of scrollpanes' width
			prefHeight = (int) screenHeight - 130;
		}
		if ( (prefWidth + 130) > screenWidth) {
			prefWidth = (int) screenWidth - 150;
		}
		// set JScrollPane size for frame:
		frameScrollPane.setPreferredSize(new Dimension(prefWidth, prefHeight + 50));
	}
	
	public final static MainView getInstance() {
		if (frame == null) {
			return new MainView();
		} else {
			System.err.println("MainView: existing instance.");
			return null;
		}
	}



	@Override
	public void actionPerformed(ActionEvent ape) {
		String com = ape.getActionCommand();
		
		if (com.equals("newProject")) {
			
			ProjectSelection proj = new ProjectSelection();
			proj.setLocation( (int)this.getLocation().getX() + 100, (int)this.getLocation().getY() + 60 );
			proj.setVisible(true); 

			
		} else if ( com.startsWith("charTable")) {	
			if(com.equals("charTable1")){
				CharTable table = new CharTable(this, pswField);
				table.setVisible(true);
			} else {
				CharTable table = new CharTable(this, pswField2);
				table.setVisible(true);				
			}
						
		} else if ( com.equals("create")) {	
		
			EntropyPool.getInstance().stopCollection();
			
			if (scriptCheck.isSelected() ) {
				unixScript = true;
			} else {
				unixScript = false;
			}

			if (jarNameField.getText() == null) {
				JarStuff.setJarFileName("default.jar");				
			} else if (jarNameField.getText().length() > 0) {
				JarStuff.setJarFileName(jarNameField.getText() + ".jar");
			} else {
				JarStuff.setJarFileName("default.jar");
			}
			
			JarStuff.setLabelText(labelTextField.getText() );
			
			if (DataType.getCurrentType() instanceof ImageType
					&& ImageType.getImageName() == null && blankPea == false) {
				JOptionPane.showMessageDialog(frame,
						languageBundle.getString("open_image_button"),//"No image selected.",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if (DataType.getCurrentType() instanceof FileType) {
				
				if (filePanel.getSelectedFileNames().length == 1) { // only one valid file selected
					
					// ask for default file: -> openedFileName
					int n = JOptionPane.showConfirmDialog(this,
							languageBundle.getString("ask_default_file"),			    			
			    			"Warning", 
			    			JOptionPane.YES_NO_CANCEL_OPTION,
			    			JOptionPane.QUESTION_MESSAGE);
					if (n == JOptionPane.YES_OPTION) { // ok			
						MainView.setOpenedFileName(filePanel.getSelectedFileNames()[0]);
					} else if (n == JOptionPane.NO_OPTION) {
						MainView.setOpenedFileName(null);
					} else { // cancel, close
						return;
					}
				} else if (filePanel.getSelectedFileNames() == null || 
						filePanel.getSelectedFileNames().length == 0){
					
					if (CipherStuff.isBound() == true) {
						JOptionPane.showMessageDialog(frame,
								"No file selected.",
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else{
					MainView.setOpenedFileName(null);
				}
			}			
			
			progressBar.setBorderPainted(true);
			progressBar.setStringPainted(true);

			PeaFactory.createFile();
					
			if (frame != null) {
				frame.dispose(); 
				frame = null;
			}				

			ProjectSelection proj = new ProjectSelection();
			proj.setLocation( (int)this.getLocation().getX() + 100, (int)this.getLocation().getY() + 60 );
			proj.setVisible(true); 			
		}	
	}
	
	private final void updatePasswordCheck() {
		char[] pwd = pswField.getPassword();

		int quality = PasswordQualityCheck.checkQuality(pwd);//pswField.getPassword() );
		if (pwd != null){
			Zeroizer.zero(pwd);
		}
		pwd = null;
		char[] q = new char[quality];
		Arrays.fill(q,  '*');
		
		if (quality < 10) {
			qualityLabel.setText("weak ");
			qualityLabelStars.setText(new String(q)); 
			qualityLabelStars.setForeground(Color.RED);
		} else if (quality < 15) {
			qualityLabel.setText("medium ");
			qualityLabelStars.setText(new String(q)); 
			qualityLabelStars.setForeground(Color.YELLOW);
		} else {
			qualityLabel.setText("strong ");
			qualityLabelStars.setText(new String(q)); 
			qualityLabelStars.setForeground(Color.GREEN);
		}
	}
	
	public final static void updateFrame() {
		
		if (frame != null) {
			frame.dispose(); 
			frame = null;
		}			
		frame = MainView.getInstance();
		frame.pack();
		frame.setVisible(true);
	}
	
	
	//==========================================
	// Getter & Setter

	public static char[] getPsw() {
		return pswField.getPassword();
	}
	public static char[] getPsw2() {
		return pswField2.getPassword();
	}
	// Re-"Setter":
	public static void resetPsw() {
		pswField.setText("");
	}
	public static void resetPsw2() {
		pswField2.setText("");
	}
	public final static String getOpenedFileName() {
		return openedFileName;
	}
	public final static void setOpenedFileName(String _openedFileName) {
		openedFileName = _openedFileName;
	}
	public final static Point getFrameLocation() {
		return frame.getLocation();
	}
	public final static FileTypePanel getFilePanel() {
		return filePanel;
	}
	public final static boolean isDisplayed() {// used in ProjectSelection
		if (frame == null) {
			return false; 
		} else if ( frame.isVisible() == false ) {
			return false;
		} else {
			return true;
		}
	}
	public final static Image getImage() {
		return image;
	}

	public static boolean getUnixScript() {
		return unixScript;
	}

	/**
	 * @return the blankPea
	 */
	public static boolean isBlankPea() {
		return blankPea;
	}

	/**
	 * @param blankPea the blankPea to set
	 */
	public static void setBlankPea(boolean blankPea) {
		MainView.blankPea = blankPea;
	}
}
