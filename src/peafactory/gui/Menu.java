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
 * Menu of MainView. 
 * All setting for pea production. 
 */

import javax.swing.JMenuBar;

import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.crypto.HashStuff;
import cologne.eck.peafactory.crypto.KeyDerivation;
import cologne.eck.peafactory.crypto.kdf.BcryptKDF;
import cologne.eck.peafactory.crypto.kdf.CatenaKDF;
import cologne.eck.peafactory.crypto.kdf.PomeloKDF;
import cologne.eck.peafactory.crypto.kdf.ScryptKDF;
import cologne.eck.peafactory.peagen.FileModifier;
import cologne.eck.peafactory.peas.gui.PasswordGeneratorDialog;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.Shacal2Engine;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.engines.SerpentEngine;
import org.bouncycastle.crypto.engines.ThreefishEngine;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.digests.RIPEMD320Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.WhirlpoolDigest;//512 bit
import org.bouncycastle.crypto.digests.SkeinDigest;//512
import org.bouncycastle.crypto.digests.SHA512Digest;//512
//import org.bouncycastle.crypto.digests.RIPEMD256Digest;//256 bit



@SuppressWarnings("serial")
public class Menu extends JMenuBar implements ActionListener {

	private static int securityLevel = 3;//standard 1-5
	
	private static ResourceBundle languageBundle;// = PeaFactory.getLanguagesBundle();
	
	public Menu(ResourceBundle _languageBundle) {
	
		languageBundle = _languageBundle;

		setBorder(new EtchedBorder() );
		setMinimumSize(new Dimension(200, 50));

		JMenu menu = new JMenu(languageBundle.getString("menu"));
		menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		menu.setMnemonic(KeyEvent.VK_M);
		this.add(menu);
		
		JMenuItem newItem = new JMenuItem(languageBundle.getString("new_project"));
		newItem.setActionCommand("newProject");
		newItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		newItem.setMnemonic(KeyEvent.VK_N);
		newItem.addActionListener(this);
		menu.add(newItem);
/*		
		JMenuItem newBlankItem = new JMenuItem(languageBundle.getString("blank_check"));
		newBlankItem.setActionCommand("newBlankProject");
		newBlankItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		newBlankItem.setMnemonic(KeyEvent.VK_B);
		newBlankItem.addActionListener(this);
		menu.add(newBlankItem);*/
		
		JMenuItem randomItem = new JMenuItem(languageBundle.getString("random_password"));
		randomItem.setActionCommand("randomPassword");
		randomItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		randomItem.setMnemonic(KeyEvent.VK_R);
		randomItem.addActionListener(this);
		menu.add(randomItem);
		
		JMenuItem quitItem = new JMenuItem(languageBundle.getString("quit"));
		quitItem.setActionCommand("quit");
		quitItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		quitItem.setMnemonic(KeyEvent.VK_B);
		quitItem.addActionListener(this);
		menu.add(quitItem);
		
		


		JMenu setMenu = new JMenu(languageBundle.getString("settings"));
		setMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setMenu.setMnemonic(KeyEvent.VK_S);
		this.add(setMenu);
		

		JMenuItem setPeaSettings = new JMenuItem(languageBundle.getString("general_pea_settings"));
		setPeaSettings.setActionCommand("generalPeaSettings"); 
		setPeaSettings.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setPeaSettings.setMnemonic(KeyEvent.VK_T);
		setPeaSettings.addActionListener(this);
		setMenu.add(setPeaSettings);
		
		JMenu setLevel = new JMenu(languageBundle.getString("set_security_level")); 
		setLevel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setLevel.setMnemonic(KeyEvent.VK_L);
		setMenu.add(setLevel);		

		JMenuItem setThoughtless = new JMenuItem(languageBundle.getString("thoughtless"));
		setThoughtless.setActionCommand("setThoughtless"); 
		setThoughtless.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setThoughtless.setMnemonic(KeyEvent.VK_T);
		setThoughtless.addActionListener(this);
		setLevel.add(setThoughtless);
		
		JMenuItem setLow = new JMenuItem(languageBundle.getString("low"));
		setLow.setActionCommand("setLow"); 
		setLow.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setLow.setMnemonic(KeyEvent.VK_L);
		setLow.addActionListener(this);
		setLevel.add(setLow);

		JMenuItem setStandard = new JMenuItem(languageBundle.getString("standard"));
		setStandard.setActionCommand("setStandard"); 
		setStandard.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setStandard.setMnemonic(KeyEvent.VK_S);
		setStandard.addActionListener(this);
		setLevel.add(setStandard);

		JMenuItem setHigh = new JMenuItem(languageBundle.getString("high"));
		setHigh.setActionCommand("setHigh"); 
		setHigh.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setHigh.setMnemonic(KeyEvent.VK_H);
		setHigh.addActionListener(this);
		setLevel.add(setHigh);

		JMenuItem setParanoid = new JMenuItem(languageBundle.getString("paranoid"));
		setParanoid.setActionCommand("setParanoid"); 
		setParanoid.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setParanoid.setMnemonic(KeyEvent.VK_P);
		setParanoid.addActionListener(this);
		setLevel.add(setParanoid);

		JMenu cryptMenu = new JMenu(languageBundle.getString("set_crypt")); 
		cryptMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		cryptMenu.setMnemonic(KeyEvent.VK_C);
		setMenu.add(cryptMenu);
		
		JMenu setKeyDerivation = new JMenu(languageBundle.getString("set_kdf")); 
		setKeyDerivation.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setKeyDerivation.setMnemonic(KeyEvent.VK_K);
		cryptMenu.add(setKeyDerivation);		
		
		JMenuItem setDragonfly = new JMenuItem(languageBundle.getString("set_dragonfly"));
		setDragonfly.setActionCommand("setDragonfly"); 
		setDragonfly.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setDragonfly.setMnemonic(KeyEvent.VK_D);
		setDragonfly.addActionListener(this);
		setKeyDerivation.add(setDragonfly);		
		
		JMenuItem setButterfly = new JMenuItem(languageBundle.getString("set_butterfly"));
		setButterfly.setActionCommand("setButterfly"); 
		setButterfly.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setButterfly.setMnemonic(KeyEvent.VK_U);
		setButterfly.addActionListener(this);
		setKeyDerivation.add(setButterfly);		
		
		JMenuItem setScrypt = new JMenuItem(languageBundle.getString("set_scrypt"));
		setScrypt.setActionCommand("setScrypt"); 
		setScrypt.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setScrypt.setMnemonic(KeyEvent.VK_S);
		setScrypt.addActionListener(this);
		setKeyDerivation.add(setScrypt);		
				
		JMenuItem setPomelo = new JMenuItem(languageBundle.getString("set_pomelo"));
		setPomelo.setActionCommand("setPomelo"); 
		setPomelo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setPomelo.setMnemonic(KeyEvent.VK_P);
		setPomelo.addActionListener(this);
		setKeyDerivation.add(setPomelo);	
		
		JMenuItem setBcrypt = new JMenuItem(languageBundle.getString("set_bcrypt"));
		setBcrypt.setActionCommand("setBcrypt"); 
		setBcrypt.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setBcrypt.setMnemonic(KeyEvent.VK_B);
		setBcrypt.addActionListener(this);
		setKeyDerivation.add(setBcrypt);		
		
		
		JMenu setKDFParams = new JMenu(languageBundle.getString("set_params"));
		setKDFParams.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setKDFParams.setMnemonic(KeyEvent.VK_P);
		setKeyDerivation.add(setKDFParams);
		
		JMenuItem setCatenaParameters = new JMenuItem(languageBundle.getString("set_catena_params"));
		setCatenaParameters.setActionCommand("setCatenaParameters"); 
		setCatenaParameters.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setCatenaParameters.setMnemonic(KeyEvent.VK_P);
		setCatenaParameters.addActionListener(this);
		setKDFParams.add(setCatenaParameters);
		
		JMenuItem setScryptParameters = new JMenuItem(languageBundle.getString("set_scrypt_params"));
		setScryptParameters.setActionCommand("setScryptParameters"); 
		setScryptParameters.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setScryptParameters.setMnemonic(KeyEvent.VK_S);
		setScryptParameters.addActionListener(this);
		setKDFParams.add(setScryptParameters);
		
		JMenuItem setPomeloParameters = new JMenuItem(languageBundle.getString("set_pomelo_params"));
		setPomeloParameters.setActionCommand("setPomeloParameters"); 
		setPomeloParameters.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setPomeloParameters.setMnemonic(KeyEvent.VK_P);
		setPomeloParameters.addActionListener(this);
		setKDFParams.add(setPomeloParameters);
		
		JMenuItem setBcryptParameters = new JMenuItem(languageBundle.getString("set_bcrypt_params"));
		setBcryptParameters.setActionCommand("setBcryptParameters"); 
		setBcryptParameters.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setBcryptParameters.setMnemonic(KeyEvent.VK_B);
		setBcryptParameters.addActionListener(this);
		setKDFParams.add(setBcryptParameters);	

		
		JMenu setCipherAlgo = new JMenu(languageBundle.getString("set_cipher")); 
		setCipherAlgo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setCipherAlgo.setMnemonic(KeyEvent.VK_C);
		cryptMenu.add(setCipherAlgo);

		JMenuItem setTwofish = new JMenuItem(languageBundle.getString("set_twofish"));
		setTwofish.setActionCommand("setTwofish"); 
		setTwofish.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setTwofish.setMnemonic(KeyEvent.VK_T);
		setTwofish.addActionListener(this);
		setCipherAlgo.add(setTwofish);		
		
		JMenuItem setAES256 = new JMenuItem(languageBundle.getString("set_aes"));
		setAES256.setActionCommand("setAES"); 
		setAES256.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setAES256.setMnemonic(KeyEvent.VK_A);
		setAES256.addActionListener(this);
		setCipherAlgo.add(setAES256);
		
		JMenuItem setShacal2 = new JMenuItem(languageBundle.getString("set_shacal2"));
		setShacal2.setActionCommand("setShacal2"); 
		setShacal2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setShacal2.setMnemonic(KeyEvent.VK_H);
		setShacal2.addActionListener(this);
		setCipherAlgo.add(setShacal2);		
		
		JMenuItem setSerpent = new JMenuItem(languageBundle.getString("set_serpent"));
		setSerpent.setActionCommand("setSerpent"); 
		setSerpent.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setSerpent.setMnemonic(KeyEvent.VK_S);
		setSerpent.addActionListener(this);
		setCipherAlgo.add(setSerpent);
		
		JMenuItem setThreefish256 = new JMenuItem(languageBundle.getString("set_threefish256"));
		setThreefish256.setActionCommand("setThreefish256"); 
		setThreefish256.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setThreefish256.setMnemonic(KeyEvent.VK_H);
		setThreefish256.addActionListener(this);
		setCipherAlgo.add(setThreefish256);

		JMenuItem setThreefish512 = new JMenuItem(languageBundle.getString("set_threefish512"));
		setThreefish512.setActionCommand("setThreefish512"); 
		setThreefish512.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setThreefish512.setMnemonic(KeyEvent.VK_R);
		setThreefish512.addActionListener(this);
		setCipherAlgo.add(setThreefish512);

		JMenuItem setThreefish1024 = new JMenuItem(languageBundle.getString("set_threefish1024"));
		setThreefish1024.setActionCommand("setThreefish1024"); 
		setThreefish1024.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setThreefish1024.setMnemonic(KeyEvent.VK_E);
		setThreefish1024.addActionListener(this);
		setCipherAlgo.add(setThreefish1024);		
		
		JMenuItem setAESFast = new JMenuItem(languageBundle.getString("set_aes_fast"));
		setAESFast.setActionCommand("setAESFast"); 
		setAESFast.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setAESFast.setMnemonic(KeyEvent.VK_F);
		setAESFast.addActionListener(this);
		setCipherAlgo.add(setAESFast);	

		
		JMenu setHashAlgo = new JMenu(languageBundle.getString("set_hash")); 
		setHashAlgo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setHashAlgo.setMnemonic(KeyEvent.VK_H);
		cryptMenu.add(setHashAlgo);

		JMenuItem setBLAKE512 = new JMenuItem(languageBundle.getString("set_blake512"));
		setBLAKE512.setActionCommand("setBlake512"); 
		setBLAKE512.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setBLAKE512.setMnemonic(KeyEvent.VK_B);
		setBLAKE512.addActionListener(this);
		setHashAlgo.add(setBLAKE512); 
		
		
		JMenuItem setSkein512 = new JMenuItem(languageBundle.getString("set_skein512"));
		setSkein512.setActionCommand("setSkein512"); 
		setSkein512.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setSkein512.setMnemonic(KeyEvent.VK_S);
		setSkein512.addActionListener(this);
		setHashAlgo.add(setSkein512);	
		
		JMenuItem setWhirlpool = new JMenuItem(languageBundle.getString("set_whirlpool"));
		setWhirlpool.setActionCommand("setWhirlpool"); 
		setWhirlpool.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setWhirlpool.setMnemonic(KeyEvent.VK_W);
		setWhirlpool.addActionListener(this);
		setHashAlgo.add(setWhirlpool);
		
		JMenuItem setKeccak = new JMenuItem(languageBundle.getString("set_sha3") + " (Keccak)");
		setKeccak.setActionCommand("setKeccak"); 
		setKeccak.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setKeccak.setMnemonic(KeyEvent.VK_K);
		setKeccak.addActionListener(this);
		setHashAlgo.add(setKeccak);
		
		JMenuItem setSHA512 = new JMenuItem(languageBundle.getString("set_sha512"));
		setSHA512.setActionCommand("setSha512"); 
		setSHA512.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setSHA512.setMnemonic(KeyEvent.VK_H);
		setSHA512.addActionListener(this);
		setHashAlgo.add(setSHA512);
		
		JMenuItem setSHA384 = new JMenuItem(languageBundle.getString("set_sha384"));
		setSHA384.setActionCommand("setSha384"); 
		setSHA384.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setSHA384.setMnemonic(KeyEvent.VK_A);
		setSHA384.addActionListener(this);
		setHashAlgo.add(setSHA384);
		
		JMenuItem setRipemd320 = new JMenuItem(languageBundle.getString("set_ripemd320"));
		setRipemd320.setActionCommand("setRipemd320"); 
		setRipemd320.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setRipemd320.setMnemonic(KeyEvent.VK_R);
		setRipemd320.addActionListener(this);
		setHashAlgo.add(setRipemd320);

/*		JMenuItem setRipemd256 = new JMenuItem(languageBundle.getString("set_ripemd256"));
		setRipemd256.setActionCommand("setRipemd256"); 
		setRipemd256.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setRipemd256.setMnemonic(KeyEvent.VK_I);
		setRipemd256.addActionListener(this);
		setHashAlgo.add(setRipemd256); */


		JMenuItem keyboardItem = new JMenuItem(languageBundle.getString("keyboard"));
		keyboardItem.setActionCommand("keyboard");
		keyboardItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		keyboardItem.setMnemonic(KeyEvent.VK_K);
		keyboardItem.addActionListener(this);
		setMenu.add(keyboardItem);


		JMenuItem pswGeneratorItem = new JMenuItem(languageBundle.getString("psw_generator"));
		pswGeneratorItem.setActionCommand("psw_generator");
		pswGeneratorItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		pswGeneratorItem.setMnemonic(KeyEvent.VK_P);
		pswGeneratorItem.addActionListener(this);
		setMenu.add(pswGeneratorItem);
	
		
		JMenuItem setImageParameters = new JMenuItem(languageBundle.getString("set_image_params"));
		setImageParameters.setActionCommand("setImageParameters"); 
		setImageParameters.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setImageParameters.setMnemonic(KeyEvent.VK_I);
		setImageParameters.addActionListener(this);
		setMenu.add(setImageParameters);
		
		JMenu setLanguage = new JMenu(languageBundle.getString("set_language")); 
		setLanguage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setLanguage.setMnemonic(KeyEvent.VK_L);
		setMenu.add(setLanguage);
		
		JMenuItem setDE = new JMenuItem(languageBundle.getString("set_de"));
		setDE.setActionCommand("setDE"); 
		setDE.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setDE.setMnemonic(KeyEvent.VK_D);
		setDE.addActionListener(this);
		setLanguage.add(setDE);

		JMenuItem setEN = new JMenuItem(languageBundle.getString("set_en"));
		setEN.setActionCommand("setEN"); 
		setEN.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		setEN.setMnemonic(KeyEvent.VK_E);
		setEN.addActionListener(this);
		setLanguage.add(setEN);
		

		JMenu helpMenu = new JMenu(languageBundle.getString("help"));
		helpMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		helpMenu.setMnemonic(KeyEvent.VK_H);
		this.add(helpMenu);
		
		JMenuItem noteItem = new JMenuItem(languageBundle.getString("notes_description"));
		noteItem.setActionCommand("notes");
		noteItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		noteItem.setMnemonic(KeyEvent.VK_N);
		noteItem.addActionListener(this);
		helpMenu.add(noteItem);
		
		JMenuItem editorItem = new JMenuItem(languageBundle.getString("editor_description"));
		editorItem.setActionCommand("editor");
		editorItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		editorItem.setMnemonic(KeyEvent.VK_E);
		editorItem.addActionListener(this);
		helpMenu.add(editorItem);
		
		JMenuItem imageItem = new JMenuItem(languageBundle.getString("image_description"));
		imageItem.setActionCommand("image");
		imageItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		imageItem.setMnemonic(KeyEvent.VK_I);
		imageItem.addActionListener(this);
		helpMenu.add(imageItem);
		
		JMenuItem fileItem = new JMenuItem(languageBundle.getString("file_description"));
		fileItem.setActionCommand("file");
		fileItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		fileItem.setMnemonic(KeyEvent.VK_F);
		fileItem.addActionListener(this);
		helpMenu.add(fileItem);
		
		JMenuItem keyboardInfoItem = new JMenuItem("Info: " + languageBundle.getString("keyboard"));
		keyboardInfoItem.setActionCommand("keyboard_info");
		keyboardInfoItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		keyboardInfoItem.setMnemonic(KeyEvent.VK_K);
		keyboardInfoItem.addActionListener(this);
		helpMenu.add(keyboardInfoItem);	
		JMenuItem howToItem = new JMenuItem(languageBundle.getString("how_to_use"));
		howToItem.setActionCommand("howToUse");
		howToItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		howToItem.setMnemonic(KeyEvent.VK_U);
		howToItem.addActionListener(this);
		helpMenu.add(howToItem);
		
		JMenuItem problemHelp = new JMenuItem(languageBundle.getString("problem_help"));
		problemHelp.setActionCommand("problemHelp");
		problemHelp.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		problemHelp.setMnemonic(KeyEvent.VK_P);
		problemHelp.addActionListener(this);
		helpMenu.add(problemHelp);
		
		JMenuItem aboutLicense = new JMenuItem(languageBundle.getString("about_license"));
		aboutLicense.setActionCommand("aboutLicense");
		aboutLicense.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		aboutLicense.setMnemonic(KeyEvent.VK_L);
		aboutLicense.addActionListener(this);
		helpMenu.add(aboutLicense);		
	}
	

	@Override
	public void actionPerformed(ActionEvent ape) {
		
		//JComponent source = (JComponent) ape.getSource();
		String command = ape.getActionCommand();
		
		//Menu
		if (command.equals("newProject")) {
			ProjectSelection proj = new ProjectSelection();
			Point p = MainView.getFrameLocation();
			proj.setLocation( (int)p.getX() + 100, (int)p.getY() + 60 );
			proj.setVisible(true);
		} else if (command.equals("randomPassword")) {			
			PasswordGeneratorDialog pg = new PasswordGeneratorDialog(PeaFactory.getFrame() );
			pg.setVisible(true);
		} else if (command.equals("keyboard")) {
			int input = JOptionPane.showConfirmDialog(PeaFactory.getFrame(), 
					languageBundle.getString("add_keyboard")," ",
                    JOptionPane.YES_NO_OPTION);
			if (input == 0) {
			FileModifier.setSetKeyboard(true);
			} else {
				FileModifier.setSetKeyboard(false);
			}
		} else if (command.equals("psw_generator")){
			int input = JOptionPane.showConfirmDialog(PeaFactory.getFrame(), 
					languageBundle.getString("add_psw_generator")," ",
                    JOptionPane.YES_NO_OPTION);
			if (input == 0) {
				FileModifier.setPswGenerator(true);
			} else {
				FileModifier.setPswGenerator(false);
			}
		} else if (command.equals("quit")) {
			System.exit(0);
			
		} else if (command.equals("generalPeaSettings")) {
			
			@SuppressWarnings("unused")
			GeneralPeaSettings imageSetting = new GeneralPeaSettings();			

			
		}else if (command.equals("setThoughtless")) {			
			securityLevel = 1;
			setSecurityLevel(1);
		} else if (command.equals("setLow")) {
			securityLevel = 2;
			setSecurityLevel(2);
		} else if (command.equals("setStandard")) {
			securityLevel = 3;
			setSecurityLevel(3);
		} else if (command.equals("setHigh")) {
			securityLevel = 4;
			setSecurityLevel(4);		
		} else if (command.equals("setParanoid")) {
			securityLevel = 5;
			setSecurityLevel(5);
			
			
		} else if (command.equals("setBcrypt")) {
			setSecurityLevel(securityLevel);
			KeyDerivation.setKdf( new BcryptKDF() );
		} else if (command.equals("setScrypt")) {
			setSecurityLevel(securityLevel);
			KeyDerivation.setKdf( new ScryptKDF() );
		} else if (command.equals("setDragonfly")) {
			setSecurityLevel(securityLevel);
			CatenaKDF.setVersionID("Dragonfly-Full");
			KeyDerivation.setKdf( new CatenaKDF() );
		} else if (command.equals("setButterfly")) {
			setSecurityLevel(securityLevel);
			CatenaKDF.setVersionID("Butterfly-Full");
			KeyDerivation.setKdf( new CatenaKDF() );
		} else if (command.equals("setPomelo")) {
			setSecurityLevel(securityLevel);
			KeyDerivation.setKdf( new PomeloKDF() );
			
			
		} else if (command.equals("setBcryptParameters")) {

			@SuppressWarnings("unused")
			BcryptSetting bcryptSetting = new BcryptSetting();
			
		} else if (command.equals("setPomeloParameters")) {

			@SuppressWarnings("unused")
			PomeloSetting pomeloSetting = new PomeloSetting();

		} else if (command.equals("setScryptParameters")) {
			
			@SuppressWarnings("unused")
			ScryptSetting scryptSetting = new ScryptSetting();

		} else if (command.equals("setCatenaParameters")) {
			
			@SuppressWarnings("unused")
			CatenaSetting catenaSetting = new CatenaSetting();			
			
		} else if (command.equals("setImageParameters")) {
			
			@SuppressWarnings("unused")
			ImageSetting imageSetting = new ImageSetting();			

			 
		} else if (command.equals("setShacal2")) {
			CipherStuff.setCipherAlgo( new Shacal2Engine() ); 
		} else if (command.equals("setThreefish256")) {
			CipherStuff.setCipherAlgo( new ThreefishEngine(256) ); 
		} else if (command.equals("setThreefish512")) {
			CipherStuff.setCipherAlgo( new ThreefishEngine(512) ); 
		} else if (command.equals("setThreefish1024")) {
			CipherStuff.setCipherAlgo( new ThreefishEngine(1024) ); 
		} else if (command.equals("setTwofish")) {
			CipherStuff.setCipherAlgo( new TwofishEngine() ); 
		} else if (command.equals("setSerpent")) {
			CipherStuff.setCipherAlgo( new SerpentEngine() ); 
		} else if (command.equals("setAES")) {
			CipherStuff.setCipherAlgo( new AESEngine() ); 
		} else if (command.equals("setAESFast")) {
			CipherStuff.setCipherAlgo( new AESFastEngine() ); 

			
		// hash function:
		} else if (command.equals("setWhirlpool")) {
			HashStuff.setHashAlgo( new WhirlpoolDigest() ); 
		} else if (command.equals("setKeccak")) {
			HashStuff.setHashAlgo( new SHA3Digest() ); 
		} else if (command.equals("setSha512")) {
			HashStuff.setHashAlgo( new SHA512Digest() ); 
		} else if (command.equals("setSha384")) {
			HashStuff.setHashAlgo( new SHA384Digest() ); 
		} else if (command.equals("setSkein512")) {
			HashStuff.setHashAlgo( new SkeinDigest(512, 512) ); 
		} else if (command.equals("setBlake512")) {
			HashStuff.setHashAlgo( new Blake2bDigest() ); 
//		} else if (command.equals("setRipemd256")) {
//			HashStuff.setHashAlgo( new RIPEMD256Digest() ); 			
		} else if (command.equals("setRipemd320")) {
			HashStuff.setHashAlgo( new RIPEMD320Digest() ); 
			
			
		} else if (command.equals("setDE")) {
			PeaFactory.setI18n("de");
		} else if (command.equals("setEN")) {
			PeaFactory.setI18n("en");

		} else if (command.equals("notes")) {
			@SuppressWarnings("unused")
			InfoDialog info = new InfoDialog(languageBundle.getString(
					"notes_description"), 
					null, 
					"notes");
		} else if (command.equals("editor")) {
			@SuppressWarnings("unused")
			InfoDialog info = new InfoDialog(languageBundle.getString(
					"editor_description"), 
					null, 
					"editor");
		} else if (command.equals("image")) {
			@SuppressWarnings("unused")
			InfoDialog info = new InfoDialog(languageBundle.getString(
					"image_description"), 
					null, 
					"image");
		} else if (command.equals("keyboard_info")) {
			@SuppressWarnings("unused")
			InfoDialog info = new InfoDialog(
					"Onscreen Keyboard", 
					null, 
					"keyboard");
		} else if (command.equals("file")) {
			@SuppressWarnings("unused")
			InfoDialog info = new InfoDialog(languageBundle.getString(
					"file_description"), 
					null, 
					"file");
			
		} else if (command.equals("problemHelp")) {
			JOptionPane pane = new JOptionPane(
					languageBundle.getString("problem_help_dialog"), 
					JOptionPane.PLAIN_MESSAGE, 
					JOptionPane.OK_OPTION,
					null,null);//new ImageIcon(PswDialogView.getImage()), null);
			pane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			//pane.setIconImage(PswDialogView.getImage());
			pane.setVisible(true);
			//pane.showMessageDialog(null, languageBundle.getString("problem_help_dialog"), null, JOptionPane.PLAIN_MESSAGE);
		} else if (command.equals("howToUse")) {
			JOptionPane.showMessageDialog(PeaFactory.getFrame(), languageBundle.getString("how_to_use_dialog"), null, JOptionPane.PLAIN_MESSAGE);
		} else if (command.equals("aboutLicense")) {
			JOptionPane.showMessageDialog(PeaFactory.getFrame(), languageBundle.getString("about_license_dialog"), null, JOptionPane.PLAIN_MESSAGE);
		}		
	}	
	
	//
	// this will set the Level for all KDF schemes
	protected void setSecurityLevel(int securityLevel){
		if (securityLevel == 1) {
			//// ca. 40ms
			BcryptKDF.setRounds(6);
			
			CatenaKDF.setGarlicDragonfly(14);
			CatenaKDF.setLambdaDragonfly(2);
			CatenaKDF.setGarlicButterfly(12);
			CatenaKDF.setLambdaButterfly(4);
			
			PomeloKDF.setMemoryCost(10);
			PomeloKDF.setTimeCost(0);
			
			ScryptKDF.setcPUFactor(4096);
			ScryptKDF.setMemoryFactor(8);
			ScryptKDF.setParallelFactor(1);
		
	
		}
		if (securityLevel == 2) {
			//// ca. 120ms
			BcryptKDF.setRounds(10);
			
			CatenaKDF.setGarlicDragonfly(16);
			CatenaKDF.setLambdaDragonfly(2);
			CatenaKDF.setGarlicButterfly(13);
			CatenaKDF.setLambdaButterfly(4);
			
			PomeloKDF.setMemoryCost(14);
			PomeloKDF.setTimeCost(0);
			
			ScryptKDF.setcPUFactor(16384);
			ScryptKDF.setMemoryFactor(8);
			ScryptKDF.setParallelFactor(1);
		}
		if (securityLevel == 3) {
			//// ca. 450ms
			BcryptKDF.setRounds(12);			
			
			CatenaKDF.setGarlicDragonfly(18);
			CatenaKDF.setLambdaDragonfly(2);
			CatenaKDF.setGarlicButterfly(14);
			CatenaKDF.setLambdaButterfly(4);
			
			PomeloKDF.setMemoryCost(15);
			PomeloKDF.setTimeCost(0);
			
			ScryptKDF.setcPUFactor(16384);
			ScryptKDF.setMemoryFactor(32);
			ScryptKDF.setParallelFactor(1);			
		}
		if (securityLevel == 4) {
			//// ca. 1,4s (bei 13: 800ms)
			BcryptKDF.setRounds(14);
			
			CatenaKDF.setGarlicDragonfly(20);
			CatenaKDF.setLambdaDragonfly(2);
			CatenaKDF.setGarlicButterfly(15);
			CatenaKDF.setLambdaButterfly(4);
			
			PomeloKDF.setMemoryCost(18);
			PomeloKDF.setTimeCost(0);
			
			ScryptKDF.setcPUFactor(16384);
			ScryptKDF.setMemoryFactor(64);
			ScryptKDF.setParallelFactor(1);						
		}
		if (securityLevel == 5) {
			//// ca. 5,5s (bei 15: 2,7s, bei 17: 11s)
			BcryptKDF.setRounds(16);
			
			CatenaKDF.setGarlicDragonfly(22);
			CatenaKDF.setLambdaDragonfly(2);
			CatenaKDF.setGarlicButterfly(17);
			CatenaKDF.setLambdaButterfly(4);
			
			PomeloKDF.setMemoryCost(20);
			PomeloKDF.setTimeCost(1);
			
			ScryptKDF.setcPUFactor(16384);
			ScryptKDF.setMemoryFactor(256);
			ScryptKDF.setParallelFactor(1);			
		}
		// the instances of KeyDerivation must be updated with the new parameters
		updateKDF();
	}
	// Set a new instance of the current instance
	private void updateKDF(){
		if (KeyDerivation.getKdf() instanceof ScryptKDF){
			KeyDerivation.setKdf(new ScryptKDF() );
		} else if (KeyDerivation.getKdf() instanceof BcryptKDF){
			KeyDerivation.setKdf(new BcryptKDF() );
		} else if (KeyDerivation.getKdf() instanceof CatenaKDF){
			KeyDerivation.setKdf(new CatenaKDF() );
		} else if (KeyDerivation.getKdf() instanceof PomeloKDF){
			KeyDerivation.setKdf(new PomeloKDF() );
		}
	}
}
