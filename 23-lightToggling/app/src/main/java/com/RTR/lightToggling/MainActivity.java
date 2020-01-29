package com.RTR.lightToggling;
//default packages
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
//packages added by me
import android.view.Window;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;
import android.graphics.Color;



//to remove navigation bar
import android.view.View;


public class MainActivity extends AppCompatActivity {
	private GLESView glesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	   //setContentView(R.layout.activity_main);

	//to remove title bar of window
	supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

	//make fullscreen
	this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

	//to remove navigation bar
	this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE);

	//to do forced landscape orientation
	//this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

	//set background color
	this.getWindow().getDecorView().setBackgroundColor(Color.BLACK);



	//define our own view

	 glesView=new GLESView(this);

	//set view as content view of the activity(means window)
	setContentView(glesView);
}//on create end

	@Override
	protected void onPause()
	{
		super.onPause();
	} //onpause end

	@Override
	protected void onResume()
	{
		super.onResume();
	}
}//class end
