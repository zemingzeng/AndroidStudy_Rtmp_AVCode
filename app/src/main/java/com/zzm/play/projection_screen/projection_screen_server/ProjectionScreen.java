package com.zzm.play.projection_screen.projection_screen_server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import androidx.annotation.Nullable;

import com.zzm.play.utils.l;

import static android.app.Activity.RESULT_OK;

public class ProjectionScreen {
    private Activity a;
    private static final int PORT = 9876;

    public ProjectionScreen(Activity a) {

        init(a);
    }

    private MediaProjectionManager manager;

    private void init(Activity a) {
        this.a = a;
    }

    public void start() {
        manager = (MediaProjectionManager) a.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = manager.createScreenCaptureIntent();
        a.startActivityForResult(intent, 100);
    }

    private MediaProjection mediaProjection;

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 100) {
            mediaProjection = manager.getMediaProjection(resultCode, data);
            if (null != mediaProjection) {
                openSocketAndProjectionScreen();
            }
        }

    }

    private H26xEnCode h26xEnCode;
    private Server server;

    private void openSocketAndProjectionScreen() {
        server = new Server(PORT);
        h26xEnCode = new H26xEnCode(mediaProjection,server);

        //开启服务端
        server.startMe();

        //开始录屏并且编码
        h26xEnCode.startProjectionScreenAndEncode();

    }

    public void stop() {
        try {
            if (null != server)
                server.closeMe();
            if (null != h26xEnCode)
                h26xEnCode.stopMe();
            if (null != mediaProjection)
                mediaProjection.stop();
        } catch (Exception e) {
            l.i(e.toString());
        }
    }
}
