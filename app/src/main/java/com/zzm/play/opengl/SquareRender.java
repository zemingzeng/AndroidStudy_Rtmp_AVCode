package com.zzm.play.opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class SquareRender extends BaseRender {

    //coordinate clockwise
    private final float[] coords = new float[]{
            0f, 0.5f, 0f,
            1f, 0.5f, 0f,
            0.5f, -0.5f, 0f,
            -0.5f, -0.5f, 0f
    };

    private final float[] colors = {
            0.5f, 0f, 0f, 1.0f,//红对应第1个顶点
            0f, 0.5f, 0f, 1.0f,//绿对应第2个顶点
            0f, 0f, 0.5f, 1.0f,//蓝对应第3个顶点
            1f, 1f, 1f, 1.0f //应第4个顶点
    };

    //指定顶点coords顺序来画三角形
   private final byte[] vertexOrder=new byte[]{
     0,1,2,0,2,3
   };

    private FloatBuffer vertexFloatBuffer;
    private FloatBuffer colorsFloatBuffer;
    private ByteBuffer vertexOrderByteBuffer;

    @Override
    public void created() {

        //ByteOrder.nativeOrder()
        //返回本地jvm运行的硬件的字节顺序.使用和硬件一致的字节顺序可能使buffer更加有效
        //float 4字节
        ByteBuffer vertexBB = ByteBuffer.allocateDirect(coords.length * 4);
        vertexBB.order(ByteOrder.nativeOrder());
        vertexFloatBuffer = vertexBB.asFloatBuffer();
        vertexFloatBuffer.put(coords);
        //to read the first coord
        vertexFloatBuffer.position(0);

        vertexBB = ByteBuffer.allocateDirect(colors.length * 4);
        vertexBB.order(ByteOrder.nativeOrder());
        colorsFloatBuffer = vertexBB.asFloatBuffer();
        colorsFloatBuffer.put(colors);
        //to read the first coord
        colorsFloatBuffer.position(0);

        vertexBB = ByteBuffer.allocateDirect(vertexOrder.length * 4);
        vertexBB.order(ByteOrder.nativeOrder());
        vertexOrderByteBuffer = vertexBB;
        vertexOrderByteBuffer.put(vertexOrder);
        //to read the first coord
        vertexOrderByteBuffer.position(0);

        String vertexShaderCode =
                "attribute vec4 vPosition;" +
                        "varying vec4 colors;" +
                        "attribute vec4 vColor;" +
                        "void main(){" +
                        "gl_Position=vPosition;" +
                        "colors=vColor;" +
                        "}";

        //创建个顶点shader
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        //顶点shader的code
        GLES20.glShaderSource(vertexShader, vertexShaderCode);

        String fragmentShaderCode =
                "precision mediump float;" +
                        "varying vec4 colors;" +
                        "void main(){" +
                        "gl_FragColor=colors;" +
                        "}";
        //创建个片元shader
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);

        //编译
        GLES20.glCompileShader(vertexShader);
        GLES20.glCompileShader(fragmentShader);

        //创建program
        program = GLES20.glCreateProgram();

        //program连接shader
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        //creates OpenGL ES program executables
        GLES20.glLinkProgram(program);
    }

    @Override
    public void changed(int width, int height) {


    }

    private int program;

    @Override
    public void draw() {

        //使用program executables
        GLES20.glUseProgram(program);


        //把cpu的数据给到gpu的program
        //就是找到gpu中program的变量把cpu参数传递给它
        int vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        //让它准备接收数据
        GLES20.glEnableVertexAttribArray(vPosition);
        //学习地址：https://learnopengl-cn.github.io/01%20Getting%20started/04%20Hello%20Triangle/?
        //size:一个点有（x,y,z）则为3
        //normalized:被设置为GL_TRUE，意味着整数型的值会被映射至区间-1,1，
        // 或者区间[0,1]（无符号整数），反之，这些值会被直接转换为浮点值而不进行归一化处理
        //stride :指定连续顶点属性之间的偏移量(x,y,z float)*4
        GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexFloatBuffer);


        int vColor = GLES20.glGetAttribLocation(program, "vColor");
        GLES20.glEnableVertexAttribArray(vColor);
        GLES20.glVertexAttribPointer(vColor, 4, GLES20.GL_FLOAT, false, 4 * 4, colorsFloatBuffer);


        //开始绘制三角形
        //glDrawArrays函数第一个参数是我们打算绘制的OpenGL图元的类型。
        // 希望绘制的是一个三角形，这里传递GL_TRIANGLES给它。
        // 第二个参数指定了顶点数组的起始索引，我们这里填0。
        // 最后一个参数指定我们打算绘制多少个顶点，这里是3（只从数据中渲染一个三角形，它只有3个顶点长）。
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexOrder.length, GLES20.GL_UNSIGNED_BYTE,vertexOrderByteBuffer);


        //禁用顶点位置变量id，一般绘制结束都需要禁用
        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(vColor);
    }
}
