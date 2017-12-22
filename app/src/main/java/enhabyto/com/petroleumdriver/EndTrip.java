package enhabyto.com.petroleumdriver;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tapadoo.alerter.Alerter;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dmax.dialog.SpotsDialog;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class EndTrip extends Fragment {

    private View view;

    private  String key;
    int size;

    private DatabaseReference d_root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference d_end = d_root;

    AutoCompleteTextView stateName_et;
    DatabaseReference d_networkStatus = FirebaseDatabase.getInstance().getReference().child("checkNetwork").child("isConnected");
    AlertDialog dialog_endingTrip;
    AutoCompleteTextView pumpName_et;
    EditText  address_et;
    String pumpName_tx, address_tx, stateName_tx;

    FancyButton submit_btn;
    String contactUID_tx, connected;

    private String actualDate, actualTime;
    Spinner spinner;

    final String [] states = new String[]{"Andra Pradesh","Arunachal Pradesh","Assam","Bihar","Chhattisgarh","Goa","Gujarat"
            ,"Haryana","Himachal Pradesh","Jammu and Kashmir","Jharkhand","Karnataka","Kerala","Madya Pradesh","Maharashtra"
            ,"Manipur","Meghalaya","Mizoram","Nagaland","Orissa","Punjab","Rajasthan","Sikkim"
            ,"Tamil Nadu","Tripura","Uttaranchal","Uttar Pradesh","West Bengal","Delhi","Lakshadeep","Pondicherry"
            ,"Andaman and Nicobar","Chandigarh","Dadar and Nagar","Daman and Diu"};



    public EndTrip() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_end_trip, container, false);

        SharedPreferences shared = getActivity().getSharedPreferences("firstLog", MODE_PRIVATE);
        try{
            contactUID_tx = (shared.getString("contact_uid", ""));
        }
        catch (NullPointerException e){
            contactUID_tx  = "";
        }


        pumpName_et = view.findViewById(R.id.end_pumpNameEditText);
        stateName_et = view.findViewById(R.id.end_stateEditText);
        address_et = view.findViewById(R.id.end_locationEditText);


        submit_btn = view.findViewById(R.id.end_submitButton);

        dialog_endingTrip = new SpotsDialog(getActivity(), R.style.dialog_endingTrip);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(),android.R.layout.simple_list_item_1,states);
        stateName_et.setAdapter(adapter);

        spinner = view.findViewById(R.id.end_spinner);

        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {

                            String selected = spinner.getSelectedItem().toString();
                            if (!TextUtils.equals(selected,"Select Pump Name")){
                                pumpName_et.setText(selected);
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



        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pumpName_tx = pumpName_et.getText().toString().trim();
                stateName_tx = stateName_et.getText().toString().trim();
                address_tx = address_et.getText().toString().trim();

                if (!isNetworkAvailable()){
                    Alerter.create(getActivity())
                            .setTitle("No Internet Connection!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.no_internet)
                            .show();

                    return;
                }


                if (TextUtils.isEmpty(pumpName_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Pump Name Required!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();

                    return;
                }

                if (TextUtils.isEmpty(address_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Pump location Required!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();

                    return;
                }

                if (TextUtils.isEmpty(stateName_tx)){
                    Alerter.create(getActivity())
                            .setTitle("state Name Required!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();

                    return;
                }

                new MaterialDialog.Builder(getActivity())
                        .title("Are You Sure to End Trip?")
                        .content("This cannot be undone once submitted, so be very very sure.")
                        .positiveText("Yes")
                        .positiveColor(getResources().getColor(R.color.lightRed))
                        .negativeText("No")
                        .negativeColor(getResources().getColor(R.color.lightGreen))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog_endingTrip.show();

                                if(!isNetworkAvailable()){
                                    Alerter.create(getActivity())
                                            .setTitle("No Internet Connection!")
                                            .setContentGravity(1)
                                            .setBackgroundColorRes(R.color.black)
                                            .setIcon(R.drawable.no_internet)
                                            .show();
                                    dialog_endingTrip.dismiss();
                                    return;
                                }



                                d_networkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {


                                        connected = dataSnapshot.getValue(String.class);

                                        Query query1 = d_root.child("trip_details").child(contactUID_tx).orderByKey().limitToLast(1);

                                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot child: dataSnapshot.getChildren()) {

                                                    key = child.getKey();

                                                }
                                                if (!TextUtils.equals(connected, "connected")){
                                                    Alerter.create(getActivity())
                                                            .setTitle("Unable to connect to server!")
                                                            .setContentGravity(1)
                                                            .setBackgroundColorRes(R.color.black)
                                                            .setIcon(R.drawable.error)
                                                            .show();
                                                    dialog_endingTrip.dismiss();
                                                    return;
                                                }

                                                Calendar c = Calendar.getInstance();
                                                SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
                                                SimpleDateFormat sdf1 = new SimpleDateFormat("HH_mm");
                                                actualDate = sdf.format(c.getTime());
                                                actualTime = sdf1.format(c.getTime());

                                                d_end.child("trip_details").child(contactUID_tx).child(key).child("end_trip").child("pump_name").setValue(pumpName_tx);
                                                d_end.child("trip_details").child(contactUID_tx).child(key).child("end_trip").child("address").setValue(address_tx);
                                                d_end.child("trip_details").child(contactUID_tx).child(key).child("start_trip").child("end_location").setValue(address_tx);
                                                d_end.child("trip_details").child(contactUID_tx).child(key).child("end_trip").child("state_name").setValue(stateName_tx);
                                                d_end.child("trip_details").child(contactUID_tx).child(key).child("end_trip").child("end_date").setValue(actualDate+"_"+actualTime);


                                                d_end.child("trip_details").child(contactUID_tx)
                                                        .child(key).child("start_trip").child("status").setValue("ended");

                                                d_root.child("trip_status").child(key).setValue(null);

                                                Alerter.create(getActivity())
                                                        .setTitle("Trip Ended")
                                                        .setContentGravity(1)
                                                        .setBackgroundColorRes(R.color.black)
                                                        .setIcon(R.drawable.success_icon)
                                                        .show();

                                                dialog_endingTrip.dismiss();

                                                Intent mIntent = getActivity().getIntent();
                                                getActivity().finish();
                                                startActivity(mIntent);




                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                                Alerter.create(getActivity())
                                                        .setTitle("Server Error! Try Again")
                                                        .setContentGravity(1)
                                                        .setBackgroundColorRes(R.color.black)
                                                        .setIcon(R.drawable.error)
                                                        .show();
                                                dialog_endingTrip.dismiss();

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {



                                        Alerter.create(getActivity())
                                                .setTitle("Server Error! Try Again")
                                                .setContentGravity(1)
                                                .setBackgroundColorRes(R.color.black)
                                                .setIcon(R.drawable.error)
                                                .show();
                                        dialog_endingTrip.dismiss();
                                    }
                                });




                                // TODO
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                // TODO
                                dialog_endingTrip.dismiss();
                            }
                        }) .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        dialog_endingTrip.dismiss();
                    }
                })
                        .show();




            }
        });






        return view;
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
                    pumpName_et.setAdapter(areasAdapter1);

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

  //end
}
