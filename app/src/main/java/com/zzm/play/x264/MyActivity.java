package com.zzm.play.x264;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;

import com.zzm.play.R;
import com.zzm.play.utils.PermissionUtil;

public class MyActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.x264_camera2_layout);

        init();

        doSomeThing();

    }

    private Camera2Tool camera2Tool;

    private TextureView textureView;

    private void init() {

        textureView = findViewById(R.id.texture_view);

        camera2Tool = new Camera2Tool(this);


    }

    private void doSomeThing() {

        PermissionUtil.checkPermission(this);

        camera2Tool.setDisplay(textureView);

    }

    @Override
    protected void onDestroy() {
        if (camera2Tool != null) {
            camera2Tool.release();
            camera2Tool = null;
        }
        super.onDestroy();
    }
}
