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
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.iceteck.silicompressorr.SiliCompressor;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import im.delight.android.location.SimpleLocation;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class PetrolFilling extends Fragment {

    private View view;
    AutoCompleteTextView stateName_et;
    AutoCompleteTextView pumpName_et;
    EditText  pump_address_et, moneyPaid_et, petrolFilled_et, pump_token_et;
    String pumpName_tx, pump_address_tx, stateName_tx, moneyPaid_tx, petrolFilled_tx, pump_token_tx, actualFuelRate;

    FancyButton submit, select_image, cancel_image;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    private DatabaseReference d_root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference d_petrol_filling_details;

    DatabaseReference d_networkStatus = FirebaseDatabase.getInstance().getReference().child("checkNetwork").child("isConnected");

    AlertDialog dialog_petrol;
    String contactUID_tx, connected;
    int PICK_IMAGE_REQUEST = 111;
    Uri ImageFilePath;
    AlertDialog dialog_uploading;
    ImageView bill_img;

    private  String key, secondKey, pumpDateTime;
    int size;

    Spinner spinner;
    final String [] states = new String[]{"Andra Pradesh","Arunachal Pradesh","Assam","Bihar","Chhattisgarh","Goa","Gujarat"
            ,"Haryana","Himachal Pradesh","Jammu and Kashmir","Jharkhand","Karnataka","Kerala","Madya Pradesh","Maharashtra"
            ,"Manipur","Meghalaya","Mizoram","Nagaland","Orissa","Punjab","Rajasthan","Sikkim"
            ,"Tamil Nadu","Tripura","Uttaranchal","Uttar Pradesh","West Bengal","Delhi","Lakshadeep","Pondicherry"
            ,"Andaman and Nicobar","Chandigarh","Dadar and Nagar","Daman and Diu"};

    public PetrolFilling() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_petrol_filling, container, false);



        pumpName_et = view.findViewById(R.id.petrol_pumpNameEditText);
        pump_address_et = view.findViewById(R.id.petrol_locationNameEditText);
        stateName_et = view.findViewById(R.id.petrol_stateNameEditText);
        moneyPaid_et = view.findViewById(R.id.petrol_amountPaidEditText);
        petrolFilled_et = view.findViewById(R.id.petrol_quantityEditText);
        submit = view.findViewById(R.id.petrol_submitPetrolDetails);
        select_image = view.findViewById(R.id.petrol_selectBillPhoto);
        cancel_image = view.findViewById(R.id.petrol_cancelImage);
        bill_img = view.findViewById(R.id.petrol_billImage);


        pump_token_et = view.findViewById(R.id.petrol_tokenNumberEditText);

        spinner = view.findViewById(R.id.petrol_spinnerPumpName);

        dialog_petrol = new SpotsDialog(getActivity(), R.style.dialog_petrolFilled);
        dialog_uploading = new SpotsDialog(getActivity(), R.style.dialog_uploadingImage);

        SharedPreferences shared = getActivity().getSharedPreferences("firstLog", MODE_PRIVATE);

        try{
            contactUID_tx = (shared.getString("contact_uid", ""));
        }
        catch (NullPointerException e){
            contactUID_tx  = "";
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(),android.R.layout.simple_list_item_1,states);
        stateName_et.setAdapter(adapter);

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


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog_petrol.show();
                pumpName_tx = pumpName_et.getText().toString().trim();
                pump_address_tx = pump_address_et.getText().toString().trim();
                stateName_tx = stateName_et.getText().toString().trim();
                moneyPaid_tx = moneyPaid_et.getText().toString().trim();
                petrolFilled_tx = petrolFilled_et.getText().toString().trim();
                pump_token_tx = pump_token_et.getText().toString().trim();


                if (!isNetworkAvailable()){
                    Alerter.create(getActivity())
                            .setTitle("No Internet Connection!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.no_internet)
                            .show();
                    dialog_petrol.dismiss();
                    return;
                }

                if (pumpName_tx.length()<3){
                    Alerter.create(getActivity())
                            .setTitle("Enter full name of Pump!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    dialog_petrol.dismiss();
                    return;
                }

                if (TextUtils.isEmpty(pump_token_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Enter Token Number!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    dialog_petrol.dismiss();
                    return;
                }

                if (TextUtils.isEmpty(petrolFilled_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Enter Petrol filled!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    dialog_petrol.dismiss();
                    return;
                }




                if (TextUtils.isEmpty(moneyPaid_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Enter Petrol Rate!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    dialog_petrol.dismiss();
                    return;
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
                            dialog_petrol.dismiss();
                            return;
                        }

                        Query query1 = d_root.child("trip_details").child(contactUID_tx).orderByKey().limitToLast(1);

                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot child: dataSnapshot.getChildren()) {
                                    //  trip_check = child.child("status").getValue().toString();

                                    key = child.getKey();


                                    d_petrol_filling_details = d_root.child("trip_details").child(contactUID_tx)
                                            .child(key).child("petrol_filled");

                                    secondKey = d_petrol_filling_details.push().getKey();

                                    d_root.child("fuel_rate").child(pumpName_tx)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    actualFuelRate = dataSnapshot.child("current_rate").getValue(String.class);
                                                    pumpDateTime = dataSnapshot.child("updated_on").getValue(String.class);



                                    d_petrol_filling_details.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            int total = Integer.parseInt(moneyPaid_tx) * Integer.parseInt(petrolFilled_tx);

                                            String latitude = String.valueOf(getLatitude());
                                            String longitude = String.valueOf(getLongitude());
                                            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                            String gps_address="";

                                            try {
                                                List<Address> addresses = null;
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

                                            d_petrol_filling_details.child(secondKey).child("name").setValue(pumpName_tx);
                                            d_petrol_filling_details.child(secondKey).child("address").setValue(pump_address_tx);
                                            d_petrol_filling_details.child(secondKey).child("state").setValue(stateName_tx);
                                            d_petrol_filling_details.child(secondKey).child("money_paid").setValue(moneyPaid_tx);
                                            d_petrol_filling_details.child(secondKey).child("petrol_filled").setValue(petrolFilled_tx);
                                            d_petrol_filling_details.child(secondKey).child("token_number").setValue(pump_token_tx);
                                            d_petrol_filling_details.child(secondKey).child("gps_latitude").setValue(latitude);
                                            d_petrol_filling_details.child(secondKey).child("gps_longitude").setValue(longitude);
                                            d_petrol_filling_details.child(secondKey).child("gps_location").setValue(gps_address);
                                            d_petrol_filling_details.child(secondKey).child("total_money").setValue(String.valueOf(total));
                                            d_petrol_filling_details.child(secondKey).child("pump_fuel_rate").setValue(actualFuelRate);
                                            d_petrol_filling_details.child(secondKey).child("pump_fuel_rate_date").setValue(pumpDateTime);

                                            d_petrol_filling_details.child(secondKey).child("date_time").setValue(actualDate+"_"+actualTime);


                                            Alerter.create(getActivity())
                                                    .setTitle("Success")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.success_icon)
                                                    .show();
                                            UploadImageFileToFirebaseStorage();
                                            dialog_petrol.dismiss();



                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                            Alerter.create(getActivity())
                                                    .setTitle("Error! Try Again")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.error)
                                                    .show();
                                            dialog_petrol.dismiss();
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


        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);

            }
        });

        cancel_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageFilePath = null;
                bill_img.setVisibility(View.GONE);
                select_image.setText("Select Image");
            }
        });


        return view;
    }

//    select image

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageFilePath = data.getData();

            try {

                // Getting selected image into Bitmap.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), ImageFilePath);

                // Setting up bitmap selected image into ImageView.
                bill_img.setVisibility(View.VISIBLE);
                bill_img.setImageBitmap(bitmap);

                // After selecting image change choose button above text.
                select_image.setText("Selected");

            }
            catch (IOException e) {

                e.printStackTrace();
            }
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


    public void UploadImageFileToFirebaseStorage() {

         try{

        if (ImageFilePath != null) {


            File actualImage = FileUtils.getFile(getActivity(), ImageFilePath);
            try {

                File compressedImageFile = new Compressor(getActivity())
                        .setQuality(15)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .compressToFile(actualImage);
                ImageFilePath = Uri.fromFile(compressedImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }



            StorageReference childRef = storageRef.child("trip_details/").child(contactUID_tx+"/")
                    .child(key+"/").child("petrol_filled_bill").child(secondKey+"/").child("bill_image.jpg");

            childRef.putFile(ImageFilePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            @SuppressWarnings("VisibleForTests")
                            ImageUploadInfoPetrol imageUploadInfo = new ImageUploadInfoPetrol(taskSnapshot.getDownloadUrl().toString());

                            d_root.child("trip_details").child(contactUID_tx).child(key)
                                    .child("petrol_filled").child(secondKey).child("imageURL").setValue(imageUploadInfo.imageURL);
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
