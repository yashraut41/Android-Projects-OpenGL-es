package com.RTR.ThreeLight;

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
	
	private float lightAngleZero=0.0f;
	private float lightAngleOne=0.0f;
	private float lightAngleTwo=0.0f;
	
	private int[] vao_sphere=new int[1];
	private int[] vbo_sphere_position=new int[1];
	private int[] vbo_sphere_normal=new int[1];
	private int[] vbo_sphere_element=new int[1];
	
	private int modelMatrixUniform;
	private int viewMatrixUniform;
	private int projectionMatrixUniform;
	
	private int rotationalMatrixUniformred;
	private int rotationalMatrixUniformgreen;
	private int rotationalMatrixUniformblue;

	//light Uniforms red 
	private int lightDiffuseUniformRedLight;
	private int lightAmbientUniformRedLight;
	private int lightSpecularUniformRedLight;
	private int lightPositionUniformRedLight;

	//light Uniforms green 
	private int lightDiffuseUniformGreenLight;
	private int lightAmbientUniformGreenLight;
	private int lightSpecularUniformGreenLight;
	private int  lightPositionUniformGreenLight;

	//light Uniforms blue 
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

	

	private int numElements;
    private int numVertices;

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
        "in vec4 Per_Vertex_vPosition;" +
        "in vec3 Per_Vertex_vNormal;" +
        "uniform mat4 u_Per_Vertex_model_matrix ;" +
        "uniform mat4 u_Per_Vertex_view_matrix ;" +
        "uniform mat4 u_Per_Vertex_projection_matrix ;" +
        "uniform mat4 u_Per_Vertex_rotational_matrix_red ;" +
        "uniform mat4 u_Per_Vertex_rotational_matrix_green ;" +
        "uniform mat4 u_Per_Vertex_rotational_matrix_blue ;" +
        "uniform vec3 u_Per_Vertex_lightambient_red ;" +
        "uniform vec3 u_Per_Vertex_lightambient_blue ;" +
        "uniform vec3 u_Per_Vertex_lightambient_green ;" +
        "uniform vec3 u_Per_Vertex_lightdiffuse_red ;" +
        "uniform vec3 u_Per_Vertex_lightdiffuse_blue ;" +
        "uniform vec3 u_Per_Vertex_lightdiffuse_green ;" +
        "uniform vec3 u_Per_Vertex_lightspecular_red ;" +
        "uniform vec3 u_Per_Vertex_lightspecular_blue ;" +
        "uniform vec3 u_Per_Vertex_lightspecular_green ;" +
        "uniform vec4 u_Per_Vertex_light_position_red ;" +
        "uniform vec4 u_Per_Vertex_light_position_blue ;" +
        "uniform vec4 u_Per_Vertex_light_position_green ;" +

        "uniform vec3 u_Per_Vertex_materialambient ;" +
		"uniform vec3 u_Per_Vertex_materialdiffuse ;" +
		"uniform vec3 u_Per_Vertex_materialspecular ;" +
        "uniform float u_Per_Vertex_material_shininess;" +
        "uniform mediump int lKeyPress;" +
        "out vec3 phong_ads_light;" +
        "void main(void)" +
        "{" +
        "if(lKeyPress==1)" +
        "{" +
        "vec4 eye_coordinates      = u_Per_Vertex_view_matrix * u_Per_Vertex_model_matrix * Per_Vertex_vPosition ;" +
        "vec4 eye_coordinatesred   = u_Per_Vertex_view_matrix * u_Per_Vertex_model_matrix * Per_Vertex_vPosition * u_Per_Vertex_rotational_matrix_red;" +
        "vec4 eye_coordinatesgreen = u_Per_Vertex_view_matrix * u_Per_Vertex_model_matrix * Per_Vertex_vPosition * u_Per_Vertex_rotational_matrix_green;" +
        "vec4 eye_coordinatesblue  = u_Per_Vertex_view_matrix * u_Per_Vertex_model_matrix * Per_Vertex_vPosition * u_Per_Vertex_rotational_matrix_blue;" +
        "vec3 tnorm                = normalize(mat3 (u_Per_Vertex_view_matrix * u_Per_Vertex_model_matrix) * Per_Vertex_vNormal);" +
        "vec4 rotationalLightPositionred   = u_Per_Vertex_light_position_red * u_Per_Vertex_rotational_matrix_red;" +
        "vec4 rotationalLightPositiongreen = u_Per_Vertex_light_position_green * u_Per_Vertex_rotational_matrix_green;" +
        "vec4 rotationalLightPositionblue  = u_Per_Vertex_light_position_blue * u_Per_Vertex_rotational_matrix_blue;" +
        "vec3 lightdirection_red           = normalize(vec3 (rotationalLightPositionred -  eye_coordinatesred));" +
        "vec3 lightdirection_blue          = normalize(vec3 (rotationalLightPositionblue - eye_coordinatesblue));" +
        "vec3 lightdirection_green         = normalize(vec3 (rotationalLightPositiongreen - eye_coordinatesgreen));" +
        "float tn_dot_ld_red               = max(dot (lightdirection_red, tnorm),0.0);" +
        "float tn_dot_ld_blue              = max(dot (lightdirection_blue, tnorm),0.0);" +
        "float tn_dot_ld_green             = max(dot (lightdirection_green, tnorm),0.0);" +
        " vec3 reflection_vector_red       = reflect(-lightdirection_red,tnorm);" +
        " vec3 reflection_vector_blue      = reflect(-lightdirection_blue,tnorm);" +
        " vec3 reflection_vector_green     = reflect(-lightdirection_green,tnorm);" +
        " vec3 viewer_vector               = normalize(vec3(- eye_coordinates.xyz));" +
        " vec3 ambient                     = u_Per_Vertex_lightambient_red * u_Per_Vertex_materialambient;" +
        " vec3 diffuse                     = u_Per_Vertex_lightdiffuse_red * u_Per_Vertex_materialdiffuse * tn_dot_ld_red;" +
        " vec3 specular                    = u_Per_Vertex_lightspecular_red * u_Per_Vertex_materialspecular * pow(max(dot(reflection_vector_red,viewer_vector),0.0),u_Per_Vertex_material_shininess);" +
        " vec3 ambientblue                 = u_Per_Vertex_lightambient_blue * u_Per_Vertex_materialambient;" +
        " vec3 diffuseblue                 = u_Per_Vertex_lightdiffuse_blue * u_Per_Vertex_materialdiffuse * tn_dot_ld_blue;" +
        " vec3 specularblue                = u_Per_Vertex_lightspecular_blue * u_Per_Vertex_materialspecular * pow(max(dot(reflection_vector_blue,viewer_vector),0.0),u_Per_Vertex_material_shininess);" +
        " vec3 ambientgreen                = u_Per_Vertex_lightambient_green * u_Per_Vertex_materialambient;" +
        " vec3 diffusegreen                = u_Per_Vertex_lightdiffuse_green * u_Per_Vertex_materialdiffuse * tn_dot_ld_green;" +
        " vec3 speculargreen               = u_Per_Vertex_lightspecular_green * u_Per_Vertex_materialspecular * pow(max(dot(reflection_vector_green,viewer_vector),0.0),u_Per_Vertex_material_shininess);" +
        "phong_ads_light                   = ambient + ambientblue + ambientgreen + diffuse + diffusegreen + diffuseblue + specular + specularblue + speculargreen;" +
        "}" +
        "else" +
        "{" +
        "phong_ads_light=vec3(1.0,1.0,1.0);" +
        "}" +
        "gl_Position=u_Per_Vertex_projection_matrix * u_Per_Vertex_view_matrix * u_Per_Vertex_model_matrix * Per_Vertex_vPosition;" +
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
		 	"precision highp float;" +
        "in vec3 phong_ads_light;" +
		"out vec4 FragColor;" +
		"void main(void)" +
		"{" +
		"FragColor=vec4(phong_ads_light,1.0);" +
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
			GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.AMC_ATTRIBUTE_POSITION,"Per_Vertex_vPosition");
			GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.AMC_ATTRIBUTE_NORMAL,"Per_Vertex_vNormal");

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

		modelMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_model_matrix");			
		viewMatrixUniform  = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_view_matrix");
		projectionMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_projection_matrix");

		rotationalMatrixUniformred = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_rotational_matrix_red");			
		rotationalMatrixUniformgreen  = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_rotational_matrix_green");
		rotationalMatrixUniformblue = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_rotational_matrix_blue");

		lightAmbientUniformRedLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_lightambient_red");			
		lightDiffuseUniformRedLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_lightdiffuse_red");
		lightSpecularUniformRedLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_lightspecular_red");
		lightPositionUniformRedLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_light_position_red");



		lightAmbientUniformGreenLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_lightambient_green");			
		lightDiffuseUniformGreenLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_lightdiffuse_green");
		lightSpecularUniformGreenLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_lightspecular_green");
		lightPositionUniformGreenLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_light_position_green");

		

		lightAmbientUniformBlueLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_lightambient_blue");			
		lightDiffuseUniformBlueLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_lightdiffuse_blue");
		lightSpecularUniformBlueLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_lightspecular_blue");
		lightPositionUniformBlueLight = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_light_position_blue");



		materialAmbientUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_materialambient");
		materialDiffuseUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_materialdiffuse");
		materialSpecularUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_materialspecular");
		materialShininessUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Per_Vertex_material_shininess");
		
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
		//float[] rotationMatrix=new float[16];
		float[] translationaMatrix=new float[16];
		float[] viewMatrix=new float[16];
		float[] rotationalMatrixred=new float[16];
		float[] rotationalMatrixgreen=new float[16];
		float[] rotationalMatrixblue=new float[16];
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		//Matrix.setIdentityM(rotationMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
		Matrix.setIdentityM(rotationalMatrixred,0);		
		Matrix.setIdentityM(rotationalMatrixgreen,0);		
		Matrix.setIdentityM(rotationalMatrixblue,0);		

		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			
			GLES32.glUniform3f(lightAmbientUniformRedLight, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniformRedLight, 1.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightSpecularUniformRedLight, 1.0f, 0.0f, 0.0f);
			GLES32.glUniform4f(lightPositionUniformRedLight, 0.0f, 0.0f, 0.0f,1.0f);
			
			GLES32.glUniform3f(lightAmbientUniformGreenLight, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniformGreenLight, 0.0f, 1.0f, 0.0f);
			GLES32.glUniform3f(lightSpecularUniformGreenLight, 0.0f, 1.0f, 0.0f);
			GLES32.glUniform4f(lightPositionUniformGreenLight, 0.0f, 0.0f, 0.0f,1.0f);
			
			GLES32.glUniform3f(lightAmbientUniformBlueLight, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniformBlueLight, 0.0f, 0.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniformBlueLight, 0.0f, 0.0f, 1.0f);
			GLES32.glUniform4f(lightPositionUniformBlueLight, 0.0f, 0.0f, 0.0f,1.0f);
			

			GLES32.glUniform3f(materialAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(materialSpecularUniform, 1.0f, 1.0f, 1.0f);
			
			GLES32.glUniform1f(materialShininessUniform, 128.0f);

		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,0.0f,0.0f,-3.0f);
		Matrix.rotateM(rotationalMatrixred,0,lightAngleZero,1.0f,0.0f,0.0f);
		Matrix.rotateM(rotationalMatrixgreen,0,lightAngleOne,0.0f,1.0f,0.0f);
		Matrix.rotateM(rotationalMatrixblue,0,lightAngleTwo,0.0f,0.0f,1.0f);

		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,perspectiveProjectionMatrix,0);
		
		GLES32.glUniformMatrix4fv(rotationalMatrixUniformred,1,false,rotationalMatrixred,0);
		GLES32.glUniformMatrix4fv(rotationalMatrixUniformgreen,1,false,rotationalMatrixgreen,0);
		GLES32.glUniformMatrix4fv(rotationalMatrixUniformblue,1,false,rotationalMatrixblue,0);


		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		
		GLES32.glBindVertexArray(0);

		GLES32.glUseProgram(0);
		requestRender();
	}
	private void update()
	{	
		lightAngleZero = lightAngleZero + 2.5f;
		lightAngleOne  =  lightAngleOne + 2.5f;
		lightAngleTwo = lightAngleTwo + 2.5f;

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
