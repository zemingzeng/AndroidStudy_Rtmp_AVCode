package com.zzm.play.projection_screen.projection_screen_server;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.zzm.play.R;
import com.zzm.play.utils.PermissionUtil;

public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.projection_screen_server);

        PermissionUtil.checkPermission(this);

        init();

    }


    private Button bt;
    private ProjectionScreen projectionScreen;

    private void init() {
        bt = findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (projectionScreen == null) {
                    projectionScreen = new ProjectionScreen(MyActivity.this);
                    projectionScreen.start();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        projectionScreen.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {

        if (null != projectionScreen)
            projectionScreen.stop();

        super.onDestroy();
    }

}
