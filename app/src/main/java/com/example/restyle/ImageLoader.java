package com.example.restyle;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;
import java.io.File;
import java.util.List;

public class ImageLoader {

    // Загрузка изображения товара
    public static void loadProductImage(Context context, Product product, ImageView imageView) {
        if (product == null) {
            setPlaceholder(imageView, R.drawable.ic_placeholder);
            return;
        }

        List<String> imageList = product.getImageList();
        if (imageList != null && !imageList.isEmpty()) {
            String imagePath = imageList.get(0);
            loadImage(context, imagePath, imageView, R.drawable.ic_placeholder);
        } else {
            setPlaceholder(imageView, R.drawable.ic_placeholder);
        }
    }

    // Загрузка аватара пользователя
    public static void loadUserAvatar(Context context, User user, ImageView imageView) {
        if (user == null) {
            setPlaceholder(imageView, R.drawable.ic_profile);
            return;
        }

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Log.d("ImageLoader", "Loading user avatar: " + avatarUrl);
            loadImage(context, avatarUrl, imageView, R.drawable.ic_profile);
        } else {
            setPlaceholder(imageView, R.drawable.ic_profile);
        }
    }

    // Загрузка аватара по URL
    public static void loadUserAvatar(Context context, String avatarUrl, ImageView imageView) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            setPlaceholder(imageView, R.drawable.ic_profile);
            return;
        }

        loadImage(context, avatarUrl, imageView, R.drawable.ic_profile);
    }

    private static void loadImage(Context context, String imagePath, ImageView imageView, int placeholderResId) {
        if (imagePath == null || imagePath.isEmpty()) {
            setPlaceholder(imageView, placeholderResId);
            return;
        }

        try {
            Log.d("ImageLoader", "Loading image: " + imagePath);

            // HTTP/HTTPS URL
            if (imagePath.startsWith("http")) {
                Picasso.get()
                        .load(imagePath)
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .fit()
                        .centerCrop()
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d("ImageLoader", "✅ SUCCESS loading: " + imagePath);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("ImageLoader", "❌ ERROR loading: " + imagePath, e);
                                setPlaceholder(imageView, placeholderResId);
                            }
                        });
            }
            // FILE PATH
            else if (imagePath.startsWith("/")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    Picasso.get()
                            .load(file)
                            .placeholder(placeholderResId)
                            .error(placeholderResId)
                            .fit()
                            .centerCrop()
                            .into(imageView);
                } else {
                    Log.e("ImageLoader", "❌ File not found: " + imagePath);
                    setPlaceholder(imageView, placeholderResId);
                }
            }
            // UNKNOWN FORMAT
            else {
                Log.w("ImageLoader", "⚠️ Unknown image format: " + imagePath);
                setPlaceholder(imageView, placeholderResId);
            }

        } catch (Exception e) {
            Log.e("ImageLoader", "❌ Exception loading image: " + imagePath, e);
            setPlaceholder(imageView, placeholderResId);
        }
    }

    private static void setPlaceholder(ImageView imageView, int placeholderResId) {
        try {
            imageView.setImageResource(placeholderResId);
        } catch (Exception e) {
            Log.e("ImageLoader", "Error setting placeholder", e);
        }
    }
}