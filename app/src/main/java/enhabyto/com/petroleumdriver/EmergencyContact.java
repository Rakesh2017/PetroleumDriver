package enhabyto.com.petroleumdriver;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;
import util.android.textviews.FontTextView;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmergencyContact extends Fragment {

    private View view;

    FontTextView contact1_et,contact2_et,contact3_et;
    String contact1_tx,contact2_tx,contact3_tx;
    Dialog dialog;

    RelativeLayout r1, r2, r3;

    ImageView image1, image2, image3;

    public EmergencyContact() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_emergency_contact, container, false);

        contact1_et = view.findViewById(R.id.emergency_contact1EditText);
        contact2_et = view.findViewById(R.id.emergency_contact2EditText);
        contact3_et = view.findViewById(R.id.emergency_contact3EditText);

        image1 = view.findViewById(R.id.emergency_image1);
        image2 = view.findViewById(R.id.emergency_image2);
        image3 = view.findViewById(R.id.emergency_image3);

        r1 = view.findViewById(R.id.emergency1);
        r2 = view.findViewById(R.id.emergency2);
        r3 = view.findViewById(R.id.emergency3);

        dialog = new SpotsDialog(getActivity(),R.style.LoggingOut);







        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", contact1_tx, null));

                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getActivity(), "Give Call Permission from Settings", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(intent);
            }
        });

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", contact2_tx, null));
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {


                    Toast.makeText(getActivity(), "Give Call Permission from Settings", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(intent);
            }
        });

        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", contact3_tx, null));

                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {


                    Toast.makeText(getActivity(), "Give Call Permission from Settings", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(intent);
            }
        });

        return view;
    }


    public void onStart() {
        super.onStart();


        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
           @Override
          public void onDataChange(DataSnapshot snapshot) {

               boolean connected = snapshot.getValue(Boolean.class);
               if (connected) {

                   DatabaseReference root_contact = FirebaseDatabase.getInstance().getReference().child("emergency_contacts");

                   root_contact.addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {


                           dialog.show();
                           contact1_tx = dataSnapshot.child("contact1").getValue(String.class);
                           contact2_tx = dataSnapshot.child("contact2").getValue(String.class);
                           contact3_tx = dataSnapshot.child("contact3").getValue(String.class);
                           try {
                               SharedPreferences dataSave = getContext().getSharedPreferences("firstLog", MODE_PRIVATE);
                               SharedPreferences.Editor editor = dataSave.edit();
                               editor.putString("emergencyContact1", contact1_tx);
                               editor.putString("emergencyContact2", contact2_tx);
                               editor.putString("emergencyContact3", contact3_tx);
                               editor.apply();
                           }
                           catch (NullPointerException e){
                               e.printStackTrace();
                           }


                           setData();


                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });
               }
               else {

                   try {
                       SharedPreferences shared = getContext().getSharedPreferences("firstLog", MODE_PRIVATE);
                       contact1_tx = (shared.getString("emergencyContact1", ""));
                       contact2_tx = (shared.getString("emergencyContact2", ""));
                       contact3_tx = (shared.getString("emergencyContact3", ""));
                   }
                   catch (NullPointerException e){
                       e.printStackTrace();
                   }


                    setData();
               }
           }



            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void setData(){

        if (!contact1_tx.isEmpty()) {
            r1.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.Landing)
                    .duration(3000)
                    .repeat(0)
                    .playOn(r1);
            contact1_et.setText(contact1_tx);
            view.findViewById(R.id.emergency_text).setVisibility(View.GONE);
        }

        if (!contact2_tx.isEmpty()) {
            r2.setVisibility(View.VISIBLE);
            r1.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.Landing)
                    .duration(3000)
                    .repeat(0)
                    .playOn(r2);
            contact2_et.setText(contact2_tx);
            view.findViewById(R.id.emergency_text).setVisibility(View.GONE);
        }

        if (!contact3_tx.isEmpty()) {
            r3.setVisibility(View.VISIBLE);
            r1.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.Landing)
                    .duration(3000)
                    .repeat(0)
                    .playOn(r3);
            contact3_et.setText(contact3_tx);
            view.findViewById(R.id.emergency_text).setVisibility(View.GONE);
        }

        if (contact1_tx.isEmpty() && contact2_tx.isEmpty() && contact3_tx.isEmpty()) {
            view.findViewById(R.id.emergency_text).setVisibility(View.VISIBLE);
            r1.setVisibility(View.GONE);
            r2.setVisibility(View.GONE);
            r3.setVisibility(View.GONE);
        }


        dialog.dismiss();


    }

}
