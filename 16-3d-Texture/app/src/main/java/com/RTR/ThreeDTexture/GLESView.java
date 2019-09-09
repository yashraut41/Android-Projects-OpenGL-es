package com.RTR.ThreeDTexture;

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

//for texture
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
//FOR texImage2D()
import android.opengl.GLUtils;

//implement all the method from implemented class
public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener,OnDoubleTapListener
{
	private GestureDetector gestureDetector; //field class varaible
	private final Context context;
	private int vertexShaderObject;
	private int fragmentShaderObject;
	private int shaderProgramObject;
	private float anglePyramid=0.0f;
	private float angleCube=0.0f;
	private float rotationSpeed=0.5f;

	private int[] vaoPyramid=new int[1];
	private int[] vaoCube=new int[1];

	private int[] vboPyramidVertices=new int[1];
	private int[] vboCubeVertices=new int[1];
	
	private int[] vboPyramidTexture=new int[1];
	private int[] vboCubeTexture=new int[1];

	private int[] stoneTexture=new int[1];
	private int[] kundaliTexture=new int[1];


	private int samplerUniform;
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

		//private void uninitialize();
		//private int loadTexture(int imageFileResourceId);
		vertexShaderObject=GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
			//vertex shader code
		final String vertexShaderSourceCode=
		String.format
		(
		"#version 320 es" +
		"\n" +
		"in vec4 vPosition;" +
		"in vec2 vTexCoord;" +
		"out vec2 out_TexCoord;"+
		"uniform mat4 u_mvp_matrix;" +
		"void main(void)" +
		"{" +
		"gl_Position=u_mvp_matrix*vPosition;" +
		"out_TexCoord=vTexCoord;"+
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
		 "in vec2 out_TexCoord;"+
		 "uniform sampler2D u_sampler;"+
		 "out vec4 FragColor;" +
		 "void main(void)" +
		 "{" +
		 "FragColor=texture(u_sampler,out_TexCoord);" +
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
			GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.AMC_ATTRIBUTE_TEXCOORD0,"vTexCoord");

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
			samplerUniform=GLES32.glGetUniformLocation(shaderProgramObject,"u_sampler");

			final float[] pyramidVertices=new float[]
					{
							0.0f,1.0f,0.0f,
							-1.0f,-1.0f,1.0f,
							1.0f,-1.0f,1.0f,

							0.0f,1.0f,0.0f,
							1.0f,-1.0f,1.0f,
							1.0f,-1.0f,-1.0f,

							0.0f,1.0f,0.0f,
							1.0f,-1.0f,-1.0f,
							-1.0f,-1.0f,-1.0f,

							0.0f,1.0f,0.0f,
						    -1.0f,-1.0f,-1.0f,
							-1.0f,-1.0f,1.0f
					};

			final float[] pyramidTexCoord=new float[]
			{
					0.5f,1.0f,
					0.0f,0.0f,
					1.0f,0.0f,

					0.5f,1.0f,
					1.0f,0.0f,
					0.0f,0.0f,

					0.5f,1.0f,
					1.0f,0.0f,
					0.0f,0.0f,

					0.5f,1.0f,
					0.0f,0.0f,
					1.0f,0.0f		
			};

			final float[] cubeVertices=new float[]
			{
				 1.0f,1.0f,-1.0f,
				-1.0f,1.0f,-1.0f,
				-1.0f,-1.0f,-1.0f,
				 1.0f,-1.0f,-1.0f,

				1.0f,1.0f,-1.0f,
				1.0f,1.0f,1.0f,
				1.0f,-1.0f,1.0f,
				1.0f,-1.0f,-1.0f,

				1.0f,1.0f,-1.0f,
				-1.0f,1.0f,-1.0f,
				-1.0f,1.0f,1.0f,
				1.0f,1.0f,1.0f,

				1.0f,-1.0f,-1.0f,
				-1.0f,-1.0f,-1.0f,
				-1.0f,-1.0f,1.0f,
				1.0f,-1.0f,1.0f,

				1.0f,1.0f,1.0f,
				-1.0f,1.0f,1.0f,
				-1.0f,-1.0f,1.0f,
				1.0f,-1.0f,1.0f,

				-1.0f,1.0f,1.0f,
				-1.0f,1.0f,-1.0f,
				-1.0f,-1.0f,-1.0f,
				-1.0f,-1.0f,1.0f
			};

			final float[] cubeTexCoord=new float[]
			{
				 //top
				0.0f,1.0f,
				0.0f,0.0f,
				1.0f,0.0f,
				1.0f,1.0f,

				//bottom
				1.0f,1.0f,
				0.0f,1.0f,
				0.0f,0.0f,
				1.0f,0.0f,

				//front
				0.0f,0.0f,
				1.0f,0.0f,
				1.0f,1.0f,
				0.0f,1.0f,

				//back
				1.0f,0.0f,
				1.0f,1.0f,
				0.0f,1.0f,
				0.0f,0.0f,

				//right
				1.0f,0.0f,
				1.0f,1.0f,
				0.0f,1.0f,
				0.0f,0.0f,

				//left
				0.0f,0.0f,
				1.0f,0.0f,
				1.0f,1.0f,
				0.0f,1.0f	
			};

			

		

			//<<<<<<------------START VAO FOR pyramid --------------------------->>>>
			//create vao for pyramid
			GLES32.glGenVertexArrays(1,vaoPyramid,0);
			//bind VAO
			GLES32.glBindVertexArray(vaoPyramid[0]);

			//<<<---------------START VBO pyramid position----------------------->>>>


			//create vertex buffer object (VBO) for pyramid position
			GLES32.glGenBuffers(1,vboPyramidVertices,0);
			//bind vbo for pyramid position 
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboPyramidVertices[0]);

			//create buffer from our pyramidVertices[] array so we can pass it to glBufferData()

			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect

			ByteBuffer byteBuffer=ByteBuffer.allocateDirect(pyramidVertices.length*4);

			//arrange the byteorder of buffer in native byte order little indian/big indian
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			FloatBuffer positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of pyramidVertices into these cooked buffer
			positionBuffer.put(pyramidVertices);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,pyramidVertices.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);

			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo for pyramid position
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);


			//<<<<<<--------------------- END VBO for pyramid position--------->>>>>>




			//<<<<<<-------------------START VBO pyramid TexCoord ------------------------>>>>>>


			//create vbo buffer for  pyramid TexCoord
			GLES32.glGenBuffers(1,vboPyramidTexture,0 );
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPyramidTexture[0]);

			//create buffer from our  pyramid TexCoord array so we can pass it to glBufferData()

			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect

			 byteBuffer=ByteBuffer.allocateDirect(pyramidTexCoord.length*4);

			//arrange the byteorder of buffer in native byte order little indian/big indian
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			 positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of pyramidTexcoord into these cooked buffer
			positionBuffer.put(pyramidTexCoord);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, pyramidTexCoord.length*4, positionBuffer, GLES32.GL_STATIC_DRAW);
			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_TEXCOORD0, 2, GLES32.GL_FLOAT, false, 0, 0);
			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_TEXCOORD0);

			//unbind vbo buffer of pyramidTexCoord 
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);

			//<<<<<-----------------------end VBO FOR pyramidTexCoord----------------------->>>> 

			//unbind vao (vertex array object) for pyramid
			GLES32.glBindVertexArray(0);
			






			//<<<<<<------------START VAO FOR cube --------------------------->>>>
			//create vao for cube
			GLES32.glGenVertexArrays(1,vaoCube,0);
			//bind VAO
			GLES32.glBindVertexArray(vaoCube[0]);

			//<<<---------------START VBO cube position----------------------->>>>


			//create vertex buffer object (VBO) for cube position
			GLES32.glGenBuffers(1,vboCubeVertices,0);
			//bind vbo for cube position 
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboCubeVertices[0]);

			//create buffer from our cubeVertices[] array so we can pass it to glBufferData()

			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect

			 byteBuffer=ByteBuffer.allocateDirect(cubeVertices.length*4);

			//arrange the byteorder of buffer in native byte order little indian/big indian
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			 positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of cubeVertices into these cooked buffer
			positionBuffer.put(cubeVertices);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,cubeVertices.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);

			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo for CUBE position
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);


			//<<<<<<--------------------- END VBO for cube position--------->>>>>>




			//<<<<<<-------------------START VBO cube TexCoord ------------------------>>>>>>


			//create vbo buffer for  cube TexCoord
			GLES32.glGenBuffers(1,vboCubeTexture,0 );
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboCubeTexture[0]);

			//create buffer from our  cube TexCoord array so we can pass it to glBufferData()

			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect

			 byteBuffer=ByteBuffer.allocateDirect(cubeTexCoord.length*4);

			//arrange the byteorder of buffer in native byte order little indian/big indian
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			 positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of cubeTexcoord into these cooked buffer
			positionBuffer.put(cubeTexCoord);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, cubeTexCoord.length*4, positionBuffer, GLES32.GL_STATIC_DRAW);
			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_TEXCOORD0, 2, GLES32.GL_FLOAT, false, 0, 0);
			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_TEXCOORD0);

			//unbind vbo buffer of cubeTexCoord 
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);

			//<<<<<-----------------------end VBO FOR cubeTexCoord----------------------->>>> 

			//unbind vao (vertex array object) for cube
			GLES32.glBindVertexArray(0);
			


			
			GLES32.glEnable(GLES32.GL_DEPTH_TEST);
			//disable cull face so we can see back side of geometry while rotation
			GLES32.glDisable(GLES32.GL_CULL_FACE);
			GLES32.glDepthFunc(GLES32.GL_LEQUAL);
			GLES32.glClearColor(0.0f,0.0f,0.0f,1.0f);
			GLES32.glEnable(GLES32.GL_TEXTURE_2D);
			stoneTexture[0]=loadTexture(R.raw.stone);
			kundaliTexture[0]=loadTexture(R.raw.kundali);

			Matrix.setIdentityM(perspectiveProjectionMatrix,0);
	}



	private int loadTexture(int imageFileResourceId)
	{
		int[] texture=new int[1];
		BitmapFactory.Options options=new BitmapFactory.Options();

		options.inScaled=false;

		Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(),imageFileResourceId,options);

		GLES32.glGenTextures(1,texture,0);
		GLES32.glBindTexture(GLES32.GL_TEXTURE_2D,texture[0]);

		GLES32.glPixelStorei(GLES32.GL_UNPACK_ALIGNMENT,4);
		GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MAG_FILTER,GLES32.GL_LINEAR);
		GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MIN_FILTER,GLES32.GL_LINEAR_MIPMAP_LINEAR);

		GLUtils.texImage2D(GLES32.GL_TEXTURE_2D,0,bitmap,0);

		GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D);
		GLES32.glBindTexture(GLES32.GL_TEXTURE_2D,0);

		return texture[0];

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
		//for pyramid 
		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(modelViewProjectionMatrix,0);
		Matrix.setIdentityM(rotationMatrix,0);
		Matrix.translateM(modelViewMatrix,0,-2.0f,0.0f,-5.0f);
		Matrix.setRotateM(rotationMatrix,0,anglePyramid,0.0f,1.0f,0.0f);
		Matrix.multiplyMM(modelViewMatrix,0,modelViewMatrix,0,rotationMatrix,0);
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
		GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, stoneTexture[0]);
		GLES32.glUniform1i(samplerUniform, 0);

		GLES32.glBindVertexArray(vaoPyramid[0]);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLES,0,12);
		GLES32.glBindVertexArray(0);
		GLES32.glBindTexture(GLES32.GL_TEXTURE_2D,0);

		//for cube 
		float[] scaleMatrix=new float[16];
		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(modelViewProjectionMatrix,0);
		Matrix.setIdentityM(rotationMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);

		Matrix.translateM(modelViewMatrix,0,2.0f,0.0f,-5.0f);
		
		Matrix.scaleM(scaleMatrix,0,0.75f,0.75f,0.75f);
		Matrix.multiplyMM(modelViewMatrix,0,modelViewMatrix,0,scaleMatrix,0);

		Matrix.setRotateM(rotationMatrix,0,angleCube,1.0f,1.0f,1.0f);
		Matrix.multiplyMM(modelViewMatrix,0,modelViewMatrix,0,rotationMatrix,0);

		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
		GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, kundaliTexture[0]);
		GLES32.glUniform1i(samplerUniform, 0);

		GLES32.glBindVertexArray(vaoCube[0]);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,0,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,4,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,8,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,12,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,16,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,20,4);
		GLES32.glBindVertexArray(0);
		GLES32.glBindTexture(GLES32.GL_TEXTURE_2D,0);


		GLES32.glUseProgram(0);
		requestRender();
	}
	
	private void update()
	{	
		anglePyramid+=rotationSpeed;
		angleCube+=rotationSpeed;

		if(anglePyramid >= 360.0f)
		{
			anglePyramid=0.0f;
		}

		if(angleCube >= 360.0f)
		{
			angleCube=0.0f;
		}
	
	}
	private void uninitialize()
	{
		if(vboCubeTexture[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vboCubeTexture,0);
			vboCubeTexture[0]=0;
		}

		if(vboCubeVertices[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vboCubeVertices,0);
			vboCubeVertices[0]=0;
		}

		if(vaoCube[0]!=0)
		{
			GLES32.glDeleteVertexArrays(1,vaoCube,0);
			vaoCube[0]=0;
		}


		if(vboPyramidTexture[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vboPyramidTexture,0);
			vboPyramidTexture[0]=0;
		}

		if(vboPyramidVertices[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vboPyramidVertices,0);
			vboPyramidVertices[0]=0;
		}

		if(vaoPyramid[0]!=0)
		{
			GLES32.glDeleteVertexArrays(1,vaoPyramid,0);
			vaoPyramid[0]=0;
		}

		if(kundaliTexture[0]!=0)
		{
			GLES32.glDeleteTextures(1,kundaliTexture,0);
			kundaliTexture[0]=0;
		}

		if(stoneTexture[0]!=0)
		{
			GLES32.glDeleteTextures(1,stoneTexture,0);
			stoneTexture[0]=0;
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
