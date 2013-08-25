package com.reviosync.hushcal;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class EventScheduler {

	public static void schedule(Context ctx, HCEvent hCEvent) {
		//schedule method is being called, but the alarm manager is not being set for some reason
		long start = hCEvent.getStartTime();
		long end = hCEvent.getEndTime();
		String status = hCEvent.getStatus();

		Intent intent = new Intent(ctx, AlarmReceiver.class);
		intent.putExtra("time", end);
		intent.putExtra("status", status);
		
		PendingIntent sender = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, start, sender);
	}
	
	public static void unschedule(Context ctx, Calendar time, String status) {
		Intent intent = new Intent(ctx, AlarmReceiver.class);
		intent.putExtra("status", status);
		
		PendingIntent sender = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), sender);		
	}
}
