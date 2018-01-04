
/**
 * Created by liuxuetao on 12/28/16.
 */


package com.starmaker.albumkit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.provider.MediaStore;
import android.util.Log;

import com.starmaker.albumkit.R;


public class AlbumKitRender implements GLSurfaceView.Renderer {

    /*vertex shader 字符串*/
    private final static String vShaderStr =
            "attribute vec4 a_position;\n" +

                    "attribute vec2 a_texCoord;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "void main()\n" +
                    "{\n" +
                    "   gl_Position = a_position;\n" +
                    "   v_texCoord = a_texCoord;\n" +
                    "}\n";

    /*frage shader字符串*/
    private final static String fZoomShaderStr =
            "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform sampler2D s_baseMap;\n" +
                    //      "uniform sampler2D s_maskMap;\n" +
                    "uniform sampler2D s_bgMap;\n" +
                    "uniform float uT;\n" +
                    "uniform float u_color;\n" +
                    "void main()\n" +
                    "{\n" +
                    "  vec4 baseColor;\n" +
                    //      "  vec4 maskColor;\n" +
                    "  vec4 bgColor;\n" +
                    //    "  maskColor = texture2D(s_maskMap, v_texCoord);\n" +
                    // "  baseColor = texture2D(s_baseMap, v_texCoord);\n" +
                    "float prefix = 1.0 - 0.3*uT;\n" +
                    "baseColor = texture2D(s_baseMap, prefix*v_texCoord);\n" +
                    "bgColor = texture2D(s_bgMap, v_texCoord);\n" +
                    "gl_FragColor = vec4(mix(baseColor.rgb, bgColor.rgb, bgColor.a * uT), baseColor.a);\n" +
                    "}\n";


    private final static String fLtoRShaderStr =
            "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform sampler2D s_baseMap;\n" +
                    //      "uniform sampler2D s_maskMap;\n" +
                    "uniform sampler2D s_bgMap;\n" +
                    "uniform float uT;\n" +
                    "uniform int u_type;\n" +

                    "void main()\n" +
                    "{\n" +
                    "  vec4 baseColor;\n" +
                    "  vec4 maskColor;\n" +
                    "  vec4 bgColor;\n" +
                    "  float  fPos ;\n" +
                    "\n" +
                    //    "  maskColor = texture2D(s_maskMap, v_texCoord);\n" +
                    //   "if(u_type <= 1)\n"+
                    "{\n" +
                    "if(v_texCoord.x < uT)\n" +
                    "{\n" +
                    "  fPos = 1.0-uT;\n" +
                    "  bgColor = texture2D(s_bgMap, vec2((fPos+v_texCoord.x),v_texCoord.y));\n" +
                    "  baseColor = texture2D(s_baseMap, v_texCoord);\n" +
                    // "  bgColor = texture2D(s_bgMap, v_texCoord);\n" +
                    "  gl_FragColor =bgColor;\n" +
                    "}\n" +
                    "else\n" +
                    "{\n" +
                    "  baseColor = texture2D(s_baseMap, v_texCoord);\n" +
                    "  bgColor = texture2D(s_bgMap, v_texCoord);\n" +
                    "  gl_FragColor = baseColor;\n" +
                    "}\n" +
                    "}\n" +

                    "}\n";

    private final static String fRtoLShaderStr =
            "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform sampler2D s_baseMap;\n" +
                    //      "uniform sampler2D s_maskMap;\n" +
                    "uniform sampler2D s_bgMap;\n" +
                    "uniform float uT;\n" +
                    "uniform int u_type;\n" +

                    "void main()\n" +
                    "{\n" +
                    "  vec4 baseColor;\n" +
                    //    "  vec4 maskColor;\n" +
                    "  vec4 bgColor;\n" +
                    "  float  fPos ;\n" +
                    "\n" +
                    //    "  maskColor = texture2D(s_maskMap, v_texCoord);\n" +
                    //   "if(u_type <= 1)\n"+
                    "{\n" +
                    "  fPos = 1.0-uT;\n" +
                    "if(v_texCoord.x < fPos)\n" +
                    "{\n" +

                    "  baseColor = texture2D(s_baseMap, vec2((uT+v_texCoord.x),v_texCoord.y));\n" +
                    // "  baseColor = texture2D(s_baseMap, v_texCoord);\n" +
                    "  bgColor = texture2D(s_bgMap, v_texCoord);\n" +
                    "  gl_FragColor =baseColor;\n" +
                    "}\n" +
                    "else\n" +
                    "{\n" +
                    "  baseColor = texture2D(s_baseMap, v_texCoord);\n" +
                    "  bgColor = texture2D(s_bgMap, v_texCoord);\n" +
                    "  gl_FragColor = bgColor;\n" +
                    "}\n" +
                    "}\n" +

                    "}\n";

    private final static String fDtoTopShaderStr =
            "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform sampler2D s_baseMap;\n" +
                    //   "uniform sampler2D s_maskMap;\n" +
                    "uniform sampler2D s_bgMap;\n" +
                    "uniform float uT;\n" +
                    "uniform int u_type;\n" +

                    "void main()\n" +
                    "{\n" +
                    "  vec4 baseColor;\n" +
                    //   "  vec4 maskColor;\n" +
                    "  vec4 bgColor;\n" +
                    "  float  fPos ;\n" +
                    "\n" +
                    //    "  maskColor = texture2D(s_maskMap, v_texCoord);\n" +
                    "  fPos = 1.0-uT;\n" +
                    "if(v_texCoord.y < fPos)\n" +
                    "{\n" +
                    "  baseColor = texture2D(s_baseMap, vec2(v_texCoord.x,v_texCoord.y+uT));\n" +
                    // "  baseColor = texture2D(s_baseMap, v_texCoord);\n" +
                    "  bgColor = texture2D(s_bgMap, v_texCoord);\n" +
                    "  gl_FragColor =baseColor;\n" +
                    "}\n" +
                    "else\n" +
                    "{\n" +
                    "  baseColor = texture2D(s_baseMap, v_texCoord);\n" +
                    "  bgColor = texture2D(s_bgMap, v_texCoord);\n" +
                    "  gl_FragColor = bgColor;\n" +
                    "}\n" +
                    "}\n";


    private final static String fToptoDShaderStr =
            "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform sampler2D s_baseMap;\n" +
                    //     "uniform sampler2D s_maskMap;\n" +
                    "uniform sampler2D s_bgMap;\n" +
                    "uniform float uT;\n" +
                    "uniform int u_type;\n" +

                    "void main()\n" +
                    "{\n" +
                    "  vec4 baseColor;\n" +
                    "  vec4 maskColor;\n" +
                    "  vec4 bgColor;\n" +
                    "  float  fPos ;\n" +
                    "\n" +
                    //     "  maskColor = texture2D(s_maskMap, v_texCoord);\n" +
                    "  fPos = 1.0-uT;\n" +
                    "if(v_texCoord.y < uT)\n" +
                    "{\n" +
                    "  bgColor = texture2D(s_bgMap, vec2(v_texCoord.x,v_texCoord.y+fPos));\n" +
                    "  baseColor = texture2D(s_baseMap, v_texCoord);\n" +
                    // "  bgColor = texture2D(s_bgMap, v_texCoord);\n" +
                    "  gl_FragColor =bgColor;\n" +
                    "}\n" +
                    "else\n" +
                    "{\n" +
                    "  baseColor = texture2D(s_baseMap, v_texCoord);\n" +
                    "  bgColor = texture2D(s_bgMap, v_texCoord);\n" +
                    "  gl_FragColor = baseColor;\n" +
                    "}\n" +
                    "}\n";


    private final static String fPageShaderStr =
            "precision highp float;\n"+
                    "varying vec2 v_texCoord;\n"+
                    "uniform sampler2D sourceTex;\n"+
                    "uniform sampler2D s_maskMap;\n" +
                    "uniform sampler2D targetTex;\n"+
                    "uniform float time; \n"+ // Ranges from 0.0 to 1.0
                    "const float MIN_AMOUNT = -0.16;\n"+
                    "const float MAX_AMOUNT = 1.3;\n"+
                    "float amount = time * (MAX_AMOUNT - MIN_AMOUNT) + MIN_AMOUNT;\n"+
                    "const float PI = 3.141592653589793;\n"+
                    "const float scale = 512.0;\n"+
                    "const float sharpness = 3.0;\n"+
                    "float cylinderCenter = amount;\n"+
                    "float cylinderAngle = 2.0 * PI * amount;\n"+
                    "const float cylinderRadius = 1.0 / PI / 2.0;\n"+
                    "vec3 hitPoint(float hitAngle, float yc, vec3 point, mat3 rrotation) {\n"+
                    "float hitPoint = hitAngle / (2.0 * PI);\n"+
                    "point.y = hitPoint;\n"+
                    " return rrotation * point;\n"+
                    " }\n"+
                    "vec4 antiAlias(vec4 color1, vec4 color2, float distance) {\n"+
                    "distance *= scale;\n"+
                    "if (distance < 0.0) return color2;\n"+
                    "if (distance > 2.0) return color1;\n"+
                    "float dd = pow(1.0 - distance / 2.0, sharpness);\n"+
                    "return ((color2 - color1) * dd) + color1;\n"+
                    "}\n"+
                    "float distanceToEdge(vec3 point) {\n"+
                    "float dx = abs(point.x > 0.5 ? 1.0 - point.x : point.x);\n"+
                    "float dy = abs(point.y > 0.5 ? 1.0 - point.y : point.y);\n"+
                    "if (point.x < 0.0) dx = -point.x;\n"+
                    "if (point.x > 1.0) dx = point.x - 1.0;\n"+
                    "if (point.y < 0.0) dy = -point.y;\n"+
                    "if (point.y > 1.0) dy = point.y - 1.0;\n"+
                    "if ((point.x < 0.0 || point.x > 1.0) && (point.y < 0.0 || point.y > 1.0)) return sqrt(dx * dx + dy * dy);\n"+
                    "return min(dx, dy);\n"+
                    "}\n"+
                    "vec4 seeThrough(float yc, vec2 p, mat3 rotation, mat3 rrotation) {\n"+
                    "float hitAngle = PI - (acos(yc / cylinderRadius) - cylinderAngle);\n"+
                    "vec3 point = hitPoint(hitAngle, yc, rotation * vec3(p, 1.0), rrotation);\n"+
                    "if (yc <= 0.0 && (point.x < 0.0 || point.y < 0.0 || point.x > 1.0 || point.y > 1.0)) {\n"+
                    "return texture2D(targetTex, v_texCoord);\n"+
                    "}\n"+
                    "if (yc > 0.0) return texture2D(sourceTex, p);\n"+
                    "vec4 color = texture2D(sourceTex, point.xy);\n"+
                    "vec4 tcolor = vec4(0.0);\n"+
                    "return antiAlias(color, tcolor, distanceToEdge(point));\n"+
                    "}\n"+
                    "vec4 seeThroughWithShadow(float yc, vec2 p, vec3 point, mat3 rotation, mat3 rrotation) {\n"+
                    "float shadow = distanceToEdge(point) * 30.0;\n"+
                    "shadow = (1.0 - shadow) / 3.0;\n"+
                    "if (shadow < 0.0) shadow = 0.0;\n"+
                    "else shadow *= amount;\n"+
                    "vec4 shadowColor = seeThrough(yc, p, rotation, rrotation);\n"+
                    "shadowColor.r -= shadow;\n"+
                    "shadowColor.g -= shadow;\n"+
                    "shadowColor.b -= shadow;\n"+
                    "return shadowColor;\n"+
                    "}\n"+
                    "vec4 backside(float yc, vec3 point) {\n"+
                    "vec4 color = texture2D(sourceTex, point.xy);\n"+
                    "float gray = (color.r + color.b + color.g) / 15.0;\n"+
                    "gray += (8.0 / 10.0) * (pow(1.0 - abs(yc / cylinderRadius), 2.0 / 10.0) / 2.0 + (5.0 / 10.0));\n"+
                    " color.rgb = vec3(gray);\n"+
                    " return color;\n"+
                    "}\n"+
                    "vec4 behindSurface(float yc, vec3 point, mat3 rrotation) {\n"+
                    "float shado = (1.0 - ((-cylinderRadius - yc) / amount * 7.0)) / 6.0;\n"+
                    "shado *= 1.0 - abs(point.x - 0.5);\n"+
                    "yc = (-cylinderRadius - cylinderRadius - yc);\n"+
                    "float hitAngle = (acos(yc / cylinderRadius) + cylinderAngle) - PI;\n"+
                    "point = hitPoint(hitAngle, yc, point, rrotation);\n"+
                    "if (yc < 0.0 && point.x >= 0.0 && point.y >= 0.0 && point.x <= 1.0 && point.y <= 1.0 && (hitAngle < PI || amount > 0.5)){\n"+
                    "shado = 1.0 - (sqrt(pow(point.x - 0.5, 2.0) + pow(point.y - 0.5, 2.0)) / (71.0 / 100.0));\n"+
                    "shado *= pow(-yc / cylinderRadius, 3.0);\n"+
                    "shado *= 0.5;\n"+
                    "} else\n"+
                    "shado = 0.0;\n"+
                    "return vec4(texture2D(targetTex, v_texCoord).rgb - shado, 1.0);\n"+
                    "}\n"+
                    "void main(void) {\n"+
                    "const float angle = 30.0 * PI / 180.0;\n"+
                    "float c = cos(-angle);\n"+
                    "float s = sin(-angle);\n"+
                    "mat3 rotation = mat3(\n"+
                    "c, s, 0,\n"+
                    "-s, c, 0,\n"+
                    "0.12, 0.258, 1\n"+
                    ");\n"+
                    "c = cos(angle);\n"+
                    "s = sin(angle);\n"+
                    "mat3 rrotation = mat3(\n"+
                    "c, s, 0,\n"+
                    "-s, c, 0,\n"+
                    "0.15, -0.5, 1\n"+
                    ");\n"+
                    "vec3 point = rotation * vec3(v_texCoord, 1.0);\n"+
                    "float yc = point.y - cylinderCenter;\n"+
                    "if (yc < -cylinderRadius) {\n"+
                    // Behind surface
                    "gl_FragColor = behindSurface(yc, point, rrotation);\n"+
                    "return;\n"+
                    "}\n"+
                    "if (yc > cylinderRadius) {\n"+
                    // Flat surface
                    "gl_FragColor = texture2D(sourceTex, v_texCoord);\n"+
                    "return;\n"+
                    "}\n"+
                    "float hitAngle = (acos(yc / cylinderRadius) + cylinderAngle) - PI;\n"+
                    "float hitAngleMod = mod(hitAngle, 2.0 * PI);\n"+
                    "if ((hitAngleMod > PI && amount < 0.5) || (hitAngleMod > PI/2.0 && amount < 0.0)) {\n"+
                    "gl_FragColor = seeThrough(yc, v_texCoord, rotation, rrotation);\n"+
                    "return;\n"+
                    "}\n"+
                    "point = hitPoint(hitAngle, yc, point, rrotation);\n"+
                    "if (point.x < 0.0 || point.y < 0.0 || point.x > 1.0 || point.y > 1.0) {\n"+
                    "gl_FragColor = seeThroughWithShadow(yc, v_texCoord, point, rotation, rrotation);\n"+
                    "return;\n"+
                    "}\n"+
                    "vec4 color = backside(yc, point);\n"+
                    "vec4 otherColor;\n"+
                    "if (yc < 0.0) {\n"+
                    "float shado = 1.0 - (sqrt(pow(point.x - 0.5, 2.0) + pow(point.y - 0.5, 2.0)) / 0.71);\n"+
                    "shado *= pow(-yc / cylinderRadius, 3.0);\n"+
                    "shado *= 0.5;\n"+
                    "otherColor = vec4(0.0, 0.0, 0.0, shado);\n"+
                    "} else {\n"+
                    "otherColor = texture2D(sourceTex, v_texCoord);\n"+
                    "}\n"+
                    "color = antiAlias(color, otherColor, cylinderRadius - abs(yc));\n"+
                    "vec4 cl = seeThroughWithShadow(yc, v_texCoord, point, rotation, rrotation);\n"+
                    "float dist = distanceToEdge(point);\n"+
                    "gl_FragColor = antiAlias(color, cl, dist);\n"+
                    "}\n";


    private String[] filterlist = {fZoomShaderStr, fToptoDShaderStr, fZoomShaderStr, fRtoLShaderStr,
            fZoomShaderStr, fToptoDShaderStr, fZoomShaderStr, fLtoRShaderStr,fPageShaderStr};


    private final static boolean D = true;
    private final static String TAG = "AlbumKitRender";

    private static final int INCREMENT = 1;
    private static final int DELAY = 40;
    private long lastTick = 0;
    private int threadhold = 0;

    private static final int FLOAT_SIZE_BYTES = Float.SIZE / 8;
    private static final int SHORT_SIZE_BYTES = Short.SIZE / 8;

    /*位置坐标信息*/
    private static final int VERTICES_DATA_POS_OFFSET = 0;
    private static final int VERTICES_DATA_POS_SIZE = 3;   //位置信息大小

    private int[] mBaseTexture = new int[1];
    private int[] mBackTexture = new int[1];
    /*保存照片的文件*/
    private Uri[] mArrayPhotoFile;
    /*当前相册中照片的数目*/
    private int mPhotoCount;
    private int mVertexShader, mFragmentShader;


    /**
     * UV坐标是指所有的图象文件都是二维的一个平面。水平方向是U，垂直方向是V，
     * 通过这个平面的，二维的UV坐标系。我们可以定位图象上的任意一个象素。
     ***/

    private static final int VERTICES_DATA_UV_OFFSET = 3;
    private static final int VERTICES_DATA_UV_SIZE = 2;    //UV信息大小
    private static final int VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;

    private final short[] mIndicesData = {
            0, 1, 2, 0, 2, 3
    };

    private final float[] mVerticesData = {
            -1.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 0.0f
    };


    private int mProgramObject;
    private int mPositionLoc, mTexCoordLoc;
    private int mBaseMapLoc, mMaskMapLoc, mBgMapLoc;
    private int mTypeLoc;

    private int mUTMapLoc;
    private int mBaseMapTexId, mMaskMapTexId, mBgMapTexId;
    private int mWidth;
    private int mHeight;
    private int mType;
    private FloatBuffer mVertices;
    private ShortBuffer mIndices;
    private Context mContext;
    private int mFilterIndex = 0;
    private int index = 1;
    private Random random = new Random();
    private int mTextureNum = 11;
    private boolean status_start = false;
    private boolean first_run = true;
    private int mCurPhotoIndex = 0;
    private int mPrePhotoIndex = 0;
    private int mLoopStep;

    /*相册中包含的最大照片数目*/
    public static int MAX_PHOTO_NUMBER = 12;
    private float[] mProjMatrix = new float[16];

    public AlbumKitRender(Context context) {
        mContext = context;
        /*
		* public static ByteBuffer allocateDirect(int capacity)分配新的直接字节缓冲区。新缓冲区的位置将为零，其界限将为其容量，其标记是不确定的。无论它是否具有底层实现数组，其标记都是不确定的。
	参数：
		capacity - 新缓冲区的容量，以字节为单位
	返回：
		新的字节缓冲区
	抛出：
		IllegalArgumentException - 如果 capacity 为负整数
		* */

		/*
		* 对于多字节的数据在系统中的存储，通常按数据的高位和低位在系统内存中的高地址和低地址存放分为
		* 大端(big endian)和小端(little endian)两种方式
		*asFloatBuffer 方法将创建 FloatBuffer 类的一个实
		* */
        mVertices = ByteBuffer.allocateDirect(mVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
		/*将mVerticesData数据放入mVertices，并将位置设为起始位置*/
        mVertices.put(mVerticesData).position(0);

		/*同上理*/
        mIndices = ByteBuffer.allocateDirect(mIndicesData.length * SHORT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
		/*将mIndicesData数据放入mIndices,并将位置设为起始位置*/
        mIndices.put(mIndicesData).position(0);
        mArrayPhotoFile = new Uri[MAX_PHOTO_NUMBER];
        mPhotoCount = 0;
        mLoopStep = 100;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        initTexture();
        first_run = true;
		/*它是通过glClear使用红，绿，蓝以及AFA值来清除颜色缓冲区的，并且都被归一化在（0，1）之间的值，其实就是清空当前的所有颜色*/
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        lastTick = System.currentTimeMillis();
    }

    public void setAlbumPhotoCount(int count) {
        mPhotoCount = count;
    }

    public void setAlbumResolution(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int addPhotoInAlbum(Uri photoUri) throws Exception {
        /*超出允许的最大照片数，抛出异常*/
        if (mPhotoCount >= MAX_PHOTO_NUMBER) {
            throw new Exception("Beyond the range of photo number");

        }
        /*保存路径，返回当前照片数*/
        mArrayPhotoFile[mPhotoCount] = photoUri;
        mPhotoCount++;
        return mPhotoCount;
    }


    public void watchAlbum() {
        status_start = true;
        first_run = true;

    }

    public void setFilterType() {

        	/*加载vertext shader和fragement shader*/
        if (mFilterIndex % 3 == 0) {
            loadProgram(vShaderStr, filterlist[mFilterIndex]);
		/*获取shader中变量的位置*/
            mPositionLoc = GLES20.glGetAttribLocation(mProgramObject, "a_position");
            mTexCoordLoc = GLES20.glGetAttribLocation(mProgramObject, "a_texCoord");
            mBaseMapLoc = GLES20.glGetUniformLocation(mProgramObject, "s_baseMap");
            mBgMapLoc = GLES20.glGetUniformLocation(mProgramObject, "s_bgMap");
            mUTMapLoc = GLES20.glGetUniformLocation(mProgramObject, "uT");
        }

        if (mFilterIndex < 26)
            mFilterIndex++;
        else
            mFilterIndex = 0;

    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
/**
 * 调用glViewPort函数来决定视见区域，告诉OpenGL应把渲染之后的图形绘制在窗体的哪个部位。当视见区域是整个窗体时，
 * OpenGL将把渲染结果绘制到整个窗口。
 * glViewPort(x:GLInt;y:GLInt;Width:GLSizei;Height:GLSizei);
 * 其中，参数X，Y指定了视见区域的左下角在窗口中的位置，一般情况下为（0，0），Width和Height指定了视见区域的宽度和高度。
 * 注意OpenGL使用的窗口坐标和WindowsGDI使用的窗口坐标是不一样的。
 **/


        GLES20.glViewport(0, 0, mWidth, mHeight);
        float ratio = (float) width / height;

/*
* 投影就是要构造一个包含所需投影物体的可视空间区域，称之为视景体，然后将视景体内的物体投影到近平面上，
* 再将近平面上投影出的内容映射到屏幕上的视口，近平面的大小与屏幕的大小对应。
* 我们主要关心的是近平面，left、right、bottom、top为近平面各侧距中心的距离。
* 屏幕区域的集合O为{-width,width,-height,height}，近平面区域的集合A（自己设定），近平面投影到屏幕上的区域的集合为B，
* 集合A中的元素x按照对应关系f“乘以height”和集合B中的元素对应，从而确定物体在屏幕中的位置。
* 屏幕向右为x正方向，向下为y正方向，
* ratio = (float) width / height;
* 1、 Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 2, 100);
* 则近平面区域的集合为A1{-ratio,ratio,-1,1}，按照对应关系得到的集合刚好为集合O，即近平面映射到屏幕后刚好为屏幕的区域；
* 2、Matrix.frustumM(mProjMatrix, 0, -0.5f*ratio, 1.5f*ratio, -1.5f, 0.5f, 2, 100);
* 则近平面区域的集合A2为{ -0.5f*ratio, 1.5f*ratio, -1.5f, 0.5f}，相当与集合A向右移到0.5个单位，再向上移动0.5个单位，
* 物体被向右移动了0.5个单位，向上移动了0.5个单位；
* 3、Matrix.frustumM(mProjMatrix, 0, -0.5f*ratio, 0.5f*ratio, -2f, 2f, 2, 100);
* 则近平面区域的集合A2为{-0.5f*ratio, 0.5f*ratio, -2f, 2f}，相当与集合A在x方面缩小了一倍，再在y方向放大了一倍，
* 因为近平面的大小是与屏幕的大小对应的，最后结果是物体在x方向被放大了一倍，在y方向被缩小了一倍；
*
* */
        Matrix.frustumM(mProjMatrix, 0, ratio, -ratio, 1, -1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        try {
            if (!status_start) {
                return;
            }
            if (first_run) {
                first_run = false;
                setFilterType();
                String mPathBack = getRealFilePath(this.mContext, mArrayPhotoFile[mCurPhotoIndex]);
                Bitmap bitmapBack = BitmapFactory.decodeFile(mPathBack);
                bindBaseTexture(bitmapBack);

                String mPathBase = getRealFilePath(this.mContext, mArrayPhotoFile[mCurPhotoIndex]);
                Bitmap bitmapBase = BitmapFactory.decodeFile(mPathBase);
                bindBackTexture(bitmapBase);
            }
            final long ct = System.currentTimeMillis();
            final long time = ct - lastTick;
            if (time < DELAY) {
                java.lang.Thread.sleep(DELAY - time);
                //System.Threading.Thread.Sleep(DELAY - time);
            }

            lastTick = System.currentTimeMillis();
            threadhold += INCREMENT;
            if (threadhold > mLoopStep - 1) {
                threadhold = 0;
                setFilterType();
                String mPathBack = getRealFilePath(this.mContext, mArrayPhotoFile[mCurPhotoIndex]);
                Bitmap bitmapBack = BitmapFactory.decodeFile(mPathBack);
                bindBaseTexture(bitmapBack);

                if (mCurPhotoIndex < mPhotoCount - 1)
                    mCurPhotoIndex++;
                else
                    mCurPhotoIndex = 0;
                String mPathBase = getRealFilePath(this.mContext, mArrayPhotoFile[mCurPhotoIndex]);
                Bitmap bitmapBase = BitmapFactory.decodeFile(mPathBase);
                bindBackTexture(bitmapBase);

            }

            //设置视口和颜色信息
            GLES20.glViewport(0, 0, mWidth, mHeight);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            //Log.e(TAG, "value of program ID is:" + mProgramObject);
            GLES20.glUseProgram(mProgramObject);
            checkGlError("glUseProgram");
            // Log.e(TAG, "use program successful");
            mVertices.position(VERTICES_DATA_POS_OFFSET);
                /*
                * void glVertexAttribPointer( GLuint index,
                * GLint size, GLenum type, GLboolean normalized, GLsizei stride,const GLvoid * pointer);
                *参数：
                *index
                *指定要修改的顶点属性的索引值
                *size
                *指定每个顶点属性的组件数量。必须为1、2、3或者4。初始值为4。（如position是由3个（x,y,z）组成，而颜色是4个（r,g,b,a））
                *type
                *指定数组中每个组件的数据类型。可用的符号常量有GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT,GL_UNSIGNED_SHORT,
                * GL_FIXED, 和 GL_FLOAT，初始值为GL_FLOAT。
                *normalized
                *指定当被访问时，固定点数据值是否应该被归一化（GL_TRUE）或者直接转换为固定点值（GL_FALSE）。
                *stride
                *指定连续顶点属性之间的偏移量。如果为0，那么顶点属性会被理解为：它们是紧密排列在一起的。初始值为0。
                *pointer
                *指定第一个组件在数组的第一个顶点属性中的偏移量。该数组与GL_ARRAY_BUFFER绑定，储存于缓冲区中。初始值为0；
                *
                * */

            GLES20.glVertexAttribPointer(mPositionLoc,
                    VERTICES_DATA_POS_SIZE, GLES20.GL_FLOAT, false,
                    VERTICES_DATA_STRIDE_BYTES, mVertices);
            checkGlError("glVertexAttribPointer mPositionLoc");
            GLES20.glEnableVertexAttribArray(mPositionLoc);
            checkGlError("glEnableVertexAttribArray mPositionLoc");

            mVertices.position(VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(mTexCoordLoc,
                    VERTICES_DATA_UV_SIZE, GLES20.GL_FLOAT, false,
                    VERTICES_DATA_STRIDE_BYTES, mVertices);
            checkGlError("glVertexAttribPointer mTexCoordLoc");
            GLES20.glEnableVertexAttribArray(mTexCoordLoc);
            checkGlError("glEnableVertexAttribArray mTexCoordLoc");
            //选择可以由纹理函数进行修改的当前纹理单位，
            //texUnit是一个符号常量，其形式为GL_TEXTUREi，其中i的范围是从0到k-1，k是纹理单位的最大数量*/
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                /*允许建立一个绑定到目标纹理的有名称的纹理。*/
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBaseMapTexId);
                 /*glUniform1i进行纹理层和采样器地址进行绑定*/
            GLES20.glUniform1i(mBaseMapLoc, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBgMapTexId);
            GLES20.glUniform1i(mBgMapLoc, 1);

    /*
    * 为当前程序对象指定Uniform变量的值。（译者注：注意，由于OpenGL ES由C语言编写，但是C语言不支持函数的重载，
    * 所以会有很多名字相同后缀不同的函数版本存在。其中函数名中包含数字（1、2、3、4）表示接受这个数字个用于更改uniform变量的值，
    * i表示32位整形，f表示32位浮点型，ub表示8位无符号byte，ui表示32位无符号整形，v表示接受相应的指针类型.）
    * */
            float xPos = (float) threadhold / 100;
            String mystr = String.valueOf(xPos);
            GLES20.glUniform1f(mUTMapLoc, xPos);
    /*
    * void glDrawElements( GLenum mode, GLsizei count,GLenum type, const GLvoid *indices）；
    *其中：
    * mode指定绘制图元的类型，它应该是下列值之一，GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES, GL_TRIANGLE_STRIP,
    * GL_TRIANGLE_FAN, GL_TRIANGLES, GL_QUAD_STRIP, GL_QUADS, and GL_POLYGON.
    * count为绘制图元的数量乘上一个图元的顶点数。
    * type为索引值的类型，只能是下列值之一：GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT, or GL_UNSIGNED_INT。
    * indices：指向索引存贮位置的指针。
    * glDrawElements函数能够通过较少的函数调用绘制多个几何图元，而不是通过OPENGL函数调用来传递每一个顶点，法线，颜色信息。
    * 你可以事先准备一系列分离的顶点、法线、颜色数组，并且调用一次glDrawElements把这些数组定义成一个图元序列。
    * 当调用glDrawElements函数的时候，它将通过索引使用count个成序列的元素来创建一系列的几何图元。mode指定待创建的图元类型和数
    * 组元素如何用来创建这些图元。但是如果GL_VERTEX_ARRAY 没有被激活的话，不能生成任何图元。被glDrawElements修改的顶点属性在
    * glDrawElements调用返回后的值具有不确定性，例如，GL_COLOR_ARRAY被激活后，当glDrawElements执行完成时，当前的颜色值是没有指定的。
    * 没有被修改的属性值保持不变。可以在显示列表中包含glDrawElements，当glDrawElements被包含进显示列表时，
    * 相应的顶点、法线、颜色数组数据也得进入显示列表的，因为那些数组指针是ClientSideState的变量，在显示列表创建的时候而不是在显示
    * 列表执行的时候，这些数组指针的值影响显示列表。glDrawElements只能用在OPENGL1.1或者更高的版本。
    * */
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndices);
            checkGlError("glDrawArrays");

        } catch (Exception e) {
            Log.d("Exceptrion", e.getMessage().toString());
        }
    }

    public static int loadShader(int type, String shaderSrc) {
        int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            return 0;
        }
        GLES20.glShaderSource(shader, shaderSrc);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            if (D) {
                Log.e(TAG, "Could not compile shader " + type + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            }
            GLES20.glDeleteShader(shader);
            shader = 0;
            return 0;
        }
        return shader;
    }

    public void resetProgram() {
        Log.e(TAG, "reset program");
        if (mVertexShader != 0) {
            GLES20.glDeleteShader(mVertexShader);
            mVertexShader = 0;
        }
        if (mFragmentShader != 0) {
            GLES20.glDeleteShader(mFragmentShader);
            mFragmentShader = 0;
        }
        if (mProgramObject != 0) {
            GLES20.glDeleteProgram(mProgramObject);
            mProgramObject = 0;
        }

    }

    public void loadProgram(String vertShaderSrc, String fragShaderSrc) {

        resetProgram();
        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertShaderSrc);
        if (mVertexShader == 0) {
            return;
        }
        mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragShaderSrc);
        if (mFragmentShader == 0) {
            GLES20.glDeleteShader(mVertexShader);
            return;
        }
        mProgramObject = GLES20.glCreateProgram();
        if (mProgramObject == 0) {
            return;
        }
        GLES20.glAttachShader(mProgramObject, mVertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(mProgramObject, mFragmentShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(mProgramObject);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(mProgramObject, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            if (D) {
                Log.e(TAG, "Error linking program:");
                Log.e(TAG, GLES20.glGetProgramInfoLog(mProgramObject));
            }
            GLES20.glDeleteProgram(mProgramObject);
            mProgramObject = 0;

        }
        // GLES20.glDeleteShader(vertexShader);
        // GLES20.glDeleteShader(fragmentShader);
        // return programObject;
    }
    /*
    * OpenGL 提供了三个函数来指定纹理: glTexImage1D(), glTexImage2D(), glTexImage3D(). 这三个版本用于相应维数的纹理，
    * 我们用到的是 2D 版本: glTexImage2D().
    函数原型：
    void glTexImage2D(GLenum target,
                        GLint level,
                        GLint components,
                        GLsizei width,
                        glsizei height,
                        GLint border,
                        GLenum format,
                        GLenum type,
                        const GLvoid *pixels);
    函数功能：定义一个二维纹理映射。

    函数参数：

    target ---- 是常数GL_TEXTURE_2D。

    level ---- 表示多级分辨率的纹理图像的级数，若只有一种分辨率，则level设为0。
    　　components ---- 是一个从1到4的整数，指出选择了R、G、B、A中的哪些分量用于调整和混合，
               1表示选择了R分量，2表示选择了R和A两个分量，3表示选择了R、G、B三个分量，4表示选择了R、G、B、A四个分量。
    　　width和height ---- 给出了纹理图像的长度和宽度，参数border为纹理边界宽度，它通常为0，
               width和height必须是2m+2b，这里m是整数，长和宽可以有不同的值，b是border的值。纹理映射的最大尺寸依赖于OpenGL，
               但它至少必须是使用64x64（若带边界为66x66），若width和height设置为0，则纹理映射有效地关闭。
    　　format和type ---- 描述了纹理映射的格式和数据类型，它们在这里的意义与在函数glDrawPixels()中的意义相同，
               事实上，纹理数据与glDrawPixels()所用的数据有同样的格式。参数format可以是GL_COLOR_INDEX、GL_RGB、
               GL_RGBA、GL_RED、GL_GREEN、GL_BLUE、GL_ALPHA、GL_LUMINANCE或GL_LUMINANCE_ALPHA
               （注意：不能用GL_STENCIL_INDEX和GL_DEPTH_COMPONENT）。
               类似地，参数type是GL_BYPE、GL_UNSIGNED_BYTE、GL_SHORT、 GL_UNSIGNED_SHORT、
               GL_INT、GL_UNSIGNED_INT、GL_FLOAT或GL_BITMAP。
    　　pixels ---- 包含了纹理图像数据，这个数据描述了纹理图像本身和它的边界。

    参数过多，可以使用 GLUtils 中的 texImage2D() 函数，好处是直接将 Bitmap 数据作为参数:

    void texImage2D (int target, int level, Bitmap bitmap, int border)

    参数:

    target ---- 操作的目标类型，设为 GL_TEXTURE_2D 即可

    level ---- 纹理的级别

    bitmap ---- 图像

    border ---- 边框，一般设为0
    * */

    private void bindBaseTexture(Bitmap bitmap) {

        if (bitmap != null) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBaseTexture[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            mBaseMapTexId = mBaseTexture[0];
        }
    }

    private void bindBackTexture(Bitmap bitmap) {

        if (bitmap != null) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBackTexture[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            mBgMapTexId = mBackTexture[0];
        }
    }


    private static void checkGlError(String op) {
        if (D) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.e(TAG, op + ": glError " + error);
                throw new RuntimeException(op + ": glError " + error);
            }
        }


    }


    private void initTexture() {

        GLES20.glGenTextures(1, mBackTexture, 0);
        GLES20.glGenTextures(1, mBaseTexture, 0);
    }

    public void setSteps(int vStep) {

        mLoopStep = vStep;

    }


    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context 知道图片路径 Uri 转换为 String 路径            TODO
     * @param uri
     * @return the file path or null
     * <p>
     * Uri获取String类型的绝对路径
     * String path = Uri.getPath();
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    public enum FilterType {
        NO_EFFECT,
        Zoom_EFFECT,
        LEFTTORIGHT_EFFECT,
        RIGHTTOLEFT_EFFECT,
        TOPTODOWN_EFFECT
    }

}
