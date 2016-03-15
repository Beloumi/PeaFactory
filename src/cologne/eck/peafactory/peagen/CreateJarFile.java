package cologne.eck.peafactory.peagen;

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
 * Managing of entries of the jar file (Password Encrypted Archive).
 * The pea should contain only the required classes. 
 */

import java.io.*;
import java.util.jar.*;
import java.util.regex.Matcher;

import org.bouncycastle.crypto.digests.Blake2bDigest;
//import org.bouncycastle.crypto.digests.RIPEMD256Digest;
import org.bouncycastle.crypto.digests.RIPEMD320Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.digests.SkeinDigest;
import org.bouncycastle.crypto.digests.WhirlpoolDigest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.SerpentEngine;
import org.bouncycastle.crypto.engines.Shacal2Engine;
import org.bouncycastle.crypto.engines.ThreefishEngine;
import org.bouncycastle.crypto.engines.TwofishEngine;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.*;
import cologne.eck.peafactory.crypto.kdf.*;
import cologne.eck.peafactory.gui.MainView;
import cologne.eck.peafactory.peas.editor_pea.EditorType;
import cologne.eck.peafactory.peas.file_pea.FileType;
import cologne.eck.peafactory.peas.image_pea.ImageType;
import cologne.eck.peafactory.peas.note_pea.NotesType;
import cologne.eck.peafactory.tools.WriteResources;




public class CreateJarFile {
	//All JarEntry's names should NOT begin with "/".
	//Directory names must end with a slash '/'
	//All paths must use '/' style slashes, not '\'

	
	public static void createJarFile() { 
		
		DataType dataType = DataType.getCurrentType();

		String currentJarFileName = JarStuff.getJarFileName();
		if (MainView.isBlankPea() == true ||
				(MainView.getOpenedFileName() == null 
				&& !(dataType instanceof FileType) 
				&& !(dataType instanceof ImageType && MainView.isBlankPea() == false))) {
			String dirName = JarStuff.getJarFileName().substring(0, JarStuff.getJarFileName().length() - 4); // extract ".jar"
			currentJarFileName = dirName + File.separator + currentJarFileName;
		}
		currentJarFileName = "peas" + File.separator + currentJarFileName;

		// replace with File.seperator to do some normal file operations
		if (currentJarFileName.contains("/") || currentJarFileName.contains("\\")) {
			currentJarFileName = currentJarFileName.replaceAll("/", File.separator);
			if (currentJarFileName.contains(Matcher.quoteReplacement("\\")) ){
				currentJarFileName = currentJarFileName.replaceAll( Matcher.quoteReplacement("\\"), File.separator  );
			}
			// if last directory not exist: create with all parent directories
			int index1 =  currentJarFileName.lastIndexOf( currentJarFileName.lastIndexOf(File.separator) -1 ) + 1;// if only one exists: return -1
			String lastDir = currentJarFileName.substring( index1 , currentJarFileName.lastIndexOf(File.separator));
			if ( ! new File(lastDir).exists()) {
				if (! new File(lastDir).mkdirs() ) {
					System.err.println("CreateJarFile.java: Can not create directories: " + lastDir);
					System.exit(3);
				}
			}
		}
		// operation on jar-file:		
		File jarFile = new File(currentJarFileName);
		// warn if file already exists
		if ( jarFile.exists() ) {
			System.out.println("Warning: existing jar-archive will be overwritten");
		}
		
		// Name of pea package
		String pathToPeaClasses = "bin" + File.separator + PeaFactory.getPackagePath() + File.separator;
		String pathToBcClasses = "bin" + File.separator + "org" + File.separator + "bouncycastle" + File.separator;

		// Manifest:
		Manifest manifest = new Manifest();
		Attributes att = manifest.getMainAttributes();		
		att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		att.put(Attributes.Name.CLASS_PATH, ".");
		att.put(Attributes.Name.MAIN_CLASS, "start.Start");


		//-----------------
		// add the classes
		//
		File[] tmpClassFiles = new File[128]; // must be enough, we yet do not know the number
		int index = 0;

		//==============================================
		// FIXED classes for all peas:
		//==============================================
		
		//===========================
		// peafactory classes:
		tmpClassFiles[index++] = new File("start/Start.class");
		tmpClassFiles[index++] = new File("settings/PeaSettings.class");
		
		tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/PswDialogBase.class");	
		
		tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/gui/PswDialogView.class"); 
		tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/gui/PswDialogView$WhileTypingThread.class"); // inner class
		tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/gui/CharTable.class");	
		tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/gui/KeyButton.class");	
		
		tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/RandomStuff.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/gui/LockFrame.class"); 
		tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/gui/NewPasswordDialog.class"); 
		
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/ReadResources.class");	
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/WriteResources.class");	
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/Attachments.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/Converter.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/Zeroizer.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/Comparator.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/MouseRandomCollector.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/KeyRandomCollector.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/EntropyPool.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/EntropyPool$EntropyThread.class"); // inner class
		
		// only for testing:
		tmpClassFiles[index++] = new File(pathToPeaClasses + "tools/Help.class");
		
		tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/AuthenticatedEncryption.class");	
		tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/HashStuff.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/KeyDerivation.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/CipherStuff.class");
		tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/SessionKeyCrypt.class");
		
		//===========================
		// bouncycastle:
		tmpClassFiles[index++] = new File(pathToBcClasses + "util/Arrays.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "util/Memoable.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "util/Pack.class");
		
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/InvalidCipherTextException.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/DataLengthException.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/OutputLengthException.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/RuntimeCryptoException.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/CryptoException.class");

		
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/BlockCipher.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/BufferedBlockCipher.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/CipherParameters.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/Digest.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/ExtendedDigest.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/Mac.class");
		
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/SkippingStreamCipher.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/StreamCipher.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/StreamBlockCipher.class");	
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/SkippingCipher.class");
		
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/macs/CMac.class");
		
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/paddings/BlockCipherPadding.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/paddings/PaddedBufferedBlockCipher.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/paddings/PKCS7Padding.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/paddings/ISO7816d4Padding.class");
		
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/modes/CBCBlockCipher.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/modes/EAXBlockCipher.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/modes/AEADBlockCipher.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/modes/SICBlockCipher.class");

		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/params/KeyParameter.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/params/ParametersWithIV.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/params/ParametersWithRandom.class");
		tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/params/AEADParameters.class");

		//=======================================
		// Variable classes: 
		//=======================================
		
		//===========================
		// Cryptography setting specific classes:
		//if (KDFScheme.getKDFScheme() instanceof SCryptConnector) {
		if (KeyDerivation.getKdf() instanceof ScryptKDF) {
			
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/ScryptKDF.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/generators/SCrypt.class");
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/PBEParametersGenerator.class");
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/generators/PKCS5S2ParametersGenerator.class");
					tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/macs/HMac.class");
						tmpClassFiles[index++] = new File(pathToBcClasses + "util/Integers.class");
					tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SHA1Digest.class");		
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SHA256Digest.class");
					tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/GeneralDigest.class");
					tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/EncodableDigest.class");
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/Salsa20Engine.class");
					tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/MaxBytesExceededException.class");			
					tmpClassFiles[index++] = new File(pathToBcClasses + "util/Strings.class");		
			
		} else if (KeyDerivation.getKdf() instanceof BcryptKDF) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/generators/BcryptCore.class");
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/BcryptKDF.class");
		} else if (KeyDerivation.getKdf() instanceof PomeloKDF) {
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/PomeloKDF.class");
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/PasswordHashingScheme.class");
		} else if (KeyDerivation.getKdf() instanceof CatenaKDF) {
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/CatenaKDF.class");
			// CatenaKDF selects between all instances, so to avoid NoClassDefFoundError
			// all classes are included
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/Catena.class");
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/CatenaBRG.class");
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/CatenaDBG.class");
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/PasswordHashingScheme.class");
			if (! (HashStuff.getHashAlgo() instanceof Blake2bDigest)) {
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/Blake2bDigest.class");
			}
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/Catena_Blake2b_1.class");				
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/kdf/ReducedDigest.class");

		} else {
			System.err.println("CreateJarFile: Invalid kdf.");
			System.exit(3);
		}
		
		if (CipherStuff.getCipherAlgo() instanceof Shacal2Engine) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/Shacal2Engine.class");
		} else if (CipherStuff.getCipherAlgo() instanceof TwofishEngine) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/TwofishEngine.class");
		} else if (CipherStuff.getCipherAlgo() instanceof AESEngine) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/AESEngine.class");
		} else if (CipherStuff.getCipherAlgo() instanceof AESFastEngine) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/AESFastEngine.class");
		} else if (CipherStuff.getCipherAlgo() instanceof ThreefishEngine) {
			
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/ThreefishEngine.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/params/TweakableBlockCipherParameters.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/ThreefishEngine$ThreefishCipher.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/ThreefishEngine$Threefish1024Cipher.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/ThreefishEngine$Threefish512Cipher.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/ThreefishEngine$Threefish256Cipher.class");
		} else if (CipherStuff.getCipherAlgo() instanceof SerpentEngine) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/SerpentEngine.class");
		} else {
			System.err.println("CreateJarFile: Invalid cipher algorithm.");
			System.exit(3);			
		}
		
		if (CipherStuff.getCipherMode() instanceof EAXMode) {
			tmpClassFiles[index++] = new File(pathToPeaClasses + "crypto/EAXMode.class");	
		}
		
		if (HashStuff.getHashAlgo() instanceof WhirlpoolDigest) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/WhirlpoolDigest.class");
		} else if (HashStuff.getHashAlgo() instanceof SkeinDigest) {
			if ( ! (CipherStuff.getCipherAlgo() instanceof ThreefishEngine) ) {
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/ThreefishEngine.class");
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/ThreefishEngine$ThreefishCipher.class");
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/ThreefishEngine$Threefish256Cipher.class");
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/ThreefishEngine$Threefish512Cipher.class");
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/engines/ThreefishEngine$Threefish1024Cipher.class");
			}
			if ( ! (KeyDerivation.getKdf() instanceof ScryptKDF)) {
				tmpClassFiles[index++] = new File(pathToBcClasses + "util/Integers.class");
			}
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SkeinDigest.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SkeinEngine.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SkeinEngine$UBI.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SkeinEngine$UbiTweak.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SkeinEngine$Configuration.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SkeinEngine$Parameter.class");
			
		} else if (HashStuff.getHashAlgo() instanceof SHA512Digest) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SHA512Digest.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/LongDigest.class");
			if (! (KeyDerivation.getKdf() instanceof ScryptKDF) ) {
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/EncodableDigest.class");
			}
		} else if (HashStuff.getHashAlgo() instanceof SHA384Digest) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SHA384Digest.class");
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/LongDigest.class");
			if (! (KeyDerivation.getKdf() instanceof ScryptKDF) ) {
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/EncodableDigest.class");
			}
		} else if (HashStuff.getHashAlgo() instanceof SHA3Digest) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/SHA3Digest.class");	
		} else if (HashStuff.getHashAlgo() instanceof Blake2bDigest) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/Blake2bDigest.class");			
/*		} else if (HashStuff.getHashAlgo() instanceof RIPEMD256Digest) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/RIPEMD256Digest.class");
			if (! (KeyDerivation.getKdf() instanceof ScryptKDF) ) {
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/GeneralDigest.class");
			} */
		} else if (HashStuff.getHashAlgo() instanceof RIPEMD320Digest) {
			tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/RIPEMD320Digest.class");
			if (! (KeyDerivation.getKdf() instanceof ScryptKDF) ) {
				tmpClassFiles[index++] = new File(pathToBcClasses + "crypto/digests/GeneralDigest.class");
			}
		} else {
			System.err.println("CreateJarFile: Invalid hash algorithm.");
			System.exit(3);						
		}
		
		if (FileModifier.isSetKeyboard() == true) {
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/gui/Keyboard.class");
			
		}
		
		//===========================
		// pea type specific classes: 
		
		if (dataType instanceof FileType) {
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/file_pea/PswDialogFile.class"); 
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/file_pea/LockFrameFile.class");
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/file_pea/FileTypePanel.class");
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/file_pea/FileTypePanel$ProgressTask.class"); // inner class
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/file_pea/FileComposer.class");
		} else if (dataType instanceof ImageType) {
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/image_pea/PswDialogImage.class"); 
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/image_pea/LockFrameImage.class");					
		} else if (dataType instanceof NotesType) {
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/note_pea/" + "PswDialogNotes.class"); 
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/note_pea/" + "LockFrameNotes.class");
			if (FileModifier.isPswGenerator() == true) {
				tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/gui/PasswordGeneratorDialog.class"); 
			}
		} else if (dataType instanceof EditorType) {
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/editor_pea/PswDialogEditor.class"); 
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/editor_pea/LockFrameEditorMenu.class"); 
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/editor_pea/LockFrameEditorMenu$TxtFilter.class"); // inner class
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/editor_pea/LockFrameEditorMenu$RtfFilter.class"); // inner class
			//tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/editor_pea/LockFrameEditorMenu$LockFilter.class"); // inner class
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/editor_pea/LockFrameEditor.class");
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/editor_pea/LockFrameEditor$CopyCutPastePopup.class"); // inner class
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/editor_pea/LockFrameEditor$UndoAction.class"); // inner class
			tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/editor_pea/LockFrameEditor$RedoAction.class"); // inner class
			if (FileModifier.isPswGenerator() == true) {
				tmpClassFiles[index++] = new File(pathToPeaClasses + "peas/gui/PasswordGeneratorDialog.class"); 
			}	
		} else {
			System.err.println("CreateJarFile: Invalid file type.");
			System.exit(3);
		}

		// now, we know the real number of files:
		File[] classFiles = new File[index];
		System.arraycopy(tmpClassFiles,  0,  classFiles,  0,  classFiles.length);
		

		//
		// stream to write jar file:
		//
		JarOutputStream jos = null;
		try {
			// create jar file with manifest			
			jos = new JarOutputStream(new FileOutputStream(jarFile), manifest);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//
		// Add class files to jar file
		//
		for ( File source : classFiles ) {		
			BufferedInputStream in = null;
			try  {
				// cut "bin/" and replace File.sep with "/"
				JarEntry entry = null;
				if(source.getPath().startsWith("settings")
						|| source.getPath().startsWith("start")){
					entry = new JarEntry( (source.getPath().replace(File.separator, "/") ));	
				} else {
					entry = new JarEntry( (source.getPath().substring(4, source.getPath().length())).replace(File.separator, "/") );	
				}
				entry.setTime(source.lastModified());
				jos.putNextEntry(entry);
				in = new BufferedInputStream(new FileInputStream(source));
			    byte[] buffer = new byte[1024];
			    while (true)  {
			    	int count = in.read(buffer);
			    	if (count == -1)  break;
			    	jos.write(buffer, 0, count);
			    }
			    jos.closeEntry();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} // End for classFiles
		

		//
		// Add resource files to jar file
		//
		// from directory "resources"
		//
		File resourcesDir = new File("resources");

		if (! resourcesDir.exists() ) System.err.println("Error: resources does not exist: " + resourcesDir.getAbsolutePath() );
		if (! resourcesDir.isDirectory() ) System.err.println("Error: resources is not directory");
		if ( resourcesDir.listFiles().length == 0 ) System.err.println("Error: resources is empty");
		
		for ( File source : resourcesDir.listFiles() ) {		
			
			if (source.getName().equals("pea-lock.png") // image icon
					|| source.getName().startsWith("PeaLanguagesBundle") // language support
					// only for internal (copied) images:
					|| ( (dataType instanceof ImageType) 
							&& (source.getName().equals("text.lock"))	// encrypted image
							&& (MainView.getOpenedFileName() == null) ) 
					) {				
				BufferedInputStream in = null;
				try  {
					JarEntry entry = new JarEntry(source.getPath().replace(File.separator, "/"));

					entry.setTime(source.lastModified());
					jos.putNextEntry(entry);
					in = new BufferedInputStream(new FileInputStream(source));
				    byte[] buffer = new byte[1024];
				    while (true)  {
				    	int count = in.read(buffer);
				    	if (count == -1)  break;
				    	jos.write(buffer, 0, count);
				    }
				    jos.closeEntry();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} else {
				
			}
		} // End for resources
		
		if (jos != null) {
			try {
				jos.flush();
				jos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		// set jar file executable if possible: 
		// (does not overwrite settings from partition)
		if ( ! jarFile.setExecutable(true,true) ) { // only executable for owner: second true
			try{
				Runtime.getRuntime().exec("chmod u+x " + JarStuff.getJarFileName());
			} catch (Exception e) {			
				System.out.println("Can not set jar-archive executable.");
			}
		}
			
		if (MainView.getUnixScript() == true) {
			
			// jar file without ".jar"
			String jarName = JarStuff.getJarFileName().substring(0, JarStuff.getJarFileName().length() - 4);

			try {
				// create script content:
				String scriptContent = "#!/bin/sh \n"
						+ "java -jar " + JarStuff.getJarFileName();
				
				// get file name of script
				String fileName = null;
				if (MainView.getOpenedFileName() == null) { // jar file in folder
					fileName = "peas" + File.separator + jarName + File.separator 
						+ "start_" + jarName + ".sh";
				} else {
					fileName = "peas" + File.separator 
						+ "start_" + jarName + ".sh";				
				}
				
				// Write script file:
				WriteResources.writeText(scriptContent, fileName);
				
				// make script file executable:
				Runtime.getRuntime().exec("chmod u+x " + fileName);
				
			} catch (Exception e) {
				System.out.println("Error: can not create unix script");
			}			
			
		} else {
			//System.out.println("jar-archive is executable for owner.");
		}
	}
}
