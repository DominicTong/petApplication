package com.petapp;

import com.petapp.model.Animal;
import com.petapp.model.Task;
import com.petapp.model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Singleton that owns the MySQL connection and exposes all database
 * operations needed by the application.
 *
 * Connection settings are read from {@code config.properties} on the
 * classpath (see {@code src/main/resources/config.properties}).
 */
public class DatabaseManager {

    // ── Constants ──────────────────────────────────────────────────────────────
    public static final int TASK_COIN_REWARD  = 10;   // coins per completed task
    public static final int PACK_COST         = 50;   // coins per pack purchase
    public static final int ANIMALS_PER_PACK  = 3;    // animals drawn per pack

    // ── Singleton ──────────────────────────────────────────────────────────────
    private static DatabaseManager instance;

    private Connection connection;
    private String     dbUrl;
    private String     dbUser;
    private String     dbPassword;

    private DatabaseManager() {
        Properties props = loadConfig();
        dbUrl      = props.getProperty("db.url",      "jdbc:mysql://localhost:3306/petapp");
        dbUser     = props.getProperty("db.user",     "root");
        dbPassword = props.getProperty("db.password", "");
        connect();
    }

    /** Thread-safe lazy initialiser. */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    // ── Connection management ──────────────────────────────────────────────────

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader()
                                        .getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                System.err.println("[Warning] config.properties not found — using defaults.");
            }
        } catch (IOException e) {
            System.err.println("[Warning] Could not read config.properties: " + e.getMessage());
        }
        return props;
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("[DB] Connected to MySQL successfully.");
        } catch (SQLException e) {
            throw new RuntimeException(
                "Could not connect to MySQL.\n" +
                "• Is MySQL running on localhost:3306?\n" +
                "• Have you run schema.sql?\n" +
                "• Check src/main/resources/config.properties\n\n" +
                "SQL error: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if the connection is alive and reconnects if needed.
     * Call at the start of every public method to handle dropped connections.
     */
    private void ensureConnected() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            System.out.println("[DB] Reconnecting...");
            connect();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TASK OPERATIONS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns all tasks ordered so incomplete ones come first,
     * then ordered by creation date (newest first).
     */
    public List<Task> getAllTasks() throws SQLException {
        ensureConnected();
        List<Task> tasks = new ArrayList<>();
        String sql = """
                SELECT id, title, completed, created_at
                FROM   tasks
                ORDER  BY completed ASC, created_at DESC
                """;
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getBoolean("completed"),
                        rs.getTimestamp("created_at")));
            }
        }
        return tasks;
    }

    /** Inserts a new incomplete task. */
    public void addTask(String title) throws SQLException {
        ensureConnected();
        String sql = "INSERT INTO tasks (title) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, title.trim());
            ps.executeUpdate();
        }
    }

    /** Deletes a task (works whether complete or not). */
    public void deleteTask(int id) throws SQLException {
        ensureConnected();
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Marks a task complete and awards coins.
     *
     * @return {@code true} if the task was newly completed and coins were awarded;
     *         {@code false} if it was already marked complete (no double-award).
     */
    public boolean completeTask(int taskId) throws SQLException {
        ensureConnected();

        // Guard: don't award twice
        String check = "SELECT completed FROM tasks WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(check)) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getBoolean("completed")) return false;
        }

        // Mark complete
        String markDone = "UPDATE tasks SET completed = TRUE, completed_at = NOW() WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(markDone)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        }

        // Award coins + increment counter in a single statement
        String reward = """
                UPDATE user_data
                SET    coins = coins + ?,
                       total_tasks_completed = total_tasks_completed + 1
                WHERE  id = 1
                """;
        try (PreparedStatement ps = connection.prepareStatement(reward)) {
            ps.setInt(1, TASK_COIN_REWARD);
            ps.executeUpdate();
        }

        return true;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  USER DATA
    // ══════════════════════════════════════════════════════════════════════════

    /** Fetches the current coin balance and stats for the player. */
    public UserData getUserData() throws SQLException {
        ensureConnected();
        String sql = "SELECT coins, total_tasks_completed FROM user_data WHERE id = 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new UserData(rs.getInt("coins"),
                                    rs.getInt("total_tasks_completed"));
            }
        }
        return new UserData(0, 0);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SHOP / PACK SYSTEM
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Attempts to purchase a pack.
     * Deducts {@link #PACK_COST} coins, draws {@link #ANIMALS_PER_PACK} animals
     * using weighted-random selection, saves them to the collection, and returns
     * the drawn animals.
     *
     * @return the list of drawn {@link Animal} objects, or {@code null} if the
     *         player cannot afford the pack.
     */
    public List<Animal> buyPack() throws SQLException {
        ensureConnected();

        // Affordability check
        UserData data = getUserData();
        if (data.getCoins() < PACK_COST) return null;

        // Deduct cost
        String deduct = "UPDATE user_data SET coins = coins - ? WHERE id = 1";
        try (PreparedStatement ps = connection.prepareStatement(deduct)) {
            ps.setInt(1, PACK_COST);
            ps.executeUpdate();
        }

        // Draw animals
        return drawAnimals(ANIMALS_PER_PACK);
    }

    /**
     * Weighted-random draw from the full animal pool.
     * Each animal's {@code drop_weight} is its relative probability.
     */
    private List<Animal> drawAnimals(int count) throws SQLException {
        List<Animal> pool        = getAnimalPool();
        int          totalWeight = pool.stream().mapToInt(Animal::getDropWeight).sum();
        Random       rng         = new Random();
        List<Animal> drawn       = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int roll       = rng.nextInt(totalWeight);
            int cumulative = 0;
            for (Animal animal : pool) {
                cumulative += animal.getDropWeight();
                if (roll < cumulative) {
                    drawn.add(animal);
                    addAnimalToCollection(animal.getId());
                    break;
                }
            }
        }
        return drawn;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ANIMAL POOL & COLLECTION
    // ══════════════════════════════════════════════════════════════════════════

    /** Returns every animal row (used for the weighted draw). */
    private List<Animal> getAnimalPool() throws SQLException {
        List<Animal> pool = new ArrayList<>();
        String sql = "SELECT id, name, emoji, rarity, description, drop_weight FROM animals";
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pool.add(mapAnimal(rs));
            }
        }
        return pool;
    }

    /**
     * Returns the player's unique animals, each annotated with how many
     * copies they own ({@link Animal#getCount()}).
     * Sorted: Legendary → Rare → Uncommon → Common, then alphabetically.
     */
    public List<Animal> getUserCollection() throws SQLException {
        ensureConnected();
        List<Animal> collection = new ArrayList<>();
        String sql = """
                SELECT   a.id, a.name, a.emoji, a.rarity, a.description,
                         a.drop_weight, COUNT(uc.id) AS cnt
                FROM     animals a
                JOIN     user_collection uc ON a.id = uc.animal_id
                GROUP BY a.id, a.name, a.emoji, a.rarity, a.description, a.drop_weight
                ORDER BY FIELD(a.rarity, 'LEGENDARY','RARE','UNCOMMON','COMMON'),
                         a.name ASC
                """;
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Animal a = mapAnimal(rs);
                a.setCount(rs.getInt("cnt"));
                collection.add(a);
            }
        }
        return collection;
    }

    /** Inserts one copy of an animal into the player's collection. */
    private void addAnimalToCollection(int animalId) throws SQLException {
        String sql = "INSERT INTO user_collection (animal_id) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, animalId);
            ps.executeUpdate();
        }
    }

    /** Helper: maps the current ResultSet row to an {@link Animal}. */
    private Animal mapAnimal(ResultSet rs) throws SQLException {
        return new Animal(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("emoji"),
                rs.getString("rarity"),
                rs.getString("description"),
                rs.getInt("drop_weight"));
    }
}
