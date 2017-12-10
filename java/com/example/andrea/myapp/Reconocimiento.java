package com.example.andrea.myapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;

public class Reconocimiento extends AppCompatActivity {

    public static Mat frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_reconocimiento);

        Intent intent = getIntent();
        long address = intent.getLongExtra("frame", 0);
        Mat frame_temp = new Mat(address);
        frame = frame_temp.clone();

        //Mat mat_gray = new Mat();
        Imgproc.cvtColor(frame, frame, COLOR_BGR2GRAY);

        //Volvemos a pasar el frame a la actividad Camera
        long address2 = frame.getNativeObjAddr(); //para pasar Mat's se hace con el address
        Intent intent2 = new Intent();
        intent2.putExtra("frame2", address2);
        setResult(RESULT_OK, intent2);
        finish();
    }

}
