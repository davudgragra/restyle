package com.example.restyle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdminPagerAdapter extends FragmentStateAdapter {

    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AdminUsersFragment();
            case 1:
                return new AdminProductsFragment();
            default:
                return new AdminUsersFragment(); // Просто возвращаем UsersFragment как запасной вариант
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Только 2 вкладки: Пользователи и Товары
    }
}