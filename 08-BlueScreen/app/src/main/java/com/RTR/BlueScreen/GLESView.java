package com.RTR.BlueScreen;

//added by me

import android.content.Context;
import android.view.Gravity;
import android.graphics.Color;

//view which will support OpenGL
import android.opengl.GLSurfaceView;

//for OpenGL ES 3.2 
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
		String version=gl.glGetString(GL10.GL_VERSION);
		System.out.println("AMC:"+version);

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
		GLES32.glClearColor(0.0f,0.0f,1.0f,1.0f);
	}

	private void resize(int width,int height)
	{	
		
		GLES32.glViewport(0,0,width,height);
	}	

	private void display()
	{
		GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT|GLES32.GL_DEPTH_BUFFER_BIT);
		requestRender();
	}
}