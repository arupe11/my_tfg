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
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.imgproc.Moments;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;


public class DetectionActivity extends AppCompatActivity {


    /*Rectángulo que engloba la figura geométrica. La declaramos fuera porque se usa tanto en
     figureClassification (para crearlo) como en colorDetection (para crear una submatriz de la
     foto original). Static porque ambos métodos son static*/
    public static Rect rect;


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
        Bitmap photo = intent.getParcelableExtra("image");

        TextView textView = (TextView) findViewById(R.id.textView);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        Mat photo_original = new Mat();


        //Pasamos el bitmap a Mat
        Utils.bitmapToMat(photo, photo_original);
        Mat photo_mat = photo_original.clone(); //la copio para tener la original intacta


        //Encontrar los contornos. Cada contorno se almacena como un vector de puntos
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        contours = findContours(photo_mat, contours);

        //Detectamos la figura
        MatOfPoint2f approxCurve;
        String tipo_figura = figureClasification(photo_mat, contours);

        if (tipo_figura != "") {
            //Detectamos el color
            String color = colorDetection(photo_original);


            //Pasamos a bitmap y mostramos la imagen
            Bitmap bitmap = Bitmap.createBitmap(photo_mat.cols(), photo_mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(photo_mat, bitmap);
            imageView.setImageBitmap(bitmap);
            String forma_color = tipo_figura + " " + color;
            textView.setText(forma_color);
        }
        else{
            textView.setText("Ninguna figura detectada :(");

        }
    }


    static List <MatOfPoint> findContours (Mat mat_img, List<MatOfPoint> contours){

        Mat canny = new Mat();
        Mat mat_gray = new Mat();
        Imgproc.cvtColor(mat_img, mat_gray,COLOR_BGR2GRAY);
        Imgproc.Canny(mat_gray, canny, 100, 100*3);
        Imgproc.findContours(canny, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;

    }
    static void drawContours(Mat mat_img, List<MatOfPoint> edgeContours) { //no queremos que nos devuelva nada sino que lo haga directamente

       // Mat img = mat_img.clone();
        Imgproc.drawContours(mat_img, edgeContours, -1, new Scalar(0,0, 255, 0)); //-1 dibuja todos los contornos
    }



    /**Método que clasifica las figuras geométricas en cuadrados, rectángulos y círculos y me
     * devuelve 0,1,2 o -1 dependiendo de cuál sea**/

    static String figureClasification (Mat mat_img, List <MatOfPoint> contours){

        String figure="";

        /*Recorremos cada contorno y calculamos su área, si es mayor que la calculada anteriormente,
        esa es la nueva maxArea (nos quedamos con la mayor)*/
        double maxArea = 0;
        MatOfPoint2f contour = new MatOfPoint2f(); //contorno definitivo (el mayor)
        MatOfPoint2f approxCurve2f = new MatOfPoint2f();
        MatOfPoint approxCurve = new MatOfPoint();
        List <MatOfPoint> approxCurves = new ArrayList<MatOfPoint>();



        for (int i=0; i<contours.size();i++) {
            MatOfPoint2f contour_temp = new MatOfPoint2f(contours.get(i).toArray());
            double area = Imgproc.contourArea(contour_temp);
            if (area > maxArea) {
                maxArea = area;
                contour = contour_temp;
            }
        }

        /*Creamos un rectángulo que englobe ese contorno*/
        contour.convertTo(approxCurve, CvType.CV_32S); //convertimos contour (MatOfPoint2f) a MatOfPoint
        rect = Imgproc.boundingRect(approxCurve); //argumento: MatOfPoint

        /*Calculamos los momentos para calcular el centro de masas*/
        Moments moments = Imgproc.moments(contour);
        Point centroid = new Point();
        centroid.x = moments.get_m10() / moments.get_m00();
        centroid.y = moments.get_m01() / moments.get_m00();

        /*Calculamos la relación de aspecto para ver si es un cuadrado o no*/
        int aspectratio = rect.width / rect.height;


        Imgproc.approxPolyDP(contour, approxCurve2f, Imgproc.arcLength(contour, true) * 0.02, true);
        if (approxCurve2f.total() == 4) {
            if (0.80 <= aspectratio && aspectratio <= 1.20) { //margen?
                figure="cuadrado"; //cuadrado
                approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
                approxCurves.add(approxCurve);
                Imgproc.putText(mat_img, "Cuadrado", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar (0, 255, 0, 0), 0); //from version 3.0 .putText() (with same parameters) moved from Core to Imgproc class.

            } else {
                figure = "rectángulo"; //rectángulo
                approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
                approxCurves.add(approxCurve);
                Imgproc.putText(mat_img, "Rectangulo", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0, 0), 0);
            }

        }

        if (approxCurve2f.total() == 3) {
            figure="triángulo"; //triángulo
            approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
            approxCurves.add(approxCurve);
            Imgproc.putText(mat_img, "Triangulo", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0,0), 0);
        }


        drawContours(mat_img, approxCurves);
        return figure;
    }


    /**Método que clasifica las figuras geométricas en cuadrados, rectángulos y círculos y me devuelve la
     imagen original con las figuras contorneadas y labeladas**/
    /**El objetivo es que devuelva un int que indique el color, pero de momento está devolviendo la imagen
     para ver el proceso**/

    static String colorDetection (Mat mat_img){

        Mat img = mat_img.clone();
        Mat submat = img.submat(rect); //rectángulo con únicamente la figura que nos interesa
        double[] pixelColor;
        int cols = submat.cols();
        int rows = submat.rows();
        int x = cols/2;
        int y = rows/2;
     //   for(int i=0; i<rows; i++){
       //     for(int j=0; j<cols; j++){
        pixelColor = submat.get(x,y);
        String color = obtenerColor(pixelColor);
      //      }
      //  }

        return color;
    }


    static String obtenerColor (double [] pixel) {
        String color ="";
        double [] morado_down = {70, 30, 80};
        double [] morado_up = {140, 90, 160};
        double [] azul_down = {10, 50, 80};
        double [] azul_up = {50, 100, 130};


        if (pixel[0]>=morado_down[0] && pixel[0]<= morado_up[0] &&
                pixel[1]>=morado_down[1] && pixel[1]<= morado_up[1] &&
                pixel[2]>=morado_down[2] && pixel[2]<= morado_up[2] ){
            color="morado";
        }else if (pixel[0]>=azul_down[0] && pixel[0]<= azul_up[0] &&
                pixel[1]>=azul_down[1] && pixel[1]<= azul_up[1] &&
                pixel[2]>=azul_down[2] && pixel[2]<= azul_up[2]){
            color="azul";
        }

        return color;
    }


    public void okClicked(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

}
