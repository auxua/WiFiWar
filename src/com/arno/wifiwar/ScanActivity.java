package com.arno.wifiwar;

import java.util.List;

import main.Logger;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ScanActivity extends Activity implements OnClickListener {

	private int scanNums = 0;
	private TextView statusText;
	private ProgressBar statusBar; 
	private List<ScanResult> scanList;
	private WifiManager myWiFi;
	private int scanUID = 0;
	//private boolean working = false;
	private WiFiReceiver wiFiRec;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_scan);
        
        
        Intent myIntent = getIntent();
        scanNums = myIntent.getIntExtra("NUM_SCANS", main.Config.numberOfSamples);
        //Random should avoid collission as good as possible... (For when Intent is damaged)
        scanUID = myIntent.getIntExtra("SCAN_UID", 1);
        //Positive Scannumber needed
        if (scanNums < 1) scanNums = main.Config.numberOfSamples;
        statusText = (TextView) findViewById(R.id.textStatus);
        statusBar = (ProgressBar) findViewById(R.id.proStatus);
        
        
        System.err.println("Created? (scan)");
        
        myWiFi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //Go-Back-Button
        Button button = (Button) findViewById(R.id.but_back);
		button.setOnClickListener(this);
        
		// Get WiFi status
		WifiInfo info = myWiFi.getConnectionInfo();
		Logger.getInstance().log("Testscan-Info","\n\nWiFi Status: " + info.toString());
		
		wiFiRec = new WiFiReceiver();
        
    }
	
	@Override
	protected void onResume() {		
		super.onResume();
		if (statusText == null)
			System.err.println("Resuming");
		else 
			System.err.println("Resuming without null");
		//this.statusText.setText("Test");
		
		//UI
		statusBar.setMax(scanNums);
		
		//Perorming Scans
		//for (int i=0; i<scanNums; i++) {
			//scan();
			//WiFiReceiver wiFiRec = new WiFiReceiver();
			registerReceiver(wiFiRec, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			myWiFi.startScan();
			//Wait a bit for not only dups of scans
			//try {
			//	Thread.sleep(50);
			//} catch (Exception e) {
			//	//Do nothing...
			//}
		//	this.statusText.setText("Init "+(i+1)+" of "+scanNums+" started");
		//}
		
	} 
	
	//This class is a receiver to be used when a scan completed.
	// It automatically initiates the new scan!
	class WiFiReceiver extends BroadcastReceiver {
        private int i = 0;
		
		public void onReceive(Context c, Intent intent) {
            
        	System.err.println("Receiver reached");
        	
        	statusText.setText("Sample "+((i++)+1)+" of "+scanNums+" completed");
        	
        	scanList = myWiFi.getScanResults();
        	
        	if (i<scanNums) 
        		myWiFi.startScan();
        		//No longer Intended
        	else
        		unregisterReceiver(this);
        	
        	//Lgging Data
            
            for (ScanResult wScan : scanList) {
            	Logger.getInstance().dataLog(scanUID, wScan);
            }

            statusBar.incrementProgressBy(1);
            
        }
        
        
    }

	@Override
	public void onClick(View v) {
		//This is only to go back
		Logger.getInstance().dataFinish();
		finish();
		
	}

}
