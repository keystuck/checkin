package com.example.android.checkin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import static com.example.android.checkin.R.string.reset;

public class CheckInService extends Service {

    //number to which to send noon update
//    private final static String CONTACT_PHONE_NUMBER = "6178998974";
    private String mContactNum;


    private final static String ALARM_WAKE_UP_START = "AlarmWakeUpStart";
    private final static String ALARM_WAKE_UP_END = "AlarmWakeUpEnd";




    //TESTING
    private int START_TIME = 6;
    private int END_TIME = 12;


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

                Toast.makeText(this, "Make sure to set contact phone number in settings!", Toast.LENGTH_LONG).show();

                //TESTING: send a message as soon as installed to check time
                mTimeReport = Calendar.getInstance();
                mTimeReport.setTimeInMillis(System.currentTimeMillis());
                String timeUsed = mTimeReport.get(Calendar.HOUR_OF_DAY) + ":" + mTimeReport.get(Calendar.MINUTE);

                int hour = mTimeReport.get(Calendar.HOUR_OF_DAY);

                Log.v(LOG_TAG, "first run and hour is " + hour);

                //change "user-present" to true because installing it implies using device
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.user_present_today), true);

                //put current time in "last-time-used" field
                editor.putString(getString(R.string.last_used_time), timeUsed);



                //If we are after end time or before start time
                if (hour >= END_TIME || hour < START_TIME){

                    String resetTime = START_TIME + ":00";
                    if (hour >= END_TIME){
                        resetTime = resetTime + " tomorrow";
                    }
                    Log.v(LOG_TAG, "Next alarm should be a reset at " + resetTime);

                    //update the Preferences with the type and time of the next alarm
                    editor.putString(getString(R.string.next_type), getString(reset));
                    editor.putString(getString(R.string.next_time), resetTime);
                    editor.apply();

                    //set the alarm for tomorrow's start time
                    setAlarm(START_TIME, true);
                }
                //otherwise we're between times and should start with an end alarm
                else if (hour >=  START_TIME){

                    String updateTime = END_TIME + ":00";
                    Log.v(LOG_TAG, "Next alarm should be an update at " + updateTime);

                    //update Preferences with type andtime of next alarm
                    editor.putString(getString(R.string.next_type), getString(R.string.update));
                    editor.putString(getString(R.string.next_time), updateTime);
                    editor.apply();

                    //set the alarm for the next end time
                    setAlarm(END_TIME, false);

                }


            }

            //if you've gotten the "first use" message
            else if (intent.getStringExtra(Intent.EXTRA_TEXT).equals(getString(R.string.first_use_today))) {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                //update the present_today flag to true
                editor.putBoolean(getString(R.string.user_present_today), true);

                //calculate and update the "time-last-used" to the current time
                mTimeReport = Calendar.getInstance();
                mTimeReport.setTimeInMillis(System.currentTimeMillis());
                String timeUsed = mTimeReport.get(Calendar.HOUR_OF_DAY) + ":" + mTimeReport.get(Calendar.MINUTE);
                editor.putString(getString(R.string.last_used_time), timeUsed);
                editor.apply();


            }


            else if (intent.getStringExtra(Intent.EXTRA_TEXT).equals(ALARM_WAKE_UP_START)) {

                SharedPreferences.Editor editor = sharedPreferences.edit();

                //reset user-present-flag to false and update time-used to blank
                editor.putBoolean(getString(R.string.user_present_today), false);
                editor.putString(getString(R.string.last_used_time), "");
                String updateTime = END_TIME + ":00";

                //update with next type and time of alarm
                editor.putString(getString(R.string.next_type), getString(R.string.update));
                editor.putString(getString(R.string.next_time), updateTime);
                editor.apply();

                Log.v(LOG_TAG, "Next alarm should be an update at " + updateTime);


                //set the alarm
                setAlarm(END_TIME, false);
            }

            else if (intent.getStringExtra(Intent.EXTRA_TEXT).equals(ALARM_WAKE_UP_END)) {
                boolean used_today = sharedPreferences.getBoolean(getString(R.string.user_present_today), false);
                if (!used_today) {
                    //user hasn't used phone today, send message
                    sendStatusMessage(false);
                } else {
                    sendStatusMessage(true);
                }

                //update the preferences with type and time of next alarm
                String resetTime = START_TIME + ":00";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.next_type), getString(reset));
                editor.putString(getString(R.string.next_time), resetTime);
                editor.apply();

                Log.v(LOG_TAG, "Next alarm should be an update at " + resetTime);


                //set morning alarm for next day!
                setAlarm(START_TIME, true);
            }
        }

            ///service will be restarted by alarm or receiver; don't stick
        return Service.START_NOT_STICKY;
    }


    private void setAlarm(int timeArg, boolean startTime){

         //      For actual use
        //current date and time

//        Calendar timeToWake = Calendar.getInstance();
//        timeToWake.setTimeInMillis(System.currentTimeMillis());
        Calendar timeToWake = mTimeReport;
        int hour = mTimeReport.get(Calendar.HOUR_OF_DAY);

        //if we're setting the start alarm
        if (startTime) {
            //advance day if setting for next morning, don't if for same morning
            if (hour >= END_TIME) {

                //set the time to the next day for a start alarm
                timeToWake.add(Calendar.DAY_OF_YEAR, 1);
                timeToWake.set(Calendar.HOUR_OF_DAY, timeArg);
                timeToWake.set(Calendar.MINUTE, 0);
            }
            //if we're starting before the start-alarm time
            else if (hour < START_TIME) {
                //set the alarm time to start_time
                timeToWake.set(Calendar.HOUR_OF_DAY, timeArg);
                timeToWake.set(Calendar.MINUTE, 0);
            }
            //otherwise we're between start and end alarm; this SHOULD NOT HAPPEN
            else {
                Toast.makeText(this, "Can't set start alarm between start and end", Toast.LENGTH_LONG).show();
            }
        }
        //otherwise we're setting the end alarm for same day
        else {
            timeToWake.set(Calendar.HOUR_OF_DAY, timeArg);
            timeToWake.set(Calendar.MINUTE, 0);
        }



        //get the millisecond value of the time to wake up
        long wakeUpTimeMillis = timeToWake.getTimeInMillis();

        /*
            //for testing use
            //current date and time
            Calendar timeToWake = Calendar.getInstance();
            //set the time to 6 AM (or, here, x seconds from now)
            timeToWake.add(Calendar.SECOND, timeArg);
            //get the millisecond value of the time to wake up

            long wakeUpTimeMillis = timeToWake.getTimeInMillis();
*/

            // set the requested alarm
            Intent intent1 = new Intent(getApplicationContext(), CheckInService.class);
            if (startTime) {
                intent1.putExtra(Intent.EXTRA_TEXT, ALARM_WAKE_UP_START);
            }
            else  {
                intent1.putExtra(Intent.EXTRA_TEXT, ALARM_WAKE_UP_END);
            }
            PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager mAlarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mAlarmMgr.set(AlarmManager.RTC_WAKEUP, wakeUpTimeMillis, pendingIntent);
        Log.v(LOG_TAG, "Setting first alarm for " + timeToWake.get(Calendar.HOUR_OF_DAY) + ": "
                + timeToWake.get(Calendar.MINUTE) + " on day " + timeToWake.get(Calendar.DAY_OF_MONTH));

    }





    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private void sendStatusMessage(boolean hasUsed){
        String message;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Log.v(LOG_TAG, "I think the time of the alarm is " +
                mTimeReport.get(Calendar.MONTH) + " " +  mTimeReport.get(Calendar.DAY_OF_MONTH)
                        + " " + mTimeReport.get(Calendar.YEAR)
                        + " " + mTimeReport.get(Calendar.HOUR_OF_DAY)
                        + " " + mTimeReport.get(Calendar.MINUTE));

        mContactNum = sharedPreferences.getString(getString(R.string.contact_num_key), "");

        if (!mContactNum.isEmpty()) {

            if (hasUsed) {
                String timeUsed = sharedPreferences.getString(getString(R.string.last_used_time), "");
                message = "Phone last used at " + timeUsed;
            } else {
                message = "Phone not used as of "
                        + " " + END_TIME;
            }

//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            try {
                //Eventually send SMS
                SmsManager smsManager = SmsManager.getDefault();
                //destination, sc
                smsManager.sendTextMessage(mContactNum, null, message, null, null);
            } catch (Exception e) {
                Toast.makeText(this, "Message not sent", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(this, "No contact number supplied", Toast.LENGTH_LONG).show();
        }



    }
}
