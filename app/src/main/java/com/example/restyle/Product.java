package com.example.restyle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Product implements Serializable {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String size;
    private String condition;
    private String images; // JSON строка или просто список URL через запятую
    private String sellerId;
    private String sellerName;
    private String status;
    private String createdAt;

    public Product() {}

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Методы для работы с изображениями
    public List<String> getImageList() {
        List<String> imageList = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            try {
                // Пытаемся разобрать как JSON
                if (images.trim().startsWith("[")) {
                    // JSON формат: ["url1", "url2"]
                    String cleanJson = images.replace("[", "").replace("]", "").replace("\"", "");
                    String[] imagesArray = cleanJson.split(",");
                    for (String image : imagesArray) {
                        if (!image.trim().isEmpty()) {
                            imageList.add(image.trim());
                        }
                    }
                } else {
                    // Простой формат: url1,url2,url3
                    String[] imagesArray = images.split(",");
                    for (String image : imagesArray) {
                        if (!image.trim().isEmpty()) {
                            imageList.add(image.trim());
                        }
                    }
                }
            } catch (Exception e) {
                // Если произошла ошибка, пробуем просто взять всю строку как одно изображение
                imageList.add(images.trim());
            }
        }
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        if (imageList != null && !imageList.isEmpty()) {
            // Сохраняем как JSON массив для единообразия
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < imageList.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(imageList.get(i)).append("\"");
            }
            sb.append("]");
            this.images = sb.toString();
        } else {
            this.images = "";
        }
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }
}