package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationListener {
    public static double log, lat;
    LocationManager locationManager;
    public static String fg, name, desc, io, humidity, pressure, speed,description;
    public String TAG="MainActivity.class";
    public static int temp, visibility;
    public static long sunr, suns;
    public TextView jk, hj, gh, sd, df,mn;
    public static StringBuilder output = new StringBuilder();
    public static String[] k = new String[6];
    public ListView lf, ri;
    public VideoView videoView;
    public static ArrayList<String> as = new ArrayList<>();
    public static ArrayAdapter<String> arrayAdapter, arrayAdapter2;
    int abc=20;
    public static ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        k[0] = "HUMADITY";
        k[1] = "PRESSURE";
        k[2] = "VISIBILITY";
        k[3] = "WIND SPEED";
        k[4] = "SUNRISE";
        k[5] = "SUNSET";

        arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, k);
        arrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, as);

        jk = findViewById(R.id.place);
        hj = findViewById(R.id.tempc);
        gh = findViewById(R.id.cond);
        sd = findViewById(R.id.log);
        df = findViewById(R.id.lat);
        mn = findViewById(R.id.des);
        imageView =(ImageView)findViewById(R.id.image);

        lf = findViewById(R.id.listl);
        ri = findViewById(R.id.listr);
        lf.setAdapter(arrayAdapter);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        getLocation();
    }

    void getLocation() {
        int abc=10;
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        int a=this.abc;
    }

    @Override
    public void onLocationChanged(Location location) {
        log = location.getLongitude();
        lat = location.getLatitude();
        sd.setText("Longtude: " + Double.toString(log));
        df.setText("Latitude: " + Double.toString(lat));
        // new data().execute();
        data ds = new data();
        ds.execute();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public class data extends AsyncTask<String, String, String> {


        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {

            String iu = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + log + "&appid=38e8b3043b0eda5c4cae704a57a20e93";
            String line = "";

            try {
                URL url = new URL(iu);
                Log.e("inside try catch",url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                line = bufferedReader.readLine();
                while (line != null) {
                    output.append(line);
                    Log.e("MainActivity.class", output.toString());
                    line = bufferedReader.readLine();
                }


                try {
                    JSONObject baseoject = new JSONObject(output.toString());
                    JSONObject weather = baseoject.getJSONObject("main");
                    humidity = weather.getString("humidity");
                    pressure = weather.getString("pressure");
                    temp = weather.getInt("temp");
                    visibility = baseoject.getInt("visibility");
                    JSONObject wind = baseoject.getJSONObject("wind");
                    speed = wind.getString("speed");
                    JSONObject sys = baseoject.getJSONObject("sys");
                    sunr = sys.getLong("sunrise");
                    suns = sys.getLong("sunset");
                    name = baseoject.getString("name");
                    JSONArray sa = baseoject.getJSONArray("weather");
                    JSONObject des = sa.getJSONObject(0);
                    desc = des.getString("main");
                    description=des.getString("description");

                    humidity = humidity + "%";
                    pressure = pressure + "hPa";
                    speed = speed + "m/s";

                    temp = temp - 275;
                    io = Double.toString(temp);


                    visibility = visibility / 1000;
                    fg = Double.toString(visibility);
                    fg = fg + "Km";


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String a) {
            super.onPostExecute(a);

            jk.setText(name);
            hj.setText(io);
            gh.setText(desc);
           as.add(humidity);
             as.add(pressure);
             as.add(fg);
             as.add(speed);
             as.add(gettime(sunr));
             as.add(gettime(suns));
             ri.setAdapter(arrayAdapter2);
             ri.deferNotifyDataSetChanged();
             imageView.setImageResource(R.drawable.cloud);
             mn.setText(description);

       /*     if(desc=="Clouds") {
                Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.clouds1);
                videoView.setVideoURI(uri);
            } else if(desc=="Clear") {
                Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.clear);
                videoView.setVideoURI(uri);
            } else if(desc=="Snow") {
                Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.snow1);
                videoView.setVideoURI(uri);
            } else if(desc=="Rain") {
                Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.rain3);
                videoView.setVideoURI(uri);
            }
            else if(desc=="Thunterstorm") {
                Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.thunderstrom);
                videoView.setVideoURI(uri);
            } else {
                Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.haze);
                videoView.setVideoURI(uri);
            }
            */

            //  videoView.start();

        }

        private String gettime(long a) {
            Date date = new java.util.Date(a * 1000L);

            SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");

            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+5:30"));
            String formattedDate = sdf.format(date);
            return formattedDate;
        }
    }
}