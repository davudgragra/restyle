package com.example.restyle;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnResetPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Введите email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Введите корректный email");
            etEmail.requestFocus();
            return;
        }

        boolean userExists = databaseHelper.checkUserExists(email);

        if (userExists) {
            btnResetPassword.setEnabled(false);
            btnResetPassword.setText("Отправка...");

            // Имитация отправки письма (без реального сброса пароля)
            new android.os.Handler().postDelayed(() -> {
                Toast.makeText(this,
                        "Инструкции по восстановлению пароля отправлены на " + email,
                        Toast.LENGTH_LONG).show();

                // Возвращаемся на экран логина
                finish();
            }, 2000); // 2 секунды задержки для имитации отправки

        } else {
            Toast.makeText(this, "Пользователь с таким email не найден", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}