package com.aof.mcinabox.gamecontroller.ckb;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.aof.mcinabox.R;
import com.aof.mcinabox.gamecontroller.ckb.achieve.CkbManager;
import com.aof.mcinabox.gamecontroller.ckb.achieve.CkbManagerDialog;
import com.aof.mcinabox.gamecontroller.ckb.support.CallCustomizeKeyboard;
import com.aof.mcinabox.gamecontroller.client.Client;
import com.aof.mcinabox.gamecontroller.controller.Controller;
import com.aof.mcinabox.gamecontroller.controller.VirtualController;
import com.aof.mcinabox.gamecontroller.definitions.id.key.KeyEvent;
import com.aof.mcinabox.gamecontroller.input.screen.CustomizeKeyboard;
import com.aof.mcinabox.utils.PicUtils;

import java.util.Timer;
import java.util.TimerTask;

public class CustomizeKeyboardEditorActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener, View.OnClickListener, DrawerLayout.DrawerListener, CallCustomizeKeyboard, Client {

    private Toolbar mToolbar;
    private ViewGroup mLayout_main;
    private DrawerLayout mDrawerLayout;
    private AppCompatToggleButton toggleButtonMode;

    private int screenWidth;
    private int screenHeight;

    private int pointer[] = new int[]{0, 0};
    private Controller mController;
    private boolean isGrabbed;
    private TimerTask systemUiTimerTask;
    private Timer mTimer;
    private final static int SYSTEM_UI_HIDE_DELAY_MS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //?????????????????????
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ckbe);

        //?????????
        screenWidth = this.getResources().getDisplayMetrics().widthPixels;
        screenHeight = this.getResources().getDisplayMetrics().heightPixels;
        initUI();

        //??????
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                hideSystemUI(getWindow().getDecorView());
                mTimer = new Timer();
            }
        });
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(this);
            hideSystemUI(decorView);
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(null);
            if (systemUiTimerTask != null) systemUiTimerTask.cancel();
        }
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            if (systemUiTimerTask != null) systemUiTimerTask.cancel();
            systemUiTimerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> hideSystemUI(getWindow().getDecorView()));
                }
            };
            mTimer.schedule(systemUiTimerTask, SYSTEM_UI_HIDE_DELAY_MS);
        }
    }

    private void initUI() {

        mToolbar = findViewById(R.id.ckbe_toolbar);
        mLayout_main = findViewById(R.id.ckbe_layout_main);
        mDrawerLayout = findViewById(R.id.ckbe_drawerlayout);
        toggleButtonMode = findViewById(R.id.activity_ckbe_toggle_mode);

        //???????????????
        setSupportActionBar(mToolbar);

        //????????????
        mLayout_main.setOnClickListener(this);
        mDrawerLayout.addDrawerListener(this);
        toggleButtonMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mController != null) {
                    mController.setGrabCursor(isChecked);
                    CustomizeKeyboardEditorActivity.this.isGrabbed = isChecked;
                }
            }
        });

        //????????????
        mLayout_main.setBackground(new BitmapDrawable(getResources(), PicUtils.blur(this, 10, ((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.background)).getBitmap())));

        //??????????????????
        mController = new VirtualController(this, KeyEvent.KEYMAP_TO_X) {
            @Override
            public void init() {
                super.init();
                //?????????????????????
                this.removeInput(onscreenTouchpad);
                //?????????????????????
                this.custmoizeKeyboard.setEnabled(false);
                //?????????????????????
                ((CustomizeKeyboard)this.custmoizeKeyboard).mManager.autoSaveKeyboard();
                //?????????????????????
                this.removeInput(custmoizeKeyboard);
                //??????????????????????????????????????????????????????
                this.custmoizeKeyboard = new CustomizeKeyboard() {
                    @Override
                    public boolean load(Context context, Controller controller) {
                        //????????????????????????????????????????????????????????????????????????
                        this.mManager = new CkbManager(context, CustomizeKeyboardEditorActivity.this, null);
                        this.mDialog = new CkbManagerDialog(context, mManager);
                        return true;
                    }
                };
                //???????????????????????????
                this.addInput(custmoizeKeyboard);
                //???????????????????????????
                this.custmoizeKeyboard.setEnabled(true);
                //?????????????????????????????????????????????
                bindViewWithInput();
            }
        };
    }


    @Override
    public void onClick(View v) {
        if (v == mLayout_main) {
            switchToolbar();
        }
    }

    private Float viewPosY;

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        if (viewPosY == null) {
            viewPosY = mToolbar.getY();
        }
        int viewHeight = mToolbar.getHeight();
        float slideSize = viewHeight * slideOffset;
        mToolbar.setY(viewPosY - slideSize);

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    private void switchToolbar() {
        int v = View.VISIBLE;
        switch (mToolbar.getVisibility()) {
            case View.INVISIBLE:
            case View.GONE:
                v = View.VISIBLE;
                break;
            case View.VISIBLE:
                v = View.GONE;
                break;
            default:
                break;
        }
        mToolbar.setVisibility(v);
    }

    @Override
    public void setKey(int keyCode, boolean pressed) {
        //stub
    }

    @Override
    public void setMouseButton(int mouseCode, boolean pressed) {
        //stub
    }

    @Override
    public void setPointer(int x, int y) {
        //stub
    }

    @Override
    public void setPointerInc(int xInc, int yInc) {
        //stub
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void addView(View view) {
        if (view.getLayoutParams() == null) {
            return;
        }
        if (!(view.getLayoutParams() instanceof RelativeLayout.LayoutParams)) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(view.getLayoutParams().width, view.getLayoutParams().height);
            view.setLayoutParams(params);
        }
        this.mLayout_main.addView(view);
    }

    @Override
    public void typeWords(String str) {
        //stub
    }

    @Override
    public int[] getGrabbedPointer() {
        return pointer;
    }

    @Override
    public int[] getLoosenPointer() {
        return mController.getLossenPointer();
    }

    @Override
    public ViewGroup getViewsParent() {
        return mLayout_main;
    }

    @Override
    public View getSurfaceLayerView() {
        return mLayout_main;
    }

    @Override
    public boolean isGrabbed() {
        return this.isGrabbed;
    }

    @Override
    public void onStop() {
        super.onStop();
        //???Activity???????????????????????????????????????
        mController.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mController.onResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        mController.onPaused();
    }

    private void hideSystemUI(View decorView) {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}