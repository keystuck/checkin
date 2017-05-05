package com.example.android.checkin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import android.telephony.SmsManager;

import java.util.Calendar;
import java.util.Locale;

public class CheckInService extends Service {

    //number to which to send noon update
//    private final static String CONTACT_PHONE_NUMBER = "6178998974";
    private String mContactNum;


    private final static String ALARM_WAKE_UP = "AlarmWakeUp";
    private final static int MESSAGE_SEND_TIME = 12;
    private final static int MESSAGE_SEND_TIME_MINUTES = 00;
    //time interval for recurring - right now five minutes, eventually one day
    private final static long INTERVAL_TO_RECUR = AlarmManager.INTERVAL_DAY;




    //field to store last time used
    private Calendar mTimeReport;

    private final static String LOG_TAG = CheckInService.class.getSimpleName();

    //empty constructor
    public CheckInService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //get preferences - will be used later
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        if (intent.hasExtra(Intent.EXTRA_TEXT)) {

            //if this is the first run after installation
            if (intent.getStringExtra(Intent.EXTRA_TEXT).equals(getString(R.string.first_run))) {

                //if user hasn't entered a contact phone number, remind them
                if (sharedPreferences.getString(getString(R.string.contact_num_key), "").isEmpty()) {
                    Toast.makeText(this, "Make sure to set contact phone number in settings!", Toast.LENGTH_LONG).show();
                }

                // Get current time
                mTimeReport = Calendar.getInstance();
                mTimeReport.setTimeInMillis(System.currentTimeMillis());

                //Calculate time to set recurring alarm
                Calendar timeToWake = Calendar.getInstance();
                timeToWake.setTimeInMillis(System.currentTimeMillis());

                int hour = mTimeReport.get(Calendar.HOUR_OF_DAY);
                int min = mTimeReport.get(Calendar.MINUTE);

                if (hour > MESSAGE_SEND_TIME || (hour == MESSAGE_SEND_TIME && min >= MESSAGE_SEND_TIME_MINUTES))  {

                    //set the time to the next day for a start alarm
                    timeToWake.add(Calendar.DAY_OF_YEAR, 1);
                    timeToWake.set(Calendar.HOUR_OF_DAY, MESSAGE_SEND_TIME);
                    timeToWake.set(Calendar.MINUTE, MESSAGE_SEND_TIME_MINUTES);
                }

                else {
                    //if we're starting same day before the start-alarm time
                    //set the alarm time to start_time
                    timeToWake.set(Calendar.HOUR_OF_DAY, MESSAGE_SEND_TIME);
                    timeToWake.set(Calendar.MINUTE, MESSAGE_SEND_TIME_MINUTES);
                }




                long wakeUpTimeMillis = timeToWake.getTimeInMillis();

                //Make a string of "_:__" of time last used
                String timeUsed = mTimeReport.get(Calendar.HOUR_OF_DAY) + ":";
                String timeForMessage = timeToWake.get(Calendar.HOUR_OF_DAY) + ":"
                        + timeToWake.get(Calendar.MINUTE)
                        + " on "
                        + timeToWake.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + ", "
                        + timeToWake.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
                        + " " + timeToWake.get(Calendar.DAY_OF_MONTH);


                Log.d(LOG_TAG, "wakeup set to " + timeForMessage);

                if (min == 0) {
                    timeUsed += "00";
                } else if (min < 10) {
                    timeUsed += "0" + min;
                } else {
                    timeUsed += min;
                }
                timeUsed += " on " + mTimeReport.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + ", "
                        + mTimeReport.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) +
                        " " + (mTimeReport.get(Calendar.DAY_OF_MONTH));



                SharedPreferences.Editor editor = sharedPreferences.edit();

                //put current time in "last-time-used" field
                editor.putString(getString(R.string.last_used_time), timeUsed);
                editor.putString(getString(R.string.next_time), timeForMessage);
                editor.apply();

                // set the repeating alarm
            Intent intent1 = new Intent(getApplicationContext(), CheckInService.class);
                intent1.putExtra(Intent.EXTRA_TEXT, ALARM_WAKE_UP);

            PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager mAlarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, wakeUpTimeMillis, INTERVAL_TO_RECUR, pendingIntent);

        Log.d(LOG_TAG, "Setting first alarm for " + timeToWake.get(Calendar.HOUR_OF_DAY) + ": "
                + timeToWake.get(Calendar.MINUTE) + " on day " + timeToWake.get(Calendar.DAY_OF_MONTH));


            }

            else if (intent.getStringExtra(Intent.EXTRA_TEXT).equals(ALARM_WAKE_UP)) {

                    sendStatusMessage(true);

            }
        }

            ///service will be restarted by alarm or receiver; don't stick
        return Service.START_NOT_STICKY;
    }








    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private void sendStatusMessage(boolean hasUsed){

        //field to hold message to send to the contact
        String message;

        //get preferences handle for getting details
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        //get the number to which to send the alert -- if none, quit
        mContactNum = sharedPreferences.getString(getString(R.string.contact_num_key), "");

        if (!mContactNum.isEmpty()) {

            if (hasUsed) {
                String timeUsed = sharedPreferences.getString(getString(R.string.last_used_time), "");
                message = "Phone last used at " + timeUsed;
            } else {
                message = "Phone not used as of "
                        + " " + MESSAGE_SEND_TIME + ":00";
            }

//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(mContactNum, null, message, null, null);
                Log.d(LOG_TAG, message);

            } catch (Exception e) {
                Toast.makeText(this, "Message not sent", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        //no contact number -can't send
        //TODO: make it so if the user enters a number, it tries again
        else {
            Toast.makeText(this, "No contact number supplied", Toast.LENGTH_LONG).show();

        }



    }

}
