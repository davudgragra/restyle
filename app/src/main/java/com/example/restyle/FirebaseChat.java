package com.example.restyle;

import android.util.Log;
import com.google.firebase.firestore.*;
import java.util.*;

public class FirebaseChat {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "FirebaseChat";

    // ==================== ОТПРАВИТЬ СООБЩЕНИЕ ====================
    public static void sendMessage(String chatId, Message message) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", message.getId());
        data.put("senderId", message.getSenderId());
        data.put("senderName", message.getSenderName());
        data.put("text", message.getText());
        data.put("createdAt", message.getCreatedAt());

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(message.getId())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Сообщение отправлено: " + message.getText());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка отправки: " + e.getMessage());
                });
    }

    // ==================== СЛУШАТЬ СООБЩЕНИЯ ====================
    public static ListenerRegistration listenToMessages(String chatId, MessageListener listener) {
        return db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Ошибка слушателя: " + error.getMessage());
                        return;
                    }

                    if (snapshots != null) {
                        List<Message> messages = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Message msg = new Message();
                            msg.setId(doc.getString("id"));
                            msg.setSenderId(doc.getString("senderId"));
                            msg.setSenderName(doc.getString("senderName"));
                            msg.setText(doc.getString("text"));
                            msg.setCreatedAt(doc.getString("createdAt"));
                            messages.add(msg);
                        }
                        listener.onNewMessages(messages);
                    }
                });
    }

    // ==================== СОЗДАТЬ ЧАТ ====================
    public static void createChat(String chatId, String userId1, String userId2, String productId) {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("id", chatId);
        chatData.put("user1", userId1);
        chatData.put("user2", userId2);
        chatData.put("productId", productId);
        chatData.put("createdAt", String.valueOf(System.currentTimeMillis()));

        db.collection("chats")
                .document(chatId)
                .set(chatData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Чат создан: " + chatId);
                });
    }

    // ==================== ПОЛУЧИТЬ ЧАТЫ ПОЛЬЗОВАТЕЛЯ ====================
    public static void getUserChats(String userId, ChatsListener listener) {
        // Ищем чаты где пользователь является user1 или user2
        db.collection("chats")
                .whereEqualTo("user1", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> chatIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        chatIds.add(doc.getId());
                    }

                    // Ищем чаты где пользователь user2
                    db.collection("chats")
                            .whereEqualTo("user2", userId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                for (DocumentSnapshot doc : queryDocumentSnapshots2.getDocuments()) {
                                    if (!chatIds.contains(doc.getId())) {
                                        chatIds.add(doc.getId());
                                    }
                                }

                                // Загружаем полную информацию о чатах
                                List<Chat> chats = new ArrayList<>();
                                for (String chatId : chatIds) {
                                    loadChatInfo(chatId, userId, chats, listener);
                                }

                                if (chatIds.isEmpty()) {
                                    listener.onChatsLoaded(new ArrayList<>());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка получения чатов: " + e.getMessage());
                    listener.onChatsLoaded(new ArrayList<>());
                });
    }

    private static void loadChatInfo(String chatId, String userId, List<Chat> chats, ChatsListener listener) {
        db.collection("chats").document(chatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Chat chat = new Chat();
                        chat.setId(chatId);
                        chat.setProductId(documentSnapshot.getString("productId"));

                        // Определяем кто другой пользователь
                        String user1 = documentSnapshot.getString("user1");
                        String user2 = documentSnapshot.getString("user2");
                        String otherUserId = userId.equals(user1) ? user2 : user1;

                        chat.setBuyerId(userId.equals(user1) ? userId : otherUserId);
                        chat.setSellerId(userId.equals(user1) ? otherUserId : userId);
                        chat.setCreatedAt(documentSnapshot.getString("createdAt"));

                        // Загружаем последнее сообщение
                        loadLastMessage(chatId, chat, chats, listener);
                    }
                });
    }

    private static void loadLastMessage(String chatId, Chat chat, List<Chat> chats, ChatsListener listener) {
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot lastMsg = queryDocumentSnapshots.getDocuments().get(0);
                        chat.setLastMessage(lastMsg.getString("text"));
                    } else {
                        chat.setLastMessage("Нет сообщений");
                    }

                    // Загружаем информацию о товаре
                    loadProductInfo(chat, chats, listener);
                });
    }

    private static void loadProductInfo(Chat chat, List<Chat> chats, ChatsListener listener) {
        if (chat.getProductId() != null) {
            db.collection("products").document(chat.getProductId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            chat.setProductTitle(documentSnapshot.getString("title"));
                        }

                        // Загружаем информацию о другом пользователе
                        loadOtherUserInfo(chat, chats, listener);
                    });
        } else {
            loadOtherUserInfo(chat, chats, listener);
        }
    }

    private static void loadOtherUserInfo(Chat chat, List<Chat> chats, ChatsListener listener) {
        String otherUserId = chat.getBuyerId().equals(chat.getSellerId()) ?
                chat.getSellerId() :
                (chat.getBuyerId().equals(chat.getBuyerId()) ? chat.getSellerId() : chat.getBuyerId());

        if (otherUserId != null) {
            db.collection("users").document(otherUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            chat.setOtherUserName(documentSnapshot.getString("name"));
                        }

                        chats.add(chat);
                        // Когда все загружено - вызываем callback
                        if (chats.size() == chats.size()) {
                            listener.onChatsLoaded(chats);
                        }
                    });
        } else {
            chats.add(chat);
            if (chats.size() == chats.size()) {
                listener.onChatsLoaded(chats);
            }
        }
    }

    // ==================== ГЕНЕРАЦИЯ ID ЧАТА ====================
    public static String generateChatId(String user1, String user2, String productId) {
        // Сортируем ID чтобы у обоих одинаковый chatId
        String[] users = {user1, user2};
        Arrays.sort(users);
        return "chat_" + productId + "_" + users[0] + "_" + users[1];
    }

    // ==================== ИНТЕРФЕЙСЫ ====================
    public interface MessageListener {
        void onNewMessages(List<Message> messages);
    }

    public interface ChatsListener {
        void onChatsLoaded(List<Chat> chats);
    }
}