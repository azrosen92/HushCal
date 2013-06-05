package com.example.hushcal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.ParseException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventTableHandler extends SQLiteOpenHelper {

	private static final String KEY_ID = "id";
	private static final String KEY_EVENT_NAME = "event_name";
	private static final String KEY_START_TIME = "event_start";
	private static final String KEY_END_TIME = "event_end";
	private static final String KEY_STATUS = "status";

	private static final int DATABASE_VERSION = 2;
	private static final String EVENT_TABLE_NAME = "events";
	private static final String EVENT_TABLE_CREATE =
			"CREATE TABLE " + EVENT_TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY, " 
					+ KEY_EVENT_NAME + " TEXT, " + KEY_START_TIME + " DATETIME, " + KEY_END_TIME + " DATETIME, " 
					+ KEY_STATUS + " TEXT" +  ");";

	EventTableHandler (Context context) {
		super(context, "database", null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(EVENT_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		arg0.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE_NAME);

		onCreate(arg0);

	}

	// Adding new contact
	public void addEvent(Event event) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_EVENT_NAME, event.getName());
		values.put(KEY_START_TIME, event.getStartTime());
		values.put(KEY_END_TIME, event.getEndTime());
		values.put(KEY_STATUS, event.getStatus());

		db.insert(EVENT_TABLE_NAME, null, values);
		db.close();

	}

	// Getting All Contacts
	public List<Event> getAllEvents() {
		List<Event> eventsList = new ArrayList<Event>();

		String selectQuery = "SELECT  * FROM " + EVENT_TABLE_NAME;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar inputCal = Calendar.getInstance();

		if (cursor.moveToFirst()) {
			do {
				Event event = new Event();
				try {
					event.setName(cursor.getString(0));
					inputCal.setTime(df.parse(cursor.getString(1)));
					event.setStartTime(inputCal);
					inputCal.setTime(df.parse(cursor.getString(2)));
					event.setEndTime(inputCal);
					event.setStatus(cursor.getString(3));
				} catch (java.text.ParseException e) {
					e.printStackTrace();
				}
				eventsList.add(event);
			} while(cursor.moveToNext());
		}
		return eventsList;
	}

	// Getting contacts Count
	public int getEventsCount() {
		String countQuery = "SELECT * FROM " + EVENT_TABLE_NAME;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();
		return cursor.getCount();
	}

	// Updating single contact
	public int updateEvent(Event event) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		
		values.put(KEY_EVENT_NAME, event.getName());
		values.put(KEY_START_TIME, event.getStartTime());
		values.put(KEY_END_TIME, event.getEndTime());
		values.put(KEY_STATUS, event.getStatus());
		
		return db.update(EVENT_TABLE_NAME, values, KEY_ID + "=?", 
				new String[] { String.valueOf(event.getId()) });
	}

	// Deleting single contact
	public void deleteEvent(Event event) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.delete(EVENT_TABLE_NAME, KEY_ID + "=?", 
				new String[] { String.valueOf(event.getId()) });
		db.close();
	}


}
