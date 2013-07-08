package com.example.hushcal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SyncCal extends Activity {
	
	private static HashMap<String, Long> cal_map;
	private static EventTableHandler handler;
	private static HashMap<String, Event> events_list; //list of events constructed from data taken from android calendar
	private static HashMap<String, String> event_status_map; //list of event names and statuses taken from hushcal database
	
	static Context app_context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.synccal);
		app_context = getApplicationContext();
		
		handler = new EventTableHandler(this);

		/**
		 * generate spinner dropdown menu and populate with calendars from phone
		 */
		Spinner calendar_list = (Spinner)findViewById(R.id.calendar_list);
		calendar_list.setOnItemSelectedListener(spinner_listener);
		cal_map = getCalendars();
		ArrayList<String> selector_array = new ArrayList<String>();
		selector_array.addAll(cal_map.keySet());
		
		List<Event> events_list = handler.getAllEvents(); //turn this into event_status_map
		event_status_map = new HashMap<String, String>();
		for (Event event : events_list) {
			String title = event.getName();
			String status = event.getStatus();
			event_status_map.put(title, status);
		}
		
		ArrayAdapter<String> spinner_array = 
				new ArrayAdapter<String>(getApplicationContext(), 
						android.R.layout.simple_spinner_item, 
						selector_array);
		spinner_array.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		calendar_list.setAdapter(spinner_array);

	}

	// The indices for the projection array above.
	private static final int CALENDAR_ID_INDEX = 0;
	private static final int CALENDAR_DISPLAY_NAME_INDEX = 1;

	//returns a hashmap of all available calendars and their ids
	private HashMap<String, Long> getCalendars() {

		HashMap<String, Long> results_list = new HashMap<String, Long>();

		if (android.os.Build.VERSION.SDK_INT >= 4.0) {

			// Run query
			Cursor cur = null;
			ContentResolver cr = getContentResolver();
			Uri uri = Calendars.CONTENT_URI;	
			// Submit the query and get a Cursor object back. 
			cur = cr.query(uri, null, null, null, null);

			//get list of calendars from cursor object
			while(cur.moveToNext()) {
				long cal_id = cur.getLong(CALENDAR_ID_INDEX);
				String cal_display_name = cur.getString(CALENDAR_DISPLAY_NAME_INDEX);
				
				results_list.put(cal_display_name, cal_id);
			}

			return results_list;
		} else {
			//TODO: get calendars from google calendar api - for previous android versions (low priority, maybe save for update)
			return results_list;
		}

	}

	
	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] {
	    Events.ORIGINAL_ID,                     // 0
	    Events.TITLE,                  			// 1
	    Events.DTSTART,							// 2
	    Events.DTEND							// 3
	};

	// The indices for the projection array above.
	private static final int EVENT_ID_INDEX = 0;
	private static final int EVENT_TITLE_INDEX = 1;
	private static final int EVENT_START_INDEX = 2;
	private static final int EVENT_END_INDEX = 3;

	//returns an arraylist of events constructed from data queried from the android calendar
	private HashMap<String, Event> getCalendarEvents(String calendar_name) 
	{
		HashMap<String, Event> events = new HashMap<String, Event>();
		
		String calendarID = cal_map.get(calendar_name).toString();
		
		if (android.os.Build.VERSION.SDK_INT >= 4.0) {
			Cursor cur = null;
			ContentResolver cr = getContentResolver();
			Uri uri = Events.CONTENT_URI;
			String selection = "((" + Events.CALENDAR_ID + " = ?))";
			String [] selectionArgs = new String[] {calendarID};
			cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
			
			while(cur.moveToNext()) {
				//TODO: getting null for event_id
				String event_id = cur.getString(EVENT_ID_INDEX);
				String event_title = cur.getString(EVENT_TITLE_INDEX);
				Calendar event_start = Calendar.getInstance();
				event_start.setTimeInMillis(cur.getLong(EVENT_START_INDEX));
				Calendar event_end = Calendar.getInstance();
				event_end.setTimeInMillis(cur.getLong(EVENT_END_INDEX));
		
				Event event = new Event(Integer.parseInt(event_id), event_title, event_start, event_end, null);
				events.put(event_title, event);
			}
		} else {
			//TODO: get events from google calendar api - for previous android versions (low priority, maybe save for update)
		}
		
		return events;

	}
	
	OnCheckedChangeListener event_status_listener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
			String status = checkedRadioButton.getText().toString();
			
			TableRow parent = (TableRow) group.getParent();
			String event_name = ((TextView) parent.getChildAt(0)).getText().toString();
			
			//If the event is already in hushcal database, then update its status
			if (event_status_map.keySet().contains(event_name)) {
				//get the id from the Event with name event_name
				int event_id = events_list.get(event_name).getId();
				Event updated_event = new Event(event_id, event_name, null, null, status);
				//TODO: unschedule old event (might not have to do this because of PendingIntent.FLAG_UPDATE_CURRENT, need to test)
				handler.updateEvent(updated_event);			
				EventScheduler.schedule(app_context, updated_event);
			}
			//otherwise (if you change an event taken from the android calendar but is not in
			//hushcal database yet) add event to hushcal database with new status 
			else {			
				Event updated_event = events_list.get(event_name);
				updated_event.setStatus(status);
				handler.addEvent(updated_event);
				EventScheduler.schedule(app_context, updated_event);
			}
			
		}		
		
	};

	OnItemSelectedListener spinner_listener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			//TODO: this needs to work every time the spinner changes
			String selected = parent.getItemAtPosition(pos).toString();
			events_list = getCalendarEvents(selected);
			TableLayout events_table = (TableLayout)findViewById(R.id.event_table);
			//events_table.removeAllViewsInLayout();
			for (String event : events_list.keySet()) {
				//make row in table including event name and silence/vibrate radio button
				//and maybe a pop up that shows more info about event, such as start and
				//end time.
				TableRow tr = (TableRow)getLayoutInflater().inflate(R.layout.event_row_layout, null);

				TextView text = (TextView) tr.getChildAt(0);
				text.setText(event);
				events_table.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
																	  TableLayout.LayoutParams.WRAP_CONTENT));

				RadioGroup status_group = (RadioGroup) tr.getChildAt(1);
				
				//when you generate the table, populate it's radio buttons based on their
				//status in database
				if (event_status_map.get(event).equals("vibrate")) {
					events_list.get(event).setStatus("vibrate");
					((RadioButton) status_group.getChildAt(1)).setChecked(true);
				} else if (event_status_map.get(event).equals("silence")) {
					events_list.get(event).setStatus("silence");
					((RadioButton) status_group.getChildAt(0)).setChecked(true);
				}
				
				// query Events database each time a radio button is pressed to
				// update its silence/vibrate status
				status_group.setOnCheckedChangeListener(event_status_listener);
				

			}
		}


		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	};
	

}
