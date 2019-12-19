package com.example.climate;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import org.json.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static void main(String[] args) throws IOException, JSONException {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        LatLng brisbane = new LatLng(-33, 129);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        try {
            JSONObject beijing = readJsonFromUrl("https://api.waqi.info/feed/beijing/?token=489dc5c42ae0d28cddba1c0f0818b15cf64d4dc0");
            String[] beijingLoc = beijing.getJSONObject("data").getJSONObject("city").get("geo").toString().split(",",2);;
            beijingLoc[0] = beijingLoc[0].replace("[","");
            beijingLoc[1] = beijingLoc[1].replace("]","");
            LatLng beijingMark = new LatLng(Double.parseDouble(beijingLoc[0]),Double.parseDouble(beijingLoc[1]));
            mMap.addMarker(new MarkerOptions().position(beijingMark).title(beijingLoc[1]));
            mMap.addMarker(new MarkerOptions().position(brisbane).title(beijingLoc[1]));
        } catch (IOException | JSONException e) {
                System.err.println(e);
        }

    }

}
