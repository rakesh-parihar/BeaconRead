package com.beaconread;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Home Activity Class
 *
 * @author Rakesh
 */
public class HomeActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String[] LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int RC_LOCATION_CONTACTS_PERM = 124;
    private LinearLayout transmit, receive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (!isBluetoothEnabled())
            enableBluetooth();

        transmit = findViewById(R.id.transmit);
        receive = findViewById(R.id.receive);
        transmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasLocationAndContactsPermissions()) {
                    locationAndContactsTask();
                } else {
                    if (!isBluetoothEnabled()) {
                        enableBluetooth();
                    } else {

                        if (checkLocationServices())
                            startActivity(new Intent(HomeActivity.this, TransmitActivity.class));

                    }
                }
            }

        });

        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasLocationAndContactsPermissions()) {
                    locationAndContactsTask();
                } else {
                    if (!isBluetoothEnabled()) {
                        enableBluetooth();
                    } else {
                        if (checkLocationServices())
                            startActivity(new Intent(HomeActivity.this, ReceiverActivity.class));
                    }
                }

            }
        });


        locationAndContactsTask();
        checkLocationServices();

    }

    private boolean hasLocationAndContactsPermissions() {
        return EasyPermissions.hasPermissions(this, LOCATION);
    }

    @AfterPermissionGranted(RC_LOCATION_CONTACTS_PERM)
    public void locationAndContactsTask() {
        if (!hasLocationAndContactsPermissions()) {
            EasyPermissions.requestPermissions(this, getString(R.string.str_msg), RC_LOCATION_CONTACTS_PERM, LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }


    /**
     * Method to check if bluetooth is on or off
     *
     * @return true if enabled
     */
    public static boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enable :)
            return false;
        } else {
            return true;
        }

    }

    /**
     * Method to enable bluetooth
     */
    public static void enableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    /**
     * Method to disable bluetooth
     */
    public static void disableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }


    /**
     * Method to check location service
     *
     * @return true if enabled
     */
    public Boolean checkLocationServices() {
        try {

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                settingsRequest();
                return false;
            } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Enable location service
     */
    public void settingsRequest() {

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(HomeActivity.this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(HomeActivity.this, 99);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

}
