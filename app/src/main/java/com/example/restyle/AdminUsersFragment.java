package com.example.restyle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> userList;
    private DatabaseHelper databaseHelper;
    private TextView tvNoUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        databaseHelper = new DatabaseHelper(getContext());

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        tvNoUsers = view.findViewById(R.id.tvNoUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(userList, new AdminUserAdapter.AdminUserListener() {
            @Override
            public void onBlockUser(User user) {
                boolean isBlocked = !user.isBlocked();
                if (databaseHelper.updateUserBlockStatus(user.getId(), isBlocked)) {
                    user.setBlocked(isBlocked);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(),
                            isBlocked ? "Пользователь заблокирован" : "Пользователь разблокирован",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeleteUser(User user) {
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("Удаление пользователя")
                        .setMessage(String.format("Удалить пользователя %s? Все его товары также будут удалены.", user.getName()))
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            if (databaseHelper.deleteUser(user.getId())) {
                                userList.remove(user);
                                adapter.notifyDataSetChanged();
                                updateEmptyState();
                                Toast.makeText(getContext(), "Пользователь удален", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);

        loadUsers();

        return view;
    }

    private void loadUsers() {
        userList.clear();
        userList.addAll(databaseHelper.getAllUsers());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (userList.isEmpty()) {
            tvNoUsers.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoUsers.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }
}