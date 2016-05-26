package com.mine.flippant.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mine.flippant.R;
import com.mine.flippant.core.ChatService;
import com.mine.flippant.core.Game;
import com.mine.flippant.ui.views.RoundedImageView;
import com.mine.flippant.utils.Consts;
import com.mine.flippant.utils.DialogUtils;
import com.mine.flippant.utils.ImageUtils;
import com.mine.flippant.utils.ReceiveFileFromBitmapTask;
import com.mine.flippant.utils.ReceiveUriScaledBitmapTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;

import java.io.File;

/**
 * Created by Administrator on 4/12/2016.
 */


public class ProfileActivity extends BaseActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener, ReceiveUriScaledBitmapTask.ReceiveUriScaledBitmapListener {

    private QBUser currentUser ;
    private Context context;
    private RoundedImageView avatarImageView;
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private ActionBar actionBar;

    private File avatarFile;
    private Boolean isNeedUpdateAvatar;
    private ImageUtils imageUtils;
    private Uri outputUri;
    private Bitmap avatarBitmapCurrent;

    public static void start(Context context){
        Intent intent =  new Intent(context,ProfileActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        isNeedUpdateAvatar = false;
        context = this;
        imageUtils = new ImageUtils(this);
        avatarFile = null;
        currentUser = new QBUser();
//        actionBar = getSupportActionBar();

//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);
        initUI();
    }

    @Override
    public void onBackPressed() {
        goGameMain();
    }

    public void onProfileConfirm(View view){
        //TODO Code for saving profile
        // Check Empty value
        if(fullNameEditText.getText().length()==0 || emailEditText.getText().length()==0 ||
                oldPasswordEditText.getText().length()==0 || newPasswordEditText.getText().length()==0||
                confirmPasswordEditText.getText().length()==0){
            Toast.makeText(ProfileActivity.this, "Empty Value.", Toast.LENGTH_LONG).show();
            return;

        }
        // check email
        if(emailEditText.getText().toString().indexOf("@") <0 ){

            Toast.makeText(context, "Email type is not correct.", Toast.LENGTH_LONG).show();
            return;
        }
        // password  length 8
        if(oldPasswordEditText.getText().length() < 8 || newPasswordEditText.getText().length() < 8||
                confirmPasswordEditText.getText().length() < 8){
            Toast.makeText(ProfileActivity.this, "Password minimum is 8 characters", Toast.LENGTH_LONG).show();
            return;
        }
        // confirm password
        if(!newPasswordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())){
            Toast.makeText(ProfileActivity.this, "Password isn't equal.", Toast.LENGTH_LONG).show();
            return;
        }


        currentUser.setId(Game.currentQBUser.getId());
        currentUser.setLogin(fullNameEditText.getText().toString());
        currentUser.setEmail(emailEditText.getText().toString());
        currentUser.setFullName(fullNameEditText.getText().toString());
        currentUser.setOldPassword(oldPasswordEditText.getText().toString());
        currentUser.setPassword(confirmPasswordEditText.getText().toString());

        if (isNeedUpdateAvatar) {
            new ReceiveFileFromBitmapTask(this).
                    execute(imageUtils, avatarBitmapCurrent, true);// if no error, go onCachedImageFileReceived
        }else{
            UpdateProfile();
        }


    }
    private void UpdateProfile(){
        if (avatarFile != null && isNeedUpdateAvatar) {
//                    currentUser.setId(qbUser.getId());
            UploadAvatar();
        }
        else{
            UpdateAll();
        }

    }
    private void UpdateAll(){
        QBUsers.updateUser(currentUser, new QBEntityCallback<QBUser>(){
            @Override
            public void onSuccess(QBUser user, Bundle args) {
                Game.currentQBUser = currentUser;
                Toast.makeText(ProfileActivity.this, "Profile Updated.", Toast.LENGTH_LONG).show();
                goGameMain();
            }

            @Override
            public void onError(QBResponseException errors) {
                Toast.makeText(ProfileActivity.this, "Profile Update Failed."+errors.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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

                UpdateAll();


            }

            @Override
            public void onError(QBResponseException errors) {
                Toast.makeText(ProfileActivity.this, "avatar upload errors: " + errors, Toast.LENGTH_LONG).show();
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {

            }
        });
    }
    @SuppressWarnings("unchecked")
    protected <T> T _findViewById(int viewId) {
        return (T) findViewById(viewId);
    }

	private void initUI() {
        fullNameEditText = _findViewById(R.id.login_edittext);
        emailEditText = _findViewById(R.id.profile_email_edittext);
        oldPasswordEditText = _findViewById(R.id.current_password_edittext);
        newPasswordEditText = _findViewById(R.id.new_password_edittext);
        confirmPasswordEditText = _findViewById(R.id.confirm_new_password_edittext);
        avatarImageView = _findViewById(R.id.avatar_imageview);
        if(Game.currentQBUser != null){
            if(Game.currentQBUser.getWebsite() != null){
                String url = Game.currentQBUser.getWebsite();
                ImageLoader.getInstance().displayImage(url, avatarImageView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
            }
            fillField(fullNameEditText, Game.currentQBUser.getLogin());
            emailEditText.setText(Game.currentQBUser.getEmail());
            oldPasswordEditText.setText(Game.currentQBUser.getPassword());
        }



    }
	
	protected void fillField(TextView textView, String value) {
        if (!TextUtils.isEmpty(value)) {
            textView.setText(value);
        }
    }
	
    public void backToMain(View view){
        goGameMain();
    }

    private void goGameMain(){
        GameMenuActivity.start(ProfileActivity.this);
        finish();
    }

    public void onLogOut(View view){
        //TODO Look here
        ChatService.getInstance().logout();
        Toast.makeText(ProfileActivity.this, "Log out succeeded.", Toast.LENGTH_SHORT).show();
        LandingActivity.start(ProfileActivity.this);
        finish();
    }

    //  avatar change


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
        UpdateProfile();

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
