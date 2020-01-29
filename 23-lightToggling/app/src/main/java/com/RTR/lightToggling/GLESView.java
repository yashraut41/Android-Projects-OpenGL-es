package com.RTR.lightToggling;

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
	
	//shaders object for perVertex
	private int perVertexShaderObject;
	private int perVertexFragmentShaderObject;
	private int perVertexShaderProgramObject;
	
	//shaders object for perFragment
	private int perFragmentVertexShaderObject;
	private int perFragmentShaderObject;
	private int perFragmentShaderProgramObject;
	
	//uniforms for perVertex
	private int perVertexModelMatrixUniform;
	private int perVertexViewMatrixUniform;
	private int perVertexProjectionMatrixUniform;
	private int perVertexlightDiffuseUniform;
	private int perVertexlightAmbientUniform;
	private int perVertexLightSpecularUniform;
	private int perVertexLightPositionUniform;
	private int perVertexLKeyPressUniform;
	private int materialAmbientUniform;
	private int materialDiffuseUniform;
	private int materialSpecularUniform;
	private int materialShininessUniform;


	//uniforms for perFragment
	private int perFragmentModelMatrixUniform;
	private int perFragmentViewMatrixUniform;
	private int perFragmentProjectionMatrixUniform;
	private int perFragmentlightDiffuseUniform;
	private int perFragmentlightAmbientUniform;
	private int perFragmentLightSpecularUniform;
	private int perFragmentLightPositionUniform;
	private int lKeyPressUniform;
	private int perFragmentMaterialAmbientUniform;
	private int perFragmentMaterialDiffuseUniform;
	private int perFragmentMaterialSpecularUniform;
	private int perFragmentMaterialShininessUniform;

	//for vao and vbo
	private int[] vao_sphere=new int[1];
	private int[] vbo_sphere_position=new int[1];
	private int[] vbo_sphere_normal=new int[1];
	private int[] vbo_sphere_element=new int[1];
	
	
	private int projectionMatrixUniform;
	

	
	
	//for sphere data 
	private int numElements;
    private int numVertices;

	private boolean bLight=true;
	private boolean bPerVertexShaderLight=true;
	private boolean bPerFragmentShaderLight=false;

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
			if (bPerFragmentShaderLight == false) 
            {
                bPerFragmentShaderLight = true;
                bPerVertexShaderLight=false;
            }
            else
             {
                bPerFragmentShaderLight = false;
                bPerVertexShaderLight = true;
            }
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
		
	}

	//our custom methods


	private void initialize()
	{
			perVertexLight();
			perFragmentLight();

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








	private void perVertexLight()
	{
		perVertexShaderObject=GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
			//vertex shader code
		final String perVertexShaderSourceCode=
		String.format
		(
		"#version 320 es" +
		"\n" +
		"in vec4 vPosition;" +
        "in vec3 vNormal;" +
        "uniform mat4 u_model_matrix;" +
        "uniform mat4 u_view_matrix;" +
        "uniform mat4 u_projection_matrix;" +
        "uniform mediump int lKeyPress;" +
        "uniform vec3 u_lightambient;" +
        "uniform vec3 u_lightdiffuse;" +
        "uniform vec3 u_lightspecular;" +
        "uniform vec4 u_light_position;" +
        "uniform vec3 u_materialambient;" +
        "uniform vec3 u_materialdiffuse;" +
        "uniform vec3 u_materialspecular;" +
        "uniform float materialshinines;" +
        "out vec3 phong_ads_color;" +
        "void main(void)" +
        "{" +
        "	if(lKeyPress==1)" +
        "{" +
        "vec4 eye_coordinates   = u_view_matrix * u_model_matrix * vPosition;" +
        "vec3 tnorm             = normalize(mat3 (u_view_matrix * u_model_matrix) * vNormal);" +
        "vec3 lightdirection    = normalize(vec3 (u_light_position ) - eye_coordinates.xyz);" +
        "float tn_dot_ld        = max(dot (tnorm,lightdirection ),0.0 );" +
        "vec3 ambient           = u_lightambient * u_materialambient;" +
        "vec3 diffuse           = u_lightdiffuse * u_materialdiffuse * tn_dot_ld;" +
        "vec3 reflection_vector = reflect(-lightdirection,tnorm);" +
        "vec3 viewer_vector     = normalize(-eye_coordinates.xyz);" +
        "vec3 specular          =u_lightspecular * u_materialspecular * pow(max (dot (reflection_vector,viewer_vector),0.0),materialshinines);" +
        "phong_ads_color        =ambient + diffuse + specular;" +
        "}" +
        "else" +
        "{" +
        "phong_ads_color=vec3(1.0,1.0,1.0);" +
        "}" +
        "gl_Position=u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;" +
        "}"

		);

		//specify above shader source code to vertexShaderObject
		//give shader source code
		GLES32.glShaderSource(perVertexShaderObject,perVertexShaderSourceCode);

		//compile the vertex shader code
		GLES32.glCompileShader(perVertexShaderObject);

		//error checking code for vertex shader
		int []iShaderCompileStatus=new int[1];
		int []iInfoLogLength=new int[1];
		String szInfoLog=null;

		GLES32.glGetShaderiv(perVertexShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompileStatus,0);

		if(iShaderCompileStatus[0]==GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(perVertexShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

			if(iInfoLogLength[0]>0)
			{
				szInfoLog=GLES32.glGetShaderInfoLog(perVertexShaderObject);
				System.out.println("AMC: PERvertex shader compilation Log:"+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}

		//fragment shader
		perVertexFragmentShaderObject=GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

		 final String perVertexFragmentShaderSourceCode=
		 String.format
		 (
		 "#version 320 es" +
		 "\n" +
		 	"precision highp float;"+
    	"in vec3 phong_ads_color ;" +
        "out vec4 FragColor;" +
        "void main(void)" +
        "{" +
        "FragColor=vec4(phong_ads_color,1.0);" +
        "}"
		 );

		 GLES32.glShaderSource(perVertexFragmentShaderObject,perVertexFragmentShaderSourceCode);

		 GLES32.glCompileShader(perVertexFragmentShaderObject);
		 iShaderCompileStatus[0]=0;
		 iInfoLogLength[0]=0;
		 szInfoLog=null;
		 GLES32.glGetShaderiv(perVertexFragmentShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompileStatus,0);

		 if(iShaderCompileStatus[0]==GLES32.GL_FALSE)
		 {
			 GLES32.glGetShaderiv(perVertexFragmentShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

			 if(iInfoLogLength[0]>0)
			 {
				 szInfoLog=GLES32.glGetShaderInfoLog(perVertexFragmentShaderObject);
				 System.out.println("AMC:PERVERTEX  fragment shader compilation log:"+szInfoLog);
				 uninitialize();
				 System.exit(0);
			 }
		 }
		 	//create shader program object
			perVertexShaderProgramObject=GLES32.glCreateProgram();

			//attach vertex shader to shader program
			GLES32.glAttachShader(perVertexShaderProgramObject,perVertexShaderObject);

			//attach fragment shader  to shaderprogram
			GLES32.glAttachShader(perVertexShaderProgramObject,perVertexFragmentShaderObject);

			//bind attributes to shader variable
			GLES32.glBindAttribLocation(perVertexShaderProgramObject,GLESMacros.AMC_ATTRIBUTE_POSITION,"vPosition");
			GLES32.glBindAttribLocation(perVertexShaderProgramObject,GLESMacros.AMC_ATTRIBUTE_NORMAL,"vNormal");

			//NOW Link shader program
			GLES32.glLinkProgram(perVertexShaderProgramObject);

			int []iProgramLinkStatus=new int[1];
			iInfoLogLength[0]=0;
			szInfoLog=null;

			GLES32.glGetProgramiv(perVertexShaderProgramObject,GLES32.GL_LINK_STATUS,iProgramLinkStatus,0);

			if(iProgramLinkStatus[0]==GLES32.GL_FALSE)
			{
				GLES32.glGetProgramiv(perVertexShaderProgramObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

				if(iInfoLogLength[0]>0)
				{
					szInfoLog=GLES32.glGetProgramInfoLog(perVertexShaderProgramObject);
					System.out.println("AMC: PERVERTEX Shader Program Link Log:"+szInfoLog);
					uninitialize();
					System.exit(0);
				}
			}

		perVertexModelMatrixUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "u_model_matrix");			
		perVertexViewMatrixUniform  = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "u_view_matrix");
		perVertexProjectionMatrixUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "u_projection_matrix");

		perVertexlightAmbientUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "u_lightambient");			
		perVertexlightDiffuseUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "u_lightdiffuse");
		perVertexLightSpecularUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "u_lightspecular");
		perVertexLightPositionUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "u_light_position");

		materialAmbientUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "u_materialambient");
		materialDiffuseUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "u_materialdiffuse");
		materialSpecularUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "u_materialspecular");
		materialShininessUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "materialshinines");
		perVertexLKeyPressUniform = GLES32.glGetUniformLocation(perVertexShaderProgramObject, "lKeyPress");

	}

































	private void perFragmentLight()
	{
		perFragmentVertexShaderObject=GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
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
        "tnorm=mat3(u_view_matrix*u_model_matrix)*vNormal;" +
        "lightdirection=vec3(u_light_position) - eye_coordinates.xyz;" +
        "viewer_vector=-eye_coordinates.xyz;" +
        "}" +
        "gl_Position=u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;" +
        "}"
		);

		//specify above shader source code to vertexShaderObject
		//give shader source code
		GLES32.glShaderSource(perFragmentVertexShaderObject,vertexShaderSourceCode);

		//compile the vertex shader code
		GLES32.glCompileShader(perFragmentVertexShaderObject);

		//error checking code for vertex shader
		int []iShaderCompileStatus=new int[1];
		int []iInfoLogLength=new int[1];
		String szInfoLog=null;

		GLES32.glGetShaderiv(perFragmentVertexShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompileStatus,0);

		if(iShaderCompileStatus[0]==GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(perFragmentVertexShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

			if(iInfoLogLength[0]>0)
			{
				szInfoLog=GLES32.glGetShaderInfoLog(perFragmentVertexShaderObject);
				System.out.println("AMC: PERFRAGMENT  vertex shader compilation Log:"+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}

		//fragment shader
		perFragmentShaderObject=GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

		 final String fragmentShaderSourceCode=
		 String.format
		 (
		 "#version 320 es" +
		 "\n" +
		"precision highp float;" +
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
        "}" +
        "else" +
        "{" +
        "phong_ADS_light=vec3(1.0,1.0,1.0);" +
        "}" +
        "FragColor=vec4(phong_ADS_light,1.0);" +
        "}"
		 );

		 GLES32.glShaderSource(perFragmentShaderObject,fragmentShaderSourceCode);

		 GLES32.glCompileShader(perFragmentShaderObject);
		 iShaderCompileStatus[0]=0;
		 iInfoLogLength[0]=0;
		 szInfoLog=null;
		 GLES32.glGetShaderiv(perFragmentShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompileStatus,0);

		 if(iShaderCompileStatus[0]==GLES32.GL_FALSE)
		 {
			 GLES32.glGetShaderiv(perFragmentShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

			 if(iInfoLogLength[0]>0)
			 {
				 szInfoLog=GLES32.glGetShaderInfoLog(perFragmentShaderObject);
				 System.out.println("AMC: PER fragment shader compilation log:"+szInfoLog);
				 uninitialize();
				 System.exit(0);
			 }
		 }
		 	//create shader program object
			perFragmentShaderProgramObject=GLES32.glCreateProgram();

			//attach vertex shader to shader program
			GLES32.glAttachShader(perFragmentShaderProgramObject,perFragmentVertexShaderObject);

			//attach fragment shader  to shaderprogram
			GLES32.glAttachShader(perFragmentShaderProgramObject,perFragmentShaderObject);

			//bind attributes to shader variable
			GLES32.glBindAttribLocation(perFragmentShaderProgramObject,GLESMacros.AMC_ATTRIBUTE_POSITION,"vPosition");
			GLES32.glBindAttribLocation(perFragmentShaderProgramObject,GLESMacros.AMC_ATTRIBUTE_NORMAL,"vNormal");

			//NOW Link shader program
			GLES32.glLinkProgram(perFragmentShaderProgramObject);

			int []iProgramLinkStatus=new int[1];
			iInfoLogLength[0]=0;
			szInfoLog=null;

			GLES32.glGetProgramiv(perFragmentShaderProgramObject,GLES32.GL_LINK_STATUS,iProgramLinkStatus,0);

			if(iProgramLinkStatus[0]==GLES32.GL_FALSE)
			{
				GLES32.glGetProgramiv(perFragmentShaderProgramObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

				if(iInfoLogLength[0]>0)
				{
					szInfoLog=GLES32.glGetProgramInfoLog(perFragmentShaderProgramObject);
					System.out.println("AMC:PER FRAGMENT Shader Program Link Log:"+szInfoLog);
					uninitialize();
					System.exit(0);
				}
			}

		perFragmentModelMatrixUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "u_model_matrix");			
		perFragmentViewMatrixUniform  = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "u_view_matrix");
		perFragmentProjectionMatrixUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "u_projection_matrix");

		perFragmentlightAmbientUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "u_lightambient");			
		perFragmentlightDiffuseUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "u_lightdiffuse");
		perFragmentLightSpecularUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "u_lightspecular");
		perFragmentLightPositionUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "u_light_position");

		perFragmentMaterialAmbientUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "u_materialambient");
		perFragmentMaterialDiffuseUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "u_materialdiffuse");
		perFragmentMaterialSpecularUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "u_materialspecular");
		perFragmentMaterialShininessUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "materialshinines");
		lKeyPressUniform = GLES32.glGetUniformLocation(perFragmentShaderProgramObject, "lKeyPress");

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

		//declaration of matrices
		float[] modelMatrix=new float[16];
		//float[] rotationMatrix=new float[16];
		float[] translationaMatrix=new float[16];
		float[] viewMatrix=new float[16];

		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		//Matrix.setIdentityM(rotationMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
	if(bPerVertexShaderLight==true)
	{			
		GLES32.glUseProgram(perVertexShaderProgramObject);
		if (bLight == true)
		{
			GLES32.glUniform1i(perVertexLKeyPressUniform, 1);
			
			GLES32.glUniform3f(perVertexlightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(perVertexlightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(perVertexLightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4f(perVertexLightPositionUniform, 100.0f, 100.0f, 100.0f,1.0f);
			
			GLES32.glUniform3f(materialAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(materialDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(materialSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform1f(materialShininessUniform, 128.0f);

		}
		else
		{
			GLES32.glUniform1i(perVertexLKeyPressUniform, 0);
		}
	
		Matrix.translateM(translationaMatrix,0,0.0f,0.0f,-3.0f);
		//Matrix.rotateM(rotationMatrix,0,angleSphere,0.0f,1.0f,0.0f);
		
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(perVertexModelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(perVertexProjectionMatrixUniform,1,false,perspectiveProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(perVertexViewMatrixUniform,1,false,viewMatrix,0);


		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		
		GLES32.glBindVertexArray(0);

		GLES32.glUseProgram(0);
	}
		

	if(bPerFragmentShaderLight==true)
	{			
		GLES32.glUseProgram(perFragmentShaderProgramObject);
		if (bLight == true)
		{
			GLES32.glUniform1i(lKeyPressUniform, 1);
			
			GLES32.glUniform3f(perFragmentlightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(perFragmentlightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(perFragmentLightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4f(perFragmentLightPositionUniform, 100.0f, 100.0f, 100.0f,1.0f);
			
			GLES32.glUniform3f(perFragmentMaterialAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(perFragmentMaterialDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(perFragmentMaterialSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform1f(perFragmentMaterialShininessUniform, 128.0f);

		}
		else
		{
			GLES32.glUniform1i(lKeyPressUniform, 0);
		}
	
		//set iDDENTITY OF MATRICES
		Matrix.setIdentityM(modelMatrix,0);
		//Matrix.setIdentityM(rotationMatrix,0);
		Matrix.setIdentityM(translationaMatrix,0);		
		Matrix.setIdentityM(viewMatrix,0);		
	
		Matrix.translateM(translationaMatrix,0,0.0f,0.0f,-3.0f);
		//Matrix.rotateM(rotationMatrix,0,angleSphere,0.0f,1.0f,0.0f);
		
		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(perFragmentModelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(perFragmentProjectionMatrixUniform,1,false,perspectiveProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(perFragmentViewMatrixUniform,1,false,viewMatrix,0);


		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vao_sphere[0]);
		
		GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);
		GLES32.glDrawElements(GLES32.GL_TRIANGLES,numElements,GLES32.GL_UNSIGNED_SHORT,0);
		
		GLES32.glBindVertexArray(0);

		GLES32.glUseProgram(0);
	}
		requestRender();
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


		if(perVertexShaderProgramObject!=0)
		{
			int[] shaderCount=new int[1];
			int shaderNumber;
			GLES32.glUseProgram(perVertexShaderProgramObject);

			GLES32.glGetProgramiv(perVertexShaderProgramObject,GLES32.GL_ATTACHED_SHADERS,shaderCount,0);
			int[] shader=new int[shaderCount[0]];
			if(shader[0]!=0)
			{
				GLES32.glGetAttachedShaders(perVertexShaderProgramObject,shaderCount[0],shaderCount,0,shader,0);

				for(shaderNumber=0;shaderNumber<shaderCount[0];shaderNumber++)
				{
					GLES32.glDetachShader(perVertexShaderProgramObject,shader[shaderNumber]);
					GLES32.glDeleteShader(shader[shaderNumber]);
					shader[shaderNumber]=0;
				}
			}
			GLES32.glUseProgram(0);
		GLES32.glDeleteShader(perVertexShaderProgramObject);
		perVertexShaderProgramObject=0;
		}


		if(perFragmentShaderProgramObject!=0)
		{
			int[] shaderCount=new int[1];
			int shaderNumber;
			GLES32.glUseProgram(perFragmentShaderProgramObject);

			GLES32.glGetProgramiv(perFragmentShaderProgramObject,GLES32.GL_ATTACHED_SHADERS,shaderCount,0);
			int[] shader=new int[shaderCount[0]];
			if(shader[0]!=0)
			{
				GLES32.glGetAttachedShaders(perFragmentShaderProgramObject,shaderCount[0],shaderCount,0,shader,0);

				for(shaderNumber=0;shaderNumber<shaderCount[0];shaderNumber++)
				{
					GLES32.glDetachShader(perFragmentShaderProgramObject,shader[shaderNumber]);
					GLES32.glDeleteShader(shader[shaderNumber]);
					shader[shaderNumber]=0;
				}
			}
			GLES32.glUseProgram(0);
		GLES32.glDeleteShader(perFragmentShaderProgramObject);
		perFragmentShaderProgramObject=0;
		}
    }
}
