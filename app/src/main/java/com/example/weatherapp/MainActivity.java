package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private String tempUnits = "fahrenheit";
    private TimeZone tz;
    private double lastLongitude = -95.36; // default coordinates is Houston
    private double lastLatitude = 29.76;
    private double defaultLongitude = -95.36;
    private double defaultLatitude = 29.76;
    private double autoLongitude = -95.36;
    private double autoLatitude = 29.76;
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
    private DecimalFormat dr = new DecimalFormat ("#.##");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        setDates();
        setCords(lastLongitude,lastLatitude);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Switch autoSwitch = findViewById(R.id.autoLocation);
        Switch unitSwitch = findViewById(R.id.unitsForTemp);
        Button refreshButton = findViewById(R.id.refreshButton);
        workWithJson ();


        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Controls", "Refresh button has been tapped");
                EditText lonText = findViewById(R.id.longitudeInput);
                EditText latText = findViewById(R.id.latitudeInput);
                String lon = lonText.getText().toString().trim();
                Log.i("Test", "Longitude val:" + lon);
                String lat =latText.getText().toString().trim();
                Log.i("Test", "Latitude val:" + lat);
                if(lon.equals("") && !lat.equals("")){
                    lonText.setText("Latitude cannot be empty");
                }
                else if(!lon.equals("") && lat.equals("")){
                    latText.setText("Longitude cannot be empty");
                }
                else if(!lon.equals("") && !lat.equals("")){
                    lastLatitude = defaultLatitude = Double.valueOf(lat);
                    lastLongitude = defaultLongitude = Double.valueOf(lon);
                    setCords(lastLongitude,lastLatitude);
                    workWithJson ();
                }
                else {
                workWithJson ();
                }

            }
        });

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
                    Log.d("Controls", "Setting to auto location");
                    latitude.setVisibility(View.INVISIBLE);
                    longitude.setVisibility(View.INVISIBLE);
                    lastLatitude = autoLatitude;
                    lastLongitude = autoLongitude;
                    setCords(lastLongitude,lastLatitude);
                    workWithJson();


                } else {
                    Log.d("Controls", "Setting to default Location");
                    latitude.setVisibility(View.VISIBLE);
                    longitude.setVisibility(View.VISIBLE);
                    lastLatitude = defaultLatitude;
                    lastLongitude = defaultLongitude;
                    setCords(lastLongitude,lastLatitude);
                    workWithJson();


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
                    workWithJson();

                } else {
                    tempUnits = "fahrenheit";
                    Log.d("Switch", "Units now in Fahrenheit");
                    workWithJson();

                }
            }
        });

    }
    private void setDates (){
        Log.d("App", "Setting the Date");
        TextView dateTimeDisplay = (TextView) findViewById(R.id.Date);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
        SimpleDateFormat temp = new SimpleDateFormat("EEE");
        String date = dateFormat.format(calendar.getTime());
        String dayTemp = temp.format(calendar.getTime());
        Log.d("Test", dayTemp);
        dateTimeDisplay.setText(date);
        setWeek(dayTemp);
    }

    private void setCords (Double longitudeNum, Double latitudeNum){
        Log.d("App", "Setting Coordionates");
        TextView longitude = findViewById(R.id.lonView);
        TextView latitude = findViewById(R.id.latView);
        longitude.setText("Longitude: " + dr.format(longitudeNum));
        latitude.setText("Latitude: " + dr.format(latitudeNum));

    }

    private void setWeek (String currentDay){ //Ape man funtion to set the week
        Log.i("App","Setting the forcast table");
        int i = week.indexOf(currentDay);
        TextView tempDay = findViewById(R.id.day1);
        String holdData = tempDay.getText().toString().substring(3);

        if(i !=  week.size() - 1){
            tempDay.setText(week.get(i+1) + holdData);
            i++;
        }
        else{
            i = 0;
            tempDay.setText(week.get(i) + holdData);
        }
       tempDay = findViewById(R.id.day2);
         holdData = tempDay.getText().toString().substring(3);

        if(i !=  week.size() - 1){
            tempDay.setText(week.get(i+1) + holdData);
            i++;
        }
        else{
            i = 0;
            tempDay.setText(week.get(i) + holdData);
        }
        tempDay = findViewById(R.id.day3);
        holdData = tempDay.getText().toString().substring(3);

        if(i !=  week.size() - 1){
            tempDay.setText(week.get(i+1) + holdData);
            i++;
        }
        else{
            i = 0;
            tempDay.setText(week.get(i) + holdData);
        }
        tempDay = findViewById(R.id.day4);
        holdData = tempDay.getText().toString().substring(3);

        if(i !=  week.size() - 1){
            tempDay.setText(week.get(i+1) + holdData);
            i++;
        }
        else{
            i = 0;
            tempDay.setText(week.get(i) + holdData);
        }
        tempDay = findViewById(R.id.day5);
        holdData = tempDay.getText().toString().substring(3);

        if(i != week.size() - 1){
            tempDay.setText(week.get(i+1) + holdData);
            i++;
        }
        else{
            i = 0;
            tempDay.setText(week.get(i) + holdData);
        }
        tempDay = findViewById(R.id.day6);
        holdData = tempDay.getText().toString().substring(3);

        if(i != week.size() - 1){
            tempDay.setText(week.get(i+1) + holdData);
            i++;
        }
        else{
            i = 0;
            tempDay.setText(week.get(i) + holdData);
        }

    }
    //https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m,relativehumidity_2m,weathercode,visibility&temperature_unit=fahrenheit&timezone=America%2FChicago
    private void workWithJson (){
        Log.d("App", "Calling API");
        String url = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m,relativehumidity_2m,weathercode,visibility&temperature_unit=fahrenheit&timezone=America%2FChicago";
        StringRequest apiCall = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("test",response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.toString().trim(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    private void setTemp (){

    }
    private void setWeatherCode (){

    }
    private void currentConditions (){

    }

}