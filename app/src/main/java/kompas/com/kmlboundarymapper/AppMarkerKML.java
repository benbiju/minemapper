package kompas.com.kmlboundarymapper;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static kompas.com.kmlboundarymapper.MapsActivity.convertLatitud;
import static kompas.com.kmlboundarymapper.MapsActivity.convertLongitude;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.text.DecimalFormat;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppMarkerKML extends FragmentActivity implements OnMapReadyCallback {
    private String getrec;
    private String getacc;
    private String globalLatitude;
    private String globalLongitude;
    private String accuracy;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 3 * 1000;
    private long FASTEST_INTERVAL = 1000;
    View mapView;
    AlertDialog.Builder builder;
    List<MapPoint> latLongList;
    List<Marker> markerList;
    String polygon;
    double latitude;
    double longitude;
    Dialog dialog;
    Dialog deletePillarDialog;
    Dialog editPillarDialog;
    Integer pillarCount;
    String currentAccuracy;
    String notesStringMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.markermapkmlform);
        pillarCount = 0;
        markerList = new ArrayList<>();
        latLongList = new ArrayList<>();
        polygon = "";
        ActivityCompat.requestPermissions(AppMarkerKML.this, new String[]{
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
        mapFragment.getMapAsync(this);
        builder = new AlertDialog.Builder(this);
        final Button addPoint = (Button) findViewById(R.id.addMarkerPoint);
        addPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pillarCount++;
                if (latitude == 0 || longitude == 0) {
                    return;
                }
                MapPoint latlong = new MapPoint(latitude, longitude, currentAccuracy);

                latlong.setName(String.valueOf(pillarCount));
                latLongList.add(latlong);
                // builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
                builder.setMessage(convertLatitud(latitude) + "," + convertLongitude(longitude)).setCancelable(false)
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("Marker " + latlong.name + " added");
                alert.show();
                TextView coordinates = (TextView) findViewById(R.id.coordinates);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    polygon = latLongList.stream().map(e -> e.toString()).reduce("", String::concat);
                }
               // polygon = polygon + "\n" + findDistancesBetweenPillars() + "\n" + calculatePolygonArea();
                coordinates.setText(polygon);
                coordinates.setMovementMethod(new ScrollingMovementMethod());
                LatLng kmlPoint = new LatLng(latitude, longitude);
                // Marker marker =mMap.addMarker(new MarkerOptions().position(kmlPoint).title("Pillar " + latLongList.size() + ": " + convertLatitud(latitude) + " " + convertLongitude(longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(kmlPoint));
                //test
                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
                Canvas canvas1 = new Canvas(bmp);

                Paint color = new Paint();
                color.setTextSize(50);
                color.setColor(Color.YELLOW);


                canvas1.drawText(latlong.name, 30, 40, color);

                Marker marker = mMap.addMarker(new MarkerOptions().position(kmlPoint).title("Pillar " + latLongList.size() + ": " + convertLatitud(latitude) + " " + convertLongitude(longitude) + " Acc: " + currentAccuracy+"m")
                        .icon(BitmapDescriptorFactory.fromBitmap(bmp)));
                markerList.add(marker);


            }
        });
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.createdialog);
        final Button createKML = (Button) findViewById(R.id.createMarkerKML);
        createKML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (latLongList == null || latLongList.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "No points added", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                   // toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
                    toast.show();
                    return;
                }
                dialog.show();
                Button createKMLbutton = (Button) dialog.findViewById(R.id.createKMLButton);

                createKMLbutton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View v) {
                        createKMLfile();
                    }
                });
            }
        });
        deletePillarDialog = new Dialog(this);
        deletePillarDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deletePillarDialog.setContentView(R.layout.deletemarkerdesc);
        final Button deletePoint = (Button) findViewById(R.id.deleteMarkerPoint);
        deletePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePillarDialog.show();
                Button deletePillarButton = (Button) deletePillarDialog.findViewById(R.id.deleteMarkerButton);
                deletePillarButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText pillarName = (EditText) deletePillarDialog.findViewById(R.id.deleteMarkerText);
                        MapPoint toBeDeletedPillar = null;
                        for (MapPoint mapPoint : latLongList) {
                            if (mapPoint.name.equalsIgnoreCase(pillarName.getText().toString())) {
                                toBeDeletedPillar = mapPoint;
                            }
                        }
                        if (toBeDeletedPillar == null) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Marker not found", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        if (toBeDeletedPillar != null) {
                            for (Marker marker : markerList) {
                                if (marker.getPosition().latitude == toBeDeletedPillar.lat && marker.getPosition().longitude == toBeDeletedPillar.lon) {
                                    marker.remove();

                                }
                            }
                        }
                        latLongList.remove(toBeDeletedPillar);

                        deletePillarDialog.dismiss();
                    }
                });
            }
        });
        editPillarDialog = new Dialog(this);
        editPillarDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editPillarDialog.setContentView(R.layout.addmarkerdesc);
        final Button editPoint = (Button) findViewById(R.id.addMarkerPoint);
        editPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPillarDialog.show();
                Button editPillarButton = (Button) editPillarDialog.findViewById(R.id.addMarkerButton);
                editPillarButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editPillarName = (EditText) editPillarDialog.findViewById(R.id.addMarkerText);
                        if(editPillarName.getText()==null || editPillarName.getText().equals("")) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Please add a name for the marker", Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
                        MapPoint marker = null;
                        marker = new MapPoint(latitude, longitude, accuracy);
                        marker.setName(editPillarName.getText().toString());
                        LatLng kmlPoint = new LatLng(latitude, longitude);
                        Toast toast = Toast.makeText(getApplicationContext(), "Marker added", Toast.LENGTH_SHORT);
                        toast.show();
                        editPillarDialog.dismiss();
                        Marker markeronmap = mMap.addMarker(new MarkerOptions().position(kmlPoint).title(editPillarName.getText() + ": " + convertLatitud(latitude) + " " + convertLongitude(longitude))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        markerList.add(markeronmap);
                        latLongList.add(marker);
                    }
                });

            }
        });
        Dialog notesDialog = new Dialog(this);
        notesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        notesDialog.setContentView(R.layout.notesdialog);
        final Button notes = (Button) findViewById(R.id.markerComments);
        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesDialog.show();
                Button notesSubmit = (Button) notesDialog.findViewById(R.id.notesSubmit);
                notesSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText notesSection = (EditText) notesDialog.findViewById(R.id.commentsTextArea);
                        notesStringMarker=notesSection.getText().toString();
                        notesDialog.dismiss();
                    }
                });
            }
        });
    }







    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createKMLfile() {
        KML kml = new KML();
        Integer count =1;
        List<MapPoint> mapPoints = new ArrayList<>();
        StringBuilder pillarsInfo = new StringBuilder();
        EditText fileName = (EditText) dialog.findViewById(R.id.filename);
        if(fileName.getText().toString().isEmpty()) {
            return;
        }
        if(latLongList.size()>0){
            mapPoints.add(latLongList.get(0));
        }
        //pillarsInfo.append(calculatePolygonArea()+"\n\n");
        if(notesStringMarker!=null&&!notesStringMarker.isEmpty()){
            pillarsInfo.append("Field Notes\n"+notesStringMarker+"\n\n");
        }
        pillarsInfo.append("Map name: "+fileName.getText().toString()+" | created on: "+ LocalDate.now()+ " "+LocalDateTime.now().getHour()+":"+LocalDateTime.now().getMinute()+"\n\n");
        pillarsInfo.append("Mine Mapper | © Biju Sebastian 2023\n\n");

        for(MapPoint latlong: latLongList) {
            MapPoint mapPoint = new MapPoint(latlong.lat,latlong.lon,latlong.accuracy);
            mapPoint.setName(latlong.name);
            kml.addMarker(mapPoint,pillarsInfo.toString());
            pillarsInfo.append(latlong.name+":\nLat: "+convertLatitud(latlong.lat)+"\nLon: "+convertLongitude(latlong.lon)+"\nAcc: "+latlong.accuracy+"m\n\n");
            mapPoints.add(mapPoint);
            count++;
        }

       // kml.addMarkers();
       // kml.addPath(mapPoints,"Path",findDistancesBetweenPillars(),calculatePolygonArea(),pillarsInfo.toString());
        String rootPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/Download/MineMapper/";
        File root = new File(rootPath);
        if (!root.exists()) {
            root.mkdirs();
        }

        File f = new File(rootPath + fileName.getText().toString()+".kml");
        if(f.exists()) {
            builder.setMessage("KML file already exists in location. Please try a different file name").setCancelable(false)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Dialog newDialog = new Dialog(AppMarkerKML.this);
                            newDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            newDialog.setContentView(R.layout.createdialog);
                            final Button createKML = (Button) findViewById(R.id.createMarkerKML);
                            createKML.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(latLongList == null || latLongList.isEmpty()) {
                                        Toast toast = Toast.makeText(getApplicationContext(),"No points added",Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                       // toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
                                        toast.show();
                                        return;
                                    }
                                    newDialog.show();
                                    Button createKMLbutton = (Button)newDialog.findViewById(R.id.createKMLButton);

                                    createKMLbutton.setOnClickListener(new View.OnClickListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onClick(View v) {
                                            createKMLfile();
                                        }
                                    });
                                }
                            });
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setTitle("File creation failed");
            alert.show();
        }
        else {
            kml.writeFile(f);
            builder.setMessage("KML file saved in folder Download/MineMapper/").setCancelable(false)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setTitle("Success");
            alert.show();
        }
    }

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

        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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




        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        latitude=(location.getLatitude());
        longitude=(location.getLongitude());

        textView2.setText(convertLatitud(location.getLatitude())+"   "+convertLongitude(location.getLongitude()));
        TextView textView3 = (TextView) findViewById(R.id.accuracy);
        DecimalFormat df = new DecimalFormat("#.#");
        accuracy = df.format(location.getAccuracy());
        currentAccuracy = df.format(location.getAccuracy());
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

}
