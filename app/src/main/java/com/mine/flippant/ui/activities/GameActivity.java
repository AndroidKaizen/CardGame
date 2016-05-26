package com.mine.flippant.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mine.flippant.R;
import com.mine.flippant.core.Chat;
import com.mine.flippant.core.ChatService;
import com.mine.flippant.core.Game;
import com.mine.flippant.core.GameMessage;
import com.mine.flippant.core.GroupChatImpl;
import com.mine.flippant.core.MessageResult;
import com.mine.flippant.core.PrivateChatImpl;
import com.mine.flippant.ui.adapters.ChatAdapter;
import com.mine.flippant.ui.views.RoundedImageView;
import com.mine.flippant.utils.Consts;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GameActivity extends BaseActivity {

    public static final String EXTRA_DIALOG = "dialog";
    public static TextView unread_count_text;
    public static VoteFragment voting;
    public static QuesAnsFragment questioning;
    protected android.support.v7.app.ActionBar actionBar;

    private static final String TAG = GameActivity.class.getSimpleName();
    private final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    private ProgressBar progressBar;
    private LinearLayout users_layout;
    private ListView messagesContainer;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ImageView im_nav_open;
    private ImageView im_nav_close;
    private Button sendButton;
    private EditText messageEditText;
    private QBDialog dialog;
    private Chat chat;
    private ChatAdapter adapter;
    private RelativeLayout container;
    private boolean needStartGame = false;

    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtras(bundle);
        Game.listAnswers = new ArrayList<GameMessage>();
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        actionBar.setTitle(actionBar.getTitle() +"(" + Game.currentQBUser.getEmail()+")");

        // init View
        initViews();

        // Init chat if the session is active
        //
        if (isSessionActive()) {
            initChat();
        }

        if (savedInstanceState == null)
            needStartGame = true;

        ChatService.getInstance().addConnectionListener(chatConnectionListener);
        SystemClock.sleep(3000);
        if (needStartGame) Game.startGame();
    }

    @Override
    public void onBackPressed() {
        GamesActivity.start(this);
        Game.joinedCount = 0;
        finish();
    }

    public void gameFinishOnclickListener(View view) {
        onBackPressed();
    }

//    public void onSubmitAnswer(View view) {
//        String answer = answerCard.getText().toString();
//        Game.sendMessage(new GameMessage(GameMessage.MESSAGE_ANSWER,
//                answer, Game.currentQBUser.getLogin()));
//        Button button = (Button) findViewById(R.id.btn_answer);
//        button.setClickable(false);
//        TextView answerText = (TextView) findViewById(R.id.answerCardText);
//        answerText.setEnabled(false);
//    }

    private void sendChatMessage(String messageText) {
        QBChatMessage chatMessage = new QBChatMessage();

        chatMessage.setBody(messageText);
        // chatMessage.setBody(Game.handleMessageText(messageText));
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(new Date().getTime() / 1000);

        try {
            chat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            Log.e(TAG, "failed to send a message", e);
        } catch (SmackException sme) {
            Log.e(TAG, "failed to send a message", sme);
        }

        messageEditText.setText("");

        if (dialog.getType() == QBDialogType.PRIVATE) {
            showMessage(chatMessage);
        }
    }

    private void initViews() {
        // Get Intent
        Intent intent = getIntent();
        dialog = (QBDialog) intent.getSerializableExtra(EXTRA_DIALOG);
        Game.currentDialog = dialog;
        Game.playerCount = dialog.getOccupants().size();

        // Game Frame
//        answer_text1 =  (TextView) findViewById(R.id.answer1_textview);
//        answer_text2 =  (TextView) findViewById(R.id.answer2_textview);
//        answer_text3 =  (TextView) findViewById(R.id.answer3_textview);
//        answer_text4 =  (TextView) findViewById(R.id.answer4_textview);
        unread_count_text = (TextView) findViewById(R.id.unread_msg_count);
        im_nav_open =   (ImageView) findViewById(R.id.btn_chat_nav);
        im_nav_close =  (ImageView) findViewById(R.id.btn_nav_close);
//        answerButton =  (Button) findViewById(R.id.btn_answer);
//        answerCard =    (TextView) findViewById(R.id.answerCardText);
        users_layout = (LinearLayout)findViewById(R.id.linear_avatars);

        //Get room users login id
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        List<Integer> usersIDs = new ArrayList<>();

        for(int i = 0; i < dialog.getOccupants().size(); i++){
            usersIDs.add(dialog.getOccupants().get(i));
        }
        requestBuilder.setPage(1);
        requestBuilder.setPerPage(usersIDs.size());
        QBUsers.getUsersByIDs(usersIDs, requestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                // Save users
                //
                Game.gameUsers = users;
                for (int i = 5; i > dialog.getOccupants().size(); i--){
                    users_layout.removeView(users_layout.getChildAt(i - 1));
                }
                for (int i = 0; i < dialog.getOccupants().size(); i++){
                    LinearLayout user = (LinearLayout) users_layout.getChildAt(i);
                    TextView name = (TextView) user.getChildAt(2);
                    name.setText(users.get(i).getLogin());
                    if(Game.currentQBUser.getLogin().equals(users.get(i).getLogin()) ){
                        RoundedImageView roundedImageView = (RoundedImageView)user.getChildAt(1);

                        if(Game.currentQBUser.getWebsite() !=null ){
                            try {
                                ImageLoader.getInstance().displayImage(Game.currentQBUser.getWebsite(), roundedImageView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
                            }catch (Error error)
                            {

                            }
                        }

                    }
                }


            }

            @Override
            public void onError(QBResponseException errors) {
                Toast.makeText(GameActivity.this, "Cannot get game players", Toast.LENGTH_LONG).show();
            }

        });

        //Drag
//        src_in = (LinearLayout)findViewById(R.id.src_in);
//        for(int i = 0; i < src_in.getChildCount(); i++){
//            View child = src_in.getChildAt(i);
//            child.setTag(IMAGEVIEW_TAG);
//            child.setOnLongClickListener(new LongClickListener()); // the child is its own drag handle
//        }

        // Users Layout
        // TODO check necessity of following

//        for (int i = 0; i < Game.playerCount; i++){
//            LinearLayout user = (LinearLayout) users_layout.getChildAt(i);
//            TextView score = (TextView) user.getChildAt(0);
//            TextView name = (TextView) user.getChildAt(2);
//            score.setText("0");
//            try {
//                QBUsers.getUser(dialog.getOccupants().get(i));
//                if (qbUser != null) {
//                    String user_name = qbUser.getLogin();
//                    name.setText(user_name);
//                }
//            } catch (QBResponseException e) {
//                e.printStackTrace();
//            }
//        }



//        for(int i = Game.playerCount; i < users_layout.getChildCount(); i++){
//            View child = users_layout.getChildAt(i);
//            child.setTag("USER ".concat(String.valueOf(i)));
//            child.setOnLongClickListener(new LongClickListener()); // the child is its own drag handle
//        }

//        findViewById(R.id.dest_in).setOnDragListener(new DragListener());
//        findViewById(R.id.src_in).setOnDragListener(new DragListener());

        // Chat Frame
        container = (RelativeLayout) findViewById(R.id.container);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        sendButton = (Button) findViewById(R.id.chatSendButton);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer) {
            //Called when a drawer has settled in a completely closed state
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                Log.d(TAG, "Chat folded");
                Game.chat_opened = false;
                Game.msg_read = Game.msg_count;
                invalidateOptionsMenu();
            }

            //Called when a drawer has settled in a completely open state.
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.d(TAG, "Chat opened");
                Game.chat_opened = true;
                unread_count_text.setText("0");
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        im_nav_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    drawerLayout.closeDrawer(Gravity.RIGHT);
                } else {
                    drawerLayout.openDrawer(Gravity.RIGHT);
                }
            }
        });
        im_nav_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    drawerLayout.closeDrawer(Gravity.RIGHT);
                } else {
                    drawerLayout.openDrawer(Gravity.RIGHT);
                }
            }
        });

        // Send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }
                sendChatMessage(messageText);

            }
        });

        // Show question and answer fragment
        questioning = new QuesAnsFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, questioning);
        //ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    public void showMessage(QBChatMessage message) {
        if (adapter == null) return;

        adapter.add(message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                scrollDown();
            }
        });
    }

    private void initChat() {

        if (dialog.getType() == QBDialogType.GROUP) {
            chat = new GroupChatImpl(this);
            Log.d(TAG, "chat = GroupChatImpl()");

            // Join group chat
            //
            progressBar.setVisibility(View.VISIBLE);
            //
            joinGroupChat();

        } else if (dialog.getType() == QBDialogType.PRIVATE) {
            Integer opponentID = ChatService.getInstance().getOpponentIDForPrivateDialog(dialog);

            chat = new PrivateChatImpl(this, opponentID);

            // Load CHat history
            //
            loadChatHistory();
        }

        Game.setChat(chat);
    }

    private void joinGroupChat() {
        ((GroupChatImpl) chat).joinGroupChat(dialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                Log.d(TAG, "joinGroupChat: onSuccess");
                // Load Chat history
                //
                loadChatHistory();
            }

            @Override
            public void onError(QBResponseException list) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GameActivity.this);
                dialog.setMessage("error when join group chat: " + list.toString()).create().show();
            }
        });
    }

    private void loadChatHistory() {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setLimit(100);
        customObjectRequestBuilder.sortDesc("date_sent");

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {

                adapter = new ChatAdapter(GameActivity.this, new ArrayList<QBChatMessage>());
                messagesContainer.setAdapter(adapter);

                for (int i = messages.size() - 1; i >= 0; --i) {
                    QBChatMessage msg = messages.get(i);
                    showMessage(msg);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(QBResponseException errors) {
                if (!GameActivity.this.isFinishing()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(GameActivity.this);
                    dialog.setMessage("load chat history errors: " + errors).create().show();
                }
            }
        });
    }

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


    ConnectionListener chatConnectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            Log.i(TAG, "connected");
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean authenticated) {
            Log.i(TAG, "authenticated");
        }

        @Override
        public void connectionClosed() {
            Log.i(TAG, "connectionClosed");
        }

        @Override
        public void connectionClosedOnError(final Exception e) {
            Log.i(TAG, "connectionClosedOnError: " + e.getLocalizedMessage());

            // leave active room
            //
            if (dialog.getType() == QBDialogType.GROUP) {
                GameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((GroupChatImpl) chat).leave();
                    }
                });
            }
        }

        @Override
        public void reconnectingIn(final int seconds) {
            if (seconds % 5 == 0) {
                Log.i(TAG, "reconnectingIn: " + seconds);
            }
        }

        @Override
        public void reconnectionSuccessful() {
            Log.i(TAG, "reconnectionSuccessful");

            // Join active room
            //
            if (dialog.getType() == QBDialogType.GROUP) {
                GameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        joinGroupChat();
                    }
                });
            }
        }

        @Override
        public void reconnectionFailed(final Exception error) {
            Log.i(TAG, "reconnectionFailed: " + error.getLocalizedMessage());
        }
    };


    //
    // ApplicationSessionStateCallback
    //
    @Override
    public void onStartSessionRecreation() {

    }

    @Override
    public void onFinishSessionRecreation(final boolean success) {
    }

    public void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                switch(MessageResult.messageType) {
                    case GameMessage.MESSAGE_HELLO:
                        for (int i = 0; i < dialog.getOccupants().size(); i++){
                            LinearLayout user = (LinearLayout) users_layout.getChildAt(i);
//                            TextView name = (TextView) user.getChildAt(2);
//                            name.setText(Game.gameUsers.get(i).getFullName());
                            if(MessageResult.userId.equals(Game.gameUsers.get(i).getId().toString()) ){
                                RoundedImageView roundedImageView = (RoundedImageView)user.getChildAt(1);

                                if(Game.gameUsers.get(i).getWebsite() !=null ){
                                    try {
                                        ImageLoader.getInstance().displayImage(Game.gameUsers.get(i).getWebsite(), roundedImageView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
                                    }catch (Error error)
                                    {

                                    }
                                }

                            }
                        }
                        break;
                    case GameMessage.MESSAGE_CARD:
                        questioning = new QuesAnsFragment();
                        Game.currentQuestion = MessageResult.questionCard;
                        questioning.setQuestion(MessageResult.questionCard);
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container, questioning);
                        //ft.addToBackStack(null);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.commit();
                        break;

                    case GameMessage.MESSAGE_ANSWER:
                        if (Game.state != Game.GAME_VOTE) break;
                        voting.refresh();
                        break;

                    case GameMessage.MESSAGE_WIN:
                        Toast.makeText(GameActivity.this, MessageResult.toast_msg, Toast.LENGTH_SHORT).show();
                        // TODO updating score of players
                        int i = 0 ;
                        Iterator<Map.Entry<String, Integer>> points = Game.listPoint.entrySet().iterator();
                        try {
                            while (points.hasNext()) {
                                Map.Entry<String, Integer> point = points.next();
                                for (i = 0; i < users_layout.getChildCount(); i++){
                                    LinearLayout user = (LinearLayout) users_layout.getChildAt(i);
                                    TextView name = (TextView) user.getChildAt(2);
                                    TextView score = (TextView) user.getChildAt(0);
                                    if (point.getKey().equals(name.getText()))
                                        score.setText(String.valueOf(point.getValue()));
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        break;

                    case GameMessage.MESSAGE_RESULT:
                        Toast.makeText(GameActivity.this, MessageResult.toast_msg +
                                "\n The game will be closed soon.", Toast.LENGTH_LONG).show();
//                        QBChatMessage chatMessage = new QBChatMessage();
//                        chatMessage.setBody(Game.LAST_MESSAGE);
//                        chatMessage.setProperty(Game.PROPERTY_SAVE_TO_HISTORY, "1");
//                        chatMessage.setDateSent(new Date().getTime() / 1000);
//                        try {
//                            chat.sendMessage(chatMessage);
//                        } catch (XMPPException e) {
//                            e.printStackTrace();
//                        } catch (SmackException.NotConnectedException e) {
//                            e.printStackTrace();
//                        }
                        SystemClock.sleep(10000);
                        GameMenuActivity.start(GameActivity.this);
                        Game.msg_count = 0;
                        Game.msg_read = 0;
                        Game.joinedCount = 0;
                        finish();
                        break;
                }
            }
        });
    }

    public void sendPendingMessages(MessageResult result) {
        if (result.sendList.size() == 0)
            return;

        AsyncTask<MessageResult, Void, Boolean> messageSender = new AsyncTask<MessageResult, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(MessageResult... messageResult) {
//                SystemClock.sleep(500);
                for (GameMessage gameMessage: messageResult[0].sendList) {
                    QBChatMessage chatMessage = new QBChatMessage();

                    chatMessage.setBody(gameMessage.getBody());
                    chatMessage.setProperty(Game.PROPERTY_SAVE_TO_HISTORY, "1");
                    chatMessage.setDateSent(new Date().getTime() / 1000);

                    try {
                        Log.d(TAG, "sendMessage: " + chatMessage.toString());
                        chat.sendMessage(chatMessage);
                    } catch (XMPPException e) {
                        Log.e(TAG, "failed to send a message", e);
                    } catch (SmackException sme) {
                        Log.e(TAG, "failed to send a message", sme);
                    }
                }

                adapter.notifyDataSetChanged();
                scrollDown();

                return true;
            }

            protected void onPostExecute(Boolean success) {
            }

        };

        messageSender.execute(result, null);
    }

    //For LongClickListen
//    private final class LongClickListener implements
//            View.OnLongClickListener{
//
//        @Override
//        public boolean onLongClick(View view) {
//            // Generate Tag
//            ClipData.Item item = new ClipData.Item(
//                    (CharSequence) view.getTag());
//
//            String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
//            ClipData data = new ClipData(view.getTag().toString(),
//                    mimeTypes, item);
//            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
//                    view);
//
//            view.startDrag(data, // data to be dragged
//                    shadowBuilder, // drag shadow
//                    view, //  drag   View
//                    0 //
//            );
//
//
////          view.setVisibility(View.INVISIBLE);
//            return false;
//        }
//    }

    //Drag and drop for vote
//    private class DragListener implements View.OnDragListener {
//
////      Drawable normalShape = getResources().getDrawable(
////              R.drawable.normal_shape);
////      Drawable targetShape = getResources().getDrawable(
////              R.drawable.target_shape);
//
//        public boolean onDrag(View v, DragEvent event) {
//
//            switch (event.getAction()) {
//                case DragEvent.ACTION_DRAG_STARTED:
//                    Log.d("DragClickListener", "ACTION_DRAG_STARTED");
//                    break;
//                case DragEvent.ACTION_DRAG_ENTERED:
//                    Log.d("DragClickListener", "ACTION_DRAG_ENTERED");
////                        v.setBackground(targetShape);
//                    break;
//                case DragEvent.ACTION_DRAG_EXITED:
//                    Log.d("DragClickListener", "ACTION_DRAG_EXITED");
////                        v.setBackground(normalShape);
//                    break;
//                case DragEvent.ACTION_DROP:
//                    Log.d("DragClickListener", "ACTION_DROP");
//                    if(dragged_flag == true)
//                        break;
//                    if (v == findViewById(R.id.dest_in)) {
//                        TextView view = (TextView) event.getLocalState();
//                        TextView vote_text = (TextView) findViewById(R.id.voteAreaText);
//                        if (view.getText() == "") break;
//                        vote_text.setText(view.getText());
//                        dragged_flag = true;
//
//                        //Sending Answer
//                        switch (view.getId()) {
//                            case R.id.answer1_textview:
//                                Game.sendMessage(new GameMessage(GameMessage.MESSAGE_VOTE, "", Game.listAnswers.get(0).getUser_id()));
//                                break;
//                            case R.id.answer2_textview:
//                                Game.sendMessage(new GameMessage(GameMessage.MESSAGE_VOTE, "", Game.listAnswers.get(1).getUser_id()));
//                                break;
//                            case R.id.answer3_textview:
//                                Game.sendMessage(new GameMessage(GameMessage.MESSAGE_VOTE, "", Game.listAnswers.get(2).getUser_id()));
//                                break;
//                            case R.id.answer4_textview:
//                                Game.sendMessage(new GameMessage(GameMessage.MESSAGE_VOTE, "", Game.listAnswers.get(3).getUser_id()));
//                                break;
//                        }
//                        Toast.makeText(GameActivity.this, "voted successfully", Toast.LENGTH_SHORT).show();
//
//                    }else {
//                        View view = (View) event.getLocalState();
//                        view.setVisibility(View.VISIBLE);
//                        Context context = getApplicationContext();
//                        Toast.makeText(context,
//                                "Image can't be drag other area.",
//                                Toast.LENGTH_LONG).show();
//                        break;
//                    }
//                    break;
//                case DragEvent.ACTION_DRAG_ENDED:
//                    Log.d("DragClickListener", "ACTION_DRAG_ENDED");
////                        v.setBackground(normalShape); // go back to normal shape
//                default:
//                    break;
//            }
//            return true;
//        }
//    }



    //Checking lifecycle
//    @Override
//    protected void onStart(){
//        super.onStart();
//        Log.i(MY_TAG, "onStart");
//    }
//
//    @Override
//    protected void onResume(){
//        super.onResume();
//        Log.i(MY_TAG, "onResume");
//    }
//
//    @Override
//    protected void onPause(){
//        super.onPause();
//        Log.i(MY_TAG, "onPause");
//    }
//
//    @Override
//    protected void onStop(){
//        super.onStop();
//        Log.i(MY_TAG, "onStop");
//    }
//
//    @Override
//    protected void onDestroy(){
//        super.onDestroy();
//        Log.i(MY_TAG, "onDestroy");
//    }
//
//    @Override
//    protected void onRestart(){
//        super.onRestart();
//        Log.i(MY_TAG, "onRestart");
//    }
}
