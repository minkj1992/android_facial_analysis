package com.example.cameraxapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.pm.PackageManager;
import android.os.Bundle;

import android.Manifest;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.graphics.Matrix;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Handler;
import android.os.HandlerThread;

//private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private TextureView viewFinder;
    private ImageButton captureBtn;
    public String mCurrentPhotoPath;

    private void startCamera() {
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(new Rational(1, 1))
                .setTargetResolution(new Size(640, 640))
                .build();
        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
//                        output.getSurfaceTexture();
                        updateTransform();
                    }
                });
        //미리보기 설정 끝

        //사진찍기 설정 시작
        ImageCaptureConfig imageCaptureConfig =
                new ImageCaptureConfig.Builder()
                        .setTargetAspectRatio(new Rational(1, 1))
                        .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                        .build();
        final ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);
        captureBtn = findViewById(R.id.capture_button);
        captureBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("Capture", String.valueOf(ex));
                }

                imageCapture.takePicture(photoFile,
                        new ImageCapture.OnImageSavedListener() {
                            @Override
                            public void onImageSaved(File file) {
                                Toast.makeText(getApplicationContext(),"사진이[".concat(mCurrentPhotoPath).concat("]에 저장되었습니다."),Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onError(
                                    ImageCapture.UseCaseError error,
                                    String message,
                                    Throwable exc) {
                                Toast.makeText(getApplicationContext(),"사진저장실패:".concat(message),Toast.LENGTH_SHORT).show();
                                Log.e("CameraXApp", message);
                            }
                        });

            }
        });
        // 사진찍기 끝
        // 이미지 프로세싱 설정 시작
//        HandlerThread analyzerThread = new HandlerThread("LuminosityAnalysis");
//        analyzerThread.start();
//        ImageAnalysisConfig analyzerConfig =
//                new ImageAnalysisConfig.Builder()
//                        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
//                        .setCallbackHandler(new Handler(analyzerThread.getLooper()))
//                        .build();
//
//
//        ImageAnalysis analyzerUseCase = new ImageAnalysis(analyzerConfig).setAnalyzer(LuminosityAnalyzer());
        // 이미지 프로세싱 설정 끝

//        CameraX.bindToLifecycle((LifecycleOwner) this, preview, imageCapture, analyzerUseCase);
        CameraX.bindToLifecycle((LifecycleOwner) this, preview, imageCapture);
    }
    private void updateTransform(){
        Matrix matrix = new Matrix();
        float centerX = viewFinder.getWidth()/2f;
        float centerY = viewFinder.getHeight()/2f;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: rotation = 0;break;
            case Surface.ROTATION_90: rotation = 90;break;
            case Surface.ROTATION_180: rotation = 180;break;
            case Surface.ROTATION_270: rotation = 270;break;

        }
        matrix.postRotate((float)-rotation,centerX,centerY);
        viewFinder.setTransform(matrix);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewFinder = findViewById(R.id.view_finder);
        if (allPermissionsGranted()) {
            viewFinder.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        }else{
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        viewFinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                updateTransform();
            }
        });
    }

    private boolean allPermissionsGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted())
            viewFinder.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        }else{
            Toast.makeText(this,
                    "권한이 허용되지 않았습니다.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }




//    private boolean allPermissionsGranted(){
//        for (permission in REQUIRED_PERMISSIONS) {
//            if (ContextCompat.checkSelfPermission(
//                    this, permission) != PackageManager.PERMISSION_GRANTED) {
//                return false
//            }
//        }
//        return true
//    }


//    private class LuminosityAnalyzer implements ImageAnalysis.Analyzer {
//        private long lastAnalyzedTimestamp = 0L;
//        /**
//         * 이미지 버퍼를 바이트 배열로 추출하기 위한 익스텐션
//         */
//        private fun ByteBuffer.toByteArray(): ByteArray {
//            rewind()    // 버퍼의 포지션을 0으로 되돌림
//            val data = ByteArray(remaining())
//            get(data)   // 바이트 버퍼를 바이트 배열로 복사함
//            return data // 바이트 배열 반환함
//        }
//
//        @Override
//        public void analyze(ImageProxy image, int rotationDegrees) {
//
//        }
//    }

    //파일 생성 함수
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix  */
                ".jpg",         /* suffix  */
                storageDir      /* directory  */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
