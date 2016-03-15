package cologne.eck.peafactory.peas.gui;

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
 * View of the pea dialog to type the password. 
 */

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import settings.PeaSettings;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.tools.Attachments;
import cologne.eck.peafactory.tools.EntropyPool;
import cologne.eck.peafactory.tools.KeyRandomCollector;
import cologne.eck.peafactory.tools.MouseRandomCollector;
import cologne.eck.peafactory.tools.Zeroizer;



/*
 * View of PswDialogNotes, PswDialogEditor, PswDialogFile
 *  - not for PswDialogImage 
 */


@SuppressWarnings("serial")
public class PswDialogView extends JDialog implements WindowListener,
		ActionListener, KeyListener {
	
	private static final Color peaColor = new Color(230, 249, 233);
	private static final Color messageColor = new Color(252, 194, 171);//(255, 216, 151)
	
	private static JLabel pswLabel;
	private static JPasswordField pswField;
	private JButton okButton;
	
	private JPanel filePanel;
	
	private static JLabel fileLabel;

	private static String fileType = "dingsda";
	
	private static JCheckBox rememberCheck;
	
	private static PswDialogView dialog;
	
	// display warnings and error messages
	private static JTextArea messageArea;
	
	private static WhileTypingThread t;
	
	private static Image image;
	
	private static ResourceBundle languagesBundle;
	
	private static boolean started = false;// to check if decryption was startet by ok-button
	
	private static boolean initializing = false; // if started first time (no password, no content)
	
	private PswDialogView() {
		
		setUI();
		
		languagesBundle = PswDialogBase.getBundle();
		
		fileType = PswDialogBase.getFileType();
	
		dialog = this;
		this.setTitle(PeaSettings.getJarFileName() );
				
		this.setBackground(peaColor);

		URL url = this.getClass().getResource("resources/pea-lock.png");
		if (url == null) {
			try{
			image = new ImageIcon(getClass().getClassLoader().getResource("resources/pea-lock.png")).getImage();
			} catch (Exception e) {
				System.out.println("image not found");
			}
		} else {			
			image = new ImageIcon(url).getImage();
		}


	    this.setIconImage(image);
	
		this.addWindowListener(this);
		
		this.addMouseMotionListener(new MouseRandomCollector() );

		JPanel topPanel = (JPanel) this.getContentPane();
		// more border for right and bottom to start
		// EntropyPool if there is no password
		topPanel.setBackground(peaColor);
		topPanel.setBorder(new EmptyBorder(5,5,15,15));
		
		topPanel.addMouseMotionListener(new MouseRandomCollector() );
		
		messageArea = new JTextArea();
		messageArea.setEditable(false);	
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		messageArea.setBackground(topPanel.getBackground());		
		
		okButton = new JButton("ok");
		okButton.setPreferredSize(new Dimension( 60, 30));
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		// if there is no password, this might be the only 
		// chance to start EntropyPool
		okButton.addMouseMotionListener(new MouseRandomCollector() );
		
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));		
		
		topPanel.add(messageArea);
		
		if (initializing == false) {			
			
			if (PswDialogBase.getWorkingMode().equals("-r")) { // rescue mode
				JLabel rescueLabel = new JLabel("=== RESCUE MODE ===");
				rescueLabel.setForeground(Color.RED);
				topPanel.add(rescueLabel);
			}

			if (PeaSettings.getExternFile() == true) {	// display files and menu		
				
				if (fileType.equals("file") ) {
					filePanel = PswDialogBase.getTypePanel();
					filePanel.addMouseMotionListener(new MouseRandomCollector() );
					topPanel.add(filePanel);
					
				} else { // text, passwordSafe, image: one single file
					
					JPanel singleFilePanel = new JPanel();
					singleFilePanel.setLayout(new BoxLayout(singleFilePanel, BoxLayout.Y_AXIS));
					
					JPanel fileNamePanel = new JPanel();
					fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.X_AXIS));
					singleFilePanel.add(fileNamePanel);
					
					JLabel noteLabel = new JLabel();
					noteLabel.setText(languagesBundle.getString("encrypted_file"));//"encryptedFile: ");				
					fileNamePanel.add(noteLabel);
					fileNamePanel.add(Box.createHorizontalStrut(10));
					
					fileLabel = new JLabel(languagesBundle.getString("no_valid_file_found") + languagesBundle.getString("select_new_file"));//"no valid file found - select a new file");
					fileNamePanel.add(fileLabel);
					fileNamePanel.add(Box.createHorizontalGlue() );
					
					JPanel openButtonPanel = new JPanel();
					openButtonPanel.setLayout(new BoxLayout(openButtonPanel, BoxLayout.X_AXIS));
					singleFilePanel.add(openButtonPanel);
					
					JButton openFileButton = new JButton();
					openFileButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
					
					if (fileType.equals("passwordSafe")){
						openFileButton.setText("open directory");
					} else { // text, image
						openFileButton.setText(languagesBundle.getString("open_file"));//"open file");
					}
					openFileButton.addActionListener(this);
					openFileButton.setActionCommand("openFile");
					openButtonPanel.add(openFileButton);
					openButtonPanel.add(Box.createHorizontalGlue() );
					
					topPanel.add(singleFilePanel);
					
					if (PswDialogBase.searchSingleFile() == null) {
						JOptionPane.showMessageDialog(this,
								PswDialogBase.getErrorMessage(),
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
						//reset errorMessage:
						PswDialogBase.setErrorMessage("");
					}
				}
			}//else
			topPanel.add(Box.createVerticalStrut(10));//Box.createVerticalStrut(10));
			
			JPanel pswLabelPanel = new JPanel();
			pswLabelPanel.addMouseMotionListener(new MouseRandomCollector() );
			pswLabelPanel.setLayout(new BoxLayout(pswLabelPanel, BoxLayout.X_AXIS));
			topPanel.add(pswLabelPanel);
			
			if (PeaSettings.getLabelText() == null) {
				pswLabel = new JLabel( languagesBundle.getString("enter_password") );
			} else {
				pswLabel = new JLabel( PeaSettings.getLabelText() );
				//pswLabel.setText(PeaSettings.getLabelText() );
			}
			pswLabel.setPreferredSize(new Dimension( 460, 20));
			pswLabelPanel.add(pswLabel);
			pswLabelPanel.add(Box.createHorizontalGlue());
			JButton charTableButton = new JButton(languagesBundle.getString("char_table"));
			charTableButton.addMouseMotionListener(new MouseRandomCollector() );
			charTableButton.addActionListener(this);
			charTableButton.setActionCommand("charTable1");
			pswLabelPanel.add(charTableButton);

			
			JPanel pswPanel = new JPanel();
			pswPanel.setLayout(new BoxLayout(pswPanel, BoxLayout.X_AXIS));
			
			pswField = new JPasswordField();
			pswField.setBackground(new Color(231, 231, 231) );
			pswField.setPreferredSize(new Dimension(400, 30));
			pswField.addKeyListener(this);
			pswField.addKeyListener(new KeyRandomCollector() );

			pswPanel.add(pswField);
			
			pswPanel.add(okButton);
			topPanel.add(pswPanel);
		
		} else {// initializing
			
			if (fileType.equals("file") ) {
				JPanel initPanel1 = new JPanel();
				initPanel1.setLayout(new BoxLayout(initPanel1, BoxLayout.X_AXIS));
				JLabel fileInitializationLabel = new JLabel("Select one file and type a password to initialize...");
				initPanel1.add(fileInitializationLabel);
				initPanel1.add(Box.createHorizontalGlue());
				topPanel.add(initPanel1);
				
				JPanel initPanel2 = new JPanel();
				initPanel2.setLayout(new BoxLayout(initPanel2, BoxLayout.X_AXIS));
				JLabel fileInitializationLabel2 = new JLabel("(you can add more files or directories later).");
				initPanel2.add(fileInitializationLabel2);
				initPanel2.add(Box.createHorizontalGlue());
				topPanel.add(initPanel2);				
			}
			
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(400, 300));
			panel.addMouseMotionListener(new MouseRandomCollector() );
			
			JLabel infoLabel = new JLabel("Initialization...  ");
			infoLabel.setForeground(Color.RED);
			infoLabel.addMouseMotionListener(new MouseRandomCollector() );
			panel.add(infoLabel);
			panel.add(okButton);
			
			topPanel.add(panel);
		}		
		
		if (PeaSettings.getExternFile() == true){		
			
			JPanel checkRememberPanel = new JPanel();
			checkRememberPanel.setLayout(new BoxLayout(checkRememberPanel, BoxLayout.X_AXIS));
			
			rememberCheck = new JCheckBox();
		    rememberCheck = new JCheckBox();	
		    rememberCheck.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		    rememberCheck.setText(languagesBundle.getString("remember_file_names"));//"remember selected file names");
		    rememberCheck.setToolTipText(languagesBundle.getString("file_storage_info")// "file names will be saved in the file " +
		    		+  "  "  + PeaSettings.getJarFileName() + ".path");// in current directory");
			
		    checkRememberPanel.add(Box.createVerticalStrut(10));
		    checkRememberPanel.add(rememberCheck);
		    checkRememberPanel.add(Box.createHorizontalGlue() );
		    
		    topPanel.add(checkRememberPanel);
		}
		if (PeaSettings.getKeyboard() != null) {
			JButton keyboardButton = new JButton("keyboard");
			keyboardButton.addActionListener(this);
			keyboardButton.setActionCommand("keyboard");
			topPanel.add(keyboardButton);			
		}

		this.setLocation(100,30);
		pack();
	}
	
	public final static PswDialogView getInstance() {
		if (dialog == null) {
			dialog = new PswDialogView();
		} else {
			//return null
		}
		return dialog;
	}
	
	public static void checkInitialization(){
		// check if this file was not yet initialized and if not,  
		// remove password field
		try {
			// the String "uninitializedFile" is stored in the file "text.lock"
			// if the pea was not yet initialized
			
			// try to read only the first line: 
			BufferedReader brTest = new BufferedReader(new FileReader("resources" + File.separator + "text.lock"));
		    String test = brTest .readLine();
		    brTest.close();
			if (test.startsWith("uninitializedFile")){
				// set variable initializing to indicate that there is no password or content 
				//PswDialogView.setInitialize(true);
				initializing = true;
				System.out.println("Initialization");
			}
		} catch (Exception e) {
			// ignore
		}
	}

	public final void displayErrorMessages(String topic) {
		
		setMessage(topic + ":\n" + CipherStuff.getErrorMessage());

		PswDialogView.clearPassword();
	}

	@Override
	public void keyPressed(KeyEvent kpe) {
		// EntropyPool must be started by mouse events if initializing
		if(kpe.getKeyChar() == KeyEvent.VK_ENTER && initializing == false){
			okButton.doClick();
		}
	}
	@Override
	public void keyReleased(KeyEvent arg0) {}
	@Override
	public void keyTyped(KeyEvent arg0) {}

	@Override
	public void actionPerformed(ActionEvent ape) {
		String command = ape.getActionCommand();
		//System.out.println("command: " + command);
		if(command.equals("keyboard")){
			PeaSettings.getKeyboard().setVisible(true);
			
		} else if (command.equals("charTable1")){
			CharTable table = new CharTable(this, pswField);
			table.setVisible(true);
			
		} else if (command.equals("ok")){

			if (PeaSettings.getKeyboard() != null) {
				PeaSettings.getKeyboard().dispose();
			}
			
			started = true;

			EntropyPool.getInstance().stopCollection();

			if (PeaSettings.getExternFile() == true && initializing == false) {

				// remember this file?
				if (rememberCheck.isSelected() ) {
					
						String[] newNames;
						if (PswDialogBase.getFileType().equals("file")) {
							// set encrypted file names:
							newNames = PswDialogBase.getDialog().getSelectedFileNames();

						} else {
							newNames = new String[1];
							newNames[0] = PswDialogBase.getEncryptedFileName();
						}
						PswDialogBase.addFilesToPathFile( newNames );
				}
				
				if ( ! fileType.equals("file")) { // text, image
					
					// check if one valid file is selected:
					if (PswDialogBase.getEncryptedFileName() == null) {
						
						setMessage(languagesBundle.getString("no_file_selected"));

						return;
					}
					
					PswDialogBase.getDialog().startDecryption();
				} else { // file
					PswDialogBase.getDialog().startDecryption();
				}
			} else { // internal File in directory "resources"

				// set resources/text.lock as encryptedFileName
				String newFileName = "resources" + File.separator + "text.lock";

				PswDialogBase.setEncryptedFileName(newFileName);

				PswDialogBase.getDialog().startDecryption();
		
				// remember this file?
				if (PeaSettings.getExternFile() == true && initializing == false) {
					if (rememberCheck.isSelected() ) {
						String[] newName = { PswDialogBase.getEncryptedFileName() };
						PswDialogBase.addFilesToPathFile( newName);
					}
				}
			}
			started = false;

			
		} else if (command.equals("openFile")) {

			JFileChooser chooser = new JFileChooser();

			if (fileType.equals("text")) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text", "rtf");
				chooser.setFileFilter( filter );
			} else if (fileType.equals("passwordSafe")) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			} else if (fileType.equals("image")) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter("IMAGE FILES", "png", "jpg", "jpeg", "bmp", "gif");
				chooser.setFileFilter( filter );
			}
		    int returnVal = chooser.showOpenDialog(this);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {		    
		    	
		    	File file = chooser.getSelectedFile();
		    	String selectedFileName = file.getAbsolutePath();
		    	
		    	if ( PswDialogBase.checkFile(selectedFileName ) == true ) {
					fileLabel.setText( selectedFileName );
					PswDialogBase.setEncryptedFileName( selectedFileName ); 
		    	} else {
		    		setMessage(languagesBundle.getString("error") 
		    				+ "\n" + languagesBundle.getString("no_access_to_file")
		    				+ "\n" + file.getAbsolutePath());
					return;
		    	}
		    }
		}		
	}
	

	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) { // dispose

		// if attacker has access to memory but not to file
		if (Attachments.getNonce() != null) {
			Zeroizer.zero(Attachments.getNonce());
		}
		if (PswDialogBase.getDialog() != null) {
			PswDialogBase.getDialog().clearSecretValues();
		}	

		this.dispose();
		//System.exit(0);	// do not exit, otherwise files leave unencrypted	
	}
	@Override
	public void windowClosing(WindowEvent arg0) { // x
		
		if (started == true) {
			return;
		} else {
			// if attacker has access to memory but not to file
			if (Attachments.getNonce() != null) {
				Zeroizer.zero(Attachments.getNonce());
			}
			if (PswDialogBase.getDialog() != null) {
				PswDialogBase.getDialog().clearSecretValues();
			}
			System.exit(0);
		}	
	}
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {
		
		// do some expensive computations while user tries to remember the password
		t = this.new WhileTypingThread();
		t.setPriority( Thread.MAX_PRIORITY );// 10
		t.start();
		if (initializing == false) {
			pswField.requestFocus();
		}
	}

	
	//=======================================
	// Helper Functions
	
	public final static void setUI() {
//		UIManager.put("Button.background", peaColor );					
//		UIManager.put("MenuBar.background", peaColor ); // menubar
//		UIManager.put("MenuItem.background", color2 ); // menuitem
		UIManager.put("PopupMenu.background", peaColor );	//submenu									
		UIManager.put("OptionPane.background", peaColor );
		UIManager.put("Panel.background", peaColor );
		UIManager.put("RadioButton.background", peaColor );
		UIManager.put("ToolTip.background", peaColor );
		UIManager.put("CheckBox.background", peaColor );	
		
		UIManager.put("InternalFrame.background", peaColor );
		UIManager.put("ScrollPane.background", peaColor );
		UIManager.put("Viewport.background", peaColor );	
		// ScrollBar.background  Viewport.background
	}
	

	
	protected final static void updateView(){
		dialog.dispose();
		dialog = new PswDialogView();
		dialog.setVisible(true);
	}
	
	/**
	 * Perform he action for ok button. Used for initialization. 
	 */
	public void clickOkButton() {
		okButton.doClick();
	}
	

	
	//===============================================
	// Getter & Setter
	public final static char[] getPassword() {
		return pswField.getPassword();
	}
	protected final static void setPassword(char[] psw) { // for Keyboard
		pswField.setText( new String(psw) );
	}
	protected final static void setPassword(char pswChar) { // for charTable
		pswField.setText("" +  pswChar );
	}
	public final static void clearPassword() {
		if (pswField != null) {
			pswField.setText("");
		}
	}
	public final static void setMessage(String message) {
		if (message != null) {
			messageArea.setBackground(messageColor);
			messageArea.setText(message);
			dialog.pack();
		}
	}
	public final static void setLabelText(String labelText) {
		if (initializing == true){
			pswLabel.setText(labelText);
		}
	}
	public final static WhileTypingThread getThread() {
		return t;
	}
	public final static Image getImage() {
		if (image == null) {
			System.err.println("PswDialogView getImage: image null");
		}
	    return image;
	}
	public final static PswDialogView getView() {
		return dialog;
	}
	public final JPanel getFilePanel() {
		return filePanel;
	}
	public final static void setFileName(String fileName) {
		fileLabel.setText(fileName);
	}
	public final static String getFileName() {
		return fileLabel.getText();
	}
	public final static Color getPeaColor(){
		return peaColor;
	}
	
	/**
	 * @return the initializing
	 */
	public static boolean isInitializing() {
		return initializing;
	}

	/**
	 * @param initializing the initializing to set
	 */
	public static void setInitializing(boolean initialize) {
		PswDialogView.initializing = initialize;
	}

	//================================================
	// inner class
	public class WhileTypingThread extends Thread {
		// some pre-computations while typing the password
		@Override
		public void run() {		
			if (PswDialogBase.getDialog() != null) {
				PswDialogBase.getDialog().preComputeInThread();
			}
		}
	}
}
