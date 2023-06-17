package kompas.com.kmlboundarymapper;

import static kompas.com.kmlboundarymapper.MapsActivity.convertLatitud;
import static kompas.com.kmlboundarymapper.MapsActivity.convertLongitude;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AddKML extends Activity {

    AlertDialog.Builder builder;
    List<MapPoint> latLongList;
    String polygon;
    String m_strLatitudeDMS;
    String m_strLongitudeDMS;
    Dialog dialog;
    View mapView;
    List<Marker> markerList;
    double latitude;
    double longitude;
    Dialog deletePillarDialog;
    Dialog editPillarDialog;
    Integer pillarCount;
    String editLatitude;
    String editLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        polygon="";
        pillarCount=0;
        latLongList = new ArrayList<>();
        setContentView(R.layout.kmlform);
        builder = new AlertDialog.Builder(this);

        final Button addPoint = (Button) findViewById(R.id.addPointC);
        addPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                latitude=convertLatitudetoDecimal();
                longitude=convertLongitudeToDecimal();
                if(latitude==0||longitude==0) {
                    if(latitude==0) {
                        Toast toast = Toast.makeText(getApplicationContext(),"Invalid latitude",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
                        toast.show();
                    }
                    if(longitude==0) {
                        Toast toast = Toast.makeText(getApplicationContext(),"Invalid longitude",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
                        toast.show();                    }
                    return;
                }
                pillarCount++;
                MapPoint latlong = new MapPoint(latitude,longitude);
                latlong.setName(String.valueOf(pillarCount));
                latLongList.add(latlong);
                // builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
                builder.setMessage(convertLatitud(latitude)+","+convertLongitude(longitude)).setCancelable(false)
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("Pillar "+latlong.name+" added");
                alert.show();
                TextView coordinates = (TextView) findViewById(R.id.coordinates);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    polygon=latLongList.stream().map(e -> e.toString()).reduce("", String::concat);
                }
                polygon=polygon+"\n"+findDistancesBetweenPillars()+"\n"+calculatePolygonArea();
                coordinates.setText(polygon);
                coordinates.setMovementMethod(new ScrollingMovementMethod());
            }
        });
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.createdialog);
        final Button createKML = (Button) findViewById(R.id.createKML);
        createKML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(latLongList == null || latLongList.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(),"No pillars added",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
                    toast.show();
                    return;
                }
                dialog.show();
                Button createKMLbutton = (Button)dialog.findViewById(R.id.createKMLButton);

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
        final Button deletePoint = (Button) findViewById(R.id.deletePointC);
        deletePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePillarDialog.show();
                Button deletePillarButton = (Button)deletePillarDialog.findViewById(R.id.deletePillarButton);
                deletePillarButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText pillarName = (EditText) deletePillarDialog.findViewById(R.id.deletePillar);
                        MapPoint toBeDeletedPillar = null;
                        for(MapPoint mapPoint:latLongList) {
                            if(mapPoint.name.equalsIgnoreCase(pillarName.getText().toString())){
                                toBeDeletedPillar=mapPoint;
                            }
                        }
                        if(toBeDeletedPillar==null) {
                            Toast toast = Toast.makeText(getApplicationContext(),"Pillar number not found",Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
                            toast.show();                         }
                        latLongList.remove(toBeDeletedPillar);
                        String deletePolygon="";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            deletePolygon=latLongList.stream().map(e -> e.toString()).reduce("", String::concat);
                        }
                        deletePolygon=deletePolygon+"\n"+findDistancesBetweenPillars()+"\n"+calculatePolygonArea();
                        TextView coordinates = (TextView) findViewById(R.id.coordinates);
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
        editPillarPosDialog.setContentView(R.layout.editpillarinput);
        final Button editPoint = (Button) findViewById(R.id.editPointC);
        editPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPillarDialog.show();
                Button editPillarButton = (Button)editPillarDialog.findViewById(R.id.editPillarButton);
                editPillarButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editPillarName = (EditText) editPillarDialog.findViewById(R.id.editPillarText);
                        MapPoint tobeEditedPillar = null;
                        for(MapPoint mapPoint:latLongList) {
                            if(mapPoint.name.equalsIgnoreCase(editPillarName.getText().toString())){
                                tobeEditedPillar=mapPoint;
                                EditText editLat = editPillarPosDialog.findViewById(R.id.editLat);
                                EditText editLon = editPillarPosDialog.findViewById(R.id.editLon);
                                EditText latval = (EditText)findViewById(R.id.latitudeVal);
                                EditText lonval = (EditText)findViewById(R.id.longitudeVal);
                                editLat.setText(latval.getText().toString());
                                editLon.setText(lonval.getText().toString());
                            }
                        }
                        if(tobeEditedPillar==null) {
                            Toast toast = Toast.makeText(getApplicationContext(),"Pillar number not found",Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
                            toast.show();
                            editPillarDialog.dismiss();
                        }
                        if(tobeEditedPillar!=null) {
                            editPillarDialog.dismiss();
                            editPillarPosDialog.show();
                        }
                    }
                });
                Button editPillarUpdateButton = (Button)editPillarPosDialog.findViewById(R.id.updateInput);
                editPillarUpdateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editPillarName = (EditText) editPillarDialog.findViewById(R.id.editPillarText);
                        MapPoint tobeEditedPillar = null;
                        for(MapPoint mapPoint:latLongList) {
                            if(mapPoint.name.equalsIgnoreCase(editPillarName.getText().toString())){
                                tobeEditedPillar=mapPoint;
                            }
                        }
                        if(tobeEditedPillar!=null){
                            EditText editLat = editPillarPosDialog.findViewById(R.id.editLat);
                            EditText editLon = editPillarPosDialog.findViewById(R.id.editLon);
                            editLatitude=editLat.getText().toString();
                            editLongitude=editLon.getText().toString();
                            latitude=convertLatitudetoDecimaEdit();
                            longitude=convertLongitudeToDecimalEdit();
                            if(latitude==0||longitude==0) {
                                if(latitude==0) {
                                    Toast toast = Toast.makeText(getApplicationContext(),"Invalid latitude",Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
                                    toast.show();
                                }
                                if(longitude==0) {
                                    Toast toast = Toast.makeText(getApplicationContext(),"Invalid longitude",Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));
                                    toast.show();
                                }
                                return;
                            }
                            tobeEditedPillar.lat=latitude;
                            tobeEditedPillar.lon=longitude;

                        }
                        String editPolygon="";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            editPolygon=latLongList.stream().map(e -> e.toString()).reduce("", String::concat);
                        }
                        editPolygon=editPolygon+"\n"+findDistancesBetweenPillars()+"\n"+calculatePolygonArea();
                        TextView coordinates = (TextView) findViewById(R.id.coordinates);
                        coordinates.setText(editPolygon);
                        editPillarPosDialog.dismiss();
                    }
                });

            }
        });

        //
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

    public String calculatePolygonArea()
    {
        double area=ComputeSignedArea(latLongList);
        return "Area: "+String.format("%.5f",Math.abs(area)/10000)+"ha | "+String.format("%.5f",Math.abs(area))+"sq.m | "+String.format("%.5f",Math.abs(area)/4047)+"acres";
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
            MapPoint mapPoint = new MapPoint(latlong.lat,latlong.lon);
            mapPoint.setName(latlong.name);
            kml.addMark(mapPoint);
            mapPoints.add(mapPoint);
            pillarsInfo.append("Pillar "+latlong.name+":\nLat: "+convertLatitud(latlong.lat)+"\nLon: "+convertLongitude(latlong.lon)+"\n\n");
            count++;
        }
        EditText fileName = (EditText) dialog.findViewById(R.id.filename);
        if(fileName.getText().toString().isEmpty()) {
            return;
        }
        if(latLongList.size()>0){
            mapPoints.add(latLongList.get(0));
        }
        pillarsInfo.append(calculatePolygonArea()+"\n\n");
        pillarsInfo.append("Map name: "+fileName.getText().toString()+" | Created on: "+ LocalDate.now()+" "+ LocalDateTime.now().getHour()+":"+LocalDateTime.now().getMinute()+"\n\n");
        pillarsInfo.append("Mine Mapper | © Biju Sebastian 2021\n\n");
        kml.addPath(mapPoints,"Path",findDistancesBetweenPillars(),calculatePolygonArea(),pillarsInfo.toString());
        String rootPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/Download/MineMapper/";
        File root = new File(rootPath);
        if (!root.exists()) {
            root.mkdirs();
        }

        File f = new File(rootPath + fileName.getText().toString()+".kml");
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


    public double convertLatitudetoDecimaEdit() {

        m_strLatitudeDMS = editLatitude;
        if (isValidLatitude(m_strLatitudeDMS))
        {
            String[] strDMS = TextUtils.split(m_strLatitudeDMS, ":");

            int nDegrees    = Integer.valueOf(strDMS[0]);
            int nMinutes    = Integer.valueOf(strDMS[1]);
            double dSeconds = Double.valueOf(strDMS[2]);

            double m_dLatitude = nDegrees + (nMinutes / 60.0) + (dSeconds / 3600.0);


            Spinner spinLatitudeDir = (Spinner) findViewById(R.id.latSpinner);
            if(spinLatitudeDir.getSelectedItemPosition() == 0) {
                return m_dLatitude;
            }
            else return -1*m_dLatitude;
        }
        return 0;
    }

    public double convertLongitudeToDecimalEdit() {

        m_strLongitudeDMS = editLongitude;
        if (isValidLongitude(m_strLongitudeDMS))
        {
            String[] strDMS = TextUtils.split(m_strLongitudeDMS, ":");

            int nDegrees    = Integer.valueOf(strDMS[0]);
            int nMinutes    = Integer.valueOf(strDMS[1]);
            double dSeconds = Double.valueOf(strDMS[2]);

            double m_dLongitude = nDegrees + (nMinutes / 60.0) + (dSeconds / 3600.0);


            Spinner spinLongitudeDir = (Spinner) findViewById(R.id.longSpinner);
            if(spinLongitudeDir.getSelectedItemPosition() == 0) {
                return m_dLongitude;
            }
            else return -1*m_dLongitude;
        }
        return 0;
    }


    public double convertLatitudetoDecimal() {
        EditText latitude = (EditText) findViewById(R.id.latitudeVal);
        m_strLatitudeDMS = latitude.getText().toString();
        if (isValidLatitude(m_strLatitudeDMS))
        {
            String[] strDMS = TextUtils.split(m_strLatitudeDMS, ":");

            int nDegrees    = Integer.valueOf(strDMS[0]);
            int nMinutes    = Integer.valueOf(strDMS[1]);
            double dSeconds = Double.valueOf(strDMS[2]);

            double m_dLatitude = nDegrees + (nMinutes / 60.0) + (dSeconds / 3600.0);


            Spinner spinLatitudeDir = (Spinner) findViewById(R.id.latSpinner);
            if(spinLatitudeDir.getSelectedItemPosition() == 0) {
                return m_dLatitude;
            }
            else return -1*m_dLatitude;
        }
        return 0;
    }

    public double convertLongitudeToDecimal() {
        EditText longitude = (EditText) findViewById(R.id.longitudeVal);
        m_strLongitudeDMS = longitude.getText().toString();
        if (isValidLongitude(m_strLongitudeDMS))
        {
            String[] strDMS = TextUtils.split(m_strLongitudeDMS, ":");

            int nDegrees    = Integer.valueOf(strDMS[0]);
            int nMinutes    = Integer.valueOf(strDMS[1]);
            double dSeconds = Double.valueOf(strDMS[2]);

            double m_dLongitude = nDegrees + (nMinutes / 60.0) + (dSeconds / 3600.0);


            Spinner spinLongitudeDir = (Spinner) findViewById(R.id.longSpinner);
            if(spinLongitudeDir.getSelectedItemPosition() == 0) {
                return m_dLongitude;
            }
            else return -1*m_dLongitude;
        }
        return 0;
    }

    public static boolean isValidLatitude( String strLatitude )
    {
        // this handles the format but doesn't check the ranges (without extra symbols)
        // return strLatitude.matches("[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}(\\.[0-9]+)?");

        String[] lat = TextUtils.split(strLatitude, ":");
        if (lat.length == 3) // all three parts must exist
        {
            double dDegrees = Double.parseDouble(lat[0]);
            double dMinutes = Double.parseDouble(lat[1]);
            double dSeconds = Double.parseDouble(lat[2]);

            // and each must be within range
            return (dDegrees >= 0.0 && dDegrees <= 90.0) &&
                    (dMinutes >= 0.0 && dMinutes < 60.0) &&
                    (dSeconds >= 0.0 && dSeconds < 60.0);
        }

        return false;
    }

    public static boolean isValidLongitude( String strLongitude )
    {
        // this handles the format but doesn't check the ranges (without extra symbols)
        // return strLatitude.matches("[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}(\\.[0-9]+)?");

        String[] lon = TextUtils.split(strLongitude, ":");
        if (lon.length == 3) // all three parts must exist
        {
            double dDegrees = Double.parseDouble(lon[0]);
            double dMinutes = Double.parseDouble(lon[1]);
            double dSeconds = Double.parseDouble(lon[2]);

            // and each must be within range
            return (dDegrees >= 0.0 && dDegrees <= 180.0) &&
                    (dMinutes >= 0.0 && dMinutes < 60.0) &&
                    (dSeconds >= 0.0 && dSeconds < 60.0);
        }
        return false;
    }
}
