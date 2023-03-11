package com.zzm.play.opengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.zzm.play.utils.l;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class BaseRender implements GLSurfaceView.Renderer {
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        l.i(getClass().getName() + " onSurfaceCreated");

        // Set the background frame color
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);

        created();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        l.i(getClass().getName() + " onSurfaceChanged width height : " + width + " " + height);

        GLES20.glViewport(0, 0, width, height);

        changed(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        l.i(getClass().getName() + " onDrawFrame");

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        draw();
    }

    public abstract void created();

    public abstract void changed(int width, int height);

    public abstract void draw();

}
