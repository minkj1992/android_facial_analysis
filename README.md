# 딥러닝 관상 모델링

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
  

# 안드로이드
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
    

