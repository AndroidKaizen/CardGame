package com.mine.flippant.ui.activities;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mine.flippant.R;
import com.mine.flippant.core.Game;
import com.mine.flippant.core.GameMessage;

public class QuesAnsFragment extends Fragment {

    private static final String IMAGEVIEW_TAG = "Draging Image";

    private String question;
    private View rootView;
    private TextView answerText;
    private TextView questionText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question_answer, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        rootView = getView();
        if (rootView != null) {
            int cardSize;
            questionText = (TextView) rootView.findViewById(R.id.questionCardText);
            //cardSize = questionText.getLayoutParams().height;
            //questionText.setLayoutParams(new ViewGroup.LayoutParams(cardSize / 5 * 4, cardSize));
            answerText = (TextView) rootView.findViewById(R.id.answerCardText);
            //cardSize = answerText.getLayoutParams().height;
            //answerText.setLayoutParams(new ViewGroup.LayoutParams(cardSize / 5 * 4, cardSize));

            answerText.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    SubmitAnswerActivity.start(getActivity());
                }
            });
            questionText.setText(question);
            Game.state = Game.GAME_QUESTION;    // Receive question
            Game.state = Game.GAME_ANSWER;      // Writing answer
            answerText.setTag(IMAGEVIEW_TAG);   //TODO Check necessity
            answerText.setOnLongClickListener(new LongClickListener());
            rootView.findViewById(R.id.questionCardText).setOnDragListener(new AnswerDragListener());
            //TODO check whether following is necessary.
            //rootView.findViewById(R.id.answerCardText).setOnDragListener(new AnswerDragListener());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //savedInstanceState.putLong("workoutId", workoutId);
    }

    //Drag and drop for submit answer
    private class AnswerDragListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    if (v == rootView.findViewById(R.id.questionCardText)) {
                        TextView view = (TextView) event.getLocalState();
                        TextView questionText = (TextView) v;
                        if (questionText.getText().equals("")) {
                            Toast.makeText(getActivity(), "Wait for others coming to room", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        if (view.getText().equals("")) {
                            Toast.makeText(getActivity(), "Can't submit empty answer.", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        //Sending Answer
                        String answer = answerText.getText().toString();
                        Game.sendMessage(new GameMessage(GameMessage.MESSAGE_ANSWER,
                                answer, Game.currentQBUser.getLogin()));
                        Toast.makeText(getActivity(), "Submitting succeeded", Toast.LENGTH_SHORT).show();
                        //TODO change to another fragment
                        GameActivity.voting = new VoteFragment();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container, GameActivity.voting);
                        //ft.addToBackStack(null);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.commit();
                    }else {
                        Toast.makeText(getActivity(),
                                "Answer can't be dragged to other area.",
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    //For LongClickListen
    private final class LongClickListener implements View.OnLongClickListener{

        @Override
        public boolean onLongClick(View view) {
            // Generate Tag
            ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());

            String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
            ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

            view.startDrag(data, // data to be dragged
                    shadowBuilder, // drag shadow
                    view, //  drag   View
                    0 //
            );
            return false;
        }
    }

    public void setQuestion(String question){
        this.question = question;
    }

    public void refresh(String answer){
        answerText.setText(answer);
    }
}
