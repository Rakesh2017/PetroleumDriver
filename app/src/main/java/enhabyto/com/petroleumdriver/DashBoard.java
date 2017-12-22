package enhabyto.com.petroleumdriver;


import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tapadoo.alerter.Alerter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import br.com.goncalves.pugnotification.notification.PugNotification;
import dmax.dialog.SpotsDialog;
import mehdi.sakout.fancybuttons.FancyButton;
import util.android.textviews.FontTextView;

public class DashBoard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    AlertDialog dialog_logging_out;
    String contactUID_tx, connected;

    FontTextView contact_uid_tv, user_name_tv;
    private DatabaseReference d_root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference d_profile = d_root.child("driver_profiles");
    private DatabaseReference d_schedule_trip;
    private ImageView nav_profileImageView;

    String day, month, year, hour, minute;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://rajpetroleum-4d3fa.appspot.com/");
    FancyButton startNewTrip_btn, cancelTrip_btn, startTrip_btn, back_btn;
    RelativeLayout startNewTrip_rl, startTrip_rl, tripPage_rl;

    FancyButton stoppage_btn, petrolFilling_btn, otherFilling_btn, load_btn
            , breakDown_btn, endTrip_btn;

    AutoCompleteTextView truckNumber_act, truckLocation_act, expectedStoppage_act, tripStartLocation_act;

    Spinner spinner_truckNumber, spinner_truckLocation, spinner_expectedStoppage, spinner_tripLocation;

    DatabaseReference d_networkStatus = FirebaseDatabase.getInstance().getReference().child("checkNetwork").child("isConnected");
    DatabaseReference dataRef_spinner = FirebaseDatabase.getInstance().getReference();
    String selected_truck_tx, selected_truckLocation_tx, selected_expectedStoppage_tx, fuelTaken_tx;
    EditText expensesTaken_et, cityName_et, fuelPrice_et, fuelTaken_et;
    AutoCompleteTextView stateName_et;
    String truckNumber_tx, truckLocation_tx, expensesTaken_tx, expected_stoppage_tx, stateName_tx, cityName_tx, fuelPrice_tx, tripStartLocation_tx;
    AlertDialog dialog_scheduleTrip, dialog_checkingStatus, dialog_updatingData;
    private String actualDate, actualTime;
    String trip_check;
    FontTextView header_text;

    TextView noInternet;
    private Context context;

    int counter = 1;


    final String [] states = new String[]{"Andra Pradesh","Arunachal Pradesh","Assam","Bihar","Chhattisgarh","Goa","Gujarat"
            ,"Haryana","Himachal Pradesh","Jammu and Kashmir","Jharkhand","Karnataka","Kerala","Madya Pradesh","Maharashtra"
            ,"Manipur","Meghalaya","Mizoram","Nagaland","Orissa","Punjab","Rajasthan","Sikkim"
            ,"Tamil Nadu","Tripura","Uttaranchal","Uttar Pradesh","West Bengal","Delhi","Lakshadeep","Pondicherry"
            ,"Andaman and Nicobar","Chandigarh","Dadar and Nagar","Daman and Diu"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        if (ActivityCompat.checkSelfPermission(DashBoard.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DashBoard.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DashBoard.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }


        this.context = this;

        Intent alarm = new Intent(this.context, AlarmReceiver.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(this.context, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null);
        if(!alarmRunning) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, alarm, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 300000, pendingIntent);
            }
        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ImageButton logout_btn = findViewById(R.id.app_bar_dash_logoutButton);
        logout_btn.setOnClickListener(this);



        nav_profileImageView = navigationView.getHeaderView(0).findViewById(R.id.header_profile_image);
        contact_uid_tv = navigationView.getHeaderView(0).findViewById(R.id.header_uid);
        user_name_tv = navigationView.getHeaderView(0).findViewById(R.id.header_name);

        startNewTrip_btn = findViewById(R.id.dash_startNewTripButton);
        startTrip_btn = findViewById(R.id.dash_startTripButton);
        startNewTrip_rl = findViewById(R.id.dash_relativeLayout1);
        startTrip_rl = findViewById(R.id.dash_relativeLayout2);
        cancelTrip_btn = findViewById(R.id.dash_cancelTripButton);
        spinner_expectedStoppage = findViewById(R.id.dash_spinnerExpectedStoppage);
        spinner_truckLocation = findViewById(R.id.dash_spinnerTruckLocation);
        spinner_truckNumber = findViewById(R.id.dash_spinnerTruckNumber);
        spinner_tripLocation = findViewById(R.id.dash_spinnerStartLocation);
        fuelPrice_et = findViewById(R.id.dash_fuelPriceEditText);
        truckLocation_act = findViewById(R.id.dash_truckLocationEditText);
        truckNumber_act = findViewById(R.id.dash_truckNumberEditText);
        expectedStoppage_act = findViewById(R.id.dash_expectedStoppageEditText);
        stateName_et = findViewById(R.id.dash_stateNameEditText);
        cityName_et = findViewById(R.id.dash_cityNameEditText);
        expensesTaken_et = findViewById(R.id.dash_expensesTakenEditText);
        tripPage_rl = findViewById(R.id.dash_relativeLayout3);
        header_text = findViewById(R.id.dash_tripDateTextView);
        back_btn = findViewById(R.id.dash_backButton);
        noInternet = findViewById(R.id.no_internet);
        fuelTaken_et = findViewById(R.id.taken_fuelPriceEditText);

        tripStartLocation_act = findViewById(R.id.dash_startLocationEditText);

        stoppage_btn = findViewById(R.id.dash_stoppageButton);
        petrolFilling_btn = findViewById(R.id.dash_petrolFillingButton);
        otherFilling_btn = findViewById(R.id.dash_otherFillingButton);
        load_btn = findViewById(R.id.dash_loadButton);
        breakDown_btn = findViewById(R.id.dash_breakDownButton);
        endTrip_btn = findViewById(R.id.dash_endTripButton);


        startNewTrip_btn.setOnClickListener(this);
        startTrip_btn.setOnClickListener(this);
        cancelTrip_btn.setOnClickListener(this);
        petrolFilling_btn.setOnClickListener(this);
        otherFilling_btn.setOnClickListener(this);
        load_btn.setOnClickListener(this);
        breakDown_btn.setOnClickListener(this);
        endTrip_btn.setOnClickListener(this);
        stoppage_btn.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        noInternet.setOnClickListener(this);


        dialog_scheduleTrip = new SpotsDialog(DashBoard.this, R.style.dialog_schedulingTrip);
        dialog_checkingStatus = new SpotsDialog(DashBoard.this, R.style.dialog_checkingStatus);
        dialog_updatingData = new SpotsDialog(DashBoard.this, R.style.dialog_updating);




        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (DashBoard.this,android.R.layout.simple_list_item_1,states);
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







        SharedPreferences shared = getSharedPreferences("firstLog", MODE_PRIVATE);

        try{
            contactUID_tx = (shared.getString("contact_uid", ""));
        }
        catch (NullPointerException e){
            contactUID_tx  = "";
        }

        if (!isNetworkAvailable()){
            Alerter.create(DashBoard.this)
                    .setTitle("No Internet Connection!")
                    .setText("Some Modules may not work")
                    .setContentGravity(1)
                    .setBackgroundColorRes(R.color.black)
                    .setIcon(R.drawable.no_internet)
                    .show();
        }




        dialog_logging_out = new SpotsDialog(DashBoard.this,R.style.LoggingOut);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            FragmentManager fragmentManager = this.getSupportFragmentManager();
            int s =fragmentManager.getBackStackEntryCount() - 1;
            if (s >= 0){
                super.onBackPressed();

            }
            else {
                super.onBackPressed();
                moveTaskToBack(true);
                finish();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dash_board, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //   if (id == R.id.action_settings) {
        //     return true;
        // }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.nav_dashBoard) {

            Intent intent = new Intent(DashBoard.this, DashBoard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_DashBoard,new Profile()).addToBackStack("TruckFragments").commit();


        } else if (id == R.id.nav_emergency_contact) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_DashBoard,new EmergencyContact()).addToBackStack("EmergencyContactFragments").commit();

        }

        else if (id == R.id.nav_newTrip) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancelAll();
            }
          startActivity(new Intent(DashBoard.this, TripSchedules.class));
        }



        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){

            case R.id.dash_backButton:
                findViewById(R.id.dash_relativeLayout2).setVisibility(View.GONE);
                findViewById(R.id.dash_relativeLayout1).setVisibility(View.VISIBLE);

                break;

            case R.id.app_bar_dash_logoutButton:

                new MaterialDialog.Builder(this)
                        .title("Logout")
                        .content("Are You Sure to Logout?")
                        .positiveText("Yes")
                        .positiveColor(getResources().getColor(R.color.lightRed))
                        .negativeText("No")
                        .negativeColor(getResources().getColor(R.color.lightGreen))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog_logging_out.show();
                                SharedPreferences dataSave = getSharedPreferences("firstLog", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = dataSave.edit();
                                editor.putString("LaunchApplication", "Login");
                                editor.apply();

                                new Handler().postDelayed(new Runnable() {
                                    public void run() {

                                        dialog_logging_out.dismiss();
                                        moveTaskToBack(true);
                                        finish();

                                    }

                                }, 2000);


                                // TODO
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                // TODO
                                dialog_logging_out.dismiss();
                            }
                        }) .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        dialog_logging_out.dismiss();
                    }
                })
                        .show();

                break;



//                start New Trip
            case R.id.dash_startNewTripButton:
                startNewTrip_rl.setVisibility(View.GONE);
                startTrip_rl.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn)
                        .duration(1000)
                        .repeat(0)
                        .playOn(startTrip_rl);

                break;


//                Cancel Trip

            case R.id.dash_cancelTripButton:
                new MaterialDialog.Builder(DashBoard.this)
                        .title("Are You Sure to Cancel Trip?")
                        .positiveText("Yes")
                        .contentColor(getResources().getColor(R.color.blackNinety))
                        .positiveColor(getResources().getColor(R.color.lightRed))
                        .negativeText("No")
                        .negativeColor(getResources().getColor(R.color.lightGreen))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                                startNewTrip_rl.setVisibility(View.VISIBLE);
                                startTrip_rl.setVisibility(View.GONE);
                                YoYo.with(Techniques.FadeIn)
                                        .duration(1000)
                                        .repeat(0)
                                        .playOn(startNewTrip_rl);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        })
                        .show();
                break;

//                start trip
            case R.id.dash_startTripButton:


              /*  Thread t = new Thread(){
                    public void run(){

                        String timeServer = "time-a.nist.gov";
                        NTPUDPClient timeClient = new NTPUDPClient();
                        InetAddress inetAddress = null;
                        try {
                            inetAddress = InetAddress.getByName(timeServer);
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        TimeInfo timeInfo = null;
                        try {
                            timeInfo = timeClient.getTime(inetAddress);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        assert timeInfo != null;
                        long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                        DateFormat date = new SimpleDateFormat("dd_MM_yyyy");
                        DateFormat time = new SimpleDateFormat("HH_mm");

                        Date t = new Date(returnTime);

                        actualDate = date.format(t);
                        actualTime = time.format(t);

                    }
                };
                t.start();*/

                dialog_scheduleTrip.show();
                truckNumber_tx = truckNumber_act.getText().toString().trim();
                truckLocation_tx = truckLocation_act.getText().toString().trim();
                expected_stoppage_tx = expectedStoppage_act.getText().toString().trim();
                fuelPrice_tx = fuelPrice_et.getText().toString().trim();
                stateName_tx = stateName_et.getText().toString().trim();
                cityName_tx = cityName_et.getText().toString().trim();
                expensesTaken_tx = expensesTaken_et.getText().toString().trim();
                tripStartLocation_tx = tripStartLocation_act.getText().toString().trim();
                fuelTaken_tx = fuelTaken_et.getText().toString().trim();

                if (TextUtils.equals(truckNumber_tx, "")){
                    Alerter.create(DashBoard.this)
                            .setTitle("Enter truck Number!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    dialog_scheduleTrip.dismiss();
                    return;
                }

                if (TextUtils.equals(expected_stoppage_tx, "")){
                    Alerter.create(DashBoard.this)
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
                        Alerter.create(DashBoard.this)
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
                        Alerter.create(DashBoard.this)
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

                new MaterialDialog.Builder(DashBoard.this)
                        .title("Confirm Trip Details")
                        .content("Truck Number: "+ truckNumber_tx
                                + "\n\nTruck Location: " + truckLocation_tx
                                + "\n\nTrip Start Location: " + tripStartLocation_tx
                                + "\n\nExpected Stoppage: "+ expected_stoppage_tx
                                + "\n\nFuel Taken: "+ fuelTaken_tx +"/Litres"
                                + "\n\nFuel Price: Rs"+ fuelPrice_tx +"/Litre"
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
                                    Alerter.create(DashBoard.this)
                                            .setTitle("No Internet Connection!")
                                            .setContentGravity(1)
                                            .setBackgroundColorRes(R.color.black)
                                            .setIcon(R.drawable.no_internet)
                                            .show();
                                    dialog_scheduleTrip.dismiss();
                                    return;
                                }

                                d_networkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        connected = dataSnapshot.getValue(String.class);


                                        if (!TextUtils.equals(connected, "connected")){
                                            Alerter.create(DashBoard.this)
                                                    .setTitle("Unable to connect to server!")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.error)
                                                    .show();
                                            dialog_scheduleTrip.dismiss();
                                            return;
                                        }


                                                new Handler().postDelayed(new Runnable() {
                                                    public void run() {

                                                        if (!TextUtils.equals(connected, "connected")){
                                                            Alerter.create(DashBoard.this)
                                                                    .setTitle("Unable to connect Server!")
                                                                    .setText("We get time from internet and it needs decent internet connection. Please check if internet working.")
                                                                    .setContentGravity(1)
                                                                    .setBackgroundColorRes(R.color.black)
                                                                    .setIcon(R.drawable.error)
                                                                    .show();
                                                            dialog_scheduleTrip.dismiss();
                                                            return;
                                                        }

                                                        Calendar c = Calendar.getInstance();
                                                        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
                                                        SimpleDateFormat sdf1 = new SimpleDateFormat("HH_mm");
                                                        actualDate = sdf.format(c.getTime());
                                                        actualTime = sdf1.format(c.getTime());


                                                        final String key = d_root.push().getKey();


                                                        d_schedule_trip = d_root.child("trip_details").child(contactUID_tx).child(key).child("start_trip");

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

                                                        d_root.child("driver_profiles").child(contactUID_tx).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                d_root.child("trip_status").child(key).child("driverContact").setValue(contactUID_tx);
                                                                d_root.child("trip_status").child(key).child("driverName").setValue(dataSnapshot.getValue(String.class));
                                                                d_root.child("trip_status").child(key).child("tripStarted").setValue(actualDate+"_"+actualTime);
                                                                d_root.child("trip_status").child(key).child("truckNumber").setValue(truckNumber_tx);



                                                            }


                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                                dialog_scheduleTrip.dismiss();
                                                            }
                                                        });

                                                        Alerter.create(DashBoard.this)
                                                                .setTitle("Trip Started")
                                                                .setContentGravity(1)
                                                                .setBackgroundColorRes(R.color.black)
                                                                .setIcon(R.drawable.success_icon)
                                                                .show();
                                                        dialog_scheduleTrip.dismiss();


                                                        Intent mIntent = getIntent();
                                                        finish();
                                                        startActivity(mIntent);

                                                    }

                                                }, 2000);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {


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
                break;


        case R.id.dash_stoppageButton:
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_DashBoard, new Stoppage()).addToBackStack("stop").commit();
            break;

        case R.id.dash_petrolFillingButton:
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_DashBoard,new PetrolFilling()).addToBackStack("petrol").commit();
            break;


       case R.id.dash_otherFillingButton:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_DashBoard,new OtherFilling()).addToBackStack("other").commit();
                break;


       case R.id.dash_loadButton:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_DashBoard,new LoadUnLoad()).addToBackStack("load").commit();
                break;


       case R.id.dash_breakDownButton:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_DashBoard,new BreakDown()).addToBackStack("breakDown").commit();
                break;

       case R.id.dash_endTripButton:
           getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_DashBoard,new EndTrip()).addToBackStack("endTrip").commit();
           break;


       case R.id.no_internet:
           if (!isNetworkAvailable()) {

               YoYo.with(Techniques.Shake)
                       .duration(500)
                       .repeat(0)
                       .playOn(noInternet);

           }
           else {
               DashBoard.this.recreate();
           }

           break;


        }
    }


    protected  void onStart(){
        super.onStart();


        if (counter == 1){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.dash_splash_logo).setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.Landing)
                            .duration(1500)
                            .repeat(0)
                            .playOn(findViewById(R.id.dash_splash_logo));


                }
            },500);



            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    YoYo.with(Techniques.FadeOut)
                            .duration(1000)
                            .repeat(0)
                            .playOn(findViewById(R.id.dash_splash_relative));
                    //  dialog_updatingData.show();
                }
            },3000);
            counter++;
        }





        SharedPreferences shared = getSharedPreferences("firstLog", MODE_PRIVATE);

        try {
            contactUID_tx = (shared.getString("contact_uid", ""));
            String name = (shared.getString("profileName", ""));
            contact_uid_tv.setText(contactUID_tx);
            user_name_tv.setText(name);
        } catch (NullPointerException e) {
            contactUID_tx = "";
        }


        if (!isNetworkAvailable()) {
                   noInternet.setVisibility(View.VISIBLE);
                   YoYo.with(Techniques.FadeIn)
                           .duration(3000)
                           .repeat(0)
                           .playOn(noInternet);

                   if (!DashBoard.this.isDestroyed()){
                       Glide.with(DashBoard.this)
                               .load(R.drawable.driver_default_image_icon)
                               .fitCenter()
                               .centerCrop()
                               .crossFade(1200)
                               .diskCacheStrategy(DiskCacheStrategy.ALL)
                               .into(nav_profileImageView);}

                   dialog_updatingData.dismiss();
                   return;
               }

        d_networkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                connected = dataSnapshot.getValue(String.class);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



               d_root.child("trip_details").addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot snapshot) {


                       if (!snapshot.hasChild(contactUID_tx)) {
                           startNewTrip_rl.setVisibility(View.VISIBLE);
                           noInternet.setVisibility(View.GONE);
                           dialog_updatingData.dismiss();
                           return;
                       }


                       Query query = d_root.child("trip_details").child(contactUID_tx).orderByKey().limitToLast(1);
                       query.addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot) {
                               for (DataSnapshot child : dataSnapshot.getChildren()) {
                                   final String key = child.child("start_trip").child("start_date").getValue(String.class);

                                   try {
                                       day = TextUtils.substring(key, 0, 2);
                                       month = TextUtils.substring(key, 3, 5);
                                       year = TextUtils.substring(key, 6, 10);
                                       hour = TextUtils.substring(key, 11, 13);
                                       minute = TextUtils.substring(key, 14, 16);
                                       conversion();
                                       noInternet.setVisibility(View.GONE);
                                       header_text.setVisibility(View.VISIBLE);
                                       header_text.setText(header_text.getText()+" "+day+"-"+month+"-"+year+", "+hour+":"+minute);

                                   } catch (NullPointerException e) {
                                       e.printStackTrace();
                                   }


                                   YoyoAnimations();


                                   try{
                                       trip_check = child.child("start_trip").child("status").getValue(String.class);
                                       if (TextUtils.equals(trip_check, "active")) {
                                           startNewTrip_rl.setVisibility(View.GONE);
                                           startTrip_rl.setVisibility(View.GONE);
                                           tripPage_rl.setVisibility(View.VISIBLE);
                                           noInternet.setVisibility(View.GONE);
                                           dialog_updatingData.dismiss();


                                       } else {
                                           noInternet.setVisibility(View.GONE);
                                           startNewTrip_rl.setVisibility(View.VISIBLE);

                                       }
                                   }
                                   catch (NullPointerException e){
                                       e.printStackTrace();
                                   }



                                   dialog_updatingData.dismiss();
                               }

                           }

                           @Override
                           public void onCancelled(DatabaseError databaseError) {

                           }
                       });

                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });


               SpinnerData();


        DatabaseReference d_notification = d_root.child("trip_schedules_driver").child(contactUID_tx);


        d_notification.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String schedule_status = dataSnapshot.child("status").getValue(String.class);

                if (TextUtils.equals(schedule_status, "waiting")) {

                    String truck_number = dataSnapshot.child("truck_number").getValue(String.class);
                    String start_date = dataSnapshot.child("expected_start_date").getValue(String.class);

                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    Intent showIntent = new Intent(context, TripSchedules.class);
                    PendingIntent pIntent = PendingIntent.getActivity(context
                            , 0, showIntent, PendingIntent.FLAG_CANCEL_CURRENT);


                    PugNotification.with(context)
                            .load()
                            .title("You Have a new Trip")
                            .sound(soundUri)
                            .smallIcon(R.drawable.ic_local_shipping_black)
                            .autoCancel(true)
                            .largeIcon(R.drawable.app_logo)
                            .flags(Notification.FLAG_AUTO_CANCEL)
                            .bigTextStyle("Truck Number " + truck_number
                                    + " is allocated to you."
                                    +"Trip is expected to be start on " +start_date)
                            .click(pIntent)
                            .simple()
                            .build();

                    hideItem();


                    assert v != null;
                    v.vibrate(300);

                }

                //hello

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



               // profile image

        //pasting profile image

        StorageReference load_profile_image = storageRef.child("driver_profiles/").child(contactUID_tx).child("profile_image/").child("image.jpg");
        try {

            load_profile_image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    if (!DashBoard.this.isDestroyed()){

                    Glide.with(DashBoard.this)
                            .load(uri)
                            .asBitmap()
                            .fitCenter()
                            .centerCrop()
                            .error(R.drawable.driver_default_image_icon)
                            .placeholder(R.drawable.driver_default_image_icon)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(new BitmapImageViewTarget(nav_profileImageView) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    RoundedBitmapDrawable circularBitmapDrawable =
                                            RoundedBitmapDrawableFactory.create(getApplication().getResources(), resource);
                                    circularBitmapDrawable.setCircular(true);
                                    nav_profileImageView.setImageDrawable(circularBitmapDrawable);
                                }
                            });}


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    if (!DashBoard.this.isDestroyed()){
                    Glide.with(DashBoard.this)
                            .load(R.drawable.driver_default_image_icon)
                            .fitCenter()
                            .centerCrop()
                            .crossFade(1200)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(nav_profileImageView);}
                }
            });

        } catch (IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
        }

        d_profile.child(contactUID_tx).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_name_tv.setText(dataSnapshot.child("name").getValue(String.class));
                contact_uid_tv.setText(dataSnapshot.child("contact").getValue(String.class));
                SharedPreferences dataSave = getSharedPreferences("firstLog", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = dataSave.edit();
                editor.putString("profileName", dataSnapshot.child("name").getValue(String.class));
                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.equals(connected, "connected")){
                    noInternet.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn)
                            .duration(3000)
                            .repeat(0)
                            .playOn(noInternet);

                    if (!DashBoard.this.isDestroyed()){
                        Glide.with(DashBoard.this)
                                .load(R.drawable.driver_default_image_icon)
                                .fitCenter()
                                .centerCrop()
                                .crossFade(1200)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(nav_profileImageView);}

                    dialog_updatingData.dismiss();
                }
            }
        },12000);




    }// start end




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
        if ( dialog_updatingData!=null && dialog_updatingData.isShowing() ){
            dialog_updatingData.dismiss();
        }
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

    public void hideItem()
    {
        NavigationView navigationView =  findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_newTrip).setVisible(true);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_newTrip);
        SpannableString s = new SpannableString(menuItem.getTitle());
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.lightRed)), 0, s.length(), 0);
        menuItem.setTitle(s);

    }



    public void YoyoAnimations(){

        YoYo.with(Techniques.FadeIn)
                .duration(2000)
                .repeat(0)
                .playOn(header_text);

        YoYo.with(Techniques.FadeIn)
                .duration(2000)
                .repeat(0)
                .playOn(stoppage_btn);

        YoYo.with(Techniques.FadeIn)
                .duration(2000)
                .repeat(0)
                .playOn(petrolFilling_btn);

        YoYo.with(Techniques.FadeIn)
                .duration(2000)
                .repeat(0)
                .playOn(otherFilling_btn);

        YoYo.with(Techniques.FadeIn)
                .duration(2000)
                .repeat(0)
                .playOn(load_btn);

        YoYo.with(Techniques.FadeIn)
                .duration(2000)
                .repeat(0)
                .playOn(breakDown_btn);

        YoYo.with(Techniques.FadeIn)
                .duration(2000)
                .repeat(0)
                .playOn(endTrip_btn);

    }


    public void SpinnerData(){

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
                    ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(DashBoard.this, android.R.layout.simple_spinner_item, areas);
                    ArrayAdapter<String> areasAdapter1 = new ArrayAdapter<String>(DashBoard.this, android.R.layout.simple_list_item_1, areas);

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

                    ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(DashBoard.this, android.R.layout.simple_spinner_item, areas);
                    ArrayAdapter<String> areasAdapter1 = new ArrayAdapter<String>(DashBoard.this, android.R.layout.simple_list_item_1, areas);

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

                    ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(DashBoard.this, android.R.layout.simple_spinner_item, areas);
                    ArrayAdapter<String> areasAdapter1 = new ArrayAdapter<String>(DashBoard.this, android.R.layout.simple_list_item_1, areas);

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

                    ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(DashBoard.this, android.R.layout.simple_spinner_item, areas);
                    ArrayAdapter<String> areasAdapter1 = new ArrayAdapter<String>(DashBoard.this, android.R.layout.simple_list_item_1, areas);

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

}

