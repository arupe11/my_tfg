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
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;


public class DetectionActivity extends AppCompatActivity {


    /*Rectángulo que engloba la figura geométrica. La declaramos fuera porque se usa tanto en
     figureClassification (para crearlo) como en colorDetection (para crear una submatriz de la
     foto original). Static porque ambos métodos son static*/
    public static RotatedRect rotrect;
    public static Rect rect;
    public static String Hue="";



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
        TextView textView2 = (TextView) findViewById(R.id.textView2);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        Mat photo_original = new Mat();


        //Pasamos el bitmap a Mat
        Utils.bitmapToMat(photo, photo_original);
        Mat photo_mat = photo_original.clone(); //la copio para tener la original intacta


        //Encontrar los contornos. Cada contorno se almacena como un vector de puntos
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        contours = findContours(photo_mat, contours);

        //Detectamos la figura
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
            textView2.setText(Hue);

        } else{
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

        /*Creamos un rectángulo que englobe ese contorno (uno recto para luego hacer el submat y
          otro rotado para el aspect ratio*/
        contour.convertTo(approxCurve, CvType.CV_32S); //convertimos contour (MatOfPoint2f) a MatOfPoint
        rect = Imgproc.boundingRect(approxCurve);
        rotrect = Imgproc.minAreaRect(contour); //argumento: MatOfPoint2f

        /*Calculamos los momentos para calcular el centro de masas*/
        Moments moments = Imgproc.moments(contour);
        Point centroid = new Point();
        centroid.x = moments.get_m10() / moments.get_m00();
        centroid.y = moments.get_m01() / moments.get_m00();

        /*Calculamos la relación de aspecto para ver si es un cuadrado o no*/
        double aspectratio = rotrect.size.width / rotrect.size.height;

        Imgproc.approxPolyDP(contour, approxCurve2f, Imgproc.arcLength(contour, true) * 0.02, true);


        if (approxCurve2f.total() == 4) {
            if (0.80 <= aspectratio && aspectratio <= 1.20) {
                figure="cuadrado"; //cuadrado
                approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
                approxCurves.add(approxCurve);
                Imgproc.putText(mat_img, "Cuadrado", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar (0, 255, 0, 0), 0); //from version 3.0 .putText() (with same parameters) moved from Core to Imgproc class.

            }else{
                figure = "rectángulo";
                approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
                approxCurves.add(approxCurve);
                Imgproc.putText(mat_img, "Rectangulo", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0, 0), 0);
            }
        }
        if (approxCurve2f.total() == 3) {
            figure="triángulo";
            approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
            approxCurves.add(approxCurve);
            Imgproc.putText(mat_img, "Triangulo", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0,0), 0);
        }

        drawContours(mat_img, approxCurves);
        return figure;
    }


    /**
    Método que clasifica las figuras geométricas en cuadrados, rectángulos y círculos y me devuelve
    la imagen original con las figuras contorneadas y labeladas.
    **/

    static String colorDetection (Mat mat_img){

        Mat img = mat_img.clone();
        Mat submat = img.submat(rect); //rectángulo con únicamente la figura que nos interesa
        Mat submat_hsv = new Mat();
        cvtColor(submat, submat_hsv,Imgproc.COLOR_RGB2HSV); //pasamos de RGB a HSV
        double[] pixelColor;
        int cols = submat_hsv.cols();
        int rows = submat_hsv.rows();
        int x = cols/2;
        int y = rows/2;
        pixelColor = submat_hsv.get(x,y); //pixel del centro del rectángulo

        /*Con esto se imprimen los valores RGB del píxel. Temporal*/
        Hue = String.valueOf(pixelColor[0]);


        return obtenerColorHSV(pixelColor[0]);
        //return obtenerColorHSV(pixelColor);
    }

    static String obtenerColorHSV (double pixel) {


        //TESTING: http://www.color-blindness.com/color-name-hue/ (remember: hue en OpenCV [0, 180]
        String color ="";
        double verde_down = 35; //incluido
        double verde_up = 83; //incluido
        double azul_down = 84; //incluido
        double azul_up = 125; //incluido
        double morado_down = 126; //incluido
        double morado_up = 152; //incluido



        if (pixel>=verde_down && pixel<verde_up){
            color="verde";
        } else if (pixel>=morado_down && pixel<=morado_up){
            color="morado";
        }else if (pixel>=azul_down && pixel<=azul_up){
            color="azul";
        }

        return color;
    }



    public void okClicked(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

}
