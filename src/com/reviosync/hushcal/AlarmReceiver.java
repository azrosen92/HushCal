package com.reviosync.hushcal;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//Toast.makeText(context, "changing status", Toast.LENGTH_SHORT).show();
		ContextWrapper c = new ContextWrapper(context);
		AudioManager am = (AudioManager) c.getBaseContext().getSystemService(Context.AUDIO_SERVICE);
		
		Calendar set_time = Calendar.getInstance();
		int current_ringer_mode = am.getRingerMode();
		String current_status = "";
		
		switch (current_ringer_mode) {
			case AudioManager.RINGER_MODE_VIBRATE: current_status = "vibrate";
			break;
			
			case AudioManager.RINGER_MODE_SILENT: current_status = "silence";
			break;
			
			case AudioManager.RINGER_MODE_NORMAL: current_status = "sound";
			break;
			
			default: current_status = "silence";
			break;
		}
		
		try {
			Bundle bundle = intent.getExtras();
			String status = bundle.getString("status");
			set_time.setTimeInMillis(bundle.getLong("time"));
			if (status.equals("vibrate")) {
				//turn phone on vibrate
				am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				if (bundle.getLong("time") != 0L) {
					//set unscheduler
					EventScheduler.unschedule(context.getApplicationContext(), 
							set_time, 
							current_status);
				}
			} else if (status.equals("silence")) {
				//turn phone on silence
				am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				if (bundle.getLong("time") != 0L) {
					//set unscheduler
					EventScheduler.unschedule(context.getApplicationContext(), 
							set_time, 
							current_status);
				}
			} else if (status.equals("sound")) {
				//turn on sound
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			}
			
		} catch(Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

}
