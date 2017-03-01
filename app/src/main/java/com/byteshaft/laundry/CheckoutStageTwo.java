package com.byteshaft.laundry;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.laundry.account.LoginActivity;
import com.byteshaft.laundry.utils.AppGlobals;
import com.byteshaft.laundry.utils.Helpers;
import com.byteshaft.laundry.utils.TimeDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.byteshaft.laundry.CheckOutActivity.sAddressId;

/**
 * Created by s9iper1 on 1/14/17.
 */

public class CheckoutStageTwo extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        DatePickerDialog.OnDateSetListener, CompoundButton.OnCheckedChangeListener {

    private Spinner selectLocation;
    private static final int PICK_LAUNDRY_MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private static final int DROP_LAUNDRY_MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private JSONArray jsonArray;
    private ArrayList<String> locationNames;
    private String addNewLocation = "add new address";
    private DatePickerDialog datePickerDialog;
    private TextView selectDropTime;
    private SwitchCompat switchCompat;
    private static final int LOCATION_OFF = 0;
    private ArrayAdapter<String> locationAdapter;
    private TextView totalPrice;
    private int totalPriceOfItems = 0;
    private int expressPrice = 0;
    private int normalPrice = 0;
    private Button sendRequest;
    private String laundryType = "normal";
    private static CheckoutStageTwo sInstance;
    private LinearLayout pickUpLayout;
    private LinearLayout deliveryTimeLayout;
    public TextView pickUpTimeText;
    public boolean pickUpTimeSelected = false;
    public boolean dropTimeSelected = false;
    public String dropDateString;
    public String dropTimeString;
    public String pickUpTimeString;
    public TextView dropTime;
    public static int sPickUpSelected = 0;
    public static int sDropSelected = 0;

    public boolean isPickUpTime = false;
    public String pickUpDateString = "";


    public static CheckoutStageTwo getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_stage_two);
        sInstance = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setTitle("Checkout");
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
        selectLocation = (Spinner) findViewById(R.id.spinner);
        deliveryTimeLayout = (LinearLayout) findViewById(R.id.layout_drop);
        selectDropTime = (TextView) findViewById(R.id.drop_time_text);
        pickUpTimeText = (TextView) findViewById(R.id.pick_up_time_text);
        switchCompat = (SwitchCompat) findViewById(R.id.express_service);
        totalPrice = (TextView) findViewById(R.id.total_price);
        sendRequest = (Button) findViewById(R.id.send_request);
        pickUpLayout = (LinearLayout) findViewById(R.id.layout_pick_up);
        dropTime = (TextView) findViewById(R.id.drop_time);
        dropTime.setText(Helpers.getTimeAndDate(true));
        pickUpTimeText.setText(Helpers.getTimeAndDate(false));

        pickUpLayout.setOnClickListener(this);
        sendRequest.setOnClickListener(this);
        switchCompat.setOnCheckedChangeListener(this);
        deliveryTimeLayout.setOnClickListener(this);
        switchCompat.setTypeface(AppGlobals.typefaceBold);
        refreshData();
        printMap(CheckOutActivity.sTotalPrice);
        selectLocation.setOnItemSelectedListener(this);

        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(CheckoutStageTwo.this, R.style.MyDialogTheme,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            totalPriceOfItems = totalPriceOfItems + Integer.valueOf(String.valueOf(pair.getValue()));
        }
        Log.i("TAG", "total " + totalPriceOfItems);
        totalPrice.setText(totalPriceOfItems + " SAR");
        normalPrice = totalPriceOfItems;
        expressPrice = totalPriceOfItems + totalPriceOfItems;
    }

    private void refreshData() {
        jsonArray = AddressesActivity.jsonArray;
        locationNames = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                locationNames.add(AddressesActivity.hashMap.get(i).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        locationNames.add(addNewLocation);
        locationAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, locationNames);
        selectLocation.setAdapter(locationAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_right_in, R.anim.anim_right_out);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.spinner:
                break;
            case R.id.layout_drop:
                datePickerDialog.setTitle("Select date");
                long addOneDay;
                if (switchCompat.isChecked()) {
                    addOneDay = TimeUnit.MILLISECONDS.convert(0, TimeUnit.DAYS);
                } else {
                    addOneDay = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
                }
                long tenDays = TimeUnit.MILLISECONDS.convert(10, TimeUnit.DAYS);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() + addOneDay);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() + tenDays);
                datePickerDialog.show();
                break;
            case R.id.layout_pick_up:
                isPickUpTime = true;
                datePickerDialog.show();
                break;
            case R.id.send_request:
                if (!pickUpTimeSelected) {
                    Toast.makeText(this, "please select pickup time", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!dropTimeSelected) {
                    Toast.makeText(this, "please select drop time", Toast.LENGTH_SHORT).show();
                    return;
                }
                CheckOutActivity.getInstance().sendData(pickUpDateString + " " + pickUpTimeString
                        , dropDateString + " " + dropTimeString, laundryType);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOCATION_OFF:
                if (locationEnabled()) {
                    startActivity(new Intent(getApplicationContext(), PickLDropLaundryActivity.class));
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PICK_LAUNDRY_MY_PERMISSIONS_REQUEST_LOCATION:
            case DROP_LAUNDRY_MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (requestCode == PICK_LAUNDRY_MY_PERMISSIONS_REQUEST_LOCATION) {
                        if (!locationEnabled()) {
                            // notify user
                            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                            dialog.setMessage("Location is not enabled");
                            dialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    // TODO Auto-generated method stub
                                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(myIntent, LOCATION_OFF);
                                    //get gps
                                }
                            });
                            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    // TODO Auto-generated method stub

                                }
                            });
                            dialog.show();
                        } else {
                            startActivity(new Intent(getApplicationContext(), PickLDropLaundryActivity.class));
                        }
                    } else {
                        if (!locationEnabled()) {
                            // notify user
                            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                            dialog.setMessage("Location is not enabled");
                            dialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    // TODO Auto-generated method stub
                                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(myIntent);
                                    //get gps
                                }
                            });
                            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    // TODO Auto-generated method stub

                                }
                            });
                            dialog.show();
                        } else {
                            startActivity(new Intent(getApplicationContext(), PickLDropLaundryActivity.class));
                        }
                    }


                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private boolean locationEnabled() {
        LocationManager lm = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        return gps_enabled || network_enabled;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (locationNames.get(i).equals(addNewLocation)) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        PICK_LAUNDRY_MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                if (!locationEnabled()) {
                    // notify user
                    String message;
                    String title;
                    if (jsonArray.length() < 1) {
                        message = "Location is not enabled. \nPlease add your address.";
                        title = "Note";
                    } else {
                        message = "Location is not enabled.";
                        title = "Location";
                    }
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle(title);
                    dialog.setMessage(message);
                    dialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(myIntent, LOCATION_OFF);
                            //get gps
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub

                        }
                    });
                    dialog.show();
                } else {
                    if (AppGlobals.isUserLoggedIn()) {
                        startActivity(new Intent(getApplicationContext(), PickLDropLaundryActivity.class));
                    } else {
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setTitle("Not logged in !");
                        alertDialogBuilder.setMessage("Do you want to login?");
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            }
                        });
                        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
            }
        } else {
            try {
                sAddressId = AddressesActivity.hashMap.get(i).getInt("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        Log.i("TAG", "year " + i + "month " + i1 + "date " + i2);
        if (isPickUpTime) {
            isPickUpTime = false;
            pickUpDateString = i2 + "-" + (i1 + 1) + "-" + i;
            if (pickUpDateString.trim().isEmpty()) {
                Toast.makeText(this, "please select date", Toast.LENGTH_SHORT).show();
                return;
            } else {
                TimeDialog timeDialog = new TimeDialog(CheckoutStageTwo.this, R.style.MyDialogTheme,
                        0);
                timeDialog.show();
            }
        } else {
            dropDateString = i2 + "-" + (i1 + 1) + "-" + i;
            if (dropDateString.trim().isEmpty()) {

                Toast.makeText(this, "please select date", Toast.LENGTH_SHORT).show();
                return;
            } else {
                TimeDialog timeDialog = new TimeDialog(CheckoutStageTwo.this, R.style.MyDialogTheme,
                        1);
                timeDialog.show();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            laundryType = "express";
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Express Laundry");
            alertDialogBuilder.setMessage("Express laundry service will allow you to get your laundry done rapidly." +
                    "Minimum drop time you can select is 2 hours. \"Double cost applied!!\"").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            selectDropTime.setText("Select Drop Time(Minimum 2 hours after pickup)");
            totalPrice.setText(expressPrice + " SAR");
            dropTime.setText(Helpers.getTimeAndDate(false));
        } else {
            laundryType = "normal";
            selectDropTime.setText("Select Drop Time(Must be between 14:00 - 20:00)");
            totalPrice.setText((normalPrice) + " SAR");
            dropTime.setText(Helpers.getTimeAndDate(true));
        }
    }
}
