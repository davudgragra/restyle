package com.example.restyle;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeedFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        databaseHelper = new DatabaseHelper(getContext());

        initViews(view);
        loadProducts();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    private void loadProducts() {
        try {
            Log.d("FeedFragment", "Starting to load products...");

            // Получаем товары из БД
            List<Product> products = databaseHelper.getProducts();

            // Отладочная информация
            Log.d("FeedFragment", "=== LOADING PRODUCTS ===");
            Log.d("FeedFragment", "Total products from DB: " + products.size());

            // Фильтруем только available товары
            List<Product> availableProducts = filterAvailableProducts(products);
            Log.d("FeedFragment", "Available products after filtering: " + availableProducts.size());

            for (Product product : availableProducts) {
                Log.d("FeedFragment", "Available Product: " + product.getTitle() +
                        ", Price: " + product.getPrice() +
                        ", Seller: " + product.getSellerName() +
                        ", Images: " + (product.getImageList() != null ? product.getImageList().size() : 0));
            }

            if (availableProducts.isEmpty()) {
                Toast.makeText(getContext(), "Нет доступных товаров", Toast.LENGTH_SHORT).show();
                Log.d("FeedFragment", "No available products to display");
            }

            // Создаем адаптер
            productAdapter = new ProductAdapter(getContext(), availableProducts, new ProductAdapter.OnProductClickListener() {
                @Override
                public void onProductClick(Product product) {
                    // Открываем детальную страницу товара
                    openProductDetail(product);
                }

                @Override
                public void onContactSeller(Product product) {
                    // Открываем чат с продавцом
                    contactSeller(product);
                }
            });

            recyclerView.setAdapter(productAdapter);
            Log.d("FeedFragment", "Adapter set successfully");

        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка загрузки товаров", Toast.LENGTH_SHORT).show();
            Log.e("FeedFragment", "Error loading products", e);
            e.printStackTrace();
        }
    }

    private List<Product> filterAvailableProducts(List<Product> allProducts) {
        // Фильтруем только товары со статусом "available"
        java.util.List<Product> availableProducts = new java.util.ArrayList<>();
        for (Product product : allProducts) {
            if ("available".equals(product.getStatus())) {
                availableProducts.add(product);
            }
        }
        return availableProducts;
    }

    private void openProductDetail(Product product) {
        try {
            // Создаем фрагмент детальной информации о товаре
            ProductDetailFragment detailFragment = ProductDetailFragment.newInstance(product);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack("product_detail")
                    .commit();

            Log.d("FeedFragment", "Opening product detail: " + product.getTitle());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка открытия товара", Toast.LENGTH_SHORT).show();
            Log.e("FeedFragment", "Error opening product detail", e);
        }
    }

    private void contactSeller(Product product) {
        try {
            // Получаем текущего пользователя
            User currentUser = (User) getActivity().getIntent().getSerializableExtra("user");

            if (currentUser != null) {
                // Проверяем, что пользователь не пишет сам себе
                if (product.getSellerId().equals(currentUser.getId())) {
                    Toast.makeText(getContext(), "Вы не можете написать себе", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Создаем фрагмент чата
                ChatFragment chatFragment = ChatFragment.newInstance(product, currentUser);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, chatFragment)
                        .addToBackStack("chat")
                        .commit();

                Log.d("FeedFragment", "Opening chat for product: " + product.getTitle());
            } else {
                Toast.makeText(getContext(), "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка открытия чата", Toast.LENGTH_SHORT).show();
            Log.e("FeedFragment", "Error opening chat", e);
        }
    }

    public void refreshProducts() {
        loadProducts();
    }

    // НЕ закрываем базу данных здесь!
}