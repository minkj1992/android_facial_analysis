# Using TensorFlow Lite on Android

- `Inception` and `MobileNets`에는 호환 잘 된다.

## 사용법

1) `import org.tensorflow.lite.Interpreter;`
2) Interpreter instance를 생성 한다.
```java
protected Interpreter tflite;
tflite = new Interpreter(loadModelFile(activity));
```

3) `MappedByteBuffer`에 load
```java
    private static final String MODEL_NAME = "mnist.tflite";
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_NAME);
        // fileDescriptor.getFileDescriptor()?
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        // ?
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
```

4) `tflite.run(imgData, labelProbArray);`
```java
private final Interpreter mInterpreter;
private final ByteBuffer mImageData;
private final int[] mImagePixels = new int[IMG_HEIGHT * IMG_WIDTH];
private final float[][] mResult = new float[1][NUM_CLASSES];

mInterpreter.run(mImageData, mResult);
```

    
## `ByteBuffer`
- 이미지 처리시 ByteBuffer는 필수적

### 메소드 정리

- `버퍼를 생성하는 3가지 방법`
    - `allocate()` : JVM의 힙 영역에 바이트 버퍼를 생성한다. 인수는 생성할 바이트 버퍼 크기다. capacity 속성값이다. 
    - `allocateDirect()` : JVM의 힙 영역이 아닌 운영체제의 커널 영역에 버퍼를 생성한다. 앞서 말한 allocate와 동일하다.
    - `wrap()` : 입력된 바이트 배열을 사용하여 버퍼를 생성한다. 입력에 사용된 바이트 배열이 변경되면 wrap를 사용해서 생성한 바이트 버퍼도 변경된다.

    - `ByteBuffer.order(ByteOrder.nativeOrder());` : 빅엔디언/리틀엔디언와 같이 byte 배열 세팅

