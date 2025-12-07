package com.example.restyle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private DatabaseHelper databaseHelper;

    // Фиксированные данные администратора
    private static final String ADMIN_EMAIL = "admin@restyle.com";
    private static final String ADMIN_PASSWORD = "Admin123!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d("LoginActivity", "=== LOGIN ATTEMPT ===");
        Log.d("LoginActivity", "Email: " + email);
        Log.d("LoginActivity", "Password: " + password);

        if (email.isEmpty()) {
            etEmail.setError("Введите email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Введите пароль");
            etPassword.requestFocus();
            return;
        }

        // ПРОВЕРКА АДМИНИСТРАТОРА (фиксированные данные в коде)
        if (email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
            // Создаем объект администратора
            User adminUser = new User();
            adminUser.setId("admin_" + System.currentTimeMillis());
            adminUser.setName("Администратор");
            adminUser.setEmail(ADMIN_EMAIL);
            adminUser.setPassword(ADMIN_PASSWORD);
            adminUser.setAdmin(true);
            adminUser.setBlocked(false);
            adminUser.setCreatedAt(String.valueOf(System.currentTimeMillis()));
            adminUser.setRating(5.0);
            adminUser.setLocation("Админ-панель");

            Log.d("LoginActivity", "=== ADMIN LOGIN SUCCESS ===");
            Toast.makeText(this, "Вход выполнен как администратор", Toast.LENGTH_SHORT).show();

            // Переход в админ-панель
            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
            intent.putExtra("user", adminUser);
            startActivity(intent);
            finish();
            return;
        }

        // ОБЫЧНЫЙ ПОЛЬЗОВАТЕЛЬ
        User user = databaseHelper.authenticateUser(email, password);

        if (user != null) {
            // Гарантируем, что обычный пользователь не админ
            user.setAdmin(false);

            // Проверка блокировки
            if (user.isBlocked()) {
                Toast.makeText(this, "Ваш аккаунт заблокирован администратором", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d("LoginActivity", "=== USER LOGIN SUCCESS ===");
            Log.d("LoginActivity", "User: " + user.getName() + ", Email: " + user.getEmail());

            Toast.makeText(this, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user", user);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.d("LoginActivity", "=== LOGIN FAILED ===");
            Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
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