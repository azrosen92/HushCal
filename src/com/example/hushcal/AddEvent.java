package com.example.hushcal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

//TODO: make sure data is persistent when upgraded in app store
@SuppressWarnings("unused")
public class AddEvent extends FragmentActivity implements OnClickListener {

	CustomDateTimePicker custom;
	private int mButtonPressed;
	EventTableHandler handler;
	
	static Context app_context;

	boolean is_end_time_set = false;
	boolean is_start_time_set = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.addevent);
		app_context = getApplicationContext();

		handler = new EventTableHandler(this);

		final Calendar beginTime = Calendar.getInstance();
		final Calendar endTime = Calendar.getInstance();

		final EditText name = (EditText)findViewById(R.id.editText1);

		final RadioButton vibrate = (RadioButton)findViewById(R.id.vibrate);
		final RadioButton silence = (RadioButton)findViewById(R.id.silence);

		Button submit = (Button)findViewById(R.id.submit_event_button);

		//Method for when the "Done" button is pressed.
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String status = "";

				if (vibrate.isChecked()) status = "vibrate";
				else if(silence.isChecked()) status = "silence";
				String name_text = name.getText().toString();

				//error handling variables
				Context context = getApplicationContext();
				CharSequence statusText = "Please choose silence/vibrate";
				CharSequence nameText="Please enter a name";
				CharSequence timeText="Please enter a start and end time";
				int duration = Toast.LENGTH_SHORT;

				//if all fields are not filled show errors as Toast dialogs
				List<Toast> toast_list = new ArrayList<Toast>();
				if(status.equals("")){
					Toast toast = Toast.makeText(context, statusText, duration);
					toast_list.add(toast);
				}
				if(!is_end_time_set || !is_start_time_set){
					Toast toast = Toast.makeText(context, timeText, duration);
					toast_list.add(toast);
				}

				if(toast_list.isEmpty()) {					
					insertIntoCalendar(beginTime, endTime, name.getText().toString());
					int event_id = 1; //getEventIdFromCalendar(beginTime, endTime, name.getText().toString());
					Event new_event = new Event(event_id, name.getText().toString(), beginTime, endTime, status);
					handler.addEvent(new_event);
					//TODO: event scheduler not working need to read broadcastreceiver docs
					EventScheduler.schedule(app_context, new_event);
					
					//go back to home page
//					Toast.makeText(app_context, "Event created", Toast.LENGTH_SHORT).show();
//					Intent home_page = new Intent(app_context, MainActivity.class);
//					startActivity(home_page);
				}
				else {
					for(Toast toast : toast_list) {
						toast.show();
					}
				}
			}
		});

		/**
		 * Found this code online, really nice time and date picker dialog
		 * see http://stackoverflow.com/questions/15354089/get-date-and-time-picker-value-from-dialog-fragment-and-set-it-in-edit-text
		 */
		custom = new CustomDateTimePicker(this,
				new CustomDateTimePicker.ICustomDateTimeListener() {

			/**
			 * Event thrown when time and date have been set
			 * sets the TextView to the time and date
			 * then sets the beginTime variable to be used later
			 */
			@Override
			public void onSet(Dialog dialog, Calendar calendarSelected,
					Date dateSelected, int year, String monthFullName,
					String monthShortName, int monthNumber, int date,
					String weekDayFullName, String weekDayShortName,
					int hour24, int hour12, int min, int sec,
					String AM_PM) {
				String min_string = "" + min;
				if (min < 10) {
					min_string = "0" + min;
				}
				if (mButtonPressed == R.id.set_start) {
					((TextView) findViewById(R.id.start_time_label))
					.setText((monthNumber+1)
							+ "/" + calendarSelected
							.get(Calendar.DAY_OF_MONTH) + "/" + year
							+ ", " + hour12 + ":" + min_string
							+ " " + AM_PM);
					//TODO: check month for off by one error
					beginTime.set(year, monthNumber+1, Calendar.DAY_OF_MONTH, hour24, min);
				}
				else if (mButtonPressed == R.id.set_end) {
					((TextView) findViewById(R.id.end_time_label))
					.setText((monthNumber+1)
							+ "/" + calendarSelected
							.get(Calendar.DAY_OF_MONTH) + "/" + year
							+ ", " + hour12 + ":" + min_string
							+ " " + AM_PM);
					//TODO: check month for off by one error
					endTime.set(year, monthNumber+1, Calendar.DAY_OF_MONTH, hour24, min);
				}
			}

			@Override
			public void onCancel() {}
		});

		/**
		 * Pass Directly current time format it will return AM and PM if you set
		 * false
		 */
		custom.set24HourFormat(true);
		/**
		 * Pass Directly current data and time to show when it pop up
		 */
		custom.setDate(Calendar.getInstance());

		Button start_time_button = (Button)findViewById(R.id.set_start);
		start_time_button.setOnClickListener(this);

		Button end_time_button = (Button)findViewById(R.id.set_end);
		end_time_button.setOnClickListener(this);

		//check to see if start date and time have been set for error handler
		TextView start_time_text = (TextView)findViewById(R.id.start_time_label);
		start_time_text.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count){
				is_start_time_set = true;
			}

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
		});

		//checks to see if end date and time have been set for error handler
		TextView end_time_text = (TextView)findViewById(R.id.end_time_label);
		end_time_text.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count){
				is_end_time_set = true;
			}

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
		});




	}

	//returns the event_id from the android calendar, queried by event name, and time
	//the only problem I could see with this is if there are two events at the same time
	//with the same name, in which case I can't see it mattering which one you get the id from.
	protected int getEventIdFromCalendar(Calendar beginTime, Calendar endTime,
			String title) {
		int event_id = 0;
		
		// Run query
		Cursor cur = null;
		ContentResolver cr = getContentResolver();
		Uri uri = Events.CONTENT_URI;
		String[] projection = { Events.ORIGINAL_ID };
		String selection = "((" + Events.DTSTART + " = ?) AND (" 
				+ Events.DTEND + " = ?) AND (" + Events.TITLE + " = ?))";
		String[] selectionArgs = { String.valueOf(beginTime.getTimeInMillis()), 
				String.valueOf(endTime.getTimeInMillis()), 
				title};
		cur = cr.query(uri, projection, selection, selectionArgs, "original_id DESC");
		
		if(cur.moveToNext()) {
			event_id = Integer.parseInt(cur.getString(0));
		}
		return event_id;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/**
	 * event thrown for "set" buttons
	 * pressedButton(v): tells us which of the two buttons were pressed
	 * then shows the date and time picker dialog
	 */
	@Override
	public void onClick(View v) {
		pressedButton(v);
		custom.showDialog(v.getId());
	}

	//tells us which of the two "set" buttons was pressed
	public void pressedButton(View v) {
		mButtonPressed = v.getId();
	}

	//inserts information from the form into the hushcal database
	public void insertIntoCalendar(Calendar startTime, Calendar endTime, String name) {
		if (android.os.Build.VERSION.SDK_INT >= 4.0) {
			Intent intent = new Intent(Intent.ACTION_INSERT);
			intent.setType("vnd.android.cursor.item/event");
			intent.setData(CalendarContract.Events.CONTENT_URI);
			intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTimeInMillis());
			intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
			intent.putExtra(Events.TITLE, name);
			startActivity(intent);
		}
		else {
			//TODO: add event with google calendar api (low priority, maybe save for update)
		}
	}
}
