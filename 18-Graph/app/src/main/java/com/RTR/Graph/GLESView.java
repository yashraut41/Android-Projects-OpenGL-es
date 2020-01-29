package com.RTR.Graph;

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

	private int[] vaoVerticalLines=new int[1];
	private int[] vboVerticalLines=new int[1];
	//private int[] vboVerticalLinesColor=new int[1];

	private int[] vaoHorizontalLines=new int[1];
	private int[] vboHorizontalLines=new int[1];
	//private int[] vboHorizontalLinesColor=new int[1];
	
	private int[] vaoRedHorizontalLine=new int[1];
	private int[] vaoGreenVerticalLine=new int[1];

	private int[] vboRedHorizontalLine=new int[1];
	private int[] vboGreenVerticalLine=new int[1];

	private int[] vboRedHorizontalLineColor=new int[1];
	private int[] vboGreenVerticalLineColor=new int[1];


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
		"in vec4 vColor;" +
		"out vec4 out_Color;"+
		"uniform mat4 u_mvp_matrix;" +
		"void main(void)" +
		"{" +
		"gl_Position=u_mvp_matrix*vPosition;" +
		"out_Color=vColor;"+
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
		 "in vec4 out_Color;" +
		 "out vec4 FragColor;" +
		 "void main(void)" +
		 "{" +
		 "FragColor=out_Color;" +
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
			GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.AMC_ATTRIBUTE_COLOR,"vColor");

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

			//private float x,y;
			float[] redHorizontalLine=new float[] 
			{
				-1.0f,0.0f,0.0f,
				1.0f,0.0f,0.0f
			};

			float[] redHorizontalLineColor=new float[]
			{
				1.0f,0.0f,0.0f,
				1.0f,0.0f,0.0f
			};

			float[] greenVerticalLine=new float[]
			{
				0.0f,1.0f,0.0f,
				0.0f,-1.0f,0.0f
			};

			float[] greenVerticalLineColor=new float[]
			{
				0.0f,1.0f,0.0f,
				0.0f,1.0f,0.0f
			};
			
			float[] graphVerticalLines=new float[]
			{
					0.0f,1.0f,0.0f,
					0.0f,-1.0f,0.0f,

					0.05f, 1.0f, 0.0f,
					0.05f, -1.0f, 0.0f,
					
					0.10f, 1.0f, 0.0f,
					0.10f, -1.0f, 0.0f,

					0.15f, 1.0f, 0.0f,
					0.15f, -1.0f, 0.0f,

					0.20f, 1.0f, 0.0f,
					0.20f, -1.0f, 0.0f,
					
					0.25f, 1.0f, 0.0f,
	 				0.25f, -1.0f, 0.0f,

					 0.30f, 1.0f, 0.0f,
					 0.30f, -1.0f, 0.0f,

					 0.35f, 1.0f, 0.0f,
					 0.35f, -1.0f, 0.0f,

					 0.40f, 1.0f, 0.0f,
					 0.40f, -1.0f, 0.0f,

					 0.45f, 1.0f, 0.0f,
					 0.45f, -1.0f, 0.0f,

					 0.50f, 1.0f, 0.0f,
					 0.50f, -1.0f, 0.0f,

	 				0.55f, 1.0f, 0.0f,
	 				0.55f, -1.0f, 0.0f,

					 0.60f, 1.0f, 0.0f,
	 				0.60f, -1.0f, 0.0f,

	 				0.65f, 1.0f, 0.0f,
					0.65f, -1.0f, 0.0f,

					 0.70f, 1.0f, 0.0f,
					 0.70f, -1.0f, 0.0f,

	 				0.75f, 1.0f, 0.0f,
					 0.75f, -1.0f, 0.0f,

					 0.80f, 1.0f, 0.0f,
					 0.80f, -1.0f, 0.0f,

					 0.85f, 1.0f, 0.0f,
					 0.85f, -1.0f, 0.0f,

					 0.90f, 1.0f, 0.0f,
					 0.90f, -1.0f, 0.0f,

					 0.95f, 1.0f, 0.0f,
					 0.95f, -1.0f, 0.0f,

	 				1.0f, 1.0f, 0.0f,
	 				1.0f, -1.0f, 0.0f,

	//20 vertical line <- side
					 -0.05f, 1.0f, 0.0f,
					 -0.05f, -1.0f, 0.0f,

	 				-0.10f, 1.0f, 0.0f,
					 -0.10f, -1.0f, 0.0f,

					 -0.15f, 1.0f, 0.0f,
					 -0.15f, -1.0f, 0.0f,

					 -0.20f, 1.0f, 0.0f,
					 -0.20f, -1.0f, 0.0f,

					 -0.25f, 1.0f, 0.0f,
					 -0.25f, -1.0f, 0.0f,

					 -0.30f, 1.0f, 0.0f,
					 -0.30f, -1.0f, 0.0f,

					 -0.35f, 1.0f, 0.0f,
					 -0.35f, -1.0f, 0.0f,

					 -0.40f, 1.0f, 0.0f,
					 -0.40f, -1.0f, 0.0f,

					 -0.45f, 1.0f, 0.0f,
					 -0.45f, -1.0f, 0.0f,

					 -0.50f, 1.0f, 0.0f,
					 -0.50f, -1.0f, 0.0f,

					   -0.55f, 1.0f, 0.0f,  
					  -0.55f, -1.0f, 0.0f , 

					  -0.60f, 1.0f, 0.0f  ,
					  -0.60f, -1.0f, 0.0f  ,

					 -0.65f, 1.0f, 0.0f  ,
					-0.65f, -1.0f, 0.0f ,

					-0.70f, 1.0f, 0.0f ,
					-0.70f, -1.0f, 0.0f ,

					-0.75f, 1.0f, 0.0f ,
					 -0.75f, -1.0f, 0.0f ,

					 -0.80f, 1.0f, 0.0f ,
	  				 -0.80f, -1.0f, 0.0f ,

	  				-0.85f, 1.0f, 0.0f ,
	  				-0.85f, -1.0f, 0.0f ,

				    -0.90f, 1.0f, 0.0f ,
				    -0.90f, -1.0f, 0.0f ,

	 			    -0.95f, 1.0f, 0.0f ,
	 			    -0.95f, -1.0f, 0.0f ,

	 				-1.0f, 1.0f, 0.0f ,
	  				-1.0f, -1.0f, 0.0f,

				};
		
	float[] graphHorizontalLines=new float[]
	{
		//20 horizontal line upper x axis
		-1.0f, 0.05f, 0.0f,
	    1.0f, 0.05f, 0.0f  ,

	    -1.0f, 0.10f, 0.0f  ,
	    1.0f, 0.10f, 0.0f  ,

	    -1.0f, 0.15f, 0.0f  ,
	    1.0f, 0.15f, 0.0f  ,

	    -1.0f, 0.20f, 0.0f  ,
	    1.0f, 0.20f, 0.0f  ,

	    -1.0f, 0.25f, 0.0f  ,
	    1.0f, 0.25f, 0.0f  ,

	    -1.0f, 0.30f, 0.0f  ,
	    1.0f, 0.30f, 0.0f  ,

	    -1.0f, 0.35f, 0.0f  ,
	    1.0f, 0.35f, 0.0f  ,

	    -1.0f, 0.40f, 0.0f  ,
	    1.0f, 0.40f, 0.0f  ,

	    -1.0f, 0.45f, 0.0f  ,
	    1.0f, 0.45f, 0.0f  ,

	    -1.0f, 0.50f, 0.0f  ,
	    1.0f, 0.50f, 0.0f  ,

	    -1.0f, 0.55f, 0.0f  ,
	    1.0f, 0.55f, 0.0f  ,

	    -1.0f, 0.60f, 0.0f  ,
	    1.0f, 0.60f, 0.0f  ,

	    -1.0f, 0.65f, 0.0f  ,
	    1.0f, 0.65f, 0.0f  ,

	    -1.0f, 0.70f, 0.0f  ,
	    1.0f, 0.70f, 0.0f  ,

	    -1.0f, 0.75f, 0.0f  ,
	    1.0f, 0.75f, 0.0f  ,

	    -1.0f, 0.80f, 0.0f  ,
	    1.0f, 0.80f, 0.0f  ,

	    -1.0f, 0.85f, 0.0f  ,
	    1.0f, 0.85f, 0.0f  ,


	    






	   -1.0f, -0.05f, 0.0f   ,
	   1.0f, -0.05f, 0.0f   ,

	   -1.0f, -0.10f, 0.0f   ,
	   1.0f, -0.10f, 0.0f   ,

	   -1.0f, -0.15f, 0.0f   ,
	   1.0f, -0.15f, 0.0f   ,

	   -1.0f, -0.20f, 0.0f   ,
	   1.0f, -0.20f, 0.0f   ,

	   -1.0f, -0.25f, 0.0f   ,
	   1.0f, -0.25f, 0.0f   ,

	   -1.0f, -0.30f, 0.0f   ,
	   1.0f, -0.30f, 0.0f   ,

	   -1.0f, -0.35f, 0.0f   ,
	   1.0f, -0.35f, 0.0f   ,

	   -1.0f, -0.40f, 0.0f   ,
	   1.0f, -0.40f, 0.0f   ,

	   -1.0f, -0.45f, 0.0f   ,
	   1.0f, -0.45f, 0.0f   ,

	   -1.0f, -0.50f, 0.0f   ,
	   1.0f, -0.50f, 0.0f   ,

	   -1.0f, -0.55f, 0.0f   ,
	   1.0f, -0.55f, 0.0f   ,

	   -1.0f, -0.60f, 0.0f   ,
	   1.0f, -0.60f, 0.0f   ,

	   -1.0f, -0.65f, 0.0f   ,
	   1.0f, -0.65f, 0.0f   ,

	   -1.0f, -0.70f, 0.0f   ,
	   1.0f, -0.70f, 0.0f   ,

	   -1.0f, -0.75f, 0.0f   ,
	   1.0f, -0.75f, 0.0f   ,

	   -1.0f, -0.80f, 0.0f   ,
	   1.0f, -0.80f, 0.0f   ,

	   -1.0f, -0.85f, 0.0f   ,
	   1.0f, -0.85f, 0.0f   ,

	   

	}  ;

			
			
			//create vao
			GLES32.glGenVertexArrays(1,vaoRedHorizontalLine,0);
			//bind VAO
			GLES32.glBindVertexArray(vaoRedHorizontalLine[0]);

			//create vertex buffer object (VBO)
			GLES32.glGenBuffers(1,vboRedHorizontalLine,0);
			//bind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboRedHorizontalLine[0]);

			//create buffer from our redHorizontalLine array so we can pass it to buffer data

			//allocate buffer directly  from native memeory (not VM memory)

			ByteBuffer byteBuffer=ByteBuffer.allocateDirect(redHorizontalLine.length*4);

			//arrange the byteorder of buffer in native byte order
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			FloatBuffer positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of redHorizontalLine into these cooked buffer
			positionBuffer.put(redHorizontalLine);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,redHorizontalLine.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);

			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//create vbo for vboRedHorizontalLineColor
			//create vertex buffer object (VBO)
			GLES32.glGenBuffers(1,vboRedHorizontalLineColor,0);
			//bind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboRedHorizontalLineColor[0]);

			//create buffer from our redHorizontalLineColor array so we can pass it to buffer data

			//allocate buffer directly  from native memeory (not VM memory)

			 byteBuffer=ByteBuffer.allocateDirect(redHorizontalLineColor.length*4);

			//arrange the byteorder of buffer in native byte order
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			 positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of redHorizontalLine into these cooked buffer
			positionBuffer.put(redHorizontalLineColor);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,redHorizontalLineColor.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR,3,GLES32.GL_FLOAT,false,0,0);

			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR);

			//unbind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//unbind vao (vertex array object)
			GLES32.glBindVertexArray(0);



			//green Vertical Line
			//create vao
			GLES32.glGenVertexArrays(1,vaoGreenVerticalLine,0);
			//bind VAO
			GLES32.glBindVertexArray(vaoGreenVerticalLine[0]);

			//create vertex buffer object (VBO)
			GLES32.glGenBuffers(1,vboGreenVerticalLine,0);
			//bind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboGreenVerticalLine[0]);

			//create buffer from our greenVerticalLine array so we can pass it to buffer data

			//allocate buffer directly  from native memeory (not VM memory)

			 byteBuffer=ByteBuffer.allocateDirect(greenVerticalLine.length*4);

			//arrange the byteorder of buffer in native byte order
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			 positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of redHorizontalLine into these cooked buffer
			positionBuffer.put(greenVerticalLine);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,greenVerticalLine.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);

			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//create vbo for vboGreenVerticalLineColor
			//create vertex buffer object (VBO)
			GLES32.glGenBuffers(1,vboGreenVerticalLineColor,0);
			//bind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboGreenVerticalLineColor[0]);

			//create buffer from our greenVerticalLineColor array so we can pass it to buffer data

			//allocate buffer directly  from native memeory (not VM memory)

			 byteBuffer=ByteBuffer.allocateDirect(greenVerticalLineColor.length*4);

			//arrange the byteorder of buffer in native byte order
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			 positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of redHorizontalLine into these cooked buffer
			positionBuffer.put(greenVerticalLineColor);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,greenVerticalLineColor.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR,3,GLES32.GL_FLOAT,false,0,0);

			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR);

			//unbind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//unbind vao (vertex array object)
			GLES32.glBindVertexArray(0);

			//multiple vertical lines 
			GLES32.glGenVertexArrays(1,vaoVerticalLines,0);
			//bind VAO
			GLES32.glBindVertexArray(vaoVerticalLines[0]);

			//create vertex buffer object (VBO)
			GLES32.glGenBuffers(1,vboVerticalLines,0);
			//bind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboVerticalLines[0]);

			//create buffer from our graphVerticalLines array so we can pass it to buffer data

			//allocate buffer directly  from native memeory (not VM memory)

			 byteBuffer=ByteBuffer.allocateDirect(graphVerticalLines.length*4);

			//arrange the byteorder of buffer in native byte order
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			 positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of redHorizontalLine into these cooked buffer
			positionBuffer.put(graphVerticalLines);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,graphVerticalLines.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);

			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);
			GLES32.glVertexAttrib3f(GLESMacros.AMC_ATTRIBUTE_COLOR,0.0f,0.0f,1.0f);
			//unbind vao (vertex array object)
			GLES32.glBindVertexArray(0);






			//multiple horizontal lines
			GLES32.glGenVertexArrays(1,vaoHorizontalLines,0);
			//bind VAO
			GLES32.glBindVertexArray(vaoHorizontalLines[0]);

			//create vertex buffer object (VBO)
			GLES32.glGenBuffers(1,vboHorizontalLines,0);
			//bind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboHorizontalLines[0]);

			//create buffer from our graphHorizontalLines array so we can pass it to buffer data

			//allocate buffer directly  from native memeory (not VM memory)

			 byteBuffer=ByteBuffer.allocateDirect(graphHorizontalLines.length*4);

			//arrange the byteorder of buffer in native byte order
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			 positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of redHorizontalLine into these cooked buffer
			positionBuffer.put(graphHorizontalLines);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,graphHorizontalLines.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);

			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);
			GLES32.glVertexAttrib3f(GLESMacros.AMC_ATTRIBUTE_COLOR,0.0f,0.0f,1.0f);
			//unbind vao (vertex array object)
			GLES32.glBindVertexArray(0);
	
			GLES32.glEnable(GLES32.GL_DEPTH_TEST);
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

		//multiple vertical lines
		Matrix.setIdentityM(modelViewMatrix,0);

		Matrix.translateM(modelViewMatrix,0,0.0f,0.0f,-2.0f);
		
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		//BIND WITH vao
		GLES32.glBindVertexArray(vaoVerticalLines[0]);
		GLES32.glDrawArrays(GLES32.GL_LINES,0,120);
		GLES32.glBindVertexArray(0);


		//multiple horizontal lines
		Matrix.setIdentityM(modelViewMatrix,0);

		Matrix.translateM(modelViewMatrix,0,0.0f,0.0f,-2.0f);
		
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		//BIND WITH vao
		GLES32.glBindVertexArray(vaoHorizontalLines[0]);
		GLES32.glDrawArrays(GLES32.GL_LINES,0,68);
		GLES32.glBindVertexArray(0);

		//red horizontalLine
		Matrix.setIdentityM(modelViewMatrix,0);

		Matrix.translateM(modelViewMatrix,0,0.0f,0.0f,-1.0f);
		
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		//BIND WITH vao
		GLES32.glBindVertexArray(vaoRedHorizontalLine[0]);
		GLES32.glLineWidth(50.0f);
		GLES32.glDrawArrays(GLES32.GL_LINES,0,2);
		GLES32.glBindVertexArray(0);

		//green vertical line
		Matrix.setIdentityM(modelViewMatrix,0);

		Matrix.translateM(modelViewMatrix,0,0.0f,0.0f,-2.0f);
		
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		//BIND WITH vao
		GLES32.glBindVertexArray(vaoGreenVerticalLine[0]);
		GLES32.glLineWidth(50.0f);
		GLES32.glDrawArrays(GLES32.GL_LINES,0,2);
		GLES32.glBindVertexArray(0);

		
		GLES32.glUseProgram(0);
		requestRender();
	}

	private void uninitialize()
	{
		if(vboVerticalLines[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vboVerticalLines,0);
			vboVerticalLines[0]=0;
		}

		if(vaoVerticalLines[0]!=0)
		{
			GLES32.glDeleteVertexArrays(1,vaoVerticalLines,0);
			vaoVerticalLines[0]=0;
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
