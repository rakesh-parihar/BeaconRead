package com.beaconread;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.HashSet;

/**
 * Receiver Activity class
 *
 * @author Rakesh
 */
public class ReceiverActivity extends AppCompatActivity implements BeaconConsumer {

    private BeaconManager beaconManager;
    private SwitchCompat swh;
    private TextView txtCount;
    private HashSet<Beacon> devices = new HashSet<>();
    private Button btn;
    private ProgressBar pbar;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        prefs = getSharedPreferences("myPrefs", 0);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        devices.clear();
        pbar = (ProgressBar) findViewById(R.id.pbar);
        pbar.setVisibility(View.INVISIBLE);
        btn = (Button) findViewById(R.id.btn);
        btn.setVisibility(View.INVISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtCount.setText("Detected \n0");
                devices.clear();
            }
        });


        txtCount = (TextView) findViewById(R.id.txtCount);
        txtCount.setVisibility(View.INVISIBLE);
        txtCount.setText("Detected \n0");

        swh = (SwitchCompat) findViewById(R.id.swh);
        swh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!swh.isChecked()) {
                    setVisiblity(false);
                    beaconManager.unbind(ReceiverActivity.this);
                } else {

                    init();

                }
            }
        });



/*
        Beacon	Layout
        ALTBEACON	    m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25
        IBEACON	        m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24
        EDDYSTONE  TLM	x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15
        EDDYSTONE  UID	s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19
        EDDYSTONE  URL	s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v
        URI_BEACON_LAYOUT = "s:0-1=fed8,m:2-2=00,p:3-3:-41,i:4-21v";
*/


    }


    /**
     * Init views and variables
     */
    private void init() {
        setVisiblity(true);
        devices.clear();
        txtCount.setText("Detected \n0");

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        beaconManager.bind(ReceiverActivity.this);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        swh.setChecked(false);
        setVisiblity(false);
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        startMonitoring();
    }


    RangeNotifier rangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {

            devices.addAll(collection);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtCount.setText("Detected \n" + devices.size());
                }
            });

        }
    };


    private void startMonitoring() {
        try {
            beaconManager.addRangeNotifier(rangeNotifier);
            beaconManager.startRangingBeaconsInRegion(new Region("myBeaons", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void updateList(final Collection<Beacon> beacons) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                for (Beacon oneBeacon : beacons) {
                    String str = oneBeacon.getId1() + "_" + oneBeacon.getId2() + "_" + oneBeacon.getId3();
                    Log.d("test", "UUID=" + str);
                    txtCount.setText("Detected \n" + devices.size());
                }

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ReceiverActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void setVisiblity(boolean isVisible) {
        int mode = isVisible ? View.VISIBLE : View.INVISIBLE;
        pbar.setVisibility(mode);
        txtCount.setVisibility(mode);
        btn.setVisibility(mode);
    }
}