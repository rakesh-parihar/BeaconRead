package com.beaconread;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shawnlin.numberpicker.NumberPicker;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Transmit Activity class
 *
 * @author Rakesh
 */
public class TransmitActivity extends AppCompatActivity {

    private NumberPicker stepperMajor;
    private TextView txtDevices;
    private ProgressBar pbar;
    private AppCompatButton btnTransmit;
    private static boolean flagStart = false;
    public static final String BTN_ACTIVE = "Stop Transmitting";
    public static final String BTN_INACTIVE = "Start Transmitting";

    private List<BeaconTransmitter> beaconList = new ArrayList<>();
    private static int successCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        txtDevices = (TextView) findViewById(R.id.cap3);
        pbar = (ProgressBar) findViewById(R.id.pbar);
        stepperMajor = (NumberPicker) findViewById(R.id.stepperMajor);
        btnTransmit = (AppCompatButton) findViewById(R.id.btnStart);
        btnTransmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flagStart) {
                    //stop
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnTransmit.setEnabled(false);

                            for (int i = 0; i < beaconList.size(); i++)
                                beaconList.get(i).stopAdvertising();


                            HomeActivity.disableBluetooth();
                            HomeActivity.enableBluetooth();

                            flagStart = false;
                            setVisiblity(false);

                            btnTransmit.setEnabled(true);
                        }
                    });

                } else {
                    //start
                    flagStart = true;
                    setVisiblity(true);
                    startTransmit();
                }
            }
        });

        setVisiblity(false);

        int result = BeaconTransmitter.checkTransmissionSupported(this);
        if (result != BeaconTransmitter.SUPPORTED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.app_name));
            alert.setMessage(R.string.msg_no_support);
            alert.setNeutralButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    TransmitActivity.this.finish();
                }
            });
            alert.show();
        }


    }

    private void setVisiblity(boolean flag) {
        if (flag) {
            txtDevices.setVisibility(View.VISIBLE);
            pbar.setVisibility(View.VISIBLE);
            btnTransmit.setText(BTN_ACTIVE);
            stepperMajor.setEnabled(false);

        } else {
            txtDevices.setVisibility(View.INVISIBLE);
            pbar.setVisibility(View.INVISIBLE);
            btnTransmit.setText(BTN_INACTIVE);
            stepperMajor.setEnabled(true);

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                TransmitActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void startTransmit() {
        beaconList.clear();
        successCount = 0;
        generateBeacon(generateUUID(), stepperMajor.getValue() + "", successCount + "");
    }


    private String generateUUID() {
        return "aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa";
    }


    private void generateBeacon(String UUID, String major, String minor) {
        Beacon beacon = new Beacon.Builder()
                .setId1(UUID)
                .setId2(major)
                .setId3(minor)
                .setManufacturer(0x004C)
                .build();

        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        beaconTransmitter.setAdvertiseTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        beaconList.add(beaconTransmitter);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {


            @Override
            public void onStartFailure(int errorCode) {
                btnTransmit.setEnabled(true);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                btnTransmit.setEnabled(false);
                successCount++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtDevices.setText("Transmitting " + successCount + " beacons");
                    }
                });
                generateBeacon(generateUUID(), stepperMajor.getValue() + "", successCount + "");
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (btnTransmit.isEnabled())
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < beaconList.size(); i++)
            beaconList.get(i).stopAdvertising();
    }
}

