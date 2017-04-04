package com.example.android.checkin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

// Receiver listens for the broadcast of "USER_PRESENT" by the system

public class ActionReceiver extends BroadcastReceiver {


    private static final String LOG_TAG = ActionReceiver.class.getSimpleName();


    public ActionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Access the settings to find out if this is the first time today the broadcast was received
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //Has the user been present today?
        boolean usedToday = sharedPreferences.getBoolean("user_present_today", false);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //get current time
        Calendar mTimeReport = Calendar.getInstance();
        mTimeReport.setTimeInMillis(System.currentTimeMillis());
        String timeUsed = mTimeReport.get(Calendar.HOUR_OF_DAY) + ":" + mTimeReport.get(Calendar.MINUTE);

        //update preferences with current time (as time-last-used)
        editor.putString(context.getString(R.string.last_used_time), timeUsed);
        editor.apply();


        //if this is the first use of the day, start the service
        if (!usedToday) {

            //start the service with "first-use-today" message
            Intent newServiceIntent = new Intent(context, CheckInService.class);
            newServiceIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.first_use_today));
            context.startService(newServiceIntent);
        }

    }
}
