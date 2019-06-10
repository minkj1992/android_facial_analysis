package spartons.com.imagecropper;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Friend {
    //@COVERTHIS
    private int avatar;
    private String nickname;
    private int background;
    private List<String> interests = new ArrayList<>();
//    private float[] result = new float[5];

    public Friend(int avatar, String nickname, int background, String... interest) {
        //@COVERTHIS
        this.avatar = avatar;
        this.nickname = nickname;
        this.background = background;
        interests.addAll(Arrays.asList(interest));
//        this.result = result;
    }

    public int getAvatar() {
        return avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public int getBackground() {
        return background;
    }

//    public float[] getResult() {return result;}

    public List<String> getInterests() {
        return interests;
    }


}