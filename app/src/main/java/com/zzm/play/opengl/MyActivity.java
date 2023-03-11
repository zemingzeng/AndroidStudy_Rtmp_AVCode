package com.zzm.play.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.LayoutInflaterCompat;

import com.zzm.play.R;
import com.zzm.play.utils.l;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        l.i("MyActivity onCreate getBaseContext:" + getBaseContext().toString());

        setTheme(R.style.App_Theme);

        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new MyFactory2());

        super.onCreate(savedInstanceState);

        l.i("MyActivity onCreate");

        setContentView(R.layout.opengl_activity_layout);

        init();

        doSomething();

    }

    private GLSurfaceView glSurfaceView;

    private void init() {

        glSurfaceView = findViewById(R.id.gl_surface_view);


    }

    private void doSomething() {

        l.i("doSomething");
        //设置版本 0x00020000 gles20
        glSurfaceView.setEGLContextClientVersion(2);

        //设置渲染工具
        glSurfaceView.setRenderer(new SquareRender());

        //设置渲染模式
        //RENDERMODE_WHEN_DIRTY手动渲染:glSurfaceView.requestRender(),glSurfaceView.onResume()
        //RENDERMODE_CONTINUOUSLY一直不停地渲染
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        l.i("MyActivity attachBaseContext : " + newBase.toString());
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onResume() {
        super.onResume();
        l.i("MyActivity onResume");

        if (null != glSurfaceView)
            glSurfaceView.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        l.i("MyActivity onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        l.i("MyActivity onPause");

        if (null != glSurfaceView)
            glSurfaceView.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        l.i("MyActivity onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        l.i("MyActivity onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        l.i("MyActivity onDestroy");
    }

    class MyFactory2 implements LayoutInflater.Factory2 {

        @Nullable
        @Override
        public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
            l.i("MyFactory2  onCreateView Factory2 name : " + name);
            return getDelegate().createView(parent, name, context, attrs);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
            l.i("MyFactory2  onCreateView  Factory  name : " + name);
            return null;
        }
    }
}
