package com.RTR.Tesselation;

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
	private int gTessellationControlShaderObject;
	private int gTessellationEvaluationShaderObject;
	private int fragmentShaderObject;
	private int shaderProgramObject;

	private int[] vao=new int[1];
	private int[] vbo=new int[1];

	private int mvpUniform;
	private int gNumberOfSegmentsUniform;
	private int gNumberOfStripsUniform;
	private int gLineColorUniform;

	private int gNumberOfLineSegments;
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
					gNumberOfLineSegments=gNumberOfLineSegments+1;
		if(gNumberOfLineSegments>30)
			gNumberOfLineSegments=30;
		
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
				System.exit(0);
		uninitialize();
	}

	//abstract method from OnGestureListener so must be implemented
	@Override
	public boolean onScroll(MotionEvent e1,MotionEvent e2,float distanceX,float distanceY)
	{
			gNumberOfLineSegments=gNumberOfLineSegments-1;
			if(gNumberOfLineSegments<=0)
				gNumberOfLineSegments=1;
		
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
		"in vec2 vPosition;" +
		"void main(void)" +
		"{" +
		"gl_Position=vec4(vPosition,0.0,1.0);" +
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



		//==================  TESSELLATION CONTROL SHADER =====================
		gTessellationControlShaderObject=GLES32.glCreateShader(GLES32.GL_TESS_CONTROL_SHADER);
			//TESSELLATION shader code
		final String tessellationControlShaderSourceCode=
		String.format
		(
		"#version 320 es" +
		"\n" +
		"layout(vertices = 4) out;" +
		"uniform int numberOfSegments;" +
		"uniform int numberOfStrips;" +
		"void main(void)" +
		"{" +
		"gl_out[gl_InvocationID].gl_Position=gl_in[gl_InvocationID].gl_Position;" +
		"gl_TessLevelOuter[0]=float(numberOfStrips);" +
		"gl_TessLevelOuter[1]=float(numberOfSegments);" +
		"}"
		);

		//specify above shader source code to TESSELLATION
		//give shader source code
		GLES32.glShaderSource(gTessellationControlShaderObject,tessellationControlShaderSourceCode);

		//compile the TESSELLATION shader code
		GLES32.glCompileShader(gTessellationControlShaderObject);

		//error checking code for gTessellationControlShaderObject shader

		 iShaderCompileStatus[0]=0;
		 iInfoLogLength[0]=0;
		 szInfoLog=null;

		GLES32.glGetShaderiv(gTessellationControlShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompileStatus,0);

		if(iShaderCompileStatus[0]==GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(gTessellationControlShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

			if(iInfoLogLength[0]>0)
			{
				szInfoLog=GLES32.glGetShaderInfoLog(gTessellationControlShaderObject);
				System.out.println("AMC: Tessellation Control Shader Log:"+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}





		//==================  TESSELLATION EVALUATION SHADER =====================
		gTessellationEvaluationShaderObject=GLES32.glCreateShader(GLES32.GL_TESS_EVALUATION_SHADER);
			//TESSELLATION shader code
		final String tessellationEvaluationShaderSourceCode=
		String.format
		(
		"#version 320 es" +
		"\n" +
		"layout(isolines)in;" +
		"uniform mat4 u_mvp_matrix;" +
		"void main(void)" +
		"{" +
		"float u=gl_TessCoord.x;" +
		"vec3 p0 = gl_in[0].gl_Position.xyz;" +
		"vec3 p1 = gl_in[1].gl_Position.xyz;" +
		"vec3 p2 = gl_in[2].gl_Position.xyz;" +
		"vec3 p3 = gl_in[3].gl_Position.xyz;" +
		"float u1= (1.0-u);" +
		"float u2= u * u;" +
		"float b3= u2 * u ;" +
		"float b2= 3.0 * u2 * u1;" +
		"float b1= 3.0 * u * u1 * u1;" +
		"float b0= u1 * u1 * u1;" +
		"vec3 p= p0*b0 + p1*b1 + p2*b2 + p3*b3;" +
		"gl_Position =u_mvp_matrix * vec4(p,1.0) ;" +
		"}"
		);

		//specify above shader source code to TESSELLATION
		//give shader source code
		GLES32.glShaderSource(gTessellationEvaluationShaderObject,tessellationEvaluationShaderSourceCode);

		//compile the TESSELLATION shader code
		GLES32.glCompileShader(gTessellationEvaluationShaderObject);

		//error checking code for gTessellationControlShaderObject shader

		 iShaderCompileStatus[0]=0;
		 iInfoLogLength[0]=0;
		 szInfoLog=null;

		GLES32.glGetShaderiv(gTessellationEvaluationShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompileStatus,0);

		if(iShaderCompileStatus[0]==GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(gTessellationEvaluationShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

			if(iInfoLogLength[0]>0)
			{
				szInfoLog=GLES32.glGetShaderInfoLog(gTessellationEvaluationShaderObject);
				System.out.println("AMC: Tessellation Evaluation Shader Compilation  Log:"+szInfoLog);
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
		"uniform vec4 lineColor;" +
		"out vec4 FragColor;" +
		"void main(void)" +
		"{" +
			"FragColor=lineColor;" +
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

			//attach gTessellationControlShaderObject shader to shader program
			GLES32.glAttachShader(shaderProgramObject,gTessellationControlShaderObject);

			//attach gTessellationEvaluationShaderObject shader  to shaderprogram
			GLES32.glAttachShader(shaderProgramObject,gTessellationEvaluationShaderObject);

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
			gNumberOfSegmentsUniform=GLES32.glGetUniformLocation(shaderProgramObject,"numberOfSegments");
			gNumberOfStripsUniform=GLES32.glGetUniformLocation(shaderProgramObject,"numberOfStrips");
			gLineColorUniform=GLES32.glGetUniformLocation(shaderProgramObject,"lineColor");

			final float[] Vertices=new float[]
			{
					-1.0f,-1.0f,-0.5f,1.0f,0.5f,-1.0f,1.0f,1.0f
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

			ByteBuffer byteBuffer=ByteBuffer.allocateDirect(Vertices.length*4);

			//arrange the byteorder of buffer in native byte order
			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER
			FloatBuffer positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of triangle into these cooked buffer
			positionBuffer.put(Vertices);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);


			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,Vertices.length*4,positionBuffer,GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,2,GLES32.GL_FLOAT,false,0,0);

			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//unbind vao (vertex array object)
			GLES32.glBindVertexArray(0);

			GLES32.glEnable(GLES32.GL_DEPTH_TEST);
			//GLES32.glEnable(GLES32.GL_CULL_FACE);
			GLES32.glDepthFunc(GLES32.GL_LEQUAL);
			GLES32.glClearColor(0.0f,0.0f,0.0f,1.0f);
			
			gNumberOfLineSegments = 1;
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
		GLES32.glLineWidth(5.0f);
		//declaration of matrices
		float[] modelViewMatrix=new float[16];
		float[] modelViewProjectionMatrix=new float[16];


		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(modelViewMatrix,0);

		Matrix.translateM(modelViewMatrix,0,0.5f,0.5f,-2.0f);
		
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);
		GLES32.glUniform1i(gNumberOfSegmentsUniform,gNumberOfLineSegments);
		GLES32.glUniform1i(gNumberOfStripsUniform,1);
		if(gNumberOfLineSegments>1 &&  gNumberOfLineSegments<30)
		{
			GLES32.glUniform4f(gLineColorUniform,0.0f,1.0f,0.0f,1.0f);
		}
		else if(gNumberOfLineSegments==30)
		{
			GLES32.glUniform4f(gLineColorUniform,1.0f,0.0f,0.0f,1.0f);
		}
		else
		{
			GLES32.glUniform4f(gLineColorUniform,1.0f,1.0f,0.0f,1.0f);
		}
		//BIND WITH vao
		GLES32.glBindVertexArray(vao[0]);
		GLES32.glDrawArrays(GLES32.GL_PATCHES,0,4);
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
