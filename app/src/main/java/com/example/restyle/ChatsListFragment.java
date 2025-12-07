package com.example.restyle;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatsListFragment extends Fragment {

    private static final String ARG_USER = "user";

    private User currentUser;
    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerViewChats;
    private TextView tvNoChats;
    private ChatsAdapter chatsAdapter;
    private List<Chat> chats = new ArrayList<>();

    public static ChatsListFragment newInstance(User user) {
        ChatsListFragment fragment = new ChatsListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable(ARG_USER);
        }
        databaseHelper = new DatabaseHelper(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats_list, container, false);

        initViews(view);
        loadUserChatsFromDB();

        return view;
    }

    private void initViews(View view) {
        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        tvNoChats = view.findViewById(R.id.tvNoChats);
        TextView tvBack = view.findViewById(R.id.tvBack);

        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsAdapter = new ChatsAdapter(chats, new ChatsAdapter.OnChatClickListener() {
            @Override
            public void onChatClick(Chat chat) {
                openChat(chat);
            }
        });
        recyclerViewChats.setAdapter(chatsAdapter);

        tvBack.setOnClickListener(v -> goBack());
    }

    private void loadUserChatsFromDB() {
        if (currentUser != null) {
            try {
                // Получаем все чаты пользователя из реальной БД
                List<Chat> userChats = databaseHelper.getUserChats(currentUser.getId());

                // Обогащаем чаты дополнительной информацией
                enrichChatsWithDetails(userChats);

                chats.clear();
                chats.addAll(userChats);
                chatsAdapter.notifyDataSetChanged();

                // Показываем/скрываем сообщение "нет чатов"
                if (chats.isEmpty()) {
                    tvNoChats.setVisibility(View.VISIBLE);
                    recyclerViewChats.setVisibility(View.GONE);
                    tvNoChats.setText("У вас пока нет чатов.\nНачните обсуждение товара!");
                } else {
                    tvNoChats.setVisibility(View.GONE);
                    recyclerViewChats.setVisibility(View.VISIBLE);
                }

                Log.d("ChatsListFragment", "Загружено " + chats.size() + " чатов из БД");

            } catch (Exception e) {
                Toast.makeText(getContext(), "Ошибка загрузки чатов", Toast.LENGTH_SHORT).show();
                Log.e("ChatsListFragment", "Ошибка загрузки чатов из БД", e);

                // Показываем тестовые чаты в случае ошибки
                showFallbackChats();
            }
        }
    }

    private void enrichChatsWithDetails(List<Chat> chats) {
        for (Chat chat : chats) {
            try {
                // Получаем информацию о товаре
                Product product = databaseHelper.getProductById(chat.getProductId());
                if (product != null) {
                    chat.setProductTitle(product.getTitle());
                } else {
                    chat.setProductTitle("Товар");
                }

                // Определяем ID другого пользователя
                String otherUserId = chat.getBuyerId().equals(currentUser.getId()) ?
                        chat.getSellerId() : chat.getBuyerId();

                // Получаем имя другого пользователя
                String otherUserName = databaseHelper.getUserNameById(otherUserId);
                chat.setOtherUserName(otherUserName != null ? otherUserName : "Пользователь");

            } catch (Exception e) {
                Log.e("ChatsListFragment", "Ошибка обогащения чата: " + chat.getId(), e);
            }
        }
    }

    private void showFallbackChats() {
        // Метод-заглушка на случай ошибки БД
        List<Chat> fallbackChats = new ArrayList<>();

        // Чат 1
        Chat chat1 = new Chat();
        chat1.setId("chat_fallback_1");
        chat1.setProductId("prod_1");
        chat1.setProductTitle("Красное коктейльное платье");
        chat1.setBuyerId("user2");
        chat1.setSellerId("user1");
        chat1.setOtherUserName("Анна Иванова");
        chat1.setLastMessage("Здравствуйте! Платье еще доступно?");
        chat1.setCreatedAt(String.valueOf(System.currentTimeMillis() - 86400000));
        fallbackChats.add(chat1);

        // Чат 2
        Chat chat2 = new Chat();
        chat2.setId("chat_fallback_2");
        chat2.setProductId("prod_2");
        chat2.setProductTitle("Кожаная куртка черная");
        chat2.setBuyerId("user3");
        chat2.setSellerId("user2");
        chat2.setOtherUserName("Максим Петров");
        chat2.setLastMessage("Можно ли примерить куртку?");
        chat2.setCreatedAt(String.valueOf(System.currentTimeMillis() - 43200000));
        fallbackChats.add(chat2);

        // Чат 3
        Chat chat3 = new Chat();
        chat3.setId("chat_fallback_3");
        chat3.setProductId("prod_3");
        chat3.setProductTitle("Джинсы с высокой талией");
        chat3.setBuyerId("user1");
        chat3.setSellerId("user3");
        chat3.setOtherUserName("Елена Смирнова");
        chat3.setLastMessage("Какой размер джинсов?");
        chat3.setCreatedAt(String.valueOf(System.currentTimeMillis() - 21600000));
        fallbackChats.add(chat3);

        chats.clear();
        chats.addAll(fallbackChats);
        chatsAdapter.notifyDataSetChanged();

        tvNoChats.setVisibility(View.GONE);
        recyclerViewChats.setVisibility(View.VISIBLE);

        Toast.makeText(getContext(), "Загружены примеры чатов", Toast.LENGTH_SHORT).show();
    }

    private void openChat(Chat chat) {
        if (currentUser != null) {
            try {
                // Получаем информацию о товаре из БД
                Product product = databaseHelper.getProductById(chat.getProductId());

                if (product != null) {
                    // Открываем чат с товаром
                    ChatFragment chatFragment = ChatFragment.newInstance(product, currentUser);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, chatFragment)
                            .addToBackStack("chat")
                            .commit();

                    Log.d("ChatsListFragment", "Открываем чат для товара: " + product.getTitle());
                } else {
                    Toast.makeText(getContext(), "Ошибка: товар не найден", Toast.LENGTH_SHORT).show();
                    Log.e("ChatsListFragment", "Товар не найден для чата: " + chat.getProductId());
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Ошибка открытия чата", Toast.LENGTH_SHORT).show();
                Log.e("ChatsListFragment", "Ошибка открытия чата", e);
            }
        }
    }

    private void goBack() {
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем список чатов при возвращении на экран
        loadUserChatsFromDB();
    }
}