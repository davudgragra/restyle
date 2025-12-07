package com.example.restyle;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageUtils {

    public static List<String> saveImagesToFiles(Context context, List<Uri> imageUris) {
        List<String> savedPaths = new ArrayList<>();

        for (Uri imageUri : imageUris) {
            try {
                String filePath = saveImageToFile(context, imageUri);
                if (filePath != null) {
                    savedPaths.add(filePath);
                    Log.d("ImageUtils", "Image saved to: " + filePath);
                }
            } catch (Exception e) {
                Log.e("ImageUtils", "Error saving image", e);
            }
        }

        return savedPaths;
    }

    private static String saveImageToFile(Context context, Uri imageUri) {
        try {
            // Создаем уникальное имя файла
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "product_" + timeStamp + "_" + System.currentTimeMillis() + ".jpg";

            // Сохраняем в публичную папку Pictures
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File restyleDir = new File(storageDir, "ReStyle");

            if (!restyleDir.exists()) {
                restyleDir.mkdirs();
            }

            File imageFile = new File(restyleDir, imageFileName);

            // Копируем файл
            ContentResolver resolver = context.getContentResolver();
            InputStream inputStream = resolver.openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Возвращаем абсолютный путь
            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e("ImageUtils", "Error saving image to file", e);
            return null;
        }
    }

    public static boolean isFileExists(String filePath) {
        try {
            File file = new File(filePath);
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }
}