# Error 1
> 현재 브랜치 master 브랜치가 'origin/master'보다 5개 커밋만큼 앞에 있습니다.

> remote error: this exceeds GitHub's file size limit of 100.00 MB


- `해결방법` : commit 파일들 중에서 용량 초과 파일들을 지워준다.
`git filter-branch -f --index-filter 'git rm --cached --ignore-unmatch cropMaterial/androidCropKeras/app/src/main/assets/inception_v3.tflite'`

`git filter-branch -f --index-filter 'git rm --cached --ignore-unmatch Keras/inception_v3_face.h5'`

# Error 2

> git cannot rewrite branches your index contains uncommitted changes

- uncommit 상태인 변경사항들이 있어서 branch filter를 사용할 수 없는 경우.
- 그냥 git commit 해준뒤, 필요한 명령어 진행해주면 된다.
