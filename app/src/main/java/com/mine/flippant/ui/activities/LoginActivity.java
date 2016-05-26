package com.mine.flippant.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mine.flippant.R;
import com.mine.flippant.core.ChatService;
import com.mine.flippant.core.Game;
import com.mine.flippant.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
/**
 * Created by Administrator on 4/12/2016.
 */

public class LoginActivity extends BaseActivity {

    private Context context;
	private EditText username_edit;
    private EditText email_edit ;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    protected ProgressDialog progressDialog;
    private String password;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        initUI();
    }
    public void GoSignUpOnClickListener(View view) {
        SignUpActivity.start(LoginActivity.this);
        finish();
    }
    public void loginOnClickListener(View view){
        /**
         *  Login progress
         * */

        if(email_edit.getText().toString().length() ==0 || username_edit.getText().toString().length() ==0){
            Toast.makeText(context, "Empty Value.", Toast.LENGTH_LONG).show();
            return;

        }
        // check email
        if(email_edit.getText().toString().indexOf("@") <0 ){

            Toast.makeText(context, "Email type is not correct.", Toast.LENGTH_LONG).show();
            return;
        }
		if( passwordEditText.getText().length() < 8){
            Toast.makeText(context, "Minimum password length is 8", Toast.LENGTH_LONG).show();
            return; //wrong input
        }
        QBUser user = new QBUser();
        password = passwordEditText.getText().toString();
		user.setEmail(email_edit.getText().toString());
		user.setLogin(username_edit.getText().toString());
		user.setPassword(password);

        progressDialog.show();
        QBUsers.signIn(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle params) {
                user.setPassword(password);
                if(ChatService.getInstance().getCurrentUser() != null){
                    ChatService.getInstance().logout();
                }
                ChatService.getInstance().login(user, new QBEntityCallback<Void>() {

                    @Override
                    public void onSuccess(Void result, Bundle bundle) {
                        // Go to Dialogs screen
                        progressDialog.hide();
                        Game.currentQBUser = ChatService.getInstance().getCurrentUser();
                        GameMenuActivity.start(LoginActivity.this);


            }

                    @Override
                    public void onError(QBResponseException errors) {
                        progressDialog.hide();
                        if(errors.getLocalizedMessage().indexOf("log")<0){
                            Game.currentQBUser = ChatService.getInstance().getCurrentUser();
                            GameMenuActivity.start(LoginActivity.this);
                        }

                        //AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                        //dialog.setMessage("chat login errors: " + errors).create().show();
                        Toast.makeText(LoginActivity.this, "login error:" +errors.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(QBResponseException errors) {
                progressDialog.hide();
                Toast.makeText(LoginActivity.this, "sign In error:" +errors.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }
    @Override
    public void onBackPressed() {
        LandingActivity.start(this);
        finish();
    }
    @SuppressWarnings("unchecked")
    protected <T> T _findViewById(int viewId) {
        return (T) findViewById(viewId);
    }

    private void initUI() {
        progressBar = _findViewById(R.id.progressBar);
        progressDialog = DialogUtils.getProgressDialog(this);
		email_edit= _findViewById(R.id.email_edit);
        username_edit =_findViewById(R.id.username_edit);
        passwordEditText = _findViewById(R.id.password_edittext);
    }
}
