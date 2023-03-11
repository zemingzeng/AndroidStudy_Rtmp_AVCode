//
// Created by Admin on 2020/12/2.
//
#include "../../head/jpeg/play.h"


extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_MainActivity_play(JNIEnv *env, jclass clazz) {
    // TODO: implement play()
    __android_log_print(ANDROID_LOG_INFO, "22m", "name : %s", "jni测试成功！");
    std::string data;
    data.data();
}
typedef uint8_t BYTE;
typedef uint32_t INT;

void jpeg_compress(BYTE *data, const char *path, int w, int h);

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_bitmap_1compress_MyActivity_bitmapCompress(JNIEnv *env, jclass clazz,
                                                             jobject bitmap, jstring _path) {
    const char *path = env->GetStringUTFChars(_path, 0);


    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, bitmap, &info);


    BYTE *pBitmap;
    AndroidBitmap_lockPixels(env, bitmap, (void **) &pBitmap);

    int w = info.width;
    int h = info.height;


    BYTE R, G, B;

    BYTE *data = (BYTE *) malloc(w * h * 3);
    //获取像素成功
    if (pBitmap) {

        INT color;
        //A R G B 0x00 00 00 00
        for (int i = 0; i < w * h; ++i) {

            color = *((int *) pBitmap);
            R = (color >> 16) & 0xff;
            G = (color >> 8) & 0xff;
            B = color & 0xff;

            //装数据 jpeg 规则 倒着放 B G R A
            *data = B;
            *(data + 1) = G;
            *(data + 2) = R;
            data += 3;

            pBitmap += 4;

        }

    }

    AndroidBitmap_unlockPixels(env, bitmap);

    //返回首地址
    data -= w * h * 3 ;
    //哈夫曼压缩
    jpeg_compress(data, path, w, h);

    env->ReleaseStringUTFChars(_path, path);
}

void jpeg_compress(BYTE *data, const char *path, int w, int h) {

    jpeg_compress_struct jpeg_struct;

    jpeg_error_mgr error;
    jpeg_struct.err = jpeg_std_error(&error);

    //创建结构体分配内存
    jpeg_create_compress(&jpeg_struct);

    FILE *file = fopen(path, "wb");
    //设置输出文件
    jpeg_stdio_dest(&jpeg_struct, file);

    jpeg_struct.image_width = w;
    jpeg_struct.image_height = h;
    // red green blue
    jpeg_struct.in_color_space = JCS_RGB;
    //位深 3 R G B
    jpeg_struct.input_components = 3;
    // TRUE=arithmetic coding, FALSE=Huffman
    jpeg_struct.arith_code = FALSE;
    jpeg_struct.optimize_coding = TRUE;

    //其他参数默认
    jpeg_set_defaults(&jpeg_struct);
    //设置压缩质量（20~40效果最佳）
    jpeg_set_quality(&jpeg_struct, 20, TRUE);

    //开始准备压缩了
    jpeg_start_compress(&jpeg_struct, TRUE);

    JSAMPROW row_pointer[1];
    int row_stride = w * 3;
    //一行一行的写入
    while (jpeg_struct.next_scanline < h) {
        //读一行
        row_pointer[0] = &data[jpeg_struct.next_scanline * row_stride];
        //写一行
        jpeg_write_scanlines(&jpeg_struct, row_pointer, 1);
    }

    //释放资源
    jpeg_finish_compress(&jpeg_struct);
    jpeg_destroy_compress(&jpeg_struct);
    fclose(file);

}
