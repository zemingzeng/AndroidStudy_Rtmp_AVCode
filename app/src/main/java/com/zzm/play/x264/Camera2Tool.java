package com.zzm.play.x264;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.zzm.play.utils.YUVUtil;
import com.zzm.play.utils.l;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.hardware.camera2.CameraDevice.*;

public class Camera2Tool {

    static {

        System.loadLibrary("x264_camera2");

    }

    private Context c;

    private AudioRecorderTool audioRecorderTool;

    public Camera2Tool(Context c) {

        this.c = c;

    }


    private TextureView textureView;

    public void setDisplay(TextureView textureView) {
        this.textureView = textureView;
        textureView.setSurfaceTextureListener(new MySurfaceTextureListener());
    }

    class MySurfaceTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {

            l.i("onSurfaceTextureAvailable  width  height:" + "(" + width + " , " + height + ")");

            surfaceTexture = surface;

            previewViewSize = new Size(width, height);

            try {
                openCamera();
            } catch (Exception e) {
                l.i(e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            l.i("onSurfaceTextureSizeChanged");

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
    }

    private Handler camera2Handle;
    private HandlerThread camera2HandleThread;
    private CameraManager cameraManager;
    private SurfaceTexture surfaceTexture;
    //texture view的size
    private Size previewViewSize;
    //camera 给到的支持的最合适的尺寸
    private static Size suitablePreviewSize;
    private static int sensorOrientation;

    @SuppressLint("MissingPermission")
    public void openCamera() throws CameraAccessException {

        //初始化x264
        nativeX264EncodeInit();

        //初始化faac 打开编码器
        audioRecorderTool = new AudioRecorderTool(44100, 1);

        //链接服务器做好准备
        nativeRtmpstart(url);

        //testX264("hello x264");
        cameraManager = (CameraManager) c.getSystemService(Context.CAMERA_SERVICE);

        String[] cameraIds = cameraManager.getCameraIdList();
        for (String cameraId : cameraIds) {
            l.i("have camera id : " + cameraId);
        }

        String backMainCameraId = "-1";
        CameraCharacteristics characteristics = null;
        //取得后置主相头的camera id
        for (String cameraId : cameraIds) {
            characteristics = cameraManager.getCameraCharacteristics(cameraId);
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                backMainCameraId = cameraId;
                l.i("backMainCameraId : " + backMainCameraId);
                break;
            }
        }
        if (null == backMainCameraId || backMainCameraId.equals("-1"))
            return;

        //camera sensor 角度一般是90度所以宽高的对换一下
        sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        l.i("sensorOrientation : " + sensorOrientation);
        if (sensorOrientation == 90) {
            previewViewSize = new Size(previewViewSize.getHeight(), previewViewSize.getWidth());
        }

        //获取支持的尺寸
        StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] outputSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
        for (Size outputSize : outputSizes) {
            l.i("StreamConfigurationMap获取的支持的尺寸 ： " + outputSize.toString());
        }

        //找到最合适的支持尺寸
        suitablePreviewSize = getSuitablePreviewSize(outputSizes, previewViewSize);

        //打开相机后续有耗时操作在子线程中运行，camera2Handle的作用就是把初始化的looper给到camera仅此而以
        //这个looper其实是在camera2HandleThread run方法中创建，其实最终运行在线程camera2HandleThread
        //的run方法中
        camera2HandleThread = new HandlerThread("camera2-preview");
        camera2HandleThread.start();
        camera2Handle = new Handler(camera2HandleThread.getLooper(), null);
        cameraManager.openCamera(backMainCameraId, new MyStateCallBack(), camera2Handle);

    }

    private Size getSuitablePreviewSize(Size[] outputSizes, Size previewViewSize) {

        int outWidth = 0;
        int outHeight = 0;

        int width = previewViewSize.getWidth();
        int height = previewViewSize.getHeight();

        //从支持的大尺寸到小尺寸地开始循环
        for (Size outputSize : outputSizes) {

            outWidth = outputSize.getWidth();
            outHeight = outputSize.getHeight();

            if (outWidth == width && outHeight == height) {
                //完全相同的尺寸
                break;
            } else if (outWidth / width == outHeight / height && outWidth < width && outHeight < height) {
                //比率相同的尺寸
                break;
            } else if (outWidth < width && outHeight < height) {
                //比view尺寸最接近且小于的尺寸
                break;
            }

        }

        Size size = new Size(outWidth, outHeight);
        l.i("找到的最合适的尺寸 ： " + size.toString());
        return size;
    }

    private CameraDevice cameraDevice;

    class MyStateCallBack extends StateCallback {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            l.i("MyStateCallBack onOpened ");

            cameraDevice = camera;

            //创建和camera的预览会话
            createCamera2PreviewSession();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            l.i("CameraDevice StateCallBack  onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            l.i("CameraDevice StateCallBack  onError");
        }
    }


    private void createCamera2PreviewSession() {

        try {
            cameraDevice.createCaptureSession(getSurfaceList(), new MyStateCallBack1(), camera2Handle);
        } catch (CameraAccessException e) {
            l.i(e.toString());
            e.printStackTrace();
        }

    }

    private Surface previewSurface;
    private ImageReader imageReader;

    private List getSurfaceList() {

        //需要把预览数据装载在哪些surface里面
        List<Surface> list = new ArrayList<>();

        //预览的view
        //surfaceTexture.setDefaultBufferSize();
        list.add(previewSurface = new Surface(surfaceTexture));

        //image reader
        imageReader = createImageReader();

        //得到每帧的yuv数据的image reader的surface
        list.add(imageReader.getSurface());

        return list;

    }

    private ImageReader createImageReader() {

        // maxImages The maximum number of images the user will want to
        // access simultaneously. This should be as small as possible to
        // limit memory use. Once maxImages Images are obtained by the
        // user, one of them has to be released before a new Image will
        // become available for access through
        ImageReader imageReader = ImageReader.newInstance(suitablePreviewSize.getWidth(),
                suitablePreviewSize.getHeight(),
                ImageFormat.YUV_420_888,
                1);

        imageReader.setOnImageAvailableListener(new MyImageAvailableListener(), camera2Handle);

        return imageReader;
    }

    private CameraCaptureSession previewSession;

    class MyStateCallBack1 extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {

            l.i("CameraCaptureSession.StateCallback  onConfigured");

            previewSession = session;


            //会话配置好了 开始预览了
            startPreview();

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            l.i("CameraCaptureSession.StateCallback  onConfigureFailed");
        }
    }

    private void startPreview() {

        try {

            createAndConfigurePreviewSessionRequest();

            configurePreviewSession();

            //打开x264编码器
            if (sensorOrientation == 90) {
                nativeX264EncodeOpen(suitablePreviewSize.getHeight(),
                        suitablePreviewSize.getWidth(),
                        10, suitablePreviewSize.getWidth() * suitablePreviewSize.getHeight());
            }

            //开始录音
            audioRecorderTool.startRecording();

        } catch (CameraAccessException e) {
            l.i(e.toString());
            e.printStackTrace();
        }


    }


    private CaptureRequest.Builder previewRequest;

    private void createAndConfigurePreviewSessionRequest() throws CameraAccessException {

        //TEMPLATE_PREVIEW 预览
        previewRequest = cameraDevice.createCaptureRequest(TEMPLATE_PREVIEW);
        //自动对焦等
        previewRequest.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        //AF_MODE_CONTINUOUS_PICTURE：快速持续聚焦，用于静态图片的ZSL捕获。一旦达到扫描目标，触发则立即锁住焦点。取消而继续持续聚焦。
        //previewRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        previewRequest.addTarget(previewSurface);
        previewRequest.addTarget(imageReader.getSurface());

    }

    private void configurePreviewSession() throws CameraAccessException {

        //一直预览 不是只预览一帧的画面
        previewSession.setRepeatingRequest(previewRequest.build(), null, camera2Handle);

    }


    static  class MyImageAvailableListener implements ImageReader.OnImageAvailableListener {

        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = reader.acquireNextImage();

            Image.Plane[] planes = image.getPlanes();

            //处理planes里面的数据
            dealPlanes(planes);

            image.close();
        }
    }

    private static void dealPlanes(Image.Plane[] planes) {

        //3
        //l.i("camera preview Image.Plane[] planes number : "+planes.length);

        Image.Plane y = planes[0];
        Image.Plane u = planes[1];
        Image.Plane v = planes[2];
        ByteBuffer yBuffer = y.getBuffer();
        ByteBuffer uBuffer = u.getBuffer();
        ByteBuffer vBuffer = v.getBuffer();

        //l.i("Y planes[0] length : "+yBuffer.remaining());
        //l.i("U planes[1] length : "+uBuffer.remaining());
        //l.i("V planes[2] length : "+vBuffer.remaining());


        int yLength = yBuffer.remaining();
        //yuv420 转成 yu12
        byte[] temp = new byte[yLength * 3 / 2];
        //填充y
        yBuffer.get(temp, 0, yBuffer.remaining());
        //填充u
        //1 没有无效数据，2 数据之间有一个无效数据
        int pixelStride = u.getPixelStride();
        int index = yLength;
        for (int i = 0; i < uBuffer.remaining(); i += pixelStride) {
            temp[index++] = uBuffer.get(i);
        }
        //填充v
        pixelStride = v.getPixelStride();
        for (int i = 0; i < vBuffer.remaining(); i += pixelStride) {
            temp[index++] = vBuffer.get(i);
        }

        if (sensorOrientation == 90) {
            temp = YUVUtil.YUV420BytesClockwise90Rotate(temp, suitablePreviewSize.getWidth(),
                    suitablePreviewSize.getHeight(), YUVUtil.YUV420p);
            //FileUtil.writeEncodeBytes(temp, System.currentTimeMillis() + ".yuv");
        }

        //发送yu12数据给x264
        nativeSendPreviewData(temp, temp.length);

    }

    public void release() {

        if (null != previewSession) {
            previewSession.close();
            previewSession = null;
        }

        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }

        if (null != previewRequest) {
            previewRequest = null;
        }

        if (null != previewSurface) {
            previewSurface.release();
            previewSurface = null;
        }

        if (null != camera2Handle) {
            camera2Handle = null;
        }

        if (null != camera2HandleThread) {
            camera2HandleThread.quitSafely();
            camera2HandleThread = null;
        }

        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }

        if (null != audioRecorderTool) {
            audioRecorderTool.releaseMe();
            audioRecorderTool = null;
        }

        nativeStop();

        nativeRelease();

    }

    //jni 回调的方法 回调编码好的h264数据
    private void getDataFromJni(byte[] data) {

        l.i("getDataFromJni data length : " + data.length);
        //FileUtil.writeBytesTo16Chars(data, "xh264.h264");

    }

    private native void testX264(String a);

    //初始化
    private native void nativeX264EncodeInit();


    //打开X264，准备编码 //需要宽高
    private native void nativeX264EncodeOpen(int width, int height, int fps, int bitRate);

    //发送preview yuv数据给x264编码
    private static native void nativeSendPreviewData(byte[] data, int dataLength);

//    private final String url = "rtmp://192.168.71.129:8080/mingzz_live/xxx";
    private final String url = "rtmp://ifast3.vipnps.vip:19837/mingzz_live/xxx";
//    private final String url = "rtmp://live-push.bilivideo.com/live-bvc/?streamname=live_479017059_73139358&key=9d275e91d11ad2ac32e99e15763039fb&schedule=rtmp&pflag=1";

    //开始子线程链接服务器，然后取队列中的数据发送
    private native void nativeRtmpstart(String url);

    //stop
    private native void nativeStop();

    //release
    private native void nativeRelease();

}
