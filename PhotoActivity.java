package com.example.andrea.shapedetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class PhotoActivity extends AppCompatActivity {

    public static Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Intent intent = getIntent();
        photo = (Bitmap) intent.getParcelableExtra("image");
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(photo);
    }

    //Go to Detection activity when the button is clicked
    public void detButtonClicked(View view){
        Intent intent = new Intent(this, DetectionActivity.class);
        intent.putExtra("image", photo);
        startActivity(intent);

    }
}
