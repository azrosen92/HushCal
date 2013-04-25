package com.example.hushcal;

import android.net.Uri;
import android.media.AudioManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity 
{

	private static final int StartTime = 0;
	private static final int EndTime = 0;
	private static final long periods = 0;
	private AudioManager mAudioManager;
	private boolean mPhoneIsSilent;
	private TimerTask task1;
	private Bundle cursor_event;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		silenceNow();
		normaliseNow();
		//Change - just added the timer function in here.
		//Timer Timer1 = new Timer(); 
		//timer1.scheduleAtFixedRate(task1,StartTime,periods);
		setButtonClickListener();

		//toggleUi();

	}



	/** Check user calendars to see if an event is happening.
	 * If one is, print a message. */
	public void checkCal() 
	{
		Context context = getApplicationContext();    
		ContentResolver contentResolver = context.getContentResolver();
		//Initialising variables
		long StartTime, EndTime;


		// get current UTC time
		long now = System.currentTimeMillis();  

		// Read all calendars available on device

		// Run query


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

		StartTime = cur.getLong(3);  
		EndTime = cur.getLong(4);  

		if (cur.moveToFirst()) 
		{  
			String[] events = new String[cur.getCount()];  
			int[] calIds = new int[cur.getCount()];

			for (int i = 0; i < events.length; i++) 
			{  
				calIds[i] = cur.getInt(0);             
				events[i] = "Event"+cur.getInt(0)+
						": \nTitle: " + cur.getString(1) +
						"\nDescription: " + cur.getString(2) +
						"\nStart Date: " + cur.getLong(3) +
						"\nEnd Date : " + cur.getLong(4);  

				// Get event's start and end times
				//cursor_event.getLong(cursor_event.getColumnIndex("dtstart"));
				// cursor_event.getLong(cursor_event.getColumnIndex("dtend"));



				// Compare current time with calendar start and end times. 
				silenceNow();
				System.exit(0);
				System.out.println("An event is currently happening!");  
				//toggleUi();

				/** if ((StartTime <= now)&&(EndTime >= now)) 
	                 {  
	            	 // Here, instead of printing a message, we should trigger the silence function.
	            	    //checkIfPhoneIsSilent();
	            	    silenceNow();
	                    System.out.println("An event is currently happening!");  
	                    toggleUi();
	                    break;
	                 }
	               if ( i < (events.length-1))
	               {
	            	   boolean moved = cur.moveToNext();
	               }
	              }
	            } 



	               else 
	               {
	            	   normaliseNow();
	            	  // checkIfPhoneIsLoud();
	               }

				 */

				/**  
				if ((StartTime > now)&&(EndTime < now)) {
	            	 // Now, trigger the revert to loud function.
	            	  checkIfPhoneIsLoud();
	            	  System.out.println("You just got out of an event!");
	            	  toggleUi();



				}
				 */
				// break;

				cur.close();
			}
		}



		/**	
	class ClickListener implements ActionListener {

		final static long DELAY = StartTime;

		public void actionPerformed(ActionEvent event) {
		int ringerMode = mAudioManager.getRingerMode();
        ringerMode = AudioManager.RINGER_MODE_SILENT;

	}
   }

   ActionListener listener = new ClickListener();
   toggleButton.addActionListener(listener);

   class TimerListener implements ActionListener {
        public void actionPerformed(ActionEvent event)
        {
           toggleButton.doClick();
        }
      }

      ActionListener timerListener = new TimerListener();
      Timer t = new Timer(DELAY, timerListener);
      t.start();
		 */ 


		task1 = new TimerTask() {
			public void run() {
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			}
		};
	}

	Timer timer1 = new Timer();
	//Timer1.scheduleAtFixedRate(task1,StartTime,events);
	/**	
   public void onClickListener(Button toggleButton)
	{
	 //fields
	 long StartTime;
	 long periods;
	 long EndTime;
	Cursor cur = ContentResolver.

    periods = EndTime - StartTime;
	 StartTime = cur.getLong(3);  
     EndTime = cur.getLong(4); 

	 Timer Timer1 = new Timer(); 
	timer1.scheduleAtFixedRate(task1,StartTime,periods);
	}

	 */	

	public static final String[] EVENT_PROJECTION = new String[] {
		Calendars._ID,                           // 0
		Calendars.ACCOUNT_NAME,                  // 1
		Calendars.CALENDAR_DISPLAY_NAME,         // 2
		Calendars.OWNER_ACCOUNT,                  // 3
	};

	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
	private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
	private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;


	@SuppressLint("NewApi")
	public abstract class CalChecker extends AsyncTask {

		ContentResolver cr = getContentResolver();
		Uri uri = Calendars.CONTENT_URI;   
		String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
				+ Calendars.ACCOUNT_TYPE + " = ?) AND ("
				+ Calendars.OWNER_ACCOUNT + " = ?))";
		String[] selectionArgs = new String[] {"sampleuser@gmail.com", "com.google",
		"sampleuser@gmail.com"};


		// Submit the query and get a Cursor object back. 
		Cursor cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
	}

	private void setButtonClickListener()
	{

		Button toggleButton = (Button)findViewById(R.id.toggleButton);
		toggleButton.setOnClickListener(new View.OnClickListener()  {

			public void onClick(View v) {

				if (mPhoneIsSilent) {
					//change back to Normal mode
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					normaliseNow();
					mPhoneIsSilent = false;

				}

				else {
					//Change to silent mode
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					silenceNow();
					mPhoneIsSilent = true;
				}

				//Now toggle the UI again
				toggleUi();
			}
		});
	}




	public void silenceNow() {
		int ringerMode = mAudioManager.getRingerMode();
		ringerMode = AudioManager.RINGER_MODE_SILENT;
	}

	public void normaliseNow()
	{
		int ringerMode = mAudioManager.getRingerMode();
		ringerMode = AudioManager.RINGER_MODE_NORMAL;
	}

	/**   
        public void checkIfPhoneIsSilent() {
		int ringerMode = mAudioManager.getRingerMode();
		if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
			mPhoneIsSilent = true;
		}
		else {
			mPhoneIsSilent = false;
		}
	}

	public void checkIfPhoneIsLoud() {
		   int ringerMode = mAudioManager.getRingerMode();
		   if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
			   mPhoneIsSilent = false;
		   }
		   else {
			   mPhoneIsSilent = true;
		   }
		   }
	 */

	public void toggleUi() {
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		Drawable newPhoneImage;
		if (mPhoneIsSilent) {
			newPhoneImage = getResources().getDrawable(R.drawable.toggle_status);
		}
		else {
			newPhoneImage = getResources().getDrawable(R.drawable.toggle_status_on);
		}
		imageView.setImageDrawable(newPhoneImage);
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}

