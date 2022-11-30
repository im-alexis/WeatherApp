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
    private Switch autoSwitch;
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


    //https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m,precipitation,weathercode&temperature_unit=fahrenheit
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dateTimeDisplay = (TextView) findViewById(R.id.Date);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
        SimpleDateFormat temp = new SimpleDateFormat("EEE");
        date = dateFormat.format(calendar.getTime());
        String dayTemp = temp.format(calendar.getTime());
        dateTimeDisplay.setText(date);
        TextView lat = (TextView) findViewById(R.id.latView);
        TextView lon = (TextView) findViewById(R.id.lonView);
        lat.setText("Latitude: " + lastLatitude );
        lon.setText("Longitude: " + lastLongitude );
        int start = week.indexOf(dayTemp);
        for (int i = start; i < week.size(); i ++){

        }


    }
}