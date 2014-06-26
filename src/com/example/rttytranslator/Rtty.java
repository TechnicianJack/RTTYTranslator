package com.example.rttytranslator;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.File;
 
import android.content.Context;
import android.util.Log;
 
public class Rtty {
 
    private final static String TAG = "SquirrelRadio";
 
    // (CENTRE_FREQ + FREQ_SHIFT/2) / BAUD_RATE must be integer
    public final static int CENTRE_FREQ = 1075; // 1075
    public final static int FREQ_SHIFT = 350; //350
    public final static int BAUD_RATE = 50; //50
 
    // 8000Hz, 11025Hz, 44100Hz all work
    public final static int SAMPLE_RATE = 44100;
 
    // Length (bits) of lock-on tone before messages
    public final static int LOCK_ON = 300; //300
 
    // Minimum time (ms) between transmissions
    public final static int MIN_GAP = 5000;
 
    private final byte[] mark;
    private final byte[] space;
    
    private Context context;
 
    public String lastGeneneratedFilename;
    
    // Initialises the mark and space 16 bit sound arrays
    public Rtty(Context context) {
        
        this.context = context;
        
        int markFreq = CENTRE_FREQ + FREQ_SHIFT / 2;
        int spaceFreq = CENTRE_FREQ - FREQ_SHIFT / 2;
 
        mark = generateTone(markFreq);
        space = generateTone(spaceFreq);
 
    }
 
    // Generates a tone of particular frequency (1 bit long at specified baud
    // rate)
    private byte[] generateTone(double freq) {
 
        int numSamples = SAMPLE_RATE / BAUD_RATE;
        double[] sample = new double[numSamples];
        byte[] output = new byte[2 * numSamples];
 
        // Create sine-wave sample
        double x = 2 * Math.PI * freq / SAMPLE_RATE;
        for (int i = 0; i < numSamples; i++) {
            sample[i] = Math.sin(x * i);
        }
 
        // Convert to 16 bit WAV PCM sound array
        int j = 0;
        for (final double dVal : sample) {
            final short val = (short) (dVal * Short.MAX_VALUE);
            output[j++] = (byte) (val & 0x00ff); // Lower byte
            output[j++] = (byte) ((val & 0xff00) >>> 8); // Upper byte
        }
 
        return output;
    }
 
    // Takes string and returns RTTY audio sample, complete with start and stop
    // bits.
    public File generateWav(String text) {
 
        boolean[] bits = createBits(text);
 
        int numSamples = mark.length * (bits.length + LOCK_ON);
        WavBuilder wav = new WavBuilder(1, SAMPLE_RATE, numSamples / 2, context);
        this.lastGeneneratedFilename = wav.filenameWithExtension;
        
        BufferedOutputStream output = wav.getOutputStream();
 
        if (!wav.isError()) {
            try {
                // Lock-on bits
                for (int k = 0; k < LOCK_ON; k++) {
                    output.write(mark);
                }
 
                // Data bits
                for (boolean bit : bits) {
                    byte[] tone = bit ? mark : space;
                    output.write(tone);
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
                wav.error();
            }
        }
 
        wav.close();
 
        return wav.getFile();
    }
 
    // Takes string and returns RS232 (serial) bits
    private boolean[] createBits(String msg) {
        byte[] bytes = msg.getBytes();
        boolean[] bits = new boolean[bytes.length * (Byte.SIZE + 3)];
 
        int j = 0;
 
        for (byte b : bytes) {
 
            bits[j++] = false; // Start bit
 
            int val = b;
            for (int i = 0; i < 8; i++) {
                // Least-significant bit first
                bits[j++] = ((val & 1) == 0) ? false : true;
                val >>= 1;
            }
 
            // Stop bits
            bits[j++] = true;
            bits[j++] = true;
 
        }
 
        return bits;
    }
 
}
