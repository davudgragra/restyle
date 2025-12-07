package com.example.restyle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    public interface AdminProductListener {
        void onViewProduct(Product product);
        void onDeleteProduct(Product product);
    }

    private List<Product> productList;
    private AdminProductListener listener;
    private SimpleDateFormat dateFormat;

    public AdminProductAdapter(List<Product> productList, AdminProductListener listener) {
        this.productList = productList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvProductTitle.setText(product.getTitle());

        BigDecimal price = product.getPrice();
        if (price != null) {
            holder.tvProductPrice.setText("Цена: " + price + " ₽");
        } else {
            holder.tvProductPrice.setText("Цена: не указана");
        }

        holder.tvProductSeller.setText("Продавец: " +
                (product.getSellerName() != null ? product.getSellerName() : "Неизвестно"));

        holder.tvProductCategory.setText("Категория: " +
                (product.getCategory() != null ? product.getCategory() : "Не указана"));

        String status = product.getStatus();
        if ("available".equals(status)) {
            holder.tvProductStatus.setText("Доступен");
            holder.tvProductStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_green_dark));
        } else if ("sold".equals(status)) {
            holder.tvProductStatus.setText("Продан");
            holder.tvProductStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            holder.tvProductStatus.setText(status != null ? status : "Неизвестно");
            holder.tvProductStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.darker_gray));
        }

        // Дата создания
        try {
            long timestamp = Long.parseLong(product.getCreatedAt());
            String date = dateFormat.format(new Date(timestamp));
            holder.tvProductCreated.setText("Создан: " + date);
        } catch (Exception e) {
            holder.tvProductCreated.setText("Создан: " + product.getCreatedAt());
        }

        // Кнопки
        holder.btnView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewProduct(product);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteProduct(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductTitle, tvProductPrice, tvProductSeller,
                tvProductCategory, tvProductStatus, tvProductCreated;
        Button btnView, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductTitle = itemView.findViewById(R.id.tvProductTitle);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductSeller = itemView.findViewById(R.id.tvProductSeller);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvProductStatus = itemView.findViewById(R.id.tvProductStatus);
            tvProductCreated = itemView.findViewById(R.id.tvProductCreated); // Теперь есть!
            btnView = itemView.findViewById(R.id.btnView);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}