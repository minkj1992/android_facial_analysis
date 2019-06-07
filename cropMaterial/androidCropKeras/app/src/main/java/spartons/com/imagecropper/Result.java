package spartons.com.imagecropper;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.razerdp.widget.animatedpieview.AnimatedPieView;
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.razerdp.widget.animatedpieview.data.IPieInfo;
import com.razerdp.widget.animatedpieview.data.PieOption;
import com.razerdp.widget.animatedpieview.data.SimplePieInfo;

@SuppressLint("ValidFragment")
public class Result extends Fragment{
    private float[] mProbability;
    AnimatedPieView mAnimatedPieView;

    public Result(float[] out) {
        mProbability = out;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.result_fragment, container, false);
        mAnimatedPieView = view.findViewById(R.id.animatedPieChart);
        drawPie();
        return view;
    }

    public void drawPie() {

        AnimatedPieViewConfig config = new AnimatedPieViewConfig();
        config.startAngle(-90)// Starting angle offset
                .addData(new SimplePieInfo(mProbability[0], Color.parseColor("#77dd77"), "athlete"))//Data (bean that implements the IPieInfo interface)
                .addData(new SimplePieInfo(mProbability[1], Color.parseColor("#ff6961"), "celebrity"))
                .addData(new SimplePieInfo(mProbability[2], Color.parseColor("#77dd77"), "ceo"))//Data (bean that implements the IPieInfo interface)
                .addData(new SimplePieInfo(mProbability[3], Color.parseColor("#ff6961"), "crime"))//Data (bean that implements the IPieInfo interface)
                .addData(new SimplePieInfo(mProbability[4], Color.parseColor("#77dd77"), "professor"))//Data (bean that implements the IPieInfo interface)
                .duration(3000);// draw pie animation duration
        config.floatShadowRadius(18f);
        config.floatUpDuration(500);
        config.interpolator(new DecelerateInterpolator(4f));

        // The following two sentences can be replace directly 'mAnimatedPieView.start (config); '
        mAnimatedPieView.applyConfig(config);
        mAnimatedPieView.start();
    }

}
