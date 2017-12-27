package enhabyto.com.petroleumdriver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import mehdi.sakout.fancybuttons.FancyButton;
import util.android.textviews.FontTextView;

public class SplashScreen extends AppCompatActivity {

    FancyButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences dataSave = getSharedPreferences("firstLog", 0);

        try {
            if(dataSave.getString("LaunchApplication","").equals("DashBoard")){
                Intent intent = new Intent(SplashScreen.this, DashBoard.class);
                startActivity(intent);
            }

        }
        catch (NullPointerException e){
            e.printStackTrace();
        }

        checkPermissions();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        button = findViewById(R.id.splash_submitButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });

        YoYo.with(Techniques.Landing)
                .duration(3000)
                .repeat(0)
                .playOn(findViewById(R.id.splash_logo));



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                button.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn)
                        .duration(3000)
                        .repeat(0)
                        .playOn(button);



            }
        },4000);

    }


    public void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(SplashScreen.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(SplashScreen.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(SplashScreen.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(SplashScreen.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SplashScreen.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
                    , android.Manifest.permission.CALL_PHONE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        else {
            Intent i = new Intent(getBaseContext(), Login.class);
            startActivity(i);

            SplashScreen.this.finish();
        }
    }

}


