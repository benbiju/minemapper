package kompas.com.kmlboundarymapper;


import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class Mode extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createkmloptions);
        final Spinner mode = (Spinner) findViewById(R.id.mode);
        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position== 1) {
                    Intent i;
                    i = new Intent(Mode.this, AddKML.class);
                    startActivity(i);
                    finish();
                }
                if(position == 2) {
                    Intent i;
                    i = new Intent(Mode.this, AddMapKML.class);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


                //startActivity(new Intent(MapsActivity.this,Compass.class));



    }
}