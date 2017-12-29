package enhabyto.com.petroleumdriver;


import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tapadoo.alerter.Alerter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import dmax.dialog.SpotsDialog;
import im.delight.android.location.SimpleLocation;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoadUnLoad extends Fragment implements View.OnClickListener{

    private View view;
    AutoCompleteTextView  unload_pumpName_et;
    EditText load_oilLoaded_et,load_location_et,load_nextStoppage_et, unload_oilUnLoaded_et
            , unload_location_et, unload_oilLeft_et, unload_nextStoppage_et;

    AutoCompleteTextView unload_stateName_et, load_stateName_et;

    String load_oilLoaded_tx,load_location_tx,load_stateName_tx,load_nextStoppage_tx, unload_oilUnLoaded_tx
            , unload_pumpName_tx, unload_location_tx, unload_stateName_tx, unload_oilLeft_tx, unload_nextStoppage_tx;

    FancyButton load_submit_btn, unLoad_submit_btn, load_btn, unLoad_btn;

    private DatabaseReference d_root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference d_load;

    DatabaseReference d_networkStatus = FirebaseDatabase.getInstance().getReference().child("checkNetwork").child("isConnected");

    AlertDialog dialog_update;
    String contactUID_tx, connected;
    Spinner spinner;

    final String [] states = new String[]{"Andra Pradesh","Arunachal Pradesh","Assam","Bihar","Chhattisgarh","Goa","Gujarat"
            ,"Haryana","Himachal Pradesh","Jammu and Kashmir","Jharkhand","Karnataka","Kerala","Madya Pradesh","Maharashtra"
            ,"Manipur","Meghalaya","Mizoram","Nagaland","Orissa","Punjab","Rajasthan","Sikkim"
            ,"Tamil Nadu","Tripura","Uttaranchal","Uttar Pradesh","West Bengal","Delhi","Lakshadeep","Pondicherry"
            ,"Andaman and Nicobar","Chandigarh","Dadar and Nagar","Daman and Diu"};

    public LoadUnLoad() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_load_un_load, container, false);

        load_oilLoaded_et = view.findViewById(R.id.load_oilLoadedEditText);
        load_location_et = view.findViewById(R.id.load_locationEditText);
        load_stateName_et = view.findViewById(R.id.load_stateEditText);
        load_nextStoppage_et = view.findViewById(R.id.load_goingToEditText);
        unload_oilUnLoaded_et = view.findViewById(R.id.unload_oilLoadedEditText);
        unload_pumpName_et = view.findViewById(R.id.unload_pumpNameEditText);
        unload_location_et = view.findViewById(R.id.unload_locationEditText);
        unload_stateName_et = view.findViewById(R.id.unload_stateEditText);
        unload_oilLeft_et = view.findViewById(R.id.unload_oilLeftEditText);
        unload_nextStoppage_et = view.findViewById(R.id.unload_goingToEditText);

        load_submit_btn = view.findViewById(R.id.load_submitLoadDetails);
        unLoad_submit_btn = view.findViewById(R.id.unload_submitUnLoadDetails);
        load_btn = view.findViewById(R.id.load_Button);
        unLoad_btn = view.findViewById(R.id.unload_Button);

        load_submit_btn.setOnClickListener(this);
        unLoad_submit_btn.setOnClickListener(this);
        load_btn.setOnClickListener(this);
        unLoad_btn.setOnClickListener(this);
        dialog_update = new SpotsDialog(getActivity(), R.style.dialog_updating);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(),android.R.layout.simple_list_item_1,states);
        unload_stateName_et.setAdapter(adapter);
        load_stateName_et.setAdapter(adapter);

        spinner = view.findViewById(R.id.uload_spinner);

        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {

                            String selected = spinner.getSelectedItem().toString();
                            if (!TextUtils.equals(selected,"Select Pump Name")){
                                unload_pumpName_et.setText(selected);
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

        SharedPreferences shared = getActivity().getSharedPreferences("firstLog", MODE_PRIVATE);

        try{
            contactUID_tx = (shared.getString("contact_uid", ""));
        }
        catch (NullPointerException e){
            contactUID_tx  = "";
        }


        return view;
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id){

            case R.id.load_Button:

                view.findViewById(R.id.load_relative).setVisibility(View.VISIBLE);
                view.findViewById(R.id.unload_relative).setVisibility(View.GONE);
                YoYo.with(Techniques.FadeInLeft)
                        .duration(700)
                        .repeat(0)
                        .playOn( view.findViewById(R.id.load_relative));
                YoYo.with(Techniques.FadeOutRight)
                        .duration(400)
                        .repeat(0)
                        .playOn( view.findViewById(R.id.unload_relative));
                break;


            case R.id.unload_Button:

                view.findViewById(R.id.load_relative).setVisibility(View.GONE);
                view.findViewById(R.id.unload_relative).setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInRight)
                        .duration(700)
                        .repeat(0)
                        .playOn( view.findViewById(R.id.unload_relative));
                YoYo.with(Techniques.FadeOutLeft)
                        .duration(400)
                        .repeat(0)
                        .playOn( view.findViewById(R.id.load_relative));
                break;


            case R.id.load_submitLoadDetails:

                load_oilLoaded_tx = load_oilLoaded_et.getText().toString().trim();
                load_location_tx = load_location_et.getText().toString().trim();
                load_stateName_tx = load_stateName_et.getText().toString().trim();
                load_nextStoppage_tx = load_nextStoppage_et.getText().toString().trim();

                if (!isNetworkAvailable()){
                    Alerter.create(getActivity())
                            .setTitle("No Internet Connection!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.no_internet)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(load_oilLoaded_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Enter Oil Loaded!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(load_location_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Enter Location!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    return;
                }
                dialog_update.show();
                d_networkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        connected = dataSnapshot.getValue(String.class);

                        if (!TextUtils.equals(connected, "connected")){
                            Alerter.create(getActivity())
                                    .setTitle("Unable to connect to server!")
                                    .setContentGravity(1)
                                    .setBackgroundColorRes(R.color.black)
                                    .setIcon(R.drawable.error)
                                    .show();
                            dialog_update.dismiss();
                            return;
                        }

                        Query query1 = d_root.child("trip_details").child(contactUID_tx).orderByKey().limitToLast(1);

                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot child: dataSnapshot.getChildren()) {
                                    //  trip_check = child.child("status").getValue().toString();

                                    final String key = child.getKey();
                                    Log.w("432", key);

                                    d_load = d_root.child("trip_details").child(contactUID_tx)
                                            .child(key).child("load");

                                    final  String secondKey = d_load.push().getKey();

                                    d_load.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {


                                            String latitude = String.valueOf(getLatitude());
                                            String longitude = String.valueOf(getLongitude());
                                            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                            String gps_address="";
                                            try {
                                                List<Address> addresses;
                                                addresses = geocoder.getFromLocation(getLatitude(),getLongitude(), 1);
                                                gps_address = addresses.get(0).getAddressLine(0);
                                            } catch (IOException | IndexOutOfBoundsException e) {

                                                e.printStackTrace();
                                            }

                                            Calendar c = Calendar.getInstance();
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
                                            SimpleDateFormat sdf1 = new SimpleDateFormat("HH_mm");
                                            String actualDate = sdf.format(c.getTime());
                                            String actualTime = sdf1.format(c.getTime());

                                            d_load.child(secondKey).child("oil_loaded").setValue(load_oilLoaded_tx);
                                            d_load.child(secondKey).child("location").setValue(load_location_tx);
                                            d_load.child(secondKey).child("state_name").setValue(load_stateName_tx);
                                            d_load.child(secondKey).child("next_stoppage").setValue(load_nextStoppage_tx);
                                            d_load.child(secondKey).child("gps_latitude").setValue(latitude);
                                            d_load.child(secondKey).child("gps_longitude").setValue(longitude);
                                            d_load.child(secondKey).child("gps_location").setValue(gps_address);

                                            d_load.child(secondKey).child("date_time").setValue(actualDate+"_"+actualTime);


                                            Alerter.create(getActivity())
                                                    .setTitle("Success")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.success_icon)
                                                    .show();

                                            dialog_update.dismiss();

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                            Alerter.create(getActivity())
                                                    .setTitle("Error! Try Again")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.error)
                                                    .show();
                                            dialog_update.dismiss();
                                        }
                                    });

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
                break;



            case R.id.unload_submitUnLoadDetails:

                unload_oilUnLoaded_tx =  unload_oilUnLoaded_et.getText().toString().trim();
                unload_pumpName_tx =  unload_pumpName_et.getText().toString().trim();
                unload_location_tx =  unload_location_et.getText().toString().trim();
                unload_stateName_tx =  unload_stateName_et.getText().toString().trim();
                unload_oilLeft_tx =  unload_oilLeft_et.getText().toString().trim();
                unload_nextStoppage_tx =  unload_nextStoppage_et.getText().toString().trim();



                if (!isNetworkAvailable()){
                    Alerter.create(getActivity())
                            .setTitle("No Internet Connection!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.no_internet)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(unload_oilUnLoaded_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Enter Oil UnLoaded!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(unload_location_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Enter Location!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(unload_oilLeft_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Enter Oil Left!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    return;
                }

                dialog_update.show();
                d_networkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        connected = dataSnapshot.getValue(String.class);

                        if (!TextUtils.equals(connected, "connected")){
                            Alerter.create(getActivity())
                                    .setTitle("Unable to connect to server!")
                                    .setContentGravity(1)
                                    .setBackgroundColorRes(R.color.black)
                                    .setIcon(R.drawable.error)
                                    .show();
                            dialog_update.dismiss();
                            return;
                        }

                        Query query1 = d_root.child("trip_details").child(contactUID_tx).orderByKey().limitToLast(1);


                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot child: dataSnapshot.getChildren()) {
                                    //  trip_check = child.child("status").getValue().toString();

                                    final String key = child.getKey();
                                    Log.w("432", key);

                                    d_load = d_root.child("trip_details").child(contactUID_tx)
                                            .child(key).child("unload");

                                    final  String secondKey = d_load.push().getKey();

                                    d_load.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            String latitude = String.valueOf(getLatitude());
                                            String longitude = String.valueOf(getLongitude());
                                            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                            String gps_address="";
                                            try {
                                                List<Address> addresses;
                                                addresses = geocoder.getFromLocation(getLatitude(),getLongitude(), 1);
                                                gps_address = addresses.get(0).getAddressLine(0);
                                            } catch (IOException | IndexOutOfBoundsException e) {

                                                e.printStackTrace();
                                            }


                                            Calendar c = Calendar.getInstance();
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
                                            SimpleDateFormat sdf1 = new SimpleDateFormat("HH_mm");
                                            String actualDate = sdf.format(c.getTime());
                                            String actualTime = sdf1.format(c.getTime());

                                            d_load.child(secondKey).child("oil_unloaded").setValue(unload_oilUnLoaded_tx);
                                            d_load.child(secondKey).child("pump_name").setValue(unload_pumpName_tx);
                                            d_load.child(secondKey).child("location").setValue(unload_location_tx);
                                            d_load.child(secondKey).child("state_name").setValue(unload_stateName_tx);
                                            d_load.child(secondKey).child("next_stoppage").setValue(unload_nextStoppage_tx);
                                            d_load.child(secondKey).child("oil_left").setValue(unload_oilLeft_tx);
                                            d_load.child(secondKey).child("gps_latitude").setValue(latitude);
                                            d_load.child(secondKey).child("gps_longitude").setValue(longitude);
                                            d_load.child(secondKey).child("gps_location").setValue(gps_address);

                                            d_load.child(secondKey).child("date_time").setValue(actualDate+"_"+actualTime);



                                            Alerter.create(getActivity())
                                                    .setTitle("Success")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.success_icon)
                                                    .show();

                                            dialog_update.dismiss();

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                            Alerter.create(getActivity())
                                                    .setTitle("Error! Try Again")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.error)
                                                    .show();
                                            dialog_update.dismiss();
                                        }
                                    });

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
                break;



        }

    }

    //    internet check
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onStart(){
        super.onStart();
        DatabaseReference dataRef_spinner = FirebaseDatabase.getInstance().getReference();
        dataRef_spinner.child("pump_details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    final List<String> areas = new ArrayList<>();
                    areas.add("Select Pump Name");
                    for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                        String pump_name = areaSnapshot.child("pump_name").getValue(String.class);
                        areas.add(pump_name);

                    }

                    ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, areas);
                    ArrayAdapter<String> areasAdapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, areas);

                    areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(areasAdapter);
                    unload_pumpName_et.setAdapter(areasAdapter1);

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



    public double getLatitude(){

        // construct a new instance of SimpleLocation
        SimpleLocation location = new SimpleLocation(getActivity());

        // if we can't access the location yet
        if (!location.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(getActivity());
        }


        return location.getLatitude();


    }


    public double getLongitude(){

        // construct a new instance of SimpleLocation
        SimpleLocation  location = new SimpleLocation(getActivity());

        // if we can't access the location yet
        if (!location.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(getActivity());
        }


        return location.getLongitude();


    }


    //end
}
