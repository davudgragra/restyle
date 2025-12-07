package com.example.restyle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "res.db";
    private static final int DATABASE_VERSION = 1; // <-- Увеличиваем версию для миграции
    private SQLiteDatabase database;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
        ensureAdminColumns();
    }
    private void ensureAdminColumns() {
        try {
            // Проверяем существование столбца isBlocked
            Cursor cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM pragma_table_info('users') WHERE name='isBlocked'", null);

            boolean isBlockedExists = false;
            if (cursor != null && cursor.moveToFirst()) {
                isBlockedExists = cursor.getInt(0) > 0;
            }
            if (cursor != null) cursor.close();

            // Если столбца нет - создаем его
            if (!isBlockedExists) {
                try {
                    database.execSQL("ALTER TABLE users ADD COLUMN isBlocked INTEGER DEFAULT 0");
                    Log.d("DatabaseHelper", "Column isBlocked added to users table");
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error adding isBlocked column", e);
                }
            }

            // Проверяем существование столбца isAdmin
            cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM pragma_table_info('users') WHERE name='isAdmin'", null);

            boolean isAdminExists = false;
            if (cursor != null && cursor.moveToFirst()) {
                isAdminExists = cursor.getInt(0) > 0;
            }
            if (cursor != null) cursor.close();

            // Если столбца нет - создаем его
            if (!isAdminExists) {
                try {
                    database.execSQL("ALTER TABLE users ADD COLUMN isAdmin INTEGER DEFAULT 0");
                    Log.d("DatabaseHelper", "Column isAdmin added to users table");
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error adding isAdmin column", e);
                }
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error ensuring admin columns", e);
        }
    }

    // Обновить статус блокировки пользователя
    public boolean updateUserBlockStatus(String userId, boolean isBlocked) {
        try {
            ensureAdminColumns(); // Убедимся, что столбцы существуют

            ContentValues values = new ContentValues();
            values.put("isBlocked", isBlocked ? 1 : 0);

            Log.d("DatabaseHelper", "Updating block status for user: " + userId +
                    ", isBlocked: " + isBlocked);

            int result = database.update("users", values, "id = ?", new String[]{userId});

            Log.d("DatabaseHelper", "Update result: " + result + " rows affected");

            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating user block status", e);
            return false;
        }
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Миграции при обновлении БД
        if (oldVersion < 2) {
            // Добавляем новые поля isAdmin и isBlocked в таблицу users
            db.execSQL("ALTER TABLE users ADD COLUMN isAdmin INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE users ADD COLUMN isBlocked INTEGER DEFAULT 0");
        }
    }

    private void createTables(SQLiteDatabase db) {
        // Таблица пользователей
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "avatarUrl TEXT, " +
                "rating REAL DEFAULT 5.0, " +
                "location TEXT, " +
                "createdAt TEXT, " +
                "isAdmin INTEGER DEFAULT 0, " +     // <-- НОВОЕ
                "isBlocked INTEGER DEFAULT 0)";      // <-- НОВОЕ
        db.execSQL(createUsersTable);

        // Таблица товаров
        String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "price TEXT, " +
                "category TEXT, " +
                "size TEXT, " +
                "condition TEXT, " +
                "images TEXT, " +
                "sellerId TEXT, " +
                "sellerName TEXT, " +
                "status TEXT DEFAULT 'available', " +
                "createdAt TEXT)";
        db.execSQL(createProductsTable);

        // Таблица чатов
        String createChatsTable = "CREATE TABLE IF NOT EXISTS chats (" +
                "id TEXT PRIMARY KEY, " +
                "productId TEXT, " +
                "buyerId TEXT, " +
                "sellerId TEXT, " +
                "lastMessage TEXT, " +
                "createdAt TEXT)";
        db.execSQL(createChatsTable);

        // Таблица сообщений
        String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages (" +
                "id TEXT PRIMARY KEY, " +
                "chatId TEXT NOT NULL, " +
                "senderId TEXT NOT NULL, " +
                "text TEXT, " +
                "imageUrl TEXT, " +
                "createdAt TEXT)";
        db.execSQL(createMessagesTable);

        Log.i("DatabaseHelper", "All tables created successfully");
    }

    // === МЕТОДЫ ДЛЯ АДМИНА ===

    // Получить всех пользователей (кроме админов)
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        Cursor cursor = null;
        try {
            // Ищем ВСЕХ пользователей, которые НЕ админы (админ только в коде)
            String query = "SELECT * FROM users " +
                    "WHERE email != 'admin@restyle.com' " +  // исключаем админа по email
                    "ORDER BY createdAt DESC";

            Log.d("DatabaseHelper", "getAllUsers query: " + query);

            cursor = database.rawQuery(query, null);

            if (cursor != null) {
                int count = cursor.getCount();
                Log.d("DatabaseHelper", "Found " + count + " users in database");

                while (cursor.moveToNext()) {
                    User user = extractUserFromCursor(cursor);
                    if (user != null) {
                        // Гарантируем, что это не админ
                        user.setAdmin(false);
                        Log.d("DatabaseHelper", "User: " + user.getName() + ", Email: " + user.getEmail());
                        users.add(user);
                    }
                }
            }
            Log.d("DatabaseHelper", "getAllUsers: Returning " + users.size() + " users");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting all users", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return users;
    }

    // Получить всех пользователей (включая админов)
    public List<User> getAllUsersWithAdmins() {
        List<User> users = new ArrayList<>();
        try {
            String query = "SELECT * FROM users ORDER BY createdAt DESC";
            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    User user = extractUserFromCursor(cursor);
                    if (user != null) {
                        users.add(user);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting all users with admins", e);
        }
        return users;
    }

    // Получить количество товаров пользователя
    public int getUserProductCount(String userId) {
        try {
            String query = "SELECT COUNT(*) FROM products WHERE sellerId = ?";
            Cursor cursor = database.rawQuery(query, new String[]{userId});
            int count = 0;
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
            return count;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user product count", e);
            return 0;
        }
    }

    // Получить все товары (для админа)
    public List<Product> getAllProductsForAdmin() {
        List<Product> products = new ArrayList<>();
        Cursor cursor = null;
        try {
            String query = "SELECT p.*, u.name as sellerName " +
                    "FROM products p " +
                    "LEFT JOIN users u ON p.sellerId = u.id " +
                    "ORDER BY p.createdAt DESC";

            cursor = database.rawQuery(query, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Product product = extractProductFromCursor(cursor);
                    if (product != null) {
                        products.add(product);
                    }
                }
            }
            Log.d("DatabaseHelper", "Loaded " + products.size() + " products for admin");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting all products for admin", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return products;
    }

    // Обновить статус блокировки пользователя
    // Обновить статус админа
    public boolean updateUserAdminStatus(String userId, boolean isAdmin) {
        try {
            ContentValues values = new ContentValues();
            values.put("isAdmin", isAdmin ? 1 : 0);

            int result = database.update("users", values, "id = ?", new String[]{userId});
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating user admin status", e);
            return false;
        }
    }

    // Удалить пользователя (админ)
    public boolean deleteUser(String userId) {
        try {
            // Сначала удаляем товары пользователя
            int productsDeleted = database.delete("products", "sellerId = ?", new String[]{userId});
            Log.d("DatabaseHelper", "Deleted " + productsDeleted + " products for user " + userId);

            // Удаляем пользователя
            int result = database.delete("users", "id = ?", new String[]{userId});
            Log.d("DatabaseHelper", "User deleted: " + userId + ", result: " + result);
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting user", e);
            return false;
        }
    }

    // Удалить товар (админ)
    public boolean deleteProductAdmin(String productId) {
        try {
            int result = database.delete("products", "id = ?", new String[]{productId});
            Log.d("DatabaseHelper", "Product deleted by admin: " + productId);
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting product by admin", e);
            return false;
        }
    }

    // Получить статистику
    public int getUserCount() {
        int count = 0;
        Cursor cursor = null;
        try {
            // Считаем всех пользователей, кроме админа по фиксированному email
            String query = "SELECT COUNT(*) FROM users WHERE email != 'admin@restyle.com'";
            cursor = database.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                Log.d("DatabaseHelper", "User count (excluding admin): " + count);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user count", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public int getProductCount() {
        int count = 0;
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) as count FROM products";
            cursor = database.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting product count", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public int getBlockedUserCount() {
        int count = 0;
        Cursor cursor = null;
        try {
            // Считаем заблокированных пользователей, кроме админа
            String query = "SELECT COUNT(*) FROM users WHERE " +
                    "(isBlocked = 1 OR isBlocked = '1') AND " +
                    "email != 'admin@restyle.com'";
            cursor = database.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                Log.d("DatabaseHelper", "Blocked user count: " + count);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting blocked user count", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }
    // Поиск пользователей
    public List<User> searchUsers(String query) {
        List<User> users = new ArrayList<>();
        try {
            String sql = "SELECT * FROM users WHERE isAdmin = 0 AND " +
                    "(name LIKE ? OR email LIKE ?) ORDER BY name";
            String searchPattern = "%" + query + "%";
            Cursor cursor = database.rawQuery(sql,
                    new String[]{searchPattern, searchPattern});

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    User user = extractUserFromCursor(cursor);
                    if (user != null) {
                        users.add(user);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error searching users", e);
        }
        return users;
    }

    // Поиск товаров
    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();
        try {
            String sql = "SELECT p.*, u.name as sellerName FROM products p " +
                    "LEFT JOIN users u ON p.sellerId = u.id " +
                    "WHERE p.title LIKE ? OR p.description LIKE ? " +
                    "OR u.name LIKE ? ORDER BY p.createdAt DESC";
            String searchPattern = "%" + query + "%";
            Cursor cursor = database.rawQuery(sql,
                    new String[]{searchPattern, searchPattern, searchPattern});

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Product product = extractProductFromCursor(cursor);
                    if (product != null) {
                        products.add(product);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error searching products", e);
        }
        return products;
    }

    // === МЕТОДЫ ДЛЯ СООБЩЕНИЙ ===

    public boolean addMessage(Message message) {
        try {
            ContentValues values = new ContentValues();
            values.put("id", message.getId());
            values.put("chatId", message.getChatId());
            values.put("senderId", message.getSenderId());
            values.put("text", message.getText());
            values.put("imageUrl", message.getImageUrl());
            values.put("createdAt", message.getCreatedAt());

            long result = database.insert("messages", null, values);
            Log.d("DatabaseHelper", "Message added: " + message.getText() + ", result: " + result);
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding message", e);
            return false;
        }
    }

    public List<Message> getChatMessages(String chatId) {
        List<Message> messages = new ArrayList<>();
        try {
            String query = "SELECT * FROM messages WHERE chatId = ? ORDER BY createdAt ASC";
            Cursor cursor = database.rawQuery(query, new String[]{chatId});

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Message message = extractMessageFromCursor(cursor);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                cursor.close();
            }
            Log.d("DatabaseHelper", "Loaded " + messages.size() + " messages for chat: " + chatId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting chat messages", e);
        }
        return messages;
    }

    private Message extractMessageFromCursor(Cursor cursor) {
        try {
            Message message = new Message();
            message.setId(getStringFromCursor(cursor, "id"));
            message.setChatId(getStringFromCursor(cursor, "chatId"));
            message.setSenderId(getStringFromCursor(cursor, "senderId"));
            message.setText(getStringFromCursor(cursor, "text"));
            message.setImageUrl(getStringFromCursor(cursor, "imageUrl"));
            message.setCreatedAt(getStringFromCursor(cursor, "createdAt"));

            // Получаем имя отправителя
            String senderName = getUserNameById(message.getSenderId());
            message.setSenderName(senderName);

            return message;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error extracting message from cursor", e);
            return null;
        }
    }

    // === МЕТОДЫ ДЛЯ ЧАТОВ ===

    public boolean createChat(Chat chat) {
        try {
            ContentValues values = new ContentValues();
            values.put("id", chat.getId());
            values.put("productId", chat.getProductId());
            values.put("buyerId", chat.getBuyerId());
            values.put("sellerId", chat.getSellerId());
            values.put("lastMessage", chat.getLastMessage());
            values.put("createdAt", chat.getCreatedAt());

            long result = database.insert("chats", null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating chat", e);
            return false;
        }
    }

    public boolean updateChatLastMessage(String chatId, String lastMessage) {
        try {
            ContentValues values = new ContentValues();
            values.put("lastMessage", lastMessage);

            int result = database.update("chats", values, "id = ?", new String[]{chatId});
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating chat last message", e);
            return false;
        }
    }

    public List<Chat> getUserChats(String userId) {
        List<Chat> chats = new ArrayList<>();
        try {
            String query = "SELECT * FROM chats WHERE buyerId = ? OR sellerId = ? ORDER BY createdAt DESC";
            Cursor cursor = database.rawQuery(query, new String[]{userId, userId});

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Chat chat = extractChatFromCursor(cursor);
                    if (chat != null) {
                        // Получаем дополнительную информацию
                        chat.setProductTitle(getProductTitleById(chat.getProductId()));
                        chat.setOtherUserName(getOtherUserName(chat, userId));
                        chats.add(chat);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user chats", e);
        }
        return chats;
    }

    private Chat extractChatFromCursor(Cursor cursor) {
        try {
            Chat chat = new Chat();
            chat.setId(getStringFromCursor(cursor, "id"));
            chat.setProductId(getStringFromCursor(cursor, "productId"));
            chat.setBuyerId(getStringFromCursor(cursor, "buyerId"));
            chat.setSellerId(getStringFromCursor(cursor, "sellerId"));
            chat.setLastMessage(getStringFromCursor(cursor, "lastMessage"));
            chat.setCreatedAt(getStringFromCursor(cursor, "createdAt"));
            return chat;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error extracting chat from cursor", e);
            return null;
        }
    }

    private String getOtherUserName(Chat chat, String currentUserId) {
        String otherUserId = chat.getBuyerId().equals(currentUserId) ? chat.getSellerId() : chat.getBuyerId();
        return getUserNameById(otherUserId);
    }

    private String getProductTitleById(String productId) {
        try {
            String query = "SELECT title FROM products WHERE id = ?";
            Cursor cursor = database.rawQuery(query, new String[]{productId});
            if (cursor != null && cursor.moveToFirst()) {
                String title = getStringFromCursor(cursor, "title");
                cursor.close();
                return title;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting product title", e);
        }
        return "Товар";
    }

    // === ОСНОВНЫЕ МЕТОДЫ ===

    public User authenticateUser(String email, String password) {
        try {
            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            Cursor cursor = database.rawQuery(query, new String[]{email, password});

            if (cursor != null && cursor.moveToFirst()) {
                User user = extractUserFromCursor(cursor);
                cursor.close();

                // Если пользователь - админ по email, устанавливаем флаг
                if (user != null && email.equals("admin@restyle.com")) {
                    user.setAdmin(true);
                } else if (user != null) {
                    user.setAdmin(false); // обычные пользователи не админы
                }

                return user;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error authenticating user", e);
        }
        return null;
    }

    public boolean addUser(User user) {
        try {
            ContentValues values = new ContentValues();
            values.put("id", user.getId());
            values.put("name", user.getName());
            values.put("email", user.getEmail());
            values.put("password", user.getPassword());
            values.put("avatarUrl", user.getAvatarUrl());
            values.put("rating", user.getRating());
            values.put("location", user.getLocation());
            values.put("createdAt", user.getCreatedAt());
            values.put("isAdmin", user.isAdmin() ? 1 : 0);
            values.put("isBlocked", user.isBlocked() ? 1 : 0);

            long result = database.insert("users", null, values);
            Log.d("DatabaseHelper", "User added: " + user.getName() + ", isAdmin: " + user.isAdmin());
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding user", e);
            return false;
        }
    }

    public User getUserById(String userId) {
        try {
            String query = "SELECT * FROM users WHERE id = ?";
            Cursor cursor = database.rawQuery(query, new String[]{userId});

            if (cursor != null && cursor.moveToFirst()) {
                User user = extractUserFromCursor(cursor);
                cursor.close();
                return user;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user by id", e);
        }
        return null;
    }

    public String getUserNameById(String userId) {
        try {
            String query = "SELECT name FROM users WHERE id = ?";
            Cursor cursor = database.rawQuery(query, new String[]{userId});

            if (cursor != null && cursor.moveToFirst()) {
                String name = getStringFromCursor(cursor, "name");
                cursor.close();
                return name;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user name by id", e);
        }
        return "Неизвестный";
    }

    public boolean updateUser(User user) {
        try {
            ContentValues values = new ContentValues();
            values.put("name", user.getName());
            values.put("email", user.getEmail());
            values.put("location", user.getLocation());
            values.put("avatarUrl", user.getAvatarUrl());
            values.put("rating", user.getRating());
            values.put("isAdmin", user.isAdmin() ? 1 : 0);
            values.put("isBlocked", user.isBlocked() ? 1 : 0);

            int result = database.update("users", values, "id = ?", new String[]{user.getId()});
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating user", e);
            return false;
        }
    }

    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        try {
            String query = "SELECT p.*, u.name as sellerName " +
                    "FROM products p " +
                    "LEFT JOIN users u ON p.sellerId = u.id " +
                    "WHERE p.status = 'available' " +
                    "ORDER BY p.createdAt DESC";

            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Product product = extractProductFromCursor(cursor);
                    if (product != null) {
                        products.add(product);

                        Log.d("DatabaseHelper", "Product: " + product.getTitle() +
                                ", Seller: " + product.getSellerName() +
                                ", Seller ID: " + product.getSellerId());
                    }
                }
                cursor.close();
            }
            Log.d("DatabaseHelper", "Total products loaded: " + products.size());
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting products", e);
        }
        return products;
    }

    public List<Product> getUserProducts(String userId) {
        List<Product> products = new ArrayList<>();
        try {
            String query = "SELECT * FROM products WHERE sellerId = ? ORDER BY createdAt DESC";
            Cursor cursor = database.rawQuery(query, new String[]{userId});

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Product product = extractProductFromCursor(cursor);
                    if (product != null) {
                        products.add(product);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user products", e);
        }
        return products;
    }

    public Product getProductById(String productId) {
        try {
            String query = "SELECT * FROM products WHERE id = ?";
            Cursor cursor = database.rawQuery(query, new String[]{productId});

            if (cursor != null && cursor.moveToFirst()) {
                Product product = extractProductFromCursor(cursor);
                cursor.close();
                return product;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting product by id", e);
        }
        return null;
    }

    public boolean addProduct(Product product) {
        try {
            ContentValues values = new ContentValues();
            values.put("id", product.getId());
            values.put("title", product.getTitle());
            values.put("description", product.getDescription());

            // ПРЕОБРАЗУЕМ цену в строку
            BigDecimal price = product.getPrice();
            if (price != null) {
                values.put("price", price.toString());
            } else {
                values.put("price", "0");
            }

            values.put("category", product.getCategory());
            values.put("size", product.getSize());
            values.put("condition", product.getCondition());
            values.put("images", product.getImages());
            values.put("sellerId", product.getSellerId());
            values.put("sellerName", product.getSellerName());
            values.put("status", "available");
            values.put("createdAt", product.getCreatedAt());

            long result = database.insert("products", null, values);
            Log.d("DatabaseHelper", "Product added: " + product.getTitle() +
                    ", price: " + (price != null ? price.toString() : "null") +
                    ", result: " + result);
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding product", e);
            Log.e("DatabaseHelper", "Product details - Title: " + product.getTitle() +
                    ", Price: " + product.getPrice() +
                    ", Price type: " + (product.getPrice() != null ? product.getPrice().getClass().getName() : "null"));
            return false;
        }
    }

    public boolean updateProduct(Product product) {
        try {
            ContentValues values = new ContentValues();
            values.put("title", product.getTitle());
            values.put("description", product.getDescription());
            values.put("price", product.getPrice().toString());
            values.put("category", product.getCategory());
            values.put("size", product.getSize());
            values.put("condition", product.getCondition());
            values.put("images", product.getImages());
            values.put("status", product.getStatus());

            int result = database.update("products", values, "id = ?", new String[]{product.getId()});
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating product", e);
            return false;
        }
    }

    public boolean deleteProduct(String productId) {
        try {
            int result = database.delete("products", "id = ?", new String[]{productId});
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting product", e);
            return false;
        }
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private User extractUserFromCursor(Cursor cursor) {
        try {
            String id = getStringFromCursor(cursor, "id");
            String name = getStringFromCursor(cursor, "name");
            String email = getStringFromCursor(cursor, "email");
            String password = getStringFromCursor(cursor, "password");
            String avatarUrl = getStringFromCursor(cursor, "avatarUrl");
            double rating = getDoubleFromCursor(cursor, "rating");
            String location = getStringFromCursor(cursor, "location");
            String createdAt = getStringFromCursor(cursor, "createdAt");

            // Новые поля с безопасным извлечением
            boolean isAdmin = false;
            boolean isBlocked = false;

            try {
                isAdmin = getBooleanFromCursor(cursor, "isAdmin");
            } catch (Exception e) {
                // Если столбец не существует или ошибка, используем значение по умолчанию
                isAdmin = false;
            }

            try {
                isBlocked = getBooleanFromCursor(cursor, "isBlocked");
            } catch (Exception e) {
                // Если столбец не существует или ошибка, используем значение по умолчанию
                isBlocked = false;
            }

            return new User(id, name, email, password, avatarUrl, rating, location, createdAt, isAdmin, isBlocked);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error extracting user from cursor", e);
            return null;
        }
    }

    public void createTestMessages(String chatId, String currentUserId, String otherUserId) {
        try {
            // Очищаем старые сообщения для этого чата
            database.delete("messages", "chatId = ?", new String[]{chatId});

            // Получаем имена пользователей
            String currentUserName = getUserNameById(currentUserId);
            String otherUserName = getUserNameById(otherUserId);

            // Создаем тестовые сообщения от ОБОИХ пользователей
            Message message1 = new Message();
            message1.setId("msg1_" + System.currentTimeMillis());
            message1.setChatId(chatId);
            message1.setSenderId(otherUserId); // ← Сообщение от ДРУГОГО пользователя
            message1.setSenderName(otherUserName);
            message1.setText("Привет! Меня интересует этот товар");
            message1.setCreatedAt(String.valueOf(System.currentTimeMillis() - 600000));

            Message message2 = new Message();
            message2.setId("msg2_" + System.currentTimeMillis());
            message2.setChatId(chatId);
            message2.setSenderId(currentUserId); // ← Сообщение от ТЕКУЩЕГО пользователя
            message2.setSenderName(currentUserName);
            message2.setText("Здравствуйте! Товар в отличном состоянии");
            message2.setCreatedAt(String.valueOf(System.currentTimeMillis() - 300000));

            Message message3 = new Message();
            message3.setId("msg3_" + System.currentTimeMillis());
            message3.setChatId(chatId);
            message3.setSenderId(otherUserId); // ← Сообщение от ДРУГОГО пользователя
            message3.setSenderName(otherUserName);
            message3.setText("Можно посмотреть сегодня?");
            message3.setCreatedAt(String.valueOf(System.currentTimeMillis() - 120000));

            // Сохраняем сообщения
            addMessage(message1);
            addMessage(message2);
            addMessage(message3);

            Log.d("DatabaseHelper", "Created test messages: 2 from other user, 1 from current user");

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating test messages", e);
        }
    }

    private Product extractProductFromCursor(Cursor cursor) {
        try {
            Product product = new Product();
            product.setId(getStringFromCursor(cursor, "id"));
            product.setTitle(getStringFromCursor(cursor, "title"));
            product.setDescription(getStringFromCursor(cursor, "description"));

            String priceStr = getStringFromCursor(cursor, "price");
            if (priceStr != null && !priceStr.isEmpty()) {
                try {
                    product.setPrice(new BigDecimal(priceStr));
                } catch (NumberFormatException e) {
                    product.setPrice(BigDecimal.ZERO);
                }
            } else {
                product.setPrice(BigDecimal.ZERO);
            }

            product.setCategory(getStringFromCursor(cursor, "category"));
            product.setSize(getStringFromCursor(cursor, "size"));
            product.setCondition(getStringFromCursor(cursor, "condition"));
            product.setImages(getStringFromCursor(cursor, "images"));
            product.setSellerId(getStringFromCursor(cursor, "sellerId"));
            product.setSellerName(getStringFromCursor(cursor, "sellerName"));
            product.setStatus(getStringFromCursor(cursor, "status"));
            product.setCreatedAt(getStringFromCursor(cursor, "createdAt"));

            return product;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error extracting product from cursor", e);
            return null;
        }
    }

    private String getStringFromCursor(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex == -1) return "";
            String value = cursor.getString(columnIndex);
            return value != null ? value : "";
        } catch (Exception e) {
            return "";
        }
    }

    public boolean checkUserExists(String email) {
        try {
            String query = "SELECT * FROM users WHERE email = ?";
            Cursor cursor = database.rawQuery(query, new String[]{email});

            if (cursor != null) {
                boolean exists = cursor.getCount() > 0;
                cursor.close();
                return exists;
            }
            return false;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking user existence", e);
            return false;
        }
    }

    public boolean updateUserPassword(String email, String newPassword) {
        try {
            ContentValues values = new ContentValues();
            values.put("password", newPassword);

            int rowsAffected = database.update("users", values, "email = ?", new String[]{email});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating password", e);
            return false;
        }
    }

    private double getDoubleFromCursor(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            return columnIndex != -1 ? cursor.getDouble(columnIndex) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private boolean getBooleanFromCursor(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            return columnIndex != -1 && cursor.getInt(columnIndex) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    // Метод для проверки, является ли пользователь администратором
    public boolean isUserAdmin(String userId) {
        try {
            String query = "SELECT isAdmin FROM users WHERE id = ?";
            Cursor cursor = database.rawQuery(query, new String[]{userId});

            if (cursor != null && cursor.moveToFirst()) {
                boolean isAdmin = getBooleanFromCursor(cursor, "isAdmin");
                cursor.close();
                return isAdmin;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking if user is admin", e);
        }
        return false;
    }

    // Метод для проверки, заблокирован ли пользователь
    public boolean isUserBlocked(String userId) {
        try {
            String query = "SELECT isBlocked FROM users WHERE id = ?";
            Cursor cursor = database.rawQuery(query, new String[]{userId});

            if (cursor != null && cursor.moveToFirst()) {
                boolean isBlocked = getBooleanFromCursor(cursor, "isBlocked");
                cursor.close();
                return isBlocked;
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking if user is blocked", e);
        }
        return false;
    }

    // Закрытие базы данных
    @Override
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.close();
    }
}