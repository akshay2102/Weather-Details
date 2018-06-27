package com.example.akshay.web_api_example;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    String strAPIKEY = "470dc9b3af5cb53843bce68242a04517";
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(View.GONE);
    }

    public boolean connectionAvailable(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo()!=null);
    }

    private void displayResult(JSONObject jObj){   //the format of receiving JSon object depends on the API used
        try{
            String strOutput = "City : " + jObj.getString("name");
            JSONObject coordObj = jObj.getJSONObject("coord");

            strOutput += "\nLatitude: " + coordObj.getString("lat");
            strOutput += "\nLongitude: " + coordObj.getString("lon");

            JSONObject mainObj = jObj.getJSONObject("main");
            strOutput += "\nHumidity: " + mainObj.getString("humidity");

            java.text.DecimalFormat twoDForm = new java.text.DecimalFormat("#.##");
            strOutput += "\nMax. Temp: " + twoDForm.format(mainObj.getLong("temp_max")-272.15);
            strOutput += "\nMin. Temp: " + twoDForm.format(mainObj.getLong("temp_min")-272.15);
            strOutput += "\nCurrent Temp: " + twoDForm.format(mainObj.getLong("temp")-272.15);

            TextView txt = (TextView) findViewById(R.id.tvWeatherDetails);
            txt.setText(strOutput);
        }
        catch(Exception e){
            Toast.makeText(MainActivity.this,"Error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void callWebAPI(String url){
        if(!connectionAvailable()){
            Toast.makeText(this,"No Internet",Toast.LENGTH_SHORT).show();
            return;
        }

        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                 displayResult(response);
                 pb.setVisibility(View.INVISIBLE);
            }
        };
        Response.ErrorListener failureListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pb.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this,"Error: "+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        };

        JsonObjectRequest apiReq = new JsonObjectRequest(Request.Method.GET,url,null,successListener,failureListener);
        //Adding Request to Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(apiReq);
        pb.setVisibility(View.VISIBLE);
    }

    public void btnCity_Click(View v){
        EditText city = (EditText) findViewById(R.id.etCity);
        String strCity = city.getText().toString();
        callWebAPI("http://api.openweathermap.org/data/2.5/weather?q="+strCity+"&appid="+strAPIKEY);
    }

    LocationManager lm;
    LocationListener locationListener;

    public void btnLocationClick(View v){
        try{
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double lng = location.getLongitude();
                    double lat = location.getLatitude();
                    String url = String.valueOf("http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lng+"&appid="+strAPIKEY);
                    callWebAPI(url);
                    lm.removeUpdates(locationListener);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {
                    Toast.makeText(MainActivity.this,s+"turned off",Toast.LENGTH_SHORT).show();
                }
            };

            lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        }
        catch(SecurityException e){
            Toast.makeText(MainActivity.this,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
}
