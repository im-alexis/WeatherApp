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
import android.util.TypedValue;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private String tempUnits = "fahrenheit";
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
    private DecimalFormat tdr = new DecimalFormat ("#.#");
    private String deviceTimeZone;



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
        TimeZone tz = TimeZone.getDefault();
        deviceTimeZone = tz.getDisplayName(false,TimeZone.SHORT_COMMONLY_USED).trim();
        Log.d("Test", deviceTimeZone);
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
                    Log.d ("TEST", String.valueOf(lastLatitude));
                    Log.d ("TEST", String.valueOf(lastLongitude));
                    setCords(lastLongitude,lastLatitude);
                    workWithJson ();
                }
                else {
                workWithJson ();
                }

            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE}, PackageManager.PERMISSION_GRANTED);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                           autoLatitude = location.getLongitude();
                           autoLatitude = location.getLatitude();
                        }
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
        String url = "https://api.open-meteo.com/v1/forecast?latitude="+lastLatitude+"&longitude="+ lastLongitude +"&hourly=temperature_2m,relativehumidity_2m,weathercode,visibility&temperature_unit="+tempUnits   +"&timezone="+deviceTimeZone;
        StringRequest apiCall = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                String output = "";
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONObject elementsJSONArray = jsonResponse.getJSONObject("hourly");
                    JSONArray weatherArray = elementsJSONArray.getJSONArray("temperature_2m");
                    JSONArray humidityArray = elementsJSONArray.getJSONArray("relativehumidity_2m");
                    JSONArray weatherCodeArray = elementsJSONArray.getJSONArray("weathercode");
                    setTemp(weatherArray, humidityArray);
                    currentConditions(weatherCodeArray, weatherArray,humidityArray);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.toString().trim(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(apiCall);
    }

    private void setTemp (JSONArray weather, JSONArray humidity) throws JSONException {
        Log.d("App", "Setting forecast values");

        TextView tempDay = findViewById(R.id.day1);
        String holdData = tempDay.getText().toString().substring(0,4);
        double tempValue = 0;
        int hummy = 0;
        for (int i = 24; i < 48; i++){
            tempValue = tempValue + weather.getDouble(i);;
            hummy = hummy + humidity.getInt(i);
        }
        hummy = hummy/24;
        tempValue = Double.parseDouble(tdr.format(tempValue/24));
        tempDay.setText(holdData + (int)tempValue+ "°"+"\nHumidity: " + hummy +"%");

        tempDay = findViewById(R.id.day2);
        holdData = tempDay.getText().toString().substring(0,4);
        tempValue = 0;
        for (int i = 48; i < 72; i++){
            tempValue = tempValue + weather.getDouble(i);
            hummy = hummy + humidity.getInt(i);
        }
        hummy = hummy/24;
        tempValue = Double.parseDouble(tdr.format(tempValue/24));
        tempDay.setText(holdData + (int)tempValue+ "°"+"\nHumidity: " + hummy +"%");

        tempDay = findViewById(R.id.day3);
        holdData = tempDay.getText().toString().substring(0,4);
        tempValue = 0;
        for (int i = 72; i < 96; i++){
            tempValue = tempValue + weather.getDouble(i);
            hummy = hummy + humidity.getInt(i);
        }
        hummy = hummy/24;
        tempValue = Double.parseDouble(tdr.format(tempValue/24));
        tempDay.setText(holdData + (int)tempValue+ "°"+"\nHumidity: " + hummy +"%");

        tempDay = findViewById(R.id.day4);
        holdData = tempDay.getText().toString().substring(0,4);
        tempValue = 0;
        for (int i = 96; i < 120; i++){
            tempValue = tempValue + weather.getDouble(i);
            hummy = hummy + humidity.getInt(i);
        }
        hummy = hummy/24;
        tempValue = Double.parseDouble(tdr.format(tempValue/24));
        tempDay.setText(holdData + (int)tempValue+ "°"+"\nHumidity: " + hummy +"%");

        tempDay = findViewById(R.id.day5);
        holdData = tempDay.getText().toString().substring(0,4);
        tempValue = 0;
        for (int i = 120; i < 144; i++){
            tempValue = tempValue + weather.getDouble(i);
            hummy = hummy + humidity.getInt(i);
        }
        hummy = hummy/24;
        tempValue = Double.parseDouble(tdr.format(tempValue/24));
        tempDay.setText(holdData +(int)tempValue+ "°"+"\nHumidity: " + hummy +"%");

        tempDay = findViewById(R.id.day6);
        holdData = tempDay.getText().toString().substring(0,4);
        tempValue = 0;
        for (int i = 144; i < 168; i++){
            tempValue = tempValue + weather.getDouble(i);
            hummy = hummy + humidity.getInt(i);
        }
        hummy = hummy/24;
        tempValue = Double.parseDouble(tdr.format(tempValue/24));
        tempDay.setText(holdData + (int)tempValue+ "°"+"\nHumidity: " + hummy +"%");
    }

    private void currentConditions (JSONArray weatherCode, JSONArray temo, JSONArray humidity) throws JSONException {
        Log.d("App", "Setting the current weather");
        String currentTime = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
        Integer index = Integer.parseInt(currentTime);
        Log.d("Test", currentTime);
        TextView bigWeather = findViewById(R.id.temperature);
        double measure = temo.getDouble(index);
        Log.d("test", String.valueOf(measure));
        bigWeather.setText((int) measure +"°");
        TextView humidityText = findViewById(R.id.humidityText);
        int hum = humidity.getInt(index);
        humidityText.setText("Humidity: " + hum + "%");
        TextView codeText = findViewById(R.id.weatherWord);
        codeText.setText(weatherCodeWord(weatherCode.getInt(index), codeText));
    }

    private String weatherCodeWord (int code , TextView codeText){

        codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        String word = "";
        if(code == 0){
            word = "Clear sky";
        } else if(code == 1){
            word = "Mainly Clear";
        } else if(code == 2){
            word = "Partly Cloudy";
        }else if(code == 3){
            word = "Overcast";
        }else if(code == 45){
            word = "Fog";
        }else if(code == 48){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Depositing Rime Fog";
        }else if(code == 51){
            word = "Light Drizzle";
        }else if(code == 53){
            word = "Moderate Drizzle";
        }else if(code == 55){
            word = "Dense Drizzle";
        }else if(code == 56){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Freezing Light Drizzle";
        }else if(code == 57){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Freezing Dense Drizzle";
        }else if(code == 61){
            word = "light Rain";
        }else if(code == 63){
            word = "Moderate Rain";
        }else if(code == 65){
            word = "Heavy Rain";
        }else if(code == 66){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Freezing Light Rain";
        }else if(code == 67){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Freezing Heavy Rain";
        }else if(code == 71){
            word = "Light Snow";
        }else if(code == 73){
            word = "Moderate Rain";
        }else if(code == 75){
            word = "Heavy Snow";
        }else if(code == 77){
            word = "Snow Grains";
        }else if(code == 80){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Light Rain Shower";
        }else if(code == 81){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Moderate Rain Shower";
        }else if(code == 82){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Violent Rain Shower";
        }else if(code == 85){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Light Snow Shower";
        }else if(code == 86){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Heavy Snow Shower";
        }else if(code == 95){

            word = "Thunderstorm";
        }else if(code == 96){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Thunderstorm w/ Light Hail";
        }else if(code == 99){
            codeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            word = "Thunderstorm w/ Heavy Hail";
        }

        return word;
    }

}