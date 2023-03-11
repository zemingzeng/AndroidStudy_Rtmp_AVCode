package com.zzm.play.rtmp;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zzm.play.R;
import com.zzm.play.utils.PermissionUtil;

public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rtmp_activity_layout);

        init();

        doSomething();
    }


    private void init() {


    }

    private void doSomething() {

        PermissionUtil.checkPermission(this);

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();

        startActivityForResult(screenCaptureIntent, 100);

        myMediaProjection = new MyMediaProjection(mediaProjectionManager);
    }

    MyMediaProjection myMediaProjection;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        myMediaProjection.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (null != myMediaProjection) {
            myMediaProjection.stopMe();
        }
        super.onDestroy();
    }
}
