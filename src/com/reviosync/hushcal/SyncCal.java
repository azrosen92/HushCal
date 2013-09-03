package com.reviosync.hushcal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.calendar.Calendar.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.reviosync.hushcal.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SyncCal extends Activity {

	//hashmap of account names and oauth keys
	private static HashMap<String, Account> accounts;
	private static EventTableHandler handler;
	private static HashMap<String, HCEvent> events_list; //list of events constructed from data taken from android calendar
	private static HashMap<String, String> event_status_map; //list of event names and statuses taken from hushcal database

	static Context app_context;
	private static com.google.api.services.calendar.Calendar client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.synccal);
		app_context = getApplicationContext();

		handler = new EventTableHandler(this);

		/**
		 * generate spinner dropdown menu and populate with calendars from phone
		 */
		final Spinner calendar_list = (Spinner)findViewById(R.id.calendar_list);
		calendar_list.setOnItemSelectedListener(spinner_listener);
		accounts = getCalendarsOnPhone();
		ArrayList<String> calendars = new ArrayList<String>(accounts.keySet());
//		for (String account_name : accounts.keySet()) {
//			calendars.add(account_name);
//		}

		/*
		 * Get all events from in-app database, with their statuses
		 * 
		 * This will be used for quickly changing the status when the
		 * user clicks the status button on under the event
		 */
		List<HCEvent> events_list = handler.getAllEvents(); //turn this into event_status_map
		event_status_map = new HashMap<String, String>();
		for (HCEvent hCEvent : events_list) {
			String title = hCEvent.getName();
			String status = hCEvent.getStatus();
			event_status_map.put(title, status);
		}

		ArrayAdapter<String> spinner_array = 
				new ArrayAdapter<String>(getApplicationContext(), 
						android.R.layout.simple_spinner_item, 
						calendars);
		spinner_array.setDropDownViewResource(R.layout.custom_dropdown_item);
		calendar_list.setAdapter(spinner_array);
	}

	// The indices for the projection array above.
	private static final int CALENDAR_ID_INDEX = 0;
	private static final int CALENDAR_DISPLAY_NAME_INDEX = 1;

	//returns a hashmap of all available calendars and their ids
	//	private HashMap<String, Long> getCalendars() {
	//
	//		HashMap<String, Long> results_list = new HashMap<String, Long>();
	//
	//		if (android.os.Build.VERSION.SDK_INT >= 4.0) {
	//
	//			// Run query
	//			Cursor cur = null;
	//			ContentResolver cr = getContentResolver();
	//			Uri uri = Calendars.CONTENT_URI;
	//			// Submit the query and get a Cursor object back. 
	//			cur = cr.query(uri, null, null, null, null);
	//
	//			//get list of calendars from cursor object
	//			while(cur.moveToNext()) {
	//				long cal_id = cur.getLong(CALENDAR_ID_INDEX);
	//				String cal_display_name = cur.getString(CALENDAR_DISPLAY_NAME_INDEX);
	//
	//				results_list.put(cal_display_name, cal_id);
	//			}
	//
	//			return results_list;
	//		} else {
	//			AccountManager acctmgr = AccountManager.get(app_context);
	//			Account[] accounts = acctmgr.getAccountsByType("com.google");
	//
	//			for (Account account : accounts) {
	//				String key = account.name;
	//				long value = 0L;
	//				results_list.put(key, value);
	//			}
	//			//TODO: get calendars from google calendar api - for previous android versions 
	//			//		(higher priority now, should start working on this right after release)
	//			return results_list;
	//		}
	//
	//	}

	private HashMap<String, Account> getCalendarsOnPhone() {
		HashMap<String, Account> calendar_accounts = new HashMap<String, Account>();

		AccountManager acctmgr = AccountManager.get(app_context);
		Account[] accounts = acctmgr.getAccountsByType("com.google");

		for (Account account : accounts) {
//			String auth_token_type = "oauth2:https://www.googleapis.com/auth/calendar";
//			AccountManagerFuture<Bundle> amf = acctmgr.getAuthToken(account, auth_token_type, null, this, null, null);
//
//			String authToken;
//			try {
//				Bundle authTokenBundle = amf.getResult();
//				authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
//				
//				// invalidate the token since it may have expired.
//				acctmgr.invalidateAuthToken("com.google", authToken);
//				
//				amf = acctmgr.getAuthToken(account, auth_token_type, null, this, null, null);
//				authTokenBundle = amf.getResult();
//				authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
//			} catch(Exception e) {
//				authToken = "";
//			}
			calendar_accounts.put(account.name, account);
		}

		return calendar_accounts;
	}

	/*
	 * This method will query the google calendar api to retrieve all events
	 * associated with calendar_name, and return a hashmap of names of events
	 * along with the event itself in an HCEvent object
	 */
	private HashMap<String, HCEvent> getCalendarEvents(Account calendar_account) throws IOException {
		HttpTransport httpTransport = new NetHttpTransport();
	    JacksonFactory jsonFactory = new JacksonFactory();
	    
		AccountManager acctmgr = AccountManager.get(app_context);

	    String auth_token_type = "oauth2:https://www.googleapis.com/auth/calendar";
		AccountManagerFuture<Bundle> amf = acctmgr.getAuthToken(calendar_account, auth_token_type, null, this, null, null);

		String oauth_token;
		try {
			Bundle authTokenBundle = amf.getResult();
			oauth_token = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
			
			amf = acctmgr.getAuthToken(calendar_account, auth_token_type, null, this, null, null);
			authTokenBundle = amf.getResult();
			oauth_token = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
		} catch(Exception e) {
			oauth_token = "";
		}
		
		List<HCEvent> cal_list = queryCalendar(calendar_account.name, oauth_token);
		
		HashMap<String, HCEvent> return_map = new HashMap<String, HCEvent>();
//		GoogleCredential credential = new GoogleCredential().setAccessToken(oauth_token);
//		com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(httpTransport, jsonFactory, credential).build(); //create a Calendar object using the oauth token for associated with calendar_name
//		com.google.api.services.calendar.model.Events events = service.events().list("primary").setPageToken(null).execute();
//		//List<HCEvent> items = queryCalendar(calendar_name, oauth_token);
//		List<Event> items = events.getItems();
//
//		for (Event event : items) {
//			String event_name = event.getDescription(); 
//			event.getDescription();
//			EventDateTime event_start = event.getStart();
//			long start = event_start.getDateTime().getValue();
//			EventDateTime event_end = event.getEnd();
//			long end = event_end.getDateTime().getValue();
//			String event_id = event.getId();
//			HCEvent hCEvent = new HCEvent(Integer.parseInt(event_id), event_name, start, end, null);
//			return_map.put(event_name, hCEvent);
//		}
		return return_map;
	}

	private List<HCEvent> queryCalendar(String account, String oauthToken) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
		String charset = "UTF-8";
		//parameters
		String orderBy = "updated";
		String key = "AIzaSyDVPwgjtkflGj4HtANozDfrUDAleGXCgMw";
		String timeMin = sdf.format(Calendar.getInstance().getTime());

		String query = "";
		try {
			query = String.format("key=%s", URLEncoder.encode(key, charset));
//			query = String.format("orderBy=%s&key=%s&timeMin=%s", 
//					URLEncoder.encode(orderBy, charset),
//					URLEncoder.encode(key, charset),
//					URLEncoder.encode(timeMin, charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		try {
			URLConnection connection = new URL(url + "?" + query).openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Authorization", "bearer " + oauthToken);
			connection.setRequestProperty("X-JavaScript-User-Agent", "Google APIs Explorer");
			InputStream response = connection.getInputStream();
			
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		

		return null;
	}


	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] {
		Events._ID,                     		// 0
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
	//	private HashMap<String, HCEvent> getCalendarEvents(String calendar_name) 
	//	{
	//		HashMap<String, HCEvent> events = new HashMap<String, HCEvent>();
	//
	//		String calendarID = cal_map.get(calendar_name).toString();
	//
	//		if (android.os.Build.VERSION.SDK_INT >= 4.0) {
	//			Cursor cur = null;
	//			ContentResolver cr = getContentResolver();
	//			Uri uri = Events.CONTENT_URI;
	//			String selection = "((" + Events.CALENDAR_ID + " = ?) AND (" + Events.DTSTART + " >= ?))";
	//			String [] selectionArgs = new String[] {calendarID, Calendar.getInstance().getTimeInMillis() + ""};
	//			String sortOrder = Events.DTSTART + " ASC";
	//			cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, sortOrder);
	//
	//			while(cur.moveToNext()) {
	//				String event_id = cur.getString(EVENT_ID_INDEX);
	//				String event_title = cur.getString(EVENT_TITLE_INDEX);
	//				Calendar event_start = Calendar.getInstance();
	//				event_start.setTimeInMillis(cur.getLong(EVENT_START_INDEX));
	//				Calendar event_end = Calendar.getInstance();
	//				event_end.setTimeInMillis(cur.getLong(EVENT_END_INDEX));
	//
	//				HCEvent event = new HCEvent(Integer.parseInt(event_id), event_title, event_start, event_end, null);
	//				if (event != null) {
	//					events.put(event_title, event);
	//				}
	//			}
	//		} else {
	//			//TODO: get events from google calendar api - for previous android versions 
	//			//		(higher priority, should start working on this right after release)
	//		}
	//
	//		return events;
	//
	//	}

	OnCheckedChangeListener event_status_listener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);

			if (checkedRadioButton != null) {
				String status = "";

				switch(checkedRadioButton.getId()) {
				case R.id.event_sound: status = "sound"; break;
				case R.id.event_silence: status = "silence"; break;
				case R.id.event_vibrate: status = "vibrate"; break;
				}

				LinearLayout parent = (LinearLayout) group.getParent();
				String event_name = ((TextView) parent.getChildAt(0)).getText().toString();

				//If the event is already in hushcal database, then update its status
				if (event_status_map.keySet().contains(event_name)) {
					//get the id from the HCEvent with name event_name
					//int event_id = events_list.get(event_name).getId();
					HCEvent updated_event = events_list.get(event_name); //new HCEvent(event_id, event_name, null, null, status);
					updated_event.setStatus(status);
					//TODO: unschedule old event (might not have to do this because of PendingIntent.FLAG_UPDATE_CURRENT, need to test)
					int updated = handler.updateEvent(updated_event);			
					EventScheduler.schedule(app_context, updated_event);
				}
				//otherwise (if you change an event taken from the android calendar but is not in
				//hushcal database yet) add event to hushcal database with new status 
				else {			
					HCEvent updated_event = events_list.get(event_name);
					updated_event.setStatus(status);
					handler.addEvent(updated_event);
					EventScheduler.schedule(app_context, updated_event);
				}

			}	
		}

	};

	OnItemSelectedListener spinner_listener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			
			Account selected = accounts.get(parent.getItemAtPosition(pos).toString());
			try {
				HashMap<String, HCEvent> events = getCalendarEvents(selected);
				System.out.println(events.size());
			} catch (IOException e) {
				e.printStackTrace();
			}
			//
			//			List<HCEvent> tmp_events_list = handler.getAllEvents(); //turn this into event_status_map
			//			event_status_map = new HashMap<String, String>();
			//			for (HCEvent event : tmp_events_list) {
			//				String title = event.getName();
			//				String status = event.getStatus();
			//				event_status_map.put(title, status);
			//			}
			//
			//			String selected = parent.getItemAtPosition(pos).toString();
			//			events_list = getCalendarEvents(selected);
			//			LinearLayout events_table = (LinearLayout)findViewById(R.id.event_table);
			//			events_table.removeAllViews();
			//			for (String event : events_list.keySet()) {
			//				//make row in table including event name and silence/vibrate radio button
			//				//and maybe a pop up that shows more info about event, such as start and
			//				//end time.
			//				LinearLayout tr = (LinearLayout)getLayoutInflater().inflate(R.layout.event_row_layout, null);
			//
			//				TextView text = (TextView) tr.getChildAt(0);
			//				text.setText(event);
			//				events_table.addView(tr);
			//
			//				RadioGroup status_group = (RadioGroup) tr.getChildAt(1);
			//
			//				//when you generate the table, populate it's radio buttons based on their
			//				//status in database
			//				String status;
			//				try {
			//					if (event_status_map.get(event).equalsIgnoreCase("vibrate")) {
			//						events_list.get(event).setStatus("vibrate");
			//						((RadioButton) status_group.getChildAt(2)).setChecked(true);
			//						status = "vibrate";
			//					} else if (event_status_map.get(event).equalsIgnoreCase("silence")) {
			//						events_list.get(event).setStatus("silence");
			//						((RadioButton) status_group.getChildAt(1)).setChecked(true);
			//						status = "silence";
			//					} else {
			//						events_list.get(event).setStatus("sound");
			//						((RadioButton) status_group.getChildAt(0)).setChecked(true);
			//						status = "sound";
			//					}
			//				} catch (Exception e) {
			//					e.printStackTrace();
			//				}
			//
			//				// query Events database each time a radio button is pressed to
			//				// update its silence/vibrate status
			//				status_group.setOnCheckedChangeListener(event_status_listener);
			//
			//
			//			}
		}


		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	};


}
