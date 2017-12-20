package enhabyto.com.petroleumdriver;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.location.LocationListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tapadoo.alerter.Alerter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import dmax.dialog.SpotsDialog;
import im.delight.android.location.SimpleLocation;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class Stoppage extends Fragment {

    private View view;
    RadioRealButtonGroup choiceGroup;
    String contactUID_tx, connected;

    EditText placeName_et, desciption_et, moneyPaid_et, moneyPaidFor_et;
    String placeName_tx, desciption_tx, moneyPaid_tx, moneyPaidFor_tx;
    FancyButton submit_btn;

    private DatabaseReference d_root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference d_trip_detail_stoppage;
    DatabaseReference d_networkStatus = FirebaseDatabase.getInstance().getReference().child("checkNetwork").child("isConnected");

    AlertDialog dialog_addingStoppage;
    String payment = "no", secondKey;


    public Stoppage() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_stoppage, container, false);



        choiceGroup = view.findViewById(R.id.stop_paidSelector);
        placeName_et = view.findViewById(R.id.stop_stoppageNameEditText);
        desciption_et = view.findViewById(R.id.stop_descriptionEditText);
        moneyPaid_et = view.findViewById(R.id.stop_paidEditText);
        moneyPaidFor_et = view.findViewById(R.id.stop_paidForEditText);
        submit_btn = view.findViewById(R.id.stop_submitStoppageButton);
        choiceGroup.setPosition(1);

        dialog_addingStoppage = new SpotsDialog(getActivity(), R.style.dialog_stoppage);


        SharedPreferences shared = getActivity().getSharedPreferences("firstLog", MODE_PRIVATE);

        try{
            contactUID_tx = (shared.getString("contact_uid", ""));
        }
        catch (NullPointerException e){
            contactUID_tx  = "";
        }

        choiceGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                if (position == 0){
                    payment = "yes";
                    view.findViewById(R.id.stop_relativeLayout2).setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn)
                            .duration(700)
                            .repeat(0)
                            .playOn( view.findViewById(R.id.stop_relativeLayout2));
                }
                else {
                    payment = "no";
                    YoYo.with(Techniques.FadeOut)
                            .duration(700)
                            .repeat(0)
                            .playOn( view.findViewById(R.id.stop_relativeLayout2));
                    moneyPaid_et.setText(null);
                    moneyPaidFor_et.setText(null);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.findViewById(R.id.stop_relativeLayout2).setVisibility(View.GONE);
                        }
                    },400);

                }
            }
        });


        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                dialog_addingStoppage.show();
                placeName_tx = placeName_et.getText().toString().trim();
                desciption_tx = desciption_et.getText().toString().trim();
                moneyPaid_tx = moneyPaid_et.getText().toString().trim();
                moneyPaidFor_tx = moneyPaidFor_et.getText().toString().trim();

                if (!isNetworkAvailable()){
                    Alerter.create(getActivity())
                            .setTitle("No Internet Connection!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.no_internet)
                            .show();
                    dialog_addingStoppage.dismiss();
                   return;
                }

                if (placeName_tx.length()<3){
                    Alerter.create(getActivity())
                            .setTitle("Provide full name of Place!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    dialog_addingStoppage.dismiss();
                    return;
                }

                if (TextUtils.equals(payment, "yes")){
                    if (TextUtils.equals(moneyPaid_tx, "") && TextUtils.equals(moneyPaidFor_tx, "")){
                        Alerter.create(getActivity())
                                .setTitle("Enter Payment Details!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();
                        dialog_addingStoppage.dismiss();
                        return;
                    }
                }

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
                            dialog_addingStoppage.dismiss();
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

                                    d_trip_detail_stoppage = d_root.child("trip_details").child(contactUID_tx)
                                            .child(key).child("stoppage");

                                    secondKey = d_trip_detail_stoppage.push().getKey();

                                    d_trip_detail_stoppage.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            String latitude = String.valueOf(getLatitude());
                                            String longitude = String.valueOf(getLongitude());
                                            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                            String gps_address="";
                                            try {
                                                List<Address> addresses  = null;
                                                addresses = geocoder.getFromLocation(getLatitude(),getLongitude(), 1);
                                                gps_address = addresses.get(0).getAddressLine(0);
                                            } catch (IOException e) {

                                                e.printStackTrace();
                                            }

                                            Calendar c = Calendar.getInstance();
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
                                            SimpleDateFormat sdf1 = new SimpleDateFormat("HH_mm");
                                            String actualDate = sdf.format(c.getTime());
                                            String actualTime = sdf1.format(c.getTime());

                                            d_trip_detail_stoppage.child(secondKey).child("place_name").setValue(placeName_tx);
                                            d_trip_detail_stoppage.child(secondKey).child("description").setValue(desciption_tx);
                                            d_trip_detail_stoppage.child(secondKey).child("money_paid").setValue(moneyPaid_tx);
                                            d_trip_detail_stoppage.child(secondKey).child("money_paid_for").setValue(moneyPaidFor_tx);
                                            d_trip_detail_stoppage.child(secondKey).child("gps_latitude").setValue(latitude);
                                            d_trip_detail_stoppage.child(secondKey).child("gps_longitude").setValue(longitude);
                                            d_trip_detail_stoppage.child(secondKey).child("gps_location").setValue(gps_address);

                                            d_trip_detail_stoppage.child(secondKey).child("date_time").setValue(actualDate+"_"+actualTime);


                                            Alerter.create(getActivity())
                                                    .setTitle("Success")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.success_icon)
                                                    .show();

                                            dialog_addingStoppage.dismiss();

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                            Alerter.create(getActivity())
                                                    .setTitle("Error! Try Again")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.error)
                                                    .show();
                                            dialog_addingStoppage.dismiss();
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



    public double getLatitude(){

        // construct a new instance of SimpleLocation
        SimpleLocation location = new SimpleLocation(getActivity());

        // if we can't access the location yet
        if (!location.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(getActivity());
        }


        double latitude = location.getLatitude();
        return latitude;


    }


    public double getLongitude(){

        // construct a new instance of SimpleLocation
        SimpleLocation  location = new SimpleLocation(getActivity());

        // if we can't access the location yet
        if (!location.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(getActivity());
        }


        final double longitude = location.getLongitude();
        return longitude;


    }

}
