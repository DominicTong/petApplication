🌸 Pet Garden App
A Finch × Neko Atsume–inspired desktop productivity app built with Java 17 + JavaFX + MySQL.

Overview
Tab	Description
📋 Tasks	Add, tick off, and delete personal to-do tasks. Each completed task earns 10 coins.
🌿 Garden (default)	View all collected animal companions displayed on a garden background — Neko Atsume style.
🛒 Shop	Spend 50 coins to open an Animal Pack and receive 3 random animals with rarity-weighted probability.
Rarity System
Rarity	Drop Weight	Examples
🐾 Common	80–100	Cat, Dog, Rabbit, Hamster, Duck, Chick
🌟 Uncommon	40–60	Frog, Hedgehog, Penguin, Fox, Koala
✨ Rare	10–20	Owl, Deer, Peacock, Axolotl
👑 Legendary	1–3	Dragon, Unicorn, Phoenix
Prerequisites
Tool	Version	Notes
Java JDK	17 or higher	Adoptium recommended
Apache Maven	3.8+	maven.apache.org
MySQL Server	8.0+	Must be running locally
Setup
1. Clone / extract the project
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
2. Set up the MySQL database
mysql -u root -p < schema.sql
This creates the petapp database with all tables and seeds the 18 animals.

3. Configure the database connection
Edit src/main/resources/config.properties:

db.url=jdbc:mysql://localhost:3306/petapp?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_PASSWORD_HERE
If your root account has no password, leave db.password= blank.

4. Run the application
mvn javafx:run
