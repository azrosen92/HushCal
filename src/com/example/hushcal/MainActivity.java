package com.example.hushcal;

import android.net.Uri;
import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.Menu;

public class MainActivity extends Activity {
	
	private AudioManager mAudioManager;
	private boolean mPhoneIsSilent;
	mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
	checkIfPhoneIsSilent();
	checkIfPhoneIsLoud();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	/** Check user calendars to see if an event is happening.
	 * If one is, print a message. */
	public void checkCal() {
		 Context context = getApplicationContext();    
         ContentResolver contentResolver = context.getContentResolver();
         //Changes. 
         
          // get current UTC time
          long now = System.currentTimeMillis();  
          
          // Read all calendars available on device
          Cursor cur = contentResolver.
        		  query(Uri.parse("content://com.android.calendar/events"),
	        		  new String[]{ "calendar_id",
	            	  				"title", 
	            	  				"description", 
	            	  				"dtstart", 
	            	  				"dtend",}, 
	            	  "calendar_id=1", null, null);
          
          // If the user has at least one calendar event,
          // get each event's calendar id, title, description, start time, and end time.
          if (cur.moveToFirst()) {  
	          String[] events = new String[cur.getCount()];  
	          int[] calIds = new int[cur.getCount()];
	          
	          for (int i = 0; i < events.length; i++) {  
	            calIds[i] = cur.getInt(0);             
	            events[i] = "Event"+cur.getInt(0)+
	            			  ": \nTitle: " + cur.getString(1) +
	            			  "\nDescription: " + cur.getString(2) +
	            			  "\nStart Date: " + cur.getLong(3) +
	            			  "\nEnd Date : " + cur.getLong(4);  
	
	            // Get event's start and end times
	            long StartTime = cur.getLong(3);  
	            long EndTime = cur.getLong(4);  
	
	            // Compare current time with calendar start and end times. 
	            if ((StartTime <= now)&&(EndTime >= now)) {  
	            	 // Here, instead of printing a message, we should trigger the silence function.
	            	private void checkIfPhoneIsSilent() {
	            		int ringerMode = mAudioManager.getRingerMode();
	            		if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
	            			mPhoneIsSilent = true;
	            		}
	            		else {
	            			mPhoneIsSilent = false;
	            		}
	            	}
	            	
	                 System.out.println("An event is currently happening!");  
	                 break;  
	            }                  
	            cur.moveToNext();  
	            
	            if ((StartTime >= now)&&(EndTime <= now>)) {
	            	// Now, trigger the revert to loud function.
	            	private void checkIfPhoneIsLoud() {
	            		   int ringerMode = mAudioManager.getRingerMode();
	            		   if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
	            			   mPhoneIsSilent = false;
	            		   }
	            		   else {
	            			   mPhoneIsSilent = true;
	            		   }
	            		   }
	            	
	            	System.out.println("You just got out of an event!");
	            	break;
	            }
	          }
          }
          cur.close();  
	}
	
}