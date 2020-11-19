package com.e.ftool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity {

    EditText nameEdit,noEdit,passEdit;
    TextView verifyNo;
    Button register;
    Spinner user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initialize();

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
}