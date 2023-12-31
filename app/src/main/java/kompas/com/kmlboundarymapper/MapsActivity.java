package kompas.com.kmlboundarymapper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener//,SurfaceHolder.Callback
         {
    private String getrec;
    private String getacc;
    private Uri globalCurrFileUri;
    private String globalLatitude;
    private String globalLongitude;
    private String compass;
    private String accuracy;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 3 * 1000;
    private long FASTEST_INTERVAL = 1000;
    private SensorManager mSensorManager;
    private Marker marker;
    AlertDialog.Builder builder;
    private Double markerLatitude;
    private Double markerLongitude;
    private String mine;
    Dialog dialog;
    String distanceDescription;
    String pillarsInfo;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_REQUEST_CODE_VIDEO = 2;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private ImageView imageView;
    private String currentPhotoPath;
    private String mineName;
    private static final int DATE_TIME_TEXT_SIZE = 20;
    private static final int COORDINATES_TEXT_SIZE = 18;
    private static final int TEXT_MARGIN = 10;
    private static final int TEXT_COLOR = Color.WHITE;
    private static final int BACKGROUND_COLOR = Color.BLACK;
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private SurfaceView cameraPreview;
    private android.hardware.Camera camera;
    private MediaRecorder mediaRecorder;
    private File videoFile;
    private boolean isRecording = false;


    //private LocationManager locationManager;

    View mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        System.out.println("granted");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        builder = new AlertDialog.Builder(this);
        setContentView(R.layout.activity_maps);
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        }, 0);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        System.out.println("granted2");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        int i = 1;
        while (i == 1) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                System.out.println("granted");
                break;
            }

        }

//        camera = Camera.open();
//        mediaRecorder = new MediaRecorder();
//        camera.setDisplayOrientation(90);
//        cameraPreview = findViewById(R.id.cameraPreview);
//        cameraPreview.getHolder().addCallback(this);
        mapFragment.getMapAsync(this);

//        final ImageButton imageButton = (ImageButton) findViewById(R.id.you);
//        imageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (myLocation != null) {
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
//                }
//            }
//        });
        ImageButton captureButton = findViewById(R.id.capture_button);
       // imageView = findViewById(R.id.image_view);
       // imageView = findViewById(R.id.image_view);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {

//                    if (v.getId() == R.id.capture_button) {
//                        if (!isRecording) {
//                            // Start recording
//                            startRecording();
//                            isRecording = true;
//                      //      recordButton.setImageResource(R.drawable.ic_stop); // Change button icon to stop
//                        } else {
//                            // Stop recording
//                            stopRecording();
//                            isRecording = false;
//                         //   recordButton.setImageResource(R.drawable.ic_record); // Change button icon to record
//                        }
//                    }
//                    startRecording();
                    capturePhoto();
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
            }
        });

        Button openWebsiteButton = findViewById(R.id.help_external);
        openWebsiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Define the URL you want to open
                String websiteUrl = "https://www.minemapper.in/2023/09/mine-mapper-30.html"; // Replace with your desired URL

                // Create an Intent to open the web browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));

                // Start the web browser activity
                startActivity(intent);
            }
        });

      //  locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/vnd.google-earth.kml+xml");
                String[] mimeType = {"application/vnd.google-earth.kmz", "application/vnd.google-earth.kml+xml"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "DEMO"), 1001);
            }
        });

        final Button sharemap = (Button) findViewById(R.id.share_map);
        sharemap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/vnd.google-earth.kml+xml");
                String[] mimeType = {"application/vnd.google-earth.kmz", "application/vnd.google-earth.kml+xml"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "DEMO"), 1991);
            }
        });
        sharemap.setTransformationMethod(null);

        final ImageButton shareButton = (ImageButton) findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(globalCurrFileUri==null || globalCurrFileUri.toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No KML files loaded", Toast.LENGTH_SHORT).show();
                    return;
                }
                shareKMLSingleFile(globalCurrFileUri);
            }
        });
        button.setTransformationMethod(null);

        final ImageButton help =  findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
               builder.setMessage(R.string.dialog_message).setCancelable(false)
                       .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {

                           }
                       });
               AlertDialog alert = builder.create();
               alert.setTitle("About and Disclaimer");
               alert.show();
            }
        });

        TextView whatsapp =(TextView)findViewById(R.id.helptext);
        whatsapp.setClickable(true);
        whatsapp.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='https://chat.whatsapp.com/GeZwAhn2Hbs4WRu7q3ahqW'>Join Mine Mapper WhatsApp Group </a>";
        whatsapp.setText(Html.fromHtml(text));
        whatsapp.setTextColor(Color.WHITE);



        final Button addKml = (Button) findViewById(R.id.addKML);
        addKml.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                //startActivity(new Intent(MapsActivity.this,Compass.class));
                Intent i;
                i = new Intent(MapsActivity.this, Mode.class);
                startActivity(i);
            }
        });

        final ImageButton ib3 = (ImageButton) findViewById(R.id.compass);
        final ImageButton ib4 = (ImageButton) findViewById(R.id.findMarker);
        final ImageButton ib5 = (ImageButton) findViewById(R.id.distanceDialog);
        final ImageButton ib6 = (ImageButton) findViewById(R.id.capture_button);
        final ImageButton ib7 = findViewById(R.id.share_button);
        final ImageButton ib8 = findViewById(R.id.help);
        final ImageButton ib9 = findViewById(R.id.gyro);

        RelativeLayout.LayoutParams params99 = (RelativeLayout.LayoutParams) ib9.getLayoutParams();

        // Set the height of this ImageButton
        params99.height = 120;

        // Set the width of that ImageButton
        params99.width = 120;

        // Apply the updated layout parameters to last ImageButton
        ib9.setLayoutParams(params99);

        // Set the ImageButton image scale type for fourth ImageButton
        ib9.setScaleType(ImageView.ScaleType.FIT_XY);
        // Get the last ImageButton's layout parameters
        RelativeLayout.LayoutParams params77 = (RelativeLayout.LayoutParams) ib7.getLayoutParams();

        // Set the height of this ImageButton
        params77.height = 120;

        // Set the width of that ImageButton
        params77.width = 120;

        // Apply the updated layout parameters to last ImageButton
        ib7.setLayoutParams(params77);

        // Set the ImageButton image scale type for fourth ImageButton
        ib7.setScaleType(ImageView.ScaleType.FIT_XY);

        // Get the last ImageButton's layout parameters
        RelativeLayout.LayoutParams params78 = (RelativeLayout.LayoutParams) ib8.getLayoutParams();

        // Set the height of this ImageButton
        params78.height = 120;

        // Set the width of that ImageButton
        params78.width = 120;

        // Apply the updated layout parameters to last ImageButton
        ib8.setLayoutParams(params78);

        // Set the ImageButton image scale type for fourth ImageButton
        ib8.setScaleType(ImageView.ScaleType.FIT_XY);

        // Get the last ImageButton's layout parameters
        RelativeLayout.LayoutParams params20 = (RelativeLayout.LayoutParams) ib6.getLayoutParams();

        // Set the height of this ImageButton
        params20.height = 120;

        // Set the width of that ImageButton
        params20.width = 120;

        // Apply the updated layout parameters to last ImageButton
        ib6.setLayoutParams(params20);

        // Set the ImageButton image scale type for fourth ImageButton
        ib6.setScaleType(ImageView.ScaleType.FIT_XY);


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ib4.getLayoutParams();

        // Set the height of this ImageButton
        params.height = 120;

        // Set the width of that ImageButton
        params.width = 120;

        // Apply the updated layout parameters to last ImageButton
        ib4.setLayoutParams(params);

        // Set the ImageButton image scale type for fourth ImageButton
        ib4.setScaleType(ImageView.ScaleType.FIT_XY);

        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) ib3.getLayoutParams();

        // Set the height of this ImageButton
        params1.height = 120;

        // Set the width of that ImageButton
        params1.width = 120;


        // Apply the updated layout parameters to last ImageButton
        ib3.setLayoutParams(params1);

        // Set the ImageButton image scale type for fourth ImageButton
        ib3.setScaleType(ImageView.ScaleType.FIT_XY);
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) ib5.getLayoutParams();

        // Set the height of this ImageButton
        params2.height = 120;

        // Set the width of that ImageButton
        params2.width = 120;

        // Apply the updated layout parameters to last ImageButton
        ib5.setLayoutParams(params2);

        // Set the ImageButton image scale type for fourth ImageButton
        ib5.setScaleType(ImageView.ScaleType.FIT_XY);
        final ImageButton findMarker = (ImageButton) findViewById(R.id.findMarker);
        findMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
                if(markerLatitude != null) {

                    TextView mineOwner = findViewById(R.id.file);
                    if(mine != null && !mine.isEmpty()) {
                        mineOwner.setText("Mine Owner: "+mine);
                    }
                    LatLng latLngmarker = new LatLng(markerLatitude,markerLongitude);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngmarker,18));
                }

            }

        });
        final ImageButton compass = (ImageButton) findViewById(R.id.compass);
        compass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                //startActivity(new Intent(MapsActivity.this,Compass.class));
                Intent i;
                i = new Intent(MapsActivity.this, Compass.class);
                Bundle bundle = new Bundle();
                bundle.putString("latlong", getrec);
                bundle.putString("accuracy",getacc);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        final ImageButton gyro = (ImageButton) findViewById(R.id.gyro);
        gyro.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                //startActivity(new Intent(MapsActivity.this,Compass.class));
                Intent i;
                i = new Intent(MapsActivity.this, Slope.class);
                Bundle bundle = new Bundle();
                bundle.putString("latlong", getrec);
                bundle.putString("accuracy",getacc);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.distancedialogbox);
        final ImageButton distancesButton = (ImageButton) findViewById(R.id.distanceDialog);
        distancesButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if(distanceDescription!=null && distanceDescription.length()>0){
                    TextView distancesTextView = dialog.findViewById(R.id.distanceTextView);
                    distancesTextView.setMovementMethod(new ScrollingMovementMethod());
                    distancesTextView.setText(distanceDescription+"\n"+pillarsInfo);
                    distancesTextView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            ClipboardManager cm = (ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            cm.setText(distancesTextView.getText());
                            Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });
                    TextView measurementTitle = dialog.findViewById(R.id.measurementTitle);
                    measurementTitle.setText("Map Details");
                    dialog.show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"No distance data",Toast.LENGTH_SHORT).show();
                }

            }
        });


       // help.setTransformationMethod(null);


        final LocationManager gpsManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if(!gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){//(internetManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED && internetManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED)) {
                TextView gps = (TextView) findViewById(R.id.accuracy);
                    gps.setText("Location: OFF");
        }
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
        compass = "Bearing: "+degree1+"°";
        TextView textView2 = (TextView) findViewById(R.id.longitude);
        if(globalLatitude==null) {
            textView2.setText("Loading...");
        }
        else {
            textView2.setText(globalLatitude+"   "+globalLongitude+"   "+compass);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @SuppressLint({"ResourceType", "NewApi"})
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1991) {
            Uri currFileURI = data.getData();
                if (data.getClipData() != null) {
                    // Check if the data contains multiple URIs
                            int itemCount = data.getClipData().getItemCount();
                            ArrayList<Uri> fileUris = new ArrayList<>();

                            for (int i = 0; i < itemCount; i++) {
                                Uri uri = data.getClipData().getItemAt(i).getUri();
                                fileUris.add(uri);
                            }

                            // Now you have an ArrayList<Uri> (fileUris) containing multiple file URIs
                            // You can further process these URIs as needed
                            shareKMLFile(fileUris); // Example: Share the selected KML files

            }
                else if(data.getData() != null){
                    shareKMLSingleFile(data.getData());
                }
            //  shareKMLFile(currFileURI);
        }
        if (requestCode == 1001) {
            KmlLayer kmlLayer = null;
            try {
                Uri currFileURI = data.getData();
                globalCurrFileUri = data.getData();
                String path = getFileName(currFileURI);
                TextView textView = (TextView) findViewById(R.id.file);
                String fileNameWithoutExtension = path != null ? path.replaceFirst("[.][^.]+$", "") : null;
                textView.setText("Map of " + fileNameWithoutExtension);
                //shareKMLFile(currFileURI);

                mineName = path.replaceAll("\\.kml", "");
                mineName = path.replaceAll("\\.kmz", "");
                System.out.println(path);
                kmlLayer = null;
                InputStream inputStream;
                try {
                    if (path.substring(path.length() - 1).equals("z")) {
                        kmlLayer = createLayerFromKmz(currFileURI);
                    } else {
                        inputStream = getContentResolver().openInputStream(currFileURI);
                        kmlLayer = new KmlLayer(mMap, inputStream, getApplicationContext());
                    }

                    KmlContainer container = kmlLayer.getContainers().iterator().next();
                    Iterator iterator = container.getContainers().iterator();
                    KmlContainer container1 = null;
                    while (iterator.hasNext()) {
                        container1 = (KmlContainer) iterator.next();

                    }

                    Iterator iterator1 = container1.getPlacemarks().iterator();
                    LatLng latLng = null;
                    KmlPlacemark placemark = null;
                    TextView nameofFile = findViewById(R.id.file);

                    String file1 = container1.getProperty("name");
                    List<String> splittedFile1name = null;
                    if(file1 != null) {
                        splittedFile1name = Arrays.asList(file1.split("-"));
                        String requiredFile = splittedFile1name.get(1);
                        nameofFile.setText("Mine owner: "+requiredFile);
                        mine = requiredFile;
                    }
                    while (iterator1.hasNext()) {
                        placemark = (KmlPlacemark) iterator1.next();
                        if(placemark.getProperty("description") != null) {
                            //TextView textview1 = findViewById(R.id.area);
                       //     textview1.setText("");
                            List<String> findWhichKML = Arrays.asList(placemark.getProperty("description").split("#"));
                            if(findWhichKML.size()==1) {
                                String area = placemark.getProperty("description");
                                List<String> splittedFileName = Arrays.asList(area.split("/"));
                                if(splittedFileName != null && splittedFileName.size() > 1) {
                                    String requiredText = splittedFileName.get(1);
                                    if(splittedFile1name != null && splittedFile1name.size() >1) {
                                 //       textview1.setText(splittedFile1name.get(0)+"  Area:"+requiredText);
                                    }
                                }
                               // distanceDescription = null;
                              //  pillarsInfo = null;
                            }
                            else{
                                distanceDescription = findWhichKML.get(1);
                                pillarsInfo = findWhichKML.get(3);
                                String area = findWhichKML.get(2);
                           //     textview1.setText(area);
                            }

                        }

                        if (placemark.getGeometry().getGeometryType().equals("Point")) {
                            LatLng kmlPoint = (LatLng) placemark.getGeometry().getGeometryObject();
                            mMap.addMarker(new MarkerOptions().position(new LatLng(kmlPoint.latitude, kmlPoint.longitude)).title(placemark.getProperty("name") + ": " + convertLatitud(kmlPoint.latitude) + " " + convertLongitude(kmlPoint.longitude) + " ").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(kmlPoint));
                                markerLatitude=kmlPoint.latitude;
                                markerLongitude=kmlPoint.longitude;


                        }


                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                try {
                    e.printStackTrace();
                    kmlLayer.addLayerToMap();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (XmlPullParserException e1) {
                    e1.printStackTrace();
                }
            }
            try {
                kmlLayer.addLayerToMap();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Load the captured image from file
            File imageFile = new File(currentPhotoPath);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),bmOptions);
            // Add date and time to the image
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm a", Locale.getDefault());
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateTime = dateFormat.format(new Date());
            String date = dateFormat2.format(new Date());
            // bitmap = addTextToBitmap(bitmap, dateTime);

            // Add location coordinates to the imteage

            String latlong = getrec;

            String modlatlong = "Coordinates: "+latlong;
            
            // Save the modified image
            // FileOutputStream out = new FileOutputStream(imageFile);
            //  bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            //    out.flush();
            //  out.close();
            Toast.makeText(this, "Image saved with date, time, and coordinates in Gallery", Toast.LENGTH_SHORT).show();
            Bitmap editedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(editedBitmap);

            int bitmapWidth = editedBitmap.getWidth();
            int bitmapHeight = editedBitmap.getHeight();

            float minemapper = bitmapWidth * 0.035f;
            float dateTimeTextSize = bitmapWidth * 0.037f; // Adjust the multiplier as needed
            float coordinatesTextSize = bitmapWidth * 0.037f; // Adjust the multiplier as needed

            float dateTimeX = bitmapWidth * 0.02f;
            float dateTimeY;
            if (bitmapWidth > bitmapHeight) {
                 dateTimeY = bitmapHeight * 0.87f;

            } else {
                 dateTimeY = bitmapHeight * 0.92f;

            }

            float coordinatesX = bitmapWidth * 0.02f;
            float coordinatesY = dateTimeY + dateTimeTextSize + TEXT_MARGIN;

            float minemapperX = bitmapWidth * 0.02f;
            float minemapperY = coordinatesY + coordinatesTextSize + TEXT_MARGIN;
           // float minemapperY = coordinatesY + coordinatesTextSize + TEXT_MARGIN + minemapper + TEXT_MARGIN;

            Paint backgroundPaint = new Paint();
            backgroundPaint.setColor(Color.BLACK);
            backgroundPaint.setAlpha(200);

            float backgroundRectHeight = dateTimeTextSize + coordinatesTextSize + minemapper + TEXT_MARGIN * 3; // Adjust the margin as needed
            canvas.drawRect(0, dateTimeY - dateTimeTextSize - TEXT_MARGIN * 2, bitmapWidth, dateTimeY + backgroundRectHeight, backgroundPaint);


            Paint dateTimePaint = new Paint();
            dateTimePaint.setColor(TEXT_COLOR);
            dateTimePaint.setTextSize(dateTimeTextSize);
            dateTimePaint.setTypeface(Typeface.DEFAULT_BOLD);

            Paint coordinatesPaint = new Paint();
            coordinatesPaint.setColor(TEXT_COLOR);
            coordinatesPaint.setTextSize(coordinatesTextSize);
            coordinatesPaint.setTypeface(Typeface.DEFAULT_BOLD);


            Paint minemapperPaint = new Paint();
            minemapperPaint.setColor(Color.RED);
            minemapperPaint.setTextSize(minemapper);


            // Draw the coordinates text on the bitmap
            canvas.drawText(modlatlong, coordinatesX, coordinatesY, coordinatesPaint);
            canvas.drawText("Date: " +dateTime +"  "+compass, dateTimeX, dateTimeY, dateTimePaint);
            String mine;
            if(mineName == null) {
                mine = "";
            }
            else {
                String fileNameWithoutExtension = mineName != null ? mineName.replaceFirst("[.][^.]+$", "") : null;
                mine =fileNameWithoutExtension+" ";
            }
            canvas.drawText(mine + "© Mine Mapper 3.0",minemapperX,minemapperY,minemapperPaint);

//            try {
//                File outputFile = createImageFile();
//                FileOutputStream fos = new FileOutputStream(outputFile);
//                editedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                fos.flush();
//                fos.close();
//
//                // Save the edited image to the gallery
//                ContentResolver contentResolver = this.getContentResolver();
//                ContentValues contentValues = new ContentValues();
//                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "Edited Image");
//                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//                contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
//                contentValues.put(MediaStore.Images.Media.DATA, outputFile.getAbsolutePath());
//                imageFile.delete();
//
//                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                Uri contentUri = Uri.fromFile(outputFile);
//                mediaScanIntent.setData(contentUri);
//                sendBroadcast(mediaScanIntent);
//                MediaScannerConnection.scanFile(this, new String[]{outputFile.getAbsolutePath()}, null,
//                        new MediaScannerConnection.OnScanCompletedListener() {
//                            @Override
//                            public void onScanCompleted(String path, Uri uri) {
//                                // Display a toast message indicating the photo is saved
//                                Toast.makeText(getApplicationContext(), "Photo saved to gallery", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "JPEG_" + timeStamp + "_"+".jpg";
            String galleryPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/";

            File directory = new File(galleryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);
            OutputStream outputStream;

            try {
                outputStream = new FileOutputStream(file);
                editedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                // Insert the image file into the media store
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception accordingly
            }


            // Add the image to the gallery


            // Refresh the gallery to display the new image


            // Display the modified image
          //  imageView.setImageBitmap(bitmap);
        }
        if(requestCode == CAMERA_REQUEST_CODE_VIDEO && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
           // String path = Utils.getRealPathFromURI(videoUri, this);
        }
    }


    private KmlLayer createLayerFromKmz(Uri kmzFileName) {
        KmlLayer kmlLayer = null;
        InputStream inputStream;
        ZipInputStream zipInputStream;
        try {
            inputStream = getContentResolver().openInputStream(kmzFileName);
            zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    String fileName = zipEntry.getName();
                    if (fileName.endsWith(".kml")) {
                        kmlLayer = new KmlLayer(mMap, zipInputStream, getApplicationContext());
                    }
                }
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return kmlLayer;
    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        int height = 75;
        int width = 75;
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.you1);
//        Bitmap b = bitmapDrawable.getBitmap();
//        Bitmap bitmap = Bitmap.createScaledBitmap(b, width, height, false);
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(4);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixelsTop = (int) (180*scale + 0.5f);
        int dpAsPixelsBottom = (int) (20*scale + 0.5f);
        mMap.setPadding(0,dpAsPixelsTop,0,dpAsPixelsBottom);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(19));




//        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
//        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
//// position on right bottom
//        //rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//       // rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//
//        rlp.setMargins(0, h/2, 300, 0);
//

//        LatLng myLocation = new LatLng(9.42, 76.70);
//        marker = mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location").icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());

                    }
                },
                Looper.myLooper());


    }
    int count =0;
    LatLng myLocation;
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onLocationChanged(Location location) {
        final LocationManager gpsManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("ServiceCast")
        final ConnectivityManager internetManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        myLocation = new LatLng(location.getLatitude(),location.getLongitude());
        TextView textView = (TextView) findViewById(R.id.latitude);
        textView.setText("               My Location               ");
        TextView textView2 = (TextView) findViewById(R.id.longitude);
        globalLatitude=convertLatitud(location.getLatitude());
        globalLongitude=convertLongitude(location.getLongitude());

        textView2.setText(convertLatitud(location.getLatitude())+"   "+convertLongitude(location.getLongitude()));
        TextView textView3 = (TextView) findViewById(R.id.accuracy);
        DecimalFormat df = new DecimalFormat("#.#");
        accuracy = df.format(location.getAccuracy());
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
        String strDate= formatter.format(date);

        if(!gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            textView3.setText("GPS: OFF |"+" Accuracy: "+ String.valueOf((accuracy)+" m |")+" Date: "+strDate);
            getacc ="GPS: OFF | "+"Accuracy: "+ String.valueOf((accuracy)+" m |")+" Date: "+strDate;
        }
       else {
            textView3.setText("GPS: ON |"+" Accuracy: "+ String.valueOf((accuracy)+" m |")+" Date: "+strDate);
            getacc ="GPS: ON | "+"Accuracy: "+ String.valueOf((accuracy)+" m |")+" Date: "+strDate;
        }


        getrec = convertLatitud(location.getLatitude())+"   "+convertLongitude(location.getLongitude());



        count++;
        if(count==1){
            mMap.animateCamera(CameraUpdateFactory.newLatLng(myLocation));
        }

    }

    public static String convertLatitud(double latitude) {
        String result;
        String direction = "N";
        if(latitude<0){
            direction ="S";
        }
        result = convert(latitude) + direction;
        return result;
    }
    public static String convertLongitude(double longitude) {
        String result;
        String direction = "E";
        if(longitude<0){
            direction ="W";
        }
        result = convert(longitude) + direction;
        return result;
    }


    private static String convert(double d) {
        d = Math.abs(d);
        Integer i = (int) d;
        String s = String.valueOf(i) + '°';
        d = d - i;
        d = d * 60;
        i = (int) d;
        s = s + String.valueOf(i) + '\'';
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.####");
//        df.setRoundingMode(RoundingMode.);
        d = d - i;
        d = d * 60;
        s = s + String.valueOf(df.format(d)) + '"';
        return s;


    }




@SuppressLint("MissingPermission")
private void capturePhoto() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this, "kompas.com.kmlboundarymapper.fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
//    @SuppressLint("MissingPermission")
//    private void capturePhoto() {
//        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takeVideoIntent, CAMERA_REQUEST_CODE_VIDEO);
//        }
//        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(this, "kompas.com.kmlboundarymapper.fileprovider", photoFile);
//                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takeVideoIntent, CAMERA_REQUEST_CODE_VIDEO);
//            }
//        }
//    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }


    private Bitmap addTextToBitmap(Bitmap bitmap, String text) {
        // Add text to bitmap (e.g., date, time, or coordinates)
        // Code to add text to the bitmap goes here

        return bitmap;
    }

    public void shareKMLFile(ArrayList<Uri> filePaths) {

        Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        // Set the type of data to be shared (KML in this case)
        sharingIntent.setType("application/vnd.google-earth.kml+xml");

        // Attach the KML file to the Intent using a URI
       // File kmlFile = new File(filePath);
//        Uri uri = FileProvider.getUriForFile(this, "kompas.com.kmlboundarymapper.fileprovider", kmlFile);
        sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filePaths);

        // Add any additional text or subject if needed
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "Check out these KML files!");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared KML Files");

        // Start the sharing activity
        this.startActivity(Intent.createChooser(sharingIntent, "Share KML files"));
    }

             public void shareKMLSingleFile(Uri filePath) {

                 {
                     // Create an Intent with ACTION_SEND
                     Intent sharingIntent = new Intent(Intent.ACTION_SEND);

                     // Set the type of data to be shared (KML in this case)
                     sharingIntent.setType("application/vnd.google-earth.kml+xml");

                     // Attach the KML file to the Intent using a URI
                     // File kmlFile = new File(filePath);
//        Uri uri = FileProvider.getUriForFile(this, "kompas.com.kmlboundarymapper.fileprovider", kmlFile);
                     sharingIntent.putExtra(Intent.EXTRA_STREAM, filePath);

                     // Add any additional text or subject if needed
                     sharingIntent.putExtra(Intent.EXTRA_TEXT, "Check out this KML file!");
                     sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared KML File");

                     // Start the sharing activity
                     this.startActivity(Intent.createChooser(sharingIntent, "Share KML file"));
                 }
             }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CAMERA_PERMISSION);
//        }
//    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        locationManager.removeUpdates((LocationListener) MapsActivity.this);
//    }

//    private Location getLastKnownLocation() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//            if (locationGPS != null)
//                return locationGPS;
//            else if (locationNetwork != null)
//                return locationNetwork;
//            else
//                return null;
//        }
//        return null;
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capturePhoto();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

//    @Override
//    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//        camera.unlock();
//        mediaRecorder.setCamera(camera);
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mediaRecorder.setOutputFile(videoFile);
//        }
//
//        try {
//            mediaRecorder.prepare();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
//        mediaRecorder.start();
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//        mediaRecorder.stop();
//        mediaRecorder.reset();
//        mediaRecorder.release();
//        MediaScannerConnection.scanFile(this, new String[]{}, null, null);
//
//        camera.lock();
//        camera.release();
//    }
//
//    private void startRecording() {
//        // Set the output file for the recorded video
//        try {
//            videoFile = createVideoFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
//
//        // Start recording
//        try {
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // Start drawing information on the overlay
//       // infoOverlay.startDrawing();
//    }
//
//    private void stopRecording() {
//        // Stop recording
//        mediaRecorder.stop();
//        mediaRecorder.reset();
//        mediaRecorder.release();
//        mediaRecorder = null;
//
//        // Stop drawing information on the overlay
//       // infoOverlay.stopDrawing();
//
//        // Add the recorded video to the gallery
//        MediaScannerConnection.scanFile(this, new String[]{videoFile.getAbsolutePath()}, null, null);
//    }
//
//    private File createVideoFile() throws IOException {
//        return createImageFile();
//       // String fileName = "Video_" + UUID.randomUUID().toString() + ".mp4";
////        File storageDir = getExternalFilesDir(null);
////        return new File(storageDir, fileName);
//    }
}
