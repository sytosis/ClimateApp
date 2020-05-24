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
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jetty.util.IO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import java.lang.StringBuilder;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    List<List<String>> listOfLocations = new ArrayList<>();
    List<List<String>> listOfCountries = new ArrayList<>();
    List<List<String>> listOfLocationsTemp = new ArrayList<>();
    List<List<String>> listOfCountriesTemp = new ArrayList<>();
    private GoogleMap mMap;
    LatLng tapMark;
    Marker tapMarker;
    String locationName;
    String fullLocationName[];
    String fullLocationNameSearch;
    String locationAQI;
    String locationUV;
    String additionalCountryName;
    String additionalCountryCases;
    String additionalCountryActive;
    String additionalCountryDeaths;
    String additionalCountryRecovered;
    String additionalRegionalName;
    String additionalRegionalCases;
    String additionalRegionalActive;
    String additionalRegionalDeaths;
    String additionalRegionalRecovered;
    String currentDate;
    String currentDateText;
    int settingsCurrent = 1;
    boolean loading = false;
    boolean onOverview = false;
    boolean onInfo = false;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 11;
    private static final String TAG = "MainActivity";
    Timer timer = new Timer();
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

    //toggles additional info layout through buttons
    public void toggleAdditional(View view) {

        LinearLayout additionalLayout = findViewById(R.id.additional_layout);
        LinearLayout dateDisplayLayout = findViewById(R.id.date_display_layout);
        int[] location = new int[2];
        dateDisplayLayout.getLocationOnScreen(location);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) additionalLayout.getLayoutParams();
        layoutParams.topMargin = location[1];
        additionalLayout.setLayoutParams(layoutParams);
        String tag = view.getTag().toString();
        TextView additionalName = findViewById(R.id.additional_name);
        TextView additionalCases = findViewById(R.id.additional_total);
        TextView additionalActive = findViewById(R.id.additional_active);
        TextView additionalDeaths = findViewById(R.id.additional_death);
        TextView additionalRecovered = findViewById(R.id.additional_recovered);
        if (tag.equals("region")) {
            additionalName.setText(additionalRegionalName);
            additionalCases.setText("Total Cases: " + additionalRegionalCases);
            additionalActive.setText("Active Cases: " + additionalRegionalActive);
            additionalDeaths.setText("Deaths: " + additionalRegionalDeaths);
            additionalRecovered.setText("Recovered: " + additionalRegionalRecovered);
        } else if (tag.equals("country")) {
            additionalName.setText(additionalCountryName);
            additionalCases.setText("Total Cases: " + additionalCountryCases);
            additionalActive.setText("Active Cases: " + additionalCountryActive);
            additionalDeaths.setText("Deaths: " + additionalCountryDeaths);
            additionalRecovered.setText("Recovered: " + additionalCountryRecovered);
        }

        if (additionalLayout.getVisibility() == LinearLayout.GONE) {
            additionalLayout.setVisibility(LinearLayout.VISIBLE);
        } else if (additionalLayout.getVisibility() == LinearLayout.VISIBLE) {
            additionalLayout.setVisibility(LinearLayout.GONE);
        }
    }
    //toggles Info through buttons
    public void toggleInfo(View view) {
        //opens and closes the info text box
        LinearLayout infoText = findViewById(R.id.text_box);
        if (infoText.getVisibility() == LinearLayout.GONE) {
            infoText.setVisibility(LinearLayout.VISIBLE);
            //disable google map scrolling and moving when info is open
            if (mMap != null) {
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setTiltGesturesEnabled(false);
                mMap.getUiSettings().setRotateGesturesEnabled(false);
            }
        } else if (infoText.getVisibility() == LinearLayout.VISIBLE) {
            infoText.setVisibility(LinearLayout.GONE);
            //re-enables scrolling
            if (mMap != null) {
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setTiltGesturesEnabled(true);
                mMap.getUiSettings().setRotateGesturesEnabled(true);
            }
        }
    }

    public void toggleDate(View view) {
        //opens and closes the info text box
        LinearLayout dateLayout = findViewById(R.id.date_layout);

        if (dateLayout.getVisibility() == LinearLayout.GONE) {
            //forces Date UI to be directly under the Date shown on Info box
            //finds the date Display Layout
            LinearLayout dateDisplayLayout = findViewById(R.id.date_display_layout);
            //finds the location of the layout on screen, forces it to be directly under the date
            int[] location = new int[2];
            dateDisplayLayout.getLocationOnScreen(location);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) dateLayout.getLayoutParams();
            layoutParams.topMargin = location[1];
            dateLayout.setLayoutParams(layoutParams);
            dateLayout.setVisibility(LinearLayout.VISIBLE);
            if (findViewById(view.getId()) == findViewById(R.id.overview_date_button)) {
                onOverview = true;
                onInfo = false;
            } else if (findViewById(view.getId()) == findViewById(R.id.info_date_button)) {
                onInfo = true;
                onOverview = false;
            }
        } else if (dateLayout.getVisibility() == LinearLayout.VISIBLE) {
            dateLayout.setVisibility(LinearLayout.GONE);
        }
    }

    //toggles info with specified selection
    public void toggleInfoClick(Boolean bool) {
        //opens and closes the info text box
        LinearLayout infoText = findViewById(R.id.text_box);
        if (bool) {
            infoText.setVisibility(LinearLayout.VISIBLE);
            TextView dateText = findViewById(R.id.date_view);
            if (settingsCurrent == 1) {
                dateText.setText("Covid-19 total cases: " + currentDateText);
            } else if (settingsCurrent == 2) {
                dateText.setText("Covid-19 deaths cases: " + currentDateText);
            } else if (settingsCurrent == 3) {
                dateText.setText("Covid-19 recovered cases: " + currentDateText);
            } else if (settingsCurrent == 4) {
                dateText.setText("Covid-19 active cases: " + currentDateText);
            }
            //disable google map scrolling and moving when info is open
            if (mMap != null) {
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setTiltGesturesEnabled(false);
                mMap.getUiSettings().setRotateGesturesEnabled(false);
            }
        } else {
            infoText.setVisibility(LinearLayout.GONE);
            //re-enables scrolling
            if (mMap != null) {
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setTiltGesturesEnabled(true);
                mMap.getUiSettings().setRotateGesturesEnabled(true);
            }

        }
    }

    //toggles additional info with specified selection
    public void toggleAdditionalClick(Boolean bool) {
        //opens and closes the additional box
        LinearLayout additionalBox = findViewById(R.id.additional_layout);
        if (bool) {
            additionalBox.setVisibility(LinearLayout.VISIBLE);
        } else {
            additionalBox.setVisibility(LinearLayout.GONE);
        }
    }

    //toggles overview info with specified selection
    public void toggleOverview(Boolean bool) {
        //opens and closes the additional box
        LinearLayout overviewBox = findViewById(R.id.overview_box);
        if (bool) {
            //disable google map scrolling and moving when info is open
            if (mMap != null) {
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setTiltGesturesEnabled(false);
                mMap.getUiSettings().setRotateGesturesEnabled(false);
            }
            overviewBox.setVisibility(LinearLayout.VISIBLE);
        } else {
            overviewBox.setVisibility(LinearLayout.GONE);
            //re-enables scrolling
            if (mMap != null) {
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setTiltGesturesEnabled(true);
                mMap.getUiSettings().setRotateGesturesEnabled(true);
            }
        }
    }

    //toggles setting info
    public void toggleSettings(View view) {
        LinearLayout settingsBox = findViewById(R.id.settings_layout);
        if (settingsBox.getVisibility() == LinearLayout.VISIBLE) {
            settingsBox.setVisibility(LinearLayout.GONE);
            //re-enables scrolling
            if (mMap != null) {
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setTiltGesturesEnabled(true);
                mMap.getUiSettings().setRotateGesturesEnabled(true);
            }
        } else {
            //disable google map scrolling and moving when info is open
            if (mMap != null) {
                toggleAdditionalClick(false);
                toggleOverview(false);
                toggleInfoClick(false);
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setTiltGesturesEnabled(false);
                mMap.getUiSettings().setRotateGesturesEnabled(false);
            }
            settingsBox.setVisibility(LinearLayout.VISIBLE);
        }
    }

    //toggles overview info
    public void toggleOverview(View view) {
        //opens and closes the overview box
        LinearLayout overviewBox = findViewById(R.id.overview_box);
        TextView dateText = findViewById(R.id.overview_date);
        if (overviewBox.getVisibility() == LinearLayout.VISIBLE) {
            overviewBox.setVisibility(LinearLayout.GONE);
            //re-enables scrolling
            if (mMap != null) {
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setTiltGesturesEnabled(true);
                mMap.getUiSettings().setRotateGesturesEnabled(true);
            }
        } else {
            //disable google map scrolling and moving when info is open
            if (mMap != null) {
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setTiltGesturesEnabled(false);
                mMap.getUiSettings().setRotateGesturesEnabled(false);
            }

            List<List<String>> tempList = new ArrayList(listOfCountries);
            dateText.setText(currentDateText);
            int i = 0;
            while (i < 16) {
                String textID = "overview_" + i;
                int resID = getResources().getIdentifier(textID, "id", getPackageName());
                TextView textView = findViewById(resID);
                List countryList = new ArrayList();
                for (int j = 0; j < tempList.size(); j++) {
                    Boolean pass = true;
                    for (int k = 0; k < tempList.size(); k++) {
                        if (Double.parseDouble(tempList.get(j).get(settingsCurrent)) < Double.parseDouble(tempList.get(k).get(settingsCurrent))) {
                            pass = false;
                        }
                    }
                    if (pass) {
                        countryList.addAll(tempList.get(j));
                        tempList.remove(j);
                        textView.setText(countryList.get(0).toString() + ": " + countryList.get(settingsCurrent).toString());
                    }
                }
                i++;

            }
            overviewBox.setVisibility(LinearLayout.VISIBLE);
        }
    }

    //toggles date with specified selection
    public void toggleDateClick(Boolean bool) {
        //opens and closes the additional box
        LinearLayout dateLayout = findViewById(R.id.date_layout);
        if (bool) {
            dateLayout.setVisibility(LinearLayout.VISIBLE);
        } else {
            dateLayout.setVisibility(LinearLayout.GONE);
        }
    }

    //toggles loading circle with selection
    public void toggleLoadingCircle(Boolean bool) {
        //opens and closes the info text box
        ImageView loadingCircle = findViewById(R.id.loading_circle);
        if (bool) {
            loadingCircle.setVisibility(LinearLayout.VISIBLE);
        } else {
            loadingCircle.setVisibility(LinearLayout.GONE);
        }
    }

    //code to set default covid cases viewing
    public void changeDefaultCases(View view) {
        TextView overviewLayout = findViewById(R.id.overview_title);
        if (findViewById(view.getId()) == findViewById(R.id.settings_select_total)) {
            settingsCurrent = 1;
            overviewLayout.setText("Covid-19 country total cases: ");
        } else if (findViewById(view.getId()) == findViewById(R.id.settings_select_deaths)) {
            settingsCurrent = 2;
            overviewLayout.setText("Covid-19 country deaths cases: ");
        } else if (findViewById(view.getId()) == findViewById(R.id.settings_select_recovered)) {
            settingsCurrent = 3;
            overviewLayout.setText("Covid-19 country recovered cases: ");
        } else if (findViewById(view.getId()) == findViewById(R.id.settings_select_active)) {
            settingsCurrent = 4;
            overviewLayout.setText("Covid-19 country active cases: ");
        }
    }
    //code for when the user picks a date
    public void datePick(View view) {
        listOfLocationsTemp.clear();
        listOfCountriesTemp.clear();
        DatePicker picker = findViewById(R.id.datePicker);
        System.out.println(picker.getDayOfMonth() + "," + picker.getMonth() + "," + picker.getYear());
        //sets up the formatted date based on picker
        String monthString =  String.valueOf(picker.getMonth() + 1);
        if (monthString.length() == 1) {
            monthString = "0" + monthString;
        }
        String dayString =  String.valueOf(picker.getDayOfMonth());
        if (dayString.length() == 1) {
            dayString = "0" + dayString;
        }
        String formattedDate = monthString + "-" + dayString + "-" + picker.getYear();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        Date date = new Date();
        try {
           date = df.parse(formattedDate);
        } catch (ParseException e) {
            System.out.println(e.toString());
        }

        //fill temp list with data
        try {
            String csvurl = "";
            String original = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/";
            csvurl = original + formattedDate + ".csv";
            URL testUrl = new URL(csvurl);
            HttpURLConnection huc = (HttpURLConnection) testUrl.openConnection();
            int responseCode = huc.getResponseCode();
            String response = Integer.toString(responseCode);
            if (responseCode == 200) {
                android.util.Log.i("Picker Date found", "True");
            }
            //Reading csv url
            URL url = new URL(csvurl);
            try(InputStream in = url.openStream();
                InputStreamReader inr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(inr)) {
                String line = br.readLine();
                while(line != null) {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    char tester = line.charAt(0);
                    if (!Character.isLetter(tester)) {
                        line = line.substring(1);
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append(line);
                    line = builder.toString();
                    List<String> listCsv = new ArrayList<String>();

                    //for the formatted date after 22/3/2020 due to the data input format change
                    //date is formatted weird, year has to - 1900 and month starts at 0 so have to - 1
                    if (date.after(new Date(2020 - 1900,3 - 1,21))) {
                        System.out.println("Accessing data Set 3");
                        //if it has quotation marks in it, it means theres comma that breaks the split for ","
                        //this is tested by checking if the last character of the string has anything that isnt a letter eg a quotation mark
                        if (line.substring(line.length() - 1).equals("\"")) {
                            List<String> listQuotes = Arrays.asList(line.split("\""));
                            listCsv.add(listQuotes.get(1));
                            List<String> listCsvTemp;
                            listCsvTemp = Arrays.asList(listQuotes.get(0).split(","));
                            try {
                                listCsvTemp = listCsvTemp.subList(listCsvTemp.size() - 4, listCsvTemp.size());
                            } catch (IndexOutOfBoundsException e) {
                                //for some reason south korea had more than the quotation marks at the end of the string
                                System.out.println(e.toString());
                                listCsvTemp = Arrays.asList(listQuotes.get(2).split(","));
                                listCsvTemp = listCsvTemp.subList(listCsvTemp.size() - 4, listCsvTemp.size());
                            }
                            listCsv.add(listCsvTemp.get(0));
                            listCsv.add(listCsvTemp.get(1));
                            listCsv.add(listCsvTemp.get(2));
                            listCsv.add(listCsvTemp.get(3));
                        } else {
                            listCsv = Arrays.asList(line.split(",",11));
                            listCsv = listCsv.subList(6,listCsv.size());
                            String confirmed = listCsv.get(0);
                            String deaths = listCsv.get(1);
                            String recovered = listCsv.get(2);
                            String active = listCsv.get(3);
                            String name = listCsv.get(4);

                            listCsv.set(0,name);
                            listCsv.set(1,confirmed);
                            listCsv.set(2,deaths);
                            listCsv.set(3,recovered);
                            listCsv.set(4,active);
                        }

                        System.out.println(listCsv.toString());
                        //Collections.reverse(listCsv);
                        String countryName[] = listCsv.get(0).split(",");
                        //replaces any foreign characters in country
                        String testCountry = countryName[countryName.length - 1];
                        System.out.println("country" + testCountry);
                        //replace US with United states
                        if (testCountry.contains("US")) {
                            countryName[countryName.length - 1] = "United States";
                        }
                        if (testCountry.contains("South")) {
                            countryName[countryName.length - 1] = "South Korea";
                        }
                        String country = String.join(",", countryName);
                        listCsv.set(0,country);
                    } else if (date.after(new Date(2020 - 1900,2 - 1,29)) && date.before(new Date(2020 - 1900,3 - 1,22))){
                        System.out.println("Accessing data Set 2");
                        tester = line.charAt(0);
                        if (!Character.isLetter(tester)) {
                            line = line.substring(1);
                        }

                        StringBuilder sb = new StringBuilder();
                        sb.append(line);
                        line = sb.reverse().toString();
                        listCsv = Arrays.asList(line.split(",",7));
                        Collections.reverse(listCsv);
                        //reverses each content in listCsv
                        for (int j = 0; j < listCsv.size(); j++) {
                            builder = new StringBuilder();
                            builder.append(listCsv.get(j));
                            builder = builder.reverse();
                            listCsv.set(j,builder.toString());

                        }
                        System.out.println(listCsv);
                        System.out.println(listCsv.get(0));
                        String countryName[] = listCsv.get(0).split(",");
                        //replaces any foreign characters in country
                        String testCountry = countryName[countryName.length - 1];
                        System.out.println("country" + testCountry);
                        //replace US with United states
                        if (testCountry.contains("US")) {
                            countryName[countryName.length - 1] = "United States";
                        }
                        if (testCountry.contains("South")) {
                            countryName[countryName.length - 1] = "South Korea";
                        }
                        String country = String.join(",", countryName);
                        listCsv.set(0,country);
                        String confirmed = listCsv.get(2);
                        String deaths = listCsv.get(3);
                        String recovered = listCsv.get(4);
                        Double activeTemp = Double.parseDouble(confirmed) - Double.parseDouble(deaths) - Double.parseDouble(recovered);
                        String active = String.valueOf(activeTemp);
                        String name = listCsv.get(0);

                        listCsv.set(0,name);
                        listCsv.set(1,confirmed);
                        listCsv.set(2,deaths);
                        listCsv.set(3,recovered);
                        listCsv.set(4,active);
                    } else if (date.before(new Date(2020-1900,3 - 1,1))) {
                        System.out.println("Accessing data Set 1");
                        tester = line.charAt(0);
                        if (!Character.isLetter(tester)) {
                            line = line.substring(1);
                        }

                        StringBuilder sb = new StringBuilder();
                        sb.append(line);
                        line = sb.reverse().toString();
                        listCsv = Arrays.asList(line.split(",",5));
                        Collections.reverse(listCsv);
                        //reverses each content in listCsv
                        for (int j = 0; j < listCsv.size(); j++) {
                            builder = new StringBuilder();
                            builder.append(listCsv.get(j));
                            builder = builder.reverse();
                            listCsv.set(j,builder.toString());

                        }
                        System.out.println(listCsv);
                        System.out.println(listCsv.get(0));
                        String countryName[] = listCsv.get(0).split(",");
                        //replaces any foreign characters in country
                        String testCountry = countryName[countryName.length - 1];
                        System.out.println("country" + testCountry);
                        //replace US with United states
                        if (testCountry.contains("US")) {
                            countryName[countryName.length - 1] = "United States";
                        }
                        if (testCountry.contains("South")) {
                            countryName[countryName.length - 1] = "South Korea";
                        }
                        String country = String.join(",", countryName);
                        listCsv.set(0,country);
                        String confirmed = listCsv.get(2);
                        if (confirmed == "") {
                            confirmed = "0";
                        }
                        System.out.println(confirmed);
                        String deaths = listCsv.get(3);
                        if (deaths == "") {
                            deaths = "0";
                        }
                        String recovered = listCsv.get(4);
                        if (recovered == "") {
                            recovered = "0";
                        }
                        String active = "0";
                        try {
                            Double activeTemp = Double.parseDouble(confirmed) - Double.parseDouble(deaths) - Double.parseDouble(recovered);
                            active = String.valueOf(activeTemp);
                        } catch (NumberFormatException e) {

                        }

                        String name = listCsv.get(0);

                        listCsv.set(0,name);
                        listCsv.set(1,confirmed);
                        listCsv.set(2,deaths);
                        listCsv.set(3,recovered);
                        listCsv.set(4,active);
                    }
                    listOfLocationsTemp.add(listCsv);
                }
                System.out.println(listOfLocationsTemp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//setting up country data through list of locations done above
        for (int i = 0; i < listOfLocationsTemp.size(); ++i) {
            String getCountry[] = listOfLocationsTemp.get(i).get(0).split(",");
            String tempCountry = getCountry[getCountry.length-1];
            if (Character.isWhitespace(tempCountry.charAt(0))) {
                System.out.println("tried to change country space");
                tempCountry = tempCountry.substring(1);
            }
            List<String> tempLocation = new ArrayList<>();
            boolean countryNotInList = true;
            tempLocation.add(tempCountry);
            tempLocation.add(listOfLocationsTemp.get(i).get(1));
            tempLocation.add(listOfLocationsTemp.get(i).get(2));
            tempLocation.add(listOfLocationsTemp.get(i).get(3));
            tempLocation.add(listOfLocationsTemp.get(i).get(4));
            for (int x = 0; x < listOfCountriesTemp.size(); ++x) {
                if (listOfCountriesTemp.get(x).get(0).equals(tempCountry)) {
                    countryNotInList = false ;
                }
            }
            if (countryNotInList) {
                listOfCountriesTemp.add(tempLocation);
            } else {
                for (int y = 0; y < listOfCountriesTemp.size(); ++y) {
                    if (listOfCountriesTemp.get(y).get(0).equals(tempCountry)) {
                        //Cases
                        Double value = Double.parseDouble(listOfCountriesTemp.get(y).get(1)) + Double.parseDouble(tempLocation.get(1));
                        String intValue = Double.toString(value);
                        listOfCountriesTemp.get(y).set(1,intValue);

                        //deaths
                        value = Double.parseDouble(listOfCountriesTemp.get(y).get(2)) + Double.parseDouble(tempLocation.get(2));
                        intValue = Double.toString(value);
                        listOfCountriesTemp.get(y).set(2,intValue);

                        //recovered
                        value = Double.valueOf(listOfCountriesTemp.get(y).get(3)) + Double.valueOf(tempLocation.get(3));
                        intValue = Double.toString(value);
                        listOfCountriesTemp.get(y).set(3,intValue);

                        //active cases, sometimes its not recorded and tests if its 0 to double check.
                        if (Double.valueOf(tempLocation.get(4)) != 0) {
                            value = Double.valueOf(listOfCountriesTemp.get(y).get(4)) + Double.valueOf(tempLocation.get(4));
                            intValue = Double.toString(value);
                            listOfCountriesTemp.get(y).set(4,intValue);
                        } else {
                            value = Double.valueOf(listOfCountriesTemp.get(y).get(1)) - Double.valueOf(listOfCountriesTemp.get(y).get(2)) -  Double.valueOf(listOfCountriesTemp.get(y).get(3));
                            intValue = Double.toString(value);
                            listOfCountriesTemp.get(y).set(4,intValue);
                            System.out.println("Active value was found to be 0, recalculating");
                        }

                    }
                }
            }
        }
        System.out.println(listOfCountriesTemp);
        toggleDateClick(false);
        //main setup of date picker done, below is to reassign based on which button clicked it.
        if (onInfo) {
            changeInfo("0",false,true);
        } else if (onOverview) {
            TextView dateText = findViewById(R.id.overview_date);
            dateText.setText(picker.getDayOfMonth() + "/" + (picker.getMonth() + 1) + "/" + picker.getYear());
            List<List<String>> tempList = new ArrayList(listOfCountriesTemp);
            int i = 0;
            //resets the text
            while (i < 16) {
                String textID = "overview_" + i;
                int resID = getResources().getIdentifier(textID, "id", getPackageName());
                TextView textView = findViewById(resID);
                textView.setText("");
                i++;

            }
            i = 0;
            while (i < 16) {
                String textID = "overview_" + i;
                int resID = getResources().getIdentifier(textID, "id", getPackageName());
                TextView textView = findViewById(resID);
                List countryList = new ArrayList();
                for (int j = 0; j < tempList.size(); j++) {
                    Boolean pass = true;
                    for (int k = 0; k < tempList.size(); k++) {
                        if (Double.parseDouble(tempList.get(j).get(settingsCurrent)) < Double.parseDouble(tempList.get(k).get(settingsCurrent))) {
                            pass = false;
                        }
                    }
                    if (pass) {
                        countryList.addAll(tempList.get(j));
                        tempList.remove(j);
                        textView.setText(countryList.get(0).toString() + ": " + countryList.get(settingsCurrent).toString());
                    }
                }
                i++;

            }
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
                toggleLoadingCircle(true);
                tapMark = current;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Your logic here...

                        // When you need to modify a UI element, do so on the UI thread.
                        // 'getActivity()' is required as this is being ran from a Fragment.
                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                changeInfo("0",false,false);
                                toggleLoadingCircle(false);
                                timer.purge();
                                timer.cancel();
                                loading = false;
                            }
                        });
                    }
                }, 0, 5000); // End of your timer code.
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
            } else {
                android.util.Log.i("Location Error", "Location not found");
            }
            mMap.animateCamera( CameraUpdateFactory.zoomTo( 13.0f ) );
        } catch (NullPointerException e) {
            android.util.Log.i("onMapClick", "No Location found");
        }

    }

    public void changeInfo (String loc, boolean move, boolean tempDate){
        System.out.println("ON!");
        if (!tempDate) {
            toggleInfoClick(true);
        }
        //finds the nearest AQI based on lat and long, before adding it to the map
        double taplat = tapMark.latitude;
        double taplong = tapMark.longitude;
        List regionList = new ArrayList();
        List countryList = new ArrayList();
        if (!tempDate) {
            try {
                JSONObject locAQI = readJsonFromUrl("https://api.waqi.info/feed/geo:"+taplat+";"+taplong+"/?token=489dc5c42ae0d28cddba1c0f0818b15cf64d4dc0");
                Log.i("First part", tapMark.toString());
                JSONObject locUV = readJsonFromUrl("https://api.openweathermap.org/data/2.5/uvi?appid=49a87b5d0f10027bd80b4cabb1bd2132&lat="+taplat+"&lon="+taplong);
                Log.i("Second part", tapMark.toString());
                //only remove the previous marker if it exists
                if (tapMarker != null) {
                    tapMarker.remove();
                }
                JSONObject actualLoc = readJsonFromUrl("https://maps.googleapis.com/maps/api/geocode/json?latlng="+taplat+","+taplong+"&key=AIzaSyC7BRVfrayl2FA12t9jwgXvffar_Du9xr0");
                if (loc.equals("0")) {
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
                    catch (Exception e) {
                        location = locAQI.getJSONObject("data").getJSONObject("city").get("name").toString();
                        android.util.Log.i("Location AQI Error ",e.toString());
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
                JSONArray fullNameArray = actualLoc.getJSONArray("results").getJSONObject(1).getJSONArray("address_components");
                String fullNameSearch = "";
                for (int i = 0; i < fullNameArray.length(); i++) {
                    JSONObject namePart = fullNameArray.getJSONObject(i);
                    String tempName = namePart.get("long_name").toString();
                    if (!tempName.matches("\\d+")) {
                        fullNameSearch += namePart.get("long_name");
                        fullNameSearch += ",";
                    }

                }
                fullNameSearch = fullNameSearch.substring(0,fullNameSearch.length() - 1);
                String[] tapLocationName = fullNameSearch.split(",");
                //remove the event where the last part of the compound code is some numbers or else it wont be able to search the last index of code,
                if (!tapLocationName[tapLocationName.length - 1].matches("[a-zA-Z]+")) {
                    tapLocationName[tapLocationName.length - 1] ="";
                    fullNameSearch = String.join(",", tapLocationName);
                    fullNameSearch = fullNameSearch.substring(0,fullNameSearch.length() - 1);
                }
                android.util.Log.i("Full location name",fullNameSearch);
                fullLocationName = tapLocationName;
                fullLocationNameSearch = fullNameSearch;
                locationAQI = locAQI.getJSONObject("data").get("aqi").toString();
                locationUV = locUV.get("value").toString();
                tapMarker = mMap.addMarker(new MarkerOptions().position(tapMark).title(locationName));//Here is code for trying to change icon.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_for_map_purpul))););
                tapMarker.showInfoWindow();
            } catch (IOException | JSONException e) {
                System.err.println(e);
            }
        }
        List<List<String>> locationList;
        List<List<String>> countriesList;
        if (tempDate) {
            locationList = listOfLocationsTemp;
            countriesList = listOfCountriesTemp;
            System.out.println("Using Temp");
            DatePicker picker = findViewById(R.id.datePicker);
            //change the date to match this date
            TextView dateText = findViewById(R.id.date_view);
            if (settingsCurrent == 1) {
                dateText.setText("Covid-19 total cases: " + picker.getDayOfMonth() + "/" + (picker.getMonth() + 1) + "/" + picker.getYear());
            } else if (settingsCurrent == 2) {
                dateText.setText("Covid-19 deaths cases: " + picker.getDayOfMonth() + "/" + (picker.getMonth() + 1) + "/" + picker.getYear());
            } else if (settingsCurrent == 3) {
                dateText.setText("Covid-19 recovered cases: " + picker.getDayOfMonth() + "/" + (picker.getMonth() + 1) + "/" + picker.getYear());
            } else if (settingsCurrent == 4) {
                dateText.setText("Covid-19 active cases: " + picker.getDayOfMonth() + "/" + (picker.getMonth() + 1) + "/" + picker.getYear());
            }

        } else {
            System.out.println("Using Main");
            locationList = listOfLocations;
            countriesList = listOfCountries;
        }
        // for loop label
        aa:
        // searches all the lists(1) in the list of lists(0)
        // calculate state cases here
        for (int i = 0; i < locationList.size(); ++i) {
            //getting list(1)
            List list = locationList.get(i);
            // search all parts in the list(1)
            // getting a part of list(1)
            String listSearchSection = list.get(0).toString();
            // splitting each part of the fullnamesearch into parts based on space
            // checking if each part exists in a section
            for (int z = 0; z < fullLocationName.length-1; ++z) {
                // making the part and section lowercase
                String tapLocationNameLower = fullLocationName[z].toLowerCase();
                String listSearchSectionLower = listSearchSection.toLowerCase();
                // checking if the section contains a part
                if (listSearchSectionLower.contains(tapLocationNameLower)) {
                    System.out.println(tapLocationNameLower);
                    android.util.Log.i("first location match", list.get(0).toString());
                    //breaks aa for loop
                    if (listSearchSectionLower.contains(",")) {
                        String country[] = listSearchSectionLower.split(",");
                        if (country[country.length-1].contains(fullLocationName[fullLocationName.length-1].toLowerCase())) {
                            android.util.Log.i("list state found", list.toString());
                            regionList = list;
                            break aa;
                        }
                        // if the country section for both are the same break
                    }

                }
            }
        }
        //searches countries cases here
        for (int i = 0; i < countriesList.size(); ++i) {
            //gets country cases
            List listCountryList = countriesList.get(i);
            String listCountry = listCountryList.get(0).toString().toLowerCase();
            // splitting each part of the fullnamesearch into parts based on space
            String[] tapLocationFull = fullLocationNameSearch.split(",");
            String tapLocationCountry = tapLocationFull[tapLocationFull.length - 1].toLowerCase();
            if (tapLocationCountry.contains(listCountry)) {
                android.util.Log.i("list country found", listCountryList.get(0).toString());
                countryList = listCountryList;
            }

            //sometimes there's a region but not a country found, because the region name from the main api
            //does not contain the country inside so this fixes it by going with the region name instead
            //and checks if there is a region then it would use the region's name
            if (regionList.size() != 0) {
                //if country wasn't already found, find it since there was a region
                if (countryList.size() == 0) {
                    String[] regionListFull = regionList.get(0).toString().split(",");
                    String regionFindCountry = regionListFull[regionListFull.length - 1].toLowerCase();
                    if (regionFindCountry.equals(listCountry)) {
                        android.util.Log.i("list country found through region search", listCountryList.get(0).toString());
                        countryList = listCountryList;
                    }
                }
            }
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
        TextView region_name = findViewById(R.id.region_name);
        TextView region_cases = findViewById(R.id.region_cases);
        TextView country_name= findViewById(R.id.country_name);
        TextView country_cases = findViewById(R.id.country_cases);
        ImageButton regionButton = findViewById(R.id.region_detailed_button);
        ImageButton countryButton = findViewById(R.id.country_detailed_button);
        ImageButton dateButton = findViewById(R.id.info_date_button);
        if (regionList.size() != 0) {
            String[] regionListFull = regionList.get(0).toString().split(",");
            String regionName;
            if (regionListFull.length > 2) {
                if (regionList.get(0).toString().length() > 30) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(regionListFull[regionListFull.length - 2]);
                    stringBuilder.append(", ");
                    stringBuilder.append(regionListFull[regionListFull.length - 1]);
                    regionName = stringBuilder.toString();
                } else {
                    regionName = regionList.get(0).toString();
                }
            } else {
                regionName = regionList.get(0).toString();
            }

            region_name.setText("Locality: " + regionName);
            region_cases.setText("Local Cases: " + regionList.get(settingsCurrent));
            //populate the additional region cases
            additionalRegionalName = regionList.get(0).toString();
            additionalRegionalCases = regionList.get(1).toString();
            additionalRegionalDeaths = regionList.get(2).toString();
            additionalRegionalRecovered = regionList.get(3).toString();
            additionalRegionalActive = regionList.get(4).toString();
            regionButton.setVisibility(View.VISIBLE);

        } else {
            region_name.setText("No local coronavirus cases found");
            region_cases.setText("");
            regionButton.setVisibility(View.GONE);
        }
        if (countryList.size() != 0) {
            country_name.setText("Country: " + countryList.get(0));
            country_cases.setText("Country Cases: " + countryList.get(settingsCurrent));
            //populate the additional country cases
            additionalCountryName = countryList.get(0).toString();
            additionalCountryCases = countryList.get(1).toString();
            additionalCountryDeaths = countryList.get(2).toString();
            additionalCountryRecovered = countryList.get(3).toString();
            additionalCountryActive = countryList.get(4).toString();
            countryButton.setVisibility(View.VISIBLE);
            dateButton.setVisibility(View.VISIBLE);
        } else {
            country_name.setText("No regional coronavirus cases found");
            country_cases.setText("");
            countryButton.setVisibility(View.GONE);
            dateButton.setVisibility(View.GONE);
        }
        Log.i("LatLong", tapMark.toString());
        System.out.println("OFF!");

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
                 toggleLoadingCircle(true);
                 final String name = place.getName();
                 timer = new Timer();
                 timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                changeInfo(name,true,false);
                                toggleLoadingCircle(false);
                                timer.purge();
                                timer.cancel();
                                loading = false;
                            }
                        });
                    }
                }, 0, 5000); // End of your timer code.
                 mMap.animateCamera( CameraUpdateFactory.zoomTo( 13.0f ) );
            }


            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
                android.util.Log.i("onMapClick", status + "Error");
            }
        });

        //sets up coronavirus reading data
        try {
            //Get current date
            Date currentTime = Calendar.getInstance().getTime();
            //calender setup
            Calendar cal = Calendar.getInstance();
            int i = 0;
            boolean foundData = false;
            String csvurl = "";
            String original = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/";
            while (i < 4 && !foundData) {
                //minus "i" days
                cal.setTime(currentTime);
                cal.add(Calendar.DATE, -i);
                Date date = cal.getTime();
                SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
                SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
                String textDate = df2.format(date);
                String formattedDate = df.format(date);
                csvurl = original + formattedDate + ".csv";
                URL testUrl = new URL(csvurl);
                HttpURLConnection huc = (HttpURLConnection) testUrl.openConnection();
                int responseCode = huc.getResponseCode();
                String response = Integer.toString(responseCode);
                if (responseCode == 200) {
                    currentDate = formattedDate;
                    currentDateText = textDate;
                    break;
                }
                Log.i("response code", response);
                Log.i("URL address", csvurl);
                i+=1;
            }
            //Reading csv url
            URL url = new URL(csvurl);
            try(InputStream in = url.openStream();

                InputStreamReader inr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(inr)) {
                String line = br.readLine();
                while(line != null) {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    char tester = line.charAt(0);
                    if (!Character.isLetter(tester)) {
                        line = line.substring(1);
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append(line);
                    line = builder.toString();
                    List<String> listCsv = new ArrayList<String>();
                    //if it has quotation marks in it, it means theres comma that breaks the split for ","
                    //this is tested by checking if the last character of the string has anything that isnt a letter eg a quotation mark
                    if (line.substring(line.length() - 1).equals("\"")) {
                        List<String> listQuotes = Arrays.asList(line.split("\""));
                        listCsv.add(listQuotes.get(1));
                        List<String> listCsvTemp;
                        listCsvTemp = Arrays.asList(listQuotes.get(0).split(","));
                        try {
                            listCsvTemp = listCsvTemp.subList(listCsvTemp.size() - 4, listCsvTemp.size());
                        } catch (IndexOutOfBoundsException e) {
                            //for some reason south korea had more than the quotation marks at the end of the string
                            System.out.println(e.toString());
                            listCsvTemp = Arrays.asList(listQuotes.get(2).split(","));
                            listCsvTemp = listCsvTemp.subList(listCsvTemp.size() - 4, listCsvTemp.size());
                        }
                        listCsv.add(listCsvTemp.get(0));
                        listCsv.add(listCsvTemp.get(1));
                        listCsv.add(listCsvTemp.get(2));
                        listCsv.add(listCsvTemp.get(3));
                    } else {
                        listCsv = Arrays.asList(line.split(",",11));
                        listCsv = listCsv.subList(6,listCsv.size());
                        String confirmed = listCsv.get(0);
                        String deaths = listCsv.get(1);
                        String recovered = listCsv.get(2);
                        String active = listCsv.get(3);
                        String name = listCsv.get(4);

                        listCsv.set(0,name);
                        listCsv.set(1,confirmed);
                        listCsv.set(2,deaths);
                        listCsv.set(3,recovered);
                        listCsv.set(4,active);
                    }

                    System.out.println(listCsv.toString());
                    //Collections.reverse(listCsv);
                    String countryName[] = listCsv.get(0).split(",");
                    //replaces any foreign characters in country
                    String testCountry = countryName[countryName.length - 1];
                    System.out.println("country" + testCountry);
                    //replace US with United states
                    if (testCountry.contains("US")) {
                        countryName[countryName.length - 1] = "United States";
                    }
                    if (testCountry.contains("South")) {
                        countryName[countryName.length - 1] = "South Korea";
                    }
                    String country = String.join(",", countryName);
                    listCsv.set(0,country);
                    listOfLocations.add(listCsv);
                }
                System.out.println(listOfLocations);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //setting up country data through list of locations done above
        for (int i = 0; i < listOfLocations.size(); ++i) {
            String getCountry[] = listOfLocations.get(i).get(0).split(",");
            String tempCountry = getCountry[getCountry.length-1];
            if (Character.isWhitespace(tempCountry.charAt(0))) {
                System.out.println("tried to change country space");
                tempCountry = tempCountry.substring(1);
            }
            List<String> tempLocation = new ArrayList<>();
            boolean countryNotInList = true;
            tempLocation.add(tempCountry);
            tempLocation.add(listOfLocations.get(i).get(1));
            tempLocation.add(listOfLocations.get(i).get(2));
            tempLocation.add(listOfLocations.get(i).get(3));
            tempLocation.add(listOfLocations.get(i).get(4));
            for (int x = 0; x < listOfCountries.size(); ++x) {
                if (listOfCountries.get(x).get(0).equals(tempCountry)) {
                    countryNotInList = false ;
                }
            }
            if (countryNotInList) {
                listOfCountries.add(tempLocation);
            } else {
                for (int y = 0; y < listOfCountries.size(); ++y) {
                    if (listOfCountries.get(y).get(0).equals(tempCountry)) {
                        //Cases
                        Double value = Double.valueOf(listOfCountries.get(y).get(1)) + Double.valueOf(tempLocation.get(1));
                        String intValue = Double.toString(value);
                        listOfCountries.get(y).set(1,intValue);

                        //deaths
                        value = Double.valueOf(listOfCountries.get(y).get(2)) + Double.valueOf(tempLocation.get(2));
                        intValue = Double.toString(value);
                        listOfCountries.get(y).set(2,intValue);

                        //recovered
                        value = Double.valueOf(listOfCountries.get(y).get(3)) + Double.valueOf(tempLocation.get(3));
                        intValue = Double.toString(value);
                        listOfCountries.get(y).set(3,intValue);

                        //active cases, sometimes its not recorded and tests if its 0 to double check.
                        if (Double.valueOf(tempLocation.get(4)) != 0) {
                            value = Double.valueOf(listOfCountries.get(y).get(4)) + Double.valueOf(tempLocation.get(4));
                            intValue = Double.toString(value);
                            listOfCountries.get(y).set(4,intValue);
                        } else {
                            value = Double.valueOf(listOfCountries.get(y).get(1)) - Double.valueOf(listOfCountries.get(y).get(2)) -  Double.valueOf(listOfCountries.get(y).get(3));
                            intValue = Double.toString(value);
                            listOfCountries.get(y).set(4,intValue);
                            System.out.println("Active value was found to be 0, recalculating");
                        }

                    }
                }

            }
        }
        System.out.println(listOfCountries);

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
        //close the two open info bars
        toggleInfoClick(false);
        toggleAdditionalClick(false);
        toggleDateClick(false);
        String dateTempValues[] = currentDateText.split("/");
        int dateValues[] = new int[3];
        for(int i=0; i<dateTempValues.length; i++) {
            dateValues[i] = Integer.parseInt(dateTempValues[i]);
        }

        //sets the defaults for date picker
        DatePicker picker = findViewById(R.id.datePicker);
        picker.updateDate(dateValues[2],dateValues[1] - 1,dateValues[0]);
        try{
            SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
            Calendar cal = Calendar.getInstance();
            cal.setTime(df.parse(currentDate));
            picker.setMaxDate(cal.getTimeInMillis());
            cal.setTime(df.parse("01-22-2020"));
            picker.setMinDate(cal.getTimeInMillis());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //specifies googleMaps
        mMap = googleMap;
        //Create a new event listener
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            //listener on map click
            @Override
            public void onMapClick(LatLng arg0)
            {
                //only do this if the Info Text is not visible
                LinearLayout infoText = findViewById(R.id.text_box);
                if (!loading && infoText.getVisibility() == LinearLayout.GONE) {
                    //sets lat and long to a variable
                    Double taplat = arg0.latitude;
                    Double taplong = arg0.longitude;
                    tapMark = new LatLng(taplat,taplong);
                    toggleLoadingCircle(true);
                    loading = true;
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // Your logic here...

                            // When you need to modify a UI element, do so on the UI thread.
                            // 'getActivity()' is required as this is being ran from a Fragment.
                            MapsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                    changeInfo("0",false,false);
                                    toggleLoadingCircle(false);
                                    timer.purge();
                                    timer.cancel();
                                    loading = false;
                                }
                            });
                        }
                    }, 0, 5000); // End of your timer code.

                }
                }

        });
    }

}
