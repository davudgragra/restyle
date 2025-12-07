package com.example.restyle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    public interface AdminUserListener {
        void onBlockUser(User user);
        void onDeleteUser(User user);
    }

    private List<User> userList;
    private AdminUserListener listener;
    private SimpleDateFormat dateFormat;

    public AdminUserAdapter(List<User> userList, AdminUserListener listener) {
        this.userList = userList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserName.setText(user.getName());
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserLocation.setText(user.getLocation());
        holder.tvUserRating.setText(String.format(Locale.getDefault(), "Рейтинг: %.1f", user.getRating()));

        // Форматируем дату
        try {
            long timestamp = Long.parseLong(user.getCreatedAt());
            String date = dateFormat.format(new Date(timestamp));
            holder.tvUserCreated.setText("Зарегистрирован: " + date);
        } catch (Exception e) {
            holder.tvUserCreated.setText("Зарегистрирован: " + user.getCreatedAt());
        }

        // Статус блокировки
        if (user.isBlocked()) {
            holder.tvUserStatus.setText("Заблокирован");
            holder.tvUserStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.btnBlock.setText("Разблокировать");
        } else {
            holder.tvUserStatus.setText("Активен");
            holder.tvUserStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.btnBlock.setText("Заблокировать");
        }

        // Кнопки
        holder.btnBlock.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBlockUser(user);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteUser(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserLocation, tvUserRating, tvUserStatus, tvUserCreated;
        Button btnBlock, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserLocation = itemView.findViewById(R.id.tvUserLocation);
            tvUserRating = itemView.findViewById(R.id.tvUserRating);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            tvUserCreated = itemView.findViewById(R.id.tvUserCreated);
            btnBlock = itemView.findViewById(R.id.btnBlock);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}