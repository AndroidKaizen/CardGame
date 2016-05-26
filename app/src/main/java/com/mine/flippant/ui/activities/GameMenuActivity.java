package com.mine.flippant.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.mine.flippant.R;
import com.mine.flippant.core.ChatService;
import com.mine.flippant.core.Game;

/**
 * Created by Administrator on 4/12/2016.
 */
public class GameMenuActivity extends BaseActivity {
    public  static  void  start(Context context){
        Intent intent = new Intent(context, GameMenuActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);
        Game.currentQBUser = ChatService.getInstance().getCurrentUser();
    }

    @Override
    public void onBackPressed() {
        ChatService.getInstance().logout();
        Toast.makeText(GameMenuActivity.this, "Log out success.", Toast.LENGTH_SHORT).show();
        LandingActivity.start(this);
        finish();
    }

    public void onRate(View view){
        //TODO code here for rating the game
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(GameMenuActivity.this);
            dialog.setMessage("Couldn't launch the market").create().show();
        }
    }

    public void onSettings(View view){
        ProfileActivity.start(GameMenuActivity.this);
        finish();
    }

    public void onNewGame(View view){
        NewGameActivity.start(GameMenuActivity.this);
    }

    public void onJoinGame(View view){
        GamesActivity.start(GameMenuActivity.this);
    }
}
