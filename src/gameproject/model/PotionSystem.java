package gameproject.model;

import java.util.*;

/**
 * PotionSystem manages potion recipes, ingredients, and effects for the TimSort RPG game.
 */
public class PotionSystem {
    // Singleton instance
    private static PotionSystem instance;
    
    // Maps to store potion data
    private Map<String, Potion> potions;
    private Map<String, Ingredient> ingredients;
    private Map<String, List<String>> bossWeaknesses;
    
    /**
     * Potion class representing a craftable potion and its properties
     */
    public class Potion {
        private String name;
        private String description;
        private List<String> ingredientNames;
        private String imagePath;
        private String effectType;
        
        public Potion(String name, String description, List<String> ingredientNames, String imagePath, String effectType) {
            this.name = name;
            this.description = description;
            this.ingredientNames = ingredientNames;
            this.imagePath = imagePath;
            this.effectType = effectType;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public List<String> getIngredientNames() {
            return ingredientNames;
        }
        
        public String getImagePath() {
            return imagePath;
        }
        
        public String getEffectType() {
            return effectType;
        }
    }
    
    /**
     * Ingredient class representing a potion ingredient
     */
    public class Ingredient {
        private String name;
        private int value;
        private String color;
        private String imagePath;
        
        public Ingredient(String name, int value, String color, String imagePath) {
            this.name = name;
            this.value = value;
            this.color = color;
            this.imagePath = imagePath;
        }
        
        public String getName() {
            return name;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getColor() {
            return color;
        }
        
        public String getImagePath() {
            return imagePath;
        }
    }
    
    /**
     * Private constructor for singleton
     */
    private PotionSystem() {
        potions = new HashMap<>();
        ingredients = new HashMap<>();
        bossWeaknesses = new HashMap<>();
        
        initializeIngredients();
        initializePotions();
        initializeBossWeaknesses();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized PotionSystem getInstance() {
        if (instance == null) {
            instance = new PotionSystem();
        }
        return instance;
    }
    
    /**
     * Initialize all ingredients
     */
    private void initializeIngredients() {
        // Fire Resistance Potion Ingredients
        ingredients.put("Pumpkin", new Ingredient("Pumpkin", 1, "orange", "/gameproject/resources/ingredients/pumpkin.png"));
        ingredients.put("Apples", new Ingredient("Apples", 2, "red", "/gameproject/resources/ingredients/apples.png"));
        ingredients.put("Peppers", new Ingredient("Peppers", 3, "red", "/gameproject/resources/ingredients/peppers.png"));
        ingredients.put("Dragon Fire Glands", new Ingredient("Dragon Fire Glands", 4, "orange", "/gameproject/resources/ingredients/dragon_fire_glands.png"));
        ingredients.put("Fire Crystal", new Ingredient("Fire Crystal", 5, "red", "/gameproject/resources/ingredients/fire_crystal.png"));
        
        // Cold Resistance Potion Ingredients
        ingredients.put("Strawberries", new Ingredient("Strawberries", 6, "red", "/gameproject/resources/ingredients/strawberries.png"));
        ingredients.put("Wasabi", new Ingredient("Wasabi", 7, "green", "/gameproject/resources/ingredients/wasabi.png"));
        ingredients.put("Mint", new Ingredient("Mint", 8, "green", "/gameproject/resources/ingredients/mint.png"));
        ingredients.put("Dragon Ice Glands", new Ingredient("Dragon Ice Glands", 9, "blue", "/gameproject/resources/ingredients/dragon_ice_glands.png"));
        ingredients.put("Ice Crystal", new Ingredient("Ice Crystal", 10, "blue", "/gameproject/resources/ingredients/ice_crystal.png"));
        
        // Strength Potion Ingredients
        ingredients.put("Corn", new Ingredient("Corn", 11, "yellow", "/gameproject/resources/ingredients/corn.png"));
        ingredients.put("Powdered Giant Insect", new Ingredient("Powdered Giant Insect", 12, "brown", "/gameproject/resources/ingredients/powdered_giant_insect.png"));
        ingredients.put("Troll Sweat", new Ingredient("Troll Sweat", 13, "green", "/gameproject/resources/ingredients/troll_sweat.png"));
        ingredients.put("Powdered Minotaur Horn", new Ingredient("Powdered Minotaur Horn", 14, "brown", "/gameproject/resources/ingredients/powdered_minotaur_horn.png"));
        ingredients.put("Dragon Bone", new Ingredient("Dragon Bone", 15, "white", "/gameproject/resources/ingredients/dragon_bone.png"));
        
        // Dexterity Potion Ingredients
        ingredients.put("Banana Leaf", new Ingredient("Banana Leaf", 16, "green", "/gameproject/resources/ingredients/banana_leaf.png"));
        ingredients.put("Maple Sap", new Ingredient("Maple Sap", 17, "amber", "/gameproject/resources/ingredients/maple_sap.png"));
        ingredients.put("Powdered Jackalope Antlers", new Ingredient("Powdered Jackalope Antlers", 18, "brown", "/gameproject/resources/ingredients/powdered_jackalope_antlers.png"));
        ingredients.put("Griffon Feathers", new Ingredient("Griffon Feathers", 19, "gold", "/gameproject/resources/ingredients/griffon_feathers.png"));
        ingredients.put("Dragon Sinew", new Ingredient("Dragon Sinew", 20, "red", "/gameproject/resources/ingredients/dragon_sinew.png"));
    }
    
    /**
     * Initialize all potions
     */
    private void initializePotions() {
        // Fire Resistance Potion
        List<String> fireResistIngredients = Arrays.asList(
            "Pumpkin", "Apples", "Peppers", "Dragon Fire Glands", "Fire Crystal"
        );
        potions.put("Fire Resistance Potion", new Potion(
            "Fire Resistance Potion", 
            "Reduces fire damage taken.", 
            fireResistIngredients,
            "/gameproject/resources/potions/fire_resistance_potion.png",
            "fire_resistance"
        ));
        
        // Cold Resistance Potion
        List<String> coldResistIngredients = Arrays.asList(
            "Strawberries", "Wasabi", "Mint", "Dragon Ice Glands", "Ice Crystal"
        );
        potions.put("Cold Resistance Potion", new Potion(
            "Cold Resistance Potion", 
            "Reduces cold damage taken.", 
            coldResistIngredients,
            "/gameproject/resources/potions/cold_resistance_potion.png",
            "cold_resistance"
        ));
        
        // Strength Potion
        List<String> strengthIngredients = Arrays.asList(
            "Corn", "Powdered Giant Insect", "Troll Sweat", "Powdered Minotaur Horn", "Dragon Bone"
        );
        potions.put("Strength Potion", new Potion(
            "Strength Potion", 
            "Temporarily enhances physical power.", 
            strengthIngredients,
            "/gameproject/resources/potions/strength_potion.png",
            "strength"
        ));
        
        // Dexterity Potion
        List<String> dexterityIngredients = Arrays.asList(
            "Banana Leaf", "Maple Sap", "Powdered Jackalope Antlers", "Griffon Feathers", "Dragon Sinew"
        );
        potions.put("Dexterity Potion", new Potion(
            "Dexterity Potion", 
            "Enhances agility and reflexes.", 
            dexterityIngredients,
            "/gameproject/resources/potions/dexterity_potion.png",
            "dexterity"
        ));
    }
    
    /**
     * Initialize boss weaknesses
     */
    private void initializeBossWeaknesses() {
        // Flameclaw (fire boss) is weak to Fire Resistance Potion
        bossWeaknesses.put("Flameclaw", Arrays.asList("Fire Resistance Potion"));
        
        // Toxitar (poison boss) is weak to Dexterity Potion (to avoid poison) and Strength Potion
        bossWeaknesses.put("Toxitar", Arrays.asList("Dexterity Potion"));
        
        // Lord Chaosa (final boss) is weak to Cold Resistance Potion
        bossWeaknesses.put("LordChaosa", Arrays.asList("Cold Resistance Potion"));
    }
    
    /**
     * Get all potions
     */
    public List<Potion> getAllPotions() {
        return new ArrayList<>(potions.values());
    }
    
    /**
     * Get all ingredients
     */
    public List<Ingredient> getAllIngredients() {
        return new ArrayList<>(ingredients.values());
    }
    
    /**
     * Get a specific potion by name
     */
    public Potion getPotion(String name) {
        return potions.get(name);
    }
    
    /**
     * Get a specific ingredient by name
     */
    public Ingredient getIngredient(String name) {
        return ingredients.get(name);
    }
    
    /**
     * Check if a potion is effective against a boss
     */
    public boolean isPotionEffectiveAgainstBoss(String potionName, String bossName) {
        List<String> effectivePotions = bossWeaknesses.get(bossName);
        return effectivePotions != null && effectivePotions.contains(potionName);
    }
    
    /**
     * Get the effective potions for a specific boss
     */
    public List<String> getEffectivePotionsForBoss(String bossName) {
        return bossWeaknesses.getOrDefault(bossName, new ArrayList<>());
    }
    
    /**
     * Get the primary weakness potion for a boss
     */
    public String getPrimaryWeaknessPotion(String bossName) {
        List<String> effectivePotions = bossWeaknesses.get(bossName);
        if (effectivePotions != null && !effectivePotions.isEmpty()) {
            return effectivePotions.get(0);
        }
        return null;
    }
    
    /**
     * Get ingredients for a specific phase of the game
     * Phase 1: Returns all ingredient names for selection
     * Phase 2: Returns ingredients that would be in run groups
     * Phase 3: Returns potions that can be crafted
     */
    public List<String> getIngredientsForPhase(int phase, String bossName) {
        if (phase == 1) {
            // Return all ingredient names for the Eye of Pattern phase
            return new ArrayList<>(ingredients.keySet());
        } else if (phase == 2) {
            // Return ingredients needed for the current boss's effective potions
            List<String> effectivePotions = bossWeaknesses.get(bossName);
            if (effectivePotions != null && !effectivePotions.isEmpty()) {
                String primaryPotion = effectivePotions.get(0);
                return potions.get(primaryPotion).getIngredientNames();
            }
            return new ArrayList<>();
        } else if (phase == 3) {
            // Return potion options for the Mind of Unity phase
            // Always include the primary weakness potion and a distractor potion
            List<String> potionOptions = new ArrayList<>();
            String primaryPotion = getPrimaryWeaknessPotion(bossName);
            
            if (primaryPotion != null) {
                potionOptions.add(primaryPotion);
                
                // Add a distractor potion (one that's not effective)
                for (String potionName : potions.keySet()) {
                    if (!potionName.equals(primaryPotion)) {
                        potionOptions.add(potionName);
                        break;
                    }
                }
            }
            
            return potionOptions;
        }
        
        return new ArrayList<>();
    }
}