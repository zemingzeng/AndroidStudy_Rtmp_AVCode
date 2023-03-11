package com.zzm.play.test;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.LongDef;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.GestureDetector;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Scroller;

import com.zzm.play.R;
import com.zzm.play.utils.l;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import dalvik.system.PathClassLoader;
import retrofit2.Retrofit;

public class MyActivity extends AppCompatActivity {
//    static class Person {
//
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);


        setContentView(R.layout.test_layout);

        Person person = new Person();

        person.hashCode();


        init();

        try {
            doSomething();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void doSomething() throws IOException {

        Retrofit retrofit = new Retrofit.Builder().build();
        retrofit.create(null);
//        URL
//        HttpURLConnection connection;
//         connection.getInputStream();
//        BitmapFactory.decodeStream()
        Socket socket = new Socket();
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new Fragment();
        FragmentTransaction replace = fragmentTransaction.replace(android.R.id.content, fragment);
//        fragment.setArguments();

        PathClassLoader pathClassLoader = null;

        getClassLoader();

        getBaseContext();
    }

    Button bt;
    OrientationEventListener orientationEventListener;
    float[] gravity;
    float[] r;
    float[] geomagnetic;
    float[] values;
    SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
//            l.i(" onSensorChanged   : " + Arrays.toString(event.values));
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = event.values.clone();
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity = event.values.clone();
                getOritation();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            l.i(" onAccuracyChanged");
        }
    };

    private void init() {
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;  //手机平放时，检测不到有效的角度
                }
                //只检测是否有四个角度的改变
                if (orientation > 350 || orientation < 10) { //0度
                    orientation = 0;
                } else if (orientation > 80 && orientation < 100) { //90度
                    orientation = 90;
                } else if (orientation > 170 && orientation < 190) { //180度
                    orientation = 180;
                } else if (orientation > 260 && orientation < 280) { //270度
                    orientation = 270;
                } else {
                    return;
                }
                l.i("orientationEventListener onOrientationChanged : " + orientation);
                Thread.currentThread().getName();
            }
        };
        //orientationEventListener.enable();
        bt = findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOritation();
            }
        });

        /**
         * 初始化传感器
         * */
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //获取Sensor
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //初始化数组
        gravity = new float[3];//用来保存加速度传感器的值
        r = new float[9];//
        geomagnetic = new float[3];//用来保存地磁传感器的值
        values = new float[3];//用来保存最终的结果

        sensorManager.registerListener(sensorListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    /**
     * 获取手机旋转角度
     */
    public void getOritation() {
        // r从这里返回
        SensorManager.getRotationMatrix(r, null, gravity, geomagnetic);
        //values从这里返回
        SensorManager.getOrientation(r, values);
        //提取数据
        double degreeZ = Math.toDegrees(values[0]);
        double degreeX = Math.toDegrees(values[1]);
        double degreeY = Math.toDegrees(values[2]);
        l.i(" degreeX  degreeY degreeZ : " + degreeX + "  " + degreeY + "  " + degreeZ + "  ");
    }

    @Override
    protected void onDestroy() {
        if (null != orientationEventListener) {
            orientationEventListener.disable();
            orientationEventListener = null;
        }
        super.onDestroy();
    }
}
