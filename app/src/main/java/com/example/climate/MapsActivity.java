package com.example.climate;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.opengl.GLException;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jetty.util.IO;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.ExponentialBackOff;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.util.Calendar;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean changedRecently = false;
    LatLng tapMark;
    Marker tapMarker;
    String locationName;
    String locationAQI;
    String locationUV;
    private boolean Choose = false;
    ArrayList<LatLng> examplePoints = new ArrayList<>();
    ArrayList<Marker> exampleMarkers = new ArrayList<>();
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 11;
    private static final String TAG = "MainActivity";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS_READONLY };
    private GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    Timer timer = new Timer();

    private com.google.api.services.sheets.v4.Sheets mService = null;
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */

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

    public void toggleInfoClick() {
        //opens and closes the info text box
        LinearLayout infoText = findViewById(R.id.text_box);
        if (infoText.getVisibility() == LinearLayout.GONE) {
            infoText.setVisibility(LinearLayout.VISIBLE);
        } else if (infoText.getVisibility() == LinearLayout.VISIBLE) {
            infoText.setVisibility(LinearLayout.GONE);
        }
    }

    public void myLocation(View view) {
        //finds current device location
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        try {
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                android.util.Log.i("test",location.toString());
                double myLong = location.getLongitude();
                double myLat = location.getLatitude();
                // Add a location on the map
                LatLng current = new LatLng(myLat, myLong);
                tapMark = current;
                changeInfo("0",false);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
            } else {
                android.util.Log.i("Location Error", "Location not found");
            }
            mMap.animateCamera( CameraUpdateFactory.zoomTo( 13.0f ) );
        } catch (NullPointerException e) {
            android.util.Log.i("onMapClick", "No Location found");
        }

    }

    /**
     * Fetch a list of names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     * @return List of names and majors
     * @throws IOException
     */
    /*
    private List<String> getDataFromApi() throws IOException {
        chooseAccount();
        android.util.Log.i("Account",mService.toString());
        String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
        String range = "Class Data!A2:E";
        List<String> results = new ArrayList<String>();
        ValueRange response = this.mService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values != null) {
            results.add("Name, Major");
            for (List row : values) {
                results.add(row.get(0) + ", " + row.get(4));
            }
        }

            return results;
    }

     */

    public void changeInfo (String loc, boolean move){

        //finds the nearest AQI based on lat and long, before adding it to the map
        double taplat = tapMark.latitude;
        double taplong = tapMark.longitude;
        try {
            JSONObject locAQI = readJsonFromUrl("https://api.waqi.info/feed/geo:"+taplat+";"+taplong+"/?token=489dc5c42ae0d28cddba1c0f0818b15cf64d4dc0");
            android.util.Log.i("First part", tapMark.toString());
            JSONObject locUV = readJsonFromUrl("https://api.openweathermap.org/data/2.5/uvi?appid=49a87b5d0f10027bd80b4cabb1bd2132&lat="+taplat+"&lon="+taplong);
            android.util.Log.i("Second part", tapMark.toString());
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
                    location = locAQI.getJSONObject("data").getJSONObject("city").get("name").toString();
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

            //Find nearest place on website
            //Give them the details

            tapMarker = mMap.addMarker(new MarkerOptions().position(tapMark).title(locationName));//Here is code for trying to chance icon.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_for_map_purpul))););
            locationAQI = locAQI.getJSONObject("data").get("aqi").toString();
            locationUV = locUV.get("value").toString();
            tapMarker.showInfoWindow();

            //Get current date
            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
            String csvurl = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/";
            csvurl = csvurl + df + ".csv";
            android.util.Log.i("URL address", csvurl.toString());


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
        TextView infoUV = findViewById(R.id.info_uv);
        infoUV.setText("UV Index " + locationUV);
        toggleInfoClick();
        android.util.Log.i("LatLong", tapMark.toString());
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
                 mMap.animateCamera( CameraUpdateFactory.zoomTo( 13.0f ) );
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
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
     /*
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() throws IOException {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getDataFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }

        mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), mCredential)
                .setApplicationName("Google Sheets API Android Quickstart")
                .build();
        Choose = true;
    }
  */
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
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},23
                );
            }
        }

        mMap = googleMap;
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
        /*
        try {
            android.util.Log.i("Test",getDataFromApi().toString());
        } catch (IOException e) {
            android.util.Log.i("EXCEPTION",e.toString());
        }

         */

    }

}
