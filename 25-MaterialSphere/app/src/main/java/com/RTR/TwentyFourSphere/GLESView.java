package com.RTR.TwentyFourSphere;

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

import java.lang.Math;
//implement all the method from implemented class
public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener,OnDoubleTapListener
{
	private GestureDetector gestureDetector; //field class varaible
	private final Context context;
	private int vertexShaderObject;
	private int fragmentShaderObject;
	private int shaderProgramObject;
	
	private int[] vao_sphere=new int[1];
	private int[] vbo_sphere_position=new int[1];
	private int[] vbo_sphere_normal=new int[1];
	private int[] vbo_sphere_element=new int[1];
	
	private int modelMatrixUniform;
	private int viewMatrixUniform;
	private int projectionMatrixUniform;
	
	//light Uniforms
	private int lightAmbientUniform;
	private int lightDiffuseUniform;
	private int lightSpecularUniform;
	private int lightPositionUniform;

	//material Uniforms
	private int materialDiffuseUniform;
	private int materialAmbientUniform;
	private int materialSpecularUniform;
	private int materialShininessUniform;
	private int lKeyPressUniform;

	float[] MaterialAmbientEmerald=new float[]{0.0215f, 0.1745f,0.0215f,1.0f};
	float[] MaterialDiffuseEmerald = new float[] {0.07568f,0.61424f,0.07568f, 1.0f};
	float[] MaterialSpecularEmerald = new float[] {0.633f,0.727811f,0.633f, 1.0f};
	float MaterialShininessEmerald = 0.6f * 128.0f;
	float[] TranslateValuesEmerald = new float[] {1.5f,14.0f, 0.0f, 1.0f};
	float[] lightPosition = new float[] {0.0f,0.0f, 0.0f, 1.0f};


	private int numElements;
    private int numVertices;
	private int gWidth;
	private int gHeight;
	private boolean bLight=true;

	private float lightAngleZero=0.0f;
	private int keyPress=0;

	private float[]orthographicProjectionMatrix=new float[16];
	//private float[]perspectiveProjectionMatrix=new float[16];
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
		keyPress=keyPress+1;
		if(keyPress>3)
			keyPress=0;
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
		gWidth=width;
		gHeight=height;
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
		"uniform vec4 u_light_position;" +
		"uniform mediump int lKeyPress;" +
		"out vec3 lightdirection;" +
		"out vec3 tnorm;" +
		"out vec3 viewer_vector;" +
		"void main(void)" +
		"{" +
		"	if(lKeyPress==1)" +
		"{" +
		"vec4 eye_coordinates=u_view_matrix*u_model_matrix*vPosition;" +
		"tnorm=mat3(u_view_matrix*u_model_matrix)*vNormal;"+
		"lightdirection=vec3(u_light_position) - eye_coordinates.xyz;" +
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
    	"in vec3 lightdirection;" +
		"in vec3 tnorm;" +
    	"in vec3 viewer_vector;" +
    	"out vec4 FragColor;" +
		"uniform vec3 u_lightambient;" +
		"uniform vec3 u_lightdiffuse;" +
		"uniform vec3 u_lightspecular;" +
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
			"vec3 normalizelightdirection=normalize(lightdirection);" +
			"vec3 normalizeviewervector=normalize(viewer_vector);" +
			"float tn_dot_ld=max(dot(normalizetnorm,normalizelightdirection),0.0);" +
			"vec3 reflection_vector=reflect(-normalizelightdirection,normalizetnorm);" +
			"vec3 ambient=u_lightambient*u_materialambient;" +
			"vec3 diffuse=u_lightdiffuse*u_materialdiffuse*tn_dot_ld;" +
			"vec3 specular=u_lightspecular*u_materialspecular*pow(max(dot(reflection_vector,normalizeviewervector),0.0),materialshinines);" +
			"phong_ADS_light=ambient+diffuse+specular;" +
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

		lightAmbientUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightambient");			
		lightDiffuseUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightdiffuse");
		lightSpecularUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightspecular");
		lightPositionUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_light_position");

		materialAmbientUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_materialambient");
		materialDiffuseUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_materialdiffuse");
		materialSpecularUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_materialspecular");
		materialShininessUniform = GLES32.glGetUniformLocation(shaderProgramObject, "materialshinines");
		lKeyPressUniform = GLES32.glGetUniformLocation(shaderProgramObject, "lKeyPress");


			Sphere sphere=new Sphere();
			float sphere_vertices[]=new float[1146];
			float sphere_normals[]=new float[1146];
			float sphere_textures[]=new float[764];
			short sphere_elements[]=new short[2280];
			sphere.getSphereVertexData(sphere_vertices,sphere_normals,sphere_textures,sphere_elements);
			numVertices = sphere.getNumberOfSphereVertices();
			numElements = sphere.getNumberOfSphereElements();
		
			//--------------------------SPHERE ---------------------------------------------->>>>>

			
			//<<<<<------------------START VAO FOR SPHERE ----------------------------->>>>
			
			//create vertex array object for SPHERE (vao)
			GLES32.glGenVertexArrays(1,vao_sphere,0);

			//bind vao for SPHERE
			GLES32.glBindVertexArray(vao_sphere[0]);


			//<<<<<---------START VBO FOR SPHERE POSITION ---------------------------->>>>>
			//create buffer for sphere position 
			GLES32.glGenBuffers(1,vbo_sphere_position,0);

			//bind buffer
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_sphere_position[0]);


			//create buffer from our sphereVertices array so we can pass it to glBufferData()

			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect

			ByteBuffer byteBuffer=ByteBuffer.allocateDirect(sphere_vertices.length*4);


			//arrange the byteorder of buffer in native byte order little indian/big indian

			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER

			FloatBuffer positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of sphere_vertices into these cooked buffer
			positionBuffer.put(sphere_vertices);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);

			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,
								sphere_vertices.length*4,
								positionBuffer,
								GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);
			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

			//unbind vbo for sphere position  
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//<<<<<<------------END VBO FOR SPHERE POSITION------------------------------------------------>>>>

			//<<<<<-------------START VBO FOR SPHERE NORMAL-------------------------->>>>>>>
			GLES32.glGenBuffers(1,vbo_sphere_normal,0);
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_sphere_normal[0]);

			//create buffer from our sphere_normal array so we can pass it to glBufferData()
			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect
			 byteBuffer=ByteBuffer.allocateDirect(sphere_normals.length*4);

			//arrange the byteorder of buffer in native byte order little indian/big indian
			byteBuffer.order(ByteOrder.nativeOrder());
			
			//create the float type buffer and convert our byte type buffer into float type BUFFER
			 positionBuffer=byteBuffer.asFloatBuffer();
			
			//now put our array of sphere_normals  into these cooked buffer
			positionBuffer.put(sphere_normals);
			
			//set array at zeroth position of BUFFER
			positionBuffer.position(0);
			
			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,
								sphere_normals.length*4,
								positionBuffer,
								GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_NORMAL,
										 3,
										 GLES32.GL_FLOAT,
										 false,0,0);
			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_NORMAL);

			//unbind vbo for sphere normals  
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

			//<<<<<<--------------- END VBO FOR  SPHERE NORMALS ------------->>>>>>>>>>>>>>

			//<<<<<-------------START VBO FOR SPHERE ELEMENTS-------------------------->>>>>>>
			GLES32.glGenBuffers(1,vbo_sphere_element,0);
			GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);

			//create buffer from our sphere_elements array so we can pass it to glBufferData()
			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect
			 byteBuffer=ByteBuffer.allocateDirect(sphere_elements.length*2);

			//arrange the byteorder of buffer in native byte order little indian/big indian
			byteBuffer.order(ByteOrder.nativeOrder());
			
			ShortBuffer elementsBuffer=byteBuffer.asShortBuffer();
			
			//now put our array of sphere_elements  into these cooked buffer
			elementsBuffer.put(sphere_elements);
			
			//set array at zeroth position of BUFFER
			elementsBuffer.position(0);
			
			GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER,
								sphere_elements.length*2,
								elementsBuffer,
								GLES32.GL_STATIC_DRAW);

			//unbind vbo for sphere elements  
			GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,0);

			//<<<<<<--------------- END VBO FOR  SPHERE NORMALS ------------->>>>>>>>>>>>>>

			//unbind vao fo SPHERE 
			GLES32.glBindVertexArray(0);

			//<<<<<<<<-----------------------END VAO FOR SPHERE--------------------------------->>>>>>

			GLES32.glEnable(GLES32.GL_DEPTH_TEST);
			//disable cull face so we can see back side of geometry while rotation
			GLES32.glDisable(GLES32.GL_CULL_FACE);
			GLES32.glDepthFunc(GLES32.GL_LEQUAL);
			GLES32.glClearColor(0.25f,0.25f,0.25f,1.0f);

			Matrix.setIdentityM(orthographicProjectionMatrix,0);
			//Matrix.setIdentityM(perspectiveProjectionMatrix,0);

			
	}
	
	private void resize(int width,int height)
	{
		if(height==0)
		{
			height=1;
		}
		GLES32.glViewport(0,0,width,height);
		//Matrix.perspectiveM(perspectiveProjectionMatrix,0,45.0f,(float)width/(float)height,0.1f,100.0f);
		if (width<=height)
		{
			Matrix.orthoM(orthographicProjectionMatrix,
			0,
			0.0f,
			15.5f,
			0.0f,
			(15.5f*(float)height/(float)width),
			-10.0f,
			10.0f);

		}
		else
		{
			Matrix.orthoM(orthographicProjectionMatrix,
			0,
			0.0f,
			(15.5f*(float)width/(float)height),
			0.01f,
			15.5f,
			-10.0f,
			10.0f);
		}
	}



	private void display()
	{
		GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT|GLES32.GL_DEPTH_BUFFER_BIT);
		GLES32.glUseProgram(shaderProgramObject);

		//declaration of matrices
		float[] modelMatrix=new float[16];
		float[] scaleMatrix=new float[16];
		float[] translationaMatrix=new float[16];
		float[] viewMatrix=new float[16];

		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		

		if(keyPress==1)
		{
			lightPosition[1]=(float)Math.cos(lightAngleZero)*16.0f;
			lightPosition[2]=(float)Math.sin(lightAngleZero)*16.0f;;

		}
		else if(keyPress==2)
		{
			lightPosition[0]=(float)Math.cos(lightAngleZero)*16.0f;
			 lightPosition[2]=(float)Math.sin(lightAngleZero)*16.0f;;

		}
		else if(keyPress==3)
		{
			lightPosition[0]=(float)Math.cos(lightAngleZero)*16.0f;
			lightPosition[1]=(float)Math.sin(lightAngleZero)*16.0f;;

		}
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3fv(materialAmbientUniform,1, MaterialAmbientEmerald,0);
			GLES32.glUniform3fv(materialDiffuseUniform, 1,MaterialDiffuseEmerald,0);
			GLES32.glUniform3fv(materialSpecularUniform,1,MaterialSpecularEmerald,0);
			
			GLES32.glUniform1f(materialShininessUniform, MaterialShininessEmerald);

		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,TranslateValuesEmerald[0],TranslateValuesEmerald[1],TranslateValuesEmerald[2]);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);



//==========================jade 2 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.135f, 0.2225f, 0.1575f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.54f,0.89f,0.63f);
			GLES32.glUniform3f(materialSpecularUniform, 0.316228f, 0.316228f, 0.316228f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.1f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 1.5f,11.5f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end JADE 2 ===========================================


//==========================obsidian 3 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.05375f, 0.05f, 0.06625f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.18275f,0.17f,0.22525f);
			GLES32.glUniform3f(materialSpecularUniform, 0.332741f, 0.328634f, 0.346435f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.3f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,1.5f ,9.0f ,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end obsidian 3 ===========================================



//==========================pearl 4 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.25f, 0.20725f, 0.20725f);
			GLES32.glUniform3f(materialDiffuseUniform, 1.0f,0.829f,0.829f);
			GLES32.glUniform3f(materialSpecularUniform, 0.296648f, 0.296648f, 0.296648f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.088f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,1.5f , 6.5f ,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end pearl 4 ===========================================


//==========================ruby 5 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.1745f, 0.01175f, 0.01175f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.61424f,0.04136f,0.04136f);
			GLES32.glUniform3f(materialSpecularUniform, 0.727811f, 0.626959f, 0.626959f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.6f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 1.5f, 4.0f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end ruby 5 ===========================================

//==========================turquoise 6 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.1f, 0.18725f, 0.1745f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.396f,0.74151f,0.69102f);
			GLES32.glUniform3f(materialSpecularUniform, 0.297254f, 0.30829f, 0.306678f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.1f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,  1.5f, 1.5f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end turquoise 6 ===========================================

//==========================brass 7 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.329412f, 0.223529f, 0.027451f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.780392f,0.568627f,0.113725f);
			GLES32.glUniform3f(materialSpecularUniform, 0.992157f, 0.941176f, 0.807843f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.21794872f* 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 9.0f, 14.0f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end brass 7 ===========================================

//==========================bronze 8 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.2125f, 0.1275f, 0.054f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.714f,0.4284f,0.18144f);
			GLES32.glUniform3f(materialSpecularUniform, 0.393548f, 0.271906f, 0.166721f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.2f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,9.0f , 11.5f ,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end bronze 8 ===========================================

//==========================chrome 9 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.25f, 0.25f, 0.25f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.4f,0.4f,0.4f);
			GLES32.glUniform3f(materialSpecularUniform, 0.774597f, 0.774597f, 0.774597f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.6f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,9.0f ,9.0f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end chrome 9 ===========================================

//==========================copper 10 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.19125f, 0.0735f, 0.0225f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.7038f,0.27048f,0.0828f);
			GLES32.glUniform3f(materialSpecularUniform, 0.256777f, 0.137622f, 0.086014f);
			
			GLES32.glUniform1f(materialShininessUniform, 0.1f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,  9.0f, 6.5f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end copper 10 ===========================================

//==========================Gold 11 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.24725f, 0.1995f, 0.0745f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.75164f,0.60648f,0.22648f);
			GLES32.glUniform3f(materialSpecularUniform, 0.628281f, 0.555802f, 0.366065f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.4f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 9.0f ,  4.0f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end Gold 11 ===========================================

//==========================Silver 12 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.19225f, 0.19225f, 0.19225f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.50754f,0.50754f,0.50754f);
			GLES32.glUniform3f(materialSpecularUniform, 0.508273f, 0.508273f, 0.508273f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.4f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 9.0f,1.5f ,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end Silver 12 ===========================================

//==========================black-Plastic 13 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.01f,0.01f,0.01f);
			GLES32.glUniform3f(materialSpecularUniform, 0.50f, 0.50f, 0.50f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.25f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,17.25f ,14.0f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end black-Plastic 13 ===========================================

//==========================Cyan 14 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.0f, 0.1f, 0.06f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.0f,0.50980392f,0.50980392f);
			GLES32.glUniform3f(materialSpecularUniform, 0.50196078f, 0.50196078f, 0.50196078f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.25f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 17.25f ,11.5f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end Cyan 14 ===========================================

//==========================/Green Plastic 15 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.01f,0.35f,0.1f);
			GLES32.glUniform3f(materialSpecularUniform, 0.45f, 0.55f, 0.45f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.25f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 17.25f,9.0f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end /Green Plastic 15 ===========================================

//==========================Red Plastic 16 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.5f,0.0f,0.01f);
			GLES32.glUniform3f(materialSpecularUniform, 0.7f, 0.6f, 0.6f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.25f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,17.25f , 6.5f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end Red Plastic 16 ===========================================

//==========================White Plastic  17 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.55f,0.55f,0.55f);
			GLES32.glUniform3f(materialSpecularUniform, 0.70f, 0.70f, 0.70f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.25f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,17.25f ,4.0f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end White Plastic  17 ===========================================

//==========================Yellow Plastic 18 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.5f,0.5f,0.0f);
			GLES32.glUniform3f(materialSpecularUniform, 0.60f, 0.60f, 0.60f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.25f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 17.25f ,1.5f ,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end Yellow Plastic 18 ===========================================

//==========================BLACK-RUBBER 19 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.02f, 0.02f, 0.02f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.01f,0.01f,0.01f);
			GLES32.glUniform3f(materialSpecularUniform, 0.4f, 0.4f, 0.4f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.078125f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,23.0f ,14.0f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end BLACK-RUBBER 19 ===========================================

//==========================Cyan Rubber 20 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.0f, 0.05f, 0.05f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.4f,0.5f,0.5f);
			GLES32.glUniform3f(materialSpecularUniform, 0.04f, 0.7f, 0.7f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.078125f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,23.0f ,11.5f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end Cyan Rubber 20 ===========================================

//==========================Rubber Green 21 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.0f, 0.05f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.4f,0.5f,0.4f);
			GLES32.glUniform3f(materialSpecularUniform, 0.04f, 0.7f, 0.04f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.078125f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 23.0f,9.0f,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end Rubber Green 21 ===========================================

//==========================RUBBER RED 22 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.05f, 0.0f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.5f,0.4f,0.4f);
			GLES32.glUniform3f(materialSpecularUniform, 0.7f, 0.04f, 0.04f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.078125f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,  23.0f, 6.5f ,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== RUBBER RED 22 ===========================================

//==========================/rubber White 23 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.05f, 0.05f, 0.05f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.5f,0.5f,0.5f);
			GLES32.glUniform3f(materialSpecularUniform, 0.7f, 0.7f, 0.7f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.078125f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 23.0f ,4.0f ,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end /rubber White 23 ===========================================

//==========================RUBBER YELLOW 24 ======================================
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(scaleMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4fv(lightPositionUniform,1,lightPosition,0);
			
			GLES32.glUniform3f(materialAmbientUniform,  0.05f, 0.05f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 0.5f,0.5f,0.4f);
			GLES32.glUniform3f(materialSpecularUniform, 0.7f, 0.7f, 0.04f);
			
			GLES32.glUniform1f(materialShininessUniform,  0.078125f * 128.0f);
		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0, 23.0f ,1.5f ,0.0f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
		Matrix.scaleM(scaleMatrix,0,1.75f,1.75f,1.75f);
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,scaleMatrix,0);
		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,orthographicProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);

		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		GLES32.glBindVertexArray(0);
//================================== end RUBBER YELLOW 24 ===========================================


		GLES32.glUseProgram(0);
		requestRender();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void update()
	{	
		lightAngleZero=lightAngleZero+0.05f;
	
	}
	private void uninitialize()
	{
		if(vbo_sphere_normal[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vbo_sphere_normal,0);
			vbo_sphere_normal[0]=0;
		}

		if(vbo_sphere_position[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vbo_sphere_position,0);
			vbo_sphere_position[0]=0;
		}

		if(vbo_sphere_element[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vbo_sphere_element,0);
			vbo_sphere_element[0]=0;
		}

		if(vao_sphere[0]!=0)
		{
			GLES32.glDeleteVertexArrays(1,vao_sphere,0);
			vao_sphere[0]=0;
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
