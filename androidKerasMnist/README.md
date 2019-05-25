# Keras for Android(MNIST Tutorial)
[참고자료1](https://github.com/miquelbeltran/deep-learning/tree/master/android-mlkit-sample),[참고자료2](https://keraskorea.github.io/posts/2018-10-23-Keras%EC%99%80%20MLKit%EC%9D%84%20%ED%99%9C%EC%9A%A9%ED%95%9C%20%EC%86%94%20%EA%B8%80%EC%94%A8%20%EC%88%AB%EC%9E%90%20%EC%9D%B8%EC%8B%9D%ED%95%98%EA%B8%B0(feat.Android)/)

- 블로그에서는 `Ml-kit`사용했다고 하는데, 코틀린 파일 사용하지 않으면 `ML-KIT`사용 안하고, asset에서 lite 컴파일 해서 시작하는 것 같다. 그런게 아니라면 `implementation 'org.tensorflow:tensorflow-lite:+'`를 사용해서 `Interpreter`를 사용해주는 것인데, wifi 끄고 해도 작동이 되는 것을 보면, 저장한 파일에서 해석해서 값을 가져오는 것 같다.

