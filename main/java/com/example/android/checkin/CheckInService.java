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

import java.util.Calendar;

public class CheckInService extends Service {

    //    private final static String CONTACT_PHONE_NUMBER = "6178998974";
//    private final static String SELF_PHONE_NUMBER = "5403151095";
    private final static String CONTACT_PHONE_NUMBER = "6178998974";
    private final static String SELF_PHONE_NUMBER = "5556";

    private final static int CHECKIN_SMS_PERMISSION_REQUEST_CODE = 1;

    private final static String ALARM_WAKE_UP_START = "AlarmWakeUpStart";
    private final static String ALARM_WAKE_UP_END = "AlarmWakeUpEnd";


//    private int START_TIME = 6;
 //   private int END_TIME = 12;

    private long START_TIME;
    private long END_TIME;

    private final static String LOG_TAG = CheckInService.class.getSimpleName();

    ActionReceiver mActionReceiver;

    public CheckInService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            Log.v(LOG_TAG, "Start command received with " + intent.getStringExtra(Intent.EXTRA_TEXT));

            //if this is the first run...
            if (intent.getStringExtra(Intent.EXTRA_TEXT).equals("first_run")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("user_present_today", true);
                editor.apply();

                //testing purposes - fix for use


                START_TIME = setAlarm(15, true);
                Log.v(LOG_TAG, "Setting start alarm for 15 seconds from now");
                //               END_TIME = setAlarm(30, false);
                //           Log.v(LOG_TAG, "Setting stop alarm for 30 seconds from now");
                Log.v(LOG_TAG, "first run");

            }

            //if you've gotten the "first use" message
            else if (intent.getStringExtra(Intent.EXTRA_TEXT).equals("first_use_of_day")) {
                sendStatusMessage(true);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Log.v(LOG_TAG, "before first use " + sharedPreferences.getBoolean("user_present_today", false));
                editor.putBoolean("user_present_today", true);
                editor.apply();

                Log.v(LOG_TAG, "first use today, so now " + sharedPreferences.getBoolean("user_present_today", false));

            }

            //if you've gotten the "12:00 and no activity" message
/*        else if (intent.hasExtra("no_use_by_end_time")){
            if (intent.getBooleanExtra("no_use_by_end_time", true)){
                sendStatusMessage(false);
                Log.v(LOG_TAG, "no user by end time");
            }

        }
*/
            else if (intent.getStringExtra(Intent.EXTRA_TEXT).equals(ALARM_WAKE_UP_START)) {
                //get current date and time
                Calendar time = Calendar.getInstance();
                Log.v(LOG_TAG, "woke up to alarm at " + time.getTime());

                // TODO: err... figure out how to adapt this for testing
//            if (time.HOUR_OF_DAY == START_TIME) {
//            if (Math.abs(System.currentTimeMillis() - START_TIME) < 1000) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Log.v(LOG_TAG, "user present before change: " + sharedPreferences.getBoolean("user_present_today", false));
                editor.putBoolean("user_present_today", false);
                editor.apply();
                Log.v(LOG_TAG, "user present after change: " + sharedPreferences.getBoolean("user_present_today", false));
                //if it's morning, set the next morning wakeup
//                setAlarm(START_TIME);
                END_TIME = setAlarm(30, false);
                Log.v(LOG_TAG, "setting end alarm for 30 seconds from now");
//            } else if (time.HOUR_OF_DAY == END_TIME) {
            } else if (intent.getStringExtra(Intent.EXTRA_TEXT).equals(ALARM_WAKE_UP_END)) {
                boolean used_today = sharedPreferences.getBoolean("user_present_today", false);
                Log.v(LOG_TAG, "used today ? " + used_today);
                if (!used_today) {
                    //user hasn't used phone today, send message
                    sendStatusMessage(false);
                }
                //if it's noon, set the next noon wakeup
                //setAlarm(END_TIME);

                START_TIME = setAlarm(15, true);
                Log.v(LOG_TAG, "Setting start alarm for 15 seconds from now");
            }
        }

            ///service will be restarted by alarm or receiver; don't stick
        return Service.START_NOT_STICKY;
    }

    //the return type is long for testing. change to void for use
    private long setAlarm(int timeArg, boolean startTime){
        Log.v(LOG_TAG, "received alarm command for " + timeArg + " for a start command? " + startTime);
        //for actual use, "time" will be an hour for the wakeup call
        //for testing use, "time" is a number of seconds from now


         /*      For actual use
        //current date and time

        Calendar timeToWake = Calendar.getInstance();
        //set the time to the next day
        timeToWake.add(Calendar.DAY_OF_YEAR, 1);
        //set the time to start_time
        timeToWake.set(Calendar.HOUR_OF_DAY, time);
        timeToWake.set(Calendar.MINUTE, 0);
        //get the millisecond value of the time to wake up
        long wakeUpTimeMillis = timeToWake.getTimeInMillis();

        */
                //TODO: FIX THIS TESTING ISSUE
            //for testing use
            //current date and time
            Calendar timeToWake = Calendar.getInstance();
            //set the time to 6 AM (or, here, x seconds from now)
            timeToWake.add(Calendar.SECOND, timeArg);
            //get the millisecond value of the time to wake up

            long wakeUpTimeMillis = timeToWake.getTimeInMillis();


            // set an alarm for the next morning and go to sleep
            Intent intent1 = new Intent(getApplicationContext(), CheckInService.class);
            if (startTime) {
                intent1.putExtra(Intent.EXTRA_TEXT, ALARM_WAKE_UP_START);
                Log.v(LOG_TAG, "Setting start alarm");
            }
            else  {
                intent1.putExtra(Intent.EXTRA_TEXT, ALARM_WAKE_UP_END);
                Log.v(LOG_TAG, "setting end alarm");
            }
            PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.v(LOG_TAG, "sending with command? " + intent1.getStringExtra(Intent.EXTRA_TEXT));
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, wakeUpTimeMillis , pendingIntent);
        Log.v(LOG_TAG, "Have set alarm");
        return wakeUpTimeMillis;

    }





    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private void sendStatusMessage(boolean hasUsed){
        String message;
        if (hasUsed){
            message = "Phone used at " + System.currentTimeMillis();
        }
        else {
            message = "Phone not used by noon";
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

/*        //Eventually send SMS
        SmsManager smsManager = SmsManager.getDefault();
        //destination, sc
        smsManager.sendTextMessage(CONTACT_PHONE_NUMBER, null, message, null, null);

*/


    }
}
