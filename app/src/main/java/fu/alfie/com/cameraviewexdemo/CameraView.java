package fu.alfie.com.cameraviewexdemo;

import android.content.Context;
import android.hardware.Camera;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Jiunhau.Fu on 2017/10/20.
 */

public class CameraView extends SurfaceView {

    private Camera mCamera;
    private SurfaceHolder mHolder;

    public CameraView(Context context, @Nullable Camera camera) {
        super(context);
        this.mCamera = camera;
        this.mHolder = getHolder();
        this.mHolder.addCallback(new MyCallback());
    }

    private class MyCallback implements SurfaceHolder.Callback {

        public void surfaceCreated(SurfaceHolder surface) {
            if (mHolder == null) {
                mHolder = surface;
            }
            if (mCamera != null) {
                try {
                    mCamera.setPreviewDisplay(surface);
                } catch (IOException e) {
                    // Something bad happened
                }
            }
        }

        public void surfaceChanged(SurfaceHolder surface, int format, int width, int height) {
            // Ignored, Camera does all the work for us
        }

        public void surfaceDestroyed(SurfaceHolder surface) {
        }
    }
}
