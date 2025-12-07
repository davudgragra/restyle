package com.example.restyle;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatFragment extends Fragment {

    private static final String ARG_PRODUCT = "product";
    private static final String ARG_CURRENT_USER = "current_user";

    private Product product;
    private User currentUser;

    private RecyclerView recyclerViewMessages;
    private EditText etMessage;
    private Button btnSend, btnBack;
    private TextView tvChatTitle;

    private MessageAdapter messageAdapter;
    private DatabaseHelper databaseHelper;
    private List<Message> messages = new ArrayList<>();
    private String chatId;

    public static ChatFragment newInstance(Product product, User currentUser) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        args.putSerializable(ARG_CURRENT_USER, currentUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
            currentUser = (User) getArguments().getSerializable(ARG_CURRENT_USER);
        }
        databaseHelper = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_simple, container, false);

        initViews(view);
        setupChat();
        loadMessages();

        return view;
    }

    private void initViews(View view) {
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnBack = view.findViewById(R.id.btnBack);
        tvChatTitle = view.findViewById(R.id.tvChatTitle);

        // Настройка RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(messages, currentUser != null ? currentUser.getId() : "");
        recyclerViewMessages.setAdapter(messageAdapter);

        // Обработчики
        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> goBack());
    }

    private void setupChat() {
        if (product != null && currentUser != null) {
            try {
                // Получаем продавца из БД
                User seller = databaseHelper.getUserById(product.getSellerId());

                // Определяем ID другого пользователя в чате
                String otherUserId;
                String otherUserName;

                if (currentUser.getId().equals(product.getSellerId())) {
                    // Текущий пользователь - продавец, нужно найти покупателя
                    // Ищем существующий чат с этим товаром
                    otherUserId = findExistingBuyerId();
                    if (otherUserId == null) {
                        // Если чата нет, создаем тестового покупателя
                        otherUserId = createTestBuyer();
                    }
                    otherUserName = databaseHelper.getUserNameById(otherUserId);
                } else {
                    // Текущий пользователь - покупатель, другой - продавец
                    otherUserId = product.getSellerId();
                    otherUserName = seller != null ? seller.getName() : "Продавец";
                }

                // Генерируем ID чата (одинаковый для обоих пользователей)
                chatId = generateChatId(currentUser.getId(), otherUserId, product.getId());

                // Проверяем и создаем чат если нужно
                if (!isChatExists(chatId)) {
                    createNewChat(otherUserId);
                }

                // Обновляем заголовок
                tvChatTitle.setText("Чат с " + otherUserName + " о товаре: " + product.getTitle());

                // Загружаем сообщения
                loadMessages();

            } catch (Exception e) {
                Log.e("ChatFragment", "Ошибка настройки чата", e);
                Toast.makeText(getContext(), "Ошибка создания чата", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String findExistingBuyerId() {
        try {
            // Ищем существующий чат для этого товара и продавца
            String query = "SELECT buyerId FROM chats WHERE productId = ? AND sellerId = ?";
            Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                    query,
                    new String[]{product.getId(), currentUser.getId()}
            );

            if (cursor != null && cursor.moveToFirst()) {
                String buyerId = cursor.getString(cursor.getColumnIndexOrThrow("buyerId"));
                cursor.close();
                return buyerId;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("ChatFragment", "Ошибка поиска покупателя", e);
        }
        return null;
    }

    private String createTestBuyer() {
        // Создаем тестового покупателя для демонстрации
        String buyerId = "test_buyer_" + UUID.randomUUID().toString().substring(0, 8);

        try {
            // Проверяем не существует ли уже
            if (databaseHelper.getUserById(buyerId) == null) {
                User testBuyer = new User();
                testBuyer.setId(buyerId);
                testBuyer.setName("Покупатель");
                testBuyer.setEmail(buyerId + "@example.com");
                testBuyer.setPassword("password");
                testBuyer.setLocation("Москва");
                testBuyer.setCreatedAt(String.valueOf(System.currentTimeMillis()));

                databaseHelper.addUser(testBuyer);
                Log.d("ChatFragment", "Создан тестовый покупатель: " + buyerId);
            }
        } catch (Exception e) {
            Log.e("ChatFragment", "Ошибка создания тестового покупателя", e);
        }

        return buyerId;
    }

    private String generateChatId(String user1Id, String user2Id, String productId) {
        // Сортируем ID пользователей для гарантии одинакового chatId у обоих
        String[] users = {user1Id, user2Id};
        java.util.Arrays.sort(users);
        return "chat_" + productId + "_" + users[0] + "_" + users[1];
    }

    private boolean isChatExists(String chatId) {
        try {
            String query = "SELECT COUNT(*) FROM chats WHERE id = ?";
            Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(query, new String[]{chatId});
            boolean exists = false;
            if (cursor != null && cursor.moveToFirst()) {
                exists = cursor.getInt(0) > 0;
            }
            if (cursor != null) cursor.close();
            return exists;
        } catch (Exception e) {
            Log.e("ChatFragment", "Ошибка проверки чата", e);
            return false;
        }
    }

    private void createNewChat(String otherUserId) {
        try {
            // Определяем кто покупатель, кто продавец
            String buyerId, sellerId;
            if (currentUser.getId().equals(product.getSellerId())) {
                sellerId = currentUser.getId();
                buyerId = otherUserId;
            } else {
                buyerId = currentUser.getId();
                sellerId = otherUserId;
            }

            // Создаем новый чат
            Chat chat = new Chat();
            chat.setId(chatId);
            chat.setProductId(product.getId());
            chat.setBuyerId(buyerId);
            chat.setSellerId(sellerId);
            chat.setLastMessage("Чат начат");
            chat.setCreatedAt(String.valueOf(System.currentTimeMillis()));

            boolean success = databaseHelper.createChat(chat);
            if (success) {
                Log.d("ChatFragment", "Создан новый чат: " + chatId);

                // Создаем приветственное сообщение
                createWelcomeMessage();
            }
        } catch (Exception e) {
            Log.e("ChatFragment", "Ошибка создания нового чата", e);
        }
    }

    private void createWelcomeMessage() {
        try {
            Message welcomeMsg = new Message();
            welcomeMsg.setId("welcome_" + System.currentTimeMillis());
            welcomeMsg.setChatId(chatId);
            welcomeMsg.setSenderId(currentUser.getId());
            welcomeMsg.setSenderName(currentUser.getName());
            welcomeMsg.setText("Здравствуйте! Интересует ваш товар");
            welcomeMsg.setCreatedAt(String.valueOf(System.currentTimeMillis()));

            databaseHelper.addMessage(welcomeMsg);
        } catch (Exception e) {
            Log.e("ChatFragment", "Ошибка создания приветственного сообщения", e);
        }
    }

    private void loadMessages() {
        if (chatId != null) {
            try {
                // Загружаем сообщения из БД
                List<Message> dbMessages = databaseHelper.getChatMessages(chatId);

                // Если нет сообщений, создаем тестовые
                if (dbMessages.isEmpty()) {
                    createSampleMessages();
                    dbMessages = databaseHelper.getChatMessages(chatId);
                }

                messages.clear();
                messages.addAll(dbMessages);
                messageAdapter.notifyDataSetChanged();

                // Прокручиваем к последнему сообщению
                if (!messages.isEmpty()) {
                    recyclerViewMessages.scrollToPosition(messages.size() - 1);
                }

                Log.d("ChatFragment", "Загружено " + messages.size() + " сообщений");

            } catch (Exception e) {
                Log.e("ChatFragment", "Ошибка загрузки сообщений", e);
            }
        }
    }

    private void createSampleMessages() {
        try {
            // Определяем ID другого пользователя
            String otherUserId = getOtherUserId();
            if (otherUserId == null) return;

            String otherUserName = databaseHelper.getUserNameById(otherUserId);
            if (otherUserName == null) otherUserName = "Пользователь";

            long currentTime = System.currentTimeMillis();

            // Сообщение от другого пользователя
            Message msg1 = new Message();
            msg1.setId("sample1_" + currentTime);
            msg1.setChatId(chatId);
            msg1.setSenderId(otherUserId);
            msg1.setSenderName(otherUserName);
            msg1.setText("Здравствуйте! Товар еще доступен?");
            msg1.setCreatedAt(String.valueOf(currentTime - 300000));
            databaseHelper.addMessage(msg1);

            // Ответ от текущего пользователя
            Message msg2 = new Message();
            msg2.setId("sample2_" + (currentTime + 1));
            msg2.setChatId(chatId);
            msg2.setSenderId(currentUser.getId());
            msg2.setSenderName(currentUser.getName());
            msg2.setText("Да, доступен. Можете посмотреть сегодня?");
            msg2.setCreatedAt(String.valueOf(currentTime - 200000));
            databaseHelper.addMessage(msg2);

            // Сообщение от другого пользователя
            Message msg3 = new Message();
            msg3.setId("sample3_" + (currentTime + 2));
            msg3.setChatId(chatId);
            msg3.setSenderId(otherUserId);
            msg3.setSenderName(otherUserName);
            msg3.setText("Да, после 18:00 удобно. Где встретимся?");
            msg3.setCreatedAt(String.valueOf(currentTime - 100000));
            databaseHelper.addMessage(msg3);

            // Обновляем последнее сообщение в чате
            databaseHelper.updateChatLastMessage(chatId, "Да, после 18:00 удобно. Где встретимся?");

        } catch (Exception e) {
            Log.e("ChatFragment", "Ошибка создания примерных сообщений", e);
        }
    }

    private String getOtherUserId() {
        try {
            // Получаем информацию о чате
            String query = "SELECT buyerId, sellerId FROM chats WHERE id = ?";
            Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(query, new String[]{chatId});

            if (cursor != null && cursor.moveToFirst()) {
                String buyerId = cursor.getString(cursor.getColumnIndexOrThrow("buyerId"));
                String sellerId = cursor.getString(cursor.getColumnIndexOrThrow("sellerId"));
                cursor.close();

                // Возвращаем ID другого пользователя
                return currentUser.getId().equals(buyerId) ? sellerId : buyerId;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("ChatFragment", "Ошибка получения ID другого пользователя", e);
        }
        return null;
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(getContext(), "Введите сообщение", Toast.LENGTH_SHORT).show();
            return;
        }

        if (chatId == null || currentUser == null) {
            Toast.makeText(getContext(), "Ошибка чата", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Создаем новое сообщение
            Message message = new Message();
            message.setId(UUID.randomUUID().toString());
            message.setChatId(chatId);
            message.setSenderId(currentUser.getId());
            message.setSenderName(currentUser.getName());
            message.setText(messageText);
            message.setImageUrl(null);
            message.setCreatedAt(String.valueOf(System.currentTimeMillis()));

            // Сохраняем в БД
            boolean success = databaseHelper.addMessage(message);

            if (success) {
                // Добавляем в список и обновляем UI
                messages.add(message);
                messageAdapter.notifyItemInserted(messages.size() - 1);

                // Очищаем поле ввода
                etMessage.setText("");

                // Прокручиваем к новому сообщению
                recyclerViewMessages.scrollToPosition(messages.size() - 1);

                // Обновляем последнее сообщение в чате
                databaseHelper.updateChatLastMessage(chatId, messageText);

                Toast.makeText(getContext(), "Сообщение отправлено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Ошибка отправки сообщения", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ChatFragment", "Ошибка отправки сообщения", e);
        }
    }

    private void goBack() {
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onResume() {
        super.onResume();
        // При возвращении на экран обновляем сообщения
        if (chatId != null) {
            loadMessages();
        }
    }
}