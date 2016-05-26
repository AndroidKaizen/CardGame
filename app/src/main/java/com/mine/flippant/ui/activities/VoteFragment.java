package com.mine.flippant.ui.activities;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mine.flippant.R;
import com.mine.flippant.core.Game;
import com.mine.flippant.core.GameMessage;

public class VoteFragment extends Fragment {

    private static final String IMAGEVIEW_TAG = "Draging Image";
    private static final String TAG = VoteFragment.class.getSimpleName();

    private Boolean dragged_flag = false;
    private View rootView;
    private TextView voteText;
    private TextView questionText;
    private LinearLayout src_in;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Game.state = Game.GAME_VOTE;
        if (savedInstanceState != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vote, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        rootView = getView();
        if (rootView != null) {
            int cardSize;
            questionText = (TextView) rootView.findViewById(R.id.questionCardText);
//            cardSize = questionText.getLayoutParams().height;
//            questionText.setLayoutParams(new ViewGroup.LayoutParams(cardSize / 5 * 4, cardSize));
            voteText = (TextView) rootView.findViewById(R.id.voteAreaText);
//            cardSize = voteText.getLayoutParams().height;
//            voteText.setLayoutParams(new ViewGroup.LayoutParams(cardSize / 5 * 4, cardSize));
            questionText.setText(Game.currentQuestion);
            src_in = (LinearLayout) rootView.findViewById(R.id.src_in);
            for(int i = 0; i < src_in.getChildCount(); i++){
                View child = src_in.getChildAt(i);
//                cardSize = child.getLayoutParams().height;
//                child.setLayoutParams(new ViewGroup.LayoutParams(cardSize / 5 * 4, cardSize));
                child.setTag(IMAGEVIEW_TAG);
                child.setOnLongClickListener(new LongClickListener()); // the child is its own drag handle
                child.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        TextView textView = (TextView) v;
                        String answer = textView.getText().toString();
                        ShowAnswerActivity.start(getActivity(), answer);
                    }
                });
            }
            rootView.findViewById(R.id.voteAreaText).setOnDragListener(new VoteDragListener());
            //TODO check whether following is necessary.
            rootView.findViewById(R.id.src_in).setOnDragListener(new VoteDragListener());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //savedInstanceState.putLong("workoutId", workoutId);
    }

    //Drag and drop for submit answer
    private class VoteDragListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    Log.d("DragClickListener", "ACTION_DROP");
                    if(dragged_flag == true)
                        break;
                    if (v == voteText) {
                        TextView view = (TextView) event.getLocalState();
                        if (view.getText() == "") {
                            Toast.makeText(getActivity(), "Can't vote empty answer", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        voteText.setText(view.getText());
                        dragged_flag = true;

                        //Sending Answer
                        switch (view.getId()) {
                            case R.id.answer1_textview:
                                Game.sendMessage(new GameMessage(GameMessage.MESSAGE_VOTE, "", Game.listAnswers.get(0).getUser_id()));
                                break;
                            case R.id.answer2_textview:
                                Game.sendMessage(new GameMessage(GameMessage.MESSAGE_VOTE, "", Game.listAnswers.get(1).getUser_id()));
                                break;
                            case R.id.answer3_textview:
                                Game.sendMessage(new GameMessage(GameMessage.MESSAGE_VOTE, "", Game.listAnswers.get(2).getUser_id()));
                                break;
                            case R.id.answer4_textview:
                                Game.sendMessage(new GameMessage(GameMessage.MESSAGE_VOTE, "", Game.listAnswers.get(3).getUser_id()));
                                break;
                        }
                        Toast.makeText(getActivity(), "voted successfully", Toast.LENGTH_SHORT).show();
                    } else{
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

    public void refresh(){
        try {
            for (int i = 0; i < Game.listAnswers.size(); i++) {
                String ans = Game.listAnswers.get(i).getBody();
                TextView ans_text = (TextView) src_in.getChildAt(i);
                ans_text.setText(ans);
            }
        } catch (RuntimeException e) {
            Log.d(TAG, e.toString());
        }
    }
}
