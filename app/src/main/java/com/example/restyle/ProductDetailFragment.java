package com.example.restyle;

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
import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailFragment extends Fragment {

    private static final String ARG_PRODUCT = "product";

    private Product product;
    private User currentUser;
    private DatabaseHelper databaseHelper;

    private ImageView ivProductImage, ivSellerAvatar;
    private TextView tvProductTitle, tvProductPrice, tvProductCategory, tvProductSize,
            tvProductCondition, tvProductDescription, tvSellerName, tvSellerRating;
    private Button btnContactSeller, btnBack;

    public static ProductDetailFragment newInstance(Product product) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }

        // Получаем текущего пользователя
        if (getActivity() != null) {
            currentUser = (User) getActivity().getIntent().getSerializableExtra("user");
        }

        databaseHelper = new DatabaseHelper(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        initViews(view);
        loadProductData();

        return view;
    }

    private void initViews(View view) {
        ivProductImage = view.findViewById(R.id.ivProductImage);
        ivSellerAvatar = view.findViewById(R.id.ivSellerAvatar);
        tvProductTitle = view.findViewById(R.id.tvProductTitle);
        tvProductPrice = view.findViewById(R.id.tvProductPrice);
        tvProductCategory = view.findViewById(R.id.tvProductCategory);
        tvProductSize = view.findViewById(R.id.tvProductSize);
        tvProductCondition = view.findViewById(R.id.tvProductCondition);
        tvProductDescription = view.findViewById(R.id.tvProductDescription);
        tvSellerName = view.findViewById(R.id.tvSellerName);
        tvSellerRating = view.findViewById(R.id.tvSellerRating);
        btnContactSeller = view.findViewById(R.id.btnContactSeller);
        btnBack = view.findViewById(R.id.btnBack);

        btnContactSeller.setOnClickListener(v -> contactSeller());
        btnBack.setOnClickListener(v -> goBack());
    }

    private void loadProductData() {
        if (product != null) {
            // Загрузка изображения товара
            ImageLoader.loadProductImage(getContext(), product, ivProductImage);

            // Основная информация
            tvProductTitle.setText(product.getTitle());

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
            String formattedPrice = format.format(product.getPrice());
            tvProductPrice.setText(formattedPrice);

            tvProductCategory.setText("Категория: " + product.getCategory());
            tvProductSize.setText("Размер: " + product.getSize());
            tvProductCondition.setText("Состояние: " + product.getCondition());

            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                tvProductDescription.setText(product.getDescription());
            } else {
                tvProductDescription.setText("Описание отсутствует");
            }

            // Загрузка информации о продавце
            loadSellerInfo();
        }
    }

    private void loadSellerInfo() {
        if (product != null) {
            try {
                User seller = databaseHelper.getUserById(product.getSellerId());
                if (seller != null) {
                    tvSellerName.setText(seller.getName());
                    tvSellerRating.setText("Рейтинг: " + seller.getRating());

                    // Загрузка аватара продавца
                    ImageLoader.loadUserAvatar(getContext(), seller, ivSellerAvatar);
                } else {
                    tvSellerName.setText("Продавец не найден");
                    tvSellerRating.setText("Рейтинг: неизвестно");
                }
            } catch (Exception e) {
                Log.e("ProductDetailFragment", "Error loading seller info", e);
                tvSellerName.setText("Ошибка загрузки");
                tvSellerRating.setText("Рейтинг: неизвестно");
            }
        }
    }

    private void contactSeller() {
        if (product != null && currentUser != null) {
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
        } else {
            Toast.makeText(getContext(), "Ошибка: данные не найдены", Toast.LENGTH_SHORT).show();
        }
    }

    private void goBack() {
        getParentFragmentManager().popBackStack();
    }

    // НЕ закрываем базу данных здесь!
}