package com.example.restyle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddFragment extends Fragment {
    private static final int PICK_IMAGES_REQUEST = 1;

    private EditText etTitle, etDescription, etPrice;
    private Spinner spinnerCategory, spinnerSize, spinnerCondition;
    private Button btnAddProduct, btnSelectImages;
    private ImageView ivPreview1, ivPreview2, ivPreview3;
    private DatabaseHelper databaseHelper;
    private User currentUser;
    private List<Uri> selectedImageUris = new ArrayList<>();

    // Карта для преобразования русских значений в английские
    private static final Map<String, String> CONDITION_MAP = new HashMap<>();
    static {
        CONDITION_MAP.put("Новое", "new");
        CONDITION_MAP.put("Отличное", "excellent");
        CONDITION_MAP.put("Хорошее", "good");
        CONDITION_MAP.put("Удовлетворительное", "satisfactory");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        if (getActivity() != null) {
            currentUser = (User) getActivity().getIntent().getSerializableExtra("user");
        }

        databaseHelper = new DatabaseHelper(getContext());
        initViews(view);
        setupSpinners();
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etPrice = view.findViewById(R.id.etPrice);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerSize = view.findViewById(R.id.spinnerSize);
        spinnerCondition = view.findViewById(R.id.spinnerCondition);
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnSelectImages = view.findViewById(R.id.btnSelectImages);
        ivPreview1 = view.findViewById(R.id.ivPreview1);
        ivPreview2 = view.findViewById(R.id.ivPreview2);
        ivPreview3 = view.findViewById(R.id.ivPreview3);
    }

    private void setupSpinners() {
        String[] categories = {"Одежда", "Обувь", "Аксессуары", "Платья", "Верхняя одежда", "Джинсы", "Топы"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        String[] sizes = {"XS", "S", "M", "L", "XL", "36", "38", "40", "42", "44", "Универсальный"};
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sizes);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(sizeAdapter);

        String[] conditions = {"Новое", "Отличное", "Хорошее", "Удовлетворительное"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, conditions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(conditionAdapter);
    }

    private void setupClickListeners() {
        btnAddProduct.setOnClickListener(v -> addProduct());
        btnSelectImages.setOnClickListener(v -> selectImages());
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Выберите изображения"), PICK_IMAGES_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == getActivity().RESULT_OK) {
            selectedImageUris.clear();

            if (data != null) {
                if (data.getClipData() != null) {
                    int count = Math.min(data.getClipData().getItemCount(), 3);
                    for (int i = 0; i < count; i++) {
                        selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
                    }
                } else if (data.getData() != null) {
                    selectedImageUris.add(data.getData());
                }
                updateImagePreviews();
            }
        }
    }

    private void updateImagePreviews() {
        ivPreview1.setVisibility(View.GONE);
        ivPreview2.setVisibility(View.GONE);
        ivPreview3.setVisibility(View.GONE);

        for (int i = 0; i < Math.min(selectedImageUris.size(), 3); i++) {
            ImageView preview = null;
            switch (i) {
                case 0: preview = ivPreview1; break;
                case 1: preview = ivPreview2; break;
                case 2: preview = ivPreview3; break;
            }

            if (preview != null) {
                preview.setVisibility(View.VISIBLE);
                preview.setImageURI(selectedImageUris.get(i));
            }
        }
    }

    private void addProduct() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String size = spinnerSize.getSelectedItem().toString();
        String russianCondition = spinnerCondition.getSelectedItem().toString();

        // ПРЕОБРАЗУЕМ русское значение в английское
        String englishCondition = CONDITION_MAP.getOrDefault(russianCondition, "new");
        Log.d("AddFragment", "Condition converted: " + russianCondition + " -> " + englishCondition);

        if (title.isEmpty()) {
            etTitle.setError("Введите название");
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Введите цену");
            return;
        }

        if (currentUser == null) {
            Toast.makeText(getContext(), "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);

            Product product = new Product();
            product.setId(UUID.randomUUID().toString());
            product.setTitle(title);
            product.setDescription(description);
            product.setPrice(price);
            product.setCategory(category);
            product.setSize(size);
            product.setCondition(englishCondition); // Используем английское значение
            product.setSellerId(currentUser.getId());
            product.setSellerName(currentUser.getName());
            product.setStatus("available");
            product.setCreatedAt(String.valueOf(System.currentTimeMillis()));

            // Сохраняем изображения как файлы
            if (!selectedImageUris.isEmpty()) {
                List<String> savedPaths = ImageUtils.saveImagesToFiles(requireContext(), selectedImageUris);
                product.setImageList(savedPaths);

                Log.d("AddFragment", "=== SAVED IMAGES INFO ===");
                for (String path : savedPaths) {
                    boolean exists = ImageUtils.isFileExists(path);
                    Log.d("AddFragment", "Image path: " + path);
                    Log.d("AddFragment", "File exists: " + exists);
                }
            } else {
                product.setImageList(new ArrayList<>());
            }

            Log.d("AddFragment", "=== PRODUCT TO SAVE ===");
            Log.d("AddFragment", "Title: " + product.getTitle());
            Log.d("AddFragment", "Price: " + product.getPrice());
            Log.d("AddFragment", "Condition: " + product.getCondition() + " (original: " + russianCondition + ")");
            Log.d("AddFragment", "Category: " + product.getCategory());
            Log.d("AddFragment", "Seller: " + product.getSellerName());
            Log.d("AddFragment", "Images JSON: " + product.getImages());

            // Сохраняем в БД
            boolean success = databaseHelper.addProduct(product);

            if (success) {
                Toast.makeText(getContext(), "Товар успешно добавлен!", Toast.LENGTH_SHORT).show();
                clearForm();

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).refreshFeed();
                }
            } else {
                Toast.makeText(getContext(), "Ошибка при добавлении товара", Toast.LENGTH_SHORT).show();
                Log.e("AddFragment", "Failed to save product to database");
            }

        } catch (NumberFormatException e) {
            etPrice.setError("Введите корректную цену");
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("AddFragment", "Error adding product", e);
        }
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
        spinnerCategory.setSelection(0);
        spinnerSize.setSelection(0);
        spinnerCondition.setSelection(0);
        selectedImageUris.clear();
        updateImagePreviews();
    }
}