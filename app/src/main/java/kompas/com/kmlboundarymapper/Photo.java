//package kompas.com.kmlboundarymapper;
//
//import android.Manifest;
//        import android.annotation.SuppressLint;
//        import android.content.Intent;
//        import android.content.pm.PackageManager;
//        import android.graphics.Bitmap;
//        import android.graphics.BitmapFactory;
//        import android.location.Location;
//        import android.location.LocationListener;
//        import android.location.LocationManager;
//        import android.net.Uri;
//        import android.os.Bundle;
//        import android.provider.MediaStore;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//        import android.view.View;
//        import android.widget.Button;
//        import android.widget.ImageView;
//        import android.widget.Toast;
//
//
//        import java.io.File;
//        import java.io.FileOutputStream;
//        import java.io.IOException;
//        import java.text.SimpleDateFormat;
//        import java.util.Date;
//        import java.util.Locale;
//
//public class Photo extends AppCompatActivity implements LocationListener {
//
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//    private static final int REQUEST_IMAGE_CAPTURE = 1;
//    private static final String TAG = Photo.class.getSimpleName();
//    private ImageView imageView;
//    private LocationManager locationManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maps);
//        Button captureButton = findViewById(R.id.capture_button);
//        imageView = findViewById(R.id.image_view);
//
//        captureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ActivityCompat.checkSelfPermission(Photo.this, Manifest.permission.CAMERA)
//                        == PackageManager.PERMISSION_GRANTED) {
//                    capturePhoto();
//                } else {
//                    ActivityCompat.requestPermissions(Photo.this,
//                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
//                }
//            }
//        });
//
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//    }
//
//    @SuppressLint("MissingPermission")
//    private void capturePhoto() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                Log.e(TAG, "Error occurred while creating the File", ex);
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                Uri photoUri = Uri.fromFile(photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//            }
//        }
//    }
//
//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(null);
//        File imageFile = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//        return imageFile;
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            // Load the captured image from file
//            File imageFile = new File("/test");
//            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//
//            // Add date and time to the image
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//            String dateTime = dateFormat.format(new Date());
//            bitmap = addTextToBitmap(bitmap, dateTime);
//
//            // Add location coordinates to the image
//            Location location = getLastKnownLocation();
//            if (location != null) {
//                double latitude = location.getLatitude();
//                double longitude = location.getLongitude();
//                String coordinates = String.format(Locale.getDefault(), "(%.6f, %.6f)", latitude, longitude);
//                bitmap = addTextToBitmap(bitmap, coordinates);
//            }
//
//            // Save the modified image
//            try {
//                FileOutputStream out = new FileOutputStream(imageFile);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//                out.flush();
//                out.close();
//                Toast.makeText(this, "Image saved with date, time, and coordinates", Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                Log.e(TAG, "Error occurred while saving the image", e);
//            }
//
//            // Display the modified image
//            imageView.setImageBitmap(bitmap);
//        }
//    }
//
//    private Bitmap addTextToBitmap(Bitmap bitmap, String text) {
//        // Add text to bitmap (e.g., date, time, or coordinates)
//        // Code to add text to the bitmap goes here
//
//        return bitmap;
//    }
//
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
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        locationManager.removeUpdates(this);
//    }
//
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
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                capturePhoto();
//            } else {
//                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    public void onLocationChanged(@NonNull Location location) {
//        // Empty method implementation
//    }
//
//    @Override
//    public void onProviderDisabled(@NonNull String provider) {
//        // Empty method implementation
//    }
//
//    @Override
//    public void onProviderEnabled(@NonNull String provider) {
//        // Empty method implementation
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//        // Empty method implementation
//    }
//}
