package com.beaconread;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.beaconread.adapter.ListAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * List Activity of emulated beacons
 *
 * @author Rakesh
 */
public class ListActivity extends AppCompatActivity {

    private AppCompatButton btnAdd;
    private EditText edtUUID;
    private ListView listView;
    private ListAdapter adapter;
    private List<String> deviceID = new ArrayList<>();
    private Gson gson;
    private SharedPreferences prefs;
    public static final TypeToken<List<String>> token = new TypeToken<List<String>>() {
    };
    private static boolean isFormatting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        prefs = getSharedPreferences("myPrefs", 0);
        gson = new Gson();
        edtUUID = (EditText) findViewById(R.id.edtUUID);
        edtUUID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isFormatting) {
                    formatText(s.toString(), count < before);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        listView = (ListView) findViewById(R.id.listview);
        adapter = new ListAdapter(this, deviceID);
        listView.setAdapter(adapter);

        btnAdd = (AppCompatButton) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtUUID.getText().length() == 36) {
                    if (!deviceID.contains(edtUUID.getText().toString())) {
                        if (checkValidUUID(edtUUID.getText().toString())) {
                            deviceID.add(edtUUID.getText().toString());
                            saveData();
                            adapter.notifyDataSetChanged();
                        } else {
                            edtUUID.setError("Invalid UUID");
                            edtUUID.requestFocus();
                        }

                    } else {
                        edtUUID.setError("UUID Already Exist");
                        edtUUID.requestFocus();
                    }
                } else {
                    edtUUID.setError("Invalid UUID");
                    edtUUID.requestFocus();
                }

            }
        });

        deviceID.clear();
        retriveData();

    }

    /**
     * Method to format entered text
     *
     * @param str      input
     * @param isDelete true if value is deleted
     */
    private void formatText(String str, boolean isDelete) {
        isFormatting = true;
        String temp = str;
        int size = str.length();
        if (size == 8)
            temp = temp + "-";
        else if (size == 13)
            temp = temp + "-";
        else if (size == 18)
            temp = temp + "-";
        else if (size == 23)
            temp = temp + "-";
        if (isDelete && (size == 8 || size == 13 || size == 18 | +size == 23))
            temp = temp.substring(0, temp.length() - 2);

        edtUUID.setText(temp);
        edtUUID.setSelection(edtUUID.getText().length());
        isFormatting = false;
    }


    /**
     * Method to remove item
     *
     * @param pos position of item to remove
     */
    public void removeItem(int pos) {
        deviceID.remove(pos);
        saveData();
    }

    /**
     * Method to cache data in preferences
     */
    private void saveData() {
        prefs.edit().putString("list", gson.toJson(deviceID, token.getType())).apply();
    }

    /**
     * Method to retrieve saved data
     */
    private void retriveData() {
        deviceID.clear();
        ArrayList<String> temp;
        temp = gson.fromJson(prefs.getString("list", ""), token.getType());
        deviceID.addAll(temp == null ? new ArrayList<String>() : temp);
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ListActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Method to check for UUID
     *
     * @param uuid input
     * @return true or false
     */
    private boolean checkValidUUID(String uuid) {

        try {
            Identifier.parse(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
