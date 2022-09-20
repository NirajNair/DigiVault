package com.example.digivault;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;

import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class GalleryActivity extends AppCompatActivity {

    Button galleryUploadButton, camUploadButton;
    ImageButton locationBtn;
    TextView userName;
    String loc;
    Encryption encryption = new Encryption();
    private Toolbar mtoolbar;
    private List<Integer> SelectedImages = new ArrayList<>();
    private List<Integer> ImageIdList;
    //    private List<String> ImageIdList = new ArrayList<>();
    LinkedHashMap<Integer, String> ImageIds = new LinkedHashMap<Integer, String>(); // list of all the images
    // ideally we need to pull it from db
    private Context context = this;
    public String imgLocation = new String("Added from Dombivli, Thane, Maharashtra");
    ImageAdaptor adaptor; // helper class such as dbhelper, needed to deal with images

    ActivityResultLauncher<String> getContent;
    ActivityResultLauncher<Intent> getCamContent, deletePdfDoc;

    private Uri dUri;
    private LocationRequest locationRequest;
    ImageDBHelper imageDbHelper;

    public GalleryActivity() throws NoSuchAlgorithmException {
    }

    // deletes the session of the current user after logging out
    private void deleteSession(SessionManagement s) {
        s.deleteSession();
    }

    public void printToast(String text) {
        Toast.makeText(GalleryActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    //runs whenever this activity starts
    @Override
    protected void onStart() {
        super.onStart();
        ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
    }

    // runs when the activity gets created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        galleryUploadButton = (Button) findViewById(R.id.uploadButtonGallery);
        camUploadButton = (Button) findViewById(R.id.uploadButtonCam);
        locationBtn = (ImageButton) findViewById(R.id.locationBtn);
        mtoolbar = (Toolbar) findViewById(R.id.toolbar); // created a custom contextual toolbar
        setSupportActionBar(mtoolbar); // setting this toolbar to the activity

        SessionManagement sessionManagement = new SessionManagement(GalleryActivity.this);
        imageDbHelper = new ImageDBHelper(this);
        ImageIds.clear();
        ImageIds = imageDbHelper.getListOfImages(sessionManagement.getSession());
        ImageIdList = new ArrayList<>(ImageIds.keySet());

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        GridView gridView = findViewById(R.id.imageGrid);
        gridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL); // this makes the image long clickable
        gridView.setMultiChoiceModeListener(modeListener);
        try {
            adaptor = new ImageAdaptor(ImageIds, this); // creating a new adaptor obj and passing all the image ids to display
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        gridView.setAdapter(adaptor);

        // function runs when a image is clicked
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.i("imageIdList", String.valueOf(ImageIdList.get(i)));
                String img = ImageIds.get(ImageIdList.get(i)); // getting the image position ie id
                Intent intent = new Intent(getApplicationContext(), FullScreenImage.class); // moves to gallery page
                intent.putExtra("image", img);
                intent.putExtra("imageId", String.valueOf(ImageIdList.get(i)));
                startActivity(intent);
            }
        });

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation(0);
//                Log.i("loc", loc);
            }
        });

        getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result);
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArray);
                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
                    Uri uri = Uri.parse(path);
                    dUri = uri;
                    cropImage(uri);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        galleryUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
                getContent.launch("image/*");
            }
        });

        getCamContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");

                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
                    Uri uri = Uri.parse(path);
                    dUri = uri;
                    cropImage(uri);
                }
            }
        });

        camUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(GalleryActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{
                            Manifest.permission.CAMERA
                    }, 1);
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                getCamContent.launch(intent);

            }
        });

    }

    private void cropImage(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (isGPSEnabled()) {
                    getCurrentLocation(0);
                }else {
                    turnOnGPS();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ContentResolver resolver = getContentResolver();
                int res = resolver.delete(dUri, null, null);
//                Log.i("file del", String.valueOf(res));
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArray);
                byte[] img = byteArray.toByteArray();
                long lengthbmp = img.length/1024;
                String imageString;
                byte[] encryptedImg= null;
                try {
                    encryptedImg = encryption.encrypt(img);

                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imageString= Base64.encodeToString(encryptedImg, Base64.DEFAULT);
//                Log.i("size of image in kb", String.valueOf(lengthbmp));
//                Log.i("imageString", imageString);
//                Log.i("encryptedImg", String.valueOf(encryptedImg));
                SessionManagement sessionManagement = new SessionManagement(GalleryActivity.this);
                String username = sessionManagement.getSession();
                int imageId = imageDbHelper.getLastImageId(username);
                String imageName = "img" + String.valueOf(imageId + 1);
                try{
                    getCurrentLocation(1);
                } finally {
                    if (!imageDbHelper.insertImage(username, String.valueOf(imageId + 1), imageName, imageString, imgLocation)) {
                        printToast("Could not add image");
                    } else {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
       }
    }

    private void getCurrentLocation(int i) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(GalleryActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(GalleryActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @SuppressLint("LongLogTag")
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(GalleryActivity.this)
                                            .removeLocationUpdates(this);
                                    if (locationResult != null && locationResult.getLocations().size() >0){
                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();
                                        try {
                                            Locale mLocale = new Locale("EN");
                                            Geocoder geocoder = new Geocoder(GalleryActivity.this, mLocale);
                                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
//                                            Log.i("address size ", String.valueOf(addresses.size()));
                                            if (addresses != null && addresses.size() > 0) {
//                                                Log.i("getLocale ", String.valueOf(addresses.get(0).getLocale()));
                                                String locstr = addresses.get(0).getLocality()+", "+
                                                        addresses.get(0).getSubAdminArea()+", "+
                                                        addresses.get(0).getAdminArea();
//                                                Log.i("add to show", locstr);
                                                Log.i("i ki value  ", String.valueOf(i));
                                                if (i == 1) {
                                                    imgLocation = locstr;
                                                    Log.i("GalleryActivity.imgLocation ", imgLocation );

                                                } else {
                                                    printToast("Accessing from "+locstr);
                                                }
                                            }
                                            else {
//                                                Log.i("address and country ", "unknown" );
                                                printToast("Error could not find location");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
//                                        Log.i("Latitude and Longitude: ", String.valueOf(latitude) + String.valueOf(longitude));
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            }
        }
    }


    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(GalleryActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(GalleryActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }

    // creates a toolbar when the activity in created
        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            super.onCreateOptionsMenu(menu);
            getMenuInflater().inflate(R.menu.main_menu, menu); // to display a toolbar a menus has to be inflated
            return true;
        }

        // this function takes care of what happens if a menu item is clicked
        @Override
        public boolean onOptionsItemSelected (MenuItem menuItem){
            switch (menuItem.getItemId()) {
                case R.id.logout:
                    userLogout();
                    return true;
                case R.id.about:
                    return true;
                default:
                    return super.onOptionsItemSelected(menuItem);
            }
        }

        // function to logout the user
        private void userLogout() {
            SessionManagement sessionManagement = new SessionManagement(GalleryActivity.this);
            deleteSession(sessionManagement); // deletes the session
            Intent intent = new Intent(GalleryActivity.this, LoginActivity.class);
            startActivity(intent); // goes back to login activity
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        private void shareImage(List<Integer> SelectedImages) throws Exception {
            ArrayList<Uri> uriList = new ArrayList<>();
            for(Integer img:SelectedImages){
                    byte[] imgBytes = encryption.decrypt(ImageIds.get(img));

                    Bitmap decodedImage = BitmapFactory.decodeByteArray(imgBytes, 0,
                            imgBytes.length);
                        PdfDocument pdfDocument = new PdfDocument();
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(decodedImage.getWidth(), decodedImage.getHeight(), 1).create();
                        PdfDocument.Page pdfPage = pdfDocument.startPage(pageInfo);
                        Canvas canvas = pdfPage.getCanvas();
                        Paint paint = new Paint();
                        paint.setColor(Color.WHITE);
                        canvas.drawPaint(paint);
                        decodedImage = Bitmap.createScaledBitmap(decodedImage, decodedImage.getWidth(), decodedImage.getHeight(), true);
                        paint.setColor(Color.BLUE);
                        canvas.drawBitmap(decodedImage, 0, 0, null);
                        pdfDocument.finishPage(pdfPage);
                        String imgName = imageDbHelper.getImageName(String.valueOf(img));
                        String fileName = imgName+".pdf";
                        File dir = new File(getExternalFilesDir(null).getAbsolutePath().toString()+"/DigiVault/");
                        if(!dir.exists())
                            dir.mkdir();

                        File doc = new File(dir, fileName);
                        FileOutputStream fos = new FileOutputStream(doc);
                        pdfDocument.writeTo(fos);
                    pdfDocument.close();
                    String path = getExternalFilesDir(null).getAbsolutePath().toString()+"/DigiVault/"+fileName;
                    File docFile = new File(path);
                    uriList.add(Uri.fromFile(docFile));
                }

            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
            intent.setType("application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share via"));
        }

        // listener for image selected for share and delete
        AbsListView.MultiChoiceModeListener modeListener = new AbsListView.MultiChoiceModeListener() {
            //runs everytime the state of selection of image changes
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if (SelectedImages.contains(ImageIdList.get(i)))
                    SelectedImages.remove(ImageIdList.get(i));
                else
//                    Log.i("selected images i ", String.valueOf(ImageIdList.get(i)));
                    SelectedImages.add(ImageIdList.get(i));
                actionMode.setTitle(SelectedImages.size() + " selected");
                for(Integer img:SelectedImages){
//                    Log.i("pos ", String.valueOf(img)); // Logging for debugging
                }
            }

            // runs when this listener is created
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater(); // creates a inflater
                inflater.inflate(R.menu.contextual_menu, menu); // inflates the contextual menu
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            // listener when the menu item is clicked
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
//                ActivityResultLauncher<IntentSender> removeUri = registerForActivityResult(Ac)

                switch (menuItem.getItemId()) { // getting the id of the menu item clicked
                    case R.id.delete:
                        if(imageDbHelper.deleteImage(SelectedImages) == 0){
                            printToast("Could not delete image");
                        } else {
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                            return true;

                        }
                        actionMode.finish(); // this defines that the contextual menu must hide now
                    case R.id.share: // NOTE: does not work properly
                        try {
                            shareImage(SelectedImages);
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                        } catch (InvalidAlgorithmParameterException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    default:
                        return false;
                }
            }

            //clears all the list of selected images when contextual menu is stopped
            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                SelectedImages.clear();
            }
        };

}