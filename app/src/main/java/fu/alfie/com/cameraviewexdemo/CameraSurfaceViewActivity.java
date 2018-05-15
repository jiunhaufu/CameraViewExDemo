package fu.alfie.com.cameraviewexdemo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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

    public void onTakePictureClick(View view) {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Intent intent = new Intent(CameraSurfaceViewActivity.this, ResultActivity.class);
                Bundle bundle = new Bundle();
                bundle.putByteArray("PictureRawData",data);
                bundle.putInt("Width",cameraView.getMeasuredWidth());
                bundle.putInt("Height",cameraView.getMeasuredHeight());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

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
