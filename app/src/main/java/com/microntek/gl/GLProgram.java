package com.microntek.gl;

import android.opengl.GLES20;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* loaded from: classes.dex */
public class GLProgram {
    private static final String FRAGMENT_SHADER = "precision mediump float;uniform sampler2D tex_y;uniform sampler2D tex_uv;varying vec2 tc;void main() {vec3 yuv;vec3 rgb;yuv.x = (texture2D(tex_y, tc).r - 16./255.);yuv.y = (texture2D(tex_uv, tc).r - 128./255.);yuv.z = (texture2D(tex_uv, tc).a - 128./255.);rgb = mat3( 1.164,   1.164,   1.164,0,       -0.213,  2.115,1.793,   -0.534,  0 ) * yuv; gl_FragColor = vec4(rgb, 1);}";
    private static final String VERTEX_SHADER = "attribute vec4 vPosition;attribute vec2 a_texCoord;varying vec2 tc;void main() {gl_Position = vPosition;tc = a_texCoord;}";
    private int _coordHandle;
    private ByteBuffer _coord_buffer;
    private ByteBuffer _order_buffer;
    private int _positionHandle;
    private int _uvhandle;
    private ByteBuffer _vertice_buffer;
    private int _yhandle;
    private static short[] drawOrder = {0, 1, 2, 0, 2, 3};
    private static float[] vertice = {-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f};
    private static float[] vertice_mirror = {1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f};
    private static float[] coord = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f};
    private Boolean isProgBuilt = false;
    private int _program = -1;
    private int _video_width = -1;
    private int _video_height = -1;
    private int _ytid = -1;
    private int _uvtid = -1;

    public GLProgram(int attr) {
    }

    public void buildProgram() {
        if (this.isProgBuilt.booleanValue()) {
            return;
        }
        createBuffers();
        if (this._program <= 0) {
            this._program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        }
        this._positionHandle = GLES20.glGetAttribLocation(this._program, "vPosition");
        checkGlError("glGetAttribLocation vPosition");
        if (this._positionHandle == -1) {
            throw new RuntimeException("Could not get attribute location for vPosition");
        }
        this._coordHandle = GLES20.glGetAttribLocation(this._program, "a_texCoord");
        checkGlError("glGetAttribLocation a_texCoord");
        if (this._coordHandle == -1) {
            throw new RuntimeException("Could not get attribute location for a_texCoord");
        }
        this._yhandle = GLES20.glGetUniformLocation(this._program, "tex_y");
        checkGlError("glGetUniformLocation tex_y");
        if (this._yhandle == -1) {
            throw new RuntimeException("Could not get uniform location for tex_y");
        }
        this._uvhandle = GLES20.glGetUniformLocation(this._program, "tex_uv");
        checkGlError("glGetUniformLocation tex_uv");
        if (this._uvhandle == -1) {
            throw new RuntimeException("Could not get uniform location for tex_uv");
        }
        this.isProgBuilt = true;
    }

    public void buildTextures(Buffer y, Buffer uv, int width, int height) {
        boolean videoSizeChanged = (width == this._video_width && height == this._video_height) ? false : true;
        if (videoSizeChanged) {
            this._video_width = width;
            this._video_height = height;
        }
        if (this._ytid < 0 || videoSizeChanged) {
            int i = this._ytid;
            if (i >= 0) {
                GLES20.glDeleteTextures(1, new int[]{i}, 0);
                checkGlError("glDeleteTextures");
            }
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            checkGlError("glGenTextures");
            this._ytid = textures[0];
        }
        GLES20.glBindTexture(3553, this._ytid);
        checkGlError("glBindTexture");
        GLES20.glTexImage2D(3553, 0, 6409, this._video_width, this._video_height, 0, 6409, 5121, y);
        checkGlError("glTexImage2D");
        GLES20.glTexParameterf(3553, 10241, 9729.0f);
        GLES20.glTexParameterf(3553, 10240, 9729.0f);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        if (this._uvtid < 0 || videoSizeChanged) {
            int i2 = this._uvtid;
            if (i2 >= 0) {
                GLES20.glDeleteTextures(1, new int[]{i2}, 0);
                checkGlError("glDeleteTextures");
            }
            int[] textures2 = new int[1];
            GLES20.glGenTextures(1, textures2, 0);
            checkGlError("glGenTextures");
            this._uvtid = textures2[0];
        }
        GLES20.glBindTexture(3553, this._uvtid);
        GLES20.glTexImage2D(3553, 0, 6410, this._video_width / 2, this._video_height / 2, 0, 6410, 5121, uv);
        GLES20.glTexParameterf(3553, 10241, 9729.0f);
        GLES20.glTexParameterf(3553, 10240, 9729.0f);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
    }

    public void drawFrame() {
        drawFrame(false);
    }

    public void drawFrame(boolean mirror) {
        if (mirror) {
            this._vertice_buffer.asFloatBuffer().put(vertice_mirror);
        } else {
            this._vertice_buffer.asFloatBuffer().put(vertice);
        }
        this._vertice_buffer.position(0);
        GLES20.glUseProgram(this._program);
        checkGlError("glUseProgram");
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this._ytid);
        GLES20.glUniform1i(this._yhandle, 0);
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(3553, this._uvtid);
        GLES20.glUniform1i(this._uvhandle, 1);
        GLES20.glDrawArrays(5, 0, 4);
        GLES20.glFinish();
        GLES20.glEnableVertexAttribArray(this._positionHandle);
        GLES20.glVertexAttribPointer(this._positionHandle, 2, 5126, false, 8, (Buffer) this._vertice_buffer);
        GLES20.glEnableVertexAttribArray(this._coordHandle);
        GLES20.glVertexAttribPointer(this._coordHandle, 2, 5126, false, 8, (Buffer) this._coord_buffer);
        GLES20.glDrawElements(4, 6, 5123, this._order_buffer);
        GLES20.glDisableVertexAttribArray(this._positionHandle);
        GLES20.glDisableVertexAttribArray(this._coordHandle);
    }

    public int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(35633, vertexSource);
        int pixelShader = loadShader(35632, fragmentSource);
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, 35714, linkStatus, 0);
            if (linkStatus[0] != 1) {
                GLES20.glDeleteProgram(program);
                return 0;
            }
            return program;
        }
        return program;
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, 35713, compiled, 0);
            if (compiled[0] == 0) {
                GLES20.glDeleteShader(shader);
                return 0;
            }
            return shader;
        }
        return shader;
    }

    private void createBuffers() {
        if (this._vertice_buffer == null) {
            ByteBuffer allocateDirect = ByteBuffer.allocateDirect(vertice.length * 4);
            this._vertice_buffer = allocateDirect;
            allocateDirect.order(ByteOrder.nativeOrder());
            this._vertice_buffer.asFloatBuffer().put(vertice);
            this._vertice_buffer.position(0);
            ByteBuffer allocateDirect2 = ByteBuffer.allocateDirect(coord.length * 4);
            this._coord_buffer = allocateDirect2;
            allocateDirect2.order(ByteOrder.nativeOrder());
            this._coord_buffer.asFloatBuffer().put(coord);
            this._coord_buffer.position(0);
            ByteBuffer allocateDirect3 = ByteBuffer.allocateDirect(drawOrder.length * 2);
            this._order_buffer = allocateDirect3;
            allocateDirect3.order(ByteOrder.nativeOrder());
            this._order_buffer.asShortBuffer().put(drawOrder);
            this._order_buffer.position(0);
        }
    }

    private void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != 0) {
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
