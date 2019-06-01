package spartons.com.imagecropper;


import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.tensorflow.lite.Interpreter;

public class Classifier {
    private static final String LOG_TAG = Classifier.class.getSimpleName();
    private static final String MODEL_NAME = "inception_v3.tflite";


    private static final int BATCH_SIZE = 1;
    public static final int IMG_HEIGHT = 299;
    public static final int IMG_WIDTH = 299;
    private static final int NUM_CHANNEL = 3;
    private static final int NUM_CLASSES = 5;

    private final Interpreter.Options options = new Interpreter.Options();
    //    private final Interpreter mInterpreter;
    private Interpreter mInterpreter;
    private Bitmap rzimage;
//    private final ByteBuffer mImageData;

    private final float[][][][] input = new float[BATCH_SIZE][IMG_HEIGHT][IMG_WIDTH][NUM_CHANNEL];
    private final float[][] output = new float[1][1];
//    private final int[] mImagePixels = new int[IMG_HEIGHT * IMG_WIDTH];



    public Classifier(Activity activity) throws IOException {
        mInterpreter = new Interpreter(loadModelFile(activity), options);

//        // 픽셀 1개당 1바이트가 아닌 4바이트를 사용하기 때문에, *4를 해주어야 셈이 맞다.
//        mImageData = ByteBuffer.allocateDirect(
//                4 * BATCH_SIZE * IMG_HEIGHT * IMG_WIDTH * NUM_CHANNEL);
//        mImageData.order(ByteOrder.nativeOrder());
    }

    public Result classify(Bitmap bitmap) {
        rzimage = bitmap;
//        convertBitmapToByteBuffer(bitmap);
        convertBitmapTo3channel(rzimage);

//        mInterpreter.run(mImageData, mResult);
        mInterpreter.run(input, output);
        Log.v(LOG_TAG, "classify(): result = " + Arrays.toString(output));
        return new Result(output[0]);
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
        // 닫아 줘야 하는 것 아닌가?
//        inputStream.close();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

//    private void convertBitmapToByteBuffer(Bitmap bitmap) {
//        if (mImageData == null) {
//            return;
//        }
//        //rewind() :포지션은 맨 처음으로 초기화합니다. (포지션이 0이 아닐 때 다시 0으로 위치시킵니다.)
//        mImageData.rewind();
//
//        bitmap.getPixels(mImagePixels, 0, bitmap.getWidth(), 0, 0,
//                bitmap.getWidth(), bitmap.getHeight());
//        //채널이 3개일텐데 이렇게 for 2번만 돌아도 될까?
//        // 맨처음 loop에 3을 넣고, gray scale 되었다고 치고 확인을 해볼까.
//        int pixel = 0;
//        for (int i = 0; i < IMG_WIDTH; ++i) {
//            for (int j = 0; j < IMG_HEIGHT; ++j) {
//                int value = mImagePixels[pixel++];
//                mImageData.putFloat(convertPixel(value));
//            }
//        }
//    }
//
//    private static float convertPixel(int color) {
//        return (255 - (((color >> 16) & 0xFF) * 0.299f
//                + ((color >> 8) & 0xFF) * 0.587f
//                + (color & 0xFF) * 0.114f)) / 255.0f;
//    }

    /** Closes tflite to release resources. */
    public void close() {
        mInterpreter.close();
        mInterpreter = null;
    }
}
