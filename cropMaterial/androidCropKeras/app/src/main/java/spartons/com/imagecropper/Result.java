package spartons.com.imagecropper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.razerdp.widget.animatedpieview.AnimatedPieView;
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.razerdp.widget.animatedpieview.callback.OnPieSelectListener;
import com.razerdp.widget.animatedpieview.data.IPieInfo;
import com.razerdp.widget.animatedpieview.data.PieOption;
import com.razerdp.widget.animatedpieview.data.SimplePieInfo;

import org.w3c.dom.Text;

@SuppressLint("ValidFragment")
public class Result extends Fragment{
    private float[] mProbability;
    Bitmap image;
    AnimatedPieView mAnimatedPieView;
    ImageView mImage;

    public Result(float[] out, Bitmap bitmap) {
        mProbability = out;
        image = bitmap;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v("minkj1992", "Result onCreateView 불려짐");
        View view = inflater.inflate(R.layout.result_fragment, container, false);
        mImage = view.findViewById(R.id.imageView2);
        if (image != null) {
            mImage.setImageBitmap(image);
        }

        mAnimatedPieView = view.findViewById(R.id.animatedPieChart);

        drawPie(getContext());

        return view;
    }

    public void drawPie(Context context) {
        Log.v("minkj1992", "Result DrawPie 불려짐");
        AnimatedPieViewConfig config = new AnimatedPieViewConfig();
        config.startAngle(-90)// Starting angle offset
                .addData(new SimplePieInfo(mProbability[0], Color.parseColor("#46665b"), "athlete"))//Data (bean that implements the IPieInfo interface)
                .addData(new SimplePieInfo(mProbability[1], Color.parseColor("#ffa66a"), "celebrity"))
                .addData(new SimplePieInfo(mProbability[2], Color.parseColor("#f25b5e"), "ceo"))//Data (bean that implements the IPieInfo interface)
                .addData(new SimplePieInfo(mProbability[3], Color.parseColor("#833d43"), "crime"))//Data (bean that implements the IPieInfo interface)
                .addData(new SimplePieInfo(mProbability[4], Color.parseColor("#442130"), "professor"))//Data (bean that implements the IPieInfo interface)
                .duration(5000);// draw pie animation duration
        config.selectListener(new OnPieSelectListener<IPieInfo>() {
            @Override
            public void onSelectPie(@NonNull IPieInfo pieInfo, boolean isFloatUp) {
                String doubleString = String.valueOf(pieInfo.getValue()*100.0);
                int indexOfDecimal = doubleString.indexOf(".");
                Toast.makeText(context, doubleString.substring(0, indexOfDecimal)+"점", Toast.LENGTH_SHORT).show();
//                Toast.makeText(context,String.format("%.2f%", pieInfo.getValue()*100.0), Toast.LENGTH_SHORT).show();
            }
        });
        config.floatShadowRadius(18f);
        config.floatUpDuration(500);
        config.drawText(true);// Text description size
        config.textSize(40);
        config.textGravity(AnimatedPieViewConfig.ECTOPIC);
//        config.textMargin(8);// Margin between text and guide line
//        config.pieRadius(100);// Set chart radius
//        config.pieRadiusRatio(0.8f);// Chart's radius ratio for parent ViewGroup
//        config.guidePointRadius(2);// Chart's radius
//        config.guideLineWidth(4);// Text guide line stroke width
//        config.guideLineMarginStart(8);// Guide point margin from chart
        config.canTouch(true);// Whether to allow the pie click to enlarge
        config.interpolator(new DecelerateInterpolator(4f));
        config.focusAlphaType(AnimatedPieViewConfig.FOCUS_WITH_ALPHA_REV);
        config.focusAlpha(350);

        // The following two sentences can be replace directly 'mAnimatedPieView.start (config); '
        mAnimatedPieView.applyConfig(config);
        mAnimatedPieView.start();
        Log.v("minkj1992", "Result DrawPie thread Start 불려짐");

    }

}
