# `TFlite` with `Android error Handling`

## 1st Error
> Unexpected failure when preparing tensor allocations: tensorflow/lite/kernels/conv.cc:224

`원인`: 
- `Interpreter.run()`와 `Interpreter.runForMultipleInputsOutputs()`할 때 input의 형식이 다르다. 

`해결`:
- `run()`일 경우에는 `private final float[][][][] input = new float[BATCH_SIZE][IMG_HEIGHT][IMG_WIDTH][NUM_CHANNEL];`형식을 그대로 사용해도 된다.

- `runForMultipleInputsOutputs()`일경우에는 `Object[] inputs = new Object[]{input};`를 해주어 input란에 넣어주어야 한다.

## 2nd Error
> float[][] cannot be cast to float[]

`원인`: 
- `out[i] = ((float[])output_map.get(i))[0];`

`해결`:
- `out[i] = ((float[][])output_map.get(i))[0][0];`

## 3rd Error

> java.lang.NullPointerException: Attempt to write to null array

`원인`:
- 배열을 생성할 때, 초기화를 하지 않아 생기는 에러

`해결`:
- `float[] out = null` -> `float[] out = new float[5];`