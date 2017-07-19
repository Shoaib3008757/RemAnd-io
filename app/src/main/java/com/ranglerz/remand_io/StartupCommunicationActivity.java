package com.ranglerz.remand_io;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * The app's main entry point. Holds 2 fragments: {@link ChatSearchScreenFrag} and {@link ChatHistoryScreenFrag}.
 * The 1st fragment offers to scan for new chat groups and users. The 2nd offers to view chat history.
 */
public class StartupCommunicationActivity extends FragmentActivity implements ActionBar.TabListener
{
    private AlertDialog alertDialog =null;
    private OnClickListener AlertCheckBoxClickListener=null;  //used to handle check-box click events for a dialog

    SectionsForPagerAdapter sectionsForPagerAdapter;  //adapter for the tab view. Contains all the frags
    ViewPager pageViewer; //a layout widget in which each child aview is a separate page (a separate tab) in the layout.
    //both fragment will initialize these references when they're created:
    public ChatHistoryScreenFrag historyFrag = null;
    public ChatSearchScreenFrag searchFrag = null;

    boolean is_ServiceStarted = false;
    boolean was_WifiDialogShown = false;

    static int displayedFragIndex = 0;
    public static long C_RoomAccumulatingSerialNumber =0;
    public static String ID =null;
    public static String user_Name = "***";				  //setting a default user name
    static boolean is_ToNotifyOnNewMsg = false; 			  //defines if notifications should be shown on arrival of new messages
    static int RefreshPeriodInMs = 30000;				  //defines the peer refresh period

    private boolean is_RunForTheFirstTime =false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        initializingTabHandlerAndAdapter();

        if (!is_ServiceStarted && ChatSearchScreenFrag.m_Service ==null)
        {
            startService(new Intent(this, LocalService.class));
            is_ServiceStarted =true;
        }

        getPrefrences();  //get the shared prefs

        //Happens only when the app is run for the very 1st time on a device
        if (StartupCommunicationActivity.ID ==null)
        {
            user_Name = new String(Secure.getString(getContentResolver(), Secure.ANDROID_ID)); //get a unique id
            ID = new String(user_Name);
        }//if




    }//end of onCreate()

    @Override
    protected void onResume()
    {
        super.onResume();
        //check if this activity was launched by the b-cast receiver after a wifi shutdown
        boolean is_ToDisplayWifiDialog = getIntent()
                .getBooleanExtra(Constants.WIFI_BCAST_RCVR_WIFI_OFF_EVENT_INTENT_EXTRA_KEY, false);

        if (is_ToDisplayWifiDialog && !was_WifiDialogShown)
        {
            new EnableWifiDirectDialog().show(getSupportFragmentManager(),"MyDialog"); //show a dialog
            was_WifiDialogShown =true;
        }

        //if this app is run for the very 1st time, we want to launch the settings activity first.
        if (is_RunForTheFirstTime)
        {
            //launch the preferences activity
            startActivity(new Intent(this, QuickPrefsActivity.class));
            is_RunForTheFirstTime =false;
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        savePrefrences(); //save the preferences
    }//end of onPause()

    private void initializingTabHandlerAndAdapter()
    {

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the app.
        sectionsForPagerAdapter = new SectionsForPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        pageViewer = (ViewPager) findViewById(R.id.pager);
        pageViewer.setAdapter(sectionsForPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        pageViewer.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            //if a tab was changed by a swipe gesture
            public void onPageSelected(int position) {


                // actionBar.setSelectedNavigationItem(position); //update the tab bar to match the selected page


                displayedFragIndex = position;   //update the index of the currently displayed frag
                if (position == 1)  //if the view has moved to the history fragment:
                {
                    historyFrag.loadHistory(); //reload the history list view
                }
                invalidateOptionsMenu();
            }
        });

        // For each of the sections in the app, add a tab to the action bar.

    }//end of InitializingTabHandlerAndAdapter()

    /**
     * Used to modify menu item according to the app's state
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        //if the wifi-direct is disabled, we want to disable the chat room creation option
        menu.getItem(0).setEnabled(ChatSearchScreenFrag.mIsWifiDirectEnabled);

        //if this menu is opened when the chat search is active:
        if (displayedFragIndex ==0)
        {
            //hide the 'delete history option:
            menu.findItem(R.id.action_delete_all_history).setVisible(false);
        }
        else  //history frag is active:
        {
            //show the 'delete history option:
            menu.findItem(R.id.action_delete_all_history).setVisible(true);
            menu.findItem( R.id.clear_ignore_list).setVisible(false);
        }

        return true;
    }


    @SuppressLint("HandlerLeak")
    Handler FirstTimeMenuUpdater = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            StartupCommunicationActivity.this.invalidateOptionsMenu();
        }
    };


    /**
     * Called only once when the app starts
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen_menu, menu);

        FirstTimeMenuUpdater.sendEmptyMessageDelayed(0, 500);

        return true;
    }//end of onCreateOptionsMenu()


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings://setting was clicked
            {
                startActivity(new Intent(this, QuickPrefsActivity.class));
                break;
            }
            case R.id.action_create_new_chat_room: //exit app was clicked
            {

                alertDialog =CreatePublicChatCreationDialog();
                alertDialog.show();

                AlertCheckBoxClickListener= new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        AlertDialog dialog = StartupCommunicationActivity.this.alertDialog;
                        EditText ed = (EditText) dialog.findViewById(R.id.choosePassword);
                        boolean b= !ed.isEnabled();
                        ed.setEnabled(b);

                    }
                };

                CheckBox checkBox = (CheckBox) alertDialog.findViewById(R.id.checkBoxSetPassword);
                checkBox.setOnClickListener(AlertCheckBoxClickListener);
                break;
            }
            case R.id.clear_ignore_list: //exit app was clicked
            {
                if (searchFrag !=null)
                    searchFrag.ClearIgnoredUsersList();
                break;
            }
            case R.id.action_exit: //exit app was clicked
            {
                killService();
                break;
            }
            case R.id.action_delete_all_history: //delete all history was clicked
            {
                historyFrag.DeleteAllHistory();
                break;
            }
        }//switch

        return true;
    }//end of onOptionsItemSelected()


    @Override
    public void onTabSelected(ActionBar.Tab tab,FragmentTransaction fragmentTransaction)
    {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        pageViewer.setCurrentItem(tab.getPosition());
    }//end of onTabSelected()

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
    }

    /**
     * A FragmentPagerAdapter that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsForPagerAdapter extends FragmentPagerAdapter
    {

        public SectionsForPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            Fragment fragment=null; //will hold the relevant fragment to be returned

            switch (position)
            {
                case 0:
                    fragment = new ChatSearchScreenFrag();  //create a new chat search fragment
                    break;
                case 1:
                    fragment = new ChatHistoryScreenFrag();  //create a new history display fragment
                    break;
            }

            return fragment;
        }//end of getItem()

        @Override
        public int getCount()
        {
            return 2; 		// Show 2 total pages.
        }

        //Returns the title for each tab
        @Override
        public CharSequence getPageTitle(int position)
        {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.main_screen_tab1_title).toUpperCase(l);
                case 1:
                    return getString(R.string.main_screen_tab2_title).toUpperCase(l);
            }
            return null;
        }
    }//end of class



    /**
     * Called when the refresh button in the chat search fragment is clicked
     */
    public void onRefreshButtonClicked (View v)
    {
        searchFrag.onRefreshButtonClicked(v); //call the frag's method
    }//end of onRefreshButtonClicked()


    /**
     * Reads the saved preferences
     */
    protected void getPrefrences()
    {
        SharedPreferences prefs  = getPreferences(0);
        C_RoomAccumulatingSerialNumber = prefs.getLong(Constants.SHARED_PREF_CHAT_ROOM_SERIAL_NUM, 0);
        user_Name = prefs.getString(Constants.SHARED_PREF_USER_NAME, null);
        ID = prefs.getString(Constants.SHARED_PREF_UNIQUE_ID, null);
        is_ToNotifyOnNewMsg = prefs.getBoolean(Constants.SHARED_PREF_ENABLE_NOTIFICATION, false);
        RefreshPeriodInMs = prefs.getInt(Constants.SHARED_PREF_REFRESH_PERIOD, 10000);
        is_RunForTheFirstTime = prefs.getBoolean(Constants.SHARED_PREF_IS_FIRST_RUN, true);
    }//end of getPrefsencs(){

    /**
     * Saved the shared preferences
     */
    protected void savePrefrences()
    {
        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putLong(Constants.SHARED_PREF_CHAT_ROOM_SERIAL_NUM, C_RoomAccumulatingSerialNumber); //save to current SN
        editor.putString(Constants.SHARED_PREF_USER_NAME, user_Name);
        editor.putString(Constants.SHARED_PREF_UNIQUE_ID, ID);
        editor.putBoolean(Constants.SHARED_PREF_ENABLE_NOTIFICATION, is_ToNotifyOnNewMsg);
        editor.putInt(Constants.SHARED_PREF_REFRESH_PERIOD, RefreshPeriodInMs);
        editor.putBoolean(Constants.SHARED_PREF_IS_FIRST_RUN, false);
        editor.commit();
    }//end of savePrefrences()


    public void killService(){
        savePrefrences();
        searchFrag.kill();  //close the entire app (service and welcome socket)


        //we'de like to reset all static variables in our app:
        ChatActivity.mIsActive=false;
        ChatActivity.mMsgsWaitingForSendResult=null;
        ChatSearchScreenFrag.m_Service =null;
        ChatSearchScreenFrag.mIsWifiDirectEnabled=false;
        ChatSearchScreenFrag.mIsConnectedToGroup=false;
        ChatSearchScreenFrag.mManager = null;
        ChatSearchScreenFrag.mChannel = null;
        LocalService.mNotificationManager=null;

        //Indicates to the VM that it would be a good time to run the garbage collector
        System.gc();

        finish();         //close this activity
    }//killService


    private AlertDialog CreatePublicChatCreationDialog()
    {
        // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.public_chat_creation_dialog, null);
        return new AlertDialog.Builder(this)

                .setTitle("Create A New Room")
                .setView(textEntryView)
                .setIcon(R.drawable.settings_icon)

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isPassword=false;
                        String password="";
                        String roomName=null;

                        EditText ed = (EditText) alertDialog.findViewById(R.id.choosePassword);

                        //gets password if exists
                        isPassword= ed.isEnabled();
                        if(isPassword){password=ed.getText().toString();}

                        //gets rooms name
                        ed = (EditText) alertDialog.findViewById(R.id.chooseRoomsName);
                        roomName=ed.getText().toString();

                        //if the room's name is invalid:
                        if(roomName==null || roomName.length()<1){
                            // pop alert dialog and reload this dialog
                            new AlertDialog.Builder(StartupCommunicationActivity.this)
                                    .setTitle("Missing name error")
                                    .setMessage("A room must have a name")

                                            //yes button setter
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            alertDialog.show();}})//setPositive

                                    .setOnCancelListener(new OnCancelListener(){
                                        public void onCancel(DialogInterface dialog){
                                            alertDialog.show();}})

                                    .show();
                            //end of alert dialog
                        }//if

                        else{//there is a room name
                            //the room is ready to be created
                            //call the service and create a new public chat room
                            if (password.equalsIgnoreCase(""))
                                password=null;

                            ChatSearchScreenFrag.m_Service.CreateNewHostedPublicChatRoom(roomName,password);

                        }//else
                    }//onClick dialog listener


                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {}

                }).create();
    }//end of ShowPublicChatCreationDialog()

}//end of class