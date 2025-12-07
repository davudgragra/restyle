package com.example.restyle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AdminProductsFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminProductAdapter adapter;
    private List<Product> productList;
    private DatabaseHelper databaseHelper;
    private TextView tvNoProducts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_products, container, false);

        databaseHelper = new DatabaseHelper(getContext());

        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        tvNoProducts = view.findViewById(R.id.tvNoProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        productList = new ArrayList<>();
        adapter = new AdminProductAdapter(productList, new AdminProductAdapter.AdminProductListener() {
            @Override
            public void onViewProduct(Product product) {
                // Показать детали товара
                showProductDetails(product);
            }

            @Override
            public void onDeleteProduct(Product product) {
                // Удалить товар
                showDeleteProductDialog(product);
            }
        });

        recyclerView.setAdapter(adapter);

        loadProducts();

        return view;
    }

    private void showProductDetails(Product product) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Информация о товаре");

        StringBuilder message = new StringBuilder();
        message.append("Название: ").append(product.getTitle()).append("\n\n");
        message.append("Описание: ").append(product.getDescription() != null ? product.getDescription() : "Нет описания").append("\n\n");
        message.append("Цена: ").append(product.getPrice() != null ? product.getPrice() + " ₽" : "Не указана").append("\n\n");
        message.append("Категория: ").append(product.getCategory() != null ? product.getCategory() : "Не указана").append("\n\n");
        message.append("Размер: ").append(product.getSize() != null ? product.getSize() : "Не указан").append("\n\n");
        message.append("Состояние: ").append(product.getCondition() != null ? product.getCondition() : "Не указано").append("\n\n");
        message.append("Продавец: ").append(product.getSellerName() != null ? product.getSellerName() : "Неизвестно").append("\n\n");
        message.append("Статус: ").append(product.getStatus() != null ? product.getStatus() : "Неизвестно");

        builder.setMessage(message.toString());
        builder.setPositiveButton("Закрыть", null);
        builder.show();
    }

    private void showDeleteProductDialog(Product product) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Удаление товара")
                .setMessage("Удалить товар \"" + product.getTitle() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    if (databaseHelper.deleteProductAdmin(product.getId())) {
                        productList.remove(product);
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                        Toast.makeText(getContext(), "Товар удален", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void loadProducts() {
        productList.clear();
        productList.addAll(databaseHelper.getAllProductsForAdmin());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (productList.isEmpty()) {
            tvNoProducts.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoProducts.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
    }
}