package fu.alfie.com.cameraviewexdemo;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraSurfaceViewActivity extends AppCompatActivity {
    private FrameLayout preview;
    private Camera mCamera;
    private CameraView cameraView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_surfaceview);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRecordingHint(false);
        mCamera.setParameters(parameters);
        preview = (FrameLayout) findViewById(R.id.preview1);
        imageView = (ImageView) findViewById(R.id.imageView1);
        cameraView = new CameraView(this, mCamera);
        preview.addView(cameraView);

        Button captureButton = (Button)findViewById(R.id.button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, pictureCallback);
            }
        });

        mCamera.startPreview();
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

    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if(bitmap==null){
                Toast.makeText(CameraSurfaceViewActivity.this, "Captured image is empty", Toast.LENGTH_LONG).show();
                return;
            }
            Matrix mtx = new Matrix();
            mtx.postRotate(90);
            imageView.setImageBitmap(scaleDownBitmapImage(bitmap, 600, 400 ));
        }
    };

    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        Matrix mtx = new Matrix();
        mtx.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0, newWidth/2, newHeight, mtx, true);
        return rotatedBitmap;
    }


}
