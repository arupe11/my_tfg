package com.example.andrea.shapedetection;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import android.content.Intent;
import android.widget.ImageView;
import org.opencv.android.Utils;
import org.opencv.imgproc.Moments;


import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;


public class DetectionActivity extends AppCompatActivity {


static {
    if(!OpenCVLoader.initDebug()){
        Log.i("opencv", "inicializacion fallida");
    }else
        Log.i("opencv", "inicializacion correcta");
}


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);
        Intent intent = getIntent();
        Bitmap photo = (Bitmap) intent.getParcelableExtra("image");

        Mat photo_mat = new Mat();
        Mat contour_img = new Mat();

        //Pasamos el bitmap a Mat
        Utils.bitmapToMat(photo, photo_mat);

        //Encontrar los contornos. Cada contorno se almacena como un vector de puntos
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        contours = findContours(photo_mat, contours);

        //Dibujar los contornos
        //contour_img = drawContours(photo_mat,contours);

        //detectamos la figura
        MatOfPoint2f approxCurve;
        contour_img = figureClasification(photo_mat, contours);


        //Pasamos a bitmap y mostramos la imagen
        Bitmap bitmap = Bitmap.createBitmap(contour_img.cols(), contour_img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(contour_img, bitmap);
        ImageView imageView= (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);

    }


    static List <MatOfPoint> findContours (Mat mat_img, List<MatOfPoint> contours){

        Mat canny = new Mat();
        Mat mat_gray = new Mat();
        Imgproc.cvtColor(mat_img, mat_gray,COLOR_BGR2GRAY);
        Imgproc.Canny(mat_gray, canny, 100, 100*3);
        Imgproc.findContours(canny, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;

    }
   static Mat drawContours(Mat mat_img, List<MatOfPoint> edgeContours) {

        Mat img = mat_img.clone();
        Imgproc.drawContours(img, edgeContours, -1, new Scalar(25,0, 255)); //-1 dibuja todos los contornos
        return img; //imagen original con los contornos
    }




/*
Método que clasifica las figuras geométricas en cuadrados, rectángulos y círculos y me devuelve la
imagen original con las figuras contorneadas y labeladas
*/

    static Mat figureClasification (Mat mat_img, List <MatOfPoint> contours){

        //Recorremos cada contorno y calculamos su área, si es mayor que la calculada anteriormente,
        //esa es la nueva maxArea (nos quedamos con la mayor)
        double maxArea = 0;
        MatOfPoint2f contour = new MatOfPoint2f(); //contorno definitivo (el mayor)
        MatOfPoint2f approxCurve2f = new MatOfPoint2f();
        MatOfPoint approxCurve = new MatOfPoint();
        List <MatOfPoint> approxCurves = new ArrayList<MatOfPoint>(); //matriz que contendrá todos los contornos que nos interesan




        for (int i=0; i<contours.size();i++) {
            MatOfPoint2f contour_temp = new MatOfPoint2f(contours.get(i).toArray());
            double area = Imgproc.contourArea(contour_temp);
            if (area > maxArea)
                maxArea = area;
                contour = contour_temp;
        }

        contour.convertTo(approxCurve, CvType.CV_32S); //convertimos contour (MatOfPoint2f) a MatOfPoint
        Rect rect = Imgproc.boundingRect(approxCurve); //argumento: MatOfPoint

        //Calculamos los momentos para calcular el centro de masas
        Moments moments = Imgproc.moments(contour);
        Point centroid = new Point();
        centroid.x = moments.get_m10() / moments.get_m00();
        centroid.y = moments.get_m01() / moments.get_m00();

        //Calculamos la relación de aspecto para ver si es un cuadrado o no
        int aspectratio = rect.width / rect.height;


        Mat img_contornos = new Mat();



           // MatOfPoint approxCurve = new MatOfPoint();
            Imgproc.approxPolyDP(contour, approxCurve2f, Imgproc.arcLength(contour, true) * 0.02, true);

                if (approxCurve2f.total() == 4) {

                    if (0.9 <= aspectratio && aspectratio <= 1.1) { //margen?
                        approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
                        approxCurves.add(approxCurve);
                        Imgproc.putText(mat_img, "Cuadrado", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(255, 255, 255), 0); //from version 3.0 .putText() (with same parameters) moved from Core to Imgproc class.


                    } else {
                        approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
                        approxCurves.add(approxCurve);
                        Imgproc.putText(mat_img, "Rectangulo", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(255, 255, 255), 0);
                    }


                }
                if (approxCurve2f.total() == 3) {
                    approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
                    approxCurves.add(approxCurve);
                    Imgproc.putText(mat_img, "Triangulo", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(255, 255, 255), 0);
                }




        img_contornos = drawContours(mat_img, approxCurves);
        return img_contornos;
    }
    public void okClicked(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

}
