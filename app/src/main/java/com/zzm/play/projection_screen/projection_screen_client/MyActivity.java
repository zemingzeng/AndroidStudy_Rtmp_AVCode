package com.zzm.play.projection_screen.projection_screen_client;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zzm.play.R;
import com.zzm.play.utils.l;

public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.projection_screen_client);

        init();

    }

    private H26xDeCode h26xDeCode;
    private SurfaceView surfaceView;

    private void init() {

        surfaceView = findViewById(R.id.surface_view);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                l.i("surfaceCreated");
                h26xDeCode = new H26xDeCode(holder.getSurface());
                h26xDeCode.startMe();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        if (null != h26xDeCode)
            h26xDeCode.closeMe();
        super.onDestroy();
    }
}
