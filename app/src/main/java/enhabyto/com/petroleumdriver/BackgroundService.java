package enhabyto.com.petroleumdriver;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by this on 14-Dec-17.
 */

public class BackgroundService extends Service {

    private boolean isRunning;
    private Context context;
    private Thread backgroundThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.context = this;
        this.isRunning = false;
        this.backgroundThread = new Thread(myTask);
    }

    private Runnable myTask = new Runnable() {
        public void run() {

            if(!isNetworkAvailable()){
                return;
            }


            DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {

                        SharedPreferences shared = getSharedPreferences("firstLog", MODE_PRIVATE);
                        String contactUID_tx;
                        DatabaseReference d_root = FirebaseDatabase.getInstance().getReference();
                        try {
                            contactUID_tx = (shared.getString("contact_uid", ""));
                        } catch (NullPointerException e) {
                            contactUID_tx = "";
                        }

                        final DatabaseReference d_notification = d_root.child("trip_schedules_driver").child(contactUID_tx);


                        d_notification.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String schedule_status = dataSnapshot.child("status").getValue(String.class);

                                if (TextUtils.equals(schedule_status, "waiting")) {
                                    String truck_number = dataSnapshot.child("truck_number").getValue(String.class);
                                    String start_date = dataSnapshot.child("expected_start_date").getValue(String.class);

                                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                                    Intent showIntent = new Intent(context, TripSchedules.class);
                                    PendingIntent pIntent = PendingIntent.getActivity(context
                                            , 0, showIntent, PendingIntent.FLAG_CANCEL_CURRENT);


                                    PugNotification.with(context)
                                            .load()
                                            .title("You Have a new Trip")
                                            .sound(soundUri)
                                            .smallIcon(R.drawable.ic_local_shipping_black)
                                            .autoCancel(true)
                                            .largeIcon(R.drawable.app_logo)
                                            .flags(Notification.FLAG_AUTO_CANCEL)
                                            .bigTextStyle("Truck Number " + truck_number
                                                    + " is allocated to you."
                                                    +"Trip is expected to be start on " +start_date)
                                            .click(pIntent)
                                            .simple()
                                            .build();




                                    assert v != null;
                                    v.vibrate(300);
                                }

                                //hello

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });



                    } //if connected ended
                }

                @Override
                public void onCancelled(DatabaseError error) {


                }
            });




            // Do something here
            stopSelf();
        }
    };

    @Override
    public void onDestroy() {
        this.isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) (getApplication()).getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}