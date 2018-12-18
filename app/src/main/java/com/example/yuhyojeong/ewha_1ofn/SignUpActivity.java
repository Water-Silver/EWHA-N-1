package com.example.yuhyojeong.ewha_1ofn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class SignUpActivity extends AppCompatActivity {

    Button btnSignUp, btnCancel;
    EditText edtName, edtEmail, edtpassword, edtpassword_confirmed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btnSignUp = findViewById(R.id.signup_btn_signup);
        btnCancel = findViewById(R.id.signup_btn_cancel);

        edtName = findViewById(R.id.signup_edt_name);
        edtEmail = findViewById(R.id.signup_edt_email);
        edtpassword = findViewById(R.id.signup_edt_password);
        edtpassword_confirmed = findViewById(R.id.signup_edt_password_confirmed);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = edtName.getText().toString().trim();
                String password= edtpassword.getText().toString();
                String password_confirmed = edtpassword_confirmed.getText().toString();
                String fullname = edtEmail.getText().toString();

                if (!isValidData(user, password, password_confirmed, fullname)) {
                    return;
                }

            }
        });
    }

    private boolean isValidData(String login, String password, String confirm, String fullname) {

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            if (TextUtils.isEmpty(login)) {
                edtName.setError(getResources().getString(R.string.error_field_is_empty));
            }
            if (TextUtils.isEmpty(password)) {
                edtpassword.setError(getResources().getString(R.string.error_field_is_empty));
            }
            if (TextUtils.isEmpty(confirm)) {
                edtpassword_confirmed.setError(getResources().getString(R.string.error_field_is_empty));
            }
            if (TextUtils.isEmpty(fullname)) {
                edtEmail.setError(getResources().getString(R.string.error_field_is_empty));
            }
            return false;
        }

        if (!TextUtils.equals(password, confirm)) {
            edtpassword_confirmed.setError(getResources().getString(R.string.confirm_error));
            return false;
        }
        return true;
    }
}
