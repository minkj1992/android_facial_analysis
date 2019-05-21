from django import forms
from django.contrib.auth import get_user_model

User = get_user_model()

class SignupForm(forms.Form):
    username = forms.CharField(
        widget=forms.TextInput(
            attrs={
                'class': 'form-control',
            }
        )
    )
    
    email = forms.EmailField(max_length=200, help_text='Required')(
        widget=forms.EmailInput(
            attrs={
                'class': 'form-control',
                'required': 'True',
            }
        )
    )
    
    password1 = forms.CharField(
        widget=forms.PasswordInput(
            attrs={
                'class': 'form-control',
            }
        )
    )
    # 비밀번호 확인을 위한 필드
    password2 = forms.CharField(
        widget=forms.PasswordInput(
            attrs={
                'class': 'form-control',
            }
        )
    )
    class Meta:
        model = User
        fields = ('username', 'email', 'password1', 'password2')
    



# username필드의 검증에 username이 이미 사용중인지 여부 검사
    def clean_username(self):
        username = self.cleaned_data['username']
        if User.objects.filter(username=username).exists():
            raise forms.ValidationError('아이디가 이미 사용중입니다')
        return username


    # password1와 password2의 값이 일치하는지 유효성 검사
    def clean_password2(self):
        password1 = self.cleaned_data['password1']
        password2 = self.cleaned_data['password2']
        if password1 != password2:
            raise forms.ValidationError('비밀번호와 비밀번호 확인란의 값이 일치하지 않습니다')
        return password2

    # 자신이 가진 username과 password를 사용해서 유저 생성 후 반환하는 메서드
    def signup(self):
        if self.is_valid():
            return User.objects.create_user(
                username=self.cleaned_data['username'],
                password=self.cleaned_data['password2']
            )
        





# views.py

def signup(request):
    if request.method == 'POST':
        form = SignupForm(request.POST)
        # 유효성 검증에 통과한 경우 (username의 중복과 password1, 2의 일치 여부)
        if form.is_valid():
            user = form.save(commit=False)
            user_is_active = False # 이메일 확인 없이는 안되도록 하기 위해서
            user.save()
            current_site = get_current_site(request) 
            # localhost:8000
            message = render_to_string('acc_activate_email.html', {
                'user': user,
                'domain': current_site.domain,
                'uid': 	urlsafe_base64_encode(force_bytes(user.pk)).decode(),
                'token': account_activation_token.make_token(user),
            })
            mail_subject = "[SOT] 회원가입 인증 메일입니다."
            user_email = user.username
            email = EmailMessage(mail_subject, message, to=[user_email])
            email.send()
            return HttpResponse(
                '<div style="font-size: 40px; width: 100%; height:100%; display:flex; text-align:center; '
                'justify-content: center; align-items: center;">'
                '입력하신 이메일<span>로 인증 링크가 전송되었습니다.</span>'
                '</div>'
            )
            # SignupForm의 인스턴스 메서드인 signup() 실행, 유저 생성
            form.signup()
            return redirect('post:post_list')
    else:
        form = SignupForm()

    return render(request, 'signup.html', {'form':form})
    
    
    # acc_active_email.html 에서 인증 메일 받고 'activate' 누르면 처리하고 다시 원래 홈화면으로 돌아감 
    def activate(request, uid64, token):
    
        uid = force_text(urlsafe_base64_decode(uid64))
        user = User.objects.get(pk=uid)

        if user is not None and account_activation_token.check_token(user, token):
            user.is_active = True
            user.save()
            return redirect('home')
        else:
            return HttpResponse('비정상적인 접근입니다.')
