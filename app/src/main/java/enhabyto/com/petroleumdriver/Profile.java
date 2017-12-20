package enhabyto.com.petroleumdriver;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tapadoo.alerter.Alerter;

import util.android.textviews.FontTextView;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class Profile extends Fragment {

    private View view;

    private DatabaseReference d_root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference d_profile = d_root.child("driver_profiles");

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://rajpetroleum-4d3fa.appspot.com/");

    String name_tx, address_tx, birth_tx, licence_tx, hazardousLicence_tx,education_tx, marital_tx;
    FontTextView name_tv, address_tv, birth_tv, licence_tv, hazardousLicence_tv, education_tv, marital_tv;
    ImageView licence_image, hazardous_licence_image, profile_image;
    String contact_tx;



    public Profile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        name_tv = view.findViewById(R.id.profile_name);
        address_tv = view.findViewById(R.id.profile_address);
        birth_tv = view.findViewById(R.id.profile_birth);
        licence_tv = view.findViewById(R.id.profile_licence);
        hazardousLicence_tv = view.findViewById(R.id.profile_hazardousLicence);
        education_tv = view.findViewById(R.id.profile_education);
        marital_tv = view.findViewById(R.id.profile_marital);

        licence_image = view.findViewById(R.id.profile_drivingLicenceImage);
        hazardous_licence_image = view.findViewById(R.id.profile_hazardousDrivingLicenceImage);
        profile_image = view.findViewById(R.id.profile_cordinator_image);




        return view;
    }

    public void onStart(){
        super.onStart();
        if (!isNetworkAvailable()){
            Alerter.create(getActivity())
                    .setTitle("No Internet Connection!")
                    .setText("Some Modules may not work")
                    .setContentGravity(1)
                    .setBackgroundColorRes(R.color.black)
                    .setIcon(R.drawable.no_internet)
                    .show();
        }


       try{
            SharedPreferences shared = getActivity().getSharedPreferences("firstLog", MODE_PRIVATE);
            contact_tx = (shared.getString("contact_uid", ""));
        }
        catch (NullPointerException e){
            contact_tx  = "";
        }
        StorageReference load_licence_images = storageRef.child("driver_profiles/").child(contact_tx).child("licence_image/").child("licence.jpg");
        StorageReference load_hazardous_licence_images = storageRef.child("driver_profiles/").child(contact_tx).child("hazardous_licence_image/").child("hazardous_licence.jpg");
        StorageReference load_profile_image = storageRef.child("driver_profiles/").child(contact_tx).child("profile_image/").child("image.jpg");

        try {

            //            profile

            load_profile_image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (uri != null && getActivity().getIntent() != null){
                        Glide.with(getActivity())
                                .load(uri)
                                .fitCenter()
                                .centerCrop()
                                .crossFade(1200)
                                .error(R.drawable.driver_default_image_icon)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(profile_image);

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    if (getActivity().getIntent() != null) {
                        Glide.with(getActivity())
                                .load(R.drawable.driver_default_image_icon)
                                .fitCenter()
                                .centerCrop()
                                .crossFade(1200)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(profile_image);
                    }

                }
            });



//            licence image

            load_licence_images.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (uri != null && getActivity().getIntent() != null){
                        Glide.with(getActivity())
                                .load(uri)
                                .fitCenter()
                                .centerCrop()
                                .crossFade(1200)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(licence_image);
                        licence_image.setVisibility(View.VISIBLE);
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors

                }
            });

//            hazardous licence image
            load_hazardous_licence_images.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (uri != null && getActivity().getIntent() != null){
                        Glide.with(getActivity())
                                .load(uri)
                                .fitCenter()
                                .centerCrop()
                                .crossFade(1200)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(hazardous_licence_image);
                        hazardous_licence_image.setVisibility(View.VISIBLE);
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors

                }
            });


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }


        d_profile.child(contact_tx).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name_tx = dataSnapshot.child("name").getValue(String.class);
                address_tx = dataSnapshot.child("address").getValue(String.class);
                birth_tx = dataSnapshot.child("birth").getValue(String.class);
                licence_tx = dataSnapshot.child("driving_licence").getValue(String.class);
                hazardousLicence_tx = dataSnapshot.child("hazardous_driving_licence").getValue(String.class);
                education_tx = dataSnapshot.child("education").getValue(String.class);
                marital_tx = dataSnapshot.child("marital").getValue(String.class);

                name_tv.setText(name_tx);
                address_tv.setText(address_tx);
                birth_tv.setText(birth_tx);
                licence_tv.setText(licence_tx);
                hazardousLicence_tv.setText(hazardousLicence_tx);
                education_tv.setText(education_tx);
                marital_tv.setText(marital_tx);

                if (TextUtils.equals(name_tx,"")){
                    name_tv.setText("N A");

                }

                if (TextUtils.equals(address_tx,"")){
                    address_tv.setText("N A");

                }

                if (TextUtils.equals(birth_tx,"")){
                    birth_tv.setText("Not Available");

                }

                if (TextUtils.equals(licence_tx,"")){
                    licence_tv.setText("N A");

                }

                if (TextUtils.equals(hazardousLicence_tx,"")){
                    hazardousLicence_tv.setText("N A");

                }

                if (TextUtils.equals(education_tx,"")){
                    education_tv.setText("N A");

                }

                if (TextUtils.equals(marital_tx,"")){
                    marital_tv.setText("N A");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //end
}
