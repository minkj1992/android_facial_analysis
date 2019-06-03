package spartons.com.imagecropper;


import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.tensorflow.lite.Interpreter;


public class Classifier {
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



    public Classifier(Activity activity) throws IOException {
        mInterpreter = new Interpreter(loadModelFile(activity), options);

        output_map.put(0, athlete);
        output_map.put(1, celebrity);
        output_map.put(2, ceo);
        output_map.put(3, crime);
        output_map.put(4, professor);

    }

    public Result classify(Bitmap bitmap) {
        rzimage = bitmap;
//        convertBitmapToByteBuffer(bitmap);
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
        return new Result(out);
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
    public void close() {
        mInterpreter.close();
        mInterpreter = null;
    }
}
