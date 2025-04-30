package gameproject.model;

import java.util.*;
import gameproject.controller.GameController;
import gameproject.util.ResourceManager;

/**
 * The NarrativeSystem manages all story elements, dialogues, and character interactions
 * for the TimSort visualization RPG game.
 */
public class NarrativeSystem {
    // Singleton instance
    private static NarrativeSystem instance;
    
    // Current state tracking
    private int currentStoryPhase = 0;
    private String currentAct = "prologue";
    private GameState currentAlgorithmPhase = null;
    
    // Reference to game controller
    private GameController controller;
    
    // Characters in the game
    private Map<String, StoryCharacter> characters;
    
    // Dialogues for different parts of the story
    private Map<String, List<DialogueEntry>> dialogueSequences;
    
    // Private constructor for singleton
    private NarrativeSystem() {
        characters = new HashMap<>();
        dialogueSequences = new HashMap<>();
        initializeCharacters();
        initializeDialogues();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized NarrativeSystem getInstance() {
        if (instance == null) {
            instance = new NarrativeSystem();
        }
        return instance;
    }
    
    /**
     * Initialize the narrative system with controller reference
     */
    public void initialize(GameController controller) {
        this.controller = controller;
    }
    
    /**
     * Initialize all characters in the game
     */
    private void initializeCharacters() {
        // Create main characters
        characters.put("Tima", new StoryCharacter("Tima", "Hero", "/gameproject/resources/characters/tima.png", 
            "A young alchemist seeking to master the ancient art of potion crafting."));
        
        characters.put("MasterOrdin", new StoryCharacter("Master Ordin", "Mentor", "/gameproject/resources/characters/master_ordin.png", 
            "An elderly mentor and master alchemist who teaches Tima the 'Harmony of Order' technique."));
        
        characters.put("Runa", new StoryCharacter("Runa", "Helper", "/gameproject/resources/characters/runa.png", 
            "A forest sprite who helps identify natural patterns in ingredients."));
        
        characters.put("MerchantBalz", new StoryCharacter("Merchant Balz", "Helper", "/gameproject/resources/characters/merchant_balz.png", 
            "A traveling merchant specializing in rare ingredients."));
        
        characters.put("ScholarMerion", new StoryCharacter("Scholar Merion", "Helper", "/gameproject/resources/characters/scholar_merion.png", 
            "An expert in potions and their effects."));
        
        characters.put("ElderHarmony", new StoryCharacter("Village Elder Harmony", "Helper", "/gameproject/resources/characters/elder_harmony.png", 
            "Leader of Tima's village, provides guidance and context for the quests."));
        
        // Bosses
        characters.put("Flameclaw", new StoryCharacter("Flameclaw", "Boss", "/gameproject/resources/characters/flameclaw.png", 
            "A fire elemental beast that burns everything in its path."));
        
        characters.put("Toxitar", new StoryCharacter("Toxitar", "Boss", "/gameproject/resources/characters/toxitar.png", 
            "A poison-spreading creature that corrupts the land."));
        
        characters.put("LordChaosa", new StoryCharacter("Lord Chaosa", "Boss", "/gameproject/resources/characters/lord_chaosa.png", 
            "The final boss who manipulates reality itself."));
    }
    
    /**
     * Initialize all dialogues in the game
     */
    private void initializeDialogues() {
        // Prologue dialogues
        List<DialogueEntry> prologueDialogues = new ArrayList<>();
        prologueDialogues.add(new DialogueEntry("MasterOrdin", "Tima, your skills have grown tremendously these past years. I believe you're ready for the final teachings of our ancient art.", "standing"));
        prologueDialogues.add(new DialogueEntry("Tima", "The final teachings? You mean the legendary Harmony of Order technique?", "curious"));
        prologueDialogues.add(new DialogueEntry("MasterOrdin", "Indeed. The three sacred abilities passed down through generations of alchemists. But before I teach you, you must understand why we need them now.", "serious"));
        prologueDialogues.add(new DialogueEntry("ElderHarmony", "Ordin! Tima! Terrible news! The ancient seals have broken. Three chaos beasts have awakened and are threatening our lands!", "panicked"));
        prologueDialogues.add(new DialogueEntry("MasterOrdin", "Then it is as the prophecy foretold. Tima, your training must be accelerated. The Harmony of Order is our only hope.", "concerned"));
        prologueDialogues.add(new DialogueEntry("Tima", "I'll do whatever it takes to protect our village.", "determined"));
        prologueDialogues.add(new DialogueEntry("MasterOrdin", "Then let us begin with the first sacred ability: The Eye of Pattern.", "instructing"));
        dialogueSequences.put("prologue", prologueDialogues);
        
        // Phase 1 - Eye of Pattern
        List<DialogueEntry> phase1Start = new ArrayList<>();
        phase1Start.add(new DialogueEntry("MasterOrdin", "The Eye of Pattern allows you to see natural order within chaos. Every ingredient has an intrinsic value that connects it to others. Your task is to identify these natural sequences.", "instructing"));
        phase1Start.add(new DialogueEntry("MasterOrdin", "You must select exactly 10 ingredients that form natural sequences. These sequences will serve as the foundation for your potions.", "instructing"));
        dialogueSequences.put("phase1_start", phase1Start);
        
        List<DialogueEntry> phase1Middle = new ArrayList<>();
        phase1Middle.add(new DialogueEntry("Runa", "Hello, alchemist! I'm Runa, guardian of natural patterns. I sense Flameclaw approaching our village! His fiery breath burns everything in its path!", "excited"));
        phase1Middle.add(new DialogueEntry("Tima", "Flameclaw? The fire elemental?", "surprised"));
        phase1Middle.add(new DialogueEntry("Runa", "Elements that naturally resist fire will be your ally. Look for ingredients with cooling properties that flow together in sequence.", "helpful"));
        phase1Middle.add(new DialogueEntry("MasterOrdin", "Listen to Runa, Tima. The natural pattern you identify will determine which potions you can craft later.", "instructing"));
        dialogueSequences.put("phase1_middle", phase1Middle);
        
        List<DialogueEntry> phase1End = new ArrayList<>();
        phase1End.add(new DialogueEntry("MasterOrdin", "Excellent! You have mastered the Eye of Pattern. But identifying ingredients is only the beginning. Now you must learn to arrange them properly with the Hand of Balance.", "praising"));
        dialogueSequences.put("phase1_end", phase1End);
        
        // Phase 2 - Hand of Balance
        List<DialogueEntry> phase2Start = new ArrayList<>();
        phase2Start.add(new DialogueEntry("MasterOrdin", "The Hand of Balance teaches us that small groups must be perfectly arranged before they can be unified. You must sort these ingredients into two distinct potion bases.", "instructing"));
        phase2Start.add(new DialogueEntry("MasterOrdin", "Drag each ingredient to its proper position. The sequence matters greatly.", "instructing"));
        dialogueSequences.put("phase2_start", phase2Start);
        
        List<DialogueEntry> phase2Middle = new ArrayList<>();
        phase2Middle.add(new DialogueEntry("MerchantBalz", "Ah, preparing for battle, are we? I've just come from the eastern forests. Flameclaw's heat is so intense that water evaporates before reaching him.", "observing"));
        phase2Middle.add(new DialogueEntry("Tima", "How can we defeat such a creature?", "concerned"));
        phase2Middle.add(new DialogueEntry("MerchantBalz", "This mixture reminds me of the ancient frost elixirs. Those who drink them find fire harmless for a time.", "helpful"));
        phase2Middle.add(new DialogueEntry("MasterOrdin", "Balz has traveled far and knows many secrets. His insights could prove valuable.", "thoughtful"));
        dialogueSequences.put("phase2_middle", phase2Middle);
        
        List<DialogueEntry> phase2End = new ArrayList<>();
        phase2End.add(new DialogueEntry("ScholarMerion", "Fascinating arrangement! These two bases could create either a Fire Resistance potion or a Strength potion. Choose wisely when the time comes.", "analyzing"));
        phase2End.add(new DialogueEntry("MasterOrdin", "And now for the final sacred ability: The Mind of Unity. This will determine which potion you ultimately craft.", "instructing"));
        dialogueSequences.put("phase2_end", phase2End);
        
        // Phase 3 - Mind of Unity
        List<DialogueEntry> phase3Start = new ArrayList<>();
        phase3Start.add(new DialogueEntry("MasterOrdin", "The Mind of Unity teaches us that separate ordered elements must be merged into a greater whole. You must choose which potion to craft for the coming battle.", "instructing"));
        phase3Start.add(new DialogueEntry("Tima", "I must choose between these two potential potions.", "thoughtful"));
        phase3Start.add(new DialogueEntry("MasterOrdin", "Indeed. Remember all you have learned and the warnings about Flameclaw's nature.", "guiding"));
        dialogueSequences.put("phase3_start", phase3Start);
        
        List<DialogueEntry> phase3Decision = new ArrayList<>();
        phase3Decision.add(new DialogueEntry("Runa", "Flameclaw approaches! His fire burns hotter than the sun!", "urgent"));
        phase3Decision.add(new DialogueEntry("MerchantBalz", "A warrior with strength alone will still burn.", "warning"));
        phase3Decision.add(new DialogueEntry("ScholarMerion", "Consider how the elements interact. Fire consumes all except that which cannot burn.", "advising"));
        dialogueSequences.put("phase3_decision", phase3Decision);
        
        List<DialogueEntry> phase3End = new ArrayList<>();
        phase3End.add(new DialogueEntry("MasterOrdin", "The potion is complete! Now you must face Flameclaw!", "excited"));
        dialogueSequences.put("phase3_end", phase3End);
        
        // Boss battle - success scenario
        List<DialogueEntry> boss1SuccessDialogues = new ArrayList<>();
        boss1SuccessDialogues.add(new DialogueEntry("Flameclaw", "BURN! ALL WILL BURN!", "roaring"));
        boss1SuccessDialogues.add(new DialogueEntry("Tima", "Not today, creature of chaos!", "determined"));
        boss1SuccessDialogues.add(new DialogueEntry("Flameclaw", "IMPOSSIBLE! BURN! BURN!", "confused"));
        boss1SuccessDialogues.add(new DialogueEntry("Tima", "The Harmony of Order prevails against chaos!", "confident"));
        boss1SuccessDialogues.add(new DialogueEntry("MasterOrdin", "Magnificent! Your mastery of the Eye of Pattern allowed you to identify the right ingredients, your Hand of Balance arranged them perfectly, and your Mind of Unity crafted the perfect potion!", "praising"));
        dialogueSequences.put("boss1_success", boss1SuccessDialogues);
        
        // Boss battle - failure scenario
        List<DialogueEntry> boss1FailureDialogues = new ArrayList<>();
        boss1FailureDialogues.add(new DialogueEntry("Flameclaw", "BURN! ALL WILL BURN!", "roaring"));
        boss1FailureDialogues.add(new DialogueEntry("Tima", "Not today, creature of chaos!", "determined"));
        boss1FailureDialogues.add(new DialogueEntry("Flameclaw", "STRENGTH MEANS NOTHING AGAINST FIRE!", "triumphant"));
        boss1FailureDialogues.add(new DialogueEntry("MasterOrdin", "Retreat! We must try again with a different approach!", "protecting"));
        dialogueSequences.put("boss1_failure", boss1FailureDialogues);
        
        // Level 2 transition
        List<DialogueEntry> level2TransitionDialogues = new ArrayList<>();
        level2TransitionDialogues.add(new DialogueEntry("ElderHarmony", "You've saved us from Flameclaw, but I fear worse is coming. Our scouts report that Toxitar has emerged from the swamps.", "concerned"));
        level2TransitionDialogues.add(new DialogueEntry("MasterOrdin", "Toxitar spreads deadly poison wherever it goes. We must prepare a new potion.", "planning"));
        level2TransitionDialogues.add(new DialogueEntry("ScholarMerion", "This creature's toxins corrupt the body from within. Only purification can counter such effects.", "analyzing"));
        dialogueSequences.put("level2_transition", level2TransitionDialogues);
        
        // Add more dialogue sequences for Level 2 and 3 following the same pattern
    }
    
    /**
     * Start the narrative from the beginning
     */
    public void startNarrative() {
        currentStoryPhase = 0;
        currentAct = "prologue";
        advanceStory();
    }
    
    /**
     * Get the next dialogue sequence based on current state
     */
    public List<DialogueEntry> getNextDialogueSequence() {
        return dialogueSequences.getOrDefault(currentAct, new ArrayList<>());
    }
    
    /**
     * Advance to the next part of the story
     */
    public void advanceStory() {
        currentStoryPhase++;
        
        // Determine the next act based on story phase
        switch (currentStoryPhase) {
            case 1:
                currentAct = "prologue";
                break;
            case 2:
                currentAct = "phase1_start";
                currentAlgorithmPhase = GameState.TIMSORT_CHALLENGE;
                // Signal controller to switch to Eye of Pattern gameplay
                break;
            case 3:
                currentAct = "phase1_middle";
                break;
            case 4:
                currentAct = "phase1_end";
                break;
            case 5:
                currentAct = "phase2_start";
                break;
            case 6:
                currentAct = "phase2_middle";
                break;
            case 7:
                currentAct = "phase2_end";
                break;
            case 8:
                currentAct = "phase3_start";
                break;
            case 9:
                currentAct = "phase3_decision";
                break;
            case 10:
                currentAct = "phase3_end";
                break;
            case 11:
                // This would be determined by player's choice
                currentAct = "boss1_success"; // or boss1_failure
                break;
            case 12:
                currentAct = "level2_transition";
                break;
            default:
                // End of story or handle looping
                currentAct = "end";
                break;
        }
    }
    
    
    
    /**
    * Get dynamic dialogue for a specific phase based on player choices
    * @param phase The current phase (1, 2, or 3)
    * @param potionTypes The potion types being worked with (can be null in early phases)
    * @return List of DialogueEntry objects with dynamic content
    */
    public List<DialogueEntry> getDynamicDialogue(int phase, String leftPotionType, String rightPotionType) {
        List<DialogueEntry> dialogueSequence = new ArrayList<>();

        // Phase 1 - Eye of Pattern
        if (phase == 1) {
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "The Eye of Pattern allows you to see natural order within chaos. Every ingredient has an intrinsic value that connects it to others. Look for patterns of ingredients.", 
                "instructing"));
            dialogueSequence.add(new DialogueEntry("Runa", 
                "I sense Flameclaw approaching our village! His fiery breath burns everything in its path! You'll need ingredients that can counteract fire.", 
                "excited"));
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "Select ingredients that naturally flow together in sequence. The patterns you recognize will become the foundation of your potion.", 
                "instructing"));
        }
        // Phase 2 - Hand of Balance
        else if (phase == 2) {
            // Check what ingredients were selected in Phase 1
            String recommendedPotion = "Fire Resistance"; // Default

            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "The Hand of Balance teaches us that small groups must be perfectly arranged before they can be unified. Sort your ingredients carefully.",
                "instructing"));

            dialogueSequence.add(new DialogueEntry("MerchantBalz", 
                "Ah, I see you've gathered ingredients for potions. Sort them well - the order matters greatly for effective brewing.",
                "thoughtful"));

            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "Listen to Balz, Tima. Proper sorting of ingredients is crucial for potion effectiveness.",
                "instructing"));
        }
        // Phase 3 - Mind of Unity
        else if (phase == 3) {
            // Add dynamic dialogue based on the potion types identified in Phase 2
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "The Mind of Unity allows you to bring separate elements together into a greater whole. You must now choose which potion to craft.", 
                "instructing"));

            // Left potion type dialogue
            if (leftPotionType.equals("Fire Resistance")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would protect against fire and heat - quite useful against flame-based threats.", 
                    "analyzing"));
            } 
            else if (leftPotionType.equals("Cold Resistance")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would shield against ice and frost - effective against cold-based threats.", 
                    "analyzing"));
            }
            else if (leftPotionType.equals("Strength")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would enhance physical power, but remember that raw strength isn't always the answer.", 
                    "analyzing"));
            }
            else if (leftPotionType.equals("Dexterity")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would improve agility and reflexes - helpful for avoiding attacks but not stopping them.", 
                    "analyzing"));
            }

            // Right potion type dialogue
            if (rightPotionType.equals("Fire Resistance")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion would be very effective against a creature of flame.", 
                    "suggesting"));
            } 
            else if (rightPotionType.equals("Cold Resistance")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion would be ideal against ice creatures, but less effective against fire.", 
                    "suggesting"));
            }
            else if (rightPotionType.equals("Strength")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion gives raw power, but remember that some elements cannot be overcome by strength alone.", 
                    "suggesting"));
            }
            else if (rightPotionType.equals("Dexterity")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion helps evade danger but doesn't protect against the elements directly.", 
                    "suggesting"));
            }

            // Add a hint about Flameclaw specifically
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "Flameclaw's fire is intense. Consider which potion would best protect against his flames.", 
                "guiding"));
        }

        return dialogueSequence;
    }

    /**
     * Get dynamic hints based on the current phase and identified potion types
     */
    public String getDynamicHint(int phase, String leftPotionType, String rightPotionType) {
        switch(phase) {
            case 1:
                return "Look for ingredients with similar properties. Fire ingredients are red, Cold are blue, Strength are green, and Dexterity are yellow.";
            case 2:
                return "Sort each group from lowest to highest value. The order is crucial for potion effectiveness.";
            case 3:
                // Provide hint based on the potions identified
                if (leftPotionType.equals("Fire Resistance") || rightPotionType.equals("Fire Resistance")) {
                    return "Against Flameclaw's fire, direct protection is more effective than enhanced abilities. Consider what would shield you from flames.";
                } else {
                    return "Remember that Flameclaw is a fire creature. Which potion would best counteract fire?";
                }
            default:
                return "Observe the natural patterns in the ingredients and follow the guidance of the characters.";
        }
    }
    
    
    
    
    
    /**
     * Get current algorithm phase
     */
    public GameState getCurrentAlgorithmPhase() {
        return currentAlgorithmPhase;
    }
    
    /**
    * Set the outcome of a boss battle with dynamic potion information
    * @param success Whether the battle was successful
    * @param bossLevel Which boss (1-3)
    * @param selectedPotion The potion the player selected
    */
    public void setBossBattleOutcome(boolean success, int bossLevel, String selectedPotion) {
        // Get boss name based on level
        String bossName = "Unknown";
        if (bossLevel == 1) {
            bossName = "Flameclaw";
        } else if (bossLevel == 2) {
            bossName = "Toxitar";
        } else if (bossLevel == 3) {
            bossName = "LordChaosa";
        }

        // Create dynamic dialogue based on the outcome and selected potion
        List<DialogueEntry> battleDialogues = new ArrayList<>();

        if (bossLevel == 1) { // Flameclaw
            if (success) {
                // Fire Resistance was used successfully
                battleDialogues.add(new DialogueEntry("Flameclaw", "BURN! ALL WILL BURN!", "roaring"));
                battleDialogues.add(new DialogueEntry("Tima", "Not today, creature of chaos!", "determined"));
                battleDialogues.add(new DialogueEntry("Flameclaw", "IMPOSSIBLE! MY FLAMES DO NOTHING!", "confused"));
                battleDialogues.add(new DialogueEntry("Tima", "The " + selectedPotion + " protects me from your fire!", "confident"));
                battleDialogues.add(new DialogueEntry("MasterOrdin", "Excellent choice with the " + selectedPotion + "! Fire cannot harm one protected by this potion.", "praising"));
            } else {
                // Wrong potion was selected
                battleDialogues.add(new DialogueEntry("Flameclaw", "BURN! ALL WILL BURN!", "roaring"));
                battleDialogues.add(new DialogueEntry("Tima", "Not today, creature of chaos!", "determined"));

                if (selectedPotion.contains("Strength")) {
                    battleDialogues.add(new DialogueEntry("Flameclaw", "STRENGTH MEANS NOTHING AGAINST FIRE!", "triumphant"));
                } else if (selectedPotion.contains("Dexterity")) {
                    battleDialogues.add(new DialogueEntry("Flameclaw", "YOU CANNOT OUTRUN THE FLAMES!", "triumphant"));
                } else if (selectedPotion.contains("Cold")) {
                    battleDialogues.add(new DialogueEntry("Flameclaw", "YOUR COLD MAGIC IS TOO WEAK AGAINST MY INFERNO!", "triumphant"));
                } else {
                    battleDialogues.add(new DialogueEntry("Flameclaw", "YOUR POTION IS USELESS AGAINST ME!", "triumphant"));
                }

                battleDialogues.add(new DialogueEntry("MasterOrdin", "Retreat! We must try again with a different approach! The " + selectedPotion + " wasn't effective.", "protecting"));
            }
        } else if (bossLevel == 2) { // Toxitar
            // Similar dynamic dialogue for Toxitar
        } else if (bossLevel == 3) { // Lord Chaosa
            // Similar dynamic dialogue for Lord Chaosa
        }

        // Store the dialogue for use later
        dialogueSequences.put(success ? "boss" + bossLevel + "_success" : "boss" + bossLevel + "_failure", battleDialogues);
    }
    
    /**
     * Get a hint for the current phase
     */
    public String getCurrentHint() {
        switch (currentAct) {
            case "phase1_middle":
                return "Look for ingredients with cooling properties. They often have blue or white colors and form natural sequences.";
            case "phase2_middle":
                return "Frost elixirs require ingredients to be sorted from coldest to warmest. Pay attention to the temperature value of each ingredient.";
            case "phase3_decision":
                return "Against fire, resistance is better than strength. Consider what would protect you rather than what would make you powerful.";
            default:
                return "Observe the natural patterns in the ingredients and follow the guidance of the characters.";
        }
    }
    
    /**
     * Inner class representing a character in the story
     */
    public class StoryCharacter {
        private String name;
        private String role;
        private String imagePath;
        private String description;
        
        public StoryCharacter(String name, String role, String imagePath, String description) {
            this.name = name;
            this.role = role;
            this.imagePath = imagePath;
            this.description = description;
        }
        
        public String getName() {
            return name;
        }
        
        public String getRole() {
            return role;
        }
        
        public String getImagePath() {
            return imagePath;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Inner class representing a single dialogue entry
     */
    public class DialogueEntry {
        private String character;
        private String text;
        private String emotion;
        
        public DialogueEntry(String character, String text, String emotion) {
            this.character = character;
            this.text = text;
            this.emotion = emotion;
        }
        
        public String getCharacter() {
            return character;
        }
        
        public String getText() {
            return text;
        }
        
        public String getEmotion() {
            return emotion;
        }
        
        public StoryCharacter getCharacterObject() {
            return characters.get(character);
        }
    }
    
    /**
    * Get a specific dialogue sequence by key
    */
    public List<DialogueEntry> getDialogueSequence(String key) {
        return dialogueSequences.getOrDefault(key, new ArrayList<>());
    }
}