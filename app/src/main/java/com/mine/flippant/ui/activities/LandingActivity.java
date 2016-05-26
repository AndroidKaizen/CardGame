package com.mine.flippant.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.mine.flippant.R;
import com.mine.flippant.utils.Consts;
import com.mine.flippant.utils.DialogUtils;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;

/**
 * Created by Administrator on 4/12/2016.
 */
public class LandingActivity extends AppCompatActivity {

	public static QBSettings app ;
    public static final int DOUBLE_BACK_DELAY = 2000;
    private Context context;
    private boolean doubleBackToExitPressedOnce;
    protected boolean useDoubleBackPressed;
    
    public static void start(Context context) {
        Intent intent = new Intent(context, LandingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        useDoubleBackPressed = true;
        context = this;
        if(app == null) {
            app = QBSettings.getInstance().init(getApplicationContext(), Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
            QBSettings.getInstance().setAccountKey(Consts.ACCOUNT_KEY);
            QBAuth.createSession(new QBEntityCallback<QBSession>() {
                @Override
                public void onSuccess(QBSession qbSession, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException errors) {
                    // print errors that came from server
                    DialogUtils.showLong(context, errors.toString());
                }
            });
        }
    }


    public void signUpPageGo(View view) {
        SignUpActivity.start(LandingActivity.this);
        finish();
    }

    public void loginPageGo(View view) {
        LoginActivity.start(LandingActivity.this);
        finish();
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce || !useDoubleBackPressed) {
            super.onBackPressed();
            onDestroy();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        DialogUtils.show(this, getString(R.string.dlg_click_back_again));
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, DOUBLE_BACK_DELAY);
    }


}
