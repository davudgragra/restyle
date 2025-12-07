package com.example.restyle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private LayoutInflater inflater;
    private OnProductClickListener listener;
    private boolean isManagementMode; // Режим управления товарами

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onContactSeller(Product product);
    }

    // Конструктор для ленты товаров (с кликами, без управления)
    public ProductAdapter(android.content.Context context, List<Product> productList, OnProductClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.productList = productList;
        this.listener = listener;
        this.isManagementMode = false;
    }

    // Конструктор для профиля (с кликами и управлением)
    public ProductAdapter(android.content.Context context, List<Product> productList, OnProductClickListener listener, boolean isManagementMode) {
        this.inflater = LayoutInflater.from(context);
        this.productList = productList;
        this.listener = listener;
        this.isManagementMode = isManagementMode;
    }

    // Конструктор без обработчика кликов
    public ProductAdapter(android.content.Context context, List<Product> productList) {
        this.inflater = LayoutInflater.from(context);
        this.productList = productList;
        this.listener = null;
        this.isManagementMode = false;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);

        // Обработка клика на весь товар (только если есть listener)
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                if (isManagementMode) {
                    // В режиме управления - открываем диалог управления
                    listener.onProductClick(product);
                } else {
                    // В обычном режиме - открываем детали товара
                    listener.onProductClick(product);
                }
            });
        } else {
            // Если нет listener, отключаем кликабельность
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        }

        // В режиме управления показываем статус товара
        if (isManagementMode) {
            holder.tvProductStatus.setVisibility(View.VISIBLE);
            String statusText = getStatusText(product.getStatus());
            holder.tvProductStatus.setText("Статус: " + statusText);
            holder.tvProductStatus.setTextColor(getStatusColor(product.getStatus()));

            // Скрываем информацию о продавце в режиме управления (это свой товар)
            holder.tvSellerName.setVisibility(View.GONE);
            holder.ivSellerAvatar.setVisibility(View.GONE);
        } else {
            holder.tvProductStatus.setVisibility(View.GONE);
            holder.tvSellerName.setVisibility(View.VISIBLE);
            holder.ivSellerAvatar.setVisibility(View.VISIBLE);
        }
    }

    private String getStatusText(String status) {
        if (status == null) return "Неизвестно";
        switch (status) {
            case "available": return "Доступен";
            case "reserved": return "Забронирован";
            case "sold": return "Продан";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return android.graphics.Color.GRAY;
        switch (status) {
            case "available": return getColor(android.R.color.holo_green_dark);
            case "reserved": return getColor(android.R.color.holo_orange_dark);
            case "sold": return getColor(android.R.color.holo_red_dark);
            default: return android.graphics.Color.GRAY;
        }
    }

    private int getColor(int colorResId) {
        return inflater.getContext().getResources().getColor(colorResId);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateData(List<Product> newProducts) {
        this.productList = newProducts;
        notifyDataSetChanged();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage, ivSellerAvatar;
        private TextView tvProductTitle, tvProductPrice, tvProductCategory, tvSellerName, tvProductStatus;
        private DatabaseHelper databaseHelper;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            ivSellerAvatar = itemView.findViewById(R.id.ivSellerAvatar);
            tvProductTitle = itemView.findViewById(R.id.tvProductTitle);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvSellerName = itemView.findViewById(R.id.tvSellerName);
            tvProductStatus = itemView.findViewById(R.id.tvProductStatus);

            databaseHelper = new DatabaseHelper(itemView.getContext());
        }

        public void bind(Product product) {
            // Основная информация о товаре
            tvProductTitle.setText(product.getTitle());

            // Форматируем цену
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
            String formattedPrice = format.format(product.getPrice());
            tvProductPrice.setText(formattedPrice);

            tvProductCategory.setText(product.getCategory());

            // Информация о продавце
            if (product.getSellerName() != null && !product.getSellerName().isEmpty()) {
                tvSellerName.setText("Продавец: " + product.getSellerName());
            } else {
                tvSellerName.setText("Продавец: Неизвестен");
            }

            // Загрузка изображения товара
            ImageLoader.loadProductImage(itemView.getContext(), product, ivProductImage);

            // Загрузка аватара продавца
            loadSellerAvatar(product.getSellerId());
        }
        private void loadSellerAvatar(String sellerId) {
            if (sellerId != null && !sellerId.isEmpty()) {
                try {
                    User seller = databaseHelper.getUserById(sellerId);
                    if (seller != null && seller.getAvatarUrl() != null && !seller.getAvatarUrl().isEmpty()) {
                        ImageLoader.loadUserAvatar(itemView.getContext(), seller.getAvatarUrl(), ivSellerAvatar);
                    } else {
                        ivSellerAvatar.setImageResource(R.drawable.ic_profile);
                    }
                } catch (Exception e) {
                    android.util.Log.e("ProductAdapter", "Error loading seller avatar", e);
                    ivSellerAvatar.setImageResource(R.drawable.ic_profile);
                }
            } else {
                ivSellerAvatar.setImageResource(R.drawable.ic_profile);
            }
        }
    }
}