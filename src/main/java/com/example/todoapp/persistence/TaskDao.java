package com.example.todoapp.persistence;

import com.example.todoapp.business.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDao {

    private static final Logger log = LoggerFactory.getLogger(TaskDao.class);

    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    public TaskDao() {

        // Réinitialise complètement la table à chaque lancement
        dropTable();

        initTable();

        seedData();
    }

    private void dropTable() {

        String sql = "DROP TABLE IF EXISTS tasks";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

            log.info("Old table deleted.");

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to drop table",
                    e
            );
        }
    }

    private void initTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY,
                    title TEXT NOT NULL,
                    description TEXT,
                    done INTEGER NOT NULL DEFAULT 0
                )
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

            log.info("Table recreated.");

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to create table",
                    e
            );
        }
    }

    private void seedData() {

        save(new Task(
                null,
                "Réviser DS de maths",
                "Séries numériques et probabilités.",
                false
        ));

        save(new Task(
                null,
                "Valider mon PIVE",
                "PIVE Club Poker.",
                true
        ));

        save(new Task(
                null,
                "Choisir mon parcours de 4A",
                "SIR ou SIA ?",
                false
        ));

        log.info("Default tasks inserted.");
    }

    public Task save(Task task) {

        int newId = getNextAvailableId();

        String sql = """
                INSERT INTO tasks (id, title, description, done)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newId);
            ps.setString(2, task.title());
            ps.setString(3, task.description());
            ps.setInt(4, task.done() ? 1 : 0);

            ps.executeUpdate();

            return new Task(
                    newId,
                    task.title(),
                    task.description(),
                    task.done()
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save task",
                    e
            );
        }
    }

    private int getNextAvailableId() {

        String sql = """
                SELECT id
                FROM tasks
                ORDER BY id
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int expectedId = 1;

            while (rs.next()) {

                int currentId = rs.getInt("id");

                // Trou trouvé
                if (currentId != expectedId) {
                    return expectedId;
                }

                expectedId++;
            }

            return expectedId;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to get next available id",
                    e
            );
        }
    }

    public Optional<Task> findById(int id) {

        String sql = """
                SELECT id, title, description, done
                FROM tasks
                WHERE id = ?
                """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find task",
                    e
            );
        }

        return Optional.empty();
    }

    public List<Task> findAll() {

        String sql = """
                SELECT id, title, description, done
                FROM tasks
                ORDER BY id
                """;

        return query(sql);
    }

    public List<Task> findAllTodo() {

        String sql = """
                SELECT id, title, description, done
                FROM tasks
                WHERE done = 0
                ORDER BY id
                """;

        return query(sql);
    }

    public boolean update(int id, Task task) {

        String sql = """
                UPDATE tasks
                SET title = ?, description = ?, done = ?
                WHERE id = ?
                """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.title());
            ps.setString(2, task.description());
            ps.setInt(3, task.done() ? 1 : 0);
            ps.setInt(4, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update task",
                    e
            );
        }
    }

    public boolean deleteById(int id) {

        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to delete task",
                    e
            );
        }
    }

    public void deleteAll() {

        String sql = "DELETE FROM tasks";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

            log.info("All tasks deleted.");

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to delete all tasks",
                    e
            );
        }
    }

    public int count() {

        String sql = "SELECT COUNT(*) FROM tasks";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to count tasks",
                    e
            );
        }
    }


    private Connection connect() throws SQLException {

        return DriverManager.getConnection(DB_URL);
    }

    private List<Task> query(String sql) {

        List<Task> result = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                result.add(mapRow(rs));
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed query",
                    e
            );
        }

        return result;
    }

    private Task mapRow(ResultSet rs) throws SQLException {

        return new Task(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("done") == 1
        );
    }
}