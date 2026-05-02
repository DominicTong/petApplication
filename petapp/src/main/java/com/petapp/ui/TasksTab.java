package com.petapp.ui;

import com.petapp.DatabaseManager;
import com.petapp.model.Task;
import com.petapp.model.UserData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Tasks tab — the player's to-do list.
 *
 * Features:
 *  • Add tasks via a text field
 *  • Tick a checkbox to mark a task complete → awards coins
 *  • Delete incomplete tasks with the trash button
 *  • Live coin balance shown in the header
 */
public class TasksTab {

    private final VBox      root;
    private final VBox      taskListBox;
    private final Label     coinLabel;
    private final TextField taskInput;

    public TasksTab() {
        root = new VBox(16);
        root.setPadding(new Insets(28, 28, 16, 28));
        root.getStyleClass().add("tab-content");

        // ── Header row ────────────────────────────────────────────────────────
        Label title = new Label("📋  My Tasks");
        title.getStyleClass().add("page-title");

        coinLabel = new Label("🪙  0 coins");
        coinLabel.getStyleClass().add("coin-badge");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox header = new HBox(12, title, headerSpacer, coinLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        // ── Subtitle ─────────────────────────────────────────────────────────
        Label subtitle = new Label(
                "Complete tasks to earn 🪙" + DatabaseManager.TASK_COIN_REWARD +
                " coins each. Spend coins in the Shop!");
        subtitle.getStyleClass().add("subtitle");

        // ── Add-task bar ─────────────────────────────────────────────────────
        taskInput = new TextField();
        taskInput.setPromptText("What do you need to do today?");
        taskInput.getStyleClass().add("task-input");
        HBox.setHgrow(taskInput, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Task");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> handleAddTask());
        taskInput.setOnAction(e -> handleAddTask());   // Enter key also adds

        HBox addBar = new HBox(10, taskInput, addBtn);
        addBar.setAlignment(Pos.CENTER_LEFT);

        // ── Scrollable task list ──────────────────────────────────────────────
        taskListBox = new VBox(8);
        taskListBox.getStyleClass().add("task-list");

        ScrollPane scroll = new ScrollPane(taskListBox);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("task-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, subtitle, addBar, scroll);
        refresh();
    }

    /** Returns the root node to embed in the TabPane. */
    public Node getContent() { return root; }

    /** Reloads data from the database and re-renders the task list. */
    public void refresh() {
        try {
            UserData data = DatabaseManager.getInstance().getUserData();
            coinLabel.setText("🪙  " + data.getCoins() + " coins");

            List<Task> tasks = DatabaseManager.getInstance().getAllTasks();
            taskListBox.getChildren().clear();

            if (tasks.isEmpty()) {
                Label empty = new Label(
                        "No tasks yet! 🌱\nAdd your first task above to get started.");
                empty.getStyleClass().add("empty-label");
                empty.setWrapText(true);
                taskListBox.getChildren().add(empty);
            } else {
                for (Task task : tasks) {
                    taskListBox.getChildren().add(buildTaskRow(task));
                }
            }
        } catch (SQLException e) {
            showError("Failed to load tasks", e.getMessage());
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private Node buildTaskRow(Task task) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.getStyleClass().add(task.isCompleted() ? "task-row-completed" : "task-row");

        // Checkbox
        CheckBox cb = new CheckBox();
        cb.setSelected(task.isCompleted());
        cb.setDisable(task.isCompleted());   // can't un-complete

        if (!task.isCompleted()) {
            cb.setOnAction(e -> {
                try {
                    boolean rewarded = DatabaseManager.getInstance().completeTask(task.getId());
                    if (rewarded) showCoinReward(DatabaseManager.TASK_COIN_REWARD);
                    refresh();
                } catch (SQLException ex) {
                    showError("Could not complete task", ex.getMessage());
                }
            });
        }

        // Title label
        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add(task.isCompleted() ? "task-title-done" : "task-title");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(cb, titleLabel, spacer);

        if (task.isCompleted()) {
            Label doneBadge = new Label("✓ Done");
            doneBadge.getStyleClass().add("done-badge");
            row.getChildren().add(doneBadge);
        } else {
            Button deleteBtn = new Button("🗑");
            deleteBtn.getStyleClass().add("btn-delete");
            deleteBtn.setTooltip(new Tooltip("Delete this task"));
            deleteBtn.setOnAction(e -> {
                try {
                    DatabaseManager.getInstance().deleteTask(task.getId());
                    refresh();
                } catch (SQLException ex) {
                    showError("Could not delete task", ex.getMessage());
                }
            });
            row.getChildren().add(deleteBtn);
        }

        return row;
    }

    private void handleAddTask() {
        String text = taskInput.getText().trim();
        if (text.isEmpty()) return;
        try {
            DatabaseManager.getInstance().addTask(text);
            taskInput.clear();
            refresh();
        } catch (SQLException e) {
            showError("Could not add task", e.getMessage());
        }
    }

    private void showCoinReward(int amount) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task Complete! 🎉");
        alert.setHeaderText(null);
        alert.setContentText(
                "Great work! You earned 🪙 " + amount + " coins.\n" +
                "Head to the Shop to open a pack!");
        alert.show();   // non-blocking
    }

    private void showError(String title, String detail) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(detail);
        alert.show();
    }
}
