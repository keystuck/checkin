package com.example.android.checkin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.Manifest.permission.SEND_SMS;

//Add System Preferences item to store has/hasn't used phone today
//If woken at noon, if true set alarm for 6AM and finish
//if false send SMS to contact and self

public class CheckInActivity extends AppCompatActivity implements View.OnClickListener{

        private final static String CONTACT_PHONE_NUMBER = "6178998974";
    private final static String SELF_PHONE_NUMBER = "5403151095";

    //TESTING
//    private final static String SELF_PHONE_NUMBER = "5556";

    private final static int CHECKIN_SMS_PERMISSION_REQUEST_CODE = 1;

    private final static String ALARM_WAKE_UP = "AlarmWakeUp";


    private final static String LOG_TAG = CheckInActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        Button btn = (Button)findViewById(R.id.start_service);
        btn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.stop_service);
        btn.setOnClickListener(this);


        //if created by alarm, start self
        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT) && intent.getStringExtra(Intent.EXTRA_TEXT).equals(ALARM_WAKE_UP)){
            Log.v(LOG_TAG, "woken up by alarm");
        }


    }




    @Override
    public void onClick(View view) {
        Intent serviceIntent = new Intent(getApplicationContext(),
                CheckInService.class);
        Log.v(LOG_TAG, "starting service");
        serviceIntent.putExtra(Intent.EXTRA_TEXT, "first_run");

        if (view.getId() == R.id.start_service){

            //if the user is starting the service, double-check that the necessary permissions are provided
            if (ContextCompat.checkSelfPermission(this, SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                startService(serviceIntent);
            }
            else {
                Log.v(LOG_TAG, "User present but permission absent");
                ActivityCompat.requestPermissions(this, new String[]{SEND_SMS}, CHECKIN_SMS_PERMISSION_REQUEST_CODE);
            }

        }
        else if (view.getId() == R.id.stop_service){
            stopService(serviceIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.checkin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.checkin_settings){
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Intent serviceIntent = new Intent(getApplicationContext(),
                    CheckInService.class);

            Log.v(LOG_TAG, "starting service");
            serviceIntent.putExtra(Intent.EXTRA_TEXT,"first_run");

            startService(serviceIntent);
        }
        else {
            Toast.makeText(this, "SMS permission required to run app.", Toast.LENGTH_SHORT).show();
        }

    }
}
