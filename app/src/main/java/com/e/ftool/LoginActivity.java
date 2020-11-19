package com.e.ftool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class LoginActivity extends AppCompatActivity {

    EditText noEdit, passEdit;
    Button login,register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String no = noEdit.getText().toString();
                String pass = passEdit.getText().toString();

                if(no.length() != 10)
                    noEdit.setError("Incorrect Number");
                else if(pass.isEmpty())
                    passEdit.setError("Field is Empty");
                else{

                }

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

    }

    public void initialize(){

        noEdit = findViewById(R.id.number);
        passEdit = findViewById(R.id.password);
        login = findViewById(R.id.register);
        register = findViewById(R.id.register);

    }
}