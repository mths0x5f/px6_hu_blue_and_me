package com.microntek.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import com.microntek.cvbs.JniUtil;
import com.microntek.cvbs.OnFrameListener;
import java.nio.ByteBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/* loaded from: classes.dex */
public class GLFrameSurface extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static int SHOW_DELAY_FRAME = 10;
    private static final String TAG = "GLFrameSurface";
    private int mBuferIdx;
    private String mChannel;
    private int mFrameCount;
    private JniUtil mJni;
    OnFrameListener mListener;
    private boolean mMirror;
    private int mMode;
    private boolean mShow;
    private ByteBuffer mUV;
    private int mVideoHeight;
    private int mVideoWidth;

    /* renamed from: mY */
    private ByteBuffer f1mY;
    private GLProgram prog;

    public GLFrameSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMode = 0;
        this.mShow = true;
        this.mMirror = false;
        this.mBuferIdx = 0;
        this.mFrameCount = 0;
        this.mChannel = null;
        this.mJni = new JniUtil();
        this.prog = new GLProgram(0);
        this.mVideoWidth = -1;
        this.mVideoHeight = -1;
        this.mListener = new OnFrameListener() { // from class: com.microntek.gl.GLFrameSurface.1
            @Override // com.microntek.cvbs.OnFrameListener
            public void OnFrameAvailable(int w, int h, int idx) {
                GLFrameSurface.this.frameUpdate(w, h, idx);
            }

            @Override // com.microntek.cvbs.OnFrameListener
            public void OnFrameInterrupted() {
                GLFrameSurface.this.frameClear();
            }
        };
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(0);
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        this.mJni.on_surface_created();
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.mJni.on_surface_changed(0, 0, width, height);
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onDrawFrame(GL10 gl) {
        boolean show;
        boolean mirror;
        int mode;
        int idx;
        int width;
        int height;
        synchronized (this) {
            show = this.mFrameCount > SHOW_DELAY_FRAME && this.mShow;
            mirror = this.mMirror;
            mode = this.mMode;
            idx = this.mBuferIdx;
            width = this.mVideoWidth;
            height = this.mVideoHeight;
        }
        this.mJni.on_draw_frame(show, mirror, mode, idx, width, height);
    }

    public void showCamera(boolean show) {
        synchronized (this) {
            this.mShow = show;
        }
        if (!show) {
            frameClear();
        }
    }

    public void startPreview(String channel, boolean mirror) {
        this.mMirror = mirror;
        startPreview(channel);
    }

    public void startPreview(String channel) {
        Log.i("HCT-CVBS", "startPreview " + channel);
        String str = this.mChannel;
        if (str == null || !str.equals(channel)) {
            this.mChannel = channel;
            frameClear();
            this.mJni.open(this.mListener, this.mChannel);
        }
    }

    public void stopPreview() {
        Log.i("HCT-CVBS", "stopPreview " + this.mChannel);
        if (this.mChannel != null) {
            this.mChannel = null;
            frameClear();
            this.mJni.close();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void frameUpdate(int w, int h, int idx) {
        synchronized (this) {
            if (this.mShow) {
                if (this.mFrameCount < 1000) {
                    this.mFrameCount++;
                }
            } else {
                this.mFrameCount = 0;
            }
            this.mVideoWidth = w;
            this.mVideoHeight = h;
            this.mBuferIdx = idx;
            if ((w != 960 && w != 1920) || (h != 240 && h != 288)) {
                if (w != 1920 && w != 1280) {
                    this.mMode = 1;
                }
                this.mMode = 2;
            }
            this.mMode = 3;
        }
        requestRender();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void frameClear() {
        synchronized (this) {
            this.mFrameCount = 0;
        }
        requestRender();
    }
}
