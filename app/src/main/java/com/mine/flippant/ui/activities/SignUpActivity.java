package com.mine.flippant.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.mine.flippant.R;
import com.mine.flippant.core.ChatService;
import com.mine.flippant.core.Game;
import com.mine.flippant.ui.views.RoundedImageView;
import com.mine.flippant.utils.DialogUtils;
import com.mine.flippant.utils.ImageUtils;
import com.mine.flippant.utils.ReceiveFileFromBitmapTask;
import com.mine.flippant.utils.ReceiveUriScaledBitmapTask;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;

import android.app.ProgressDialog;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Administrator on 4/12/2016.
 */
public class SignUpActivity extends BaseActivity implements
        ReceiveFileFromBitmapTask.ReceiveFileListener,
        ReceiveUriScaledBitmapTask.ReceiveUriScaledBitmapListener {

    private Context context;
    private RoundedImageView avatarImageView;
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
	private String password;


    private File avatarFile;
    private Boolean isNeedUpdateAvatar;
    private ImageUtils imageUtils;
    private Uri outputUri;
    private Bitmap avatarBitmapCurrent;
    private   QBUser currentUser ;
    public static void start(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        currentUser = null;
        isNeedUpdateAvatar = false;
		context = this;
        imageUtils = new ImageUtils(this);
        avatarFile = null;
        currentUser = new QBUser();
        initUI();
    }

    public void signUpOnClickListener(View view){
        /**
         * Sign
         * */
		
		// Check Empty value
        if(fullNameEditText.getText().length()==0 || emailEditText.getText().length()==0 ||
                passwordEditText.getText().length()==0 || confirmPasswordEditText.getText().length()==0){
            Toast.makeText(context, "Empty Value.", Toast.LENGTH_LONG).show();
            return;

        }
        // check email
        if(!emailEditText.getText().toString().contains("@")){

            Toast.makeText(context, "Email Type is not correct.", Toast.LENGTH_LONG).show();
            return;
        }
		// password  length 8
        if(passwordEditText.getText().length() < 8 ){
            Toast.makeText(context, "Password minimum is 8 characters", Toast.LENGTH_LONG).show();
            return;
        }
		
        // confirm password
        if(!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())){

            Toast.makeText(context, "Password isn't equal.", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.show();
        currentUser.setLogin(fullNameEditText.getText().toString());
        currentUser.setFullName(fullNameEditText.getText().toString());
        currentUser.setPassword( passwordEditText.getText().toString());
        currentUser.setEmail(emailEditText.getText().toString());

        if (isNeedUpdateAvatar) {
            new ReceiveFileFromBitmapTask(this).
                    execute(imageUtils, avatarBitmapCurrent, true);// if no error, go onCachedImageFileReceived
        }else{
        signUpIn();
        }


//        QBUsers.signUp(currentUser, new QBEntityCallback<QBUser>() {
//            @Override
//            public void onSuccess(QBUser qbUser, Bundle bundle) {
//                //progressDialog.hide();
//				Toast.makeText(context, "Sign Up Success.", Toast.LENGTH_LONG).show();
//                //DataHolder.getDataHolder().addQbUserToList(qbUser);
//                //DataHolder.getDataHolder().setSignInQbUser(qbUser);
//                //DataHolder.getDataHolder().setSignInUserPassword(passwordEditText.getText().toString());
//				qbUser.setPassword(password);
//                ChatService.getInstance().login(qbUser, new QBEntityCallback<Void>() {
//
//                    @Override
//                    public void onSuccess(Void result, Bundle bundle) {
//                        // Go to Dialogs screen
//						progressDialog.hide();
//  		            	GameMenuActivity.start(SignUpActivity.this);
////                		finish();
//					}
//					@Override
//                    public void onError(QBResponseException errors) {
//                        progressDialog.hide();
//                        Toast.makeText(SignUpActivity.this, "chat login errors: " + errors, Toast.LENGTH_LONG).show();
////                        AlertDialog.Builder dialog = new AlertDialog.Builder(SignUpActivity.this);
////                        dialog.setMessage("Can't login. Please retry.").create().show();
//                    }
//                });
//            }
//
//            @Override
//            public void onError(QBResponseException error) {
//                progressDialog.hide();
//                DialogUtils.showLong(context, error.getLocalizedMessage());
//            }
//        });

    }
    public void login(){
        ChatService.getInstance().login(currentUser, new QBEntityCallback<Void>() {

            @Override
            public void onSuccess(Void result, Bundle bundle) {
//                progressDialog.hide();
                Game.currentQBUser = currentUser;
                GameMenuActivity.start(SignUpActivity.this);
            }
            @Override
            public void onError(QBResponseException errors) {
                progressDialog.hide();
                Toast.makeText(SignUpActivity.this, "chat login errors: " + errors, Toast.LENGTH_LONG).show();
//                        AlertDialog.Builder dialog = new AlertDialog.Builder(SignUpActivity.this);
//                        dialog.setMessage("Can't login. Please retry.").create().show();
            }
        });
    }
    public void signUpIn()  {

        final String password = currentUser.getPassword();
        currentUser.setOldPassword(password);// it can be use when User is upadated.

        StringifyArrayList<String> stringifyArrayList = new StringifyArrayList<String>();
        stringifyArrayList.add("android");
        currentUser.setTags(stringifyArrayList);

        QBUsers.signUpSignInTask(currentUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                if (avatarFile != null && isNeedUpdateAvatar) {
//                    currentUser.setId(qbUser.getId());
                    UploadAvatar();
                }
                else{
                   login();
                }

            }

            @Override
            public void onError(QBResponseException error) {
                progressDialog.hide();
                Toast.makeText(SignUpActivity.this, "SignUpSignIn errors: " + error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public void UploadAvatar(){
        Boolean fileIsPublic = true;

        QBContent.uploadFileTask(avatarFile, fileIsPublic, null, new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle params) {
                int uploadedFileID = qbFile.getId();
                currentUser.setWebsite(qbFile.getPublicUrl());
                currentUser.setFileId(uploadedFileID);

                QBUsers.updateUser(currentUser, new QBEntityCallback<QBUser>(){
                    @Override
                    public void onSuccess(QBUser user, Bundle args) {

                       login();
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        Toast.makeText(SignUpActivity.this, "Update User errors: " + errors, Toast.LENGTH_LONG).show();
                    }
                });


            }

            @Override
            public void onError(QBResponseException errors) {
                Toast.makeText(SignUpActivity.this, "avatar upload errors: " + errors, Toast.LENGTH_LONG).show();
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {

            }
        });
    }
    public void GoLoginClickListener(View view) {
        startLoginActivity();
    }
    private void startLoginActivity() {
        LoginActivity.start(SignUpActivity.this);
        finish();
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
        progressDialog = DialogUtils.getProgressDialog(this);
        fullNameEditText = _findViewById(R.id.fullname_edittext);
        emailEditText = _findViewById(R.id.email_edittext);
        passwordEditText = _findViewById(R.id.password_edittext);
        avatarImageView = _findViewById(R.id.avatar_imageview);
        confirmPasswordEditText = _findViewById(R.id.confirm_password_edittext);
    }



    public void changeAvatarOnClickListener(View view) {
        imageUtils.getImage();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        } else if (requestCode == ImageUtils.GALLERY_INTENT_CALLED && resultCode == RESULT_OK) {
            Uri originalUri = data.getData();
            if (originalUri != null) {

                new ReceiveUriScaledBitmapTask(this).execute(imageUtils, originalUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void onCachedImageFileReceived(File imageFile) {
        avatarFile = imageFile;
        //SignUp start
        signUpIn();

    }


    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    @Override
    public void onUriScaledBitmapReceived(Uri originalUri) {
        startCropActivity(originalUri);
    }

    private void startCropActivity(Uri originalUri) {
        outputUri = Uri.fromFile(new File(getCacheDir(), Crop.class.getName()));
        new Crop(originalUri).output(outputUri).asSquare().start(this);
    }



    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateAvatar = true;
            avatarBitmapCurrent = imageUtils.getBitmap(outputUri);
            avatarImageView.setImageBitmap(avatarBitmapCurrent);
        } else if (resultCode == Crop.RESULT_ERROR) {
            DialogUtils.showLong(this, Crop.getError(result).getMessage());
        }
    }
}
