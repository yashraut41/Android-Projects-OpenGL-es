package com.RTR.CubeInterleave;

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
	
	private float angleSphere=0.0f;
	private float rotationSpeed=0.5f;

	
	private int[] vaoCube=new int[1];
	private int[] vboVCNT=new int[1];
	
	private int modelMatrixUniform;
	private int viewMatrixUniform;
	private int projectionMatrixUniform;
	
	//light Uniforms
	private int lightAmbientUniform;
	private int lightDiffuseUniform;
	private int lightSpecularUniform;
	private int lightPositionUniform;

    private int samplerUniform;

	//material Uniforms

	private int materialDiffuseUniform;
	private int materialAmbientUniform;
	private int materialSpecularUniform;
	private int materialShininessUniform;

	private int lKeyPressUniform;

    //for texture
    private int[] marbleTexture=new int[1];
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
        "in vec4 vColor;" +
        "in vec3 vNormal;" +
        "in vec2 vTexcoord;" +
		"uniform mat4 u_model_matrix;" +
		"uniform mat4 u_view_matrix;" +
		"uniform mat4 u_projection_matrix;" +
		"uniform vec4 u_light_position;" +
		"uniform mediump int lKeyPress;" +
		"out vec3 lightdirection;" +
		"out vec3 tnorm;" +
		"out vec3 viewer_vector;" +
        "out vec4 out_color;"+
        "out vec2 out_texcoord;"+
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
        "out_color = vColor;" +
        "out_texcoord = vTexcoord;" +
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
        "in vec4 out_color;"+
        "in vec2 out_texcoord;" +
        "uniform sampler2D u_sampler;" +
		"uniform vec3 u_lightambient;" +
		"uniform vec3 u_lightdiffuse;" +
		"uniform vec3 u_lightspecular;" +
		"uniform vec3 u_materialambient;" +
		"uniform vec3 u_materialdiffuse;" +
		"uniform vec3 u_materialspecular;" +
		"uniform float materialshinines;" +
		"uniform int lKeyPress;" +
        "out vec4 FragColor;" +
		"void main(void)" +
	"{" +
		"vec3 phong_ADS_light;" +
        "vec4 tex = texture(u_sampler,out_texcoord);" +
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
		"FragColor=vec4( vec3(tex) * vec3(out_color) * phong_ADS_light , 1.0);" +
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
            GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.AMC_ATTRIBUTE_COLOR,"vColor");
            GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.AMC_ATTRIBUTE_TEXCOORD0,"vTexcoord");

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


        samplerUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_sampler");

          final  float[] cubeVCNT=new float[]
            {
                1.0f,1.0f,-1.0f,  1.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,	0.0f,1.0f,
                -1.0f,1.0f,-1.0f, 1.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,   0.0f,0.0f,
                -1.0f,1.0f,1.0f,   1.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,   1.0f,0.0f,
                1.0f,1.0f,1.0f,   1.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,   1.0f,1.0f,

		1.0f,-1.0f,-1.0f,    0.0f,1.0f,0.0f, 0.0f,-1.0f,0.0f,  1.0f,1.0f,
        -1.0f,-1.0f,-1.0f,    0.0f,1.0f,0.0f, 0.0f,-1.0f,0.0f,  0.0f,1.0f,
		-1.0f,-1.0f,1.0f,     0.0f,1.0f,0.0f, 0.0f,-1.0f,0.0f,  0.0f,0.0f,
		1.0f,-1.0f,1.0f,      0.0f,1.0f,0.0f, 0.0f,-1.0f,0.0f,  1.0f,0.0f,

	    1.0f,1.0f,1.0f,	      0.0f,0.0f,1.0f, 0.0f,0.0f,1.0f,   0.0f,0.0f,
		-1.0f,1.0f,1.0f,      0.0f,0.0f,1.0f, 0.0f,0.0f,1.0f,   1.0f,0.0f,
		-1.0f,-1.0f,1.0f,      0.0f,0.0f,1.0f, 0.0f,0.0f,1.0f,  1.0f,1.0f,
		1.0f,-1.0f,1.0f,       0.0f,0.0f,1.0f, 0.0f,0.0f,1.0f,  0.0f,1.0f,

        1.0f,1.0f,-1.0f,       0.0f,1.0f,1.0f, 0.0f,0.0f,-1.0f, 1.0f,0.0f,
		-1.0f,1.0f,-1.0f,      0.0f,1.0f,1.0f, 0.0f,0.0f,-1.0f, 1.0f,1.0f,
		-1.0f,-1.0f,-1.0f,     0.0f,1.0f,1.0f, 0.0f,0.0f,-1.0f, 0.0f,1.0f,
		1.0f,-1.0f,-1.0f,      0.0f,1.0f,1.0f, 0.0f,0.0f,-1.0f, 0.0f,0.0f,

		1.0f,1.0f,-1.0f,        1.0f,0.0f,1.0f,   1.0f,0.0f,0.0f,   1.0f,0.0f,
		1.0f,1.0f,1.0f,         1.0f,0.0f,1.0f,   1.0f,0.0f,0.0f,   1.0f,1.0f,
		1.0f,-1.0f,1.0f,        1.0f,0.0f,1.0f,   1.0f,0.0f,0.0f,   0.0f,1.0f,
	    1.0f,-1.0f,-1.0f,       1.0f,0.0f,1.0f,   1.0f,0.0f,0.0f,   0.0f,0.0f,

		-1.0f,1.0f, 1.0f,       1.0f, 1.0f, 0.0f,  -1.0f,0.0f,0.0f, 0.0f, 0.0f,
		-1.0f,1.0f,-1.0f,       1.0f, 1.0f, 0.0f,  -1.0f,0.0f,0.0f, 1.0f, 0.0f,
		-1.0f,-1.0f,-1.0f,      1.0f, 1.0f, 0.0f,  -1.0f,0.0f,0.0f, 1.0f, 1.0f,
		-1.0f,-1.0f,1.0f,       1.0f, 1.0f, 0.0f,  -1.0f,0.0f,0.0f, 0.0f, 1.0f

            };


			
			//--------------------------SPHERE ---------------------------------------------->>>>>

			
			//<<<<<------------------START VAO FOR SPHERE ----------------------------->>>>
			
			//create vertex array object for SPHERE (vao)
			GLES32.glGenVertexArrays(1,vaoCube,0);

			//bind vao for SPHERE
			GLES32.glBindVertexArray(vaoCube[0]);


			//<<<<<---------START VBO FOR SPHERE POSITION ---------------------------->>>>>
			//create buffer for sphere position 
			GLES32.glGenBuffers(1,vboVCNT,0);

			//bind buffer
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vboVCNT[0]);


			//create buffer from our sphereVertices array so we can pass it to glBufferData()

			//allocate buffer directly  from native memeory (not VM memory) thats why allocateDirect

			ByteBuffer byteBuffer=ByteBuffer.allocateDirect(cubeVCNT.length*4);


			//arrange the byteorder of buffer in native byte order little indian/big indian

			byteBuffer.order(ByteOrder.nativeOrder());

			//create the float type buffer and convert our byte type buffer into float type BUFFER

			FloatBuffer positionBuffer=byteBuffer.asFloatBuffer();

			//now put our array of sphere_vertices into these cooked buffer
			positionBuffer.put(cubeVCNT);

			//set array at zeroth position of BUFFER
			positionBuffer.position(0);

			GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,
								cubeVCNT.length*4,
								positionBuffer,
								GLES32.GL_STATIC_DRAW);

			GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,11*4,0);
			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

            GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR,3,GLES32.GL_FLOAT,false,11*4,3*4);
            GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR);

            GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_NORMAL,3, GLES32.GL_FLOAT, false,11*4,6*4);
			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_NORMAL);
			
            GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_TEXCOORD0,2, GLES32.GL_FLOAT, false,11*4,9*4);
			GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_TEXCOORD0);
			
            //unbind vbo for sphere position  
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);
            GLES32.glBindVertexArray(0);

			//<<<<<<------------END VBO FOR SPHERE POSITION------------------------------------------------>>>>

			
			
			GLES32.glEnable(GLES32.GL_DEPTH_TEST);
			//disable cull face so we can see back side of geometry while rotation
			GLES32.glDisable(GLES32.GL_CULL_FACE);
			GLES32.glDepthFunc(GLES32.GL_LEQUAL);
			GLES32.glClearColor(0.0f,0.0f,0.0f,1.0f);
           	GLES32.glEnable(GLES32.GL_TEXTURE_2D);

            //call loadtexture function
            marbleTexture[0]=loadTexture(R.raw.marble);

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
			
			GLES32.glUniform3f(lightAmbientUniform, 0.0f, 0.0f, 0.0f);
			GLES32.glUniform3f(lightDiffuseUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform3f(lightSpecularUniform, 1.0f, 1.0f, 1.0f);
			GLES32.glUniform4f(lightPositionUniform, 100.0f, 100.0f, 100.0f,1.0f);
			
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
		Matrix.rotateM(rotationMatrix,0,angleSphere,1.0f,0.0f,0.0f);
		Matrix.rotateM(rotationMatrix,0,angleSphere,0.0f,1.0f,0.0f);
		Matrix.rotateM(rotationMatrix,0,angleSphere,0.0f,0.0f,1.0f);

		Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,translationaMatrix,0);
        Matrix.multiplyMM(modelMatrix,0,modelMatrix,0,rotationMatrix,0);

		//send this matrices to shader
		GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
		GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,perspectiveProjectionMatrix,0);
		GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);


       GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
		GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, marbleTexture[0]);
		GLES32.glUniform1i(samplerUniform, 0);


		//BIND WITH vao of SPHERE
		GLES32.glBindVertexArray(vaoCube[0]);
		
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,0,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,4,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,8,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,12,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,16,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,20,4);

		GLES32.glBindVertexArray(0);

		GLES32.glUseProgram(0);
		requestRender();
	}
	private void update()
	{	
		angleSphere+=rotationSpeed;
		if(angleSphere >= 360.0f)
		{
			angleSphere=0.0f;
		}
	
	}
	private void uninitialize()
	{
		if(vboVCNT[0]!=0)
		{
			GLES32.glDeleteBuffers(1,vboVCNT,0);
			vboVCNT[0]=0;
		}


		if(vaoCube[0]!=0)
		{
			GLES32.glDeleteVertexArrays(1,vaoCube,0);
			vaoCube[0]=0;
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
