package enhabyto.com.petroleumdriver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.tapadoo.alerter.Alerter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import im.delight.android.location.SimpleLocation;
import mehdi.sakout.fancybuttons.FancyButton;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class BreakDown extends Fragment implements View.OnClickListener {

    private View view;

    FancyButton addResource_btn, removeResource_btn, removePhoto_btn, addPhoto_btn
            , removeBrokenPartPhoto_btn, addBrokenPartPhoto_btn, submit_btn, selectBillPhoto1_btn, selectBillPhoto2_btn, selectBillPhoto3_btn
            , selectBrokenPhoto1_btn, selectBrokenPhoto2_btn, selectBrokenPhoto3_btn;


    RadioRealButtonGroup choiceGroup;
    String contactUID_tx, connected;

    EditText failure_et, address_et, resourceUsed1_et, resourceUsed2_et, resourceUsed3_et, resourceUsed4_et
            , resourceUsed5_et, resourcePrice1_et, resourcePrice2_et, resourcePrice3_et, resourcePrice4_et, resourcePrice5_et
            , resourceQuantity1_et, resourceQuantity2_et, resourceQuantity3_et, resourceQuantity4_et, resourceQuantity5_et
            , billPrice1_et, billPrice2_et, billPrice3_et;

    String  failure_tx, address_tx, stateName_tx,resourceUsed1_tx, resourceUsed2_tx, resourceUsed3_tx, resourceUsed4_tx
            , resourceUsed5_tx, resourcePrice1_tx, resourcePrice2_tx, resourcePrice3_tx, resourcePrice4_tx, resourcePrice5_tx
            , resourceQuantity1_tx, resourceQuantity2_tx, resourceQuantity3_tx, resourceQuantity4_tx, resourceQuantity5_tx
            , billPrice1_tx, billPrice2_tx, billPrice3_tx;

    ImageView billImage1 ,billImage2, billImage3, brokenImage4 ,brokenImage5, brokenImage6;

    private  String key, secondKey;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://rajpetroleum-4d3fa.appspot.com/");

    private DatabaseReference d_root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference d_failure;

    DatabaseReference d_networkStatus = FirebaseDatabase.getInstance().getReference().child("checkNetwork").child("isConnected");

    AlertDialog dialog_updating;
    AutoCompleteTextView stateName_et;


    String gate = "no";
    int countResource = 2;
    int removeResource = 1;
    int countPhoto = 2;
    int removePhoto = 1;
    int countPhoto1 = 2;
    int removePhoto1 = 1;

    int PICK_IMAGE_REQUEST1 = 111;
    int PICK_IMAGE_REQUEST2 = 112;
    int PICK_IMAGE_REQUEST3 = 113;
    int PICK_IMAGE_REQUEST4 = 114;
    int PICK_IMAGE_REQUEST5 = 115;
    int PICK_IMAGE_REQUEST6 = 116;
    Uri ImageFilePath1, ImageFilePath2, ImageFilePath3, ImageFilePath4, ImageFilePath5, ImageFilePath6;

    final String [] states = new String[]{"Andra Pradesh","Arunachal Pradesh","Assam","Bihar","Chhattisgarh","Goa","Gujarat"
            ,"Haryana","Himachal Pradesh","Jammu and Kashmir","Jharkhand","Karnataka","Kerala","Madya Pradesh","Maharashtra"
            ,"Manipur","Meghalaya","Mizoram","Nagaland","Orissa","Punjab","Rajasthan","Sikkim"
            ,"Tamil Nadu","Tripura","Uttaranchal","Uttar Pradesh","West Bengal","Delhi","Lakshadeep","Pondicherry"
            ,"Andaman and Nicobar","Chandigarh","Dadar and Nagar","Daman and Diu"};


    public BreakDown() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_break_down, container, false);

        SharedPreferences shared = getActivity().getSharedPreferences("firstLog", MODE_PRIVATE);
        try{
            contactUID_tx = (shared.getString("contact_uid", ""));
        }
        catch (NullPointerException e){
            contactUID_tx  = "";
        }
        dialog_updating = new SpotsDialog(getActivity(), R.style.dialog_updating);

        choiceGroup = view.findViewById(R.id.break_lossSelector);
        addResource_btn = view.findViewById(R.id.break_addResource);
        removeResource_btn = view.findViewById(R.id.break_removeResource);
        addPhoto_btn = view.findViewById(R.id.break_addPhoto);
        removePhoto_btn = view.findViewById(R.id.break_removePhoto);
        addBrokenPartPhoto_btn = view.findViewById(R.id.break_addBrokenPartPhoto);
        removeBrokenPartPhoto_btn = view.findViewById(R.id.break_removeBrokenPartPhoto);
        failure_et = view.findViewById(R.id.break_failureEditText);
        address_et = view.findViewById(R.id.break_locationEditText);
        stateName_et = view.findViewById(R.id.break_stateEditText);
        submit_btn = view.findViewById(R.id.break_submitButton);

        resourcePrice1_et = view.findViewById(R.id.break_resourceAmount1);
        resourcePrice2_et = view.findViewById(R.id.break_resourceAmount2);
        resourcePrice3_et = view.findViewById(R.id.break_resourceAmount3);
        resourcePrice4_et = view.findViewById(R.id.break_resourceAmount4);
        resourcePrice5_et = view.findViewById(R.id.break_resourceAmount5);

        resourceQuantity1_et = view.findViewById(R.id.break_resourceQuantity1);
        resourceQuantity2_et = view.findViewById(R.id.break_resourceQuantity2);
        resourceQuantity3_et = view.findViewById(R.id.break_resourceQuantity3);
        resourceQuantity4_et = view.findViewById(R.id.break_resourceQuantity4);
        resourceQuantity5_et = view.findViewById(R.id.break_resourceQuantity5);

        resourceUsed1_et = view.findViewById(R.id.break_resourceName1);
        resourceUsed2_et = view.findViewById(R.id.break_resourceName2);
        resourceUsed3_et = view.findViewById(R.id.break_resourceName3);
        resourceUsed4_et = view.findViewById(R.id.break_resourceName4);
        resourceUsed5_et = view.findViewById(R.id.break_resourceName5);

        billPrice1_et = view.findViewById(R.id.break_billEditText1);
        billPrice2_et = view.findViewById(R.id.break_billEditText2);
        billPrice3_et = view.findViewById(R.id.break_billEditText3);

        selectBillPhoto1_btn = view.findViewById(R.id.break_selectBillPhoto1);
        selectBillPhoto2_btn = view.findViewById(R.id.break_selectBillPhoto2);
        selectBillPhoto3_btn = view.findViewById(R.id.break_selectBillPhoto3);

        selectBrokenPhoto1_btn = view.findViewById(R.id.break_brokenPhotoButton1);
        selectBrokenPhoto2_btn = view.findViewById(R.id.break_brokenPhotoButton2);
        selectBrokenPhoto3_btn = view.findViewById(R.id.break_brokenPhotoButton3);

        billImage1 = view.findViewById(R.id.break_billImageView1);
        billImage2 = view.findViewById(R.id.break_billImageView2);
        billImage3 = view.findViewById(R.id.break_billImageView3);

        brokenImage4 = view.findViewById(R.id.break_brokenImageView4);
        brokenImage5 = view.findViewById(R.id.break_brokenImageView5);
        brokenImage6 = view.findViewById(R.id.break_brokenImageView6);



        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(),android.R.layout.simple_list_item_1,states);
        stateName_et.setAdapter(adapter);


        choiceGroup.setPosition(1);
        choiceGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                if (position == 0){
                    gate = "yes";
                    view.findViewById(R.id.break_relative1).setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn)
                            .duration(700)
                            .repeat(0)
                            .playOn( view.findViewById(R.id.break_relative1));
                }
                else {
                    gate = "no";
                    YoYo.with(Techniques.FadeOut)
                            .duration(700)
                            .repeat(0)
                            .playOn( view.findViewById(R.id.break_relative1));

                    resourcePrice1_et.setText(null);
                    resourcePrice2_et.setText(null);
                    resourcePrice3_et.setText(null);
                    resourcePrice4_et.setText(null);
                    resourcePrice5_et.setText(null);
                    resourceQuantity1_et.setText(null);
                    resourceQuantity2_et.setText(null);
                    resourceQuantity3_et.setText(null);
                    resourceQuantity4_et.setText(null);
                    resourceQuantity5_et.setText(null);
                    resourceUsed1_et.setText(null);
                    resourceUsed2_et.setText(null);
                    resourceUsed3_et.setText(null);
                    resourceUsed4_et.setText(null);
                    resourceUsed5_et.setText(null);
                    billPrice1_et.setText(null);
                    billPrice2_et.setText(null);
                    billPrice3_et.setText(null);
                    ImageFilePath1 = null;
                    ImageFilePath2 = null;
                    ImageFilePath3 = null;
                    ImageFilePath4 = null;
                    ImageFilePath5 = null;
                    ImageFilePath6 = null;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.findViewById(R.id.break_relative1).setVisibility(View.GONE);
                        }
                    },500);










                }
            }
        });


        addResource_btn.setOnClickListener(this);
        removeResource_btn.setOnClickListener(this);
        addPhoto_btn.setOnClickListener(this);
        removePhoto_btn.setOnClickListener(this);
        addBrokenPartPhoto_btn.setOnClickListener(this);
        removeBrokenPartPhoto_btn.setOnClickListener(this);
        submit_btn.setOnClickListener(this);


        selectBillPhoto1_btn.setOnClickListener(this);
        selectBillPhoto2_btn.setOnClickListener(this);
        selectBillPhoto3_btn.setOnClickListener(this);

        selectBrokenPhoto1_btn.setOnClickListener(this);
        selectBrokenPhoto2_btn.setOnClickListener(this);
        selectBrokenPhoto3_btn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id){

            case R.id.break_addResource:

                if (countResource == 2){

                    if (TextUtils.isEmpty(resourceUsed1_et.getText().toString().trim())  || TextUtils.isEmpty(resourcePrice1_et.getText().toString().trim())){
                        Alerter.create(getActivity())
                                .setTitle("Use Resource 1!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();

                        dialog_updating.dismiss();
                        return;

                    }

                    view.findViewById(R.id.break_resourceHeader2).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.break_resourceLinear2).setVisibility(View.VISIBLE);
                    removeResource = 2;
                    countResource=3;
                    return;
                }

                if (countResource == 3){

                    if (TextUtils.isEmpty(resourceUsed2_et.getText().toString().trim())  || TextUtils.isEmpty(resourcePrice2_et.getText().toString().trim())){
                        Alerter.create(getActivity())
                                .setTitle("Use Resource 2!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();

                        dialog_updating.dismiss();
                        return;

                    }

                    view.findViewById(R.id.break_resourceHeader3).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.break_resourceLinear3).setVisibility(View.VISIBLE);
                    removeResource = 3;
                    countResource=4;
                    return;
                }

                if (countResource == 4){

                    if (TextUtils.isEmpty(resourceUsed3_et.getText().toString().trim())  || TextUtils.isEmpty(resourcePrice3_et.getText().toString().trim())){
                        Alerter.create(getActivity())
                                .setTitle("Use Resource 3!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();

                        dialog_updating.dismiss();
                        return;

                    }
                    view.findViewById(R.id.break_resourceHeader4).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.break_resourceLinear4).setVisibility(View.VISIBLE);
                    removeResource = 4;
                    countResource=5;
                    return;
                }

                if (countResource == 5){

                    if (TextUtils.isEmpty(resourceUsed4_et.getText().toString().trim())  || TextUtils.isEmpty(resourcePrice4_et.getText().toString().trim())){
                        Alerter.create(getActivity())
                                .setTitle("Use Resource 4!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();

                        dialog_updating.dismiss();
                        return;

                    }
                    view.findViewById(R.id.break_resourceHeader5).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.break_resourceLinear5).setVisibility(View.VISIBLE);
                    removeResource = 5;

                }
             break;


            case R.id.break_removeResource:

                if (removeResource == 2){
                    view.findViewById(R.id.break_resourceHeader2).setVisibility(View.GONE);
                    view.findViewById(R.id.break_resourceLinear2).setVisibility(View.GONE);
                    countResource = 2;
                    resourcePrice2_et.setText("");
                    resourceQuantity2_et.setText("");
                    resourceUsed2_et.setText("");
                    removeResource = 1;
                    return;
                }

                if (removeResource == 3){
                    view.findViewById(R.id.break_resourceHeader3).setVisibility(View.GONE);
                    view.findViewById(R.id.break_resourceLinear3).setVisibility(View.GONE);
                    countResource=3;
                    removeResource = 2;
                    resourcePrice3_et.setText("");
                    resourceQuantity3_et.setText("");
                    resourceUsed3_et.setText("");
                    return;
                }

                if (removeResource == 4){
                    view.findViewById(R.id.break_resourceHeader4).setVisibility(View.GONE);
                    view.findViewById(R.id.break_resourceLinear4).setVisibility(View.GONE);
                    countResource=4;
                    removeResource = 3;
                    resourcePrice4_et.setText("");
                    resourceQuantity4_et.setText("");
                    resourceUsed4_et.setText("");
                    return;
                }

                if (removeResource == 5){
                    view.findViewById(R.id.break_resourceHeader5).setVisibility(View.GONE);
                    view.findViewById(R.id.break_resourceLinear5).setVisibility(View.GONE);
                    countResource=5;
                    removeResource = 4;
                    resourcePrice5_et.setText("");
                    resourceQuantity5_et.setText("");
                    resourceUsed5_et.setText("");

                }

                if (removeResource == 1){
                    resourcePrice1_et.setText("");
                    resourceQuantity1_et.setText("");
                    resourceUsed1_et.setText("");
                }

           break;


            case R.id.break_addPhoto:

                if (countPhoto == 2){
                    if (ImageFilePath1 == null){
                        Alerter.create(getActivity())
                                .setTitle("Use Bill Photo 1!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();

                        dialog_updating.dismiss();
                        return;

                    }

                    view.findViewById(R.id.break_billPhotoHeader2).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.break_billPhotoLinear2).setVisibility(View.VISIBLE);
                    removePhoto = 2;
                    countPhoto = 3;
                    return;
                }

                if (countPhoto == 3){

                        if (ImageFilePath2 == null){
                            Alerter.create(getActivity())
                                    .setTitle("Use Bill Photo 2!")
                                    .setContentGravity(1)
                                    .setBackgroundColorRes(R.color.black)
                                    .setIcon(R.drawable.error)
                                    .show();

                            dialog_updating.dismiss();
                            return;

                        }

                    view.findViewById(R.id.break_billPhotoHeader3).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.break_billPhotoLinear3).setVisibility(View.VISIBLE);
                    removePhoto = 3;
                    return;
                }

                break;


            case R.id.break_removePhoto:

               if (removePhoto == 2){
                    ImageFilePath2 = null;
                    view.findViewById(R.id.break_billPhotoHeader2).setVisibility(View.GONE);
                    view.findViewById(R.id.break_billPhotoLinear2).setVisibility(View.GONE);
                    billImage2.setVisibility(View.GONE);
                    billPrice2_et.setText("");
                    countPhoto = 2;
                    removePhoto = 1;
                    return;
                }

                if (removePhoto == 3){
                    ImageFilePath3 = null;
                    view.findViewById(R.id.break_billPhotoHeader3).setVisibility(View.GONE);
                    view.findViewById(R.id.break_billPhotoLinear3).setVisibility(View.GONE);
                    billImage3.setVisibility(View.GONE);
                    billPrice3_et.setText("");
                    countPhoto = 3;
                    removePhoto = 2;
                    return;

                }
                if (removePhoto == 1){
                    ImageFilePath1 = null;
                    billImage1.setVisibility(View.GONE);
                    billPrice1_et.setText("");
                }


        break;



            case R.id.break_addBrokenPartPhoto:


                if (countPhoto1 == 2){

                    if (ImageFilePath4 == null){
                        Alerter.create(getActivity())
                                .setTitle("Use Broken Photo 1!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();

                        dialog_updating.dismiss();
                        return;

                    }


                    view.findViewById(R.id.break_brokenPhotoHeader2).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.break_brokenPhotoButton2).setVisibility(View.VISIBLE);
                    removePhoto1 = 2;
                    countPhoto1 = 3;
                    return;
                }

                if (countPhoto1 == 3){

                    if (ImageFilePath5 == null){
                        Alerter.create(getActivity())
                                .setTitle("Use Broken Photo 2!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();

                        dialog_updating.dismiss();
                        return;

                    }
                    view.findViewById(R.id.break_brokenPhotoHeader3).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.break_brokenPhotoButton3).setVisibility(View.VISIBLE);
                    removePhoto1 = 3;
                    return;
                }

                break;


            case R.id.break_removeBrokenPartPhoto:

                if (removePhoto1 == 2){

                    ImageFilePath5 = null;
                    view.findViewById(R.id.break_brokenPhotoHeader2).setVisibility(View.GONE);
                    view.findViewById(R.id.break_brokenPhotoButton2).setVisibility(View.GONE);
                    brokenImage5.setVisibility(View.GONE);
                    countPhoto1 = 2;
                    removePhoto1 = 1;
                    return;
                }

                if (removePhoto1 == 3){

                    if (ImageFilePath5 == null){
                        Alerter.create(getActivity())
                                .setTitle("Use Broken part photo 5!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();

                        dialog_updating.dismiss();
                        return;

                    }

                    ImageFilePath6 = null;
                    view.findViewById(R.id.break_brokenPhotoHeader3).setVisibility(View.GONE);
                    view.findViewById(R.id.break_brokenPhotoButton3).setVisibility(View.GONE);
                    brokenImage6.setVisibility(View.GONE);
                    countPhoto1 = 3;
                    removePhoto1 = 2;

                }

                if (removePhoto1 == 1){
                    ImageFilePath4 = null;
                    brokenImage4.setVisibility(View.GONE);

                }

                break;


            case R.id.break_submitButton:
                dialog_updating.show();



                failure_tx = failure_et.getText().toString().trim();
                address_tx = address_et.getText().toString().trim();
                stateName_tx = stateName_et.getText().toString().trim();

                resourcePrice1_tx = resourcePrice1_et.getText().toString().trim();
                resourcePrice2_tx = resourcePrice2_et.getText().toString().trim();
                resourcePrice3_tx = resourcePrice3_et.getText().toString().trim();
                resourcePrice4_tx = resourcePrice4_et.getText().toString().trim();
                resourcePrice5_tx = resourcePrice5_et.getText().toString().trim();

                resourceQuantity1_tx = resourceQuantity1_et.getText().toString().trim();
                resourceQuantity2_tx = resourceQuantity2_et.getText().toString().trim();
                resourceQuantity3_tx = resourceQuantity3_et.getText().toString().trim();
                resourceQuantity4_tx = resourceQuantity4_et.getText().toString().trim();
                resourceQuantity5_tx = resourceQuantity5_et.getText().toString().trim();

                resourceUsed1_tx = resourceUsed1_et.getText().toString().trim();
                resourceUsed2_tx = resourceUsed2_et.getText().toString().trim();
                resourceUsed3_tx = resourceUsed3_et.getText().toString().trim();
                resourceUsed4_tx = resourceUsed4_et.getText().toString().trim();
                resourceUsed5_tx = resourceUsed5_et.getText().toString().trim();

                billPrice1_tx = billPrice1_et.getText().toString().trim();
                billPrice2_tx = billPrice2_et.getText().toString().trim();
                billPrice3_tx = billPrice3_et.getText().toString().trim();


                if (!isNetworkAvailable()){
                    Alerter.create(getActivity())
                            .setTitle("No Internet Connection!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.no_internet)
                            .show();
                    dialog_updating.dismiss();
                    return;
                }




                if (TextUtils.isEmpty(failure_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Failure Name Required!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    dialog_updating.dismiss();
                    return;
                }

                if (TextUtils.isEmpty(address_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Address Required!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    dialog_updating.dismiss();
                    return;
                }

                if (TextUtils.equals(gate, "yes")){
                    if (TextUtils.isEmpty(resourcePrice1_tx) || TextUtils.isEmpty(resourceQuantity1_tx) || TextUtils.isEmpty(resourceUsed1_tx))
                    {
                        Alerter.create(getActivity())
                                .setTitle("Resource Used details Required!")
                                .setContentGravity(1)
                                .setBackgroundColorRes(R.color.black)
                                .setIcon(R.drawable.error)
                                .show();
                        dialog_updating.dismiss();
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
                            dialog_updating.dismiss();
                            return;
                        }

                        Query query1 = d_root.child("trip_details").child(contactUID_tx).orderByKey().limitToLast(1);

                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot child: dataSnapshot.getChildren()) {
                                    //  trip_check = child.child("status").getValue().toString();

                                    key = child.getKey();

                                    d_failure = d_root.child("trip_details").child(contactUID_tx)
                                            .child(key).child("failure");

                                    secondKey = d_failure.push().getKey();

                                    d_failure.addListenerForSingleValueEvent(new ValueEventListener() {
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

                                            if (TextUtils.equals(gate,"no")){
                                                d_failure.child(secondKey).child("failure_name").setValue(failure_tx);
                                                d_failure.child(secondKey).child("address").setValue(address_tx);
                                                d_failure.child(secondKey).child("state_name").setValue(stateName_tx);

                                                Alerter.create(getActivity())
                                                        .setTitle("Success")
                                                        .setContentGravity(1)
                                                        .setBackgroundColorRes(R.color.black)
                                                        .setIcon(R.drawable.success_icon)
                                                        .show();

                                                dialog_updating.dismiss();

                                            }
                                            else {

                                                d_failure.child(secondKey).child("failure_name").setValue(failure_tx);
                                                d_failure.child(secondKey).child("address").setValue(address_tx);
                                                d_failure.child(secondKey).child("state_name").setValue(stateName_tx);
                                                d_failure.child(secondKey).child("resource_name1").setValue(resourceUsed1_tx);
                                                d_failure.child(secondKey).child("resource_price1").setValue(resourcePrice1_tx);
                                                d_failure.child(secondKey).child("resource_quantity1").setValue(resourceQuantity1_tx);



                                                if (TextUtils.equals(resourceQuantity2_tx, null) || TextUtils.equals(resourceQuantity2_tx, "")){
                                                    d_failure.child(secondKey).child("resource_name2").setValue(null);
                                                    d_failure.child(secondKey).child("resource_price2").setValue(null);
                                                    d_failure.child(secondKey).child("resource_quantity2").setValue(resourceQuantity2_tx);
                                                }
                                                else {
                                                    d_failure.child(secondKey).child("resource_name2").setValue(resourceUsed2_tx);
                                                    d_failure.child(secondKey).child("resource_price2").setValue(resourcePrice2_tx);
                                                    d_failure.child(secondKey).child("resource_quantity2").setValue(resourceQuantity2_tx);
                                                }

                                                if (TextUtils.equals(resourceQuantity3_tx, null) || TextUtils.equals(resourceQuantity3_tx, "")){
                                                    d_failure.child(secondKey).child("resource_name3").setValue(null);
                                                    d_failure.child(secondKey).child("resource_price3").setValue(null);
                                                    d_failure.child(secondKey).child("resource_quantity3").setValue(null);
                                                }
                                                else {
                                                    d_failure.child(secondKey).child("resource_name3").setValue(resourceUsed3_tx);
                                                    d_failure.child(secondKey).child("resource_price3").setValue(resourcePrice3_tx);
                                                    d_failure.child(secondKey).child("resource_quantity3").setValue(resourceQuantity3_tx);
                                                }

                                                if (TextUtils.equals(resourceQuantity4_tx, null) || TextUtils.equals(resourceQuantity4_tx, "")){
                                                    d_failure.child(secondKey).child("resource_name4").setValue(null);
                                                    d_failure.child(secondKey).child("resource_price4").setValue(null);
                                                    d_failure.child(secondKey).child("resource_quantity4").setValue(null);
                                                }
                                                else {
                                                    d_failure.child(secondKey).child("resource_name4").setValue(resourceUsed4_tx);
                                                    d_failure.child(secondKey).child("resource_price4").setValue(resourcePrice4_tx);
                                                    d_failure.child(secondKey).child("resource_quantity4").setValue(resourceQuantity4_tx);
                                                }

                                                if (TextUtils.equals(resourceQuantity5_tx, null) || TextUtils.equals(resourceQuantity5_tx, "")){
                                                    d_failure.child(secondKey).child("resource_name5").setValue(null);
                                                    d_failure.child(secondKey).child("resource_price5").setValue(null);
                                                    d_failure.child(secondKey).child("resource_quantity5").setValue(null);
                                                }
                                                else {
                                                    d_failure.child(secondKey).child("resource_name5").setValue(resourceUsed5_tx);
                                                    d_failure.child(secondKey).child("resource_price5").setValue(resourcePrice5_tx);
                                                    d_failure.child(secondKey).child("resource_quantity5").setValue(resourceQuantity5_tx);
                                                }

                                                d_failure.child(secondKey).child("bill_paid1").setValue(billPrice1_tx);
                                                d_failure.child(secondKey).child("bill_paid2").setValue(billPrice2_tx);
                                                d_failure.child(secondKey).child("bill_paid3").setValue(billPrice3_tx);
                                            }

                                            d_failure.child(secondKey).child("gps_latitude").setValue(latitude);
                                            d_failure.child(secondKey).child("gps_longitude").setValue(longitude);
                                            d_failure.child(secondKey).child("gps_location").setValue(gps_address);

                                            d_failure.child(secondKey).child("date_time").setValue(actualDate+"_"+actualTime);

                                            UploadImage1();
                                            UploadImage2();
                                            UploadImage3();
                                            UploadImage4();
                                            UploadImage5();
                                            UploadImage6();
                                            // this will be used after testing....    better usage of firebase database
                                          /*  if (TextUtils.isEmpty(resourceUsed2_tx)){
                                                d_failure.child("failure"+size).child("resources_used").child("resource_name2").setValue(null);

                                            } */

                                            Alerter.create(getActivity())
                                                    .setTitle("Success")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.success_icon)
                                                    .show();

                                            dialog_updating.dismiss();


                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Alerter.create(getActivity())
                                                    .setTitle("Error! Try Again")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.error)
                                                    .show();
                                            dialog_updating.dismiss();

                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getActivity(), ""+databaseError.toException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getActivity(), ""+databaseError.toException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });





                break;


            case R.id.break_selectBillPhoto1:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST1);


                break;

            case R.id.break_selectBillPhoto2:
                Intent intent1 = new Intent();
                intent1.setType("image/*");
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent1, "Select Image"), PICK_IMAGE_REQUEST2);


                break;

            case R.id.break_selectBillPhoto3:
                Intent intent2 = new Intent();
                intent2.setType("image/*");
                intent2.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent2, "Select Image"), PICK_IMAGE_REQUEST3);
                break;



            case R.id.break_brokenPhotoButton1:
                Intent intent3 = new Intent();
                intent3.setType("image/*");
                intent3.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent3, "Select Image"), PICK_IMAGE_REQUEST4);


                break;

            case R.id.break_brokenPhotoButton2:
                Intent intent4 = new Intent();
                intent4.setType("image/*");
                intent4.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent4, "Select Image"), PICK_IMAGE_REQUEST5);


                break;

            case R.id.break_brokenPhotoButton3:
                Intent intent5 = new Intent();
                intent5.setType("image/*");
                intent5.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent5, "Select Image"), PICK_IMAGE_REQUEST6);


                break;

        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageFilePath1 = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), ImageFilePath1);
                billImage1.setVisibility(View.VISIBLE);
                billImage1.setImageBitmap(bitmap);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageFilePath2 = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), ImageFilePath2);
                billImage2.setVisibility(View.VISIBLE);
                billImage2.setImageBitmap(bitmap);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (requestCode == PICK_IMAGE_REQUEST3 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageFilePath3 = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), ImageFilePath3);
                billImage3.setVisibility(View.VISIBLE);
                billImage3.setImageBitmap(bitmap);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST4 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageFilePath4 = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), ImageFilePath4);
                brokenImage4.setVisibility(View.VISIBLE);
                brokenImage4.setImageBitmap(bitmap);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST5 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageFilePath5 = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), ImageFilePath5);
                brokenImage5.setVisibility(View.VISIBLE);
                brokenImage5.setImageBitmap(bitmap);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST6 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageFilePath6 = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), ImageFilePath6);
                brokenImage6.setVisibility(View.VISIBLE);
                brokenImage6.setImageBitmap(bitmap);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



    private void UploadImage1(){

        try {

        if (ImageFilePath1 != null) {

            File actualImage = FileUtils.getFile(getActivity(), ImageFilePath1);
            try {

                File compressedImageFile = new Compressor(getActivity())
                        .setQuality(15)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .compressToFile(actualImage);
                ImageFilePath1 = Uri.fromFile(compressedImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            StorageReference childRef = storageRef.child("trip_details/").child(contactUID_tx+"/")
                    .child(key+"/").child("failure").child(secondKey).child("/resources_used/").child("bill_photos/").child("photo1/").child("bill_image.jpg");


            childRef.putFile(ImageFilePath1)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            @SuppressWarnings("VisibleForTests")
                            ImageUploadInfoPetrol imageUploadInfo = new ImageUploadInfoPetrol(taskSnapshot.getDownloadUrl().toString());

                            d_root.child("trip_details").child(contactUID_tx).child(key)
                                    .child("failure").child(secondKey).child("imageURL1").setValue(imageUploadInfo.imageURL);
                            //  dialog_uploadingPump.dismiss();
                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // dialog_uploadingPump.dismiss();
                            // Showing exception erro message.
                            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                        }
                    });
        }

    }
        catch (IllegalArgumentException e){
        e.printStackTrace();
    }

    }

    private void UploadImage2(){

        try{
        if (ImageFilePath2 != null) {

            File actualImage = FileUtils.getFile(getActivity(), ImageFilePath2);
            try {

                File compressedImageFile = new Compressor(getActivity())
                        .setQuality(15)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .compressToFile(actualImage);
                ImageFilePath2 = Uri.fromFile(compressedImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            StorageReference childRef = storageRef.child("trip_details/").child(contactUID_tx+"/")
                    .child(key+"/").child("failure").child(secondKey).child("/resources_used/").child("bill_photos/").child("photo2/").child("bill_image.jpg");


            childRef.putFile(ImageFilePath2)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            @SuppressWarnings("VisibleForTests")
                            ImageUploadInfoPetrol imageUploadInfo = new ImageUploadInfoPetrol(taskSnapshot.getDownloadUrl().toString());

                            d_root.child("trip_details").child(contactUID_tx).child(key)
                                    .child("failure").child(secondKey).child("imageURL2").setValue(imageUploadInfo.imageURL);
                            //  dialog_uploadingPump.dismiss();
                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // dialog_uploadingPump.dismiss();
                            // Showing exception erro message.
                            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                        }
                    });
        }

    }
        catch (IllegalArgumentException e){
        e.printStackTrace();
    }


    }

    private void UploadImage3(){

        try{

        if (ImageFilePath3 != null) {

            File actualImage = FileUtils.getFile(getActivity(), ImageFilePath3);
            try {

                File compressedImageFile = new Compressor(getActivity())
                        .setQuality(15)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .compressToFile(actualImage);
                ImageFilePath3 = Uri.fromFile(compressedImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            StorageReference childRef = storageRef.child("trip_details/").child(contactUID_tx+"/")
                    .child(key+"/").child("failure").child(secondKey).child("/resources_used/").child("bill_photos/").child("photo3/").child("bill_image.jpg");


            childRef.putFile(ImageFilePath3)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            @SuppressWarnings("VisibleForTests")
                            ImageUploadInfoPetrol imageUploadInfo = new ImageUploadInfoPetrol(taskSnapshot.getDownloadUrl().toString());

                            d_root.child("trip_details").child(contactUID_tx).child(key)
                                    .child("failure").child(secondKey).child("imageURL3").setValue(imageUploadInfo.imageURL);
                            //  dialog_uploadingPump.dismiss();
                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // dialog_uploadingPump.dismiss();
                            // Showing exception erro message.
                            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                        }
                    });
        }

        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }

    }


    private void UploadImage4(){

        try {


            if (ImageFilePath4 != null) {

                File actualImage = FileUtils.getFile(getActivity(), ImageFilePath4);
                try {

                    File compressedImageFile = new Compressor(getActivity())
                            .setQuality(15)
                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                            .compressToFile(actualImage);
                    ImageFilePath4 = Uri.fromFile(compressedImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                StorageReference childRef = storageRef.child("trip_details/").child(contactUID_tx + "/")
                        .child(key + "/").child("failure").child(secondKey).child("/broken_parts/").child("photo1/").child("broken_part_image.jpg");


                childRef.putFile(ImageFilePath4)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                @SuppressWarnings("VisibleForTests")
                                ImageUploadInfoPetrol imageUploadInfo = new ImageUploadInfoPetrol(taskSnapshot.getDownloadUrl().toString());

                                d_root.child("trip_details").child(contactUID_tx).child(key)
                                        .child("failure").child(secondKey).child("imageURL4").setValue(imageUploadInfo.imageURL);
                                //  dialog_uploadingPump.dismiss();
                            }
                        })
                        // If something goes wrong .
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {

                                // dialog_uploadingPump.dismiss();
                                // Showing exception erro message.
                                Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })

                        // On progress change upload time.
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                            }
                        });
            }

        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }

    }

    private void UploadImage5(){

        try{

        if (ImageFilePath5 != null) {

            File actualImage = FileUtils.getFile(getActivity(), ImageFilePath5);
            try {

                File compressedImageFile = new Compressor(getActivity())
                        .setQuality(15)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .compressToFile(actualImage);
                ImageFilePath5 = Uri.fromFile(compressedImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            StorageReference childRef = storageRef.child("trip_details/").child(contactUID_tx+"/")
                    .child(key+"/").child("failure").child(secondKey).child("/broken_parts/").child("photo2/").child("broken_part_image.jpg");


            childRef.putFile(ImageFilePath5)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            @SuppressWarnings("VisibleForTests")
                            ImageUploadInfoPetrol imageUploadInfo = new ImageUploadInfoPetrol(taskSnapshot.getDownloadUrl().toString());

                            d_root.child("trip_details").child(contactUID_tx).child(key)
                                    .child("failure").child(secondKey).child("imageURL5").setValue(imageUploadInfo.imageURL);
                            //  dialog_uploadingPump.dismiss();
                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // dialog_uploadingPump.dismiss();
                            // Showing exception erro message.
                            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                        }
                    });
        }

        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }

    }

    private void UploadImage6(){

        try{

        if (ImageFilePath6 != null) {

            File actualImage = FileUtils.getFile(getActivity(), ImageFilePath6);
            try {

                File compressedImageFile = new Compressor(getActivity())
                        .setQuality(15)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .compressToFile(actualImage);
                ImageFilePath6 = Uri.fromFile(compressedImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            StorageReference childRef = storageRef.child("trip_details/").child(contactUID_tx+"/")
                    .child(key+"/").child("failure").child(secondKey).child("/broken_parts/").child("photo3/").child("broken_part_image.jpg");


            childRef.putFile(ImageFilePath6)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            @SuppressWarnings("VisibleForTests")
                            ImageUploadInfoPetrol imageUploadInfo = new ImageUploadInfoPetrol(taskSnapshot.getDownloadUrl().toString());

                            d_root.child("trip_details").child(contactUID_tx).child(key)
                                    .child("failure").child(secondKey).child("imageURL6").setValue(imageUploadInfo.imageURL);
                            //  dialog_uploadingPump.dismiss();
                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // dialog_uploadingPump.dismiss();
                            // Showing exception erro message.
                            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                        }
                    });
        }

        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
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

    //end
}
