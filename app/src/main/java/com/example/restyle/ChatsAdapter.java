package com.example.restyle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {

    private List<Chat> chats;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatsAdapter(List<Chat> chats, OnChatClickListener listener) {
        this.chats = chats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void updateChats(List<Chat> newChats) {
        this.chats = newChats;
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView tvChatTitle, tvLastMessage, tvOtherUser, tvTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatTitle = itemView.findViewById(R.id.tvChatTitle);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvOtherUser = itemView.findViewById(R.id.tvOtherUser);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(Chat chat) {
            tvChatTitle.setText(chat.getProductTitle() != null ? chat.getProductTitle() : "Без названия");

            if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
                tvLastMessage.setText(chat.getLastMessage());
            } else {
                tvLastMessage.setText("Нет сообщений");
            }

            if (chat.getOtherUserName() != null && !chat.getOtherUserName().isEmpty()) {
                tvOtherUser.setText("С: " + chat.getOtherUserName());
            } else {
                tvOtherUser.setText("С: Неизвестный пользователь");
            }

            // Форматируем время
            if (chat.getCreatedAt() != null) {
                try {
                    long timestamp = Long.parseLong(chat.getCreatedAt());
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM HH:mm", java.util.Locale.getDefault());
                    String time = sdf.format(new java.util.Date(timestamp));
                    tvTime.setText(time);
                } catch (NumberFormatException e) {
                    tvTime.setText("--:--");
                }
            } else {
                tvTime.setText("--:--");
            }
        }
    }
}