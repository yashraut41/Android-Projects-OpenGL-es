package com.RTR.twoDShapesAnimation;

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
	private float angleTriangle=0.0f;
	private float angleRectangle=0.0f;
	private float rotationSpeed=0.5f;

	private int[] vaoTriangle=new int[1];
	private int[] vaoRectangle=new int[1];

	private int[] vboTriangle=new int[1];
	private int[] vboRectangle=new int[1];
	private int mvpUniform;
	private float[]perspectiveProjectionMatrix=new float[16];
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
		rotationSpeed-=0.5f;
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
		rotationSpeed+=0.5f;	
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

		String vendor=gl.glGetString(GL10.GL_VENDOR);
		System.out.println("AMC:"+vendor);

		String renderer=gl.glGetString(GL10.GL_RENDERER);
		System.out.println("AMC:"+renderer);

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
		update();
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
		 "FragColor=vec4(0.0,1.0,1.0,1.0);" +
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
					0.0f,1.0f,0.0f,
					-1.0f,-1.0f,0.0f,
					1.0f,-1.0f,0.0f
			};

			final float[] rectangleVertices=new float[]
			{
				1.0f,1.0f,0.0f,
				-1.0f,1.0f,0.0f,
				-1.0f,-1.0f,0.0f,
				1.0f,-1.0f,0.0f
			};

			//create vao for triangle
			GLES32.glGenVertexArrays(1,vaoTriangle,0);
			//bind VAO
			GLES32.glBindVertexArray(vaoTriangle[0]);

			//create vertex buffer object (VBO) for triangle
			GLES32.glGenBuffers(1,vboTriangle,0);
			//bind vbo for triangle 
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboTriangle[0]);

			//create buffer from our triangleVertices array so we can pass it to glBufferData()

			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect

			ByteBuffer byteBuffer=ByteBuffer.allocateDirect(triangleVertices.length*4);

			//arrange the byteorder of buffer in native byte order little indian/big indian
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

			//unbind vbo for triangle
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//unbind vao (vertex array object) for triangle
			GLES32.glBindVertexArray(0);

			//for rectangle -><-

			//create vertex array object for rectangle (vao)
			GLES32.glGenVertexArrays(1,vaoRectangle,0);

			//bind vao for rectangle
			GLES32.glBindVertexArray(vaoRectangle[0]);

			//create buffer for rectangle vbo
			GLES32.glGenBuffers(1,vboRectangle,0);

			//bind buffer
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboRectangle[0]);


			//create buffer from our triangleVertices array so we can pass it to glBufferData()

			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect

			 byteBuffer=ByteBuffer.allocateDirect(rectangleVertices.length*4);


			//arrange the byteorder of buffer in native byte order little indian/big indian

			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER

			 positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of triangle into these cooked buffer
			positionBuffer.put(rectangleVertices);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);

			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,rectangleVertices.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);
			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo for rectangle 
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//unbind vao fo rectangle 
			GLES32.glBindVertexArray(0);


			GLES32.glEnable(GLES32.GL_DEPTH_TEST);
			//disable cull face so we can see back side of geometry while rotation
			GLES32.glDisable(GLES32.GL_CULL_FACE);
			GLES32.glDepthFunc(GLES32.GL_LEQUAL);
			GLES32.glClearColor(0.0f,0.0f,0.0f,1.0f);

			Matrix.setIdentityM(perspectiveProjectionMatrix,0);
	}

	private void resize(int width,int height)
	{
		if(height==0)
		{
			height=1;
		}
		GLES32.glViewport(0,0,width,height);
		Matrix.perspectiveM(perspectiveProjectionMatrix,0,45.0f,(float)width/(float)height,0.1f,100.0f);

	}

	private void display()
	{
		GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT|GLES32.GL_DEPTH_BUFFER_BIT);
		GLES32.glUseProgram(shaderProgramObject);

		//declaration of matrices
		float[] modelViewMatrix=new float[16];
		float[] modelViewProjectionMatrix=new float[16];
		float[] rotationMatrix=new float[16];
		

		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(modelViewProjectionMatrix,0);
		Matrix.setIdentityM(rotationMatrix,0);

		Matrix.translateM(modelViewMatrix,0,-3.0f,0.0f,-6.0f);
		//Matrix.rotateM(rotationMatrix,0,angleTriangle,0.0f,1.0f,0.0f);
		//Matrix.multiplyMM(modelViewMatrix,0,modelViewMatrix,0,rotationMatrix,0);
		Matrix.rotateM(modelViewMatrix,0,angleTriangle,0.0f,1.0f,0.0f);
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		//BIND WITH vao
		GLES32.glBindVertexArray(vaoTriangle[0]);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLES,0,3);
		GLES32.glBindVertexArray(0);

		//for rectangle
		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(modelViewProjectionMatrix,0);

		Matrix.translateM(modelViewMatrix,0,3.0f,0.0f,-6.0f);
		Matrix.rotateM(modelViewMatrix,0,angleRectangle,0.0f,1.0f,0.0f);
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		//BIND WITH vao
		GLES32.glBindVertexArray(vaoRectangle[0]);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,0,4);
		GLES32.glBindVertexArray(0);

		GLES32.glUseProgram(0);
		requestRender();
	}
	private void update()
	{	
		angleTriangle+=rotationSpeed;
		angleRectangle+=rotationSpeed;

		if(angleTriangle >= 360.0f)
		{
			angleTriangle=0.0f;
		}

		if(angleRectangle >= 360.0f)
		{
			angleRectangle=0.0f;
		}
	
	}
	private void uninitialize()
	{
		if(vboRectangle[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vboRectangle,0);
			vboRectangle[0]=0;
		}

		if(vaoRectangle[0]!=0)
		{
			GLES32.glDeleteVertexArrays(1,vaoRectangle,0);
			vaoRectangle[0]=0;
		}

			if(vboTriangle[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vboTriangle,0);
			vboTriangle[0]=0;
		}

		if(vaoTriangle[0]!=0)
		{
			GLES32.glDeleteVertexArrays(1,vaoTriangle,0);
			vaoTriangle[0]=0;
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
