package com.example.weatherapp;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.navigation.NavigationView;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationListener, NavigationView.OnNavigationItemSelectedListener {
    public static double log, lat;
    LocationManager locationManager;
    public static String fg, name, desc, io, humidity, pressure, speed, description;
    public String TAG = "MainActivity.class";
    public static int temp, visibility;
    public static long sunr, suns;
    public TextView jk, hj, gh, sd, df, mn;
    public static StringBuilder output = new StringBuilder();
    public static String[] k = new String[6];
    public ListView lf, ri;
    public VideoView videoView;
    public static ArrayList<String> as = new ArrayList<>();
    public static ArrayAdapter<String> arrayAdapter, arrayAdapter2;
    public static ImageView imageView;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        k[0] = "HUMIDITY";
        k[1] = "PRESSURE";
        k[2] = "VISIBILITY";
        k[3] = "WIND SPEED";
        k[4] = "SUNRISE";
        k[5] = "SUNSET";

        arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, k);
        arrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, as);

        sharedPreferences = MainActivity.this.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        jk = findViewById(R.id.place);
        hj = findViewById(R.id.tempc);
        gh = findViewById(R.id.cond);
        sd = findViewById(R.id.data);
        mn = findViewById(R.id.des);
        imageView = findViewById(R.id.image);
        lf = findViewById(R.id.listl);
        ri = findViewById(R.id.listr);
        lf.setAdapter(arrayAdapter);

        // setting the initial value

        set_initial_value();

        // location permission

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        getLocation();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.more_info) {

            Intent moreinfo =new Intent(this,moreinfo.class);
            startActivity(moreinfo);
        } else if (id == R.id.about_app) {

            Intent aboutapp=new Intent(this,aboutapp.class);
            startActivity(aboutapp);
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        log = location.getLongitude();
        lat = location.getLatitude();
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
                Log.e("inside try catch", url.toString());
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
//                    visibility = baseoject.getInt("visibility");
                    JSONObject wind = baseoject.getJSONObject("wind");
                    speed = wind.getString("speed");
                    JSONObject sys = baseoject.getJSONObject("sys");
                    sunr = sys.getLong("sunrise");
                    suns = sys.getLong("sunset");
                    name = baseoject.getString("name");
                    JSONArray sa = baseoject.getJSONArray("weather");
                    JSONObject des = sa.getJSONObject(0);
                    desc = des.getString("main");
                    description = des.getString("description");

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

            display_value();
            updatevalue();
        }
    }

    // conversion of unix time into normal time

    private String gettime(long a) {
        Date date = new java.util.Date(a * 1000L);

        SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");

        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+5:30"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    // updating the shared prefrance value

    private void updatevalue() {
        editor.putString("place", name);
        editor.putString("temperature", io);
        editor.putString("condition", desc);
        editor.putString("description", description);
        editor.putString("humidity", humidity);
        editor.putString("pressure", pressure);
        editor.putString("visibility", fg);
        editor.putString("speed", speed);
        editor.putLong("sunrise", sunr);
        editor.putLong("sunset", suns);

        editor.apply();

    }

    private void set_initial_value()
    {
        jk.setText(sharedPreferences.getString("place","Place"));
        hj.setText(sharedPreferences.getString("temperature","temp"));
        gh.setText(sharedPreferences.getString("condition","cond"));
        mn.setText(sharedPreferences.getString("description","des"));
        as.add(sharedPreferences.getString("humidity","humidity"));
        as.add(sharedPreferences.getString("pressure","pressure"));
        as.add(sharedPreferences.getString("visibility","visibility"));
        as.add(sharedPreferences.getString("speed","speed"));
        as.add(Double.toString(sharedPreferences.getLong("sunrise",0000)));
        as.add(Double.toString(sharedPreferences.getLong("sunset",0000)));
        ri.setAdapter(arrayAdapter2);
    }

    private String getcurrenttime()
    {
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        String dateToStr = format.format(today);
        return ("Last update: "+dateToStr);
    }

    private void display_value()
    {

        as.clear();
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
        imageView.setImageResource(updateimage(desc));
        mn.setText(description);
        sd.setText(getcurrenttime());
    }
    private int updateimage(String a)
    {

        Toast.makeText(this,a, Toast.LENGTH_SHORT).show();

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(a.equals("Thunderstorm")) {
            return R.drawable.storm;
               }
         if(a.equals("Rain")) {

            if(timeOfDay >= 5 && timeOfDay <=18)
                 return R.drawable.rain;
            else
                return R.drawable.night_rain;

        }
         if(a.equals("Clear"))
        {
            if(timeOfDay >= 5 && timeOfDay <=18)
                return R.drawable.sun;
            else
                return R.drawable.moon;
        }
        if (a.equals("Clouds"))
        {
            Toast.makeText(this,Double.toString(timeOfDay), Toast.LENGTH_SHORT).show();
            if(timeOfDay >= 5 && timeOfDay <=18)
                return R.drawable.cloud_sun;
            else
                return R.drawable.cloudy_moon;
        }
        else
            return R.drawable.cloud;

    }
}