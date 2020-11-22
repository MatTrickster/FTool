package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText noEdit, passEdit;
    Button login, register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize();

        noEdit.setText("9752003852");
        passEdit.setText("123456");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String no = noEdit.getText().toString();
                String pass = passEdit.getText().toString();

                if (no.length() != 10)
                    noEdit.setError("Incorrect Number");
                else if (pass.isEmpty())
                    passEdit.setError("Field is Empty");
                else
                    login(no, pass);

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

    }

    public void initialize() {

        noEdit = findViewById(R.id.number);
        passEdit = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);

    }

    public void login(final String no, final String pass) {

        final Boolean[] is = {false};

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customers/");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String tNo = snap.child("number").getValue().toString();
                    String tPass = snap.child("pass").getValue().toString();

                    if (tNo.equals(no)) {

                        is[0] = true;

                        if (tPass.equals(pass)) {

                            Intent intent = new Intent(LoginActivity.this, CustomerMapActivity.class);
                            intent.putExtra("uId", snap.getKey());
                            startActivity(intent);
                            finish();


                        } else {

                            Toast.makeText(LoginActivity.this, "Incorrect Password",
                                    Toast.LENGTH_SHORT).show();

                            break;
                        }
                    }
                }

                if (!is[0]) {

                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("drivers/");
                    ref1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot snap : snapshot.getChildren()) {

                                String tNo = snap.child("number").getValue().toString();
                                String tPass = snap.child("pass").getValue().toString();

                                if (tNo.equals(no)) {

                                    is[0] = true;

                                    if (tPass.equals(pass)) {

                                        Intent intent = new Intent(LoginActivity.this, DriverMapActivity.class);
                                        intent.putExtra("uId", snap.getKey());
                                        startActivity(intent);
                                        finish();


                                    } else {

                                        Toast.makeText(LoginActivity.this, "Incorrect Password",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                            }

                            if (!is[0]) {

                                Toast.makeText(LoginActivity.this, "No user found", Toast.LENGTH_SHORT).show();

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}