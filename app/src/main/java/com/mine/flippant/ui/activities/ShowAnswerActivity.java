package com.mine.flippant.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mine.flippant.R;
import com.mine.flippant.core.Game;

public class ShowAnswerActivity extends AppCompatActivity {

    private EditText answerEditText;
    private String answer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        answer = getIntent().getExtras().getString("ShowAnswer");
        setContentView(R.layout.activity_show_answer);
        answerEditText = (EditText) findViewById(R.id.answer_writing_textview);
        answerEditText.setText(answer);

    }

    public static void start(Context context, String answer){
        Intent intent = new Intent(context, ShowAnswerActivity.class);
        intent.putExtra("ShowAnswer", answer);
        context.startActivity(intent);
    }

    public void onBackImage(View view){
        onBackPressed();
        finish();
    }
}
