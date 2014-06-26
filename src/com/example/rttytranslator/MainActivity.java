package com.example.rttytranslator;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

//import com.example.rttytranslator.Rtty;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	
	Context mContext = this;
	
	Dsp_service dsps;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		//dsps = new Dsp_service ();
		
		//dsps.setBaud(50);
		//dsps.startAudio();
		
//dsps.getData().toString();

		
		
        Button buttonStart = (Button) findViewById(R.id.buttonSend);
        //Listen for when the button is pressed.
        buttonStart.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View view) {
        		
        		//Context mContext = this;
        	
				Log.v("jack","start button clicked");
				Rtty rtty = new Rtty(mContext);
				
				//TextView rx_txt;
				
				
				
				EditText ed = (EditText) findViewById(R.id.editText1);
	        	String txt = new String();
	          	String topstring;
	          	Button b = (Button) findViewById(R.id.buttonSend);
	          	// Get text from edittext. This is then encoded as RTTY tones in a WAV file
	        	txt=ed.getText().toString();
	          	
	        	
	        	//This creates the WAV file from the entered text.
				File myRttyFile = rtty.generateWav(txt);
				WavPlayer wp = new WavPlayer();
				//Find the last file that was produced and play it.
				wp.audioPlayer(rtty.lastGeneneratedFilename);
	          	
	         // Clear, send and RX buttons
	    	    Button buttonSend = (Button) findViewById(R.id.buttonSend);
	    	    Button buttonClear = (Button) findViewById(R.id.buttonClear);
	    	    Button buttonRX = (Button) findViewById (R.id.buttonRX);
	    	    
	    	    // TextChangedListener prevents send button being pressed while text entry field is empty.
	    	    ed.addTextChangedListener(new TextWatcher() {
	                @Override
	                public void onTextChanged(CharSequence s, int start, int before, int count) {
	                	Button b = (Button) findViewById(R.id.buttonSend);
	                	b.setEnabled(true);
	                	
	                }
	                
	                @Override
	                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	                    // TODO Auto-generated method stub
	                }

	                @Override
	                public void afterTextChanged(Editable s) {

	                    // TODO Auto-generated method stub
	                }
	            });
	                
	                if (ed.getText().toString().equals("") )
	          		{
	                	buttonSend.setEnabled(false); //disable button;        		
	          		}

	               //Clear button sets the content of each TextView to "" therefore clearing the display.
	                buttonClear.setOnClickListener(new OnClickListener()
	                {
	                  public void onClick(View v)
	                  {
	        	          TextView textViewRX = (TextView) findViewById(R.id.textViewRX);
	        	          TextView textViewTX = (TextView) findViewById(R.id.textViewTX);
	        	          textViewRX.setText("");
	        	          textViewTX.setText("");
	        	          
	                  }
	                });
	                
	                //Test code to demonstrate receiving.
	                buttonRX.setOnClickListener(new OnClickListener()
	                {
	                  public void onClick(View v)
	                  {
	        	          TextView textViewRX = (TextView) findViewById(R.id.textViewRX);
	        	          textViewRX.setText("RX:Hello, name is Mike");
	        	          
	                  }
	                });
	          	
	          	
	            TextView textViewTX = (TextView) findViewById(R.id.textViewTX);

	            //get text from edittext and convert it to string
	            String messageString=ed.getText().toString();

	            //Add to text window
	            topstring=textViewTX.getText().toString();
	            
	            
	            //set string from edittext to textview
	            textViewTX.setText("\nTX:" + messageString + "\n" + topstring);
	        		
	            //clear edittext after sending text to message
	            ed.setText("");
	            
	            if (ed.getText().toString().equals("") )
	      		{
	            	b.setEnabled(false); //disable button;        		
	      		}else{
	      			b.setEnabled(true);
	      		}
			}

		});
        
	}
	// TIMER

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
