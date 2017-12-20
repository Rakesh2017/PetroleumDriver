package enhabyto.com.petroleumdriver;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tapadoo.alerter.Alerter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import dmax.dialog.SpotsDialog;
import mehdi.sakout.fancybuttons.FancyButton;
import util.android.textviews.FontTextView;

/**
 * Created by this on 06-Dec-17.
 */

public class TripSchedules extends AppCompatActivity{

    FancyButton home_btn;
    String contactUID_tx;

    DatabaseReference dataRef_spinner = FirebaseDatabase.getInstance().getReference();
    String selected_truck_tx, selected_truckLocation_tx, selected_expectedStoppage_tx;
    EditText expensesTaken_et, cityName_et, fuelPrice_et, fuelTaken_et;
    AutoCompleteTextView stateName_et;
    String truckNumber_tx, truckLocation_tx, expensesTaken_tx, expected_stoppage_tx
            , stateName_tx, cityName_tx, fuelPrice_tx, tripStartLocation_tx, day, month, year
            , fuelTaken_tx, scheduleHeader_tx, scheduler_contact;

    FontTextView tripStartDate_tv, scheduleHeader_tv;
    AlertDialog dialog_scheduleTrip, dial_rejectTrip;

    AutoCompleteTextView truckNumber_act, truckLocation_act, expectedStoppage_act, tripStartLocation_act;

    RadioRealButtonGroup choiceGroup;

    Spinner spinner_truckNumber, spinner_truckLocation, spinner_expectedStoppage, spinner_tripLocation;

    final String [] states = new String[]{"Andra Pradesh","Arunachal Pradesh","Assam","Bihar","Chhattisgarh","Goa","Gujarat"
            ,"Haryana","Himachal Pradesh","Jammu and Kashmir","Jharkhand","Karnataka","Kerala","Madya Pradesh","Maharashtra"
            ,"Manipur","Meghalaya","Mizoram","Nagaland","Orissa","Punjab","Rajasthan","Sikkim"
            ,"Tamil Nadu","Tripura","Uttaranchal","Uttar Pradesh","West Bengal","Delhi","Lakshadeep","Pondicherry"
            ,"Andaman and Nicobar","Chandigarh","Dadar and Nagar","Daman and Diu"};


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.trip_schedules);

        home_btn = findViewById(R.id.schedule_backButton);
        spinner_expectedStoppage = findViewById(R.id.schedule_spinnerExpectedStoppage);
        spinner_truckLocation = findViewById(R.id.schedule_spinnerTruckLocation);
        spinner_truckNumber = findViewById(R.id.schedule_spinnerTruckNumber);
        spinner_tripLocation = findViewById(R.id.schedule_spinnerStartLocation);
        fuelPrice_et = findViewById(R.id.schedule_fuelPriceEditText);
        truckLocation_act = findViewById(R.id.schedule_truckLocationEditText);
        truckNumber_act = findViewById(R.id.schedule_truckNumberEditText);
        expectedStoppage_act = findViewById(R.id.schedule_expectedStoppageEditText);
        stateName_et = findViewById(R.id.schedule_stateNameEditText);
        cityName_et = findViewById(R.id.schedule_cityNameEditText);
        expensesTaken_et = findViewById(R.id.schedule_expensesTakenEditText);
        tripStartLocation_act = findViewById(R.id.schedule_startLocationEditText);
        tripStartDate_tv = findViewById(R.id.schedule_startDateTextView);
        fuelTaken_et = findViewById(R.id.taken_fuelTakenEditText);
        scheduleHeader_tv = findViewById(R.id.schedule_headerTextView);

        choiceGroup = findViewById(R.id.schedule_choice);
        dialog_scheduleTrip = new SpotsDialog(TripSchedules.this, R.style.dialog_schedulingTrip);
        dial_rejectTrip = new SpotsDialog(TripSchedules.this, R.style.dialog_rejectTrip);


        choiceGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {


            if (position == 1){  //  accept
                    dialog_scheduleTrip.show();
                    truckNumber_tx = truckNumber_act.getText().toString().trim();
                    truckLocation_tx = truckLocation_act.getText().toString().trim();
                    expected_stoppage_tx = expectedStoppage_act.getText().toString().trim();
                    fuelPrice_tx = fuelPrice_et.getText().toString().trim();
                    fuelTaken_tx = fuelTaken_et.getText().toString().trim();
                    stateName_tx = stateName_et.getText().toString().trim();
                    cityName_tx = cityName_et.getText().toString().trim();
                    expensesTaken_tx = expensesTaken_et.getText().toString().trim();
                    tripStartLocation_tx = tripStartLocation_act.getText().toString().trim();

                    if (TextUtils.equals(truckNumber_tx, "")){
                        Alerter.create(TripSchedules.this)
                                .setTitle("Enter truck Number!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();
                        dialog_scheduleTrip.dismiss();
                        return;
                    }

                    if (TextUtils.equals(expected_stoppage_tx, "")){
                        Alerter.create(TripSchedules.this)
                                .setTitle("Enter Expected Stoppage!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();
                        dialog_scheduleTrip.dismiss();
                        return;
                    }

                if (!fuelTaken_tx.isEmpty()){
                    if (fuelPrice_tx.isEmpty() ){
                        Alerter.create(TripSchedules.this)
                                .setTitle("Enter Fuel Price!")
                                .setText("You have to provide the Price of the fuel")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();
                        dialog_scheduleTrip.dismiss();
                        return;
                    }
                }

                if (!fuelPrice_tx.isEmpty()){
                    if (fuelTaken_tx.isEmpty() ){
                        Alerter.create(TripSchedules.this)
                                .setTitle("Enter Fuel Taken")
                                .setText("You have to provide the fuel filled in Truck")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();
                        dialog_scheduleTrip.dismiss();
                        return;
                    }
                }

                new MaterialDialog.Builder(TripSchedules.this)
                        .title("Confirm Trip Details")
                        .content("Truck Number: "+ truckNumber_tx
                                + "\n\nTruck Location: " + truckLocation_tx
                                + "\n\nTrip Start Location: " + tripStartLocation_tx
                                + "\n\nExpected Stoppage: "+ expected_stoppage_tx
                                + "\n\nFuel Price: Rs"+ fuelPrice_tx +"/Litres"
                                + "\n\nMoney Taken: Rs"+ expensesTaken_tx
                                + "\n\nState Name: "+ stateName_tx
                                + "\n\nCity Name: "+ cityName_tx
                        )
                        .positiveText("Yes")
                        .contentColor(getResources().getColor(R.color.blackNinety))
                        .positiveColor(getResources().getColor(R.color.lightGreen))
                        .negativeText("No")
                        .negativeColor(getResources().getColor(R.color.lightRed))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {

                                if(!isNetworkAvailable()){
                                    Alerter.create(TripSchedules.this)
                                            .setTitle("No Internet Connection!")
                                            .setContentGravity(1)
                                            .setBackgroundColorRes(R.color.black)
                                            .setIcon(R.drawable.no_internet)
                                            .show();
                                    dialog_scheduleTrip.dismiss();
                                    return;
                                }

                                Calendar c = Calendar.getInstance();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
                                SimpleDateFormat sdf1 = new SimpleDateFormat("HH_mm");
                                final  String actualDate = sdf.format(c.getTime());
                                final  String actualTime = sdf1.format(c.getTime());

                                final String key = FirebaseDatabase.getInstance().getReference().push().getKey();

                                DatabaseReference d_schedule_trip = dataRef_spinner.child("trip_details").child(contactUID_tx).child(key).child("start_trip");

                                d_schedule_trip.child("truck_number").setValue(truckNumber_tx);
                                d_schedule_trip.child("truck_location").setValue(truckLocation_tx);
                                d_schedule_trip.child("expenses_taken").setValue(expensesTaken_tx);
                                d_schedule_trip.child("expected_stoppage").setValue(expected_stoppage_tx);
                                d_schedule_trip.child("fuel_price").setValue(fuelPrice_tx);
                                d_schedule_trip.child("state_name").setValue(stateName_tx);
                                d_schedule_trip.child("city_name").setValue(cityName_tx);
                                d_schedule_trip.child("status").setValue("active");
                                d_schedule_trip.child("start_date").setValue(actualDate+"_"+actualTime);
                                d_schedule_trip.child("start_location").setValue(tripStartLocation_tx);
                                d_schedule_trip.child("fuel_taken").setValue(fuelTaken_tx);

                                dataRef_spinner.child("driver_profiles").child(contactUID_tx).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        dataRef_spinner.child("trip_status").child(key).child("driverContact").setValue(contactUID_tx);
                                        dataRef_spinner.child("trip_status").child(key).child("driverName").setValue(dataSnapshot.getValue(String.class));
                                        dataRef_spinner.child("trip_status").child(key).child("tripStarted").setValue(actualDate+"_"+actualTime);
                                        dataRef_spinner.child("trip_status").child(key).child("truckNumber").setValue(truckNumber_tx);

                                        Query query = dataRef_spinner.child("trip_schedules_admin").child(scheduler_contact).child(contactUID_tx).orderByKey().limitToLast(1);

                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for(DataSnapshot child : dataSnapshot.getChildren()){
                                                    final String key = child.getKey();
                                                    dataRef_spinner.child("trip_schedules_admin").child(scheduler_contact).child(contactUID_tx).child(key)
                                                            .child("status").setValue("accepted");

                                                    dataRef_spinner.child("trip_schedules_driver").child(contactUID_tx).setValue(null);

                                                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                                    if (notificationManager != null) {
                                                        notificationManager.cancelAll();
                                                    }
                                                    finish();
                                                    startActivity(new Intent(TripSchedules.this, DashBoard.class));
                                                    dialog_scheduleTrip.dismiss();



                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                dialog_scheduleTrip.dismiss();
                                            }
                                        });





                                    }



                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        dialog_scheduleTrip.dismiss();
                                    }
                                });




                                // TODO
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                // TODO
                                dialog_scheduleTrip.dismiss();

                            }
                        }) .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        dialog_scheduleTrip.dismiss();
                    }
                })
                        .show();

                }

             if (position == 0){

                 new MaterialDialog.Builder(TripSchedules.this)
                         .title("Are you sure to reject this Trip!")
                         .positiveText("Yes")
                         .contentColor(getResources().getColor(R.color.blackNinety))
                         .positiveColor(getResources().getColor(R.color.lightGreen))
                         .negativeText("No")
                         .negativeColor(getResources().getColor(R.color.lightRed))
                         .onPositive(new MaterialDialog.SingleButtonCallback() {
                             @Override
                             public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                 dial_rejectTrip.show();
                                 if(!isNetworkAvailable()){
                                     Alerter.create(TripSchedules.this)
                                             .setTitle("No Internet Connection!")
                                             .setContentGravity(1)
                                             .setBackgroundColorRes(R.color.black)
                                             .setIcon(R.drawable.no_internet)
                                             .show();
                                     dial_rejectTrip.dismiss();
                                     return;
                                 }

                                 Query query = dataRef_spinner.child("trip_schedules_admin").child(scheduler_contact).child(contactUID_tx).orderByKey().limitToLast(1);

                                 query.addListenerForSingleValueEvent(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(DataSnapshot dataSnapshot) {
                                         for(DataSnapshot child : dataSnapshot.getChildren()){
                                             final String key = child.getKey();

                                             new Handler().postDelayed(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     dataRef_spinner.child("trip_schedules_admin").child(scheduler_contact).child(contactUID_tx).child(key)
                                                             .child("status").setValue("rejected");

                                                     dataRef_spinner.child("trip_schedules_driver").child(contactUID_tx).setValue(null);

                                                     Alerter.create(TripSchedules.this)
                                                             .setTitle("Trip Rejected!")
                                                             .setContentGravity(1)
                                                             .setBackgroundColorRes(R.color.black)
                                                             .setIcon(R.drawable.success_icon)
                                                             .show();
                                                     NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                                     if (notificationManager != null) {
                                                         notificationManager.cancelAll();
                                                     }
                                                     startActivity(new Intent(TripSchedules.this, DashBoard.class));
                                                     dial_rejectTrip.dismiss();

                                                 }
                                             },2000);


                                         }
                                     }

                                     @Override
                                     public void onCancelled(DatabaseError databaseError) {
                                         dial_rejectTrip.dismiss();
                                     }
                                 });


                                 // TODO
                             }
                         })
                         .onNegative(new MaterialDialog.SingleButtonCallback() {
                             @Override
                             public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                 // TODO
                                 dial_rejectTrip.dismiss();

                             }
                         }) .onNeutral(new MaterialDialog.SingleButtonCallback() {
                     @Override
                     public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                         // TODO
                         dial_rejectTrip.dismiss();
                     }
                 })
                         .show();



             }

            }
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (TripSchedules.this,android.R.layout.simple_list_item_1,states);
        stateName_et.setAdapter(adapter);

        spinner_truckNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    spinner_truckNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {

                            selected_truck_tx = spinner_truckNumber.getSelectedItem().toString();
                            if (!TextUtils.equals(selected_truck_tx,"Select Truck")){
                                truckNumber_act.setText(selected_truck_tx);
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TODO Auto-generated method stub

                        }
                    });

                }
                return false;
            }
        });


        spinner_truckLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    spinner_truckLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {

                            selected_truckLocation_tx = spinner_truckLocation.getSelectedItem().toString();
                            if (!TextUtils.equals(selected_truckLocation_tx,"Select Location")){
                                truckLocation_act.setText(selected_truckLocation_tx);
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TODO Auto-generated method stub

                        }
                    });

                }
                return false;
            }
        });


        spinner_tripLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    spinner_tripLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {

                            selected_truckLocation_tx = spinner_tripLocation.getSelectedItem().toString();
                            if (!TextUtils.equals(selected_truckLocation_tx,"Select Location")){
                                tripStartLocation_act.setText(selected_truckLocation_tx);
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TODO Auto-generated method stub

                        }
                    });

                }
                return false;
            }
        });



        spinner_expectedStoppage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    spinner_expectedStoppage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {

                            selected_expectedStoppage_tx = spinner_expectedStoppage.getSelectedItem().toString();
                            if (!TextUtils.equals(selected_expectedStoppage_tx,"Select Location")){
                                expectedStoppage_act.setText(selected_expectedStoppage_tx);
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TODO Auto-generated method stub

                        }
                    });

                }
                return false;
            }
        });




            home_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TripSchedules.this , DashBoard.class);
                    startActivity(intent);
                }
            });
        }


        public void onStart(){
        super.onStart();

            SharedPreferences shared = getSharedPreferences("firstLog", MODE_PRIVATE);
            contactUID_tx = (shared.getString("contact_uid", ""));

            dataRef_spinner.child("trip_schedules_driver").child(contactUID_tx)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        truckNumber_tx = dataSnapshot.child("truck_number").getValue(String.class);
                                        truckLocation_tx = dataSnapshot.child("truck_location").getValue(String.class);
                                        expected_stoppage_tx = dataSnapshot.child("next_stoppage").getValue(String.class);
                                        tripStartLocation_tx = dataSnapshot.child("start_point").getValue(String.class);
                                        scheduleHeader_tx = dataSnapshot.child("scheduler_name").getValue(String.class);
                                        scheduler_contact = dataSnapshot.child("scheduler_contact").getValue(String.class);

                                        String date = dataSnapshot.child("expected_start_date").getValue(String.class);
                                        String time = dataSnapshot.child("expected_start_time").getValue(String.class);

                                        try {
                                            day = TextUtils.substring(date, 0, 2);
                                            month = TextUtils.substring(date, 3, 5);
                                            year = TextUtils.substring(date, 6, 10);
                                            conversion();
                                            if (!TextUtils.equals(date,"") || !TextUtils.equals(date,null) ){
                                                tripStartDate_tv.setVisibility(View.VISIBLE);
                                                tripStartDate_tv.setText("Trip Start on "+day+"-"+month+"-"+year+" "+time);
                                            }
                                        }
                                        catch (NullPointerException | ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e){
                                            e.printStackTrace();
                                        }



                                        truckNumber_act.setText(truckNumber_tx);
                                        truckLocation_act.setText(truckLocation_tx);
                                        expectedStoppage_act.setText(expected_stoppage_tx);
                                        tripStartLocation_act.setText(tripStartLocation_tx);

                                        scheduleHeader_tv.setText("This Trip is made by ("+scheduleHeader_tx+"). You can change the values as per your Trip.");




                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });



            dataRef_spinner.child("truck_details").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    try {
                        final List<String> areas = new ArrayList<String>();
                        areas.add("Select Truck");
                        for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                            String areaName = areaSnapshot.child("truck_number").getValue(String.class);
                            areas.add(areaName);

                        }
                        ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(TripSchedules.this, android.R.layout.simple_spinner_item, areas);
                        ArrayAdapter<String> areasAdapter1 = new ArrayAdapter<String>(TripSchedules.this, android.R.layout.simple_list_item_1, areas);

                        areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_truckNumber.setAdapter(areasAdapter);
                        truckNumber_act.setAdapter(areasAdapter1);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            });


            dataRef_spinner.child("pump_details").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    try {
                        final List<String> areas = new ArrayList<>();
                        areas.add("Select Location");
                        for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                            String pump_name = areaSnapshot.child("pump_name").getValue(String.class);
                            areas.add(pump_name);

                        }

                        ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(TripSchedules.this, android.R.layout.simple_spinner_item, areas);
                        ArrayAdapter<String> areasAdapter1 = new ArrayAdapter<String>(TripSchedules.this, android.R.layout.simple_list_item_1, areas);

                        areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_truckLocation.setAdapter(areasAdapter);
                        truckLocation_act.setAdapter(areasAdapter1);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            });


            dataRef_spinner.child("pump_details").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    try {
                        final List<String> areas = new ArrayList<>();
                        areas.add("Select Location");
                        for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                            String pump_name = areaSnapshot.child("pump_name").getValue(String.class);
                            areas.add(pump_name);

                        }

                        ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(TripSchedules.this, android.R.layout.simple_spinner_item, areas);
                        ArrayAdapter<String> areasAdapter1 = new ArrayAdapter<String>(TripSchedules.this, android.R.layout.simple_list_item_1, areas);

                        areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_expectedStoppage.setAdapter(areasAdapter);
                        expectedStoppage_act.setAdapter(areasAdapter1);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            });

            dataRef_spinner.child("pump_details").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    try {
                        final List<String> areas = new ArrayList<>();
                        areas.add("Select Location");
                        for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                            String pump_name = areaSnapshot.child("pump_name").getValue(String.class);
                            areas.add(pump_name);

                        }

                        ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(TripSchedules.this, android.R.layout.simple_spinner_item, areas);
                        ArrayAdapter<String> areasAdapter1 = new ArrayAdapter<String>(TripSchedules.this, android.R.layout.simple_list_item_1, areas);

                        areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_tripLocation.setAdapter(areasAdapter);
                        tripStartLocation_act.setAdapter(areasAdapter1);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            });
        }

    private void conversion() {

        switch (month) {

            case "1":
                month = "Jan";
                break;

            case "2":
                month = "Feb";
                break;

            case "3":
                month = "Mar";
                break;

            case "4":
                month = "Apr";
                break;

            case "5":
                month = "May";
                break;

            case "6":
                month = "Jun";
                break;

            case "7":
                month = "Jul";
                break;

            case "8":
                month = "Aug";
                break;

            case "9":
                month = "Sep";
                break;

            case "10":
                month = "Oct";
                break;

            case "11":
                month = "Nov";
                break;

            case "12":
                month = "Dec";
                break;


        }
    }

    //    internet check
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if ( dialog_scheduleTrip!=null && dialog_scheduleTrip.isShowing() ){
            dialog_scheduleTrip.dismiss();
        }
    }



}
