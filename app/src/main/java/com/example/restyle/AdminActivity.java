package com.example.restyle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private User currentUser;
    private DatabaseHelper databaseHelper;
    private TextView tvStats;
    private LinearLayout layoutContent;
    private Button btnShowUsers, btnShowProducts, btnRefresh, btnLogout;

    private boolean showingUsers = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Получаем текущего пользователя (админа)
        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser == null || !currentUser.isAdmin()) {
            Toast.makeText(this, "Доступ запрещен", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toast.makeText(this, "Панель администратора", Toast.LENGTH_SHORT).show();

        databaseHelper = new DatabaseHelper(this);

        // Инициализация UI
        tvStats = findViewById(R.id.tvStats);
        layoutContent = findViewById(R.id.layoutContent);
        btnShowUsers = findViewById(R.id.btnShowUsers);
        btnShowProducts = findViewById(R.id.btnShowProducts);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnLogout = findViewById(R.id.btnLogout);

        // Обновляем статистику и показываем пользователей
        updateStats();
        showUsers();

        // Обработчики кнопок
        btnShowUsers.setOnClickListener(v -> {
            showingUsers = true;
            btnShowUsers.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            btnShowProducts.setBackgroundColor(getResources().getColor(android.R.color.white));
            showUsers();
        });

        btnShowProducts.setOnClickListener(v -> {
            showingUsers = false;
            btnShowProducts.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            btnShowUsers.setBackgroundColor(getResources().getColor(android.R.color.white));
            showProducts();
        });

        btnRefresh.setOnClickListener(v -> {
            refreshData();
        });

        btnLogout.setOnClickListener(v -> {
            logoutAdmin();
        });
    }

    private void showUsers() {
        layoutContent.removeAllViews();
        List<User> users = databaseHelper.getAllUsers();

        if (users.isEmpty()) {
            showEmptyMessage("Нет зарегистрированных пользователей");
            return;
        }

        for (User user : users) {
            addUserView(user);
        }
    }

    private void showProducts() {
        layoutContent.removeAllViews();
        List<Product> products = databaseHelper.getAllProductsForAdmin();

        if (products.isEmpty()) {
            showEmptyMessage("Нет товаров в базе данных");
            return;
        }

        for (Product product : products) {
            addProductView(product);
        }
    }

    private void addUserView(User user) {
        View userView = getLayoutInflater().inflate(R.layout.item_admin_user, null);

        TextView tvName = userView.findViewById(R.id.tvUserName);
        TextView tvEmail = userView.findViewById(R.id.tvUserEmail);
        TextView tvLocation = userView.findViewById(R.id.tvUserLocation);
        TextView tvStatus = userView.findViewById(R.id.tvUserStatus);
        TextView tvCreated = userView.findViewById(R.id.tvUserCreated);
        Button btnBlock = userView.findViewById(R.id.btnBlock);
        Button btnDelete = userView.findViewById(R.id.btnDelete);

        // Заполняем данные
        tvName.setText(user.getName() != null ? user.getName() : "Без имени");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "Без email");
        tvLocation.setText(user.getLocation() != null ? user.getLocation() : "Не указано");

        // Дата регистрации
        try {
            long timestamp = Long.parseLong(user.getCreatedAt());
            String date = new java.text.SimpleDateFormat("dd.MM.yyyy").format(new java.util.Date(timestamp));
            tvCreated.setText("Зарегистрирован: " + date);
        } catch (Exception e) {
            tvCreated.setText("Дата регистрации: неизвестно");
        }

        // Статус блокировки
        if (user.isBlocked()) {
            tvStatus.setText("Заблокирован");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnBlock.setText("Разблокировать");
        } else {
            tvStatus.setText("Активен");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnBlock.setText("Заблокировать");
        }

        // Кнопка блокировки/разблокировки
        btnBlock.setOnClickListener(v -> {
            boolean newBlockStatus = !user.isBlocked();
            if (databaseHelper.updateUserBlockStatus(user.getId(), newBlockStatus)) {
                user.setBlocked(newBlockStatus);
                Toast.makeText(AdminActivity.this,
                        newBlockStatus ? "Пользователь заблокирован" : "Пользователь разблокирован",
                        Toast.LENGTH_SHORT).show();
                showUsers(); // Обновляем список
                updateStats(); // Обновляем статистику
            }
        });

        // Кнопка удаления
        btnDelete.setOnClickListener(v -> {
            showDeleteUserDialog(user);
        });

        layoutContent.addView(userView);
    }

    private void addProductView(Product product) {
        View productView = getLayoutInflater().inflate(R.layout.item_admin_product, null);

        TextView tvTitle = productView.findViewById(R.id.tvProductTitle);
        TextView tvPrice = productView.findViewById(R.id.tvProductPrice);
        TextView tvSeller = productView.findViewById(R.id.tvProductSeller);
        TextView tvCategory = productView.findViewById(R.id.tvProductCategory);
        TextView tvStatus = productView.findViewById(R.id.tvProductStatus);
        Button btnView = productView.findViewById(R.id.btnView);
        Button btnDelete = productView.findViewById(R.id.btnDelete);

        // Заполняем данные
        tvTitle.setText(product.getTitle() != null ? product.getTitle() : "Без названия");

        if (product.getPrice() != null) {
            tvPrice.setText("Цена: " + product.getPrice() + " ₽");
        } else {
            tvPrice.setText("Цена: не указана");
        }

        // Получаем имя продавца
        String sellerName = product.getSellerName();
        if (sellerName == null || sellerName.isEmpty()) {
            User seller = databaseHelper.getUserById(product.getSellerId());
            sellerName = seller != null ? seller.getName() : "Неизвестный продавец";
        }
        tvSeller.setText("Продавец: " + sellerName);

        tvCategory.setText("Категория: " +
                (product.getCategory() != null ? product.getCategory() : "Не указана"));

        String status = product.getStatus();
        if ("available".equals(status)) {
            tvStatus.setText("Доступен");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if ("sold".equals(status)) {
            tvStatus.setText("Продан");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvStatus.setText(status != null ? status : "Неизвестно");
            tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }

        // Кнопка просмотра деталей
        btnView.setOnClickListener(v -> {
            showProductDetails(product);
        });

        // Кнопка удаления
        btnDelete.setOnClickListener(v -> {
            showDeleteProductDialog(product);
        });

        layoutContent.addView(productView);
    }

    private void showDeleteUserDialog(User user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Удаление пользователя");
        builder.setMessage("Вы уверены, что хотите удалить пользователя:\n\n" +
                "Имя: " + user.getName() + "\n" +
                "Email: " + user.getEmail() + "\n\n" +
                "Внимание: Все товары пользователя также будут удалены!");

        builder.setPositiveButton("Удалить", (dialog, which) -> {
            if (databaseHelper.deleteUser(user.getId())) {
                Toast.makeText(AdminActivity.this, "Пользователь удален", Toast.LENGTH_SHORT).show();
                showUsers();
                updateStats();
            } else {
                Toast.makeText(AdminActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showDeleteProductDialog(Product product) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Удаление товара");
        builder.setMessage("Вы уверены, что хотите удалить товар:\n\n" +
                "Название: " + product.getTitle() + "\n" +
                "Цена: " + product.getPrice() + " ₽\n" +
                "Продавец: " + product.getSellerName());

        builder.setPositiveButton("Удалить", (dialog, which) -> {
            if (databaseHelper.deleteProductAdmin(product.getId())) {
                Toast.makeText(AdminActivity.this, "Товар удален", Toast.LENGTH_SHORT).show();
                showProducts();
                updateStats();
            } else {
                Toast.makeText(AdminActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showProductDetails(Product product) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Информация о товаре");

        StringBuilder message = new StringBuilder();
        message.append("Название: ").append(product.getTitle()).append("\n\n");
        message.append("Описание: ").append(product.getDescription() != null ? product.getDescription() : "Нет описания").append("\n\n");
        message.append("Цена: ").append(product.getPrice() != null ? product.getPrice() + " ₽" : "Не указана").append("\n\n");
        message.append("Категория: ").append(product.getCategory() != null ? product.getCategory() : "Не указана").append("\n\n");
        message.append("Размер: ").append(product.getSize() != null ? product.getSize() : "Не указан").append("\n\n");
        message.append("Состояние: ").append(product.getCondition() != null ? product.getCondition() : "Не указано").append("\n\n");

        // Получаем информацию о продавце
        User seller = databaseHelper.getUserById(product.getSellerId());
        if (seller != null) {
            message.append("Продавец: ").append(seller.getName()).append("\n");
            message.append("Email продавца: ").append(seller.getEmail()).append("\n");
            message.append("Рейтинг продавца: ").append(seller.getRating()).append("\n");
            message.append("Город: ").append(seller.getLocation() != null ? seller.getLocation() : "Не указан").append("\n");
            if (seller.isBlocked()) {
                message.append("Статус продавца: ЗАБЛОКИРОВАН").append("\n");
            }
        }

        message.append("\nСтатус товара: ").append(product.getStatus() != null ? product.getStatus() : "Неизвестно");

        builder.setMessage(message.toString());
        builder.setPositiveButton("Закрыть", null);

        // Добавляем кнопку удаления в диалог
        builder.setNegativeButton("Удалить товар", (dialog, which) -> {
            showDeleteProductDialog(product);
        });

        builder.show();
    }

    private void showEmptyMessage(String message) {
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText(message);
        tvEmpty.setTextSize(14);
        tvEmpty.setPadding(32, 32, 32, 32);
        tvEmpty.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tvEmpty.setGravity(android.view.Gravity.CENTER);
        layoutContent.addView(tvEmpty);
    }

    private void refreshData() {
        updateStats();
        if (showingUsers) {
            showUsers();
        } else {
            showProducts();
        }
        Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show();
    }

    private void updateStats() {
        try {
            int totalUsers = databaseHelper.getUserCount();
            int totalProducts = databaseHelper.getProductCount();
            int blockedUsers = databaseHelper.getBlockedUserCount();

            String stats = String.format("👥 Пользователей: %d (🚫 заблокировано: %d) | 📦 Товаров: %d",
                    totalUsers, blockedUsers, totalProducts);
            tvStats.setText(stats);
        } catch (Exception e) {
            tvStats.setText("Ошибка загрузки статистики");
        }
    }

    private void logoutAdmin() {
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}