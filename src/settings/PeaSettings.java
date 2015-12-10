package settings;

import javax.swing.JDialog;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.Digest;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.KeyDerivation;
import cologne.eck.peafactory.crypto.kdf.CatenaKDF;

/*
 * This class is only used by Peafactory internally
 * and is not modified by FileModifier
 * 
 */
public class PeaSettings {

	private static JDialog keyboard = null;
	private static final JDialog pswGenerator = null;
	
	private static final boolean BOUND = true;
	private static final String EXTERNAL_FILE_PATH = null;
	private static final boolean EXTERN_FILE = true;
	private static final String JAR_FILE_NAME = "default";
	private static final String LABEL_TEXT = null;
	
	private static final byte[] PROGRAM_RANDOM_BYTES = {-128, 125, -121, 32, -104, 88, -90, -116, 122, -64, 58, 67, -4, -51, -93, -89, 70, -22, -61, -109, 114, 75, -49, -114, -42, -128, -71, 5, -48, -92, 0, -71};
	private static final byte[] FILE_IDENTIFIER = {108, -39, -123, 86, 41, -111, -124, 107};
	
	private static final BlockCipher CIPHER_ALGO = PeaFactory.getDefaultCipher();//new TwofishEngine();
	private static final Digest HASH_ALGO = PeaFactory.getDefaultHash();//new SkeinDigest(512, 512);
	private static final KeyDerivation KDF_SCHEME = PeaFactory.getDefaultKDF();//new CatenaKDF();
	
	private static final int ITERATIONS = CatenaKDF.getDragonflyLambda();//2
	private static final int MEMORY = CatenaKDF.getGarlicDragonfly();//18;
	private static final int PARALLELIZATION = 0;
	
	private static final String VERSION_STRING = "";
	
	
	public final static JDialog getKeyboard() { 
		return keyboard; 
	}
	public final static JDialog getPswGenerator() { 
		return pswGenerator; 
	}	
	public final static Digest getHashAlgo() { 
		return HASH_ALGO; 
	}
	public final static BlockCipher getCipherAlgo() { 
		return CIPHER_ALGO; 
	}
	public final static KeyDerivation getKdfScheme() { 
		return KDF_SCHEME; 
	}
	public final static int getIterations() { 
		return ITERATIONS; 
	}
	public final static int getMemory() { 
		return MEMORY; 
	}
	public final static int getParallelization() { 
		return PARALLELIZATION; 
	}
	public final static String getVersionString() { 
		return VERSION_STRING; 
	}
	public final static byte[] getProgramRandomBytes() { 
		return PROGRAM_RANDOM_BYTES; 
	}
	public final static byte[] getFileIdentifier() { 
		return FILE_IDENTIFIER; 
	}
	public final static String getJarFileName() { 
		return JAR_FILE_NAME; 
	}
	public final static String getLabelText() { 
		return LABEL_TEXT; 
	}
	public final static boolean getExternFile() { 
		return EXTERN_FILE; 
	}
	public final static boolean getBound() { 
		return BOUND; 
	}
	public final static String getExternalFilePath() { 
		return EXTERNAL_FILE_PATH; 
	}
}
