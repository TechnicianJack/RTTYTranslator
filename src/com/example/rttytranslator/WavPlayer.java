package com.example.rttytranslator;

import java.io.File;

import android.media.MediaPlayer;
import android.os.Environment;

public class WavPlayer {
	
	 public void audioPlayer(String fileName){
		    //set up MediaPlayer    
		    MediaPlayer mp = new MediaPlayer();

		    try {
		    	File dir = Environment.getExternalStorageDirectory();
		        mp.setDataSource(dir.getPath()+"/"+fileName);
		        mp.prepare();
		        mp.start();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}

}
