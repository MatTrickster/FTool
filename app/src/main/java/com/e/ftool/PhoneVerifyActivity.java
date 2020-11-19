package com.e.ftool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneVerifyActivity extends AppCompatActivity {

    TextView no,timer;
    Button verify, resend;
    EditText code;
    String mVerificationId,sentCode;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verify);

        initialize();

        final String number = getIntent().getStringExtra("no");
        no.setText("Mobile Number : "+number);

        sendOTP(number);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String temp = code.getText().toString();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,temp);
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(PhoneVerifyActivity.this,"Phone Verification Successful",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            setResult(11,intent);
                            finish();

                        } else {
                            Toast.makeText(PhoneVerifyActivity.this,"Invalid Code",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendOTP(number);

            }
        });
    }

    public void initialize(){

        no = findViewById(R.id.number);
        timer = findViewById(R.id.timer);
        verify = findViewById(R.id.verify);
        resend = findViewById(R.id.resend);
        code = findViewById(R.id.code);

    }

    public void sendOTP(String number){

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                sentCode = credential.getSmsCode();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(PhoneVerifyActivity.this,"Invalid Number",Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(PhoneVerifyActivity.this,"Too Many Request",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Toast.makeText(PhoneVerifyActivity.this,"Code Sent",Toast.LENGTH_SHORT).show();
                startTimer(60);
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber("+91"+number)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    public void startTimer(int seconds) {
        resend.setEnabled(false);

        new CountDownTimer(seconds*1000, 1000) {

            public void onTick(long millisUntilFinished) {
                String secondsString = Long.toString(millisUntilFinished/1000);
                if (millisUntilFinished<10000) {
                    secondsString = "0"+secondsString;
                }
                timer.setText(" (0:"+ secondsString+")");
            }

            public void onFinish() {
                resend.setEnabled(true);
            }
        }.start();
    }

}