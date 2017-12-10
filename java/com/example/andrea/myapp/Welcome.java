package com.example.andrea.myapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }

    //Go to Camera activity when one of the buttons is clicked
    public void openCameraOnClick(View view){
        Button b = (Button)view;
        String buttonText = b.getText().toString();
        Intent intent = new Intent(this, Camera.class);
        intent.putExtra(Camera.NIVEL, buttonText);
        startActivity(intent);

    }


}
