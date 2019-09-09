package com.RTR.win_Event;

//added by me
import androidx.appcompat.widget.AppCompatTextView;
import android.content.Context;
import android.view.Gravity;
import android.graphics.Color;

//for MotionEvent
import android.view.MotionEvent;

//for GestureDetector
import android.view.GestureDetector;

//for OnGestureListener
import android.view.GestureDetector.OnGestureListener;

//for OnDoubleTapListener
import android.view.GestureDetector.OnDoubleTapListener;

//implement all the method from implemented class 
public class MyView extends AppCompatTextView implements OnGestureListener,OnDoubleTapListener
{	
	private GestureDetector gestureDetector; //field class varaible
	public MyView(Context context)
	{
		
		super(context);
		setTextColor(Color.rgb(255,128,0));
		setTextSize(60);
		setGravity(Gravity.CENTER);
		setText("Hello World!!!");

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
		setText("Double Tap");
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
		setText("Single Tap");
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
		setText("Long Press");
	}
	
	//abstract method from OnGestureListener so must be implemented
	@Override
	public boolean onScroll(MotionEvent e1,MotionEvent e2,float distanceX,float distanceY)
	{
		setText("Scroll");
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


}