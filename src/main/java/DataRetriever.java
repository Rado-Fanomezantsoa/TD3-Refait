import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    // ================== INGREDIENT ==================
    public Ingredient findIngredientById(int ingredientId) {
        String sql = "SELECT id, name, price, category FROM ingredient WHERE id = ?";

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new RuntimeException("Ingredient not found (id=" + ingredientId + ")");
            }

            Ingredient ingredient = new Ingredient();
            ingredient.setId(rs.getInt("id"));
            ingredient.setName(rs.getString("name"));
            ingredient.setPrice(rs.getDouble("price"));
            ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
            ingredient.setStockMovementList(findStockMovementsByIngredientId(ingredientId));

            return ingredient;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<StockMovement> findStockMovementsByIngredientId(int ingredientId) {
        String sql = """
            SELECT id, quantity, type, unit, creation_datetime
            FROM public.stock_movement
            WHERE id_ingredient = ?
            ORDER BY creation_datetime
        """;

        List<StockMovement> movements = new ArrayList<>();

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StockMovement sm = new StockMovement();
                sm.setId(rs.getInt("id"));
                sm.setQuantity(rs.getDouble("quantity"));
                sm.setMovementTypeEnum(MovementTypeEnum.valueOf(rs.getString("type")));
                sm.setUnit(Unit.valueOf(rs.getString("unit")));
                sm.setCreaction_datetime(rs.getTimestamp("creation_datetime").toInstant());
                movements.add(sm);
            }

            return movements;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient saveIngredient(Ingredient ingredient) {
        try (Connection connection = new DBConnection().getConnection()) {
            connection.setAutoCommit(false);

            // Insertion ou update de l'ingrédient
            if (ingredient.getId() == null || ingredient.getId() == 0) {
                String insertIngredient = """
                    INSERT INTO ingredient(name, price, category)
                    VALUES (?, ?, ?)
                    RETURNING id
                """;

                try (PreparedStatement ps = connection.prepareStatement(insertIngredient)) {
                    ps.setString(1, ingredient.getName());
                    ps.setDouble(2, ingredient.getPrice());
                    ps.setString(3, ingredient.getCategory().name());
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    ingredient.setId(rs.getInt(1));
                }
            }

            // Ajout des mouvements de stock
            String insertMovement = """
                INSERT INTO public.stock_movement(id, id_ingredient, quantity, type, unit, creation_datetime)
                VALUES (?, ?, ?, ?::movement_type, ?::unit_type, ?)
                ON CONFLICT (id) DO NOTHING
            """;

            for (StockMovement sm : ingredient.getStockMovementList()) {
                try (PreparedStatement ps = connection.prepareStatement(insertMovement)) {
                    ps.setInt(1, getNextSerialValue(connection, "stock_movement", "id"));
                    ps.setInt(2, ingredient.getId());
                    ps.setDouble(3, sm.getQuantity());
                    ps.setString(4, sm.getMovementTypeEnum().name());
                    ps.setString(5, sm.getUnit().name());
                    ps.setTimestamp(6, Timestamp.from(sm.getCreaction_datetime()));
                    ps.executeUpdate();
                }
            }

            connection.commit();
            return ingredient;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ================== DISH ==================
    public Dish saveDish(Dish toSave) {
        String upsertDishSql = """
            INSERT INTO dish (id, selling_price, name, dish_type)
            VALUES (?, ?, ?, ?::dish_type)
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                dish_type = EXCLUDED.dish_type,
                selling_price = EXCLUDED.selling_price
            RETURNING id
        """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            int dishId;

            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                ps.setInt(1, toSave.getId() != null ? toSave.getId() : getNextSerialValue(conn, "dish", "id"));
                if (toSave.getPrice() != null) ps.setDouble(2, toSave.getPrice());
                else ps.setNull(2, Types.DOUBLE);
                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());

                ResultSet rs = ps.executeQuery();
                rs.next();
                dishId = rs.getInt(1);
            }

            detachIngredients(conn, dishId);
            attachIngredients(conn, dishId, toSave.getIngredients());

            conn.commit();
            return findDishById(dishId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void detachIngredients(Connection conn, int dishId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dishingredient WHERE id_dish = ?")) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void attachIngredients(Connection conn, int dishId, List<DishIngredient> ingredients) throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) return;

        String attachSql = """
            INSERT INTO dishingredient (id, id_ingredient, id_dish, quantity_required, unit)
            VALUES (?, ?, ?, ?, ?::unit_type)
        """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (DishIngredient di : ingredients) {
                ps.setInt(1, getNextSerialValue(conn, "dishingredient", "id"));
                ps.setInt(2, di.getIngredient().getId());
                ps.setInt(3, dishId);
                ps.setDouble(4, di.getQuantity());
                ps.setString(5, di.getUnit().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Dish findDishById(Integer id) {
        String sql = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) throw new RuntimeException("Dish not found " + id);

            Dish dish = new Dish();
            dish.setId(rs.getInt("id"));
            dish.setName(rs.getString("name"));
            dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
            dish.setPrice(rs.getObject("selling_price") == null ? null : rs.getDouble("selling_price"));
            dish.setIngredients(findIngredientByDishId(id));

            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DishIngredient> findIngredientByDishId(int dishId) {
        String sql = """
            SELECT di.id AS di_id, di.id_ingredient, di.quantity_required, di.unit,
                   i.name AS ingredient_name, i.price AS ingredient_price, i.category
            FROM dishingredient di
            JOIN ingredient i ON di.id_ingredient = i.id
            WHERE di.id_dish = ?
        """;

        List<DishIngredient> dishIngredients = new ArrayList<>();

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DishIngredient di = new DishIngredient();
                di.setId(rs.getInt("di_id"));
                di.setQuantity(rs.getDouble("quantity_required"));
                di.setUnit(Unit.valueOf(rs.getString("unit")));

                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id_ingredient"));
                ingredient.setName(rs.getString("ingredient_name"));
                ingredient.setPrice(rs.getDouble("ingredient_price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredient.setStockMovementList(findStockMovementsByIngredientId(ingredient.getId()));

                di.setIngredient(ingredient);

                Dish dish = new Dish();
                dish.setId(dishId);
                di.setDish(dish);

                dishIngredients.add(di);
            }

            return dishIngredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================== ORDER ==================
    public Order saveOrder(Order orderToSave) {
        try (Connection connection = new DBConnection().getConnection()) {
            connection.setAutoCommit(false);

            if (orderToSave.getTableOrder() == null || orderToSave.getTableOrder().getTable() == null) {
                throw new RuntimeException("La table n'est pas fournie");
            }

            RestaurantTable table = orderToSave.getTableOrder().getTable();
            Instant checkTime = orderToSave.getCreationDatetime();

            if (!isTableAvailableAt(table.getId(), checkTime)) {
                List<RestaurantTable> allTables = findAllRestaurantTables();
                List<Integer> availableNumbers = new ArrayList<>();
                for (RestaurantTable t : allTables) {
                    if (isTableAvailableAt(t.getId(), checkTime)) {
                        availableNumbers.add(t.getNumber());
                    }
                }

                String message;
                if (availableNumbers.isEmpty()) {
                    message = "Aucune table n'est disponible.";
                } else {
                    message = "La table numéro " + table.getNumber() + " n'est pas disponible. Tables disponibles : " + availableNumbers;
                }
                throw new RuntimeException(message);
            }

            String insertOrder = """
                INSERT INTO "order"(reference, creation_datetime, table_id, arrival_datetime, departure_datetime)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
            """;

            int orderId;
            try (PreparedStatement ps = connection.prepareStatement(insertOrder)) {
                ps.setString(1, orderToSave.getReference());
                ps.setTimestamp(2, Timestamp.from(orderToSave.getCreationDatetime()));
                ps.setInt(3, table.getId());
                ps.setTimestamp(4, Timestamp.from(orderToSave.getTableOrder().getArrivalDatetime()));
                ps.setTimestamp(5, Timestamp.from(orderToSave.getTableOrder().getDepartureDatetime()));
                ResultSet rs = ps.executeQuery();
                rs.next();
                orderId = rs.getInt(1);
                orderToSave.setId(orderId);
            }

            attachDishOrder(orderToSave, connection);
            connection.commit();
            return orderToSave;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void attachDishOrder(Order orderToSave, Connection connection) throws SQLException {
        String insertDishOrder = "INSERT INTO dish_order(id, order_id, dish_id, quantity) VALUES (?, ?, ?, ?)";

        for (DishOrder dishOrder : orderToSave.getDishOrders()) {
            try (PreparedStatement ps = connection.prepareStatement(insertDishOrder)) {
                ps.setInt(1, getNextSerialValue(connection, "dish_order", "id"));
                ps.setInt(2, orderToSave.getId());
                ps.setInt(3, dishOrder.getDish().getId());
                ps.setInt(4, dishOrder.getQuantity());
                ps.executeUpdate();
            }
        }
    }

    public Order findOrderByReference(String reference) {
        String sqlOrder = "SELECT id, reference, creation_datetime, table_id, arrival_datetime, departure_datetime FROM \"order\" WHERE reference = ?";

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlOrder)) {

            ps.setString(1, reference);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) throw new RuntimeException("Order not found: " + reference);

            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setReference(rs.getString("reference"));
            order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

            RestaurantTable table = findRestaurantTableById(rs.getInt("table_id"));
            TableOrder tableOrder = new TableOrder(table, rs.getTimestamp("arrival_datetime").toInstant(), rs.getTimestamp("departure_datetime").toInstant());
            order.setTableOrder(tableOrder);

            order.setDishOrders(findDishOrderByIdOrder(order.getId()));

            return order;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DishOrder> findDishOrderByIdOrder(Integer orderId) {
        String sql = "SELECT id, dish_id, quantity FROM dish_order WHERE order_id = ?";
        List<DishOrder> dishOrders = new ArrayList<>();

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DishOrder dishOrder = new DishOrder();
                dishOrder.setId(rs.getInt("id"));
                dishOrder.setQuantity(rs.getInt("quantity"));
                dishOrder.setDish(findDishById(rs.getInt("dish_id")));
                dishOrders.add(dishOrder);
            }

            return dishOrders;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================== RESTAURANT TABLE ==================
    public RestaurantTable findRestaurantTableById(Integer id) {
        String sql = "SELECT id, number FROM restaurant_table WHERE id = ?";

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            RestaurantTable table = new RestaurantTable(rs.getInt("id"), rs.getInt("number"));
            table.setOrders(findOrdersByTableId(id));  // Charge les orders pour la méthode isAvailableAt si besoin

            return table;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Order> findOrdersByTableId(int tableId) {
        String sql = "SELECT id, reference, creation_datetime, arrival_datetime, departure_datetime FROM \"order\" WHERE table_id = ?";
        List<Order> orders = new ArrayList<>();

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, tableId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setReference(rs.getString("reference"));
                order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
                TableOrder tableOrder = new TableOrder(null, rs.getTimestamp("arrival_datetime").toInstant(), rs.getTimestamp("departure_datetime").toInstant());
                order.setTableOrder(tableOrder);
                orders.add(order);
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<RestaurantTable> findAllRestaurantTables() {
        String sql = "SELECT id, number FROM restaurant_table ORDER BY number";
        List<RestaurantTable> tables = new ArrayList<>();

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RestaurantTable table = new RestaurantTable(rs.getInt("id"), rs.getInt("number"));
                table.setOrders(findOrdersByTableId(table.getId()));  // Optionnel, si besoin pour in-memory check
                tables.add(table);
            }

            return tables;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isTableAvailableAt(int tableId, Instant t) {
        String sql = """
            SELECT COUNT(*) FROM "order"
            WHERE table_id = ? AND arrival_datetime <= ? AND departure_datetime >= ?
        """;

        try (Connection connection = new DBConnection().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, tableId);
            ps.setTimestamp(2, Timestamp.from(t));
            ps.setTimestamp(3, Timestamp.from(t));
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) == 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Pour cas spécifique (*)
    public List<RestaurantTable> getAvailableTablesAt(Instant t) {
        List<RestaurantTable> available = new ArrayList<>();
        List<RestaurantTable> allTables = findAllRestaurantTables();
        for (RestaurantTable table : allTables) {
            if (isTableAvailableAt(table.getId(), t)) {
                available.add(table);
            }
        }
        return available;
    }

    // ================== UTILITAIRES ==================
    private String getSerialSequenceName(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT pg_get_serial_sequence(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName) throws SQLException {
        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) throw new IllegalArgumentException("Aucune séquence trouvée pour " + tableName + "." + columnName);

        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";
        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );
        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}