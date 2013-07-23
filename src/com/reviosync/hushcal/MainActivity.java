package com.reviosync.hushcal;

import com.reviosync.hushcal.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

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



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}

