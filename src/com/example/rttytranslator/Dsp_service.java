package com.example.rttytranslator;

// Copyright 2012 (C) Matthew Brejza
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

//import org.mapsforge.core.GeoPoint;

//import com.brejza.matt.habmodem.PredictionGrabber.PredictionRxEvent;

//import com.example.rttytranslator.StringRxEvent;
//import com.example.rttytranslator.rtty_receiver;
//import ukhas.AscentRate;
//import ukhas.Gps_coordinate;
//import ukhas.HabitatRxEvent;
//import ukhas.Habitat_interface;
//import ukhas.Listener;
//import com.brejza.matt.habmodem.Payload;
//import ukhas.TelemetryConfig;
//import ukhas.Telemetry_string;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class Dsp_service extends Service implements StringRxEvent {

	//public final static String TELEM_RX = "com.brejza.matt.habmodem.TELEM_RX";
	//public final static String CHAR_RX = "com.brejza.matt.habmodem.CHAR_RX";
	//public final static String CHARS = "com.brejza.matt.habmodem.CHARS";
	//public final static String FFT_UPDATED = "com.brejza.matt.habmodem.FFT_UPDATED";
	//public final static String HABITAT_NEW_DATA = "com.brejza.matt.habmodem.HABITAT_NEW_DATA";
	//public final static String PREDICTION_NEW_DATA = "com.brejza.matt.habmodem.PREDICTION_NEW_DATA";
	//public final static String TELEM_STR = "com.brejza.matt.habmodem.TELEM_STR";
	//public final static String GPS_UPDATED = "com.brejza.matt.habmodem.GPS_UPDATED";
	//public final static String LOG_EVENT = "com.brejza.matt.habmodem.LOG_EVENT";
	//public final static String LOG_STR = "com.brejza.matt.habmodem.LOG_STR";
	
	 // Binder given to clients
    //private final IBinder mBinder = new LocalBinder();
    rtty_receiver rcv = new rtty_receiver();
    private AudioRecord mRecorder;
    private AudioTrack mPlayer;
	private int buffsize;
	boolean isRecording = false;
	boolean usingMic = false;
	//HeadsetReceiver headsetReceiver;
	
	private int _baud = 300;
	short[] buffer = new short[buffsize];
	//private int last_colour = 0;
	
	//Telemetry_string last_str;
	
	boolean _enableChase = false;
	boolean _enablePosition = false;
	
	private boolean enableEcho = false;
	public boolean enableBell = false;
	public boolean enableUploader = true;
	private boolean _enableDecoder = false;

	//Timer updateTimer;
	//Timer serviceInactiveTimer;
	//private int lastHabitatFreq=0;
	
	//NotificationManager nm;
	
	Handler handler;
	Handler handler2;
	
	Toast toast;
	
	
	
	public Dsp_service() {
		rcv.addStringRecievedListener(this);		
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		System.out.println("Service started");
	}

	
	public void startAudio()
	{
		if (!_enableDecoder)
			return;
		
		//boolean mic = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
		
		System.out.println("isRecording: " + isRecording);
		
		if (!isRecording)
		{
			isRecording = true;
			
			buffsize = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_IN_MONO ,AudioFormat.ENCODING_PCM_16BIT);
	    	buffsize = Math.max(buffsize, 3000);
	    	
	    	mRecorder = new AudioRecord(AudioSource.MIC,8000,
	    			AudioFormat.CHANNEL_IN_MONO ,
	    			AudioFormat.ENCODING_PCM_16BIT,buffsize);
	    	
	    	mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC,8000,AudioFormat.CHANNEL_OUT_MONO,
	    			AudioFormat.ENCODING_PCM_16BIT,2*buffsize,AudioTrack.MODE_STREAM);
	    	
	    	if (enableEcho)
	    	{
		    	AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				manager.setMode(AudioManager.MODE_IN_CALL);
		    	manager.setSpeakerphoneOn(true);
	    	}
	    	
	    	if (mRecorder.getState() != AudioRecord.STATE_INITIALIZED)
	    	{
	    		
	    		mRecorder = new AudioRecord(AudioSource.DEFAULT,8000,
		    			AudioFormat.CHANNEL_IN_MONO ,
		    			AudioFormat.ENCODING_PCM_16BIT,buffsize);
	    		
	    		
	    		
	    	}	    
	    	
	    	mRecorder.startRecording();
	    	System.out.println("STARTING THREAD");
	    	Thread ct = new captureThread();
	    	
	        ct.start();
		}
	}
	
//	public void stopAudio()
	//{
	//	isRecording = false;
	//}
	
		public void enableEcho()
	{
		if (isRecording){
			AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	    	manager.setMode(AudioManager.MODE_IN_CALL);
	    	manager.setSpeakerphoneOn(true);
		}
    	enableEcho = true;
	}
	
	public void disableEcho()
	{
		AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		manager.setMode(AudioManager.MODE_NORMAL);
    	manager.setSpeakerphoneOn(false);
    	enableEcho = false;
	}
	
	class captureThread extends Thread
    {
    	
    	public void run() 
    	{  

    		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            //short[] buffer = new short[buffsize];
            double[] s = new double[buffsize];
            mRecorder.startRecording();
            isRecording = true;
            //AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            boolean lastHead = false;
            int clippingCount = 0;
            int samplesSinceToast = 0;
            
            
 
          //  setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            
            //buffsize =  mRecorder.read(buffer, 0, buffsize);  
        	//mPlayer.write(buffer, 0, buffsize);
        	
        	int readres;
        	
            while(isRecording && _enableDecoder) 
            {
            	readres =  mRecorder.read(buffer, 0, buffsize);  
            	if (readres < 10)
            	{
            		_enableDecoder = false;
            		
            	}
            	else
            	{
	            	if (usingMic && enableEcho){	            	
		            	mPlayer.write(buffer, 0, readres);
		            	if (mPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING && lastHead==true)
		            		mPlayer.play();
		            	lastHead = true;
	            	}
	            	else{
	            		if (mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
	            			mPlayer.stop();
	            			mPlayer.flush();
	            		}
	            		lastHead = false;
	            	}
	            	
	            	if (readres >= 512)
	            	{
	            		int i;
		                s = new double [readres];
		                for (i = 0; i < readres; i++)
		            	    s[i] = (double) buffer[i];
		                
		                i=0;
		                clippingCount = 0;
		                while (i < readres)
		                {
		                	if (buffer[i] > 30000 || buffer[i] < -30000)
		                		clippingCount++;
		                	
		                	i += 10;
		                }
		                
		                if (clippingCount > 10){
		                	if (samplesSinceToast <= 0 || samplesSinceToast > 8000*3)
		                	{
		                		samplesSinceToast = readres;
			                	System.out.println("Clipping detected");
			                	handler.post(new Runnable(){
			                		@Override
			                		public void run() {
			                			if (toast != null){
			                				toast.cancel();
			                				toast = null;
			                			}
			                			toast = Toast.makeText(getApplicationContext(), "Clipping Detected", Toast.LENGTH_SHORT);
			                			toast.show();
			                		}
			                	});
		                	}
		                }
		                samplesSinceToast += readres;
		                
		                //String rxchar =  rcv.processBlock(s,_baud);

		                
		                //Intent it = new Intent(CHAR_RX);
		                //it.putExtra(CHARS, rxchar);
		                //sendBroadcast(it);
		                
		                //if (listRxStrUpdated)
		                	//sendBroadcast(new Intent(TELEM_RX));
		                //if (rcv.get_fft_updated())
		                	//sendBroadcast(new Intent(FFT_UPDATED));
	            	}	                
            	}
             }

            if (mPlayer != null)
            {
            	if(mPlayer.getState() != AudioTrack.STATE_UNINITIALIZED){
		            mPlayer.stop();
		            mPlayer.release();
            	}
            }
            if (mRecorder != null)
            {
            	if (mRecorder.getState() != AudioRecord.STATE_UNINITIALIZED){
		            mRecorder.stop();
		            mRecorder.release();
            	}
            }
            System.out.println("DONE RECORDING");
            
            isRecording = false;
            mRecorder = null;
            mPlayer = null;
            
            AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    		manager.setMode(AudioManager.MODE_NORMAL);
        	manager.setSpeakerphoneOn(false);            
            
            //nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //nm.cancel(1);
       }	
    	
    }
	
	public void enableDecoder()
	{
		_enableDecoder = true;
		startAudio();
	}

	public void disableDecoder()
	{
		_enableDecoder = false;
	}
	public boolean getDecoderRunning()
	{
		return _enableDecoder;
	}   

	public void setBaud(int baud)
	{
		_baud = baud;
	}
	public int getBaud(){
		return _baud;
	}
	
	public short[] getData() {
		return buffer;
	}

	@Override
	public void StringRx(String strrx, boolean checksum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
			

	
	

}