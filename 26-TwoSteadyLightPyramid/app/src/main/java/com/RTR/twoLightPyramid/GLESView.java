package com.RTR.twoLightPyramid;

//added by me

import android.content.Context;
import android.view.Gravity;
import android.graphics.Color;

//for sphere
import java.nio.ShortBuffer;

//view which will support OpenGL
import android.opengl.GLSurfaceView;

//for OpenGL BUFFERS
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

//for matrix math
import android.opengl.Matrix;

//for OpenGL ES  version -3.2
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
	
	private float anglePyramid=0.0f;
	private float rotationSpeed=0.5f;

	
	private int[] vaoPyramid=new int[1];
	private int[] vboPyramidPosition=new int[1];
	private int[] vboNormal=new int[1];
	
	private int modelMatrixUniform;
	private int viewMatrixUniform;
	private int projectionMatrixUniform;
	
	//light Uniforms  Red Light 
	private int lightDiffuseUniformRedLight;
	private int lightAmbientUniformRedLight;
	private int lightSpecularUniformRedLight;
	private int lightPositionUniformRedLight;

	//light Uniforms  Blue Light 
	private int lightDiffuseUniformBlueLight;
	private int lightAmbientUniformBlueLight;
	private int lightSpecularUniformBlueLight;
	private int lightPositionUniformBlueLight;

	//material Uniforms

	private int materialDiffuseUniform;
	private int materialAmbientUniform;
	private int materialSpecularUniform;
	private int materialShininessUniform;

	private int lKeyPressUniform;

	private boolean bLight=true;
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
		if(bLight==true)
		{
			bLight=false;
		}
		else
		{
			bLight=true;
		}
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
		"in vec3 vNormal;" +
		"uniform mat4 u_model_matrix;" +
		"uniform mat4 u_view_matrix;" +
		"uniform mat4 u_projection_matrix;" +
		"uniform vec4 u_lightPositionRed;" +
		"uniform vec4 u_lightPositionBlue;" +
		"uniform mediump int lKeyPress;" +
		"out vec3 lightdirectionRedLight;" +
		"out vec3 lightdirectionBlueLight;" +
		"out vec3 tnorm;" +
		"out vec3 viewer_vector;" +
		"void main(void)" +
		"{" +
		"	if(lKeyPress==1)" +
		"{" +
		"vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;" +
		"tnorm                = mat3(u_view_matrix * u_model_matrix) * vNormal;"+
		"lightdirectionRedLight=vec3(u_lightPositionRed) - eye_coordinates.xyz;" +
		"lightdirectionBlueLight=vec3(u_lightPositionBlue) - eye_coordinates.xyz;" +
		"viewer_vector=-eye_coordinates.xyz;" +
		"}" +
		"gl_Position=u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;" +
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
				System.out.println("AMC: vertex shader compilation Log:"+szInfoLog);
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
		"precision highp float;"+
    	"in vec3 lightdirectionRedLight;" +
		"in vec3 lightdirectionBlueLight;" +
		"in vec3 tnorm;" +
    	"in vec3 viewer_vector;" +
    	"out vec4 FragColor;" +
		"uniform vec3 u_lightambientRed;" +
		"uniform vec3 u_lightdiffuseRed;" +
		"uniform vec3 u_lightspecularRed;" +
		"uniform vec3 u_lightambientBlue;" +
		"uniform vec3 u_lightdiffuseBlue;" +
		"uniform vec3 u_lightspecularBlue;" +
		"uniform vec3 u_materialambient;" +
		"uniform vec3 u_materialdiffuse;" +
		"uniform vec3 u_materialspecular;" +
		"uniform float materialshinines;" +
		"uniform int lKeyPress;" +
		"void main(void)" +
	"{" +
		"vec3 phong_ADS_light;" +
		"if(lKeyPress==1)" +
		"{" +
			"vec3 normalizetnorm=normalize(tnorm);" +
			"vec3 lightDirectionRed=normalize(lightdirectionRedLight);" +
			"vec3 lightDirectionBlue=normalize(lightdirectionBlueLight);" +

			"vec3 normalizeviewervector=normalize(viewer_vector);" +
			
			"float tn_dot_ld_red=max(dot(lightDirectionRed,normalizetnorm),0.0);" +
			"float tn_dot_ld_blue=max(dot(lightDirectionBlue,normalizetnorm),0.0);" +

			"vec3 reflection_vector_red=reflect(-lightDirectionRed,normalizetnorm);" +
			"vec3 reflection_vector_blue=reflect(-lightDirectionBlue,normalizetnorm);" +

			
			"vec3 ambient_red  = u_lightambientRed  * u_materialambient;" +
			"vec3 diffuse_red  = u_lightdiffuseRed  * u_materialdiffuse * tn_dot_ld_red;" +
			"vec3 specular_red = u_lightspecularRed * u_materialspecular * pow(max(dot(reflection_vector_red,normalizeviewervector),0.0),materialshinines);" +
			
			"vec3 ambient_blue  = u_lightambientBlue  * u_materialambient;" +
			"vec3 diffuse_blue  = u_lightdiffuseBlue  * u_materialdiffuse * tn_dot_ld_blue;" +
			"vec3 specular_blue = u_lightspecularBlue * u_materialspecular * pow(max(dot(reflection_vector_blue,normalizeviewervector),0.0),materialshinines);" +
			"phong_ADS_light  =	ambient_red+ambient_blue + diffuse_red+diffuse_blue + specular_red+specular_blue;"+
		"}"	+
		"else" +
		"{" +
			"phong_ADS_light=vec3(1.0,1.0,1.0);" +
		"}" +
		"FragColor=vec4(phong_ADS_light,1.0);" +
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
				 System.out.println("AMC: fragment shader compilation log:"+szInfoLog);
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
			GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.AMC_ATTRIBUTE_NORMAL,"vNormal");

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
					System.out.println("AMC: Shader Program Link Log:"+szInfoLog);
					uninitialize();
					System.exit(0);
				}
			}

		modelMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_model_matrix");			
		viewMatrixUniform  = GLES32.glGetUniformLocation(shaderProgramObject, "u_view_matrix");
		projectionMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_projection_matrix");

		lightAmbientUniformRedLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightambientRed");			
		lightDiffuseUniformRedLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightdiffuseRed");
		lightSpecularUniformRedLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightspecularRed");
		lightPositionUniformRedLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightPositionRed");

		lightAmbientUniformBlueLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightambientBlue");			
		lightDiffuseUniformBlueLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightdiffuseBlue");
		lightSpecularUniformBlueLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightspecularBlue");
		lightPositionUniformBlueLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightPositionBlue");

		materialAmbientUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_materialambient");
		materialDiffuseUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_materialdiffuse");
		materialSpecularUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_materialspecular");
		materialShininessUniform = GLES32.glGetUniformLocation(shaderProgramObject, "materialshinines");
		lKeyPressUniform = GLES32.glGetUniformLocation(shaderProgramObject, "lKeyPress");


			float[] pyramidVertices=new float[] 
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

			float[] pyramidNormal=new float[] 
			{
				0.0f, 0.447214f, 0.894427f,
				0.0f, 0.447214f, 0.894427f,
				0.0f, 0.447214f, 0.894427f,

				0.894427f, 0.447214f, 0.0f,
				0.894427f, 0.447214f, 0.0f,
				0.894427f, 0.447214f, 0.0f,

				0.0f, 0.447214f, -0.894427f,
				0.0f, 0.447214f, -0.894427f,
				0.0f, 0.447214f, -0.894427f,

				-0.894427f, 0.447214f, 0.0f,
				-0.894427f, 0.447214f, 0.0f,
				-0.894427f, 0.447214f, 0.0f,
			};



			
			//--------------------------PYRAMID ---------------------------------------------->>>>>

			
			//<<<<<------------------START VAO FOR PYRAMID ----------------------------->>>>
			
			//create vertex array object for PYRAMID (vao)
			GLES32.glGenVertexArrays(1,vaoPyramid,0);

			//bind vao for PYRAMID
			GLES32.glBindVertexArray(vaoPyramid[0]);


			//<<<<<---------START VBO FOR PYRAMID POSITION ---------------------------->>>>>
			//create buffer for PYRAMID position 
			GLES32.glGenBuffers(1,vboPyramidPosition,0);

			//bind buffer
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboPyramidPosition[0]);


			//create buffer from our pyramidVertices array so we can pass it to glBufferData()

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

			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,
								pyramidVertices.length*4,
								positionBuffer,
								GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);
			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo for pyramid position  
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//<<<<<<------------END VBO FOR PYRAMID POSITION------------------------------------------------>>>>

			






			//<<<<<-------------START VBO FOR pyramid NORMAL-------------------------->>>>>>>
			GLES32.glGenBuffers(1,vboNormal,0);
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboNormal[0]);

			//create buffer from our pyramidNormal array so we can pass it to glBufferData()
			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect
			 byteBuffer=ByteBuffer.allocateDirect(pyramidNormal.length*4);

			//arrange the byteorder of buffer in native byte order little indian/big indian
			byteBuffer.order(ByteOrder.nativeOrder());
			
			//create the float type buffer and convert our byte type buffer into float type BUFFER
			 positionBuffer=byteBuffer.asFloatBuffer();
			
			//now put our array of sphere_normals  into these cooked buffer
			positionBuffer.put(pyramidNormal);
			
			//set array at zeroth position of BUFFER
			positionBuffer.position(0);
			
			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,
								pyramidNormal.length*4,
								positionBuffer,
								GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_NORMAL,
										 3,
										 GLES32.GL_FLOAT,
										 false,0,0);
			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_NORMAL);

			//unbind vbo for pyramid normals  
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//<<<<<<--------------- END VBO FOR  PYRAMID NORMALS ------------->>>>>>>>>>>>>>



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
		float[] modelMatrix=new float[16];
		float[] rotationMatrix=new float[16];
		float[] translationaMatrix=new float[16];
		float[] viewMatrix=new float[16];

		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(rotationMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			
			GLES32.glUniform3f(lightAmbientUniformRedLight, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniformRedLight, 1.0f, 0.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniformRedLight, 1.0f, 0.0f, 1.0f);
			GLES32.glUniform4f(lightPositionUniformRedLight, -2.0f, 0.0f, 0.0f,1.0f);
			
			GLES32.glUniform3f(lightAmbientUniformBlueLight, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniformBlueLight, 0.0f, 0.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniformBlueLight, 0.0f, 0.0f, 1.0f);
			GLES32.glUniform4f(lightPositionUniformBlueLight, 2.0f, 0.0f, 0.0f,1.0f);
			
			GLES32.glUniform3f(materialAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(materialSpecularUniform, 1.0f, 1.0f, 1.0f);
			
			GLES32.glUniform1f(materialShininessUniform, 128.0f);

		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,0.0f,0.0f,-6.0f);
		Matrix.rotateM(rotationMatrix,0,anglePyramid,0.0f,1.0f,0.0f);
		
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,rotationMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,perspectiveProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);


		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vaoPyramid[0]);
		
		GLES32.glDrawArrays(GLES32.GL_TRIANGLES,0,12);
		
		GLES32.glBindVertexArray(0);

		GLES32.glUseProgram(0);
		requestRender();
	}
	private void update()
	{	
		anglePyramid+=rotationSpeed;
		if(anglePyramid >= 360.0f)
		{
			anglePyramid=0.0f;
		}
	
	}
	private void uninitialize()
	{
		if(vboNormal[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vboNormal,0);
			vboNormal[0]=0;
		}

		if(vboPyramidPosition[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vboPyramidPosition,0);
			vboPyramidPosition[0]=0;
		}

		if(vaoPyramid[0]!=0)
		{
			GLES32.glDeleteVertexArrays(1,vaoPyramid,0);
			vaoPyramid[0]=0;
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
