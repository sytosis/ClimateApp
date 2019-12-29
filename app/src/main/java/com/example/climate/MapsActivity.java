package com.example.climate;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LatLng tapMark;
    Marker tapMarker;
    private static final String TAG = "MainActivity";

    //reads all the lines on a json
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    //reads the json from a specific URL
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
        Places.initialize(getApplicationContext(), "AIzaSyC7BRVfrayl2FA12t9jwgXvffar_Du9xr0");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                android.util.Log.i("onMapClick", "Got to the first part");
                //find AQI based on search result
                 tapMark = place.getLatLng();
                 double taplat = tapMark.latitude;
                 double taplong = tapMark.longitude;
                android.util.Log.i("onMapClick", tapMark.toString());
                try {
                    JSONObject tapLoc = readJsonFromUrl("https://api.waqi.info/feed/geo:"+taplat+";"+taplong+"/?token=489dc5c42ae0d28cddba1c0f0818b15cf64d4dc0");
                    //only remove the previous marker if it exists
                    if (tapMarker != null) {
                        tapMarker.remove();
                    }
                    tapMarker = mMap.addMarker(new MarkerOptions().position(tapMark).title(place.getName()+ " AQI:" + tapLoc.getJSONObject("data").get("aqi").toString()));
                    tapMarker.showInfoWindow();

                } catch (IOException | JSONException e) {
                    android.util.Log.i("onMapClick", e.toString());
                }
                android.util.Log.i("onMapClick", "Got to the second part ");
            }


            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
                android.util.Log.i("onMapClick", status + "Error");
            }
        });
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

        // Add a location on the map
        LatLng sydney = new LatLng(-34, 151);
        //Adds the marker
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney")).showInfoWindow();
        LatLng brisbane = new LatLng(-33, 129);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Create a new event listener
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            //listener on map click
            @Override
            public void onMapClick(LatLng arg0)
            {
                //prints out the lat and long on debug
                android.util.Log.i("onMapClick", "Hooray!"+arg0);
                //sets lat and long to a variable
                Double taplat = arg0.latitude;
                Double taplong = arg0.longitude;
                //finds the nearest AQI based on lat and long, before adding it to the map
                try {
                    JSONObject tapLoc = readJsonFromUrl("https://api.waqi.info/feed/geo:"+taplat+";"+taplong+"/?token=489dc5c42ae0d28cddba1c0f0818b15cf64d4dc0");
                    //only remove the previous marker if it exists
                    if (tapMarker != null) {
                        tapMarker.remove();
                    }
                    tapMark = new LatLng(taplat,taplong);
                    tapMarker = mMap.addMarker(new MarkerOptions().position(tapMark).title(tapLoc.getJSONObject("data").getJSONObject("city").get("name").toString() + " AQI:" + tapLoc.getJSONObject("data").get("aqi").toString()));//Here is code for trying to chance icon.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_for_map_purpul))););
                    tapMarker.showInfoWindow();

                } catch (IOException | JSONException e) {
                    System.err.println(e);
                }
            }
        });
        

        try {
            //Basic code for creating a marker with AQI info
            JSONObject beijing = readJsonFromUrl("https://api.waqi.info/feed/beijing/?token=489dc5c42ae0d28cddba1c0f0818b15cf64d4dc0");
            String[] beijingLoc = beijing.getJSONObject("data").getJSONObject("city").get("geo").toString().split(",",2);
            beijingLoc[0] = beijingLoc[0].replace("[","");
            beijingLoc[1] = beijingLoc[1].replace("]","");
            LatLng beijingMark = new LatLng(Double.parseDouble(beijingLoc[0]),Double.parseDouble(beijingLoc[1]));
            //mMap.addMarker(new MarkerOptions().position(beijingMark).title("AQI:" + beijing.getJSONObject("data").get("aqi").toString())).showInfoWindow();
            JSONObject nearMe = readJsonFromUrl("https://api.waqi.info/feed/here/?token=489dc5c42ae0d28cddba1c0f0818b15cf64d4dc0");
            mMap.addMarker(new MarkerOptions().position(beijingMark).title("Location:"+nearMe.getJSONObject("data").getJSONObject("city").get("name").toString())).showInfoWindow();
            //change this title to test logging
            mMap.addMarker(new MarkerOptions().position(brisbane).title(beijingLoc[1])).showInfoWindow();

        } catch (IOException | JSONException e) {
                System.err.println(e);
        }

    }

}
