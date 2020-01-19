# FloatIconDemo
一个全局显示的悬浮窗实现，参考[滴滴DoKit]("https://github.com/didi/DoraemonKit")功能入口的悬浮窗代码,加入一些自己的实现。

RoundImageView：圆形ImageView 来自 [RoundImageView]("https://github.com/vinc3m1/RoundedImageView")

RoundProgressBar:展示播放进度的progressBar，来自[hongyangAndroid/Android-ProgressBarWidthNumber](https://github.com/hongyangAndroid/Android-ProgressBarWidthNumber)

### 代码结构：

BaseFloatPage：悬浮窗基类，整个过程的描述，定义一些虚方法

FloatIconConfig: 使用SharedPreference保存每次移动后悬浮窗的位置

FloatPageManager: 用来管理悬浮窗的添加，显示，隐藏

PageIntent: 专属于悬浮窗的传递数据intent

TouchProxy: 处理悬浮窗的拖动

PermissionUtil: 悬浮窗权限申请

FloatIconPage： 实现悬浮窗的子类，类似唱片机旋转的效果。

## BaseFLoatPage
### 构造流程performCreate：
onCreate -> onCreateView -> onViewCreated() -> onLayoutParamsCreated()

对应的几个虚方法
```java
    protected abstract void onCreate(Context context); //初始化回调
    protected abstract View onCreateView(Context context, ViewGroup view);
    protected abstract void onViewCreated(View view, PageIntent pageIntent); 
    protected abstract void onLayoutParamsCreated(WindowManager.LayoutParams params);
```

首先接收PageIntent中的Bundle数据，接着onCreate过程，new 一个 FrameLayout作为根布局，重写dispathKeyEvent方法，添加Back键回调，onBackPressed(),把onCreateView返回的布局添加到rootView中，然后就是onViewCreated调用，new一个悬浮窗的LayoutParams，指定格式与位置，在onLayoutParamsCreated实现中具体设置，最后就是注册一个Receiver：InnerReceiver用来接收 Intent.ACTION_CLOSE_SYSTEM_DIALOGS广播，监听Home按键，与RecentApp按键。

看一下四个虚方法在FloatIconPage中的实现：

### onCreate

保存上下文，添加横竖屏切换的广播接收器（用于横竖屏切换时获取屏幕宽度设置悬浮窗位置），获取mWindowManager

```java
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
```

### onCreateView   

初始化用于添加于帧布局中的view
```java
    @Override
    protected View onCreateView(Context context, ViewGroup view) {
        return LayoutInflater.from(context).inflate(R.layout.float_video_icon, view, false);
    }
```

### onViewCreated
    在这个实现中完成一些initView的操作，控件的绑定，动画，添加点击监听以及滑动监听onTouch

```java
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
```

### onLayoutParamsCreated
    设置浮窗宽高为WRAP_CONTENT，位置y使用上次一保存的位置，默认为屏幕的0.45位置，findProperX()方法中设置浮窗的贴边，小于屏幕一半放左边，大于放右边，之前的旋屏广播用来更新此时计算用的screenWidth
```java
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
            params.y = (int)(mContext.getResources().getDisplayMetrics().heightPixels * 0.45f;
        }
        Log.d(TAG, "onLayoutParamsCreated: " + params.x);
        stylusVisible(params.x);
    }
```

### Destroy流程performCreate：
manager移除监听，unregisterReceiver
```java
        FloatPageManager.getInstance().removeListener(this);
        mContext.unregisterReceiver(orientationChangeReceiver);
```

此外还有几个回调，onEnterBackground是浮窗被隐藏实现，用于隐藏视图，关闭一些需要关闭的东西，onEnterBackground浮窗展示实现，展示视图。三个按键回调，以及浮窗Bundle数据更新回调，用于更新UI

```java
    protected abstract void onEnterBackground();  //隐藏回调
    protected abstract void onEnterBackground();  //进入前台回调
  
    protected abstract void onHomeKeyPress();      //Home键点击回调
    protected abstract void onRecentAppKeyPress(); //最近APP列表键     
    protected abstract boolean onBackPressed();    //返回键回调

    protected abstract void afterBundleDataSet();  //浮窗数据更新回调
```
## FloatPageManager 的操作
notifyBackground() 与 notifyForeground()控制全部浮窗显示隐藏，也可以根据单个浮窗的tag单独控制

### add悬浮窗操作
检查class是否为空以及是否存在是单例浮窗的这个实例，浮窗newInstance，使用PageIntent中的数据设置page，设置浮窗tag也就是名字，执行PerformCreate()，最后在windowManager中添加这个浮窗
```java
    public void add(@NonNull PageIntent pageIntent) {
        if (pageIntent.targetClass == null) {
            return;
        }
        if (pageIntent.mode == PageIntent.MODE_SINGLE_INSTANCE) {
            for (BaseFloatPage page : mPages) {
                if(pageIntent.targetClass.isInstance(page)){
                    return;
                }
            }
        }
        BaseFloatPage page = pageIntent.targetClass.newInstance();
        page.setBundle(pageIntent.bundle);
        page.setTag(pageIntent.tag);
        mPages.add(page);
        page.performCreate(mContext,pageIntent);
        mWindowManager.addView(page.getRootView(), page.getLayoutParams());
        for(FloatPageManagerListener listener : mListeners){
            listener.onPageAdd(page);
        }
    }
```

### remove操作

从windowManager中移除，Destroy操作，最后把它从浮窗列表中移除

```java
    public void remove(BaseFloatPage page) {
        mWindowManager.removeView(page.getRootView());
        page.performDestroy();
        mPages.remove(page);
    }
```


