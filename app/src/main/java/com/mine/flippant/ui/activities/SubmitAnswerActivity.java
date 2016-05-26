package com.mine.flippant.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mine.flippant.R;
import com.mine.flippant.core.Game;

public class SubmitAnswerActivity extends AppCompatActivity {

    private EditText answerEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_answer);
        answerEditText = (EditText) findViewById(R.id.answer_writing_textview);
        //TODO remove following before release
        //answerEditText.setText(Game.answerSubmitted);

    }

    public static void start(Context context){
        Intent intent = new Intent(context, SubmitAnswerActivity.class);
        context.startActivity(intent);
    }

    public void onBackImage(View view){
        onBackPressed();
        finish();
    }

    public void onSubmitAnswer(View view){
        String ans = answerEditText.getText().toString();
        if (ans.equals(""))
            Toast.makeText(SubmitAnswerActivity.this, "Answer cannot be empty.", Toast.LENGTH_SHORT).show();
        else {
            Game.answerSubmitted = ans;
            GameActivity.questioning.refresh(ans);
            onBackPressed();
            finish();
        }

    }
}
