package fu.alfie.com.cameraviewexdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

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

public class ResultActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);

        Intent intent = getIntent();
        byte[] data = intent.getExtras().getByteArray("PictureRawData");
        int width = intent.getIntExtra("Width", 480);
        int height = intent.getIntExtra("Height", 640);

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bitmap != null){
            Bitmap bitmap_new = bitmapTransform(bitmap, width, height);
            imageView.setImageBitmap(bitmap_new);
            String qr_code_content = decode(bitmap_new, BarcodeFormat.QR_CODE);
            textView.setText(qr_code_content);
        }

    }

    private Bitmap bitmapTransform(Bitmap bitmap, int width, int height) {
        Bitmap bitmap_size = Bitmap.createScaledBitmap(bitmap, height/2, width/2, true);
        Matrix mtx = new Matrix();
        mtx.postRotate(90);
        Bitmap bitmap_crop_orientation = Bitmap.createBitmap(bitmap_size, (width/2-16)/10*7/2-8, 16, (width/2-16)/10*7+8, width/2-16-16, mtx, true);
        //Bitmap bitmap_orientation = Bitmap.createBitmap(bitmap_size, 0, 0, height/2, width/2, mtx, true);  //不裁切
        return bitmap_crop_orientation;
    }

    private String decode(Bitmap bitmap, BarcodeFormat barcodeFormat) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource luminanceSource = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));

        try {
            final Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            hints.put(DecodeHintType.POSSIBLE_FORMATS, barcodeFormat);
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

}
