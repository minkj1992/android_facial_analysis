> 2019 아주대 모바일프로그래밍 얼굴 분석기 최종 발표를 위해서, 코딩했던 시행착오들과 소스코드들을 모아놓은 repo입니다.

> 프로젝트를 확인하고 싶으시다면 [/cropMaterial/androidCropKeras](https://github.com/minkj1992/android_facial_analysis/tree/master/cropMaterial/androidCropKeras)에서 확인해주세요.

- `프로젝트 주제`: 얼굴 이미지를 5가지값으로 관상분석 딥러닝하여 `근력`,`지력`,`재력`,`카리스마`,`범죄력`을 보여주고, 다른 사람들의 분석결과를 공유할 수 있는 `Android App`(`JAVA`).

- `키워드 for deeplearning :`
    - `Keras`,`Multiple outputs and multiple losses`, `tflite converter`, `grayscale image with 3channel`, `inception-v3`, `OVA`, `multiple binary classification`, `multiple sigmoid activation`

- `키워드 for android : `
    - `tflite.interpreter`, `glide for bitmap`, `bitmap resizing`, `bitmap grayscale`, `cropping`, `fragment`, `framelayout`, `fragment switching with nav bar material`, `firestore` ,`firebaseAuth`,`firestorage`,`CircularFillableLoaders`, `splashscreen`, `LocalBroadcastManager`, `intent Update`  ...




------------------
# `전반적인 폴더 설명 `

- `/Camera2`
    - 카메라 2를 활용한 사진 찍기 및 저장
- `/CameraX` 
    - 안드로이드 cameraX(`androidX ver`)사용, `ucrop`과의 호환성 문제로 부착 실패
`/ItcMyFirebaseAuth`
    - [튜토리얼 참고자료](https://iteritory.com/android-firebase-authentication-tutorial-using-firebase-ui/)
    - `firebaseUI` + [`Mikepenz's MaterialDrawer`](https://github.com/mikepenz/MaterialDrawer)(material design이 적용된 android nav bar)
- `/Keras`
    - android에 사용될 얼굴분석 keras 모델과, 이를 `tflite`화 시킬 코드 및 tflite파일(실제 tflite화는 pip 라이브러리 충돌로 인하여 google colab에서 적용하였다.)
- `/androidKerasMnist`
    - `tflite`를 활용해서 안드로이드에서 Mnist를 분석할 수 있는 튜토리얼
    - `Mnist` 모델 + `touchscreenDrawer` + `resultView`
    - 화면에서 터치스크린에 숫자를 작성해서 submit을 누르면, 분석 결과를 `resultView`에서 보여준다.
    - 이때 분석에 걸린 시간과, 정확도 수치가 나타난다.

- `/onlyImageCrop.zip`
    - 안드로이드 imageCropper인 [`UCrop`](https://github.com/Yalantis/uCrop)을 활용하여 사진찍기 및 crop하는 app
    - only `Ucrop`

- `/cropMaterial/AndroidImageCrop`
    - `UCrop` + `mikepenz/Drawer` 시킨 파일
    - `dependency` 문제 해결 완료
    - `sidebar(nav)`와 `imageView(for pick picture)`를 결합시킨 app

- `/cropMaterial/androidCropKeras`
    - 메인 프로젝트로 최종 결과물 모두 존재
    - `tflite` + `AndroidImageCrop` + `materialDesign` + `fragment & framelayout` +    

- `/materialDesign`
    - 갤러리 형식으로, image를 flip 시켜서 보여주는 material app 
    - [참고자료](https://github.com/Yalantis/FlipViewPager.Draco)

- `/giterror.md`
    - git을 사용하면서 맞이한, error들 모아놓은 자료

-------------------


# `딥러닝 모델링(관상)`
[세부사항 정리 문서](https://github.com/minkj1992/android_facial_analysis/tree/master/Keras)

## 모델링 생성 문제점
학습에 사용할 y는 [1,0,0,0,0]이런식으로 y 라벨이 1개만 true인 값이다.
하지만 이후 predict에서는 5가지 카테고리에 대하여 sigmoid 값을 뽑아내는 녀석인데 
이를 해결하기 위해서 binary classification 모델 5개를 생성하고 이에 대한 sigmoid값을 빼내고자 하였다.
하지만 일반인 얼굴을 어디서 구해야 하지? 즉 부자가 아닌 얼굴을 뭐로 설정해주어야 할까? (나머지 라벨 녀석들로? 아니면 일반인 데이터를 구해서?)

- 1. 나머지 라벨 녀석들로 25%씩 빼서 학습을 진행한다.
- 2. 또는일반인 데이터를 구해서 모든 5가지 모델에 넣어준다.
- 3. 또는 일반인 데이터를 구해서 random 하게 몇백장 뽑아서 라벨 0으로 하고 학습시작.

## 모델링 계획

- pre_trained model을 가져온다
  - [keras-vggface lib](https://github.com/rcmalli/keras-vggface)
  - [pre_trained model 방법 keras](https://nbviewer.jupyter.org/github/rickiepark/deep-learning-with-python-notebooks/blob/master/5.3-using-a-pretrained-convnet.ipynb)
  
  - [faceNEt](https://github.com/davidsandberg/facenet)
  - [vggFace사용한 블로그 예시](https://sefiks.com/2018/08/06/deep-face-recognition-with-keras/)
  - [keras_facenet lib](https://github.com/nyoki-mtl/keras-facenet)
  - [vgg with keras1](https://github.com/mzaradzki/neuralnets/tree/master/vgg_faces_keras)
  - [vgg with keras2](https://gist.github.com/EncodeTS/6bbe8cb8bebad7a672f0d872561782d9)
  

  - faceNet은 `finetune`이 어려워, `vggFace2`를 사용하기로 결정
- fine tune with asian celebrity
  - 아시아 셀럽들을 대상으로 h5를 fine tune(미세조정)해준다. 왜냐하면 우리가 만들 모델 유저는 대체로 한국인일 것이기 때문에.
  - [데이터 링크](http://trillionpairs.deepglint.com/data)
  - 원래 목적인 5가지 카테고리에 대하여 fine tune해주면 좋겠지만, 데이터가 부족하여 많은 데이터를 가지고 있는 celeb들을 대상으로하여 미세조정 실시.
- 5가지 모델을 만들고 이전에 fine tune된 h5를 import 해준다.
  - 범죄력, 근력, 지력, 카리스마, 매력을 sigmoid 결과값 뱉어내는 5가지 모델을 생성.
  - 이때 한가지 모델에 5가지 카테고리 sigmoid 하지 않는 이유는 통합했을 경우, 5가지 값 중 한가지에만 결과값이 치우쳐 생성되었기 때문
  - 이렇게 할 경우 근력의 80점과 범죄력의 80점이 동등한 값이라고 말할 수 없다는 단점을 가짐. 다만 고른 결과 수치값을 가질 수 있다.(절대적 비교 불가)
  

# `안드로이드`
- `firebaseUI` 로그인 페이지 생성
- `Camera`
    - Preview 제공
        - 실시간 사진 preview
        - 찍은 뒤, 사진 cropper로 바로 연동
    - 갤러리에서 파일 선택하기
        - 사진 cropper로 바로 이동
    - Cropper
        - 자동 vs 수동 기능 제공
        - 자동: 얼굴 부분 crop해준다.
        - 수동: 수동으로 얼굴 부분 crop 한다. (주의사항 지시: "귀,턱,앞이마, 머리카라 최대한 제외")
- `Preprocessing`
    - 사진 사이즈 변환
    - grayscale 후, 3layer로 변환
    - 5개의 keras 모델에 전달
- `Analysis`
    - 각 5개의 모델에 AsyncTask 하게 사진을 전달해주고, 학습 개시한다. (IntentService?)
    - 분석이 완료되면 알림 설정으로 알려준다.

- `ResultView`
    - 총 6개의 view로 main결과를 삼국지 스탯창으로 보여준다.
    - 각 5개의 view에 대하여 
    
- `material design`
    - 디자인을 바꾸어준다.


## 진행 사항

### Cropper
- `uCrop`를 사용하여 size 조절 부터 rotation 까지 모두 마무리 한다.
- `./cropMaterial`: `FirebaseUI`+ `Material` + `uCrop`
    - 호환 문제 해결
        - `android {compileSdkVersion 26}`
        - `implementation 'com.android.support:appcompat-v7:26.1.0'`
        - 
        ```java
            implementation("com.mikepenz:materialdrawer:6.0.7@aar") {
            transitive = true
        }
        ```
    - `login`기능 추가
    - `Material` 사용하여 sideBar(Navigation added)

### `tflite`
- keras 모델 생성한것 tflite로 변환 후 android에 적용시키기 완료
- float 5개 outputs 완료

### `firestore` : `fireauth` = `1`:`1`
- firebase auth와 fire storage 연동시키기 (1:1 one to one 관계로 db 구축하기)
- `firestore.java`추가함.
    - `createdb()`
    - `alterdb()`
- 저장되는 것 까지 완료(UID가 `document`의 id가 되도록 설정)

### `앞으로 할일`

- 갤러리 형식으로 보여주고 filter 적용하기


