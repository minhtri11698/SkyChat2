package com.example.skychat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.skychat.Adapter.MainViewPagerAdapter;
import com.example.skychat.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference userref;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        Toolbar mainToolBar = findViewById(R.id.main_page_tool_bar);
        ViewPager mainViewPager = findViewById(R.id.mainviewpager);
        TabLayout mainTabLayout = findViewById(R.id.maintab);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userref = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid());
        }
        setSupportActionBar(mainToolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("SkyChat");
        MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        mainViewPager.setAdapter(mainViewPagerAdapter);
        mainViewPager.setCurrentItem(1);
        mainTabLayout.setupWithViewPager(mainViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        if(currentUser == null){
            moveToStart();
        } else {
            userref.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null){
            userref.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    public void moveToStart(){
        Intent logoutIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(logoutIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            moveToStart();
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
        }

        if (item.getItemId() == R.id.allusermenu){
            Intent toalluserIntent = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(toalluserIntent);
        }

        if (item.getItemId() == R.id.accsettings){
            Intent settingsIntent = new Intent(MainActivity.this, AccountSettingsActivity.class);
            startActivity(settingsIntent);
        }
        return true;
    }
}
