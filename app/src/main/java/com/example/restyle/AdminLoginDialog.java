package com.example.restyle;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AdminLoginDialog extends Dialog {
    public interface AdminLoginListener {
        void onAdminLogin(String email, String password);
    }

    private AdminLoginListener listener;
    private EditText etAdminEmail, etAdminPassword;
    private Button btnAdminLogin, btnCancel;

    public AdminLoginDialog(Context context, AdminLoginListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_admin_login);

        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        btnCancel = findViewById(R.id.btnCancel);

        btnAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etAdminEmail.getText().toString().trim();
                String password = etAdminPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getContext(), "Введите email и пароль", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (listener != null) {
                    listener.onAdminLogin(email, password);
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}