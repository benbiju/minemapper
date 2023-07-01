package kompas.com.kmlboundarymapper;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Compass extends Activity implements SensorEventListener {

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    TextView tvHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);

        //
        image = (ImageView) findViewById(R.id.compass);

        // TextView that will tell the user what degree is he heading


        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            finish();
            Toast.makeText(getApplicationContext(), "This phone does not support compass", Toast.LENGTH_SHORT).show();
        }


        final Button goBack = (Button) findViewById(R.id.goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
                finish();
            }
        });
        Bundle bundle = getIntent().getExtras();

//Extract the data
        String latlong = bundle.getString("latlong");
        TextView textView = findViewById(R.id.latlong);
        textView.setText(latlong);
        String accuracy = bundle.getString("accuracy");
        TextView textView1 = findViewById(R.id.accuracyCompass);
        textView1.setText(accuracy);

        ImageView imageView = findViewById(R.id.compass);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        params.width = width;
        params.gravity = 1;
        params.height = (int) ((int) height * 0.4);
// existing height is ok as is, no need to edit it
        imageView.setLayoutParams(params);

        ImageView imageView1 = findViewById(R.id.outside_imageview);
        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) imageView1.getLayoutParams();
        params1.width = 100;
        params1.gravity = 1;

        params1.height = 100;
        imageView1.setLayoutParams(params1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        int degree1 = (int) degree;
        tvHeading = (TextView) findViewById(R.id.degree);
        tvHeading.setText("Bearing: " + Integer.toString(degree1) + "°");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}
