package com.example.digivault;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FullScreenImage extends AppCompatActivity {

    ImageView fullScreenImageView;
    TextView imageTitle;
    ImageButton shareBtn, deleteBtn, backArrowBtn, editBtn, infoBtn;
    String img;
    String imgName;
    String imgId;
    String imgLocation;

    Encryption encryption = new Encryption();
    ImageDBHelper imageDbHelper;
    private Uri dUri;
    private Context context = this;

    public void printToast(String text) {
        Toast.makeText(FullScreenImage.this, text, Toast.LENGTH_SHORT).show();
    }

    //runs whenever this activity starts
    @Override
    protected void onStart() {
        super.onStart();
    }

    private void shareImage(String imgId, String image) throws Exception {
        ArrayList<Uri> uriList = new ArrayList<>();
        String imgName = imageDbHelper.getImageName(imgId);
        String fileName = imgName+".pdf";
        String path = getExternalFilesDir(null).getAbsolutePath().toString()+"/DigiVault/"+fileName;
        File docFile = new File(path);
        if(docFile.exists()){
            uriList.add(Uri.fromFile(docFile));
        } else {
            byte[] imageBytes = encryption.decrypt(image);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
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
            File dir = new File(getExternalFilesDir(null).getAbsolutePath().toString()+"/DigiVault/");
            if(!dir.exists())
                dir.mkdir();

            File doc = new File(dir, fileName);
            try{
                FileOutputStream fos = new FileOutputStream(doc);
                pdfDocument.writeTo(fos);
            } catch (IOException e){
                e.printStackTrace();
            }
            pdfDocument.close();
            uriList.add(Uri.fromFile(docFile));
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        intent.setType("application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void cropImage(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }
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
                Log.i("file del", String.valueOf(res));
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
                byte[] img = byteArray.toByteArray();
                byte[] encryptedImg = new byte[0];
                try {
                    encryptedImg = encryption.encrypt(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long lengthbmp = img.length/1024;
                Log.i("size of image in kb", String.valueOf(lengthbmp));
                String imageString = Base64.encodeToString(encryptedImg, Base64.DEFAULT);
                if (!imageDbHelper.updateImage(imageString, imgId)) {
                    printToast("Could not add image");
                } else {
                    Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        getSupportActionBar().hide();

        imageDbHelper = new ImageDBHelper(this);

        fullScreenImageView = (ImageView) findViewById(R.id.fullScreenImageView);
        imageTitle = (TextView) findViewById(R.id.imageTitle);
        shareBtn = (ImageButton) findViewById(R.id.shareBtn);
        deleteBtn = (ImageButton) findViewById(R.id.deleteBtn);
        backArrowBtn = (ImageButton) findViewById(R.id.backBtn);
        editBtn = (ImageButton) findViewById(R.id.editBtn);
        infoBtn = (ImageButton) findViewById(R.id.infoBtn);

        Intent i = getIntent();
        img = i.getExtras().getString("image");
        imgId = i.getExtras().getString("imageId");
        Log.i("img", img);
//        Log.i("iamgeName", imgName);
//        Log.i("imageLocation", imgLocation);
        Log.i("img id", imgId);
        imgName = imageDbHelper.getImageName(imgId);
        imgLocation = imageDbHelper.getImgLocation(imgId);
        imageTitle.setText(imgName);

        byte[] imageBytes = new byte[0];
        try {
            imageBytes = encryption.decrypt(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        fullScreenImageView.setImageBitmap(decodedImage);

        // function helps in going back to signup page using back arrow button instead of mobiles's back button
        backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GalleryActivity.class); // moves to gallery page
                startActivity(intent);
                finish();
            }
        });
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printToast(imgLocation);
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> imgIdList = new ArrayList<>();
                imgIdList.add(Integer.valueOf(imgId));
                if(imageDbHelper.deleteImage(imgIdList) == 0){
                    printToast("Could not delete image");
                } else {
                    Intent intent = new Intent(getApplicationContext(), GalleryActivity.class); // moves to gallery page
                    startActivity(intent);
                    finish();
                }
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] imageBytes = new byte[0];
                try {
                    imageBytes = encryption.decrypt(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), decodedImage, "Title", null);
                Uri uri = Uri.parse(path);
                dUri = uri;
                cropImage(uri);
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    shareImage(imgId, img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}