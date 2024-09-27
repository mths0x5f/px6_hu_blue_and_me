package com.microntek.avin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.microntek.CarManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.microntek.gl.GLFrameSurface;

/* loaded from: classes.dex */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final String BOOTBCHECK = "com.microntek.bootcheck";
    private static final String MSG_MTC_CANBUS_DISPLAY = "com.microntek.canbusdisplay";
    private static final String MSG_RESETMUSICCLOCK = "com.microntek.musicclockreset";
    private static final String MSG_TYPE = "type";
    private static final String REMOVETASK = "com.microntek.removetask";
    private static final String STATECHANGETYPE = "type";
    private static final String STATECHANGE_STRING = "com.microntek.carstatechange";
    private static final String STATEVIDEO_SIGNAL = "com.microntek.videosignalchange";
    private static final String STATE_SAFE = "SAFE";
    private static final String UPDATETHEME = "updateTheme";
    private ImageView audioOnlyshow;
    private LinearLayout mAkmInfo;
    private View mAvin;
    private ImageView mBtAv1;
    private ImageView mBtAv2;
    private View mBtMusic;
    private View mFile;
    private View mMusic;
    private View mRadio;
    private LinearLayout mShortcut;
    private ImageView mSignalshow;
    private View mVideo;
    private TextView mWarnText;
    private SharedPreferences mySharedPreferences;
    private static String CHANNEL = "line";
    private static String ENTER_CHANNEL = "av_channel_enter=line";
    private static String EXIT_CHANNEL = "av_channel_exit=line";
    private static boolean mStatusBarFlag = true;
    private static boolean mVideoEnable = false;
    private final int MSG_HIDE_STATUS_BAR = 65284;
    private final int MSG_UPDATE_SIGNAL = 65285;
    private boolean captureOnFlag = false;
    private boolean surfaceShow = false;
    private GLFrameSurface mSurfaceView = null;
    private boolean mVideoSignal = true;
    private boolean mExit = false;
    private int mStartbackflag = 0;
    private boolean isFront = false;
    private CarManager mCarManager = null;
    private AudioManager mAudioManager = null;
    private boolean focusGainFlag = false;
    private int mMultiWindowModeW = -1;
    private boolean hasNaviBar = "true".equals(SystemProperties.get("persist.product.shownavbar"));
    private boolean mUiModeChange = false;
    private final ContentObserver mUpdateThemeObserver = new UpdateThemeObserver();
    private boolean removeTask = false;
    private String mCustomerName = "";
    private String mCustomerSub = "";
    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() { // from class: com.microntek.avin.MainActivity.1
        @Override // android.media.AudioManager.OnAudioFocusChangeListener
        public void onAudioFocusChange(int focusChange) {
            if (focusChange != -3) {
                if (focusChange == 1) {
                    MainActivity.this.setParameters("av_focus_gain=" + MainActivity.CHANNEL);
                    MainActivity.this.focusGainFlag = true;
                    return;
                }
                if (focusChange == -2) {
                    MainActivity.this.setParameters("av_focus_loss=" + MainActivity.CHANNEL);
                    MainActivity.this.focusGainFlag = false;
                    return;
                }
                if (focusChange == -1) {
                    MainActivity.this.setParameters("av_focus_loss=" + MainActivity.CHANNEL);
                    MainActivity.this.focusGainFlag = false;
                    MainActivity.this.finish();
                }
            }
        }
    };
    private Handler mHandler = new Handler() { // from class: com.microntek.avin.MainActivity.2
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 65284) {
                MainActivity.this.hideStatusBar();
            }
        }
    };
    private BroadcastReceiver AVINBootReceiver = new BroadcastReceiver() { // from class: com.microntek.avin.MainActivity.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context arg0, Intent arg1) {
            String action = arg1.getAction();
            if (action.equals(MainActivity.BOOTBCHECK)) {
                String classname = arg1.getStringExtra("class");
                if (!classname.equals("com.microntek.avin") && !classname.equals("phonecallin") && !classname.equals("phonecallout")) {
                    MainActivity.this.finish();
                    return;
                }
                return;
            }
            if (action.equals(MainActivity.REMOVETASK) && arg1.getStringExtra("class").equals("com.microntek.avin")) {
                MainActivity.this.removeTask = true;
                MainActivity.this.finish();
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public void captureOnOff() {
        if (mVideoEnable && this.surfaceShow) {
            if (!this.captureOnFlag) {
                this.captureOnFlag = true;
                if (CHANNEL.equals("line")) {
                    this.mSurfaceView.startPreview("avin");
                } else {
                    this.mSurfaceView.startPreview("dvr");
                }
                this.mSurfaceView.setVisibility(0);
                return;
            }
            return;
        }
        if (this.captureOnFlag) {
            this.captureOnFlag = false;
            this.mSurfaceView.stopPreview();
            this.mSurfaceView.setVisibility(4);
        }
    }

    protected void focusRequest() {
        this.mAudioManager.requestAudioFocus(this.afChangeListener, 3, 1);
        if (!this.focusGainFlag) {
            setParameters("av_focus_gain=" + CHANNEL);
            this.focusGainFlag = true;
        }
    }

    protected void focusAbandon() {
        this.mAudioManager.abandonAudioFocus(this.afChangeListener);
        if (this.focusGainFlag) {
            this.focusGainFlag = false;
            setParameters("av_focus_loss=" + CHANNEL);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideStatusBar() {
        mStatusBarFlag = false;
        if (SystemProperties.get("ro.product.customer.sub").equals("RM09")) {
            this.mBtAv1.setVisibility(8);
            this.mBtAv2.setVisibility(8);
        }
        if (this.hasNaviBar) {
            getWindow().getDecorView().setSystemUiVisibility(3846);
        } else {
            getWindow().clearFlags(65536);
            getWindow().addFlags(1024);
        }
        this.mHandler.removeMessages(65284);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resumeStatusBar() {
        mStatusBarFlag = true;
        if (SystemProperties.get("ro.product.customer.sub").equals("RM09")) {
            this.mBtAv1.setVisibility(0);
            this.mBtAv2.setVisibility(0);
        }
        if (this.hasNaviBar) {
            getWindow().getDecorView().setSystemUiVisibility(1792);
        } else {
            getWindow().clearFlags(65536);
            getWindow().clearFlags(1024);
        }
        this.mHandler.removeMessages(65284);
        if (this.mVideoSignal) {
            this.mHandler.sendEmptyMessageDelayed(65284, 10000L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setParameters(String str) {
        this.mCarManager.setParameters(str);
    }

    private String getParameters(String str) {
        return this.mCarManager.getParameters(str);
    }

    @Override // android.app.Activity
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("startbackflag", this.mStartbackflag);
        super.onSaveInstanceState(outState);
    }

    private boolean isChang() {
        int i = this.mMultiWindowModeW;
        return i == 1280 || i == 633 || i == 1024 || i == 505 || i == 800 || i == 393;
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.mMultiWindowModeW = dm.widthPixels;
        if (isChang() && !this.mUiModeChange && !this.mCustomerName.equals("HT")) {
            this.surfaceShow = false;
            captureOnOff();
            InitUi();
            this.surfaceShow = true;
            captureOnOff();
        }
        this.mUiModeChange = false;
    }

    /* loaded from: classes.dex */
    private class UpdateThemeObserver extends ContentObserver {
        public UpdateThemeObserver() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            MainActivity.this.mUiModeChange = true;
        }
    }

    private void InitUi() {
        setContentView(C0005R.layout.main);
        this.mBtAv1 = (ImageView) findViewById(C0005R.id.bt_av1);
        this.mBtAv2 = (ImageView) findViewById(C0005R.id.bt_av2);
        this.mAkmInfo = (LinearLayout) findViewById(C0005R.id.akm_info);
        this.mBtAv1.setOnClickListener(this);
        this.mBtAv2.setOnClickListener(this);
        this.mShortcut = (LinearLayout) findViewById(C0005R.id.ll_shortcut);
        this.mMusic = findViewById(C0005R.id.btn_music);
        this.mVideo = findViewById(C0005R.id.btn_video);
        this.mRadio = findViewById(C0005R.id.btn_radio);
        this.mBtMusic = findViewById(C0005R.id.btn_btmusic);
        this.mAvin = findViewById(C0005R.id.btn_avin);
        this.mFile = findViewById(C0005R.id.btn_file);
        this.mMusic.setOnClickListener(this);
        this.mVideo.setOnClickListener(this);
        this.mRadio.setOnClickListener(this);
        this.mBtMusic.setOnClickListener(this);
        this.mAvin.setOnClickListener(this);
        this.mFile.setOnClickListener(this);
        this.mySharedPreferences = getPreferenct();
        if (SystemProperties.get("ro.product.customer").equals("AKM") && !SystemProperties.get("ro.product.customer.sub").equals("AKM3")) {
            this.mAkmInfo.setVisibility(0);
        }
        if (SystemProperties.get("ro.product.customer.sub").equals("RM09")) {
            this.mBtAv1.setVisibility(0);
            this.mBtAv2.setVisibility(0);
            CHANNEL = this.mySharedPreferences.getString("channel", "line");
            ENTER_CHANNEL = this.mySharedPreferences.getString("enter_channel", "av_channel_enter=line");
            EXIT_CHANNEL = this.mySharedPreferences.getString("exit_channel", "av_channel_exit=line");
        }
        this.mSurfaceView = (GLFrameSurface) findViewById(C0005R.id.preview_content);
        if (SystemProperties.get("ro.product.customer.sub").equals("HZC39")) {
            this.mShortcut.setVisibility(0);
            this.mSurfaceView.setBottom(75);
        } else {
            this.mShortcut.setVisibility(8);
        }
        this.mSurfaceView.setOnClickListener(new View.OnClickListener() { // from class: com.microntek.avin.MainActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (MainActivity.mStatusBarFlag) {
                    if (MainActivity.this.mVideoSignal && MainActivity.mVideoEnable) {
                        MainActivity.this.hideStatusBar();
                        return;
                    }
                    return;
                }
                MainActivity.this.resumeStatusBar();
            }
        });
        this.mSignalshow = (ImageView) findViewById(C0005R.id.signalshow);
        this.audioOnlyshow = (ImageView) findViewById(C0005R.id.audio_only);
        this.mWarnText = (TextView) findViewById(C0005R.id.warntext);
        if (getParameters("sta_function=26").equals("1") || "true".equals(SystemProperties.get("ro.product.avinaudioonly"))) {
            findViewById(C0005R.id.audio_only).setVisibility(0);
        }
        if (getResources().getConfiguration().orientation == 1 && !isInMultiWindowMode()) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) findViewById(C0005R.id.layout).getLayoutParams();
            params.height = (Math.min(dm.widthPixels, dm.heightPixels) * 9) / 14;
            getWindow().getDecorView().setBackgroundColor(-16777216);
            findViewById(C0005R.id.layout).setLayoutParams(params);
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAudioManager = (AudioManager) getSystemService("audio");
        this.mCarManager = new CarManager();
        this.mCustomerName = SystemProperties.get("ro.product.customer");
        this.mCustomerSub = SystemProperties.get("ro.product.customer.sub", "HCT");
        if (!this.hasNaviBar) {
            getWindow().addFlags(256);
            getWindow().clearFlags(65536);
            getWindow().clearFlags(1024);
        }
        if (savedInstanceState != null) {
            this.mStartbackflag = savedInstanceState.getInt("startbackflag");
        }
        if (this.mStartbackflag == 0) {
            Intent it = getIntent();
            int intExtra = it.getIntExtra("start", 0);
            this.mStartbackflag = intExtra;
            if (intExtra != 0) {
                moveTaskToBack(true);
            }
        }
        Intent it1 = new Intent(BOOTBCHECK);
        it1.putExtra("class", "com.microntek.avin");
        sendBroadcast(it1);
        focusRequest();
        sendCanBusAvinOn();
        InitUi();
        setParameters(ENTER_CHANNEL);
        IntentFilter itfl = new IntentFilter();
        itfl.addAction(BOOTBCHECK);
        itfl.addAction(REMOVETASK);
        itfl.addAction(STATECHANGE_STRING);
        itfl.addAction(STATEVIDEO_SIGNAL);
        registerReceiver(this.AVINBootReceiver, itfl);
        this.mCarManager.attach(new Handler() { // from class: com.microntek.avin.MainActivity.4
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                boolean signal;
                super.handleMessage(msg);
                String msgtype = (String) msg.obj;
                if ("CarEvent".equals(msgtype)) {
                    String type = msg.getData().getString("type");
                    if ("handbrake".equals(type)) {
                        MainActivity.this.updateDrivingState();
                        MainActivity.this.captureOnOff();
                        return;
                    }
                    return;
                }
                if ("VideoSignal".equals(msgtype)) {
                    Bundle bundle = msg.getData();
                    String type2 = bundle.getString("type");
                    if ("video_signal".equals(type2)) {
                        String channel = bundle.getString("channel");
                        if ("avin".equals(channel) && MainActivity.this.mVideoSignal != (signal = bundle.getBoolean("signal"))) {
                            MainActivity.this.mVideoSignal = signal;
                            MainActivity.this.updateSignalState();
                        }
                    }
                }
            }
        }, "CarEvent,VideoSignal");
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(Settings.System.getUriFor(UPDATETHEME), true, this.mUpdateThemeObserver);
    }

    private SharedPreferences getPreferenct() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getParameters("sta_mcu_version=").indexOf("LD") != -1 && intent.getIntExtra("type", 0) == 1) {
            finish();
        }
    }

    private void exit() {
        if (!this.mExit) {
            this.mExit = true;
            unregisterReceiver(this.AVINBootReceiver);
            this.surfaceShow = false;
            captureOnOff();
            setParameters(EXIT_CHANNEL);
            focusAbandon();
            this.mHandler.removeCallbacksAndMessages(null);
            sendCanBusAvinOff();
            this.mCarManager.detach();
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        finish();
    }

    @Override // android.app.Activity
    public void finish() {
        if (isInMultiWindowMode() && !this.removeTask) {
            moveTaskToBack(true);
        }
        exit();
        super.finish();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        exit();
        getContentResolver().unregisterContentObserver(this.mUpdateThemeObserver);
        this.isFront = false;
        super.onDestroy();
    }

    @Override // android.app.Activity
    protected void onPause() {
        this.surfaceShow = false;
        captureOnOff();
        this.isFront = false;
        super.onPause();
    }

    @Override // android.app.Activity
    protected void onResume() {
        this.mVideoSignal = true;
        this.surfaceShow = true;
        updateDrivingState();
        captureOnOff();
        updateSignalState();
        sendBroadcast(new Intent(MSG_RESETMUSICCLOCK));
        focusRequest();
        this.isFront = true;
        this.mAvin.setSelected(true);
        super.onResume();
    }

    private void sendCanBusAvinOn() {
        Intent it1 = new Intent(MSG_MTC_CANBUS_DISPLAY);
        it1.putExtra("type", "avin-on");
        sendBroadcast(it1);
    }

    private void sendCanBusAvinOff() {
        Intent it1 = new Intent(MSG_MTC_CANBUS_DISPLAY);
        it1.putExtra("type", "avin-off");
        sendBroadcast(it1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDrivingState() {
        if (!this.mCarManager.getVideoEnable()) {
            mVideoEnable = true;
            this.mWarnText.setVisibility(8);
        } else {
            mVideoEnable = false;
            if (this.mCustomerSub.equals("AKM")) {
                this.mWarnText.setVisibility(8);
            } else if (!"HZC25_1".equals(this.mCustomerSub)) {
                this.mWarnText.setVisibility(0);
            } else {
                this.mWarnText.setVisibility(8);
                mVideoEnable = true;
            }
        }
        resumeStatusBar();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSignalState() {
        if (this.mVideoSignal) {
            this.mSignalshow.setVisibility(8);
            if ("HZC25_1".equals(this.mCustomerSub)) {
                this.audioOnlyshow.setVisibility(8);
            }
        } else {
            this.mSignalshow.setVisibility(0);
            if ("HZC25_1".equals(this.mCustomerSub)) {
                this.audioOnlyshow.setVisibility(0);
            }
        }
        resumeStatusBar();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        String channel = "";
        String enter_channel = "";
        String exit_channel = "";
        switch (v.getId()) {
            case C0005R.id.bt_av1 /* 2130968578 */:
                if (CHANNEL.equals("line")) {
                    return;
                }
                channel = "line";
                enter_channel = "av_channel_enter=line";
                exit_channel = "av_channel_exit=line";
                break;
            case C0005R.id.bt_av2 /* 2130968579 */:
                if (CHANNEL.equals("dvr")) {
                    return;
                }
                channel = "dvr";
                enter_channel = "av_channel_enter=dvr";
                exit_channel = "av_channel_exit=dvr";
                break;
            case C0005R.id.btn_avin /* 2130968580 */:
                if (!this.isFront) {
                    startApp("com.microntek.avin", "com.microntek.avin.MainActivity");
                    break;
                }
                break;
            case C0005R.id.btn_btmusic /* 2130968581 */:
                startApp("com.microntek.btmusic", "com.microntek.btmusic.MainActivity");
                return;
            case C0005R.id.btn_file /* 2130968582 */:
                startApp("com.microntek.FileBrowser", "com.microntek.FileBrowser.MainActivity");
                return;
            case C0005R.id.btn_music /* 2130968583 */:
                startApp("com.microntek.music", "com.microntek.music.MainActivity");
                return;
            case C0005R.id.btn_radio /* 2130968584 */:
                startApp("com.microntek.radio", "com.microntek.radio.MainActivity");
                return;
            case C0005R.id.btn_video /* 2130968585 */:
                startApp("com.microntek.media", "com.microntek.media.MainActivity");
                return;
        }
        SharedPreferences.Editor editor = this.mySharedPreferences.edit();
        editor.putString("channel", channel);
        editor.putString("enter_channel", enter_channel);
        editor.putString("exit_channel", exit_channel);
        editor.commit();
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(65536);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private void startApp(String str, String str1) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        ComponentName cn = new ComponentName(str, str1);
        intent.setComponent(cn);
        startActivity(intent);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!mStatusBarFlag && this.hasNaviBar && hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(3846);
        }
    }
}
