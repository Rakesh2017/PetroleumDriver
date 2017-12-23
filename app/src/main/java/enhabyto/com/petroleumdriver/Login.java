package enhabyto.com.petroleumdriver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tapadoo.alerter.Alerter;

import dmax.dialog.SpotsDialog;
import mehdi.sakout.fancybuttons.FancyButton;

public class Login extends AppCompatActivity {

    private EditText contact_et, password_et;
    private String password_tx, contact_tx, match_password_tx, match_contact_tx;
    private FancyButton login_btn;
    private AlertDialog dialog;
    DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference().child("checkNetwork").child("isConnected");
    String connected;
    private DatabaseReference d_root = FirebaseDatabase.getInstance().getReference();
    DatabaseReference d_driverCredentials;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);






        contact_et = findViewById(R.id.login_contactEditText);
        password_et = findViewById(R.id.login_passwordEditText);
        login_btn = findViewById(R.id.login_loginButton);
        dialog = new SpotsDialog(Login.this, R.style.logging);


//         Login button
           login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                contact_tx = contact_et.getText().toString().trim();
                password_tx = password_et.getText().toString().trim();

                if (!isNetworkAvailable()){
                    Alerter.create(Login.this)
                            .setTitle("No Internet Connection!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.blackFifty)
                            .setIcon(R.drawable.no_internet)
                            .show();
                    dialog.dismiss();
                    return;
                }



                if (contact_tx.length() < 10){
                    Alerter.create(Login.this)
                            .setTitle("Invalid Mobile Number!")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.blackFifty)
                            .setIcon(R.drawable.error)
                            .show();
                    dialog.dismiss();
                    return;
                }

                if (password_tx.length()<5){
                    Alerter.create(Login.this)
                            .setTitle("Length of Password should greater than 5")
                            .setContentGravity(1)
                            .setBackgroundColorRes(R.color.blackFifty)
                            .setIcon(R.drawable.error)
                            .show();
                    dialog.dismiss();
                    return;
                }

                connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        connected = dataSnapshot.getValue(String.class);

                        if (!TextUtils.equals(connected, "connected")){
                            Alerter.create(Login.this)
                                    .setTitle("Unable to Connect to Server!")
                                    .setContentGravity(1)
                                    .setBackgroundColorRes(R.color.black)
                                    .setIcon(R.drawable.no_internet)
                                    .show();
                            dialog.dismiss();
                            return;
                        }

                        d_driverCredentials = d_root.child("driver_credentials").child(contact_tx);
                        d_driverCredentials.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                match_contact_tx = dataSnapshot.child("identity").getValue(String.class);
                                match_password_tx = dataSnapshot.child("password").getValue(String.class);

                                if (!TextUtils.equals(match_contact_tx, contact_tx)){
                                    Alerter.create(Login.this)
                                            .setTitle("Account with "+contact_tx+" does not exist!")
                                            .setContentGravity(1)
                                            .setBackgroundColorRes(R.color.black)
                                            .setIcon(R.drawable.error)
                                            .show();
                                    dialog.dismiss();
                                    return;
                                }

                                if (TextUtils.equals(match_contact_tx, contact_tx) && TextUtils.equals(match_password_tx, password_tx)){

                                    SharedPreferences dataSave = getSharedPreferences("firstLog", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = dataSave.edit();
                                    editor.putString("LaunchApplication", "DashBoard");
                                    editor.putString("contact_uid", match_contact_tx);
                                    editor.apply();
                                    password_et.setText("");
                                    Intent intent = new Intent(Login.this, DashBoard.class);
                                    startActivity(intent);
                                    dialog.dismiss();
                                }

                                else {
                                    Alerter.create(Login.this)
                                            .setTitle("Invalid Login Credentials!")
                                            .setContentGravity(1)
                                            .setBackgroundColorRes(R.color.black)
                                            .setIcon(R.drawable.error)
                                            .show();
                                    dialog.dismiss();

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

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onStart(){
        super.onStart();


        SharedPreferences dataSave = getSharedPreferences("firstLog", 0);

        try {
            if(dataSave.getString("LaunchApplication","keys").equals("DashBoard")){
                Intent intent = new Intent(Login.this, DashBoard.class);
                startActivity(intent);
            }

        }
        catch (NullPointerException e){
            e.printStackTrace();
        }

    }


    //end
}
