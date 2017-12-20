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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import im.delight.android.location.SimpleLocation;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class OtherFilling extends Fragment {

    View view;
    EditText fillingName_et, quantity_et, moneyPaid_et, description_et;
    String fillingName_tx, quantity_tx, moneyPaid_tx, description_tx;
    FancyButton submit_btn;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://rajpetroleum-4d3fa.appspot.com/");

    private DatabaseReference d_root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference d_other_filling_details;

    DatabaseReference d_networkStatus = FirebaseDatabase.getInstance().getReference().child("checkNetwork").child("isConnected");

    AlertDialog dialog_other;
    String contactUID_tx, connected;

    FancyButton select_image, cancel_image;

    int PICK_IMAGE_REQUEST = 111;
    Uri ImageFilePath;

    ImageView bill_img;

    private  String key, secondKey;
    int size;




    public OtherFilling() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_other_filling, container, false);
        fillingName_et = view.findViewById(R.id.other_fillingNameEditText);
        quantity_et = view.findViewById(R.id.other_quantityEditText);
        moneyPaid_et = view.findViewById(R.id.other_priceEditText);
        description_et = view.findViewById(R.id.other_descriptionEditText);
        submit_btn = view.findViewById(R.id.other_submitOtherDetails);
        dialog_other = new SpotsDialog(getActivity(), R.style.dialog_otherFilling);

        select_image = view.findViewById(R.id.other_selectBillPhoto);
        cancel_image = view.findViewById(R.id.other_cancelBillPhoto);
        bill_img = view.findViewById(R.id.other_billImage);

        SharedPreferences shared = getActivity().getSharedPreferences("firstLog", MODE_PRIVATE);

        try{
            contactUID_tx = (shared.getString("contact_uid", ""));
        }
        catch (NullPointerException e){
            contactUID_tx  = "";
        }



        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillingName_tx = fillingName_et.getText().toString().trim();
                quantity_tx = quantity_et.getText().toString().trim();
                moneyPaid_tx = moneyPaid_et.getText().toString().trim();
                description_tx = description_et.getText().toString().trim();
                if (!isNetworkAvailable()){
                    Alerter.create(getActivity())
                            .setTitle("No Internet Connection!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.no_internet)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(fillingName_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Provide Filling!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    return;
                }


                if (TextUtils.isEmpty(moneyPaid_tx)){
                    Alerter.create(getActivity())
                            .setTitle("Enter Money Paid!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.black)
                            .setIcon(R.drawable.error)
                            .show();
                    return;
                }


                d_networkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dialog_other.show();
                        connected = dataSnapshot.getValue(String.class);

                        if (!TextUtils.equals(connected, "connected")){
                            Alerter.create(getActivity())
                                    .setTitle("Unable to connect to server!")
                                    .setContentGravity(1)
                                    .setBackgroundColorRes(R.color.black)
                                    .setIcon(R.drawable.error)
                                    .show();
                            dialog_other.dismiss();
                            return;
                        }

                        Query query1 = d_root.child("trip_details").child(contactUID_tx).orderByKey().limitToLast(1);

                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot child: dataSnapshot.getChildren()) {
                                    //  trip_check = child.child("status").getValue().toString();

                                    key = child.getKey();
                                    Log.w("432", key);

                                    d_other_filling_details = d_root.child("trip_details").child(contactUID_tx)
                                            .child(key).child("other_filling");

                                    secondKey = d_other_filling_details.push().getKey();

                                    d_other_filling_details.addListenerForSingleValueEvent(new ValueEventListener() {
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

                                            d_other_filling_details.child(secondKey).child("filling_name").setValue(fillingName_tx);
                                            d_other_filling_details.child(secondKey).child("quantity").setValue(quantity_tx);
                                            d_other_filling_details.child(secondKey).child("money_paid").setValue(moneyPaid_tx);
                                            d_other_filling_details.child(secondKey).child("description").setValue(description_tx);
                                            d_other_filling_details.child(secondKey).child("gps_latitude").setValue(latitude);
                                            d_other_filling_details.child(secondKey).child("gps_longitude").setValue(longitude);
                                            d_other_filling_details.child(secondKey).child("gps_location").setValue(gps_address);

                                            d_other_filling_details.child(secondKey).child("date_time").setValue(actualDate+"_"+actualTime);

                                            Alerter.create(getActivity())
                                                    .setTitle("Success")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.success_icon)
                                                    .show();

                                            UploadImageFileToFirebaseStorage();
                                            dialog_other.dismiss();

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                            Alerter.create(getActivity())
                                                    .setTitle("Error! Try Again")
                                                    .setContentGravity(1)
                                                    .setBackgroundColorRes(R.color.black)
                                                    .setIcon(R.drawable.error)
                                                    .show();
                                            dialog_other.dismiss();
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

    //    internet check
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //    select image

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageFilePath = data.getData();


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



    public void UploadImageFileToFirebaseStorage() {


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
                    .child(key+"/").child("other_filling_bill/").child(secondKey+"/").child("other_bill_image.jpg");

            childRef.putFile(ImageFilePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            @SuppressWarnings("VisibleForTests")
                            ImageUploadInfoPetrol imageUploadInfo = new ImageUploadInfoPetrol(taskSnapshot.getDownloadUrl().toString());

                            d_root.child("trip_details").child(contactUID_tx).child(key)
                                    .child("other_filling").child(secondKey).child("imageURL").setValue(imageUploadInfo.imageURL);
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
