
package com.example.firebasetest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;
    private TextView statusTextView, detailTextView;
    private EditText emailField, passwordField;
    private ProgressDialog progressDialog;
    private boolean fromChat;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(savedInstanceState == null){
            fromChat = false;
        }else{
            fromChat = savedInstanceState.getBoolean("fromChat");
        }

        statusTextView = findViewById(R.id.status);
        detailTextView = findViewById(R.id.detail);
        emailField = findViewById(R.id.fieldEmail);
        passwordField = findViewById(R.id.fieldPassword);

        findViewById(R.id.emailSignInButton).setOnClickListener(this);
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);
        findViewById(R.id.signOutButton).setOnClickListener(this);
        findViewById(R.id.verifyEmailButton).setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("fromChat", fromChat);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(auth.getCurrentUser());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_login,menu);
        this.menu = menu;
        // ob user eingelogt ist oder nicht
        if(auth.getCurrentUser() == null){
            menu.findItem(R.id.toChat).setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toChat:
                Log.d("From Chat", "onOptionsItemSelected: " + fromChat);
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    if (currentUser.isEmailVerified() && !fromChat) {
                        Intent intent = new Intent(this, SelectChatWithActivity.class);
                        fromChat = true;
                        startActivity(intent);
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUI(FirebaseUser currentUser) {
        hideProgressDialog();
        Log.d("From ", "updateUI: " + fromChat);

        if (currentUser != null) {
            if(currentUser.isEmailVerified() && !fromChat){
                Intent intent = new Intent(this, SelectChatWithActivity.class);
                fromChat=true;
                startActivity(intent);
            }else{
                fromChat=false;
            }

            statusTextView.setText(getString(R.string.emailpassword_status_fmt,
                    currentUser.getEmail(), currentUser.isEmailVerified()));
            if(menu != null)
                menu.findItem(R.id.toChat).setVisible(true);
            detailTextView.setText(getString(R.string.firebaseui_status_fmt, currentUser.getUid()));

            findViewById(R.id.emailPasswordButtons).setVisibility(View.GONE);
            findViewById(R.id.emailPasswordFields).setVisibility(View.GONE);
            findViewById(R.id.signedInButtons).setVisibility(View.VISIBLE);

            findViewById(R.id.verifyEmailButton).setEnabled(!currentUser.isEmailVerified());
        } else {

            statusTextView.setText(R.string.signed_out);
            detailTextView.setText(null);
            if(menu != null)
                menu.findItem(R.id.toChat).setVisible(false);
            findViewById(R.id.emailPasswordButtons).setVisibility(View.VISIBLE);
            findViewById(R.id.emailPasswordFields).setVisibility(View.VISIBLE);
            findViewById(R.id.signedInButtons).setVisibility(View.GONE);
        }
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(true);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.emailCreateAccountButton:
                creatAccount(emailField.getText().toString(), passwordField.getText().toString());
                break;
            case R.id.emailSignInButton:
                signIn(emailField.getText().toString(), passwordField.getText().toString());
                break;
            case R.id.signOutButton:
                signOut();
                break;
            case R.id.verifyEmailButton:
                sendEmailVerification();
                break;
        }

    }

    private void creatAccount(String email, String pwd) {
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        auth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                        }else{
                            Toast.makeText(LoginActivity.this, "Authentication faild", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        hideProgressDialog();
                    }
                });

    }

    private void signIn(String email, String pwd) {
        if (!validateForm()) {
            return;
        }
        fromChat = false;
        showProgressDialog();
        auth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication faild", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        if (!task.isSuccessful()) {
                            statusTextView.setText(R.string.auth_failed);
                        }
                        hideProgressDialog();
                    }
                });

    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Required");
            valid = false;
        } else {
            emailField.setError(null);
        }
        String password = passwordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Required");
            valid = false;
        } else {
            passwordField.setError(null);
        }
        return valid;
    }

    private void signOut() {
        auth.signOut();
        updateUI(null);

    }

    private void sendEmailVerification() {
        findViewById(R.id.verifyEmailButton).setEnabled(false);
        final FirebaseUser user = auth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        findViewById(R.id.verifyEmailButton).setEnabled(true);
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Verification mail sent to " +
                                    user.getEmail(), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "Faild to send verification email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
