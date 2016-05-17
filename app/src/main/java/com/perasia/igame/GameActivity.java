package com.perasia.igame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.endless.game.android.model.InitListener;
import com.endless.game.android.sdk.GameListener;
import com.endless.game.android.sdk.GameSDK;
import com.paycenter.sdk.GamePay;
import com.paycenter.ui.model.BaseListener;

import org.json.JSONObject;

import java.util.HashMap;


public class GameActivity extends AppCompatActivity {
    private static final String TAG = GameActivity.class.getSimpleName();

    private Button mPay;
    private Button mRestart;
    private Button mCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mPay = (Button) findViewById(R.id.pay);
        mRestart = (Button) findViewById(R.id.restatr);
        mCenter = (Button) findViewById(R.id.center);

        GameSDK.init("d128360c051b94077118048bf92457fd", this, new InitListener() {
            @Override
            public void onComplete() {
                Log.e(TAG, "init onComplete");
            }
        });

        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("amount", "1");
                params.put("serverid", 1 + "");
                params.put("product_name", "1元10金币");
                params.put("extra_info", "");

                Log.e(TAG, "pay click");

                GamePay.pay(GameActivity.this, params, new BaseListener() {
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

        mRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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


        mCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameSDK.openUserCenter();
            }
        });

    }
}
