# 인물얼굴 5가지 스탯 분류 모델링
> multilabel output model with ImageDataGenerator()

- `flow_from_directory()`는 `[[0],[0],[0],[0],[1]]`이런식으로 못만들어서 flow를 사용하였지만, 알고보니 그럴필요가 없이 [0,0,0,0,1] 이렇게 작성해주면 되었다. 결론적으로 `flow_from_directory()`사용 가능


## 모델링
- 학습시, mutually exclusive하다고 가정하고 학습을 실시한다. (one-hot-encoding)
- {x: 이미지(PIL), y: one-hot-encoding value[0,0,0,0,1]}
- 모델링 진행과정
    - inception-V3 output값 2048 layer에 hidden을 Dense를 5개 만들어주고, 이를 각각 input으로 받는 athlete,celcb,ceo,crime,professor layer를 만들어준다.
    - 그런 뒤 outputs=[athlete,celebrity,ceo,crime,professor]를 5개 적용시키고 이에대하여 각자 binary_crossentropy 시켜준다.
    - 이후 inception layer는 trainable = false 적용시켜준다.

```python
MODEL_CLASSES = 5
x = base_model.output
x = GlobalAveragePooling2D(name='avg_pool')(x)
# x = Dropout(0.4)(x)
x = Dense(2048,activation='relu')(x)
for i in range(MODEL_CLASSES):
    hidden = Dense(2048,activation='relu')(x)
athlete = Dense(1, activation='sigmoid',name='athlete')(hidden)
celebrity = Dense(1, activation='sigmoid',name='celebrity')(hidden)
ceo = Dense(1, activation='sigmoid',name='ceo')(hidden)
crime = Dense(1, activation='sigmoid',name='crime')(hidden)
professor = Dense(1, activation='sigmoid',name='professor')(hidden)
model = Model(inputs=base_model.input, outputs=[athlete,celebrity,ceo,crime,professor])
adad = Adadelta()
for layer in base_model.layers:
    layer.trainable = False
model.compile(optimizer=adad, loss=['binary_crossentropy','binary_crossentropy','binary_crossentropy','binary_crossentropy','binary_crossentropy',],metrics=['accuracy'])
```


## 문제점
- `mutually exclusive`하지 않은데, 데이터를 구하기 어려워 `exclusive`하다고 가정한 뒤 학습을 해야한다. 
- 더욱 큰 문제점은 `predict()`시점에서 한 인물에 대해 5가지 score를 제공해주어야 하는데, mutually exclusive하게 학습을 하여 제대로 보여질지 모르겠다.
- `OvA`(`One vs All`)모델링 해야하는데 keras에서는 제공하지 않는함수인듯 하다.
(`sklearn.multiclass.OneVsRestClassifier(estimator, n_jobs=None)[source]`
)
- `FaceNet`,`VGG19`,`vggFace`를 사용하여 모델링 하였는데, `tensorflow-lite`에서 호환되지 않는 `layer`를 포함한다.
- `Activation Map(CAM)`을 android에서 보여주는 방법 관련된 소스코드가 전무하다.


    generator = datagen.flow(x,y,batch_size=BATCH_SIZE,steps_per_epoch=len(x_train) / BATCH_SIZE,epochs=EPOCHS,shuffle=True,)

- 모델 5개를 편의를 위하여 한 모델에 multiple하게 적용해주다보니, mutually exclusive하다고 가정한 부분이 결과값에 영향을 미치는 것 같다. 즉 어떤 class의 정확도가 높아지면 이에 대하여 다른 class들의 정확도들이 떨어지는 현상이 발생하는 듯하다. 이럴거면 모델 5개를 각자 만들어서 결과값 내는 것이 훨씬 더 신뢰할만한 결과를 만들 것 같지만 안드로이드 영역에서 5개의 모델을 돌려주는 것이 힘들것 같아서 그냥 합친 모델을 사용해야겠다.


- validation loss가 log scale인데 1 보다 큰 경우
> Keras binary_crossentropy first convert your predicted probability to logits. Then it uses tf.nn.sigmoid_cross_entropy_with_logits to calculate cross entropy and return to you the mean of that. Mathematically speaking, if your label is 1 and your predicted probability is low (like 0.1), the cross entropy can be greater than 1, like losses.binary_crossentropy(tf.constant([1.]), tf.constant([0.1])).[원본](https://stackoverflow.com/questions/49882424/keras-tensorflow-binary-cross-entropy-loss-greater-than-1)

- 5개중 몇몇 validation acc가 변하지 않는 경우: epoch에 따라서 batch때문인가? 아닌거 같은데..loss는 변하는데 acc가 변하지 않는다.

- default validation_acc가 0.8000으로 매우 크게 잡히고, 학습에서 진행이 안되거나 error가 떴을 때, 0.8000을 유지한다.

## debug error
- `fit_generator`와 multiple outputs error
> ValueError: Error when checking model target: the list of Numpy arrays that you are passing to your model is not the size the model expected. Expected to see 5 array(s), but instead got the following list of 1 arrays: `[array([[[0.],
        [0.],
        [0.],
        [0.],
        [1.]]], dtype=float32)]...`

- `solution`
    - [해결책1](https://stackoverflow.com/questions/38972380/keras-how-to-use-fit-generator-with-multiple-outputs-of-different-type)
    - [해결책2](https://stackoverflow.com/questions/47585698/keras-using-a-generator-for-multi-output-model-with-model-fit-generator)
    - [삽질 후 채택한 해결책 v](https://medium.com/@vijayabhaskar96/multi-label-image-classification-tutorial-with-keras-imagedatagenerator-cd541f8eaf24)
```python
def generate_data_generator(generator, x, y,batch_size=BATCH_SIZE):
    data = generator.flow(x,y,batch_size=batch_size)
    for batch_x,batch_y in data:
        yield (batch_x,[batch_y[:,i] for i in range(5)])

history = model.fit_generator(generator=generate_data_generator(train_datagen,x_train,y_train),
                    steps_per_epoch=len(x_train) / BATCH_SIZE,
                    validation_data=generate_data_generator(test_datagen,x_val,y_val),
                    validation_steps=len(x_val) / BATCH_SIZE,
                    epochs=EPOCHS)
MODEL_FILE = 'inception_v3_face.h5'

model.save(MODEL_FILE)
```

- `Imagedatagenerator.flow_from_directory`의 y_label을 resahpe 해주지 못해서(tuple) -> `flow()`함수 사용했으며, y_label 값 손수 생성해 주었다. 

- 다만 `flow()`는 `target_size`를 줄 수 없어서 손수 디렉토리에서 이미지를 불러와, `read` 후 `resize` 실시하였다.
```python
def load_img(dir):
    x_list = []
    y_list = []
    for file in sorted(os.listdir(dir)):
        x_list.append(image.load_img(os.path.join(dir,file)))
        # tmp = [[0],[0],[0],[0],[0]]
        # tmp[LABEL[file.split('_')[0]]] = [1]
        tmp = [0,0,0,0,0]
        tmp[LABEL[file.split('_')[0]]] = 1
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