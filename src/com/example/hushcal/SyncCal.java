package com.example.hushcal;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SyncCal extends Activity {
	
	//TODO: this class only works for one account on the phone, need to add support for multiple
	//		accounts.
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String account_name = "";
		String account_type = "";
		String owner_account = "";
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.synccal);
		
		/**
		 * generate spinner dropdown menu and populate with calendars from phone
		 */
		Spinner calendar_list = (Spinner)findViewById(R.id.calendar_list);
		ArrayAdapter<String> spinner_array = 
				new ArrayAdapter<String>(getApplicationContext(), 
						android.R.layout.simple_spinner_item, 
						getCalendars(account_name, account_type, owner_account));
		spinner_array.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		calendar_list.setAdapter(spinner_array);
		
	}
	
	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] {
	    Calendars._ID,                           // 0
	    Calendars.ACCOUNT_NAME,                  // 1
	    Calendars.CALENDAR_DISPLAY_NAME,         // 2
	    Calendars.OWNER_ACCOUNT                  // 3
	};
	  
	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
	private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
	private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
	
	private ArrayList<String> getCalendars(String account_name, String account_type, String owner_account) {
		ArrayList<String> results_list = new ArrayList<String>();
		
		// Run query
		Cursor cur = null;
		ContentResolver cr = getContentResolver();
		Uri uri = Calendars.CONTENT_URI;   
		String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
		                        + Calendars.ACCOUNT_TYPE + " = ?) AND ("
		                        + Calendars.OWNER_ACCOUNT + " = ?))";
		
		String[] selectionArgs = new String[] {account_name, account_type, owner_account}; 
		// Submit the query and get a Cursor object back. 
		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		
		//get list of calendars from cursor object
		while(cur.moveToNext()) {
			long cal_id = cur.getLong(PROJECTION_ID_INDEX);
			String cal_display_name = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
			String cal_account_name = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
			String cal_owner_name = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);	
			
			results_list.add(cal_display_name);
			
		}
		
		return results_list;
		
	}

}
