package com.mine.flippant.ui.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mine.flippant.core.Game;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mine.flippant.custom.helper.DataHolder;
import com.mine.flippant.models.Contact;
import com.mine.flippant.utils.Consts;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.mine.flippant.R;
import com.mine.flippant.core.ChatService;
import com.mine.flippant.ui.adapters.UsersAdapter;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class NewGameActivity extends BaseActivity{

    private static final int PAGE_SIZE = 1000;
	private Context context;

    private int listViewIndex;
    private int listViewTop;
    private int currentPage = 0;
    private List<QBUser> users = new ArrayList<>();

    private PullToRefreshListView usersList;
    private Button createChatButton;
    private ProgressBar progressBar;
    private UsersAdapter usersAdapter;
	private static ArrayList<Contact> contactlist;
    private ActionBar actionBar;
	public static StringifyArrayList usersEmails;


    public static void start(Context context) {
        Intent intent = new Intent(context, NewGameActivity.class);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_new);
		context = this;
        actionBar = getSupportActionBar();
        actionBar.setTitle(actionBar.getTitle() +"(" + Game.currentQBUser.getEmail()+")");

        /**
         *  Reading phone Contact Email List.
         * */
		contactlist = new ArrayList<Contact>();
        usersEmails = new StringifyArrayList();
        Uri uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;

        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };

        String[] selectionArgs = null;

        String sortOrder = ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE LOCALIZED DESC";


        ContentResolver cr = getContentResolver();
        Cursor contactCursor = cr.query(uri, projection, null, selectionArgs, sortOrder);

        if (contactCursor != null) {

            if (contactCursor.moveToFirst()) {
                do {
                    Contact acontact = new Contact();
                    acontact.setPhotoid(contactCursor.getLong(0));
                    acontact.setEmail(contactCursor.getString(1));
                    acontact.setName(contactCursor.getString(2));

                    contactlist.add(acontact);
                    usersEmails.add(contactCursor.getString(1));
                } while (contactCursor.moveToNext());
            }
        }


        Phone_Match_Users();

        usersList = (PullToRefreshListView) findViewById(R.id.usersList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        createChatButton = (Button) findViewById(R.id.createChatButton);
        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(NewGameActivity.this, "Waiting for Creating Room", Toast.LENGTH_LONG).show();
                getQuestionList();
            }
        });

        usersList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do work to refresh the list here.
                loadNextPage();
                listViewIndex = usersList.getRefreshableView().getFirstVisiblePosition();
                View v = usersList.getRefreshableView().getChildAt(0);
                listViewTop = (v == null) ? 0 : v.getTop();
            }
        });

        if (isSessionActive()) {
            loadNextPage();
        }
   }

	private void Phone_Match_Users(){
        //---- Reading email finish ---------
        QBUsers.getUsersByEmails(usersEmails, getQBPagedRequestBuilder(currentPage), new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> filterusers, Bundle params) {
                /**
                 *  Friend list matched with phone contact emails.
                 * */

                users = new ArrayList<>();
                if (filterusers.size() == 0) {
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                filterusers.remove(0);
                users.addAll(filterusers);
                // remove own name from the users list
                for (int i = 0; i < users.size(); i++) {
                    if (Game.currentQBUser.getLogin().equals(users.get(i).getLogin())) {
                        users.remove(i);
                    }
                }
                usersAdapter = new UsersAdapter(users, context);
                usersList.setAdapter(usersAdapter);
                usersList.onRefreshComplete();
                usersList.getRefreshableView().setSelectionFromTop(listViewIndex, listViewTop);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(QBResponseException errors) {
//                handleErrors(errors);
            }

        });
    }
	private void All_Users(){
        //---- Reading all users logged in this app ---------

        QBUsers.getUsers(getQBPagedRequestBuilder(currentPage), new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> allusers, Bundle params) {

                users = new ArrayList<>();
                users.addAll(allusers);
                for (int i = 0; i < users.size(); i++) {
                    if (Game.currentQBUser.getLogin().equals(users.get(i).getLogin())) {
                        users.remove(i);
                    }
                }
                usersAdapter = new UsersAdapter(users, context);
                usersList.setAdapter(usersAdapter);
                usersList.onRefreshComplete();
                usersList.getRefreshableView().setSelectionFromTop(listViewIndex, listViewTop);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(QBResponseException errors) {
//                handleErrors(errors);
            }

        });
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_match)
        {
            progressBar.setVisibility(View.VISIBLE);
            Phone_Match_Users();
        }
        else if(id == R.id.action_all)
        {
            progressBar.setVisibility(View.VISIBLE);
            All_Users();
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        Intent i = new Intent(NewGameActivity.this, GameMenuActivity.class);
        startActivity(i);
        finish();
    }


    public static QBPagedRequestBuilder getQBPagedRequestBuilder(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(PAGE_SIZE);

        return pagedRequestBuilder;
    }


   private String usersListToChatName() {
        String chatName = "";
        for (QBUser user : usersAdapter.getSelected()) {
            String prefix = chatName.equals("") ? "" : ", ";
            chatName = chatName + prefix + user.getLogin();
        }
        return chatName;
    }

    public void startSingleChat(QBDialog dialog) {
       AlertDialog.Builder msgdialog = new AlertDialog.Builder(NewGameActivity.this);
        msgdialog.setMessage("Can't create game room. Please select more players.").create().show();

    }

    private void startGroupChat(QBDialog dialog) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(GameActivity.EXTRA_DIALOG, dialog);
        Game.roundId = "1";
        GameActivity.start(this, bundle);
    }

    private void loadNextPage() {
        ++currentPage;

//        QBUsers.getUsers(getQBPagedRequestBuilder(currentPage), this);
//        QBUsers.getUsersByEmails(getQBPagedRequestBuilder(currentPage), context)
    }

    //For obtaining question cards from backend
    private void getQuestionList() {
        // Get all Question Cards
        //
        QBCustomObjects.getObjects(Consts.CLASS_NAME, new QBEntityCallback<ArrayList<QBCustomObject>>() {
            @Override
            public void onSuccess(ArrayList<QBCustomObject> qbCustomObjects, Bundle bundle) {

                if (DataHolder.getDataHolder().size() > 0) {
                    DataHolder.getDataHolder().clear();
                }

                if (qbCustomObjects != null && qbCustomObjects.size() != 0) {
                    for (QBCustomObject customObject : qbCustomObjects) {
                        DataHolder.getDataHolder().addQuestionToList(customObject);
                    }
                    Log.v("NewGameInformation", String.valueOf(qbCustomObjects.size()).concat(" questions"));
                }
                else {
                    Toast.makeText(getBaseContext(), "No card", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                startNewDialog();
            }

            @Override
            public void onError(QBResponseException strings) {
                Toast.makeText(getBaseContext(), "Check internet connection.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public static ArrayList<Integer> getUserIds(List<QBUser> users) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (QBUser user : users) {
            ids.add(user.getId());
        }
        return ids;
    }

    public void startNewDialog() {
        ChatService.getInstance().addDialogsUsers(usersAdapter.getSelected());

        // Create new group dialog
        //
        QBDialog dialogToCreate = new QBDialog();

        //original
        //dialogToCreate.setName(usersListToChatName());

        //modified
        QBUser user = Game.currentQBUser;                       // get CurrentUser
        DataHolder.getDataHolder().setSignInUserId(user.getId());
        Game.masterID = user.getLogin();                        // store dialog name to masterID
        Game.isMaster = true;
        dialogToCreate.setName("Created by ".concat(Game.masterID));
        //TODO Change Dialog Type later

        if (usersAdapter.getSelected().size() < 2 || usersAdapter.getSelected().size() > 4) {
            Toast.makeText(NewGameActivity.this, "Can't create a new game. \n" +
                    "There can be 2 to 4 opponent players in a game.", Toast.LENGTH_LONG).show();
            return;
            //dialogToCreate.setType(QBDialogType.PRIVATE);
        } else {
            Toast.makeText(NewGameActivity.this, "Waiting for Creating Room", Toast.LENGTH_LONG).show();
            dialogToCreate.setType(QBDialogType.GROUP);
        }
        dialogToCreate.setOccupantsIds(getUserIds(usersAdapter.getSelected()));


        QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate, new QBEntityCallback<QBDialog>() {
            @Override
            public void onSuccess(QBDialog dialog, Bundle args) {

//                if (dialog.getOccupants().size() < 3 || dialog.getOccupants().size() > 5) {
//                    Toast.makeText(NewGameActivity.this, "There must be 3 to 5 players.\n Please try again.", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Log.v("StartGroupChat", String.valueOf(DataHolder.getDataHolder().size()));
//                    startGroupChat(dialog);
//                }
                startGroupChat(dialog);
            }

            @Override
            public void onError(QBResponseException errors) {
                Toast.makeText(NewGameActivity.this, "Can't Create Game Room. \n Try Again.", Toast.LENGTH_LONG).show();
//                AlertDialog.Builder dialog = new AlertDialog.Builder(NewGameActivity.this);
//                dialog.setMessage("dialog creation errors: " + errors).create().show();
            }
        });
   }

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
//                    loadNextPage();
                }
            }
        });
    }
}
