package start;

/* Automatically produced file */
/* This class is newly created for each PEA */

import java.io.*;
import javax.swing.JOptionPane;

public class Start {

private static final int MEMORY = 16;
private static final String CLASS_NAME = "image_pea/PswDialogImage";
private static final String JAR_FILE_NAME = "test-image";
private final static int FIXED_MEMORY = 160;
private final static int NEEDED_MEMORY = MEMORY + FIXED_MEMORY;
private static double requiredJavaVersion = 1.6;
private static Runtime run = Runtime.getRuntime();
private static String mode = "";

public static void main(String[] args) {

// check mode:
if (args.length > 0) {
  if (args[0] != null) {
    if(args[0].equals("-r")){// -r is rescue mode
      mode = args[0];// pass parameter to command later
      System.out.println("#####---RESCUE MODE---#####");}}}

String command = "java ";// create the program call 
// check memory:
if ( ( NEEDED_MEMORY * 1024 *1024) > run.maxMemory() ) {
  command = command +  "-Xmx" + NEEDED_MEMORY + "m ";}
else { command = command + "-Xms" + NEEDED_MEMORY + "m "; }
// check java version:
try {
  String javaVersionString = System.getProperty("java.version");
  double javaVersion = Double.parseDouble( javaVersionString.substring(0,3) );
  if (javaVersion < requiredJavaVersion) {
    JOptionPane.showMessageDialog(null,
    "Wrong java version. Required java version: " + requiredJavaVersion + ", available: "+ System.getProperty("java.version"),
    null, JOptionPane.ERROR_MESSAGE);
    System.exit(1); }
} catch (Exception e) {
  System.err.println("Check java version failed: " + System.getProperty("java.version"));
  System.exit(1);}

// check if there is an instance of this program already running:
boolean warn = false;
// check for windows and unix:
String OS = System.getProperty("os.name").toLowerCase();

if (OS.contains("windows")){
  String line;
  boolean firstOccurence = false;
  try {
    Process procCheckWin = Runtime.getRuntime().exec("wmic PROCESS where \"name like '%java%'\" get Processid,Caption,Commandline");
    BufferedReader inputCheckWin = new BufferedReader(new InputStreamReader(procCheckWin.getInputStream()));
    while ((line = inputCheckWin.readLine()) != null) {
      if(line.contains(JAR_FILE_NAME + ".jar")){
        if(firstOccurence == true){// process is running already
          warn = true;
          System.out.println("instance already running");
          break;
        } else {
          firstOccurence = true;// this is only the instance of this start program
        }
      }
    }
    inputCheckWin.close();
  } catch (IOException ioe) {
    ioe.printStackTrace();
}

} else { // should work for most Unix: Linux, BSD, Solaris, Mac OS except OS X 
  try {// Redirecting out and err
    Process checkPro = run.exec("pgrep -f "+ JAR_FILE_NAME + ".jar");
    checkPro.waitFor();
    BufferedReader buffCheck = new BufferedReader(new InputStreamReader(checkPro.getInputStream()));
    buffCheck.readLine(); // this is always existent: the ID of this process
    int checkProID = Integer.parseInt(buffCheck.readLine()); // next line: there should be no ID
    //System.out.println(checkProID);
    if( checkProID > 100){
      warn = true;
      System.out.println("instance already running");
    }
  } catch (NumberFormatException e) { 
    //System.out.println("no instance running");
  } catch (Exception e) { 
    //... this will be thrown for rarely used systems with no pgrep
}}

if(warn == true){ 
  Object[] options = {"start nevertheless", "cancel"};
  int n = JOptionPane.showOptionDialog(null,
  "It seems that there is already a running instance of this program." +
  "\nYou should close those instance before you start a new one.",
  "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, 
  options, options[1]);
  if(n != 0) { System.exit(0);}}

// create the command:
command =  command + "-cp " + JAR_FILE_NAME 
+ ".jar cologne.eck.peafactory.peas." + CLASS_NAME + " " + mode;

try {
// run the program
  Process p = run.exec(command);
// Redirect out and err:
  String line;
  BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
  BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
  while ((line = bri.readLine()) != null) { System.out.println(line); }
  bri.close();
  while ((line = bre.readLine()) != null) { System.out.println(line); }
  bre.close();
  p.waitFor();
} catch (IOException ie) {
  JOptionPane.showMessageDialog(null,
  "Execution failed."
  + ie.toString(),
  null,JOptionPane.ERROR_MESSAGE);
  ie.printStackTrace();
} catch (InterruptedException ire) { // for terminal only
  ire.printStackTrace();}
  System.exit(0);}}
