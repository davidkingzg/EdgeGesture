package com.omarea.gesture;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

class FloatVitualTouchBar {
    private static WindowManager mWindowManager = null;
    private boolean isLandscapf;
    private View bottomView = null;
    private View leftView = null;
    private View rightView = null;
    private long lastEventTime = 0L;
    private int lastEvent = -1;
    private SharedPreferences config;

    public FloatVitualTouchBar(AccessibilityService context, boolean isLandscapf) {
        this.isLandscapf = isLandscapf;
        config = context.getSharedPreferences(SpfConfig.ConfigFile, Context.MODE_PRIVATE);
        mWindowManager = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        try {
            if (config.getBoolean(SpfConfig.CONFIG_BOTTOM_ALLOW, SpfConfig.CONFIG_BOTTOM_ALLOW_DEFAULT)) {
                this.bottomView = setBottomView(context);
            }
            if (config.getBoolean(SpfConfig.CONFIG_LEFT_ALLOW, SpfConfig.CONFIG_LEFT_ALLOW_DEFAULT)) {
                this.leftView = setLeftView(context);
            }
            if (config.getBoolean(SpfConfig.CONFIG_RIGHT_ALLOW, SpfConfig.CONFIG_RIGHT_ALLOW_DEFAULT)) {
                this.rightView = setRightView(context);
            }
        } catch (Exception ex) {
            Log.d("异常", ex.getLocalizedMessage());
            Toast.makeText(context, "启动虚拟导航手势失败！", Toast.LENGTH_LONG).show();
        }
    }

    int getNavBarHeight(Context context) {
        int resourceId = 0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    /**
     * 隐藏弹出框
     */
    void hidePopupWindow() {
        if (this.bottomView != null) {
            mWindowManager.removeView(this.bottomView);
        }
        if (this.leftView != null) {
            mWindowManager.removeView(this.leftView);
        }
        if (this.rightView != null) {
            mWindowManager.removeView(this.rightView);
        }
        // KeepShellPublic.doCmdSync("wm overscan reset");
    }

    /**
     * dp转换成px
     */
    private int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void performGlobalAction(AccessibilityService context, int event) {
        if (isLandscapf && (lastEventTime + 1500 < System.currentTimeMillis() || lastEvent != event)) {
            lastEvent = event;
            lastEventTime = System.currentTimeMillis();
            Toast.makeText(context, "请重复手势~", Toast.LENGTH_SHORT).show();
        } else {
            context.performGlobalAction(event);
        }
    }

    private View setBottomView(final AccessibilityService context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.fw_vitual_touch_bar, null);

        TouchBarView bar = view.findViewById(R.id.bottom_touch_bar);

        bar.setEventHandler(
                config.getInt(SpfConfig.CONFIG_BOTTOM_EVBET, SpfConfig.CONFIG_BOTTOM_EVBET_DEFAULT),
                config.getInt(SpfConfig.CONFIG_BOTTOM_EVBET_HOVER, SpfConfig.CONFIG_BOTTOM_EVBET_HOVER_DEFAULT),
                context);
        bar.setBarPosition(TouchBarView.BOTTOM, isLandscapf);

        final LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;
        params.width = LayoutParams.MATCH_PARENT; // minSize; //
        params.height = LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        mWindowManager.addView(view, params);

        return view;
    }

    private View.OnTouchListener getTouchPierceListener(final WindowManager.LayoutParams params, final View view) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_OUTSIDE) {
                    params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    mWindowManager.updateViewLayout(view, params);
                } else if (action == MotionEvent.ACTION_DOWN) {
                    params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                    mWindowManager.updateViewLayout(view, params);
                    return true;
                } else {
                    Log.d("OnTouchListener", "" + action);
                }
                return false;
            }
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    private View setLeftView(final AccessibilityService context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.fw_vitual_touch_bar, null);
        TouchBarView bar = view.findViewById(R.id.bottom_touch_bar);

        bar.setEventHandler(config.getInt(SpfConfig.CONFIG_LEFT_EVBET, SpfConfig.CONFIG_LEFT_EVBET_DEFAULT), config.getInt(SpfConfig.CONFIG_LEFT_EVBET_HOVER, SpfConfig.CONFIG_LEFT_EVBET_HOVER_DEFAULT), context);

        bar.setBarPosition(TouchBarView.LEFT, isLandscapf);

        final LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.MATCH_PARENT;

        params.gravity = Gravity.START | Gravity.FILL_VERTICAL;
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        mWindowManager.addView(view, params);
        // view.setOnTouchListener(getTouchPierceListener(params, view));

        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View setRightView(final AccessibilityService context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.fw_vitual_touch_bar, null);

        TouchBarView bar = view.findViewById(R.id.bottom_touch_bar);

        bar.setEventHandler(config.getInt(SpfConfig.CONFIG_RIGHT_EVBET, SpfConfig.CONFIG_RIGHT_EVBET_DEFAULT), config.getInt(SpfConfig.CONFIG_RIGHT_EVBET_HOVER, SpfConfig.CONFIG_RIGHT_EVBET_HOVER_DEFAULT), context);

        bar.setBarPosition(TouchBarView.RIGHT, isLandscapf);

        final LayoutParams params = new LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.TRANSLUCENT;

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.MATCH_PARENT;

        params.gravity = Gravity.END | Gravity.FILL_VERTICAL;
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        mWindowManager.addView(view, params);
        // view.setOnTouchListener(getTouchPierceListener(params, view));

        return view;
    }
}