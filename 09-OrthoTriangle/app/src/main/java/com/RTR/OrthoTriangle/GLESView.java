package com.RTR.OrthoTriangle;

//added by me

import android.content.Context;
import android.view.Gravity;
import android.graphics.Color;

//view which will support OpenGL
import android.opengl.GLSurfaceView;

//for OpenGL BUFFERS
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

//for matrix math
import android.opengl.Matrix;

//for OpenGL ES  versio -3.2
import android.opengl.GLES32;

import javax.microedition.khronos.opengles.GL10;

import javax.microedition.khronos.egl.EGLConfig;
//for MotionEvent
import android.view.MotionEvent;

//for GestureDetector
import android.view.GestureDetector;

//for OnGestureListener
import android.view.GestureDetector.OnGestureListener;

//for OnDoubleTapListener
import android.view.GestureDetector.OnDoubleTapListener;

//implement all the method from implemented class
public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener,OnDoubleTapListener
{
	private GestureDetector gestureDetector; //field class varaible
	private final Context context;
	private int vertexShaderObject;
	private int fragmentShaderObject;
	private int shaderProgramObject;

	private int[] vao=new int[1];
	private int[] vbo=new int[1];

	private int mvpUniform;
	private float[]orthographicProjectionMatrix=new float[16];
	public GLESView(Context drawingContext)
	{

		super(drawingContext);
		context=drawingContext;

		setEGLContextClientVersion(3);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		gestureDetector=new GestureDetector(context,this,null,false);
		gestureDetector.setOnDoubleTapListener(this);  //this means handler

	}

	//handling ontouchevent is the most important beacause it triggers all gesture and tap events
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//code
		int eventaction=event.getAction();
		if(!gestureDetector.onTouchEvent(event))
			super.onTouchEvent(event);
		return(true);
	}

	//abstract method from OnDoubleTapListener

	@Override
	public boolean onDoubleTap(MotionEvent e)
	{

		return(true);
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		return(true);
	}

	//abstract method from OnDoubleTapListener
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e)
	{

		return(true);
	}

	//abstract method from OnGestureListener so must be implemented


	@Override
	public boolean onDown(MotionEvent e)
	{
		//we already wriiten onSingleTapConfirmed analogus behaviour
		return(true);
	}

	//abstract method from OnGestureListener so must be implemented
	@Override
	public boolean onFling(MotionEvent e1,MotionEvent e2,float velocityX,float velocityY)
	{
		return(true);
	}

	//abstract method from OnGestureListener so must be implemented
	@Override
	public void onLongPress(MotionEvent e)
	{

	}

	//abstract method from OnGestureListener so must be implemented
	@Override
	public boolean onScroll(MotionEvent e1,MotionEvent e2,float distanceX,float distanceY)
	{
		System.exit(0);
		uninitialize();
		return(true);
	}

	//abstract method from OnGestureListener so must be implemented
	@Override
	public void onShowPress(MotionEvent e)
	{

	}

	//abstract method from OnGestureListener so must be implemented
	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		return(true);
	}

	//IMPLEMENT GLSurfaceView.Renderer Method

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		String openglVersion=gl.glGetString(GL10.GL_VERSION);
		System.out.println("AMC:"+openglVersion);

		String shadingLanguageVersion=gl.glGetString(GLES32.GL_SHADING_LANGUAGE_VERSION);
		System.out.println("AMC:"+shadingLanguageVersion);

		initialize();
	}

	@Override
	public void onSurfaceChanged(GL10 unused,int width, int height)
	{
		resize(width,height);
	}


	@Override
	public void onDrawFrame(GL10 unused)
	{
		display();
	}

	//our custom methods


	private void initialize()
	{
		vertexShaderObject=GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
			//vertex shader code
		final String vertexShaderSourceCode=
		String.format
		(
		"#version 320 es" +
		"\n" +
		"in vec4 vPosition;" +
		"uniform mat4 u_mvp_matrix;" +
		"void main(void)" +
		"{" +
		"gl_Position=u_mvp_matrix*vPosition;" +
		"}"
		);

		//specify above shader source code to vertexShaderObject
		//give shader source code
		GLES32.glShaderSource(vertexShaderObject,vertexShaderSourceCode);

		//compile the vertex shader code
		GLES32.glCompileShader(vertexShaderObject);

		//error checking code for vertex shader
		int []iShaderCompileStatus=new int[1];
		int []iInfoLogLength=new int[1];
		String szInfoLog=null;

		GLES32.glGetShaderiv(vertexShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompileStatus,0);

		if(iShaderCompileStatus[0]==GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(vertexShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

			if(iInfoLogLength[0]>0)
			{
				szInfoLog=GLES32.glGetShaderInfoLog(vertexShaderObject);
				System.out.println("AMC vertex shader compilation Log:"+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}

		//fragment shader
		fragmentShaderObject=GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

		 final String fragmentShaderSourceCode=
		 String.format
		 (
		 "#version 320 es" +
		 "\n" +
		 "precision highp float;" +
		 "out vec4 FragColor;" +
		 "void main(void)" +
		 "{" +
		 "FragColor=vec4(1.0,1.0,0.0,1.0);" +
		 "}"
		 );

		 GLES32.glShaderSource(fragmentShaderObject,fragmentShaderSourceCode);

		 GLES32.glCompileShader(fragmentShaderObject);
		 iShaderCompileStatus[0]=0;
		 iInfoLogLength[0]=0;
		 szInfoLog=null;
		 GLES32.glGetShaderiv(fragmentShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompileStatus,0);

		 if(iShaderCompileStatus[0]==GLES32.GL_FALSE)
		 {
			 GLES32.glGetShaderiv(fragmentShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

			 if(iInfoLogLength[0]>0)
			 {
				 szInfoLog=GLES32.glGetShaderInfoLog(fragmentShaderObject);
				 System.out.println("AMC fragment shader compilation log:"+szInfoLog);
				 uninitialize();
				 System.exit(0);
			 }
		 }
		 	//create shader program object
			shaderProgramObject=GLES32.glCreateProgram();

			//attach vertex shader to shader program
			GLES32.glAttachShader(shaderProgramObject,vertexShaderObject);

			//attach fragment shader  to shaderprogram
			GLES32.glAttachShader(shaderProgramObject,fragmentShaderObject);

			//bind attributes to shader variable
			GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.AMC_ATTRIBUTE_POSITION,"vPosition");

			//NOW Link shader program
			GLES32.glLinkProgram(shaderProgramObject);

			int []iProgramLinkStatus=new int[1];
			iInfoLogLength[0]=0;
			szInfoLog=null;

			GLES32.glGetProgramiv(shaderProgramObject,GLES32.GL_LINK_STATUS,iProgramLinkStatus,0);

			if(iProgramLinkStatus[0]==GLES32.GL_FALSE)
			{
				GLES32.glGetProgramiv(shaderProgramObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

				if(iInfoLogLength[0]>0)
				{
					szInfoLog=GLES32.glGetProgramInfoLog(shaderProgramObject);
					System.out.println("AMC Shader Program Link Log:"+szInfoLog);
					uninitialize();
					System.exit(0);
				}
			}

			mvpUniform=GLES32.glGetUniformLocation(shaderProgramObject,"u_mvp_matrix");

			final float[] triangleVertices=new float[]
			{
					0.0f,50.0f,0.0f,
				 -50.0f,-50.0f,0.0f,
				 50.0f,-50.0f,0.0f
			};

			//create vao
			GLES32.glGenVertexArrays(1,vao,0);
			//bind VAO
			GLES32.glBindVertexArray(vao[0]);

			//create vertex buffer object (VBO)
			GLES32.glGenBuffers(1,vbo,0);
			//bind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo[0]);

			//create buffer from our triangleVertices array so we can pass it to buffer data

			//allocate buffer directly  from native memeory (not VM memory)

			ByteBuffer byteBuffer=ByteBuffer.allocateDirect(triangleVertices.length*4);

			//arrange the byteorder of buffer in native byte order
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			FloatBuffer positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of triangle into these cooked buffer
			positionBuffer.put(triangleVertices);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,triangleVertices.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);

			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//unbind vao (vertex array object)
			GLES32.glBindVertexArray(0);

			GLES32.glEnable(GLES32.GL_DEPTH_TEST);
			GLES32.glEnable(GLES32.GL_CULL_FACE);
			GLES32.glDepthFunc(GLES32.GL_LEQUAL);
			GLES32.glClearColor(0.0f,0.0f,1.0f,1.0f);

			Matrix.setIdentityM(orthographicProjectionMatrix,0);
	}

	private void resize(int width,int height)
	{
		if(height==0)
		{
			height=1;

		}
		GLES32.glViewport(0,0,width,height);
	
		if (width<=height)
		{
			Matrix.orthoM(orthographicProjectionMatrix,
			0,
			-100.0f,
			100.0f,
			(-100.0f*(float)height/(float)width),
			(100.0f*(float)height/(float)width),
			-100.0f,
			100.0f);

		}
		else
		{
			Matrix.orthoM(orthographicProjectionMatrix,
			0,
			(-100.0f*(float)width/(float)height),
			(100.0f*(float)width/(float)height),
			-100.0f,
			100.0f,
			-100.0f,
			100.0f);
		}
	}

	private void display()
	{
		GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT|GLES32.GL_DEPTH_BUFFER_BIT);
		GLES32.glUseProgram(shaderProgramObject);

		//declaration of matrices
		float[] modelViewMatrix=new float[16];
		float[] modelViewProjectionMatrix=new float[16];


		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(modelViewMatrix,0);

		Matrix.multiplyMM(modelViewProjectionMatrix,0,orthographicProjectionMatrix,0,modelViewMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		//BIND WITH vao
		GLES32.glBindVertexArray(vao[0]);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLES,0,3);
		GLES32.glBindVertexArray(0);
		GLES32.glUseProgram(0);
		requestRender();
	}

	private void uninitialize()
	{
		if(vbo[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vbo,0);
			vbo[0]=0;
		}

		if(vao[0]!=0)
		{
			GLES32.glDeleteVertexArrays(1,vao,0);
			vao[0]=0;
		}

		if(shaderProgramObject!=0)
		{
			int[] shaderCount=new int[1];
			int shaderNumber;
			GLES32.glUseProgram(shaderProgramObject);

			GLES32.glGetProgramiv(shaderProgramObject,GLES32.GL_ATTACHED_SHADERS,shaderCount,0);
			int[] shader=new int[shaderCount[0]];
			if(shader[0]!=0)
			{
				GLES32.glGetAttachedShaders(shaderProgramObject,shaderCount[0],shaderCount,0,shader,0);

				for(shaderNumber=0;shaderNumber<shaderCount[0];shaderNumber++)
				{
					GLES32.glDetachShader(shaderProgramObject,shader[shaderNumber]);
					GLES32.glDeleteShader(shader[shaderNumber]);
					shader[shaderNumber]=0;
				}
			}
			GLES32.glUseProgram(0);
		GLES32.glDeleteShader(shaderProgramObject);
		shaderProgramObject=0;
		}
	}
}
