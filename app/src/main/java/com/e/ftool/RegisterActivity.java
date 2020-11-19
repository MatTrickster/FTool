package com.e.ftool;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText nameEdit,noEdit,passEdit;
    TextView verifyNo;
    Button register;
    Spinner user;
    Boolean isPhoneVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initialize();

        verifyNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String no = noEdit.getText().toString();

                if(no.length() != 10)
                    noEdit.setError("Invalid Number");
                else{
                    Intent intent = new Intent(RegisterActivity.this,PhoneVerifyActivity.class);
                    intent.putExtra("no",no);
                    startActivityForResult(intent,1);
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = nameEdit.getText().toString();
                String no = noEdit.getText().toString();
                String pass = passEdit.getText().toString();

                if(name.isEmpty())
                    nameEdit.setError("Field is Empty");
                else if(no.length() != 10)
                    noEdit.setError("Incorrect Number");
                else if(pass.length() < 6)
                    passEdit.setError("Password too short");
                else if(!isPhoneVerified){
                    Toast.makeText(RegisterActivity.this, "Phone number not verified",
                            Toast.LENGTH_SHORT).show();
                }
                else{



                }

            }
        });
    }

    public void initialize(){

        nameEdit = findViewById(R.id.name);
        noEdit = findViewById(R.id.number);
        passEdit = findViewById(R.id.password);
        verifyNo = findViewById(R.id.verify);
        register = findViewById(R.id.register);
        user = findViewById(R.id.users);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 1 && resultCode == 11){
            isPhoneVerified = true;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}