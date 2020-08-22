package com.example.namastesoicalnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity
{
    private EditText userEmail, userPassword, userConfirmPassword;
    private Button createAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        loadingBar = new ProgressDialog(this);

        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_Password);
        userConfirmPassword = findViewById(R.id.register_confirm_Password);
        createAccount = findViewById(R.id.register_create_account_btn);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CreateNewAccount();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity()
    {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void CreateNewAccount()
    {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPassword = userConfirmPassword.getText().toString();

        if(email.isEmpty())
        {
            Toast.makeText(this, "Please enter the email", Toast.LENGTH_SHORT).show();
        }
        else if(password.isEmpty())
        {
            Toast.makeText(this, "Please enter the password", Toast.LENGTH_SHORT).show();
        }
        else if(confirmPassword.isEmpty())
        {
            Toast.makeText(this, "Please enter the confirm password", Toast.LENGTH_SHORT).show();
        }
        else if(!confirmPassword.equals(password))
        {
            Toast.makeText(this, "password and confirm password is don't match", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Step 1 precessing...");
            loadingBar.setMessage("Please wait, while we are authorized information");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        loadingBar.dismiss();
                        Toast.makeText(RegisterActivity.this, "Step 1 completed sucessfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else
                    {
                        loadingBar.dismiss();
                        Toast.makeText(RegisterActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }
}