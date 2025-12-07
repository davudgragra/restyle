package com.example.restyle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class UserSettingsFragment extends Fragment {

    private static final String ARG_USER = "user";

    private User currentUser;
    private DatabaseHelper databaseHelper;

    private ImageView ivUserAvatar;
    private EditText etUserName, etUserEmail, etUserLocation;
    private Button btnSave, btnBack, btnChangeAvatar;

    public static UserSettingsFragment newInstance(User user) {
        UserSettingsFragment fragment = new UserSettingsFragment();
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
        databaseHelper = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        initViews(view);
        loadUserData();

        return view;
    }

    private void initViews(View view) {
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);
        etUserName = view.findViewById(R.id.etUserName);
        etUserEmail = view.findViewById(R.id.etUserEmail);
        etUserLocation = view.findViewById(R.id.etUserLocation);
        btnSave = view.findViewById(R.id.btnSave);
        btnBack = view.findViewById(R.id.btnBack);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);

        btnSave.setOnClickListener(v -> saveUserData());
        btnBack.setOnClickListener(v -> goBack());
        btnChangeAvatar.setOnClickListener(v -> changeAvatar());
    }

    private void loadUserData() {
        if (currentUser != null) {
            ImageLoader.loadUserAvatar(getContext(), currentUser, ivUserAvatar);
            etUserName.setText(currentUser.getName());
            etUserEmail.setText(currentUser.getEmail());
            etUserLocation.setText(currentUser.getLocation() != null ? currentUser.getLocation() : "");
        }
    }

    private void saveUserData() {
        String name = etUserName.getText().toString().trim();
        String email = etUserEmail.getText().toString().trim();
        String location = etUserLocation.getText().toString().trim();

        if (name.isEmpty()) {
            etUserName.setError("Введите имя");
            return;
        }

        if (email.isEmpty()) {
            etUserEmail.setError("Введите email");
            return;
        }

        try {
            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setLocation(location);

            boolean success = databaseHelper.updateUser(currentUser);

            if (success) {
                Toast.makeText(getContext(), "Данные сохранены", Toast.LENGTH_SHORT).show();
                goBack();
            } else {
                Toast.makeText(getContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void changeAvatar() {
        // Здесь можно добавить выбор изображения из галереи
        Toast.makeText(getContext(), "Функция смены аватара скоро будет доступна", Toast.LENGTH_SHORT).show();
    }

    private void goBack() {
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
