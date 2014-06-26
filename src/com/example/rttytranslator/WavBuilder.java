package com.example.rttytranslator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
 
import android.content.Context;
import android.os.Environment;
import android.util.Log;
 
public class WavBuilder {
 
    public static final String TAG = "SquirrelRadio";
 
    public String filenameWithExtension;
    private File file;
 
    private boolean CLOSED = false;
    private boolean ERROR = false;
 
    BufferedOutputStream bufferedOutput;
 
    private Context context;
    
    public WavBuilder(int channels, int rate, int samples, Context context) {
 
        this.context = context;
        //This names the file. It is the system's current time with .wav added on to the end.
        filenameWithExtension = System.currentTimeMillis() + ".wav";
 
        try {
 
            // This bit specifices where the WAV file will be written.
            // I've changed it to save to the SD card which will make
            // it easier to test.
            file = new File(Environment.getExternalStorageDirectory(),
                    filenameWithExtension);
            bufferedOutput = new BufferedOutputStream(
                      new FileOutputStream(file));
 
            byte[] header = new byte[44];
            WavHeader.writeHeader(header, channels, rate, 2, samples);
 
            bufferedOutput.write(header);
 
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException", e);
            ERROR = true;
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            ERROR = true;
        }
 
    }
 
    public BufferedOutputStream getOutputStream() {
        return bufferedOutput;
    }
 
    public void close() {
        if (!ERROR) {
            try {
                if (bufferedOutput != null)
                    bufferedOutput.close();
                CLOSED = true;
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
                ERROR = true;
            }
        }
 
        if (ERROR) {
            try {
                // Try to delete the file
                file.delete();
            } catch (Exception e) {
                // Do nothing...
            }
        }
    }
 
    public File getFile() {
        if (CLOSED && !ERROR) {
            return file;
        } else {
            return null;
        }
    }
 
    public boolean isError() {
        return ERROR;
    }
 
    public void error() {
        ERROR = true;
    }
    
}
