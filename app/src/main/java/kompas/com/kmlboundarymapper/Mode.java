package kompas.com.kmlboundarymapper;


import android.app.Activity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Mode extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createkmloptions);


        ImageView imageView1 = findViewById(R.id.auto_saving);
        ImageView imageView2 = findViewById(R.id.manual_saving);
        ImageView imageView3 = findViewById(R.id.manual_saving_marker);
        ImageView imageView4 = findViewById(R.id.manual_saving_line);

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent i;
                    i = new Intent(Mode.this, AddKML.class);
                    startActivity(i);
                    finish();
            }
        });

        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent i;
                    i = new Intent(Mode.this, AddLineKML.class);
                    startActivity(i);
                    finish();
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent(Mode.this, AddMapKML.class);
                startActivity(i);
                finish();
            }
        });

        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent(Mode.this, AppMarkerKML.class);
                startActivity(i);
                finish();
            }
        });

    }

}
