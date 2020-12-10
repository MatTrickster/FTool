package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    EditText nameEdit,noEdit,passEdit;
    TextView register;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Objects.requireNonNull(this.getSupportActionBar()).hide();

        initialize();

        register.setOnClickListener(view -> {

            String name = nameEdit.getText().toString();
            String no = noEdit.getText().toString();
            String pass = passEdit.getText().toString();

            if(name.isEmpty())
                nameEdit.setError("Field is Empty");
            else if(no.length() != 10)
                noEdit.setError("Incorrect Number");
            else if(pass.length() < 6)
                passEdit.setError("Password too short");
            else
                checkNumberInDB(no);

        });
    }

    public void initialize(){

        nameEdit = findViewById(R.id.name);
        noEdit = findViewById(R.id.number);
        passEdit = findViewById(R.id.password);
        register = findViewById(R.id.register);
        spinner = findViewById(R.id.users);

    }

    public void checkNumberInDB(final String no){

        final boolean[] isInDB = {false};

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customers/");

        ref.orderByChild("number").equalTo(no).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot snap : snapshot.getChildren()){
                    if(snap.child("number").getValue().toString().equals(no)){

                        Toast.makeText(RegisterActivity.this,"Number Already Used",
                                Toast.LENGTH_SHORT).show();

                        isInDB[0] = true;
                        break;
                    }
                }

                if(!isInDB[0]){

                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("drivers/");
                    ref1.orderByChild("number").equalTo(no).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for(DataSnapshot snap : snapshot.getChildren()){
                                if(snap.child("number").getValue().toString().equals(no)){

                                    Toast.makeText(RegisterActivity.this,"Number Already Used",
                                            Toast.LENGTH_SHORT).show();

                                    isInDB[0] = true;
                                    break;
                                }
                            }

                            if(!isInDB[0])
                                verifyNumber(no);

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

    public void verifyNumber(String no){
        Intent intent = new Intent(RegisterActivity.this,PhoneVerifyActivity.class);
        intent.putExtra("no",no);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 1 && resultCode == 11){

            User user = new User(nameEdit.getText().toString(), noEdit.getText().toString(),
                            passEdit.getText().toString());

            DatabaseReference reference;

            if(spinner.getSelectedItemPosition() == 0){
                reference = FirebaseDatabase.getInstance().getReference("customers/");
            }else{
                reference = FirebaseDatabase.getInstance().getReference("drivers/");
            }

            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);

            if(spinner.getSelectedItemPosition() == 1)
                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("rating").setValue("0.0");

            Toast.makeText(RegisterActivity.this,"Sign Up Successful",Toast.LENGTH_SHORT).show();
            finish();

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}