package com.example.android.checkin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Locale;

// Receiver listens for the broadcast of "USER_PRESENT" by the system

public class ActionReceiver extends BroadcastReceiver {


    private static final String LOG_TAG = ActionReceiver.class.getSimpleName();


    public ActionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Access the settings to store time
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        //get current time
        Calendar mTimeReport = Calendar.getInstance();


        long timeInMillis = System.currentTimeMillis();

        mTimeReport.setTimeInMillis(timeInMillis);
        String timeUsed = mTimeReport.get(Calendar.HOUR_OF_DAY) + ":";
        if (mTimeReport.get(Calendar.MINUTE) == 0){
            timeUsed += "00";
        } else if (mTimeReport.get(Calendar.MINUTE) < 10) {
            timeUsed += "0" + mTimeReport.get(Calendar.MINUTE);
        } else {
            timeUsed += mTimeReport.get(Calendar.MINUTE);
        }

        timeUsed += " on " + mTimeReport.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + ", "
                + mTimeReport.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) +
                " " + (mTimeReport.get(Calendar.DAY_OF_MONTH));


        //update preferences with current time (as time-last-used)

        editor.putString(context.getString(R.string.last_used_time), timeUsed);

        //TESTING: wait, why is this here?
//        editor.putLong(context.getString(R.string.last_used_key), timeInMillis);
        editor.apply();

/*
        //I think the only reason for this is to set the alarm, which is no longer necessary
        //if this is the first use of the day, start the service
        if (!usedToday) {

            //start the service with "first-use-today" message
            Intent newServiceIntent = new Intent(context, CheckInService.class);
            newServiceIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.first_use_today));
            context.startService(newServiceIntent);
        }
        */

    }
}
