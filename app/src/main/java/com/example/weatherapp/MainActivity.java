package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private TextView dateTimeDisplay;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;
    private String tempUnits = "fahrenheit";
    private double lastLongitude = -95.36; // default coordinates is Houston
    private double lastLatitude = 29.76;
    private double defaultLongitude = -95.36;
    private double defaultLatitude = 29.76;
    private double autoLongitude = -95.36;
    private double autoLatitude = 29.76;
    private Switch autoSwitch;
    private Switch unitSwitch;
    ArrayList<String> week = new ArrayList<String>() {
        {
            add("Mon");
            add("Tue");
            add("Wed");
            add("Thu");
            add("Fri");
            add("Sat");
            add("Sun");
        }
    };
    private FusedLocationProviderClient fusedLocationClient;


    //https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m,precipitation,weathercode&temperature_unit=fahrenheit
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        setDates();

        TextView lat = findViewById(R.id.latView);
        TextView lon = findViewById(R.id.lonView);
        lat.setText("Latitude: " + lastLatitude);
        lon.setText("Longitude: " + lastLongitude);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        autoSwitch = findViewById(R.id.autoLocation);
        unitSwitch = findViewById(R.id.unitsForTemp);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                           autoLatitude = location.getLongitude();
                           autoLatitude = location.getLatitude();
                        }
                        Log.d("Response", "No Location there");
                    }
                });
        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                EditText longitude = findViewById(R.id.longitudeInput);
                EditText latitude = findViewById(R.id.latitudeInput);
                if (isChecked) {
                    Log.d("Switch", "Setting to auto location");
                    latitude.setVisibility(View.INVISIBLE);
                    longitude.setVisibility(View.INVISIBLE);
                    lastLatitude = autoLatitude;
                    lastLongitude = autoLongitude;
                    lat.setText("Latitude: " + autoLatitude);
                    lon.setText("Longitude: " + autoLongitude);



                } else {

                    latitude.setVisibility(View.VISIBLE);
                    longitude.setVisibility(View.VISIBLE);
                    lastLatitude = defaultLatitude;
                    lastLongitude = defaultLongitude;
                    lat.setText("Latitude: " + defaultLatitude);
                    lon.setText("Longitude: " + defaultLongitude);
                    Log.d("Switch", "Setting to default Location");

                }
            }
        });
        unitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // on below line we are checking
                // if switch is checked or not.

                if (isChecked) {
                    tempUnits = "celsius";
                    Log.d("Switch", "Units now in Celsius");

                } else {
                    tempUnits = "fahrenheit";
                    Log.d("Switch", "Units now in Fahrenheit");

                }
            }
        });

    }
    private void setDates (){
        Log.d("Build", "Setting the Date");
        dateTimeDisplay = (TextView) findViewById(R.id.Date);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
        SimpleDateFormat temp = new SimpleDateFormat("EEE");
        date = dateFormat.format(calendar.getTime());
        String dayTemp = temp.format(calendar.getTime());
        dateTimeDisplay.setText(date);
    }

    private void setCords (Double longitude, Double latitude){

    }


}