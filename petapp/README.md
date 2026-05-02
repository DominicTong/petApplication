# 🌸 Pet Garden App
> A Finch × Neko Atsume–inspired desktop productivity app built with **Java 17 + JavaFX + MySQL**.

---

## Overview

| Tab | Description |
|-----|-------------|
| 📋 **Tasks** | Add, tick off, and delete personal to-do tasks. Each completed task earns **10 coins**. |
| 🌿 **Garden** *(default)* | View all collected animal companions displayed on a garden background — Neko Atsume style. |
| 🛒 **Shop** | Spend **50 coins** to open an Animal Pack and receive **3 random animals** with rarity-weighted probability. |

### Rarity System
| Rarity | Drop Weight | Examples |
|--------|------------|---------|
| 🐾 Common | 80–100 | Cat, Dog, Rabbit, Hamster, Duck, Chick |
| 🌟 Uncommon | 40–60 | Frog, Hedgehog, Penguin, Fox, Koala |
| ✨ Rare | 10–20 | Owl, Deer, Peacock, Axolotl |
| 👑 Legendary | 1–3 | Dragon, Unicorn, Phoenix |

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java JDK | 17 or higher | [Adoptium](https://adoptium.net) recommended |
| Apache Maven | 3.8+ | [maven.apache.org](https://maven.apache.org) |
| MySQL Server | 8.0+ | Must be running locally |

---

## Setup

### 1. Clone / extract the project
```
pet-garden/
├── pom.xml
├── schema.sql
├── README.md
└── src/
    └── main/
        ├── java/com/petapp/
        │   ├── Main.java
        │   ├── DatabaseManager.java
        │   ├── model/  (Task, Animal, UserData)
        │   └── ui/     (MainApp, TasksTab, GardenTab, ShopTab, PackRevealDialog)
        └── resources/
            ├── config.properties
            └── styles.css
```

### 2. Set up the MySQL database
```bash
mysql -u root -p < schema.sql
```
This creates the `petapp` database with all tables and seeds the 18 animals.

### 3. Configure the database connection
Edit `src/main/resources/config.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/petapp?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_PASSWORD_HERE
```
If your root account has no password, leave `db.password=` blank.

### 4. Run the application
```bash
mvn javafx:run
```

---

## Project Structure

### `DatabaseManager.java`
Singleton that manages the MySQL connection and exposes all CRUD operations:
- **Tasks**: `getAllTasks()`, `addTask()`, `deleteTask()`, `completeTask()`
- **User data**: `getUserData()`
- **Shop/packs**: `buyPack()` — deducts coins + performs weighted-random animal draw
- **Collection**: `getUserCollection()`

### `ui/TasksTab.java`
Renders the task list. Checking a task's checkbox calls `completeTask()`, which awards coins and prevents double-rewarding via a `completed` guard.

### `ui/GardenTab.java`
Fetches the player's collection and renders each unique species as a `FlowPane` tile. Cards are sorted by rarity (Legendary first).

### `ui/ShopTab.java`
Shows the pack card and current balance. The buy button is automatically disabled when the player can't afford a pack.

### `ui/PackRevealDialog.java`
Modal dialog that uses JavaFX `SequentialTransition` + `FadeTransition` + `ScaleTransition` to reveal each drawn animal card one by one.

---

## Database Schema

```
user_data        — coins, total_tasks_completed (single row, id=1)
tasks            — id, title, completed, created_at, completed_at
animals          — id, name, emoji, rarity, description, drop_weight
user_collection  — id, animal_id, obtained_at
```

---

## Extending the Project (Dissertation Ideas)

- **User accounts** — add a `users` table and login screen
- **Multiple pack types** — e.g. "Rare Pack" (guaranteed ≥ Uncommon) for 150 coins
- **Animal interactions** — track favourite spots, visit counts (full Neko Atsume)
- **Streak system** — bonus coins for completing tasks N days in a row
- **Statistics screen** — chart coins earned over time using JavaFX Charts
- **Sound effects** — use JavaFX `MediaPlayer` for pack-open sounds
