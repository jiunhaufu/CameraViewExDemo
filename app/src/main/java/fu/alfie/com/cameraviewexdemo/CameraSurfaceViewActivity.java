package fu.alfie.com.cameraviewexdemo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.HashMap;
import java.util.Map;

public class CameraSurfaceViewActivity extends AppCompatActivity {

    public static final int PERMISSION_CAMERA = 0;

    private FrameLayout preview;
    private Camera mCamera;
    private CameraView cameraView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreen(true,true);//設定全螢幕
        setContentView(R.layout.activity_camera_surfaceview);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//設定直向拍照

        findViews();
        askPermissions();
    }

    public void fullScreen(Boolean title, Boolean actionbar){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove action bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar() != null && actionbar) getSupportActionBar().hide();
        //Remove notification bar
        if (title) getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void findViews() {
        preview = (FrameLayout) findViewById(R.id.preview1);
        imageView = (ImageView) findViewById(R.id.imageView1);
    }

    public void askPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {

            SharedPreferences sharedPreferences = getSharedPreferences("permission" , MODE_PRIVATE);

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //第一次詢問後按下取消 會再問一次
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
                sharedPreferences.edit().putBoolean("FirstAsk" , false).apply();
            }else{
                //第一次詢問要求權限
                if (sharedPreferences.getBoolean("FirstAsk",true)){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
                }else{
                    //按下取消而且不再詢問是否授權
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    Toast.makeText(this,"開啟權限設定並同意授權",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }else{
            agreePermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //按下同意授權
                    super.onRequestPermissionsResult( requestCode, permissions, grantResults ) ;
                    agreePermissions();
                } else {
                    //按下拒絕授權
                    super.onRequestPermissionsResult( requestCode, permissions, grantResults ) ;
                    onBackPressed();
                }
                break;
        }
    }

    public void agreePermissions(){
        openCamera();
    }

    private void openCamera() {
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRecordingHint(true);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
        cameraView = new CameraView(this, mCamera);
        preview.addView(cameraView);
    }

    public void onTakePictureClick(View view) {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if(bitmap==null){
                    Toast.makeText(CameraSurfaceViewActivity.this, "Captured image is empty", Toast.LENGTH_LONG).show();
                    return;
                }
                createImage(bitmap);
                mCamera.startPreview();
                //拍照將無法再執行自動對焦
            }
        });

    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void createImage(Bitmap bitmap) {
        imageView.setImageBitmap(scaleDownBitmapImage(bitmap, cameraView.getMeasuredHeight()/2, cameraView.getMeasuredWidth()/2 ));
    }

    private String decode(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource luminanceSource = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));

        try {
            final Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            Result result = new QRCodeReader().decode(binaryBitmap, hints);
            drawResultPoints(bitmap, 1, result);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            String result_null = "解碼失敗";
            return result_null;
        }
    }

    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newHeight, int newWidth){
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newHeight, newWidth, true);
        Matrix mtx = new Matrix();
        mtx.postRotate(90);
        //截圖視窗
        Bitmap rotatedBitmap = Bitmap.createBitmap(resizedBitmap, (newWidth-16)/10*7/2-8, 16, (newWidth-16)/10*7+8, newWidth-16-16, mtx, true);
        //原始比例 1/2視窗
//        Bitmap rotatedBitmap = Bitmap.createBitmap(resedBitmap, 0, 0, newHeight, newWidth, mtx, true);
        //分析QRcode測試
        Toast.makeText(CameraSurfaceViewActivity.this, decode(rotatedBitmap), Toast.LENGTH_LONG).show();
        return rotatedBitmap;
    }

    private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
            } else if (points.length == 4 &&
                    (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                            rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and metadata
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
                drawLine(canvas, paint, points[2], points[3], scaleFactor);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    if (point != null) {
                        canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
                    }
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
        if (a != null && b != null) {
            canvas.drawLine(scaleFactor * a.getX(),
                    scaleFactor * a.getY(),
                    scaleFactor * b.getX(),
                    scaleFactor * b.getY(),
                    paint);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
        }
    }
}
