package com.example.andrea.shapedetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button_cam = (Button) findViewById(R.id.button);
        button_cam.setOnClickListener(new View.OnClickListener() {

            // @Override
            public void onClick(View v) {
                Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
                                          }
        }
        );
    }

    /**De la actividad ACTION_VIDEO_CAPTURE se espera recibir un resultado, en este caso, el video.
    Por eso se usa startActivityForResult. El resultado se recibe en onActivityResult(por defecto),
    en forma de otro objeto Intent**/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap) data.getExtras().get("data"); //con esto cogemos el resultado de la actividad
        Intent intent1 = new Intent(this, PhotoActivity.class); //creo un nuevo intent para pasarlo a la actividad siguiente
        intent1.putExtra("image", bitmap); //Añado la imagen al intent1 y la llamo "image"
        startActivity(intent1);
    }
}







