package com.example.restyle;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Получаем пользователя из Intent
        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser != null) {
            Toast.makeText(this, "Добро пожаловать, " + currentUser.getName() + "!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
            finish();
        }

        initViews();
        setupBottomNavigation();

        // Загружаем FeedFragment по умолчанию
        if (savedInstanceState == null) {
            loadFragment(new FeedFragment());
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();

                if (itemId == R.id.navigation_feed) {
                    selectedFragment = new FeedFragment();
                } else if (itemId == R.id.navigation_add) {
                    selectedFragment = new AddFragment();
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        try {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка загрузки фрагмента", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Метод для обновления ленты товаров из других фрагментов
    public void refreshFeed() {
        loadFragment(new FeedFragment());
        bottomNavigationView.setSelectedItemId(R.id.navigation_feed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}