package com.zzm.play.camera2_h26x;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.zzm.play.R;
import com.zzm.play.utils.PermissionUtil;
import com.zzm.play.utils.YUVUtil;
import com.zzm.play.utils.l;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MyActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera2_h26x_layout);


        init();

    }

    private TextureView textureView;

    private Size previewSize = null;

    private void init() {

        PermissionUtil.checkPermission(this);

        textureView = findViewById(R.id.texture_view);

        textureView.setSurfaceTextureListener(this);

    }


    private SurfaceTexture surfaceTexture = null;

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        l.i("onSurfaceTextureAvailable width height ： " + width + "  " + height);

        previewSize = new Size(width, height);

        surfaceTexture = surface;

        doSomething();


    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        l.i("onSurfaceTextureSizeChanged width height ： " + width + "  " + height);

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        l.i("onSurfaceTextureDestroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        //l.i("onSurfaceTextureUpdated");
    }

    private Size suitablePreviewSize = null;

    @SuppressLint("MissingPermission")
    private void doSomething() {

        try {

            CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String cameraID : cameraIdList) {
                //camera id 0 一般为后主摄像头
                l.i("camera id : " + cameraID);
            }

            String mainBackCameraID = cameraIdList[0];

            //描述了camera硬件设备以及该设备的可用设置和输出参数
            CameraCharacteristics cameraCharacteristics = cameraManager
                    .getCameraCharacteristics(mainBackCameraID);

            //底层相机数据流配置信息 key value
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streamConfigurationMap == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }

            //相机Clockwise顺时针 sensor 旋转方向
            int cameraSensorClockwiseOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            l.i("sensor Clockwise 角度 : " + cameraSensorClockwiseOrientation);

            //Get a list of sizes compatible with  class to use as an output.
            Size[] outputSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
            //for (Size size : outputSizes)
            // l.i("支持的输出尺寸宽高 : " + size.getWidth() + "  " + size.getHeight());

            suitablePreviewSize = getSuitablePreviewSize(previewSize.getWidth(), previewSize.getHeight(), outputSizes);
            //((FrameLayout.LayoutParams) textureView.getLayoutParams()).width = suitablePreviewSize.getHeight();
            //((FrameLayout.LayoutParams) textureView.getLayoutParams()).height = suitablePreviewSize.getWidth();
            //textureView.requestLayout();

            cameraManager.openCamera(mainBackCameraID, new CameraStateCallBack(), null);

        } catch (CameraAccessException e) {
            l.i(e.toString());
        }

    }

    private static CameraDevice cameraDevice = null;

    private class CameraStateCallBack extends StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            l.i("CameraStateCallBack onOpened");

            cameraDevice = camera;

            initAndOpenSocket(PORT);

            startPreview();

        }


        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            l.i("CameraStateCallBack onDisconnected");

            camera.close();
            cameraDevice = null;


        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            l.i("CameraStateCallBack onError code : " + error);

            camera.close();
            cameraDevice = null;

        }
    }

    private CaptureRequest.Builder captureRequestBuilder = null;

    private ImageReader imageReader = null;

    private List<Surface> surfaceList = new ArrayList<>();

    private Surface previewSurface;

    //开始准备预览
    private void startPreview() {

        if (null == cameraDevice || null == suitablePreviewSize || !textureView.isAvailable())
            return;

        try {

            closeCaptureSession();

            surfaceTexture.setDefaultBufferSize(suitablePreviewSize.getWidth(), suitablePreviewSize.getHeight());


            setUpImageReader();

            previewSurface = new Surface(surfaceTexture);

            surfaceList.clear();
            surfaceList.add(previewSurface);
            surfaceList.add(imageReader.getSurface());
            //创建个和camera的会话
            cameraDevice.createCaptureSession(surfaceList, new CaptureSessionStateCallBack(), null);


        } catch (CameraAccessException e) {
            l.i(e.toString());
        }

    }


    private CameraCaptureSession captureSession = null;

    private class CaptureSessionStateCallBack extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            l.i("CaptureSessionStateCallBack onConfigured");

            captureSession = session;

            updatePreview();

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            l.i("CaptureSessionStateCallBack onConfigureFailed");


        }
    }

    //更新预览，一直去请求预览数据
    private void updatePreview() {

        if (null == cameraDevice)
            return;

        configCaptureRequestBuilder();

        configCaptureSession();


    }

    //设置capture request builder
    private void configCaptureRequestBuilder() {
        try {
            //capture 预览请求构建
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            //AF_MODE_CONTINUOUS_PICTURE：快速持续聚焦，用于静态图片的ZSL捕获。一旦达到扫描目标，触发则立即锁住焦点。取消而继续持续聚焦。
            //captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(imageReader.getSurface());
        } catch (CameraAccessException e) {
            l.i(e.toString());
        }
    }

    private void configCaptureSession() {

        try {
            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            l.i(e.toString());
        }
    }


    // l.i("onImageAvailable  (w,h)" + image.getWidth()
    // + " " + image.getHeight() + " plane : " + i +
    //  " pixelStride: " + pixelStride + "  rowStride: " + rowStride +
    //  "  actualBufferSize--" + actualBufferSize);
    //setup image reader
    private H26xEnCode h26xEnCode;

    private void setUpImageReader() {

        h26xEnCode = new H26xEnCode(H26xEnCode.H265,
                suitablePreviewSize.getWidth(),
                suitablePreviewSize.getHeight(), server);

        imageReader = ImageReader.newInstance(suitablePreviewSize.getWidth()
                , suitablePreviewSize.getHeight()
                , ImageFormat.YUV_420_888, 1);

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            byte[] temp = null;

            @Override
            public void onImageAvailable(ImageReader reader) {

                //int imageFormat = reader.getImageFormat();
                //l.i("onImageAvailable  imageFormat : " + imageFormat);

                Image image = reader.acquireNextImage();

                //yuv420(yy uu vv)->NV12(yyy uv uv)
                temp = YUV420ToNV21(image);

                //根据实际情况rotate bytes
                temp = YUVUtil.YUV420BytesClockwise90Rotate(temp, suitablePreviewSize.getWidth(), suitablePreviewSize.getHeight(), YUVUtil.NV12);
                //temp = YUVUtil.YUV420BytesClockwise90Rotate(temp, suitablePreviewSize.getHeight(), suitablePreviewSize.getWidth(), YUVUtil.NV12);

                //验证yuv图片
                //FileUtil.writeEncodeBytes(temp, System.currentTimeMillis() + ".yuv");

                //送去编码
                h26xEnCode.startEncode(temp);

                image.close();

            }
        }, null);
    }

    /**
     * 判断手机角度
     * int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
     * <p>
     * 判断横屏还是竖屏
     * int orientation = getResources().getConfiguration().orientation;
     * if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
     * mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
     * } else {
     * mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
     * }
     */
    //竖屏为准
    private Size getSuitablePreviewSize(int width, int height, Size[] choiceSizes) {

        //把高定为最大的一边
        int h, w, h_, w_;
        h_ = Math.max(width, height);
        w_ = Math.min(width, height);

        //最大可接受的size 宽高相差大小
        //int maxAcceptSizeDiscrepancy = 20;
        //List<Size> arrayList = new ArrayList<>();
        boolean fromBigToSmall = true;

        if (choiceSizes.length > 1) {
            fromBigToSmall = choiceSizes[0].getHeight() > choiceSizes[1].getHeight();
        }


        //从最大的size开始查询
        if (!fromBigToSmall) {
            for (int i = choiceSizes.length - 1; i >= 0; i--) {

                h = Math.max(choiceSizes[i].getWidth(), choiceSizes[i].getHeight());
                w = Math.min(choiceSizes[i].getWidth(), choiceSizes[i].getHeight());

                //先找比例尺寸相同的 再找尺寸最接近的
                if (w / h == width / height && w == width && h == height) {
                    l.i("找到了与宽高size ：" + new Size(width, height).toString() + " 比例相同的且接近的size : " + choiceSizes[i].toString());
                    return choiceSizes[i];
                } else if (h < h_ && w < w_) {
                    l.i("找到了一个与宽高size ：" + new Size(width, height).toString() + "  相近的size : " + choiceSizes[i].toString());
                    return choiceSizes[i];
                }
            }
        } else {
            int i = 0;
            while (i < choiceSizes.length) {

                h = Math.max(choiceSizes[i].getWidth(), choiceSizes[i].getHeight());
                w = Math.min(choiceSizes[i].getWidth(), choiceSizes[i].getHeight());

                //先找比例尺寸相同的 再找尺寸最接近的
                if (w / h == width / height && w == width && h == height) {
                    l.i("找到了与宽高size ：" + new Size(width, height).toString() + " 比例相同的且接近的size : " + choiceSizes[i].toString());
                    return choiceSizes[i];
                } else if (h < h_ && w < w_) {
                    l.i("找到了一个与宽高size ：" + new Size(width, height).toString() + "  相近的size : " + choiceSizes[i].toString());
                    return choiceSizes[i];
                }
                i++;
            }
        }

        Size notNiceSize = choiceSizes[choiceSizes.length - 1];
        l.i("没找到与宽高size ：" + new Size(width, height).toString() + "  合适的size,被迫返回size : " + notNiceSize.toString());
        return notNiceSize;
    }


    /**
     * 官方的保证：https://developer.android.google.cn/reference/android/graphics/ImageFormat?hl=en#YUV_420_888
     * Image#getPlanes()保证由数组返回的平面的顺序 使得平面＃0始终为Y，平面＃1始终为U（Cb），平面＃2始终为V（Cr）。
     * 保证Y平面不与U / V平面交错（特别是像素步长始终为1 in yPlane.getPixelStride()）。
     * 保证U / V平面具有相同的行跨度和像素跨度（尤其是 uPlane.getRowStride() ==vPlane.getRowStride()
     * 和 uPlane.getPixelStride() == vPlane.getPixelStride();）。
     *
     * @param image
     * @return
     */
    private byte[] YUV420ToNV21(Image image) {

        byte[] temp = new byte[image.getWidth() * image.getHeight() * 3 / 2];

        Image.Plane[] planes = image.getPlanes();

        if (planes.length != 3)
            return null;

        ByteBuffer YBuffer = planes[0].getBuffer();
        int YBufferActualSize = YBuffer.remaining();

        ByteBuffer UBuffer = planes[1].getBuffer();
        int UBufferActualSize = UBuffer.remaining();

        ByteBuffer VBuffer = planes[2].getBuffer();
        //int VBufferActualSize = VBuffer.remaining();

        //copy y 数据
        YBuffer.get(temp, 0, YBufferActualSize);

        //有google官方保证 所以 vu vu copy

        //pixel的步长 U-plane和V-plane的pixelStride则不固定(所谓pixelStride是指连续的码流中有效位的偏移，
        // 1表示数据是紧凑的，连续有效，中间不存在无效数据)
        int pixelStride = planes[1].getPixelStride();

        //一行有多少个数据
        //int rowStride = planes[1].getRowStride();

        //UBufferActualSize==VBufferActualSize

        //验证dsp支持NV12 uv uv uv不是NV21
        int start = YBufferActualSize;
        for (int i = 0; i < UBufferActualSize; i += pixelStride) {

            //u
            temp[start++] = UBuffer.get(i);

            //v
            temp[start++] = VBuffer.get(i);

        }

        //FileUtil.writeEncodeBytes(temp, System.currentTimeMillis() + ".yuv");
        //l.i("YUV420ToNV21 start : " + start + "  temp length: " + temp.length);
        //l.i("YUV420ToNV21 V : " + Arrays.toString(temp));
        return temp;
    }

    private void closeCamera() {

        closeCaptureSession();

        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }

        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    private void closeCaptureSession() {
        if (null != captureSession) {
            captureSession.close();
            captureSession = null;
        }
    }


    //Socket
    private static final int PORT = 9876;
    private Server server;

    private void initAndOpenSocket(int port) {

        server = new Server(port);

        //开启服务端
        server.startMe();


    }

    @Override
    protected void onDestroy() {

        try {

            if (null != server)
                server.closeMe();

            if (null != h26xEnCode)
                h26xEnCode.closeMe();

            closeCamera();

        } catch (Exception e) {
            l.i(e.toString());
        }

        super.onDestroy();
    }


}
