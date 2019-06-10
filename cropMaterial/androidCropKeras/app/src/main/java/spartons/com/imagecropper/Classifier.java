package spartons.com.imagecropper;


import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


import org.tensorflow.lite.Interpreter;


public class Classifier extends AppCompatActivity {
    private static final String LOG_TAG = "minkj1992";
    private static final String MODEL_NAME = "inception_v3.tflite";
    private static final int BATCH_SIZE = 1;
    public static final int IMG_HEIGHT = 299;
    public static final int IMG_WIDTH = 299;
    private static final int NUM_CHANNEL = 3;

    private final Interpreter.Options options = new Interpreter.Options();
    private Interpreter mInterpreter;
    private Bitmap rzimage;
    private final float[][][][] input = new float[BATCH_SIZE][IMG_HEIGHT][IMG_WIDTH][NUM_CHANNEL];

    private Map<Integer, Object> output_map = new TreeMap<>();
    private final float[][] crime = new float[1][1];
    private final float[][] ceo = new float[1][1];
    private final float[][] celebrity = new float[1][1];
    private final float[][] professor = new float[1][1];
    private final float[][] athlete = new float[1][1];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent(); /*데이터 수신*/
        init();
        output_map.put(0, athlete);
        output_map.put(1, celebrity);
        output_map.put(2, ceo);
        output_map.put(3, crime);
        output_map.put(4, professor);
        float[] result =  classify(intent.getParcelableExtra("gray"));
        // 3. 연산한 결과 값을 resultIntent 에 담아서 MainActivity 로 전달하고 현재 Activity 는 종료.
        Intent resultIntent = new Intent();
        resultIntent.putExtra("result",result);
        setResult(RESULT_OK,resultIntent);
        if (mInterpreter !=null) {
            mInterpreter.close();
            mInterpreter = null;
        }

        finish();
//        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void init() {
        try {
            mInterpreter = new Interpreter(loadModelFile(this), options);
        } catch (IOException e) {
            Toast.makeText(this, R.string.failed_to_create_classifier, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "init(): Failed to create Classifier", e);
        }

    }

    public float[] classify(Bitmap bitmap) {
        rzimage = bitmap;
        convertBitmapTo3channel(rzimage);

        Object[] inputs = new Object[]{input};
        mInterpreter.runForMultipleInputsOutputs(inputs, output_map);
        float[] out = new float[5];

        for (int i=0; i<5;i++) {
            float tmp = ((float[][])output_map.get(i))[0][0];
            boolean flag = true;
            while (flag) {
                if (tmp < 0.0999) {
                    tmp *= 10;
                } else {
                    flag = false;
                }
            }
            out[i] = tmp;
        }
        Log.v("minkj1992", "classify(): result = " + Arrays.toString(out));
        return out;
    }
    private void convertBitmapTo3channel(Bitmap rzimage) {
        for (int x = 0; x < 299; x++) {
            for (int y = 0; y < 299; y++) {
                int pixel = rzimage.getPixel(x, y);
                // Normalize channel values to [0.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [-1.0, 1.0] instead.

                float b = ((pixel)       & 0xFF);
                float g = ((pixel >>  8) & 0xFF);
                float r = ((pixel >> 16) & 0xFF);
                input[BATCH_SIZE-1][x][y][0] = (r - 127) / 128.0f;
                input[BATCH_SIZE-1][x][y][1] = (g - 127) / 128.0f;
                input[BATCH_SIZE-1][x][y][2] = (b - 127) / 128.0f;
            }
        }
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /** Closes tflite to release resources. */

    // https://stackoverflow.com/questions/4423671/why-does-super-ondestroy-in-java-android-goes-on-top-in-destructors
    // super.onDestroy()를 제일 마지막에.
    @Override
    public void onDestroy() {
        close();
        super.onDestroy();
    }

    public void close() {
        if (mInterpreter !=null) {
            mInterpreter.close();
            mInterpreter = null;
        }
    }
}


//
//    float[] out = mClassifier.classify(gray);
////                renderResult(result);

//
////@tmp
////resize
////                Bitmap rzBitmap = getResizedBitmap(bitmap,299,299);
////                Result result = mClassifier.classify(rzBitmap);
////                renderResult(result);