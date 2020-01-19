package com.zpf.floaticondemo.floatpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public abstract class BaseFloatPage {
    private static final String TAG = "BaseFloatPage";

    private View mRootView;
    private WindowManager.LayoutParams mLayoutParams;
    private Handler mHandler;

    private InnerReceiver mInnerReceiver = new InnerReceiver();

    private String mTag;

    protected Bundle mBundle;

    public void performCreate(Context context, @NonNull PageIntent pageIntent) {
        mBundle = pageIntent.bundle;
        mHandler = new Handler(Looper.myLooper());
        onCreate(context);
        mRootView = new FrameLayout(context){
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if(event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_HOME){
                        return onBackPressed();
                    }
                }
                return super.dispatchKeyEvent(event);
            }
        };
        View view = onCreateView(context, (ViewGroup) mRootView);
        ((ViewGroup) mRootView).addView(view);
        onViewCreated(mRootView,pageIntent);
        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.format = PixelFormat.TRANSPARENT;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        onLayoutParamsCreated(mLayoutParams);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(mInnerReceiver, intentFilter);
    }

    public void performDestroy(){
        getContext().unregisterReceiver(mInnerReceiver);
        mHandler = null;
        mRootView = null;
        onDestroy();
    }

    public Context getContext() {
        if (mRootView != null) {
            return mRootView.getContext();
        } else {
            return null;
        }
    }

    public Resources getReSources(){
        if (getContext() == null) {
            return null;
        }
        return  getContext().getResources();
    }

    public String getString(@StringRes int resId) {
        if (getContext() == null) {
            return null;
        }
        return getContext().getString(resId);
    }

    protected abstract void onCreate(Context context); //初始化回调
    protected abstract View onCreateView(Context context, ViewGroup view);
    protected abstract void onViewCreated(View view, PageIntent pageIntent);
    protected abstract void onLayoutParamsCreated(WindowManager.LayoutParams params);

    protected abstract void onDestroy();

    protected abstract void onEnterBackground();  //隐藏回调
    protected abstract void onEnterForeground();  //显示回调
    protected abstract void onHomeKeyPress();      //Home键点击回调
    protected abstract void onRecentAppKeyPress();
    protected abstract boolean onBackPressed();        //返回键回调
    protected abstract void afterBundleDataSet();


    protected void controlAnim(boolean operation) {

    }

    public void setBundle(Bundle mBundle) {
        this.mBundle = mBundle;
    }

    public Bundle getBundle() {
        return mBundle;
    }

    public void setTag(String mTag) {
        this.mTag = mTag;
    }

    public String getTag() {
        return mTag;
    }

    public boolean isShow(){
        return mRootView.isShown();
    }

    protected <T extends View> T findViewById(@IdRes int id) {
        return mRootView.findViewById(id);
    }

    public View getRootView() {
        return mRootView;
    }

    public WindowManager.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }

    public int getWidth(){
        if (mRootView != null && mRootView.getWidth() != 0) {
            return mRootView.getWidth();
        }else {
            return 195;
        }

    }

    public void post(Runnable r) {
        mHandler.post(r);
    }

    public void postDelayed(Runnable r, long delayMillis) {
        mHandler.postDelayed(r, delayMillis);
    }

    public void runAfterRenderFinish(final Runnable runnable) {
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                if (runnable != null) {
                    runnable.run();
                }
                Looper.myQueue().removeIdleHandler(this);
                return false;
            }
        });
    }

    private class InnerReceiver extends BroadcastReceiver {

        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        onHomeKeyPress();
                    }else if(reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)){
                        onRecentAppKeyPress();
                    }
                }
            }
        }
    }
}
