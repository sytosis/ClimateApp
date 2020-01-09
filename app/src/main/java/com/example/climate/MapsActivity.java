package com.example.climate;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean changedRecently = false;
    LatLng tapMark;
    Marker tapMarker;
    String locationName;
    String locationAQI;
    ArrayList<LatLng> examplePoints = new ArrayList<>();
    ArrayList<Marker> exampleMarkers = new ArrayList<>();
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 11;
    private static final String TAG = "MainActivity";
    Timer timer = new Timer();
    //reads all the lines on a json
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public void setChangedRecently(boolean bool) {
        changedRecently = bool;
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

    public void toggleInfo(View view) {
        //opens and closes the info text box
        LinearLayout infoText = findViewById(R.id.text_box);
        if (infoText.getVisibility() == LinearLayout.GONE) {
            infoText.setVisibility(LinearLayout.VISIBLE);
        } else if (infoText.getVisibility() == LinearLayout.VISIBLE) {
            infoText.setVisibility(LinearLayout.GONE);
        }
    }

    public void changeInfo (String loc, boolean move) {

        //finds the nearest AQI based on lat and long, before adding it to the map
        double taplat = tapMark.latitude;
        double taplong = tapMark.longitude;
        try {
            JSONObject tapLoc = readJsonFromUrl("https://api.waqi.info/feed/geo:"+taplat+";"+taplong+"/?token=489dc5c42ae0d28cddba1c0f0818b15cf64d4dc0");
            //only remove the previous marker if it exists
            if (tapMarker != null) {
                tapMarker.remove();
            }
            JSONObject actualLoc = readJsonFromUrl("https://maps.googleapis.com/maps/api/geocode/json?latlng="+taplat+","+taplong+"&key=AIzaSyC7BRVfrayl2FA12t9jwgXvffar_Du9xr0");
            if (loc.equals("0")) {
                String location = "";
                //gets data from geocode api so it finds actual location
                try {
                    location = actualLoc.getJSONObject("plus_code").get("compound_code").toString();
                    //gets rid of any unwanted characters from this geocode api
                    location = location.substring(8);
                    char first = location.charAt(0);
                    if (String.valueOf(first).equals(",")){
                        location = location.substring(1);
                    }
                }
                //if it fails then use location from WAQI api
                catch (JSONException e) {
                    location = tapLoc.getJSONObject("data").getJSONObject("city").get("name").toString();
                }
                //delete further strings if there are too many in it
                if (location.length()>40){
                    location = location.split("\\(")[0];
                    location = location + "...";
                }
                locationName = location;
            } else {
                locationName = loc;
            }
            tapMarker = mMap.addMarker(new MarkerOptions().position(tapMark).title(locationName));//Here is code for trying to chance icon.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_for_map_purpul))););
            locationAQI = tapLoc.getJSONObject("data").get("aqi").toString();
            tapMarker.showInfoWindow();

        } catch (IOException | JSONException e) {
            System.err.println(e);
        }
        if (move) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(tapMark));
        }
        TextView infoName = findViewById(R.id.info_name);
        infoName.setText(locationName);
        TextView infoAQI = findViewById(R.id.info_aqi);
        infoAQI.setText("AQI Level " + locationAQI);
    }
    /**
     *Adds a random point to the map
     * @param number number of points to be added
     */
    public void addRandomPoints(int number) {
        //clears the previous points and markers
        for (LatLng l : examplePoints) {
            l = null;
        }
        for (Marker m : exampleMarkers) {
            m.remove();
        }
        exampleMarkers.clear();
        examplePoints.clear();
        double bottomVal = mMap.getProjection().getVisibleRegion().nearLeft.longitude*1.1;
        double topVal = mMap.getProjection().getVisibleRegion().nearRight.longitude*0.9;
        double leftVal = mMap.getProjection().getVisibleRegion().farLeft.latitude*1.1;
        double rightVal = mMap.getProjection().getVisibleRegion().nearRight.latitude*0.9;
        Random random = new Random();
        int i = 0;
        while (i < number) {
            double taplat = leftVal + ((rightVal - leftVal) * random.nextDouble());
            double taplong = bottomVal + ((topVal - bottomVal) * random.nextDouble());
            try {
                JSONObject tapLoc = readJsonFromUrl("https://api.waqi.info/feed/geo:"+taplat+";"+taplong+"/?token=489dc5c42ae0d28cddba1c0f0818b15cf64d4dc0");
                LatLng latlngTemp = new LatLng(taplat,taplong);
                JSONObject actualLoc = readJsonFromUrl("https://maps.googleapis.com/maps/api/geocode/json?latlng="+taplat+","+taplong+"&key=AIzaSyC7BRVfrayl2FA12t9jwgXvffar_Du9xr0");
                String location;
                //gets data from geocode api so it finds actual location
                try {
                    location = actualLoc.getJSONObject("plus_code").get("compound_code").toString();
                    //gets rid of any unwanted characters from this geocode api
                    location = location.substring(8);
                    char first = location.charAt(0);
                    if (String.valueOf(first).equals(",")){
                        location = location.substring(1);
                    }
                }
                //if it fails then use location from WAQI api
                catch (JSONException e) {
                    location = tapLoc.getJSONObject("data").getJSONObject("city").get("name").toString();
                }

                //delete further strings if there are too many in it
                if (location.length()>40){
                    location = location.split("\\(")[0];
                    location = location + "...";
                }
                Marker markerTemp = mMap.addMarker(new MarkerOptions().position(latlngTemp).title(location + " AQI:" + tapLoc.getJSONObject("data").get("aqi").toString()));
                markerTemp.showInfoWindow();
                android.util.Log.i("onMapClick", "markertemp" + markerTemp.toString());
                examplePoints.add(latlngTemp);
                exampleMarkers.add(markerTemp);
                i++;
            } catch (IOException | JSONException e) {
                System.err.println(e);
            }
        }


    }

    public static void main(String[] args) throws IOException, JSONException {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //enable Strict mode
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
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME,Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //find AQI based on search result
                 tapMark = place.getLatLng();
                 changeInfo(place.getName(),true);
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

        //finds current device location
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        android.util.Log.i("test",location.toString());
        double myLong = location.getLongitude();
        double myLat = location.getLatitude();
        // Add a location on the map
        LatLng current = new LatLng(myLat, myLong);
        tapMark = current;
        changeInfo("0",false);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        //Create a new event listener
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            //listener on map click
            @Override
            public void onMapClick(LatLng arg0)
            {
                //sets lat and long to a variable
                Double taplat = arg0.latitude;
                Double taplong = arg0.longitude;
                tapMark = new LatLng(taplat,taplong);
                changeInfo("0",false);
            }
        });
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (!changedRecently) {
                    android.util.Log.i("onMapClick", "moved!");
                    //addRandomPoints(2);
                    setChangedRecently(true);
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            setChangedRecently(false);
                        }
                    }, 1000);
                }


            }
        });
    }

}
