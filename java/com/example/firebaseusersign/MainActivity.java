package com.example.firebaseusersign;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.InputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    EditText roll, name, course,contact;
    Uri filepath;
    ImageView img;
    Button browse, signup;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img=findViewById(R.id.img);
        signup=findViewById(R.id.upload);
        browse=findViewById(R.id.browse);


        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(MainActivity.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent,"Select Image File"),1);

                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                 token.continuePermissionRequest();
                            }
                        }).check();


            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadtoFireBase();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==1 && resultCode==RESULT_OK){

            filepath =data.getData();
            try{

                InputStream inputStream =getContentResolver().openInputStream(filepath);
                bitmap= BitmapFactory.decodeStream(inputStream);
                img.setImageBitmap(bitmap);

            }catch (Exception e){}
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void uploadtoFireBase() {

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Image Uploader");
        dialog.show();

        name= findViewById(R.id.t1);
        course= findViewById(R.id.t2);
        contact= findViewById(R.id.t3);
        roll= findViewById(R.id.t4);


        //Yahan pe hum image file store krenge
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference  uploader = storage.getReference("Image1"+ new Random().nextInt(50));


        uploader.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                       FirebaseDatabase db = FirebaseDatabase.getInstance();
                       DatabaseReference root= db.getReference("user");
                       dataholder obj = new dataholder(name.getText().toString(),
                                                       contact.getText().toString(),
                                                       course.getText().toString(),
                                                        uri.toString());

                       root.child(roll.getText().toString()).setValue(obj);

                       name.setText("");
                       course.setText("");
                       contact.setText("");
                       roll.setText("");
                       img.setImageResource(R.drawable.profile);
                        Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_LONG).show();

                    }
                });

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                float percent = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                dialog.setMessage("Uploaded: "+(int)percent +"%");


            }
        });

        //image storing code ends here

    }

}