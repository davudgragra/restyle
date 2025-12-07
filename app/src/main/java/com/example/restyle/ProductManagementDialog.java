package com.example.restyle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ProductManagementDialog extends DialogFragment {

    private static final String ARG_PRODUCT = "product";

    private Product product;
    private DatabaseHelper databaseHelper;
    private OnProductUpdateListener listener;

    public interface OnProductUpdateListener {
        void onProductUpdated(Product updatedProduct);
        void onProductDeleted(String productId);
    }

    public static ProductManagementDialog newInstance(Product product) {
        ProductManagementDialog dialog = new ProductManagementDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        dialog.setArguments(args);
        return dialog;
    }

    public void setProductUpdateListener(OnProductUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
        databaseHelper = new DatabaseHelper(getContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_product_management, null);

        TextView tvProductTitle = view.findViewById(R.id.tvProductTitle);
        Spinner spinnerStatus = view.findViewById(R.id.spinnerStatus);
        Button btnUpdate = view.findViewById(R.id.btnUpdate);
        Button btnDelete = view.findViewById(R.id.btnDelete);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        tvProductTitle.setText(product.getTitle());

        // Устанавливаем текущий статус
        if (product.getStatus() != null) {
            String[] statusArray = getResources().getStringArray(R.array.product_statuses);
            for (int i = 0; i < statusArray.length; i++) {
                if (statusArray[i].equals(product.getStatus())) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        }

        btnUpdate.setOnClickListener(v -> updateProductStatus(spinnerStatus.getSelectedItem().toString()));
        btnDelete.setOnClickListener(v -> deleteProduct());
        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void updateProductStatus(String newStatus) {
        try {
            product.setStatus(newStatus);
            boolean success = databaseHelper.updateProduct(product);

            if (success) {
                Toast.makeText(getContext(), "Статус товара обновлен", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onProductUpdated(product);
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "Ошибка обновления", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void deleteProduct() {
        new AlertDialog.Builder(getContext())
                .setTitle("Удаление товара")
                .setMessage("Вы уверены, что хотите удалить этот товар?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    try {
                        boolean success = databaseHelper.deleteProduct(product.getId());
                        if (success) {
                            Toast.makeText(getContext(), "Товар удален", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onProductDeleted(product.getId());
                            }
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}