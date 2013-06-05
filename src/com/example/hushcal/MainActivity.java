package com.example.hushcal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;



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



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	
	public void syncCalClickHandler(View v) {
		startActivity(new Intent(this, SyncCal.class));
	}
	
	public void addEventClickHandler(View v) {
		startActivity(new Intent(this, AddEvent.class));
	}


	
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}

