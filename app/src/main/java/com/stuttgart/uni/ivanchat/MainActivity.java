package com.stuttgart.uni.ivanchat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;
    private ViewPager mViewPager;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Ivan's Chat");

        // tabs
        mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(super.getSupportFragmentManager());

        // Set adapter for View Pager
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = this.mAuth.getCurrentUser();

        if (currentUser == null) {

            this.startStartActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case R.id.main_logout_button:
                this.mAuth.signOut();

                if (this.mAuth.getCurrentUser() == null) {

                    Toast.makeText(MainActivity.this, "You are signed out.", Toast.LENGTH_SHORT).show();
                    this.startStartActivity();

                }
                break;

            case R.id.main_settings_button:
                this.startSettingsAcitivty();
                break;

            case R.id.main_all_users_button:
                this.startUsersActivity();
                break;

            default:
                return true;
        }
        return true;
    }

    private void startUsersActivity() {

        Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
        super.startActivity(usersIntent);

    }

    private void startSettingsAcitivty() {

        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        super.startActivity(settingsIntent);

    }

    private void startStartActivity() {

        Intent startIntent = new Intent(this, StartActivity.class);
        super.startActivity(startIntent);
        super.finish();

    }
}
