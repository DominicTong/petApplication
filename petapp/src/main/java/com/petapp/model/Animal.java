package com.petapp.model;

/**
 * Represents one species of animal in the game.
 * The same class is reused for the global pool AND for the player's
 * collection — in the collection context {@code count} is how many
 * copies the player owns.
 */
public class Animal {

    private final int    id;
    private final String name;
    private final String emoji;
    private final String rarity;       // "COMMON" | "UNCOMMON" | "RARE" | "LEGENDARY"
    private final String description;
    private final int    dropWeight;   // higher = more common
    private       int    count;        // copies owned (used in collection view)

    public Animal(int id, String name, String emoji,
                  String rarity, String description, int dropWeight) {
        this.id          = id;
        this.name        = name;
        this.emoji       = emoji;
        this.rarity      = rarity;
        this.description = description;
        this.dropWeight  = dropWeight;
        this.count       = 0;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int    getId()          { return id; }
    public String getName()        { return name; }
    public String getEmoji()       { return emoji; }
    public String getRarity()      { return rarity; }
    public String getDescription() { return description; }
    public int    getDropWeight()  { return dropWeight; }
    public int    getCount()       { return count; }
    public void   setCount(int c)  { this.count = c; }

    /**
     * Returns a friendly label used in the reveal animation.
     */
    public String getRarityLabel() {
        return switch (rarity) {
            case "LEGENDARY" -> "👑 Legendary";
            case "RARE"      -> "✨ Rare";
            case "UNCOMMON"  -> "🌟 Uncommon";
            default          -> "🐾 Common";
        };
    }
}
