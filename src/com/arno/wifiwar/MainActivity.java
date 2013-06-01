package com.arno.wifiwar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

//import wifi.Scanner;

public class MainActivity extends Activity implements OnInitListener, OnClickListener {

	private int scanUID = 1;
	private TextView textScanUID;
	
	@SuppressWarnings("unused")
	private TextToSpeech tts;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts = new TextToSpeech(this, this);
        textScanUID = (TextView) findViewById(R.id.textScanUID);
        textScanUID.setText("UID: "+scanUID);
        textScanUID.setText("Test");
        System.err.println("Created?");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onResume() {
    	//setContentView(R.layout.activity_main);
    	System.err.println("Resume Test Main");
    	textScanUID.setText("UID: "+(scanUID));
    	
    	Button button = (Button) findViewById(R.id.b_scan);
		button.setOnClickListener(this);
    	super.onResume();
    }

	@Override
	public void onInit(int arg0) {
		//Scanner myScanner = new Scanner();
		//this.ScanActivity = new ScanActivity();
		
		System.err.println("Init done");
		//this.scan();
	}
	
	
	
	

	
	@Override
	public void onClick(View v) {
		System.err.println("ClickTest");
		Intent intent = new Intent(this, ScanActivity.class);
	    //Number of Scans - Shoould be modified by UI
		intent.putExtra("NUM_SCANS", main.Config.numberOfSamples);
		//pseudo-UID - incrementing while the App is running
		intent.putExtra("SCAN_UID", this.scanUID++);
		//intent.putExtra(EXTRA_MESSAGE, message);
	    startActivity(intent);
	}
	
	

	
    
}
