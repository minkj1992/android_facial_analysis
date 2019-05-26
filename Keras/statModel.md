# 인물얼굴 5가지 스탯 분류 모델링
## 모델링
- 

## 문제점
- `mutually exclusive`하지 않은데, 데이터를 구하기 어려워 `exclusive`하다고 가정한 뒤 학습을 해야한다. 
- 더욱 큰 문제점은 `predict()`시점에서 한 인물에 대해 5가지 score를 제공해주어야 하는데, mutually exclusive하게 학습을 하여 제대로 보여질지 모르겠다.
- `OvA`(`One vs All`)모델링 해야하는데 keras에서는 제공하지 않는함수인듯 하다.
(`sklearn.multiclass.OneVsRestClassifier(estimator, n_jobs=None)[source]`
)
- `FaceNet`,`VGG19`,`vggFace`를 사용하여 모델링 하였는데, `tensorflow-lite`에서 호환되지 않는 `layer`를 포함한다.
- `Activation Map(CAM)`을 android에서 보여주는 방법 관련된 소스코드가 전무하다.


    generator = datagen.flow(x,y,batch_size=BATCH_SIZE,steps_per_epoch=len(x_train) / BATCH_SIZE,epochs=EPOCHSshuffle=True,)



## debug error
- `fit_generator`와 multiple outputs error
> ValueError: Error when checking model target: the list of Numpy arrays that you are passing to your model is not the size the model expected. Expected to see 5 array(s), but instead got the following list of 1 arrays: [array([[[0.],
        [0.],
        [0.],
        [0.],
        [1.]]], dtype=float32)]...

- `solution`
    - [해결책1](https://stackoverflow.com/questions/38972380/keras-how-to-use-fit-generator-with-multiple-outputs-of-different-type)
    - [해결책2](https://stackoverflow.com/questions/47585698/keras-using-a-generator-for-multi-output-model-with-model-fit-generator)

- `Imagedatagenerator.flow_from_directory`의 y_label을 resahpe 해주지 못해서(tuple) -> `flow()`함수 사용했으며, y_label 값 손수 생성해 주었다. 

- 다만 `flow()`는 `target_size`를 줄 수 없어서 손수 디렉토리에서 이미지를 불러와, `read` 후 `resize` 실시하였다.
```python
def load_img(dir):
    x_list = []
    y_list = []
    for file in sorted(os.listdir(dir)):
        x_list.append(image.load_img(os.path.join(dir,file)))
        tmp = [[0],[0],[0],[0],[0]]
        tmp[LABEL[file.split('_')[0]]] = [1]
        y_list.append(tmp)
    return x_list,y_list

x_train,y_train = load_img(train_dir)

def modify_xshape(data):
    new_shape = (IMG_WIDTH,IMG_HEIGHT,3)
    data_new = np.empty(shape=(len(data),)+new_shape)
    for idx in range(len(data)):
        data_new[idx] = scipy.misc.imresize(data[idx], new_shape)
    return data_new

x_train = modify_xshape(x_train)
```