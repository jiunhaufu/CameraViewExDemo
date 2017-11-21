package fu.alfie.com.cameraviewexdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onCameraSurfaceViewClick(View view){
        startActivity(new Intent(this,CameraSurfaceViewActivity.class));
    }

    public void onCamera2SurfaceViewClick(View view){
        startActivity(new Intent(this,Camera2SurfaceViewActivity.class));
    }
}
