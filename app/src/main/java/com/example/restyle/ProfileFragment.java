package com.example.restyle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfileFragment extends Fragment {

    private ImageView ivUserAvatar;
    private TextView tvUserName, tvUserEmail, tvUserLocation, tvUserRating;
    private RecyclerView recyclerViewUserProducts;
    private Button btnLogout, btnSettings, btnChats;
    private ProductAdapter productAdapter;
    private DatabaseHelper databaseHelper;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Получаем текущего пользователя из MainActivity
        if (getActivity() != null) {
            currentUser = (User) getActivity().getIntent().getSerializableExtra("user");
            if (currentUser != null) {
                Log.d("ProfileFragment", "User loaded: " + currentUser.getName() +
                        ", Avatar: " + currentUser.getAvatarUrl());
            } else {
                Log.e("ProfileFragment", "No user found in intent");
            }
        }

        databaseHelper = new DatabaseHelper(requireContext());
        initViews(view);
        loadUserData();
        loadUserProducts();

        return view;
    }

    private void initViews(View view) {
        // Находим все View элементы
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserLocation = view.findViewById(R.id.tvUserLocation);
        tvUserRating = view.findViewById(R.id.tvUserRating);
        recyclerViewUserProducts = view.findViewById(R.id.recyclerViewUserProducts);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnChats = view.findViewById(R.id.btnChats);

        // Настраиваем RecyclerView
        recyclerViewUserProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Обработчики кнопок
        btnLogout.setOnClickListener(v -> logout());
        btnSettings.setOnClickListener(v -> openSettings());
        btnChats.setOnClickListener(v -> openChats());
    }

    private void loadUserData() {
        if (currentUser != null) {
            try {
                // Загружаем аватар пользователя
                Log.d("ProfileFragment", "Loading avatar for: " + currentUser.getName());
                ImageLoader.loadUserAvatar(getContext(), currentUser, ivUserAvatar);

                // Устанавливаем текстовые данные
                tvUserName.setText(currentUser.getName());
                tvUserEmail.setText(currentUser.getEmail());

                // Обработка местоположения
                if (currentUser.getLocation() != null && !currentUser.getLocation().isEmpty()) {
                    tvUserLocation.setText(currentUser.getLocation());
                } else {
                    tvUserLocation.setText("Местоположение не указано");
                }

                // Обработка рейтинга
                if (currentUser.getRating() > 0) {
                    tvUserRating.setText(String.format("Рейтинг: %.1f", currentUser.getRating()));
                } else {
                    tvUserRating.setText("Рейтинг: 5.0");
                }

                Log.d("ProfileFragment", "User data displayed successfully");

            } catch (Exception e) {
                Log.e("ProfileFragment", "Error loading user data", e);
                Toast.makeText(getContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show();
            Log.e("ProfileFragment", "Current user is null");
        }
    }

    private void loadUserProducts() {
        if (currentUser != null) {
            try {
                List<Product> userProducts = databaseHelper.getUserProducts(currentUser.getId());

                if (userProducts != null && !userProducts.isEmpty()) {
                    // Создаем адаптер с обработчиком кликов для управления товарами
                    productAdapter = new ProductAdapter(getContext(), userProducts, new ProductAdapter.OnProductClickListener() {
                        @Override
                        public void onProductClick(Product product) {
                            // Открываем диалог управления товаром
                            showProductManagementDialog(product);
                        }

                        @Override
                        public void onContactSeller(Product product) {
                            // В профиле эта функция не нужна
                            Toast.makeText(getContext(), "Это ваш товар", Toast.LENGTH_SHORT).show();
                        }
                    }, true); // true - режим управления (показываем кнопки управления)

                    recyclerViewUserProducts.setAdapter(productAdapter);
                    Log.d("ProfileFragment", "Loaded " + userProducts.size() + " user products");
                } else {
                    Log.d("ProfileFragment", "No products found for user");
                    // Показываем сообщение о отсутствии товаров
                    TextView tvNoProducts = getView().findViewById(R.id.tvNoProducts);
                    if (tvNoProducts != null) {
                        tvNoProducts.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Ошибка загрузки товаров", Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Error loading user products", e);
            }
        }
    }

    private void showProductManagementDialog(Product product) {
        // Создаем диалог управления товаром
        ProductManagementDialog dialog = ProductManagementDialog.newInstance(product);

        // Устанавливаем слушатель для обновления списка после изменений
        dialog.setProductUpdateListener(new ProductManagementDialog.OnProductUpdateListener() {
            @Override
            public void onProductUpdated(Product updatedProduct) {
                // Обновляем список товаров
                loadUserProducts();
                Toast.makeText(getContext(), "Товар обновлен", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProductDeleted(String productId) {
                // Обновляем список товаров
                loadUserProducts();
                Toast.makeText(getContext(), "Товар удален", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(getParentFragmentManager(), "product_management");
    }
    private void logout() {
        // Выход из аккаунта
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();

        Toast.makeText(getContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
    }

    private void openSettings() {
        // Открываем настройки пользователя
        if (currentUser != null) {
            UserSettingsFragment settingsFragment = UserSettingsFragment.newInstance(currentUser);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, settingsFragment)
                    .addToBackStack("settings")
                    .commit();
        } else {
            Toast.makeText(getContext(), "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
        }
    }

    private void openChats() {
        // Открываем список чатов
        if (currentUser != null) {
            ChatsListFragment chatsFragment = ChatsListFragment.newInstance(currentUser);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, chatsFragment)
                    .addToBackStack("chats")
                    .commit();
        } else {
            Toast.makeText(getContext(), "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем данные при возвращении на фрагмент
        if (currentUser != null && databaseHelper != null) {
            User updatedUser = databaseHelper.getUserById(currentUser.getId());
            if (updatedUser != null) {
                currentUser = updatedUser;
                loadUserData();
                loadUserProducts();
            }
        }
    }

    // НЕ закрываем базу данных здесь!
}