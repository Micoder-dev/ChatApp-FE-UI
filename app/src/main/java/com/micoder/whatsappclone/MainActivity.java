package com.micoder.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Toolbar mToolBar;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    private ChipNavigationBar chipNavigationBar;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private TextView drawerUserName,drawerUserStatus;
    private CircleImageView drawerProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("WhatsApp");

        chipNavigationBar = findViewById(R.id.bottom_nav_menu);
        chipNavigationBar.setItemSelected(R.id.bottom_nav_chats, true);


        drawerLayout=findViewById(R.id.drawerLayout);
        navigationView=findViewById(R.id.navigation_view);

        toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.start,R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(this);

        bottomMenu();

        checkNetwork();

        View header = navigationView.getHeaderView(0);
        drawerUserName = (TextView)header.findViewById(R.id.drawer_user_name);
        drawerUserStatus = (TextView)header.findViewById(R.id.drawer_user_status);
        drawerProfileImage = (CircleImageView)header.findViewById(R.id.drawer_profile_image);


        if (mAuth.getCurrentUser() != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ChatsFragment()).commit();

            RootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {

                        String userName = (String) dataSnapshot.child("name").getValue().toString();
                        String userStatus = (String) dataSnapshot.child("status").getValue().toString();
                        String userProfileImage = (String) dataSnapshot.child("image").getValue().toString();

                        drawerUserName.setText(userName);
                        drawerUserStatus.setText(userStatus);
                        Picasso.get().load(userProfileImage).placeholder(R.drawable.profile_image).into(drawerProfileImage);

                    }
                    else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                        String userName = (String) dataSnapshot.child("name").getValue().toString();
                        String userStatus = (String) dataSnapshot.child("status").getValue().toString();

                        drawerUserName.setText(userName);
                        drawerUserStatus.setText(userStatus);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Please set & update your info...", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void checkNetwork() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;

        if (connected==false) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Network");
            builder.setMessage("Please check your Internet connection");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog dialog = builder.show();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private void bottomMenu() {
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;

                switch (i) {
                    case R.id.bottom_nav_chats:
                        drawerClose();
                        fragment = new ChatsFragment();
                        break;
                    case R.id.bottom_nav_newFriends:
                        drawerClose();
                        fragment = new FindFriendsFragment();
                        break;
                    case R.id.bottom_nav_contacts:
                        drawerClose();
                        fragment = new ContactsFragment();
                        break;
                    case R.id.bottom_nav_requests:
                        drawerClose();
                        fragment = new RequestsFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            SendUserToLoginActivity();
        }else {

            updateUserStatus("online");

            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {

        RootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())) {

                }else {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        //nav drawer//
        if (toggle.onOptionsItemSelected(item)){
            return true;
        }//nav drawer complete//


        if (item.getItemId() == R.id.main_logout_option){
            updateUserStatus("offline");
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.main_settings_option){
            SendUserToSettingsActivityOptions();
        }

        return true;
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }
    private void SendUserToSettingsActivityOptions() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void updateUserStatus(String state) {

        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }


    //nav drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.drawer_profile:
                drawerClose();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.drawer_find_friends:
                drawerClose();
                chipNavigationBar.setItemSelected(R.id.bottom_nav_newFriends, true);
                break;
            case R.id.drawer_contacts:
                drawerClose();
                chipNavigationBar.setItemSelected(R.id.bottom_nav_contacts, true);
                break;
            case R.id.drawer_chat_requests:
                drawerClose();
                chipNavigationBar.setItemSelected(R.id.bottom_nav_requests, true);
                break;
            case R.id.drawer_info:
                Toast.makeText(this, "info", Toast.LENGTH_SHORT).show();
                break;
            case R.id.drawer_myprofile:
                drawerClose();
                Toast.makeText(MainActivity.this,"Loading...",Toast.LENGTH_SHORT).show();
                Intent myProfileIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://micoder-dev.github.io/Resume-Page/"));
                startActivity(myProfileIntent);
                break;
            case R.id.drawer_buymeacoffee:
                drawerClose();
                Toast.makeText(MainActivity.this,"Loading...",Toast.LENGTH_SHORT).show();
                Intent coffeeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/micoder"));
                startActivity(coffeeIntent);
                break;
            case R.id.drawer_github:
                drawerClose();
                Toast.makeText(MainActivity.this,"Loading...",Toast.LENGTH_SHORT).show();
                Intent githubIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Micoder-dev"));
                startActivity(githubIntent);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else
            super.onBackPressed();
    }

    public void drawerClose() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
}