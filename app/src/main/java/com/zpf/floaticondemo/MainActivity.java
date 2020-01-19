package com.zpf.floaticondemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zpf.floaticondemo.floatpage.BaseFloatPage;
import com.zpf.floaticondemo.floatpage.FloatPageManager;
import com.zpf.floaticondemo.floatpage.PageIntent;
import com.zpf.floaticondemo.util.PermissionUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int MESSAGE_SHOW_PROGRESS = 1;

    boolean floatIconVisible;

    private MediaData mMediaData = new MediaData();

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_SHOW_PROGRESS) {
                int progress = mMediaData.getProgress();
                mMediaData.setProgress(progress >= 100 ? 0 : progress + 1);
                Bundle bundle = new Bundle();
                bundle.putParcelable(FloatIconPage.AUDIO_DATA_KEY, mMediaData);
                FloatPageManager.getInstance().setFloatPageBundle(FloatIconPage.FLOAT_ICON_PAGE_TAG, bundle);
                sendEmptyMessageDelayed(MESSAGE_SHOW_PROGRESS, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatPageManager.getInstance().init(MainActivity.this.getApplicationContext());
        Button floatIconBtn = findViewById(R.id.float_icon_btn);
        if (FloatPageManager.getInstance().getFloatPage(FloatIconPage.FLOAT_ICON_PAGE_TAG) != null) {
            floatIconBtn.setText("关闭浮窗");
            floatIconVisible = true;
        }else {
            floatIconBtn.setText("打开浮窗");
        }
        floatIconBtn.setOnClickListener(this);
    }

    private void openIcon() {
        if (PermissionUtil.canDrawOverlays(this)) {
            //添加float图标
            PageIntent pageIntent = new PageIntent(FloatIconPage.class);
            pageIntent.bundle = new Bundle();

            pageIntent.bundle.putParcelable(FloatIconPage.AUDIO_DATA_KEY, mMediaData);
            pageIntent.mode = PageIntent.MODE_SINGLE_INSTANCE;
            pageIntent.tag = FloatIconPage.FLOAT_ICON_PAGE_TAG;

            FloatPageManager.getInstance().add(pageIntent);

            BaseFloatPage baseFloatPage = FloatPageManager.getInstance().getFloatPage(FloatIconPage.FLOAT_ICON_PAGE_TAG);
            if (baseFloatPage instanceof FloatIconPage) {
                FloatIconPage floatIconPage = (FloatIconPage) baseFloatPage;
                floatIconPage.setOnClickListener(new FloatIconPage.onClickListener() {
                    @Override
                    public void onClick() {
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                    }
                });
            }

            //显示float 图标
            FloatPageManager.getInstance().notifyForeground();

        }
    }

    private void closeIcon() {
        FloatPageManager.getInstance().notifyBackground();
        FloatPageManager.getInstance().remove(FloatIconPage.FLOAT_ICON_PAGE_TAG);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.float_icon_btn:
                if (!floatIconVisible) {
                    if (!PermissionUtil.canDrawOverlays(MainActivity.this)) {
                        PermissionUtil.requestDrawOverlays(MainActivity.this);
                    } else {
                        openIcon();
                        floatIconVisible = true;
                        ((Button) v).setText("关闭浮窗");
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
                    }
                } else {
                    closeIcon();
                    floatIconVisible = false;
                    ((Button) v).setText("打开浮窗");
                    mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
                }

                break;
            default:
                break;
        }
    }
}
