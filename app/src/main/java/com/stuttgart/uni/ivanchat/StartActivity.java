package com.stuttgart.uni.ivanchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button mRegisterButton;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        this.mRegisterButton = (Button) findViewById(R.id.startRegisterButton);
        this.mLoginButton = (Button) findViewById(R.id.start_login_btn);

        this.mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              startRegisterActivity();

            }
        });

        this.mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startLoginActivity();
            }
        });
    }

    private void startLoginActivity() {

        Intent login_intent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(login_intent);

    }

    private void startRegisterActivity() {

        Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
        startActivity(registerIntent);

    }

    private void TestUpload() {
        int i = 1 + 1;
    }
}
