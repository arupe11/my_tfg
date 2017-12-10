package com.example.andrea.myapp;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.rectangle;


public class Camera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    public static RotatedRect rotrect;
    public static Rect rect;
    public static String Hue = "";
    public static List<MatOfPoint> approxCurves;
    public static String tipo_figura;
    public static int contador_triangulo, contador_cuadrado, contador_rectangulo, contador_circulo=0;

    private Mat mRgba; //frame (se va actualizando todoel tiempo)
    public static Mat frame2;
    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    public static final String NIVEL = "nivel";


    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("opencv", "inicializacion fallida");
        } else
            Log.i("opencv", "inicializacion correcta");
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public Camera() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.OpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);//llama a OnCameraFrame
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);



       /* if (isTriangle()!=true){
            imageView.setVisibility(View.INVISIBLE);
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {

        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Mat mRgba_copy = mRgba.clone();

        //Dibujar cuadrado ROI
        int square_side = 300;
        int x = (mRgba.cols()/2)-square_side/2;
        int y = (mRgba.rows()/2)-square_side;
        Rect roi = new Rect (x,y,square_side, square_side);
        Imgproc.rectangle(mRgba, new Point(roi.x, roi.y), new Point(roi.x + roi.width, roi.y + roi.height), new Scalar(0, 255, 0, 255), 5);

        //Recortamos la imagen
        Mat matROI = new Mat(mRgba_copy, roi);

        //Encontrar los contornos de la imagen recortada
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        contours = findContours(matROI, contours);


        //Detectamos la figura
        tipo_figura = figureClasification(matROI, contours);

        //Detectamos el color
        // String color = colorDetection(photo_original);


        switch (tipo_figura){
            case "TRIANGULO":
                contador_triangulo++;
                break;
            case "CUADRADO":
                contador_cuadrado++;
                break;
            case "CIRCULO":
                contador_circulo++;
                break;
            case "RECTANGULO":
                contador_rectangulo++;
                break;
            default:
                break;
        }

        if(contador_triangulo>=30) {
            Imgproc.putText(mRgba, "TRIANGULO", new Point(100, 500), 3, 1, new Scalar(255, 0, 0, 255), 2);
            if (contador_triangulo >= 40) { //ya lo ha detectado
                resetCounters();
                esperar(1);
            }
        }


        if(contador_cuadrado>=30) {
            Imgproc.putText(mRgba, "CUADRADO", new Point(100, 500), 3, 1, new Scalar(255, 0, 0, 255), 2);
            if (contador_cuadrado >= 40) { //ya lo ha detectado
                resetCounters();
                esperar(1);
                }
        }

        if(contador_rectangulo>=30) {
            Imgproc.putText(mRgba, "RECTANGULO", new Point(100, 500), 3, 1, new Scalar(255, 0, 0, 255), 2);
            if (contador_rectangulo >= 40) { //ya lo ha detectado
                resetCounters();
                esperar(1);
            }
        }

        if(contador_circulo>=30) {
            Imgproc.putText(mRgba, "CIRCULO", new Point(100, 500), 3, 1, new Scalar(255, 0, 0, 255), 2);
            if (contador_circulo >= 40) { //ya lo ha detectado
                resetCounters();
                esperar(1);
            }
        }

        Imgproc.drawContours(mRgba, contours, -1, new Scalar(255, 0, 0, 255), 2);


        // RecognisedFrame(mRgba);

        return mRgba; //lo que se ve
    }

    static List<MatOfPoint> findContours(Mat mat_img, List<MatOfPoint> contours) {

        Mat canny = new Mat();
        Mat mat_gray = new Mat();
        Imgproc.cvtColor(mat_img, mat_gray, COLOR_BGR2GRAY);
        Imgproc.Canny(mat_gray, canny, 100, 100 * 3);
        Imgproc.findContours(canny, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;

    }

    static void drawContours(Mat mat_img, List<MatOfPoint> edgeContours) { //no queremos que nos devuelva nada sino que lo haga directamente

        Imgproc.drawContours(mat_img, edgeContours, -1, new Scalar(255, 0, 0, 255), 2); //-1 dibuja todos los contornos
    }


    static String figureClasification(Mat mat_img, List<MatOfPoint> contours) {

        String figure = "";

        //Recorremos cada contorno y calculamos su área, si es mayor que la calculada anteriormente,
        //esa es la nueva maxArea (nos quedamos con la mayor)
        double maxArea = 0;
        int maxAreaIdx = 0; //cada contorno tiene un índice
        MatOfPoint2f maxContour2f = new MatOfPoint2f();
        MatOfPoint2f approxCurve2f = new MatOfPoint2f();
        //MatOfPoint maxContour = new MatOfPoint();

        for (int idx = 0; idx < contours.size(); idx++) {
            double area = Imgproc.contourArea(contours.get(idx));

            if (area > maxArea) {
                maxArea = area;
                maxAreaIdx = idx;
            }

            /*¿Por qué no me deja poner estas 4 líneas fuera del for?*/

            contours.get(maxAreaIdx).convertTo(maxContour2f, CvType.CV_32FC2); //MatOfPoint to MatOfPoint2f
            Imgproc.approxPolyDP(maxContour2f, approxCurve2f, Imgproc.arcLength(maxContour2f, true) * 0.02, true);

            /*Creamos un rectángulo que englobe ese contorno (uno recto para luego hacer el submat y
            otro rotado para el aspect ratio*/
            rect = Imgproc.boundingRect(contours.get(maxAreaIdx));
            rotrect = Imgproc.minAreaRect(maxContour2f); //argumento: MatOfPoint2f*/


            //Calculamos los momentos para calcular el centro de masas
            Moments moments = Imgproc.moments(contours.get(maxAreaIdx));
            Point centroid = new Point();
            centroid.x = moments.get_m10() / moments.get_m00();
            centroid.y = moments.get_m01() / moments.get_m00();


        }




        /**CLASIFICACIÓN DEL CONTORNO**/

        if (approxCurve2f.total() == 4) {
            double aspectratio = rotrect.size.width / rotrect.size.height;
            if (0.80 <= aspectratio && aspectratio <= 1.20) {
                figure = "CUADRADO";
            } else {
                figure = "RECTANGULO";
            }
        }


        else if (approxCurve2f.total() == 3) {
            figure = "TRIANGULO";
        }

        else if (approxCurve2f.total() > 3) {
            figure = "CIRCULO";
        }


        return figure;


    }


    //    if (approxCurve2f.total() == 4) {
    //  if (0.80 <= aspectratio && aspectratio <= 1.20) {
    //          figure="cuadrado"; //cuadrado
    // approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
    // approxCurves.add(approxCurve);
    //    Imgproc.putText(mat_img, "Cuadrado", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar (0, 255, 0, 0), 0); //from version 3.0 .putText() (with same parameters) moved from Core to Imgproc class.

                /*else{
                    figure = "rectángulo";
                    approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
                    approxCurves.add(approxCurve);
                    //    Imgproc.putText(mat_img, "Rectangulo", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0, 0), 0);
                }
            }
            if (approxCurve2f.total() == 3) {
                figure="triángulo";
                approxCurve2f.convertTo(approxCurve, CvType.CV_32S);
                approxCurves.add(approxCurve);
                //  Imgproc.putText(mat_img, "Triangulo", centroid, Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0,0), 0);
            }*/
    //}

    //drawContours(mat_img, approxCurves);


    static boolean isTriangle () {

        String tr = "TRIANGULO";


        if (tipo_figura == tr) {
            return true;
        } else {
            return false;
        }
    }

    static boolean isSquare () {

        String cu = "CUADRADO";


        if (tipo_figura == cu ){
            return true;
        } else{
            return false;
        }
    }

    static boolean isRectangle () {

        String re = "RECTANGLE";


        if (tipo_figura == re ){
            return true;
        } else{
            return false;
        }
    }

    static boolean isCircle () {

        String ci = "CIRCULO";


        if (tipo_figura == ci ){
            return true;
        } else{
            return false;
        }
    }


    static void resetCounters () {

        contador_triangulo = 0;
        contador_cuadrado = 0;
        contador_rectangulo = 0;
        contador_circulo = 0;
    }

    public void esperar (int segundos) {
        try {
            Thread.sleep (segundos*1000);
        } catch (Exception e) {
// Mensaje en caso de que falle
        }
    }

/*
    public void RecognisedFrame(Mat frame){
        //Lo enviamos para reconocimiento
        long address = mRgba.getNativeObjAddr(); //para pasar Mat's se hace con el address
        Intent intent1 = new Intent(this, Reconocimiento.class);
        intent1.putExtra("frame", address);
        startActivityForResult(intent1,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


            // Recogemos el resultado de la segunda actividad.

            long address2 = data.getLongExtra("frame", 0);
            frame2 = new Mat(address2);




    }*/


}