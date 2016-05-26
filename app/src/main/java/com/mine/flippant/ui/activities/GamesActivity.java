package com.mine.flippant.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mine.flippant.core.Game;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.mine.flippant.R;
import com.mine.flippant.core.ChatService;
import com.mine.flippant.ui.adapters.DialogsAdapter;

import java.util.ArrayList;
import java.util.List;

public class GamesActivity extends BaseActivity {

    private static final String TAG = GamesActivity.class.getSimpleName();
	private ActionBar actionBar;
    private ListView dialogsListView;
    private ProgressBar progressBar;

    public  static  void  start(Context context){
        Intent intent = new Intent(context, GamesActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogs_activity);
		actionBar = getSupportActionBar();
        //assert actionBar != null : "ActionBar is null";
        if (actionBar != null)
            actionBar.setTitle(actionBar.getTitle()+"(" +Game.currentQBUser.getEmail() + ")");
        dialogsListView = (ListView) findViewById(R.id.roomsList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Get dialogs if the session is active
        //
        if(isSessionActive()){
            getDialogs();
        }
    }

	@Override
    public void onBackPressed() {
        GameMenuActivity.start(GamesActivity.this);
        finish();
    }

    private void getDialogs(){
        progressBar.setVisibility(View.VISIBLE);

        // Get dialogs
        //
        ChatService.getInstance().getDialogs(new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle bundle) {
                progressBar.setVisibility(View.GONE);

                // build list view
                //
                buildListView(dialogs);
            }

            @Override
            public void onError(QBResponseException errors) {
                progressBar.setVisibility(View.GONE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(GamesActivity.this);
                dialog.setMessage("get dialogs errors: " + errors).create().show();
            }
        });
    }


    void buildListView(List<QBDialog> dialogs){
        // remove finished game from games list
        // TODO to speed up to get games from server, get filtered dialogs from the server.
        for (int i = dialogs.size(); i > 0; i--) {
            if (dialogs.get(i-1).getLastMessage().equals("[game:]<result id='HASFINISHED'/>")) {
                dialogs.remove(i - 1);
            }
        }
        final DialogsAdapter adapter = new DialogsAdapter(dialogs, GamesActivity.this);
        dialogsListView.setAdapter(adapter);

        // Choose Game
        //
        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBDialog selectedDialog = (QBDialog) adapter.getItem(position);

                Bundle bundle = new Bundle();
                bundle.putSerializable(GameActivity.EXTRA_DIALOG, selectedDialog);

                Game.masterID = selectedDialog.getName().substring(11);
                if (Game.currentQBUser.getLogin().equals(Game.masterID))
                    Game.isMaster = true;
                else {
                    Game.isMaster= false;
                }
                // Open game activity
                //
                GameActivity.start(GamesActivity.this, bundle);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //playServicesHelper.checkPlayServices();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.rooms, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_add) {
//
//            // go to New Game activity
//            //
//            Intent intent = new Intent(GamesActivity.this, NewGameActivity.class);
//            startActivity(intent);
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    // Our handler for received Intents.
//    //
//    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            // Get extra data included in the Intent
//            String message = intent.getStringExtra(Consts.EXTRA_MESSAGE);
//
//            Log.i(TAG, "Receiving event " + Consts.NEW_PUSH_EVENT + " with data: " + message);
//        }
//    };


    //
    // ApplicationSessionStateCallback
    //

    @Override
    public void onStartSessionRecreation() {

    }

    @Override
    public void onFinishSessionRecreation(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    getDialogs();
                }
            }
        });
    }
}
