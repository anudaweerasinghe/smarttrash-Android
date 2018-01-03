package com.smarttrash.anuda.garbage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import Helpers.RestClient;
import butterknife.ButterKnife;
import butterknife.Bind;
import models.api_models.SignUp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @Bind(R.id.input_mobile)
    EditText _mobileText;
    @Bind(R.id.input_name)
    EditText _nameText;
    @Bind(R.id.btn_login)
    Button _loginButton;
    private String mobile;
    private String name;
    Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);


        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });



        SharedPreferences pref = getApplicationContext().getSharedPreferences("IdeaTrash Preferences", MODE_PRIVATE); // 0 - for private mode
        editor = pref.edit();

        if(pref.getBoolean("LogIn Status",false)){
            Intent intentNew = new Intent(LoginActivity.this, Dashboard.class);
            startActivity(intentNew);
        }else {

        }

    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        name=_nameText.getText().toString();
        mobile=_mobileText.getText().toString();

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();


        SignUp signUp = new SignUp();

        signUp.setName(name);

        signUp.setPhone(mobile);


        Call<Void> signUpCall=RestClient.garbageBinService.signUp(signUp);

        signUpCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code()==200){
                    onLoginSuccess();
                }else{
                    onLoginFailed();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                onLoginFailed();
            }
        });

//        Call<SignUp> signUpCall= RestClient.garbageBinService.signUp(signUp);
//
//
//        signUpCall.enqueue(new Callback<SignUp>() {
//            @Override
//            public void onResponse(Call<SignUp> call, Response<SignUp> response) {
//                // The network call was a success and we got a response
//                if(response.code()==200){
//                    onLoginSuccess();
//                }else{
//                    onLoginFailed();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<SignUp> call, Throwable t) {
//                // the network call was a failure
//                onLoginFailed();
//            }
//        });

        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed

                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);

    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_SIGNUP) {
//            if (resultCode == RESULT_OK) {
//
//                // TODO: Implement successful signup logic here
//                // By default we just finish the Activity and log them in automatically
//                this.finish();
//            }
//        }
//    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        editor.clear();
        editor.putBoolean("LogIn Status",true);
        editor.putString("Mobile",mobile);
        editor. putString("Name", name);
        editor.commit();
        Intent intentNew = new Intent(LoginActivity.this, Dashboard.class);
        startActivity(intentNew);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed. Please Try again", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

         mobile = _mobileText.getText().toString();
         name = _nameText.getText().toString();

        if (mobile.isEmpty() || mobile.length()!=9) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        return valid;
    }
}
