package com.zzm.play.h26x_player;

import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zzm.play.R;
import com.zzm.play.utils.l;
import com.zzm.play.utils.PermissionUtil;

import java.io.File;

public class H26xPlayerActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private SurfaceHolder holder;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init();
    }


    private String videoPath;
    private H26xPlayer h26xPlayer;

    private void init() {

        new PermissionUtil().checkPermission(this);

        //可用ffmpeg命令分割视频和音频
//        videoPath = Environment.getExternalStorageDirectory() + File.separator + "out.h264";
        videoPath = Environment.getExternalStorageDirectory() + File.separator + "out.h265";

        surfaceView = findViewById(R.id.sv);

        holder = surfaceView.getHolder();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                l.i("surfaceCreated");
                h26xPlayer = new H26xPlayer(videoPath, holder.getSurface(), H26xPlayerActivity.this);
                h26xPlayer.play();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }
}
