package settings;

/* Automatically produced file */
/* This class is newly created for each PEA */

import javax.swing.JDialog;
import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.digests.*;
import org.bouncycastle.crypto.engines.*;
import cologne.eck.peafactory.crypto.KeyDerivation;
import cologne.eck.peafactory.crypto.kdf.*;

public class PeaSettings {

private static JDialog keyboard = null;
private static final JDialog pswGenerator = null;
private static final boolean BOUND = true;
private static final String EXTERNAL_FILE_PATH = null;
private static final boolean EXTERN_FILE = false;
private static final String JAR_FILE_NAME = "test-image";
private static final String LABEL_TEXT = null;
private static final byte[] PROGRAM_RANDOM_BYTES = {68, -46, -56, -50, -100, 49, -61, -70, -88, -119, 44, -79, -126, 74, 18, -66, -87, 114, -37, 10, -94, 108, -63, 16, -28, -10, -6, -11, -3, -37, 23, 57};
private static final byte[] FILE_IDENTIFIER = {125, -33, 2, 62, 116, 66, -102, -103};
private static final BlockCipher CIPHER_ALGO = new TwofishEngine();
private static final Digest HASH_ALGO = new SkeinDigest(512, 512);
private static final KeyDerivation KDF_SCHEME = new CatenaKDF();
private static final int ITERATIONS = 2;
private static final int MEMORY = 18;
private static final int PARALLELIZATION = 0;
private static final String VERSION_STRING = "Dragonfly-Full";
public final static JDialog getKeyboard() { return keyboard; }
public final static JDialog getPswGenerator() { return pswGenerator; }
public final static Digest getHashAlgo() { return HASH_ALGO; }
public final static int getIterations() { return ITERATIONS; }
public final static int getMemory() { return MEMORY; }
public final static int getParallelization() { return PARALLELIZATION; }
public final static String getVersionString() { return VERSION_STRING; }
public final static byte[] getProgramRandomBytes() { return PROGRAM_RANDOM_BYTES; }
public final static byte[] getFileIdentifier() { return FILE_IDENTIFIER; }
public final static String getJarFileName() { return JAR_FILE_NAME; }
public final static String getLabelText() { return LABEL_TEXT; }
public final static boolean getExternFile() { return EXTERN_FILE; }
public final static boolean getBound() { return BOUND; }
public final static String getExternalFilePath() { return EXTERNAL_FILE_PATH; }
public final static BlockCipher getCipherAlgo() { return CIPHER_ALGO; }
public final static KeyDerivation getKdfScheme() { return KDF_SCHEME; }
}
