package com.example.try_td_test;

import android.app.Activity;

public class CommonUtil {
	public static int screenWidth;
	public static int screenHeight;
	public static int statusBarHeight;
	
	public static int getStatusBarHeight(Activity activity) { 
	      int result = 0;
	      int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
	      if (resourceId > 0) {
	          result = activity.getResources().getDimensionPixelSize(resourceId);
	      } 
	      return result;
	}
}
