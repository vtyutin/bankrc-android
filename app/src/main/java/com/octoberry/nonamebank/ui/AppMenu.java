package com.octoberry.nonamebank.ui;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.octoberry.nonamebank.DashboardActivity;
import com.octoberry.nonamebank.R;
import com.octoberry.nonamebank.chat.ChatActivity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class AppMenu {
	
	private SlidingMenu mMenu;
	private Activity mActivity;
	
	public AppMenu(Activity activity) {
		mActivity = activity;
		mMenu = new SlidingMenu(mActivity);
		mMenu.setMode(SlidingMenu.LEFT);
		mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mMenu.setShadowWidthRes(R.dimen.shadow_width);
		mMenu.setShadowDrawable(R.drawable.shadow);
		mMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mMenu.setFadeDegree(0.35f);
		mMenu.setMenu(R.layout.menu);
		mMenu.findViewById(R.id.startChatTextView).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				showChat();				
			}
		});
		mMenu.findViewById(R.id.dashboardTextView).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				showDashboard();				
			}
		});
		
		if (mActivity != null) {
			mMenu.attachToActivity(mActivity, SlidingMenu.SLIDING_CONTENT);
		}
	}
	
	public void showContent() {
		mMenu.showContent();		
	}
	
	public void showMenu() {
		mMenu.showMenu();
	}
	
	public void showChat() {
		Intent intent = new Intent(mActivity, ChatActivity.class);
		mActivity.startActivity(intent);
	}
	
	public void showDashboard() {
		Intent intent = new Intent(mActivity, DashboardActivity.class);
		mActivity.startActivity(intent);
		mActivity.finish();
	}
}
