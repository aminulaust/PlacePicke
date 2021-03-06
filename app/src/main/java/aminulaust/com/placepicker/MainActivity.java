package aminulaust.com.placepicker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.List;

import static aminulaust.com.placepicker.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    private static final int PLACE_PICKER_REQUEST = 1000;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private GoogleApiClient mClient;
    private LocationManager locationManager;

    Button pick, currentlocation;
    TextView myaddress, myview, myattributes;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);

        pick=(Button)findViewById(R.id.mypicker);
        myview=(TextView)findViewById(R.id.myview);
        currentlocation=(Button)findViewById(R.id.mycurrentposition);
        myaddress=(TextView)findViewById(R.id.myaddress);
     // myattributes=(TextView)findViewById(R.id.myattributes);

        //button for get current location
        currentlocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean statusOfNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                findLocationLatLng(statusOfNetwork);
              
            }
        });

        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
// Button for pick a place from google map
        pick.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        checkAndRequestPermissions();

    }

    private void findLocationLatLng(boolean statusOfNetwork) {
        if (statusOfNetwork) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {


                try {

                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    // Initialize the location fields
                    if (location != null) {
                        Toast.makeText(this, "" +

                                        String.valueOf(location.getLatitude()) + "\n"
                                        + String.valueOf(location.getLongitude())
                                , Toast.LENGTH_SHORT).show();
                        myaddress.setText(String.valueOf(location.getLatitude()) + "\n"
                                + String.valueOf(location.getLongitude()));

                    } else {
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
                    }


                } catch (Exception e) {
                    Toast.makeText(this, "LOL" + e.toString(), Toast.LENGTH_SHORT).show();

                }


            }
        } else {

            // GPS provider is not enabled

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Info");
            builder.setMessage("Looks like you have not given NETWORK permissions. Please give NETWORK" +
                    " permissions or activate your cellular network  " +
                    "and return back to the app.");
            builder.setIcon(android.R.drawable.ic_dialog_alert);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                    finish();

                }

            });

            builder.show();
        }
    }
// Check run time permission
    private boolean checkAndRequestPermissions() {

        int locationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int locationPermission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (locationPermission2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
// get result from pick place
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                StringBuilder stBuilder = new StringBuilder();
                String placename = String.format("%s", place.getName());
                String latitude = String.valueOf(place.getLatLng().latitude);
                String longitude = String.valueOf(place.getLatLng().longitude);
                String address = String.format("%s", place.getAddress());
                stBuilder.append("Name: ");
                stBuilder.append(placename);
                stBuilder.append("\n");
                stBuilder.append("Latitude: ");
                stBuilder.append(latitude);
                stBuilder.append("\n");
                stBuilder.append("Logitude: ");
                stBuilder.append(longitude);
                stBuilder.append("\n");
                stBuilder.append("Address: ");
                stBuilder.append(address);
                myview.setText(stBuilder.toString());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();
    }
    @Override
    protected void onStop() {
        mClient.disconnect();
        super.onStop();
    }

}
