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

public class AddLineKML  extends FragmentActivity implements OnMapReadyCallback {
    private String getrec;
    private String getacc;
    private GoogleMap mMap;
    private String globalLatitude;
    private String globalLongitude;
    private String accuracy;
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
    String notesStringLine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linemapkmlform);
        pillarCount = 0;
        markerList = new ArrayList<>();
        latLongList = new ArrayList<>();
        polygon = "";
        ActivityCompat.requestPermissions(AddLineKML.this, new String[]{
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
        final Button addPoint = (Button) findViewById(R.id.addLinePoint);
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
                alert.setTitle("Pillar " + latlong.name + " added");
                alert.show();
                TextView coordinates = (TextView) findViewById(R.id.lineCoordinates);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    polygon = latLongList.stream().map(e -> e.toString()).reduce("", String::concat);
                }
                polygon = polygon + "\n" + findDistancesBetweenPillars();
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
        final Button createKML = (Button) findViewById(R.id.createLineKML);
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
        deletePillarDialog.setContentView(R.layout.deletepillar);
        final Button deletePoint = (Button) findViewById(R.id.deleteLinePoint);
        deletePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePillarDialog.show();
                Button deletePillarButton = (Button) deletePillarDialog.findViewById(R.id.deletePillarButton);
                deletePillarButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText pillarName = (EditText) deletePillarDialog.findViewById(R.id.deletePillar);
                        MapPoint toBeDeletedPillar = null;
                        for (MapPoint mapPoint : latLongList) {
                            if (mapPoint.name.equalsIgnoreCase(pillarName.getText().toString())) {
                                toBeDeletedPillar = mapPoint;
                            }
                        }
                        if (toBeDeletedPillar == null) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Pillar number not found", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            //toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));

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

                        String deletePolygon = "";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            deletePolygon = latLongList.stream().map(e -> e.toString()).reduce("", String::concat);
                        }
                        deletePolygon = deletePolygon + "\n" + findDistancesBetweenPillars();
                        TextView coordinates = (TextView) findViewById(R.id.lineCoordinates);
                        coordinates.setText(deletePolygon);
                        deletePillarDialog.dismiss();
                    }
                });
            }
        });
        editPillarDialog = new Dialog(this);
        editPillarDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editPillarDialog.setContentView(R.layout.editpillar);
        Dialog editPillarPosDialog = new Dialog(this);
        editPillarPosDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editPillarPosDialog.setContentView(R.layout.editpillarpos);
        final Button editPoint = (Button) findViewById(R.id.editLinePoint);
        editPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPillarDialog.show();
                Button editPillarButton = (Button) editPillarDialog.findViewById(R.id.editPillarButton);
                editPillarButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editPillarName = (EditText) editPillarDialog.findViewById(R.id.editPillarText);
                        MapPoint toBeEditedPillar = null;
                        for (MapPoint mapPoint : latLongList) {
                            if (mapPoint.name.equalsIgnoreCase(editPillarName.getText().toString())) {
                                toBeEditedPillar = new MapPoint(mapPoint.lat, mapPoint.lon, mapPoint.accuracy);
                            }
                        }
                        if (toBeEditedPillar == null) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Pillar number not found", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                         //   toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));

                            toast.show();
                        }
                        editPillarDialog.dismiss();
                        if (toBeEditedPillar != null) {
                            editPillarPosDialog.show();
                        }
                    }
                });
                Button editPillarUpdateButton = (Button) editPillarPosDialog.findViewById(R.id.update);
                editPillarUpdateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editPillarName = (EditText) editPillarDialog.findViewById(R.id.editPillarText);
                        MapPoint toBeEditedPillar = null;
                        MapPoint editedPillar = null;
                        for (MapPoint mapPoint : latLongList) {
                            if (mapPoint.name.equalsIgnoreCase(editPillarName.getText().toString())) {
                                toBeEditedPillar = new MapPoint(mapPoint.lat, mapPoint.lon, mapPoint.accuracy);
                                mapPoint.lat = latitude;
                                mapPoint.lon = longitude;
                                mapPoint.accuracy = currentAccuracy;
                                editedPillar = mapPoint;
                            }
                        }
                        if (toBeEditedPillar == null) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Pillar number not found", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            //toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));

                            toast.show();
                        }
                        if (toBeEditedPillar != null) {
                            for (Marker marker : markerList) {
                                if (marker.getPosition().latitude == toBeEditedPillar.lat && marker.getPosition().longitude == toBeEditedPillar.lon) {
                                    marker.remove();
                                }
                            }
                        }

                        LatLng editkmlPoint = new LatLng(editedPillar.lat, editedPillar.lon);
                        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                        Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
                        Canvas canvas1 = new Canvas(bmp);

                        Paint color = new Paint();
                        color.setTextSize(50);
                        color.setColor(Color.YELLOW);


                        canvas1.drawText(editedPillar.name, 30, 40, color);
                        Marker editMarker = mMap.addMarker(new MarkerOptions().position(editkmlPoint).title("Pillar " + editedPillar.name + ": " + convertLatitud(editedPillar.lat) + " " + convertLongitude(editedPillar.lon)).icon(BitmapDescriptorFactory.fromBitmap(bmp)));
                        ;
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(editkmlPoint));
                        markerList.add(editMarker);
                        String editPolygon = "";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            editPolygon = latLongList.stream().map(e -> e.toString()).reduce("", String::concat);
                        }
                        editPolygon = editPolygon + "\n" + findDistancesBetweenPillars();
                        TextView coordinates = (TextView) findViewById(R.id.lineCoordinates);
                        coordinates.setText(editPolygon);
                        editPillarPosDialog.dismiss();
                    }
                });

            }
        });
        Dialog notesDialog = new Dialog(this);
        notesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        notesDialog.setContentView(R.layout.notesdialog);
        final Button notes = (Button) findViewById(R.id.lineComments);
        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesDialog.show();
                Button notesSubmit = (Button) notesDialog.findViewById(R.id.notesSubmit);
                notesSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText notesSection = (EditText) notesDialog.findViewById(R.id.commentsTextArea);
                        notesStringLine=notesSection.getText().toString();
                        notesDialog.dismiss();
                    }
                });
            }
        });
    }

    public String findDistancesBetweenPillars(){
        MapPoint current;
        MapPoint next;
        StringBuilder distancesString = new StringBuilder();
        double distance;
        for(int i=0;i<latLongList.size()-1;i++) {
            current=latLongList.get(i);
            next=latLongList.get(i+1);
            distance=haversineInMeters(current.lat,current.lon,next.lat,next.lon);

            distancesString.append("Distance between ").append(current.name).append(" and ").append(next.name).append(" : ").append(String.format("%.2f",distance)).append("m\n");
        }
        if(latLongList.size()>2){
            current=latLongList.get(latLongList.size()-1);
            next=latLongList.get(0);
            distance=haversineInMeters(current.lat,current.lon,next.lat,next.lon);
            distancesString.append("Distance between ").append(current.name).append(" and ").append(next.name).append(" : ").append(String.format("%.2f",distance)).append("m\n");
        }

        return distancesString.toString();
    }
    static double haversineInMeters(double lat1, double lon1,
                                    double lat2, double lon2)
    {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return (rad * c)*1000;
    }



    private static double EARTH_RADIUS = 6371009;

    static double ToRadians(double input)
    {
        return input / 180.0 * Math.PI;
    }

    public static double ComputeSignedArea(List<MapPoint> path)
    {
        return ComputeSignedArea(path, EARTH_RADIUS);
    }

    static double ComputeSignedArea(List<MapPoint> path, double radius)
    {
        int size = path.size();
        if (size < 3) { return 0; }
        double total = 0;
        MapPoint prev = path.get(size - 1);
        double prevTanLat = Math.tan((Math.PI / 2 - ToRadians(prev.lat)) / 2);
        double prevLng = ToRadians(prev.lon);

        for (MapPoint point:path) {
            double tanLat = Math.tan((Math.PI / 2 - ToRadians(point.lat)) / 2);
            double lng = ToRadians(point.lon);
            total += PolarTriangleArea(tanLat, lng, prevTanLat, prevLng);
            prevTanLat = tanLat;
            prevLng = lng;
        }
        return total * (radius * radius);
    }

    static double PolarTriangleArea(double tan1, double lng1, double tan2, double lng2)
    {
        double deltaLng = lng1 - lng2;
        double t = tan1 * tan2;
        return 2 * Math.atan2(t * Math.sin(deltaLng), 1 + t * Math.cos(deltaLng));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createKMLfile() {
        KML kml = new KML();
        Integer count =1;
        List<MapPoint> mapPoints = new ArrayList<>();
        StringBuilder pillarsInfo = new StringBuilder();
        for(MapPoint latlong: latLongList) {
            MapPoint mapPoint = new MapPoint(latlong.lat,latlong.lon,latlong.accuracy);
            mapPoint.setName(latlong.name);
            kml.addMark(mapPoint);
            pillarsInfo.append(latlong.name+":\nLat: "+convertLatitud(latlong.lat)+"\nLon: "+convertLongitude(latlong.lon)+"\nAcc: "+latlong.accuracy+"m\n\n");
            mapPoints.add(mapPoint);
            count++;
        }
        EditText fileName = (EditText) dialog.findViewById(R.id.filename);
        if(fileName.getText().toString().isEmpty()) {
            return;
        }

        if(notesStringLine!=null&&!notesStringLine.isEmpty()){
            pillarsInfo.append("Field Notes\n"+notesStringLine+"\n\n");
        }
        pillarsInfo.append("Map name: "+fileName.getText().toString()+" | created on: "+ LocalDate.now()+ " "+LocalDateTime.now().getHour()+":"+LocalDateTime.now().getMinute()+"\n\n");
        pillarsInfo.append("Mine Mapper | © Biju Sebastian 2023\n\n");
        kml.addLine(mapPoints,"Path",findDistancesBetweenPillars(),"Line String KML",pillarsInfo.toString());
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
                            Dialog newDialog = new Dialog(AddLineKML.this);
                            newDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            newDialog.setContentView(R.layout.createdialog);
                            final Button createKML = (Button) findViewById(R.id.createLineKML);
                            createKML.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(latLongList == null || latLongList.isEmpty()) {
                                        Toast toast = Toast.makeText(getApplicationContext(),"No points added",Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        //toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
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
        // Get URI and MIME type of file
//        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", f);
//        String mime = getContentResolver().getType(uri);
//
//        // Open file with user selected app
//        Intent intent = new Intent(AddMapKML.this,MapsActivity.class);
//
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setDataAndType(uri, "application/vnd.google-earth.kml+xml");
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivityForResult(Intent.createChooser(intent, "DEMO"), 1001);
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
        latitude=location.getLatitude();
        longitude=location.getLongitude();

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
