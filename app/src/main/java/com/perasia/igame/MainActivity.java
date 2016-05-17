package com.perasia.igame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.endless.game.android.model.InitListener;
import com.endless.game.android.sdk.GameListener;
import com.endless.game.android.sdk.GameSDK;
import com.paycenter.sdk.GamePay;
import com.paycenter.ui.model.BaseListener;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String SCORE = "score";
    public static final String HIGH_SCORE = "high score temp";
    public static final String UNDO_SCORE = "undo score";
    public static final String CAN_UNDO = "can undo";
    public static final String UNDO_GRID = "undo";
    public static final String GAME_STATE = "game state";
    public static final String UNDO_GAME_STATE = "undo game state";

    private Context mContext;

    private MainView mView;

    private WebView mWebView;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mView = (MainView) findViewById(R.id.main_main_view);
        mWebView = (WebView) findViewById(R.id.ad_banner_wv);
        mTextView = (TextView) findViewById(R.id.game_sdk_tv);

        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);
        mView.hasSaveState = setting.getBoolean("save_state", false);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("hasState")) {
                load();
            }
        }

        AdUtil.setAds(mContext, mWebView);

        AdUtil.setOnWebViewGoneListener(new AdUtil.OnWebViewGoneListener() {
            @Override
            public void onGone() {
                mWebView.setVisibility(View.GONE);
                mTextView.setVisibility(View.VISIBLE);
            }
        });


        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("amount", "1");
                params.put("serverid", 1 + "");
                params.put("product_name", "1元10金币");
                params.put("extra_info", "");

                Log.e(TAG, "pay click");

                GamePay.pay(MainActivity.this, params, new BaseListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        Log.e(TAG, "pay onSuccess" + jsonObject.toString());
                    }

                    @Override
                    public void onFail(JSONObject jsonObject) {
                        Log.e(TAG, "pay onFail" + jsonObject.toString());
                    }
                });
            }
        });


        initGameSdk();
    }


    private void initGameSdk() {
        GameSDK.init("4d46e56bae6d208263b6fffad98f5252", this, new InitListener() {
            @Override
            public void onComplete() {
                Log.e(TAG, "init onComplete");
                GameSDK.login(new GameListener() {
                    @Override
                    public void onComplete(JSONObject jsonObject) {
                        Log.e(TAG, "onComplete" + jsonObject.toString());
                    }

                    @Override
                    public void onCancel() {
                        Log.e(TAG, "onCancel");
                    }

                    @Override
                    public void onFail(JSONObject jsonObject) {
                        Log.e(TAG, "onFail" + jsonObject.toString());
                    }
                });
            }
        });

        GameSDK.isOpenUserCenter = false;
        GameSDK.hideToolbar();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("hasState", true);
        save();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        load();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        save();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mView.game.move(2);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                mView.game.move(0);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mView.game.move(3);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mView.game.move(1);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void load() {
        //Stopping all animations
        mView.game.aGrid.cancelAnimations();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        for (int xx = 0; xx < mView.game.grid.field.length; xx++) {
            for (int yy = 0; yy < mView.game.grid.field[0].length; yy++) {
                int value = settings.getInt(xx + " " + yy, -1);
                if (value > 0) {
                    mView.game.grid.field[xx][yy] = new Tile(xx, yy, value);
                } else if (value == 0) {
                    mView.game.grid.field[xx][yy] = null;
                }

                int undoValue = settings.getInt(UNDO_GRID + xx + " " + yy, -1);
                if (undoValue > 0) {
                    mView.game.grid.undoField[xx][yy] = new Tile(xx, yy, undoValue);
                } else if (value == 0) {
                    mView.game.grid.undoField[xx][yy] = null;
                }
            }
        }

        mView.game.score = settings.getLong(SCORE, mView.game.score);
        mView.game.highScore = settings.getLong(HIGH_SCORE, mView.game.highScore);
        mView.game.lastScore = settings.getLong(UNDO_SCORE, mView.game.lastScore);
        mView.game.canUndo = settings.getBoolean(CAN_UNDO, mView.game.canUndo);
        mView.game.gameState = settings.getInt(GAME_STATE, mView.game.gameState);
        mView.game.lastGameState = settings.getInt(UNDO_GAME_STATE, mView.game.lastGameState);

    }

    private void save() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        Tile[][] field = mView.game.grid.field;
        Tile[][] undoField = mView.game.grid.undoField;
        editor.putInt(WIDTH, field.length);
        editor.putInt(HEIGHT, field.length);
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] != null) {
                    editor.putInt(xx + " " + yy, field[xx][yy].getValue());
                } else {
                    editor.putInt(xx + " " + yy, 0);
                }

                if (undoField[xx][yy] != null) {
                    editor.putInt(UNDO_GRID + xx + " " + yy, undoField[xx][yy].getValue());
                } else {
                    editor.putInt(UNDO_GRID + xx + " " + yy, 0);
                }
            }
        }
        editor.putLong(SCORE, mView.game.score);
        editor.putLong(HIGH_SCORE, mView.game.highScore);
        editor.putLong(UNDO_SCORE, mView.game.lastScore);
        editor.putBoolean(CAN_UNDO, mView.game.canUndo);
        editor.putInt(GAME_STATE, mView.game.gameState);
        editor.putInt(UNDO_GAME_STATE, mView.game.lastGameState);
        editor.apply();
    }
}
