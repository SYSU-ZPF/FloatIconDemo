package com.zpf.floaticondemo;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.zpf.floaticondemo.floatpage.BaseFloatPage;
import com.zpf.floaticondemo.floatpage.FloatIconConfig;
import com.zpf.floaticondemo.floatpage.FloatPageManager;
import com.zpf.floaticondemo.floatpage.PageIntent;
import com.zpf.floaticondemo.floatpage.TouchProxy;
import com.zpf.floaticondemo.progressBar.RoundProgressBarWidthNumber;

import static com.zpf.floaticondemo.util.Util.dp2px;


public class FloatIconPage extends BaseFloatPage implements TouchProxy.OnTouchEventListener, FloatPageManager.FloatPageManagerListener {

    public static final String FLOAT_ICON_PAGE_TAG = "audio_float_icon";
    public static final String AUDIO_DATA_KEY = "audioData";

    private static final String TAG = "FloatIconPage";
    protected WindowManager mWindowManager;

    private TouchProxy mTouchProxy = new TouchProxy(this);

    private Context mContext;
    private int mScreenWidth;
    private OrientationChangeReceiver orientationChangeReceiver;
    private ObjectAnimator mRotaAnimator;
    private ImageView stylusStartIv;
    private ImageView stylusEndIv;
    private RoundProgressBarWidthNumber mProgressBar;
    private RoundedImageView mRoundImageView;

    public boolean clickedPause = false;


    @Override
    protected void onEnterBackground() {
        getRootView().setVisibility(View.GONE);
        if (mRotaAnimator != null) {
            mRotaAnimator.cancel();
        }
    }

    @Override
    protected void onEnterForeground() {
        getRootView().setVisibility(View.VISIBLE);
        if (mRotaAnimator != null) {
            mRotaAnimator.start();
        }
    }

    @Override
    protected void onViewCreated(View view, PageIntent pageIntent) {

        mRoundImageView = getRootView().findViewById(R.id.iv_rote);
        stylusStartIv = getRootView().findViewById(R.id.stylus_start);
        stylusEndIv = getRootView().findViewById(R.id.stylus_end);
        mProgressBar = getRootView().findViewById(R.id.audio_progress_bar);

        mRoundImageView.setImageResource(R.drawable.cd_surface);
        mRotaAnimator = ObjectAnimator.ofFloat(mRoundImageView, "rotation", 0f, 360f);
        mRotaAnimator.setInterpolator(new LinearInterpolator());
        mRotaAnimator.setRepeatCount(-1);
        mRotaAnimator.setDuration(5000);
        mRotaAnimator.start();

        getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick();
                }
            }
        });
        getRootView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (getRootView() != null) {
                    return mTouchProxy.onTouchEvent(v, event);
                } else {
                    return false;
                }
            }
        });
    }

    public interface onClickListener{
        void onClick();
    }

    private onClickListener mListener;

    public void  setOnClickListener(onClickListener listener){
        mListener  = listener;
    }


    /**
     * 获取数据更新UI
     */
    @Override
    protected void afterBundleDataSet() {
        MediaData mediaData = mBundle.getParcelable(AUDIO_DATA_KEY);
        if (mediaData != null) {
            mProgressBar.setProgress(mediaData.getProgress());
        }
        mRoundImageView.setImageResource(R.drawable.cd_surface);
    }

    /**
     *
     * @param operation start or pause
     */
    @Override
    protected void controlAnim(boolean operation) {
        super.controlAnim(operation);
        if (operation) {
            mRotaAnimator.resume();
        }else {
            mRotaAnimator.pause();
        }
    }

    @Override
    protected void onHomeKeyPress() {

    }

    @Override
    protected void onRecentAppKeyPress() {

    }

    @Override
    protected void onCreate(Context context) {
        mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        orientationChangeReceiver = new OrientationChangeReceiver();
        mContext.registerReceiver(orientationChangeReceiver, intentFilter);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        FloatPageManager.getInstance().addListener(this);
    }

    @Override
    protected boolean onBackPressed() {
        return false;
    }

    @Override
    protected View onCreateView(Context context, ViewGroup view) {
        return LayoutInflater.from(context).inflate(R.layout.float_video_icon, view, false);
    }


    private void stylusVisible(int loc) {
        if (loc == 0) {
            stylusStartIv.setVisibility(View.VISIBLE);
            stylusEndIv.setVisibility(View.INVISIBLE);
        } else {
            stylusEndIv.setVisibility(View.VISIBLE);
            stylusStartIv.setVisibility(View.INVISIBLE);
        }
    }

    private int findProperX(int oldX, int width) {
        if (mScreenWidth <= 0) return 0;
        if (oldX <= mScreenWidth / 2) {
            return 0;
        } else {
            return mScreenWidth - width - dp2px(mContext,5);
        }
    }

    /**
     * 初始化浮窗的位置
     * @param params
     */
    @Override
    protected void onLayoutParamsCreated(WindowManager.LayoutParams params) {
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        int lastX = FloatIconConfig.getLastPosX(mContext);
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.x = findProperX(mScreenWidth, getWidth());
        params.y = FloatIconConfig.getLastPosY(mContext);
        if (params.y == 0) {
            params.y = (int)(mContext.getResources().getDisplayMetrics().heightPixels * 0.45f);
        }
        Log.d(TAG, "onLayoutParamsCreated: " + params.x);
        stylusVisible(params.x);
    }

    @Override
    protected void onDestroy() {
        FloatPageManager.getInstance().removeListener(this);
        mContext.unregisterReceiver(orientationChangeReceiver);
    }

    @Override
    public void onPageAdd(BaseFloatPage page) {
        if (page == this) {
            return;
        }
        FloatPageManager.getInstance().remove(this);
        PageIntent intent = new PageIntent(FloatIconPage.class);
        intent.mode = PageIntent.MODE_SINGLE_INSTANCE;
        FloatPageManager.getInstance().add(intent);
    }

    @Override
    public void onMove(int x, int y, int dx, int dy) {
        getLayoutParams().x += dx;
        getLayoutParams().y += dy;
        mWindowManager.updateViewLayout(getRootView(), getLayoutParams());
        Log.d(TAG, "onMove: " + x);
    }

    @Override
    public void onUp(int x, int y) {
        getLayoutParams().x = findProperX(x, getWidth());
        mWindowManager.updateViewLayout(getRootView(), getLayoutParams());
        FloatIconConfig.saveLastPosX(mContext,getLayoutParams().x);
        FloatIconConfig.saveLastPosY(mContext,getLayoutParams().y);
        stylusVisible(getLayoutParams().x);
    }

    @Override
    public void onDown(int x, int y) {
        stylusStartIv.setVisibility(View.INVISIBLE);
        stylusEndIv.setVisibility(View.INVISIBLE);
    }

    class OrientationChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if ("android.intent.action.CONFIGURATION_CHANGED".equals(intent.getAction())) {
                mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            }
        }
    }
}
