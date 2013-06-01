package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

import android.net.wifi.ScanResult;
import android.os.Environment;

//import javax.swing.JOptionPane;

/**
 * @author arno
 *
 *	This Logger logs messages from the Application and organizes the data logging
 */
public class Logger {

	/**
	 * Saving the Singleton Instance
	 */
	private static Logger instance = null; 
	
	private static boolean timeOutput = false;
	
	//"Nice" Newlines
	private static String newline = System.getProperty("line.separator");
	
	public static void setTimeOutput(boolean timeOutput) {
		Logger.timeOutput = timeOutput;
	}

	
	/**
	 * Singleton-Getter - Creates Logger if nec.
	 */
	public static Logger getInstance() {
		if (instance == null) 
			instance = new Logger();
		return instance;
	}
	

	//////////////////////////////////////////
	// Logging part
	//////////////////////////////////////////
	
	/**
	 * Filename for Logging-Logfile
	 */
	private String logFileName = "";
	
	/**
	 * The path for logfiles
	 */
	private String logFilePath = "logs";
	

	private File flogfile = null;
	private FileWriter logWriter = null;
	
	/////////////////////////////////////////
	// Data-Logging-Part
	/////////////////////////////////////////
	
	/**
	 * Filename for actual data capture 
	 */
	private String dataFileName = "";
	
	/**
	 * Path for data captures
	 */
	private String dataFilePath = "WiFidata";
	

	private File fdatafile = null;
	private FileWriter dataWriter = null;
	
	//Actual scanUID - initially set to -1 to always create Files at first Data Capture
	private int scanUID = -1;
	
	/**
	 * Der private Konstruktor, um den Logger als Singleton zu realisieren
	 * Dadurhch werden unter anderem konkurierende Logger verhindert
	 */
	private Logger() {
		 //Der Konstruktor ist privat
	}
	
	
	
	/**
	 * Getter
	 */
	public final String getlogFileName() {
		return logFileName;
	}
	
	/**
	 * Getter
	 */
	public String getlogFilePath() {
		return logFilePath;
	}
	
	/**
	 * Getter ffor Full Path
	 */
	public String getFulllogFileName() {
		return logFilePath+File.separatorChar+logFileName;
	}
	
	/**
	 * Assigning File for Logs
	 * @return true for success
	 */
	private boolean assignLogFile() {
		//Der Dateiname wird aufgebaut: logxxxxxxx.log - wobei xxxxxx der Zeitstempel der erstellung des Files ist
		//Dies kann spaeter evtl. sinnvoller gemacht werden
		//Annahme: System kann mehr als 8+3 im Dateinamen!
		
		Date date = new Date();
		SimpleDateFormat format;
		
		format = new SimpleDateFormat("yyMMdd_HHmmss_S",Locale.getDefault());
		
		String suffix = format.format(date);
		
		logFileName = "log"+suffix+".log";
		
		//Create ile
		try {
			File fLogDir = new File(Environment.getExternalStorageDirectory(),File.separatorChar+main.Config.myDirectory+File.separatorChar+logFilePath+File.separatorChar); 
			fLogDir.mkdirs();
			flogfile = new File(fLogDir,logFileName);
			//Override of old Files if nec.
			logWriter = new FileWriter(flogfile);
			logWriter.write("Log-File of TimeStamp: "+suffix);
			logWriter.write(newline);
			logWriter.write(newline);
			logWriter.flush();
		} catch (Exception e) {
			System.err.println("Failed to assign File: "+e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Assigning File for ScanData.
	 * Each bunch of scan-samples has a scan (pseudo) UID - this UID increases with every consecutive bunch of scans
	 * Restarting the app will reset the UID but the timestamp makes it unique again
	 * TODO: Avoid jumping between timezones - collisions may happen ("Easy solution": fix Local time - not nice...)
	 * @return true for success
	 */
	private boolean assignDataFile(int scanUID) {
		//Der Dateiname wird aufgebaut: logxxxxxxx.log - wobei xxxxxx der Zeitstempel der erstellung des Files ist
		//Dies kann spaeter evtl. sinnvoller gemacht werden
		//Annahme: System kann mehr als 8+3 im Dateinamen!
		
		this.scanUID = scanUID;
		
		Date date = new Date();
		SimpleDateFormat format;
		
		format = new SimpleDateFormat("yyMMdd_HHmmss_S",Locale.getDefault());
		
		String suffix = format.format(date)+"_"+scanUID;
		
		dataFileName = "data_"+suffix+".log";
		
		//Create File
		try {
			File fDataDir = new File(Environment.getExternalStorageDirectory(),File.separatorChar+main.Config.myDirectory+File.separatorChar+dataFilePath+File.separatorChar);
			fDataDir.mkdirs();
			fdatafile = new File(fDataDir,dataFileName);
			//Collision-Handling made easy: Just override
			dataWriter = new FileWriter(fdatafile);
			//logWriter.write("Log-File von TimeStamp: "+suffix);
			//logWriter.write(newline);
			//logWriter.write(newline);
			dataWriter.flush();
			log("AssignDataFile", "Assigned File for scanUID "+scanUID);
		} catch (Exception e) {
			System.err.println("Failed to assign File: "+e.getMessage());
			return false;
		}
		return true;
	}
	
	private HashMap<String,levelRSSI> levels = new HashMap<String,levelRSSI>();
	
	/**
	 * This Method is Logging the Data captures. For now, assume Full lines in correct format as input
	 * TODO: optimizing format
	 * @param scanUID the scanUID of the actual Capture.
	 * @param o Data to be logged
	 */
	public void dataLog(int scanUID, ScanResult o) {
		//Log sth.
		if (this.scanUID != scanUID) {
			assignDataFile(scanUID);
			
		}	
		//Now there is a File
		try {
			//dataWriter.write(message+newline);
			levelRSSI lRSSI = (levelRSSI) levels.get(o.BSSID);
			if (lRSSI == null) {
				lRSSI = new levelRSSI();
				levels.put(o.BSSID, lRSSI);
			}
			lRSSI.insert(o.level);
			
			dataWriter.write(o.toString()+newline);
			dataWriter.flush();
		} catch (Exception e) {
			//Never tested this - might not always work with this Stream...
			System.err.print("Unable to open/assign File - No working possible. Fail desc.: "+e.getMessage());
		}
	}
	
	class levelRSSI {
		
		private LinkedList<Integer> values = new LinkedList<Integer>();
		private int samples = 0;
		
		public void insert(int v) {
			values.add(v); samples++;
		}
		
		public int mean() {
			int sum = 0;
			
			while (! values.isEmpty())
				sum += values.poll();
			
			return Math.round(sum/samples);
		}
		
		
	}
	
	private void dataCalcStats() {
		// Calculating statistical information about the samples
		//Is there an existing Writer?
		if (dataWriter == null) return;
		//Calc the mean level of RSSI
		try {
			dataWriter.write("===== RSSI Means ====="+newline);
			Set<String> keys = levels.keySet();
			
			for (String key : keys) {
				dataWriter.write("BSSID: "+key+" level: "+((levelRSSI) levels.get(key)).mean()+newline);
				dataWriter.flush();
			}
			
			
		} catch (IOException e) {
			//Do nothing
		}
		
		
		
	}


	/**
	 * Logging Objects
	 * @param sender Sender that wants to log sth. (class, package, method, little gnome...)
	 * @param o The Object (to-String-impl.)
	 */
	public void log(String sender, Object o) {
		this.log(sender, o.toString());
	}
	
	/**
	 * This Method is Logging the messages. It automatically assigns a new File if nec.
	 * @param sender Sender that wants to log sth. (class, package, method, little gnome...)
	 * @param message The message to be logged
	 */
	public void log(String sender, String message) {
		//Timestamp?
		if (timeOutput) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
			sender = sender+"@ "+sdf.format(new Date());
		}
		//Log sth.
		if (logWriter == null) {
			assignLogFile();
		}	
		//Now there is a File
		try {
			logWriter.write("["+sender+"]: "+message+newline);
			logWriter.flush();
		} catch (Exception e) {
			//Never tested this - might not always work with this Stream...
			System.err.print("Unable to open/assign File - No working possible. Fail desc.: "+e.getMessage());
		}
	}
	
	/**
	 * On destroying this object all files should be closed
	 */
	protected void finalize() throws Throwable {
		try {
			if (logWriter != null)
				logWriter.close();
		} catch (Exception e) {
			System.err.println("Finalizer has failed - "+e.getLocalizedMessage());
		} finally {
			super.finalize();
		}
	}


	public void dataFinish() {
		//Do some statiscal things
		dataCalcStats();
		levels.clear();		
	}

}
