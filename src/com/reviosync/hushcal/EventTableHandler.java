package com.reviosync.hushcal;

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

//TODO: might want to change start and end time values to long so they can be compared easier
public class EventTableHandler extends SQLiteOpenHelper {

	private static final String KEY_ID = "id";
	private static final String KEY_EVENT_NAME = "event_name";
	private static final String KEY_START_TIME = "event_start";
	private static final String KEY_END_TIME = "event_end";
	private static final String KEY_STATUS = "status";

	private static int DATABASE_VERSION = 6; //accidentally upgrdaded the db a few times so just leave this at 6
	
	private static final String EVENT_TABLE_NAME = "events";
	private static final String EVENT_TABLE_CREATE =
			"CREATE TABLE " + EVENT_TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY, " 
					+ KEY_EVENT_NAME + " TEXT, " + KEY_START_TIME + " INTEGER, " + KEY_END_TIME + " INTEGER, " 
					+ KEY_STATUS + " TEXT" +  ");";

	EventTableHandler (Context context) {
		//DATABASE_VERSION += 1;
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

	// Adding new event
	public void addEvent(HCEvent hCEvent) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_EVENT_NAME, hCEvent.getName());
		values.put(KEY_START_TIME, hCEvent.getStartTime());
		values.put(KEY_END_TIME, hCEvent.getEndTime());
		values.put(KEY_STATUS, hCEvent.getStatus());

		db.insert(EVENT_TABLE_NAME, null, values);
		db.close();

	}

	// Getting All Events
	public List<HCEvent> getAllEvents() {
		List<HCEvent> eventsList = new ArrayList<HCEvent>();

		String selectQuery = "SELECT * FROM " + EVENT_TABLE_NAME;// + " WHERE " + KEY_START_TIME + " >= ?";
		//String[] selectionArgs = {Calendar.getInstance().toString()};

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if (cursor.moveToFirst()) {
			do {
				HCEvent hCEvent = new HCEvent();
				try {
					hCEvent.setId(Integer.parseInt(cursor.getString(0)));
					hCEvent.setName(cursor.getString(1));
					hCEvent.setStartTime(cursor.getLong(2));
					hCEvent.setEndTime(cursor.getLong(3));
					hCEvent.setStatus(cursor.getString(4));
				} catch (Exception e) {
					e.printStackTrace();
				}
				eventsList.add(hCEvent);
			} while(cursor.moveToNext());
		}
		return eventsList;
	}

	// Getting events Count
	public int getEventsCount() {
		String countQuery = "SELECT * FROM " + EVENT_TABLE_NAME;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();
		return cursor.getCount();
	}

	// Updating single event
	public int updateEvent(HCEvent hCEvent) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		
		Long start_time = hCEvent.getStartTime();
		Long end_time = hCEvent.getEndTime();

		if (hCEvent.getStartTime() != 0 && hCEvent.getEndTime() != 0) {
			values.put(KEY_START_TIME, start_time);
			values.put(KEY_END_TIME, end_time);
		}
		values.put(KEY_EVENT_NAME, hCEvent.getName());
		values.put(KEY_STATUS, hCEvent.getStatus());

		return db.update(EVENT_TABLE_NAME, values, KEY_EVENT_NAME + " = ?" /* AND " + KEY_START_TIME + " = ?"*/, 
				new String[] { hCEvent.getName()/*, event.getStartTime().toString()*/ });
	}

	// Deleting single event
	public void deleteEvent(HCEvent hCEvent) {
		SQLiteDatabase db = this.getWritableDatabase();

		db.delete(EVENT_TABLE_NAME, KEY_ID + "=?", 
				new String[] { String.valueOf(hCEvent.getId()) });
		db.close();
	}


}
