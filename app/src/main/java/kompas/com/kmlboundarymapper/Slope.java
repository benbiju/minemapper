package kompas.com.kmlboundarymapper;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Slope extends AppCompatActivity implements SensorEventListener {


    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    // define the display assembly compass picture
    private ImageView image;
    private ImageView baseline;
    private float currentPitch = 0; // Initial pitch angle
    Sensor accelerometerSensor;
    Sensor magnetometerSensor;
    float[] accelerometerValues = new float[3];
    float[] magnetometerValues = new float[3];
    float[] rotationMatrix = new float[9];
    float[] orientationValues = new float[3];
    float filteredValue;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    TextView tvHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slope);
        filteredValue = 0.0f;
        image = (ImageView) findViewById(R.id.movingline);
        baseline = findViewById(R.id.baseline);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Initialize the SensorManager

        // Get the gyroscope sensor
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //

        // TextView that will tell the user what degree is he heading


        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            finish();
            Toast.makeText(getApplicationContext(), "This phone does not support gyro", Toast.LENGTH_SHORT).show();
        }


        Bundle bundle = getIntent().getExtras();

//Extract the data
        String latlong = bundle.getString("latlong");
        TextView textView = findViewById(R.id.latlong);
        textView.setText(latlong);
        String accuracy = bundle.getString("accuracy");
        TextView textView1 = findViewById(R.id.accuracyCompass);
        textView1.setText(accuracy);

        ImageView imageView = findViewById(R.id.baseline);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        params.width = width;
        params.gravity = 1;
        params.height = (int) ((int) height * 0.4);
// existing height is ok as is, no need to edit it
        imageView.setLayoutParams(params);

        ImageView imageView1 = findViewById(R.id.movingline);
        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) imageView1.getLayoutParams();

        params1.width = width;
        params1.gravity = 1;
        params1.height = (int) ((int) height * 0.4);
// existing height is ok as is, no need to edit it
        imageView1.setLayoutParams(params1);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the gyroscope sensor listener
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor == accelerometerSensor) {
                accelerometerValues = event.values.clone();
            } else if (event.sensor == magnetometerSensor) {
                magnetometerValues = event.values.clone();
            }

            // Calculate rotation matrix
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magnetometerValues);

            // Calculate orientation in degrees
            SensorManager.getOrientation(rotationMatrix, orientationValues);

            // Convert radians to degrees
            float azimuthDegrees = (float) Math.toDegrees(orientationValues[0]);
            float pitchDegrees = (float) Math.toDegrees(orientationValues[1]);
            float rollDegrees = (float) Math.toDegrees(orientationValues[2]);
            filteredValue = 0.03f * pitchDegrees + (1 - 0.03f) * filteredValue;
            tvHeading = (TextView) findViewById(R.id.pitch);
            //      tvHeading.setText(event.values[0]+" "+event.values[1]+" "+event.values[2]);
            tvHeading.setText("Slope: " + -((int)filteredValue)+"°" );
            image.setRotation((int)(filteredValue));


            // Now you have azimuthDegrees, pitchDegrees, and rollDegrees
            // Use these values as needed in your application
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not needed for this example
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the gyroscope sensor listener to save power
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
