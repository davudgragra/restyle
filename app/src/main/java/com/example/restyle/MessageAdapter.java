package com.example.restyle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        boolean isSent = message.getSenderId().equals(currentUserId);
        Log.d("MessageAdapter", "Message type - Sent: " + isSent + ", Sender: " + message.getSenderId() + ", Current: " + currentUserId);
        return isSent ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view, currentUserId);
        } else {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new MessageViewHolder(view, currentUserId);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessageText, tvMessageTime, tvSenderName;
        private String currentUserId;

        public MessageViewHolder(@NonNull View itemView, String currentUserId) {
            super(itemView);
            this.currentUserId = currentUserId;
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
        }

        public void bind(Message message) {
            tvMessageText.setText(message.getText());

            // Форматируем время
            try {
                long timestamp = Long.parseLong(message.getCreatedAt());
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String time = sdf.format(new Date(timestamp));
                tvMessageTime.setText(time);
            } catch (NumberFormatException e) {
                tvMessageTime.setText("--:--");
            }

            // Правильная логика отображения имени отправителя
            if (tvSenderName != null) {
                // Показываем имя отправителя только для ВХОДЯЩИХ сообщений
                // и если это не текущий пользователь
                if (!message.getSenderId().equals(currentUserId)) {
                    tvSenderName.setText(message.getSenderName());
                    tvSenderName.setVisibility(View.VISIBLE);
                } else {
                    // Для исходящих сообщений скрываем имя отправителя
                    tvSenderName.setVisibility(View.GONE);
                }
            }
        }
    }
}