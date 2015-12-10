package cologne.eck.peafactory.peas.file_pea;

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
 * Displays the selected files for file pea. 
 * Used in MainView, PswDialogFile and LockFrameFile.
 */

/*	Displays selected files, hierarchy, invalid files, file number, all selected size...
 * 
 * =============================================
 * 	PUBLIC: 
 * 
 *  - FileDisplayPanel(int scrollWidth, int scrollHeight, boolean _plainFiles)
 * 
 * 	- String[] getSelectedFileNames()
 *  - void setCheckBoxInvalid(String originalFileName, String newAnnotatedFileName)
 *  - void addNewCheckBoxes(File[] newFiles)
 *  - static String getInvalidMarker()
 *  
 *  =============================================
 *  ACTIONS AND METHODS: 
 *  
 * 					Action "addFiles" (Button)
 * 					- chooser.getSelectedFiles
 * 						- addNewCheckBoxes
 * 							- checkDoubles()
 * 							- add to originalFileNames and annotatedFileNames
 * 							- new JCheckBox check
 * 							- checkAccess
 * 							/ isFile: add(check)
 *							/ isDirectory: add(check), checkNumberAndSize, getNumberAndSize, listFiles, add(includedCheckBoxes) 
 *     				- displayNewNumberAndSize();
 * 				    - updateWindow();
 * 
 * 
 * 					Action "dirSelection" (JCheckBox)
 * 					- select/deselect checkBox
 * 					- select/deselect all children
 * 					- if deselection: deselectParentDirs
 * 					- displayNewNumberAndSize
 * 
 * 					Action (fileSelection" (JCheckBox)
 * 					- select/deselect CheckBox
 * 					- if deselection: deselectParentDirs
 * 					- displayNeNumberAndSize
 * 
 * 
 * TODO: Festplattenlesefehler... (IOEXceptions)
 */


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import settings.PeaSettings;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Attachments;
import cologne.eck.peafactory.tools.ReadResources;




@SuppressWarnings("serial")
public class FileTypePanel extends JPanel implements ActionListener{
	
	private static FileTypePanel ftp;
	private FileComposer fc = new FileComposer();

	private ResourceBundle languagesBundle;

	
	// Task to show a progress bar: 
	private ProgressTask task;
	
	JPanel buttonPanel;
	JPanel checkPanel; // contains only JCheckBoxes
	private JLabel infoLabel; // displays file number and size
	private JFileChooser chooser;
	private boolean plainModus = true; // select plain files or encrypted files
	private boolean directoryWarning = false; // once for each session	
	private static boolean sizeWarning = false;// warn only one time
	private static boolean extremeSizeWarning = false;
	private static boolean numberWarning = false;
	
	// Values for file check
	private static final String INVALID_MARKER = "/***/ "; // all files of annotatedFileNames starting with this will not be encrypted
	private static final String DIRECTORY_MARKER = "###"; // all directories of annotatedFileNames ends with this
	
	private int fileNumber = 0; // number of displayed valid files
	private long allSize = 0; // size of displayed valid files

	private final static int FILE_NUMBER_LIMIT = 512; // Warning if selected file number > FILE_NUMBER_LIMIT
	private final static long SIZE_LIMIT = 1024 * 1024 * 64; // 64 MiB
	
	private static final Color directoryColor = new Color(215,215,215); // background color of JCheckBox for directories
	private static final Color invalidDirectoryColor = new Color(235,210,210); // background color of JCheckBox for directories
	private static final Color fileColor = new Color(245,245,245);
	private static final Color invalidColor = new Color(255, 220, 220);
	
	
	public FileTypePanel(int scrollWidth, int scrollHeight, boolean _plainModus){//, String[] newFileNames) {
		
		ftp = this;
		PswDialogView.setUI();
	    try {
	    	languagesBundle = PswDialogBase.getBundle();
	    } catch (MissingResourceException mre) {
	    	// using in main program PeaFactory: 
	    	//File file = new File("src" + File.separator + "config" + File.separator + "i18n");
	    	File file = new File("config" + File.separator + "i18n");  
		    URL[] urls = new URL[1];
		    try {
				urls[0] = file.toURI().toURL();
			} catch (MalformedURLException e) {
				System.err.println("FileTypePanel: " + e);
				e.printStackTrace();
			}  
		    ClassLoader loader = new URLClassLoader(urls);  
		    languagesBundle = ResourceBundle.getBundle("LanguagesBundle", Locale.getDefault(), loader);  
	    }
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		fc.setFileDisplayPanel(this);
		
		plainModus = _plainModus; // selection for encrypted or plain files?
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(new EmptyBorder(10,0,5,0));
		JButton searchButton = new JButton("add new file");
		searchButton.addActionListener(this);
		searchButton.setActionCommand("addFile");
		buttonPanel.add(searchButton);
		buttonPanel.add(Box.createHorizontalGlue());
		this.add(buttonPanel);	
	
		checkPanel = new JPanel();
		checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));	
		if (plainModus == false) {
			initFileNames();
		}		

		JScrollPane scroll = new JScrollPane(checkPanel);
		scroll.setPreferredSize(new Dimension(scrollWidth, scrollHeight));		
		this.add(scroll);		

		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
		infoPanel.setBorder(new EmptyBorder(0,0,10,0));
		//infoPanel.setBackground(PswDialogView.getPeaColor());//Color.WHITE);
		infoLabel = new JLabel();
		infoLabel.setText(" Number of files: " + fileNumber + ", overall size (Byte): " + allSize + " - (MiB): " + allSize/1024/1024);
		infoPanel.add(infoLabel);
		infoPanel.add(Box.createHorizontalGlue());
		this.add(infoPanel);
		//this.add(Box.createVerticalStrut(10));
		
		JPanel colorPanel = new JPanel();
		colorPanel.setBackground(PswDialogView.getPeaColor());
		this.add(colorPanel);
		JLabel fileColorLabel = new JLabel("   valid file   ");
		fileColorLabel.setFont(new Font(Font.SANS_SERIF , Font.PLAIN, 11));
		fileColorLabel.setOpaque(true);
		fileColorLabel.setBackground(fileColor);
		fileColorLabel.setBorder(new LineBorder(Color.WHITE, 1));
		colorPanel.add(fileColorLabel);
		JLabel directoryColorLabel = new JLabel("   valid directory   ");
		directoryColorLabel.setFont(new Font(Font.SANS_SERIF , Font.PLAIN, 11));
		directoryColorLabel.setOpaque(true);
		directoryColorLabel.setBackground(directoryColor);
		directoryColorLabel.setBorder(new LineBorder(Color.WHITE, 1));
		colorPanel.add(directoryColorLabel);
		JLabel invalidFileColorLabel = new JLabel("   invalid file   ");
		invalidFileColorLabel.setFont(new Font(Font.SANS_SERIF , Font.PLAIN, 11));
		invalidFileColorLabel.setOpaque(true);
		invalidFileColorLabel.setBackground(invalidColor);
		invalidFileColorLabel.setBorder(new LineBorder(Color.WHITE, 1));
		colorPanel.add(invalidFileColorLabel);
		JLabel invalidDirectoryColorLabel = new JLabel("   invalid/empty directory   ");
		invalidDirectoryColorLabel.setFont(new Font(Font.SANS_SERIF , Font.PLAIN, 11));
		invalidDirectoryColorLabel.setOpaque(true);
		invalidDirectoryColorLabel.setBackground(invalidDirectoryColor);
		invalidDirectoryColorLabel.setBorder(new LineBorder(Color.WHITE, 1));
		colorPanel.add(invalidDirectoryColorLabel);
		
	}
	

	/**
	 * just to test view
	 */
/*	public static void main(String[] args) {
		PswDialogView.setUI();
		JFrame frame = new JFrame();

		frame.add(new FileTypePanel(300,300, true));//, null) );
		frame.setLocation(100,100);
		frame.pack();
		frame.setVisible(true);
	}
*/

	@Override
	public void actionPerformed(ActionEvent ape) {
		
		String command = ape.getActionCommand();
		if (command.equals("addFile")) {		
			addAction();
		    
		} else if (command.equals("dirSelection")) {
			
			JCheckBox dirCheck = (JCheckBox) ape.getSource();			
			String dirName = dirCheck.getText().trim();
			int index = fc.getListIndex(dirName);
			
			// check if dirCheck is selected or not 
			boolean select;
			if (dirCheck.isSelected() == true) {
				select = true;
				fileNumber ++;
			} else {
				select = false;
				fileNumber--;
			}
					
			// check if dirCheck is last check box
			if (index == fc.getOriginalFileNames().size() - 1 ) { // last file, no files included
				if (select == true) {
					dirCheck.setSelected(true);
				} else {
					dirCheck.setSelected(false);
				}
				displayNewNumberAndSize();
				return;
			}
			
			//-------------------------
			// select/deselect children
			int nextIndex = index + 1;
			while ( fc.getOriginalFileNames().get(nextIndex).startsWith( fc.getOriginalFileNames().get(index) ) ) {
				
				if ( fc.getAnnotatedFileNames().get(nextIndex).contains(INVALID_MARKER) ) {
					//System.out.println("invalid: " + annotatedFileNames.get(nextIndex).trim());
					nextIndex++;
					if ( nextIndex >= fc.getOriginalFileNames().size() ) {
						break;
					}
					continue;
				} 
				
				JCheckBox check = (JCheckBox) checkPanel.getComponent(nextIndex);
				File file = new File( fc.getOriginalFileNames().get(nextIndex) );
				if (file.isFile() ) {
					long fileSize = fc.getFileSize(file.getAbsolutePath());
					if (select == true) {
						if (check.isSelected() == (false) ){
							check.setSelected(true);							
							allSize += fileSize;
							fileNumber++;
						}
					} else {
						if (check.isSelected() == (true) ){
							check.setSelected(false);
							allSize -= fileSize;
							fileNumber--;
						}
					}
				} else if ( file.isDirectory() ) {
					if (select == true) {
						if (check.isSelected() == (false) ){
							check.setSelected(true);
							fileNumber++;
						}
					} else {
						if (check.isSelected() == (true) ){
							check.setSelected(false);
							fileNumber --;
						}
					}					
				} 
				nextIndex++;
				if ( nextIndex >= fc.getOriginalFileNames().size() ) {
					break;
				}
			}
			
			// if deselect: deselect all parent directories
			if (select == false) {
				deselectParentDirs(dirName, index);
			}
			
			displayNewNumberAndSize();

		} else if (command.equals("fileSelection")) {
			
			JCheckBox fileCheck = (JCheckBox) ape.getSource();
			String fileName = fileCheck.getText().trim();
			if (fileCheck.isSelected() == true) {
				fileNumber++;
				allSize += fc.getFileSize(fileName);
			} else {
				fileNumber--;
				allSize -= fc.getFileSize(fileName);		
				
				// deselect parent directories if selected
				deselectParentDirs(fileName, fc.getOriginalFileNames().indexOf(fileName));
			}
			displayNewNumberAndSize();
			
		} else if (command.equals("hide")) {
			JCheckBox hideCheck = (JCheckBox) ape.getSource();
			if (hideCheck.isSelected() == true) {
				chooser.setFileHidingEnabled(false);
			} else {
				chooser.setFileHidingEnabled(true);
			}
		}
	}
	
	//==============================================================================================
	// Helper functions
	
	protected final void addAction() {		
			
		chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);
		
		// Add check box for hidden files:
		JCheckBox hideCheck = new JCheckBox("<html>show<br/>hidden<br/>files?</html>");
		hideCheck.addActionListener(this);
		hideCheck.setActionCommand("hide");
		chooser.setAccessory(hideCheck);

	    int returnVal = chooser.showOpenDialog(this);
	    
	    if(returnVal == JFileChooser.APPROVE_OPTION) {		    

	    	// create new check boxes for selected files
	    	String[] selectedFileNames = new String[chooser.getSelectedFiles().length];
	    	for (int i = 0; i < chooser.getSelectedFiles().length; i++) {
	    		selectedFileNames[i] = chooser.getSelectedFiles()[i].getAbsolutePath();
	    	}
	    	showNewCheckBoxes(selectedFileNames);
	    }
		displayNewNumberAndSize();

		updateWindow();	
	}

    protected final void displayNewNumberAndSize() {
		if (allSize < 0) {
			System.err.println("FileDisplayPanel: invalid value for size of selected files (allSize) ");
			allSize = 0;
		}
		if (fileNumber < 0) {
			System.err.println("FileDisplayPanel: invalid value for number of selected files (fileNumber) ");
			fileNumber = 0;
		}
		infoLabel.setText(" Number of files: " + fileNumber + ", overall size (Byte): " + allSize + " - (MiB): " + allSize/1024/1024);
    }
    
    // Updates the root Window (JFrame, JDialog...) with new displayed files
    protected final void updateWindow() {
    	Window frame = (Window) SwingUtilities.getRoot(this);
    	if (frame == null) {
    		System.err.println("FileDisplayPanel: can not find root window");
    		System.out.println("402");
    		Thread.currentThread().getStackTrace();
    		return;
    	}
    	frame.pack();
    }
    
    private final void deselectParentDirs( String fileName, int index) {    	
		
		// get parent directory names:
		String splitter = Pattern.quote(System.getProperty("file.separator"));
		String[] dirNameParts =  fileName.split(splitter);
		// get full parent directory names (cut last part, the fileName it selves):
		String[] parentDirs = new String[dirNameParts.length - 1];
		parentDirs[0] = dirNameParts[0];
		for (int i = 1; i < dirNameParts.length - 1; i++) {
			parentDirs[i] = parentDirs[i-1] + File.separator + dirNameParts[i];
		}
    	
		// check previous check boxes:
		int parentIndex = parentDirs.length - 1;
		for (int i = index; i > 0; i--) { // break if first checkBox (index 0)

			String previousFileName = fc.getOriginalFileNames().get(i - 1);
			
			// check if file is invalid
			if ( fc.getAnnotatedFileNames().get(i - 1).contains(INVALID_MARKER) ) {
				continue;
			}
			// break if fileName is another directory
			if( !  previousFileName.startsWith( parentDirs[parentIndex] ) ) {
				break;
			}
			if  ( previousFileName.equals( parentDirs[parentIndex])) {
				JCheckBox check = ((JCheckBox)checkPanel.getComponent(i-1));
				if (check.isSelected() == true ) {
					check.setSelected(false);
					fileNumber--;
				}
				parentIndex--; // check next parent directory
			}			
		}					   	
    }
    
    private final void initFileNames() {
    	//System.out.println("FileDisplayPanel initFileNames");
    	if (Attachments.getFileIdentifier() == null) { // needs to check
    		Attachments.setFileIdentifier(PeaSettings.getFileIdentifier() );
    	}
		
    	// check EXTERNAL_FILE_PATH:
		if (PeaSettings.getExternalFilePath() != null) {
			// checks existence, fileIdentifier...:
			showNewCheckBox(PeaSettings.getExternalFilePath());
		}
		// check path file:
		String PATH_FILE_NAME = PeaSettings.getJarFileName() + ".path";
		File file = new File(PATH_FILE_NAME);
		if (! file.exists() ) {
			System.err.println("FileDisplayPanel: no path file specified");
			return;
		}
		if (! file.canRead() ) {
			System.err.println("FileDisplayPanel: can not read path file " + file.getName() );
			JOptionPane.showInternalMessageDialog(PswDialogView.getView(),
				"There is are file containing names of potentially encrypted files,\n" +
				" but no access to it: \n" + PATH_FILE_NAME, 
				"Info",//title
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		byte[] pathBytes = ReadResources.readExternFile( file.getName() );
		String pathString = null;
		try {
			pathString = new String(pathBytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.err.println("FileTypePanel: " + e);
			e.printStackTrace();
		}
		String[] pathNames = pathString.split("\n");
		if (pathNames == null) {
			System.err.println("FileDisplayPanel: path file does not contain any file name: " + file.getPath() );
			return;
		} else {	
			// show only valid files:
			for (int i = 0; i < pathNames.length; i++) {
				if (PswDialogBase.checkFile(pathNames[i]) == true) {
					showNewCheckBox(pathNames[i]);
				}
			}
		}	
	}
    

    public final boolean warnExecutionTime(long newNumber, long newSize) {
    	boolean returnValue = true;
    	//System.out.println(newNumber + "  " + newSize/1024/1024);

		if( (allSize + newSize > 1024 * 1024 * 1024) && (extremeSizeWarning == false) ){ // > 1 GiB
    		Object[] options = {"Go on",
                    "Cancel"};
    	    int n = JOptionPane.showOptionDialog(PswDialogView.getView(),
    	    		languagesBundle.getString("extreme_size_warning"),
    	/*            "This will take a very long time, because of the file size;\n "
    	    		+ "this program is not well suited for such a file size\n"
    	    		+ "(You can disable files by clicking their check boxes). \n"
    	            + "Are you willing to wait?",*/
    	    		languagesBundle.getString("warning"), 
    	            JOptionPane.YES_NO_OPTION,
    	            JOptionPane.QUESTION_MESSAGE,
    	            null,
    	            options,
    	            options[0]);
    	        		
    	    if (n == 0) {
    			returnValue = true;	
    			extremeSizeWarning = true;
    		} else { // cancel or close
    			returnValue = false;
    		}
    	       
		} else if ( (allSize + newSize > SIZE_LIMIT) && (sizeWarning == false) ) {

    		Object[] options = {"Go on",
            "Cancel"};
    		int n = JOptionPane.showOptionDialog(PswDialogView.getView(),
    				languagesBundle.getString("size_warning"),
//   				"This will take some time, because of the file size \n "
//    						+ "(You can disable files by clicking their check boxes). \n"
//    						+ "Do you want to continue?",
    				languagesBundle.getString("warning"),
    						JOptionPane.YES_NO_OPTION,
    						JOptionPane.QUESTION_MESSAGE,
    						null,
    						options,
    						options[0]);
        		
    		if (n == 0) {
    			sizeWarning = true; 
    			returnValue = true;				
    		} else { // cancel or close
    			returnValue = false;
    		}
		}
        	
		if (fileNumber + newNumber > FILE_NUMBER_LIMIT && numberWarning == false) {
    		Object[] options = {"Go on",
            "Cancel"};
    		int n = JOptionPane.showOptionDialog(PswDialogView.getView(),
    				languagesBundle.getString("number_warning"),
    //				"This will take some time, because of the file number \n "
   // 						+ "(You can disable files by clicking their check boxes). \n"
 //   						+ "Do you want to continue?",
    				languagesBundle.getString("warning"),
    						JOptionPane.YES_NO_OPTION,
    						JOptionPane.QUESTION_MESSAGE,
    						null,
    						options,
    						options[0]);
        		
    		if (n == 0) {
    			numberWarning = true; 
    			returnValue = true;				
    		} else { // cancel or close
    			returnValue = false;
    		}
        }		
		return returnValue;
    }

    
    public final void showNewCheckBox(String fileName) {
		if (fc.checkDoubles(fileName) == true)  {
			return;
		}
		//System.out.println("FileDisplayPanel: checkAndAddFile: " + fileName);
    	fc.checkAndAddFile(fileName);
    	
   		// only manually selected File bordered
		JCheckBox newBox = new JCheckBox();
		newBox.setBorder(BorderFactory.createMatteBorder(2,2,0,2,Color.GRAY));
		newBox.setBorderPainted(true);

   		int newIndex =  fc.getOriginalFileNames().indexOf(fileName);
		
    	if (! fc.getAnnotatedFileNames().get(newIndex).endsWith(DIRECTORY_MARKER) ) { //file.isFile() ) {
    		        	
    		newBox.setText(fc.getAnnotatedFileNames().get(newIndex));
    		
    		// check if annotatedFileName is invalid:
    		if (fc.getAnnotatedFileNames().get(newIndex).contains(INVALID_MARKER) ) {
	    		newBox.setSelected(false);
				newBox.setEnabled(false);
				newBox.setBackground(invalidColor);
    		} else {
    			long fileSize = fc.getFileSize(fileName);
				allSize += fileSize;
				fileNumber++;
				
	    		newBox.addActionListener(this);
	    		newBox.setActionCommand("fileSelection");
	    		newBox.setSelected(true);	
	    		newBox.setBackground(fileColor);	    		
    		}
    		
    	} else { // directory
    		String includedCheckText = fc.getAnnotatedFileNames().get(newIndex);

			includedCheckText = includedCheckText.substring(0, includedCheckText.length() - DIRECTORY_MARKER.length() );
			if (includedCheckText.contains(INVALID_MARKER) ) {
				newBox.setSelected(false);
				newBox.setEnabled(false);
				newBox.setBackground(invalidDirectoryColor);
				// fileNumber was added before: 
				fileNumber--;
			} else {
        		//check if at least one child file is valid: 
        		if (fc.checkIncludedChildren(newIndex) == true) {    	            			
        			newBox.setSelected(true);
    				newBox.setBackground(directoryColor);
	    			newBox.addActionListener(this);
	    			newBox.setActionCommand("dirSelection");
	    			newBox.setBackground(directoryColor);
        		} else {
        			newBox.setSelected(false);
        			includedCheckText = INVALID_MARKER + " no valid files inside: " + includedCheckText;
           			setCheckBoxInvalid(fc.getOriginalFileNames().get(newIndex), INVALID_MARKER + " no valid files inside: " + fc.getAnnotatedFileNames().get(newIndex));
           			
           			newBox.setEnabled(false);
           			newBox.setBackground(invalidDirectoryColor);  	            			
        		}
			}
			newBox.setText(includedCheckText);
		} 
    	checkPanel.add(newBox); 
    }
    
    public final void showNewCheckBoxes(String[] newFileNames) {
    	
   //for (int i=0;i<fc.getOriginalFileNames().size();i++) System.out.println(fc.getOriginalFileNames().get(i));

    	// add Checkboxes
    	for (int i = 0; i < newFileNames.length; i++) {    		

    		//File file = newFiles[i];
    		String fileName = newFileNames[i];
    //System.out.println("ne file: " + fileName);
    		if (fc.checkDoubles(newFileNames[i]) == true)  {
    			continue;
    		}
    		fc.checkAndAddFile(fileName);
    		
    		// warning for directory: once for each session
    		if (plainModus == true && directoryWarning == false && fileName.endsWith(DIRECTORY_MARKER) ) {	
    			directoryWarning = true; 
	    		int n = JOptionPane.showConfirmDialog(this,
		    			"Adding a directory includes all (hidden) sub-directories and files",
		    			"Warning", 
		    			JOptionPane.OK_CANCEL_OPTION,
		    			JOptionPane.WARNING_MESSAGE);
	    		if (n != 0) { // cancel (2)	or close warning (-1)		
	    			return; 
	    		}					    		
    		}
    		
    		// only manually selected File bordered
    		JCheckBox newBox = new JCheckBox();
    		newBox.setBorder(BorderFactory.createMatteBorder(2,2,0,2,Color.GRAY));
    		newBox.setBorderPainted(true);
    		
    		// get index (because of doubles != prevousIndex + i)
    		int newIndex =  fc.getOriginalFileNames().indexOf(fileName);
    		
        	if (! fc.getAnnotatedFileNames().get(newIndex).endsWith(DIRECTORY_MARKER) ) { //file.isFile() ) {
        		        	
        		newBox.setText(fc.getAnnotatedFileNames().get(newIndex));
        		
        		// check if annotatedFileName is invalid:
        		if (fc.getAnnotatedFileNames().get(newIndex).contains(INVALID_MARKER) ) {
    	    		newBox.setSelected(false);
    				newBox.setEnabled(false);
    				newBox.setBackground(invalidColor);
        		} else {
        			long fileSize = fc.getFileSize(fileName);
        			if (plainModus == true ) {
        				if (allSize + fileSize > SIZE_LIMIT ){        					
            				if	 (warnExecutionTime(0, fileSize) == true ) { // ok selected
            					//
            				} else {
            					fc.setAnnotatedFileNames(null);
            					fc.setOriginalFileNames(null);
            					break;
            				}
        				}
        			}
    				allSize += fileSize;
    				fileNumber++;
    				
    	    		newBox.addActionListener(this);
    	    		newBox.setActionCommand("fileSelection");
    	    		newBox.setSelected(true);
    	    		newBox.setBackground(fileColor);	    		
        		}
        		checkPanel.add(newBox);        		
        		    		
        	// directory: list all sub directories and files
        	} else {// if (file.isDirectory() ){        		
        		
        		// check if directory is valid:
        		if (fc.getAnnotatedFileNames().get(newIndex).contains(INVALID_MARKER) ) {
    	    		newBox.setSelected(false);
    				newBox.setEnabled(false);
    				newBox.setBackground(invalidDirectoryColor);
    	   	    	//newBox.setText(annotatedFileNames.get(newIndex));
            		// set text of checkBox: cut DIRECTORY_MARKER
            		String dirText = fc.getAnnotatedFileNames().get(newIndex).substring(0, fc.getAnnotatedFileNames().get(newIndex).length() - DIRECTORY_MARKER.length());
            		newBox.setText(dirText);

    	   	    	checkPanel.add(newBox);
        			continue;
        		}
    	    		    	    		
        		// check number and size and show warning dialog if > LIMIT
        		long[] dirProperty = fc.getNumberAndSize(fileName);
        		if (plainModus == true ){
        	//System.out.println(plainModus +" allSize: " + allSize + "  dirProp: " + dirProperty[1] + "  limit: " + SIZE_LIMIT);
    				if (allSize + dirProperty[1] > SIZE_LIMIT 
    						|| fileNumber + dirProperty[0] > FILE_NUMBER_LIMIT){        					
        				if	 (warnExecutionTime(dirProperty[0], dirProperty[1]) == true ) { // ok selected
        					//
        				} else {
        					fc.setAnnotatedFileNames(null);
        					fc.setOriginalFileNames(null);
        					break;
        				}
    				}       			
        		}
		    
        		fileNumber++; // for directory itself
    			dirProperty = fc.getNumberAndSize(fileName);
    			fileNumber += dirProperty[0];
    			allSize += dirProperty[1];
        			
    	    	//newBox.setText(annotatedFileNames.get(newIndex));
        		// set text of checkBox: cut DIRECTORY_MARKER
        		String dirText = fc.getAnnotatedFileNames().get(newIndex).substring(0, fc.getAnnotatedFileNames().get(newIndex).length() - DIRECTORY_MARKER.length());
        		newBox.setText(dirText);


        		newBox.setBackground(directoryColor);
        		        		
        		newBox.addActionListener(this);
        		newBox.setActionCommand("dirSelection");
        		checkPanel.add(newBox);

        		//
        		// get included files: 
        		//
        		int previousSize = fc.getOriginalFileNames().size();
    	    	// list sub-directories and files and set annotatedFileNames
    	    	fc.listFilesHierarchic(fileName );
    	    	    	    	
    	    	// display new files: 
    	    	for (int j = previousSize; j < fc.getOriginalFileNames().size(); j++) {
    	    	    	    		
    	    		JCheckBox newIncludedBox = new JCheckBox();
    	    		checkPanel.add(newIncludedBox); 
    	    		String includedCheckText = fc.getAnnotatedFileNames().get(j);
    	    		
    	    		if (includedCheckText.endsWith(DIRECTORY_MARKER) ) { // directory
    	    			includedCheckText = includedCheckText.substring(0, includedCheckText.length() - DIRECTORY_MARKER.length() );
    	    			if (includedCheckText.contains(INVALID_MARKER) ) {
    	    				newIncludedBox.setSelected(false);
    	    				newIncludedBox.setEnabled(false);
    	    				newIncludedBox.setBackground(invalidDirectoryColor);
    	    				// fileNumber was added before: 
    	    				fileNumber--;
    	    			} else {
    	            		//check if at least one child file is valid: 
    	            		if (fc.checkIncludedChildren(j) == true) {    	            			
    	        	    		if (plainModus == true) {
    	        	    			newIncludedBox.setSelected(true);
    	        	    		} else { 
    	        	    			newIncludedBox.setSelected(false);
    	        	    			fileNumber--;
    	        	    		}
        	    				newIncludedBox.setBackground(directoryColor);
        		    			newIncludedBox.addActionListener(this);
        		    			newIncludedBox.setActionCommand("dirSelection");
        		    			newIncludedBox.setBackground(directoryColor);
    	            		} else {
    	            			newIncludedBox.setSelected(false);
    	            			includedCheckText = INVALID_MARKER + " no valid files inside: " + includedCheckText;
    	               			setCheckBoxInvalid(fc.getOriginalFileNames().get(j), INVALID_MARKER + " no valid files inside: " + fc.getAnnotatedFileNames().get(j));
    	               			
    	               			newIncludedBox.setEnabled(false);
    	               			newIncludedBox.setBackground(invalidDirectoryColor);  	            			
    	            		}

    	    			}
    	    		} else { // file and undefined file types
    	    			if (includedCheckText.contains(INVALID_MARKER) ) {
    	    				newIncludedBox.setSelected(false);
    	    				newIncludedBox.setEnabled(false);
    	    				newIncludedBox.setBackground(invalidColor);
    	    				// fileNumber and fileSize was added before: 
    	    				fileNumber--;
    	    				// if not file and not directory: this will fail
    	    				allSize -= fc.getFileSize( fc.getOriginalFileNames().get(j)  );

    	    			} else {
    	    				newIncludedBox.setSelected(true);
    	    				newIncludedBox.setBackground(fileColor);
    		    			newIncludedBox.addActionListener(this);
    		    			newIncludedBox.setActionCommand("fileSelection");
    		    			newIncludedBox.setBackground(fileColor);
    	    			}
    	    		}
    	    		newIncludedBox.setText(includedCheckText);
    	    		//checkPanel.add(newIncludedBox);    	    		
    	    	}
           		//check if at least one child file is valid: 
    	    	// must be checked after adding children
        		if (fc.checkIncludedChildren(newIndex) == true) {
    	    		if (plainModus == true) {
    	    			newBox.setSelected(true);
    	    		} else { // do not add to selectedFieNames
    	    			newBox.setSelected(false);
    	    			fileNumber--;
    	    		}
        		} else {
        			dirText = INVALID_MARKER + " no valid files inside: " + fc.getAnnotatedFileNames().get(newIndex);
        			setCheckBoxInvalid(fc.getOriginalFileNames().get(newIndex), dirText);
        			newBox.setText(dirText.substring(0, dirText.length() - DIRECTORY_MARKER.length() ));
        			newBox.setEnabled(false);
    				newBox.setBackground(invalidDirectoryColor);
        			newBox.setSelected(false);
        		}
 
        	} 
    	}
    	checkPanel.revalidate();
 /*  	for(int i=0;i<fc.getOriginalFileNames().size(); i++) {
    		System.out.println("Orig: " + fc.getOriginalFileNames().get(i));
    		System.out.println("Annot: " + fc.getAnnotatedFileNames().get(i));
    	} */
    }
    
    public final void setCheckBoxInvalid(String originalFileName, String newAnnotatedFileName) {
    	
    	int index = fc.getOriginalFileNames().indexOf(originalFileName);
    	
    	String oldAnnotatedFileName = fc.getAnnotatedFileNames().get(index);
    	
    	if (oldAnnotatedFileName.contains(INVALID_MARKER) ) {
    		return;
    	}
    	// get number of leading spaces for file hierarchy:
    	int spaceNumber = 0;
    	char[] annotatedChars = fc.getAnnotatedFileNames().get(index).toCharArray();
    	for (int i = 0; i < annotatedChars.length; i++) {
    		if ( annotatedChars[i] == ' ') {
    			spaceNumber++;
    		}
    	}
    	StringBuilder spaceStringBuilder = new StringBuilder();
    	for (int i = 0; i < spaceNumber; i++) {
    		spaceStringBuilder.append(' ');
    	}
    	String spaceString = new String (spaceStringBuilder);
    	
    	fc.setAnnotatedFileName( spaceString + newAnnotatedFileName, index);
    	//fc.getAnnotatedFileNames().set(index, spaceString + newAnnotatedFileName);   
    	
    	// set text for checkBox
    	JCheckBox check = (JCheckBox) checkPanel.getComponent(index);
    	check.setText( fc.getAnnotatedFileNames().get(index) );

    	
    	if ( fc.getAnnotatedFileNames().get(index).endsWith(DIRECTORY_MARKER)) {
    		check.setBackground(invalidDirectoryColor);
    		check.setSelected(false);
    		check.setEnabled(false);
    		//deselectParentDirs( originalFileName,  index);
    	} else {
    		check.setBackground(invalidColor);
    		check.setSelected(false);
    		check.setEnabled(false);
    		//deselectParentDirs( originalFileName,  index);
    		allSize -= fc.getFileSize(originalFileName );
    	}
    	fileNumber--;
    }
   
    protected final String[] getUnselectedValidFileNames() {

    	int checkNumber = checkPanel.getComponentCount();
    	String[] selectedFileNames = new String[checkNumber];
    	//System.out.println("allFileNumber: " + allFileNumber + "  orig.length: " + fc.getOriginalFileNames().size());
    	
    	// check if selected and add originalFileName
    	int selectedFileNumber = 0; // counts selected files
    	for (int i = 0; i < checkNumber; i++) {
    		JCheckBox check = (JCheckBox) checkPanel.getComponent(i);
    		if (  check.isSelected() ) {
    			continue;
    		}
    		if (fc.getAnnotatedFileNames().get(i).contains(INVALID_MARKER)){
				continue;
			}
   			if (fc.getAnnotatedFileNames().get(i).endsWith(DIRECTORY_MARKER)){
				continue;
			}
			selectedFileNames[selectedFileNumber] = fc.getOriginalFileNames().get(i);
			selectedFileNumber++;    		
    	}
    	if (selectedFileNumber == 0){
    		return null;
    	} else {
        	// cut null Strings
        	String[] result = new String[selectedFileNumber];
        	System.arraycopy(selectedFileNames, 0, result, 0, result.length);
        	//for(int i=0; i < result.length;i++) System.out.println("selected: " + result[i]);
        	return result;
    	}
    }

    public final String[] getSelectedFileNames() {

    	int allFileNumber = checkPanel.getComponentCount();
    	String[] selectedFileNames = new String[allFileNumber];
    	//System.out.println("allFileNumber: " + allFileNumber + "  orig.length: " + fc.getOriginalFileNames().size());
    	
    	// check if selected and add originalFileName
    	int selectedFileNumber = 0; // counts selected files
    	for (int i = 0; i < allFileNumber; i++) {
    		if ( ( (JCheckBox) checkPanel.getComponent(i)).isSelected() ) {
    			selectedFileNames[selectedFileNumber] = fc.getOriginalFileNames().get(i);
    			selectedFileNumber++;
    		} else {
    			//
    		}
    	}
    	// cut null Strings
    	String[] result = new String[selectedFileNumber];
    	int resultIndex = 0;
    	for (int i = 0; i < allFileNumber; i++) {
    		if (selectedFileNames[i] != null){
    			result[resultIndex++] = selectedFileNames[i];
    		}
    	}
    	//System.arraycopy(selectedFileNames, 0, result, 0, result.length);
    	//for(int i=0; i < result.length;i++) System.out.println("selected: " + result[i]);
    	return result;
    }

    
    //======================================
    // Getter & Setter
       
    protected final static String getInvalidMarker() {
    	return INVALID_MARKER;
    }
    protected final static String getDirectoryMarker() {
    	return DIRECTORY_MARKER;
    }
    protected final boolean getPlainModus() {
    	return plainModus;
    }
    protected final long getAllSize() {
    	return allSize;
    }
    protected final int getFileNumber() {
    	return fileNumber;
    }
    protected final void setAllSize(long newSize) {
    	allSize = newSize;
    }
    protected final void setFileNumber(int newNumber) {
    	fileNumber = newNumber;
    }
    protected final int getFileNumberLimit() {
    	return FILE_NUMBER_LIMIT;
    }
    protected final long getSizeLimit() {
    	return SIZE_LIMIT;
    }
    protected final FileComposer getFileComposer() {
    	return fc;
    }
    protected final JPanel getCheckPanel() {
    	return checkPanel;
    }
    protected final void removeAddButton() {
    	this.remove(buttonPanel);
    }


	//================================================
	// ProgrssBar to show progress: methods and  inner class

    // starts progress bar dialog if execution > 1 second
	public final void startProgressTask(){
		task = new ProgressTask();
		task.execute();
	}	
	// updates the progress bar
	public final void setProgressValue(int value){
		task.setValue(value);
	}
	// closes the progress bar dialog 
	public final void closeProgressTask(){
		task.close();
	}
	   
	public class ProgressTask extends SwingWorker<Void, Void> {
		
    	JProgressBar threadProgressBar;
    	
    	JDialog dialog;
    	
    	// Show whether the process is finished: (set in close)
    	boolean processFinished = false;

        @Override
        public Void doInBackground() {
        	
    		//System.out.println(" eventThread: " + SwingUtilities.isEventDispatchThread() );
   	        
        	// show only if > 1 second:
        	try {
        		Thread.sleep(1000);
        	} catch (InterruptedException e) {
        		System.err.println("progressTask: thread interrupted " + e.toString());
        		return null;
        	}
        	// check if process is done:
        	if (processFinished == false) {
               	String modusText = ""; // used in title of dialog
            	if (plainModus == true) {
            		modusText = "encryption";
            	} else {
            		modusText = "decryption";
            	}

            	// create progress bar: 
            	threadProgressBar = new JProgressBar();
        		threadProgressBar = new JProgressBar(0,1000);

        		threadProgressBar.setValue(0);

        		threadProgressBar.setStringPainted(true);
        		threadProgressBar.setPreferredSize(new Dimension(400, 30));
        		
    	 
            	JOptionPane pane = new JOptionPane( threadProgressBar,
            	   JOptionPane.PLAIN_MESSAGE, 
            	    JOptionPane.DEFAULT_OPTION);

            	dialog = pane.createDialog(ftp, "File " + modusText + " in progress...");// title
            	
            	dialog.setVisible(true);
            /*	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            	threadProgressBar.setValue(80);
            	threadProgressBar.setIndeterminate(false);
            	threadProgressBar.repaint();
            	threadProgressBar.paint(threadProgressBar.getGraphics());
            	dialog.revalidate();
            	dialog.pack();
            	dialog.setVisible(true);*/
            	

        	} else {
        		processFinished = false;
        	}
            return null;
        }	 
        public void close() { // is called when work is done
        	processFinished = true; // if the dialog is not already open: do not open
        	if (dialog != null) {
        		dialog.dispose(); 
        		task.cancel(true);
        	}
        }
        public void setValue(int value){

        	if (threadProgressBar != null){
	        	threadProgressBar.setValue(value);
	        	threadProgressBar.paint(threadProgressBar.getGraphics());
        	} 
        }
    }
}
