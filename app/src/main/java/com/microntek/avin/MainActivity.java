package com.microntek.avin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.microntek.CarManager;
import android.os.Bundle;

import java.util.Objects;

public class MainActivity extends Activity {

    private static final String EXPECTED_PACKAGE = "com.microntek.avin";
    private static final String CHANNEL = "line";
    private static final String RESTART_FLAG_KEY = "restartFlag";
    private static final String IT_BOOT_CHECK = "com.microntek.bootcheck";
    private static final String IT_REMOVE_TASK = "com.microntek.removetask";
    private static final String IT_RESET_MUSIC_CLOCK = "com.microntek.musicclockreset";

    private CarManager carManager = null;
    private AudioManager audioManager = null;

    private int restartFlag = 0;
    private boolean exit = false;
    private boolean isOnAudioFocus = false;
    private boolean shouldRemoveTask = false;

    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = focusChange -> {
        if (focusChange != AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                this.setParameters("av_focus_gain=" + MainActivity.CHANNEL);
                this.isOnAudioFocus = true;
            }
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                this.setParameters("av_focus_loss=" + MainActivity.CHANNEL);
                this.isOnAudioFocus = false;
            }
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                this.setParameters("av_focus_loss=" + MainActivity.CHANNEL);
                this.isOnAudioFocus = false;
                this.finish();
            }
        }
    };

    private final AudioFocusRequest audioFocusRequest =
            new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build();

    private void requestAudioFocus() {
        this.audioManager.requestAudioFocus(this.audioFocusRequest);
        if (!this.isOnAudioFocus) {
            setParameters("av_focus_gain=" + CHANNEL);
            this.isOnAudioFocus = true;
        }
    }

    private void abandonAudioFocus() {
        this.audioManager.abandonAudioFocusRequest(this.audioFocusRequest);
        if (this.isOnAudioFocus) {
            setParameters("av_focus_loss=" + CHANNEL);
            this.isOnAudioFocus = false;
        }
    }

    private final BroadcastReceiver carBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String action = arg1.getAction();
            String classname = arg1.getStringExtra("class");
            if (Objects.equals(action, MainActivity.IT_BOOT_CHECK)) {
                if (!EXPECTED_PACKAGE.equals(classname) &&
                        !"phonecallin".equals(classname) &&
                        !"phonecallout".equals(classname)) {
                    MainActivity.this.finish();
                }
            } else if (Objects.equals(action, MainActivity.IT_REMOVE_TASK)) {
                if (EXPECTED_PACKAGE.equals(classname)) {
                    MainActivity.this.shouldRemoveTask = true;
                    MainActivity.this.finish();
                }
            }
        }
    };

    private void setParameters(String key) {
        this.carManager.setParameters(key);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_YES:
                recreate();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(RESTART_FLAG_KEY, this.restartFlag);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        this.carManager = new CarManager();

        if (savedInstanceState != null) {
            this.restartFlag = savedInstanceState.getInt(RESTART_FLAG_KEY);
        }

        if (this.restartFlag == 0) {
            Intent it = getIntent();
            int intExtra = it.getIntExtra("start", 0);
            this.restartFlag = intExtra;
            if (intExtra != 0) {
                moveTaskToBack(true);
            }
        }

        Intent it1 = new Intent(IT_BOOT_CHECK);
        it1.putExtra("class", EXPECTED_PACKAGE);
        sendBroadcast(it1);

        requestAudioFocus();
        setContentView(R.layout.main);

        setParameters("av_channel_enter=" + CHANNEL);

        IntentFilter itFilter = new IntentFilter();
        itFilter.addAction(IT_BOOT_CHECK);
        itFilter.addAction(IT_REMOVE_TASK);
        registerReceiver(this.carBroadcastReceiver, itFilter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getIntExtra("type", 0) == 1) {
            finish();
        }
    }

    private void exit() {
        if (!exit) {
            this.exit = true;
            unregisterReceiver(this.carBroadcastReceiver);
            setParameters("av_channel_exit=" + CHANNEL);
            abandonAudioFocus();
            this.carManager.detach();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        exit();
        if (isInMultiWindowMode() && !this.shouldRemoveTask) {
            moveTaskToBack(true);
        }
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        exit();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        sendBroadcast(new Intent(IT_RESET_MUSIC_CLOCK));
        requestAudioFocus();
        super.onResume();
    }

}
