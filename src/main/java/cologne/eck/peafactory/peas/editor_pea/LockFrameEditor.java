package cologne.eck.peafactory.peas.editor_pea;

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
 * Main frame of editor pea.  
 */

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.undo.*;

import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.gui.LockFrame;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Converter;



@SuppressWarnings("serial")
class LockFrameEditor extends LockFrame implements MouseListener, DocumentListener {
	
	private static LockFrameEditor frame;
	protected static JTextPane textPane;
	
	private static boolean popupStarted = false;
	private CopyCutPastePopup popup;
	protected static UndoManager manager;// = new UndoManager();
	protected static RTFEditorKit kit = null;
	
	private static boolean docChangeUnsaved = false;
	private boolean firstChange = true;

	

	protected static PswDialogEditor dialog;


	private LockFrameEditor(PswDialogEditor _dialog) {
		frame = this;
		dialog = _dialog;
		
	    this.setIconImage( PswDialogView.getImage() );
	    
		this.addWindowListener(this);// for windowClosing
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		this.setContentPane(contentPane);
		
		LockFrameEditorMenu menu = new LockFrameEditorMenu();
		contentPane.add(menu);
		
		textPane = new JTextPane();	
		kit = new RTFEditorKit();
		textPane.setEditorKit(kit);
		textPane.setEditable(true);
		textPane.setBackground(new Color(231, 231, 231) );
		textPane.setDragEnabled(true);
		textPane.setToolTipText("CTRL + ...  R (undo), W (redo), X (cut), C (copy), V (paste)");
		textPane.setText("äöü");
		
		// Cut-Copy-Paste.Menue: 
		textPane.addMouseListener(this);

		// Undo/Redo: key and menu
		manager = new UndoManager();
		UndoAction undoAction = new UndoAction(manager);
		RedoAction redoAction = new RedoAction(manager);
		textPane.getDocument().addUndoableEditListener(manager);
		   
		//keyStroke: STRG + R, STRG + W
		textPane.registerKeyboardAction(undoAction, KeyStroke.getKeyStroke(
		            KeyEvent.VK_R, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		textPane.registerKeyboardAction(redoAction, KeyStroke.getKeyStroke(
		            KeyEvent.VK_W, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
			

		JScrollPane editorScrollPane = new JScrollPane(textPane);
		//editorScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(350, 250));
		editorScrollPane.setMinimumSize(new Dimension(50, 30));

		contentPane.add(editorScrollPane);
		
		this.setLocation(100, 100);
		this.setMinimumSize(new Dimension(450, 500));
	}
	
	protected static LockFrameEditor getInstance(PswDialogEditor pswDialog) {
		LockFrameEditor frameInstance = null; 
		if (frame == null) {
			frameInstance = new LockFrameEditor(pswDialog);
		} else {
			//
		}
		return frameInstance;
	}
	


	@Override
	public void mouseClicked(MouseEvent mce) {
		if(mce.getModifiers() == InputEvent.BUTTON3_MASK){ //rechte Maustaste
			if (popupStarted == false) {
				popup = new CopyCutPastePopup (textPane);
				try {
					popup.setLocation(mce.getLocationOnScreen());				
					popup.setVisible(true);				
					popupStarted = true;
				} catch (NullPointerException npe) {
					System.err.println("Popup in EditorLockFrame: NullPointer");
				}						
			}
			else {
				popup.setVisible(false);
				popupStarted = false;
			}
		}
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}
	
	//--------------------------------------------------------------------------
	// Getter & setter
	
	protected void setChars( char[] textChars ) {

		StyledDocument doc = textPane.getStyledDocument();
		try {
			doc.insertString(0, new String(textChars), null );
		} catch (BadLocationException e) {
			System.err.println("LockFrameEditor: " + e);
			e.printStackTrace();
		}		
	}

	//----------------------------------------------------------------------------------------
	// classes: 
	
	// The Undo action
	private class UndoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public UndoAction(UndoManager manager) {
	      this.manager = manager;
	    }
	    public void actionPerformed(ActionEvent evt) {
	    	
	    	try {
	  	    	if (manager.canUndo()) {
	  	        manager.undo();
	  	    } else {
		    	JOptionPane.showMessageDialog(frame,
		    			"Cannot undo.");
		    	System.err.println("LockFrameEditor: undo action failed");
		    	}
	  	    } catch (CannotUndoException e) {
	  	        Toolkit.getDefaultToolkit().beep();
	  	    }
	    	
	    }
	    private UndoManager manager;
	}
	// The Redo action
	private class RedoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public RedoAction(UndoManager manager) {
	      this.manager = manager;
	    }
	    public void actionPerformed(ActionEvent evt) {
	      try {
	    	  if (manager.canRedo()){
	    		  manager.redo();
	    	  } else {
	    		  System.err.println("LockFrameEditor: redo action failed");
	    	  }
	      } catch (CannotRedoException e) {
	        Toolkit.getDefaultToolkit().beep();
	      }
	    }
	    private UndoManager manager;
	}
	
	private class CopyCutPastePopup extends JPopupMenu implements ActionListener {

		private static final long serialVersionUID = 1L;
		private JMenuItem cut;
		private JMenuItem copy;
		private JMenuItem paste;
		private JTextComponent area;
		
		public CopyCutPastePopup (Component invoker) {
			
			area = (JTextComponent)invoker;
			cut = new JMenuItem("cut");
		    cut.addActionListener(this);
		    this.add(cut);
		    this.addSeparator();
		    copy = new JMenuItem("copy");
		    copy.addActionListener(this);
		    this.add(copy);
		    this.addSeparator();
		    paste = new JMenuItem("paste");
		    paste.addActionListener(this);
		    this.add(paste);		    
		    JPopupMenu.setDefaultLightWeightPopupEnabled(true);
		    this.setBorderPainted(true);		    
		    this.setInvoker(invoker);		    
		    this.pack();
		}

		@Override
		public void actionPerformed(ActionEvent ape) {
			
			if (ape.getSource() == cut) {
				area.cut();
				LockFrameEditor.popupStarted = false;
			}
			if (ape.getSource() == copy) {
				area.copy();
				LockFrameEditor.popupStarted = false;
			}
			if (ape.getSource() == paste) {
				area.paste();
				LockFrameEditor.popupStarted = false;
			}		
		}	
	}

	@Override
	protected void windowClosingCommands() {
		
		if (docChangeUnsaved == true) {
			int n = JOptionPane.showConfirmDialog(
				    this,
				    "Save changes?",
				    "Unsaved Changes",
				    JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				byte[] plainBytes = getPlainBytes();
				saveContent(null, plainBytes, PswDialogBase.getEncryptedFileName());
			}
		}

		StyledDocument doc = textPane.getStyledDocument();
		try {
			doc.remove(0, doc.getLength() );
		} catch (BadLocationException e) {
			System.err.println("LockFrameEditor: " + e);
			e.printStackTrace();
		}			
		frame.dispose();
		CipherStuff.getInstance().getSessionKeyCrypt().clearKeys();
		System.exit(0);			
	}

	@Override
	protected byte[] getPlainBytes() {
		
		DefaultStyledDocument doc = (DefaultStyledDocument) textPane.getStyledDocument();
		
		byte[] plainBytes = null;

		// UndoListener can not be serialized.. 
		textPane.getDocument().removeUndoableEditListener(manager);
		plainBytes = Converter.serialize(doc);
	    textPane.getDocument().addUndoableEditListener(manager);
		textPane.getDocument().addDocumentListener(this);

		return plainBytes;
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		if (firstChange == true) {
			firstChange = false;
		} else {
			docChangeUnsaved = true;		
		}
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		docChangeUnsaved = true;	
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		docChangeUnsaved = true;		
	}

	/**
	 * @return the docChangeUnsaved
	 */
	public static boolean isDocChangeUnsaved() {
		return docChangeUnsaved;
	}

	/**
	 * @param docChangeUnsaved the docChangeUnsaved to set
	 */
	public static void setDocChangeUnsaved(boolean docChangeUnsaved) {
		LockFrameEditor.docChangeUnsaved = docChangeUnsaved;
	}
}
