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
        
        
        
        
        
        // Level 2 intro dialogue
        List<DialogueEntry> level2IntroDialogues = new ArrayList<>();
        level2IntroDialogues.add(new DialogueEntry("ElderHarmony", "The defeat of Flameclaw was only the beginning. Reports from the eastern marshes tell of Toxitar spreading deadly poison throughout the land.", "concerned"));
        level2IntroDialogues.add(new DialogueEntry("Tima", "Toxitar? The poison beast from the ancient legends?", "questioning"));
        level2IntroDialogues.add(new DialogueEntry("MasterOrdin", "Yes, and its poison corrupts everything it touches. We must craft a new potion to counter this threat.", "serious"));
        level2IntroDialogues.add(new DialogueEntry("ScholarMerion", "Toxitar's poison is unlike any other - it fills the air and seeps into the ground. Even standing near it causes corruption.", "analyzing"));
        level2IntroDialogues.add(new DialogueEntry("Runa", "I've heard only those with exceptional speed and agility can avoid Toxitar's poison clouds.", "warning"));
        level2IntroDialogues.add(new DialogueEntry("MasterOrdin", "Then we must once again employ the three sacred principles of the Harmony of Order to craft the perfect potion.", "determined"));
        dialogueSequences.put("level2_intro", level2IntroDialogues);

        // Phase 1 - Eye of Pattern (Level 2)
        List<DialogueEntry> level2Phase1Start = new ArrayList<>();
        level2Phase1Start.add(new DialogueEntry("MasterOrdin", "First, we must employ the Eye of Pattern to identify ingredients with properties of agility and speed.", "instructing"));
        level2Phase1Start.add(new DialogueEntry("MasterOrdin", "Look for ingredients that naturally enhance reflexes and movement - they often have distinctive colors and properties.", "instructing"));
        dialogueSequences.put("level2_phase1_start", level2Phase1Start);

        List<DialogueEntry> level2Phase1Middle = new ArrayList<>();
        level2Phase1Middle.add(new DialogueEntry("Runa", "The forest spirits tell me Toxitar's poison clings to those who move slowly. Only swift movements can escape its grasp.", "warning"));
        level2Phase1Middle.add(new DialogueEntry("Tima", "So I need ingredients that enhance speed and reflexes?", "understanding"));
        level2Phase1Middle.add(new DialogueEntry("Runa", "Yes! Look for ingredients with light, swift qualities. They often appear in shades of green.", "helpful"));
        dialogueSequences.put("level2_phase1_middle", level2Phase1Middle);

        List<DialogueEntry> level2Phase1End = new ArrayList<>();
        level2Phase1End.add(new DialogueEntry("MasterOrdin", "Excellent identification! These ingredients indeed have properties of swiftness and agility. Now we must arrange them with the Hand of Balance.", "praising"));
        dialogueSequences.put("level2_phase1_end", level2Phase1End);

        // Phase 2 - Hand of Balance (Level 2)
        List<DialogueEntry> level2Phase2Start = new ArrayList<>();
        level2Phase2Start.add(new DialogueEntry("MasterOrdin", "The Hand of Balance teaches us to sort and arrange our ingredients with precision. For agility potions, the order is especially important.", "instructing"));
        level2Phase2Start.add(new DialogueEntry("MasterOrdin", "Arrange these ingredients from lightest to heaviest - the essence of speed requires perfect equilibrium.", "instructing"));
        dialogueSequences.put("level2_phase2_start", level2Phase2Start);

        List<DialogueEntry> level2Phase2Middle = new ArrayList<>();
        level2Phase2Middle.add(new DialogueEntry("MerchantBalz", "I once traded with acrobats from the eastern kingdoms. They used similar ingredients to enhance their performances.", "reminiscing"));
        level2Phase2Middle.add(new DialogueEntry("Tima", "How did they prepare them?", "curious"));
        level2Phase2Middle.add(new DialogueEntry("MerchantBalz", "They were meticulous about the order - first the lightest essences, then gradually increasing in density. The sequence was as important as the ingredients themselves.", "explaining"));
        dialogueSequences.put("level2_phase2_middle", level2Phase2Middle);

        List<DialogueEntry> level2Phase2End = new ArrayList<>();
        level2Phase2End.add(new DialogueEntry("ScholarMerion", "Your arrangement is perfect! These grouped ingredients could create either a Dexterity potion or a Strength potion. The final decision will determine the potion's effectiveness against Toxitar.", "analyzing"));
        level2Phase2End.add(new DialogueEntry("MasterOrdin", "Now for the Mind of Unity - the final sacred principle that will complete our potion.", "instructing"));
        dialogueSequences.put("level2_phase2_end", level2Phase2End);

        // Phase 3 - Mind of Unity (Level 2)
        List<DialogueEntry> level2Phase3Start = new ArrayList<>();
        level2Phase3Start.add(new DialogueEntry("MasterOrdin", "The Mind of Unity teaches us that separate elements must be combined with intention and purpose. You must choose which potion to craft.", "instructing"));
        level2Phase3Start.add(new DialogueEntry("Tima", "I must decide between strength and agility to counter Toxitar's poison.", "contemplative"));
        dialogueSequences.put("level2_phase3_start", level2Phase3Start);

        List<DialogueEntry> level2Phase3Decision = new ArrayList<>();
        level2Phase3Decision.add(new DialogueEntry("Runa", "Toxitar approaches! Its poison clouds spread in every direction!", "urgent"));
        level2Phase3Decision.add(new DialogueEntry("MerchantBalz", "Raw strength cannot defeat what it cannot touch.", "warning"));
        level2Phase3Decision.add(new DialogueEntry("ScholarMerion", "Against poison that fills the air, one must either be immune... or never be touched by it at all.", "advising"));
        dialogueSequences.put("level2_phase3_decision", level2Phase3Decision);

        List<DialogueEntry> level2Phase3End = new ArrayList<>();
        level2Phase3End.add(new DialogueEntry("MasterOrdin", "The potion is complete! Now you must face Toxitar!", "encouraging"));
        dialogueSequences.put("level2_phase3_end", level2Phase3End);

        // Boss battle - Toxitar success scenario
        List<DialogueEntry> level2BossSuccessDialogues = new ArrayList<>();
        level2BossSuccessDialogues.add(new DialogueEntry("Toxitar", "POISON... SPREADS... EVERYWHERE!", "roaring"));
        level2BossSuccessDialogues.add(new DialogueEntry("Tima", "Your poison cannot catch what it cannot touch!", "confident"));
        level2BossSuccessDialogues.add(new DialogueEntry("Toxitar", "IMPOSSIBLE! TOO FAST! TOO AGILE!", "confused"));
        level2BossSuccessDialogues.add(new DialogueEntry("Tima", "The Harmony of Order grants me the speed to evade your corruption!", "triumphant"));
        level2BossSuccessDialogues.add(new DialogueEntry("MasterOrdin", "Magnificent! Your mastery of all three principles allowed you to craft the perfect Dexterity potion!", "praising"));
        dialogueSequences.put("boss2_success", level2BossSuccessDialogues);

        // Boss battle - Toxitar failure scenario
        List<DialogueEntry> level2BossFailureDialogues = new ArrayList<>();
        level2BossFailureDialogues.add(new DialogueEntry("Toxitar", "POISON... SPREADS... EVERYWHERE!", "roaring"));
        level2BossFailureDialogues.add(new DialogueEntry("Tima", "Your poison cannot catch what it cannot touch!", "confident"));
        level2BossFailureDialogues.add(new DialogueEntry("Toxitar", "STRENGTH MEANS NOTHING IN MY POISON MIST!", "triumphant"));
        level2BossFailureDialogues.add(new DialogueEntry("MasterOrdin", "Retreat! The potion isn't effective! We must try again with a different approach!", "protecting"));
        dialogueSequences.put("boss2_failure", level2BossFailureDialogues);
        
        
        
        List<DialogueEntry> level1to2TransitionDialogues = new ArrayList<>();
        level1to2TransitionDialogues.add(new DialogueEntry("ElderHarmony", "You've defeated Flameclaw! The village is safe from the fires, at least for now.", "relieved"));
        level1to2TransitionDialogues.add(new DialogueEntry("Tima", "Elder, you don't sound entirely relieved. Is there more trouble?", "concerned"));
        level1to2TransitionDialogues.add(new DialogueEntry("ElderHarmony", "I'm afraid so. A messenger just arrived from the eastern marshes. Toxitar has awakened.", "grave"));
        level1to2TransitionDialogues.add(new DialogueEntry("MasterOrdin", "The poison beast. This is as the prophecy foretold - the three chaos beasts awakening one after another.", "serious"));
        level1to2TransitionDialogues.add(new DialogueEntry("Runa", "Toxitar's poison is said to corrupt everything it touches. The lands around the eastern marshes are already beginning to wither and die.", "worried"));
        level1to2TransitionDialogues.add(new DialogueEntry("MasterOrdin", "Tima, you must use the Harmony of Order once again, but this time to craft a potion that will help you against Toxitar's poison.", "instructing"));
        level1to2TransitionDialogues.add(new DialogueEntry("Tima", "I'm ready, Master Ordin. What ingredients should we seek?", "determined"));
        dialogueSequences.put("level1to2_transition", level1to2TransitionDialogues);
        
        // Level 2 to Level 3 transition dialogues
        List<DialogueEntry> level2to3TransitionDialogues = new ArrayList<>();
        level2to3TransitionDialogues.add(new DialogueEntry("ElderHarmony", "You've defeated Toxitar! The land is safe from the poison, but I fear our greatest challenge yet approaches.", "concerned"));
        level2to3TransitionDialogues.add(new DialogueEntry("Tima", "What could be worse than Toxitar's corruption?", "concerned"));
        level2to3TransitionDialogues.add(new DialogueEntry("ElderHarmony", "The final prophecy speaks of Lord Chaosa, a being of pure chaos who can warp reality itself.", "grave"));
        level2to3TransitionDialogues.add(new DialogueEntry("MasterOrdin", "This is the final test of the Harmony of Order. Chaosa's power threatens the very fabric of our world.", "serious"));
        level2to3TransitionDialogues.add(new DialogueEntry("ScholarMerion", "Lord Chaosa's ability to manipulate reality makes conventional defenses useless. Only raw power can break through such chaos.", "analyzing"));
        level2to3TransitionDialogues.add(new DialogueEntry("MasterOrdin", "Tima, you must use the Harmony of Order one final time to craft a potion that can counter Lord Chaosa's reality-warping abilities.", "instructing"));
        level2to3TransitionDialogues.add(new DialogueEntry("Tima", "I will not fail. The balance of our world depends on it.", "determined"));
        dialogueSequences.put("level2to3_transition", level2to3TransitionDialogues);

        // Level 3 intro dialogue
        List<DialogueEntry> level3IntroDialogues = new ArrayList<>();
        level3IntroDialogues.add(new DialogueEntry("ElderHarmony", "The skies have darkened as Lord Chaosa approaches. Reality itself bends in his presence.", "panicked"));
        level3IntroDialogues.add(new DialogueEntry("Tima", "How do we fight something that can alter reality?", "concerned"));
        level3IntroDialogues.add(new DialogueEntry("MasterOrdin", "When chaos bends reality, only overwhelming force can break through. We need a potion that grants immense strength.", "serious"));
        level3IntroDialogues.add(new DialogueEntry("ScholarMerion", "Lord Chaosa feeds on uncertainty and confusion. His greatest weakness is direct, overwhelming power.", "analyzing"));
        level3IntroDialogues.add(new DialogueEntry("Runa", "I sense the fabric of nature itself tearing. We must act quickly before everything we know is consumed by the void.", "urgent"));
        level3IntroDialogues.add(new DialogueEntry("MasterOrdin", "Once more, we must employ the three sacred principles of the Harmony of Order. This time, our focus must be strength.", "determined"));
        dialogueSequences.put("level3_intro", level3IntroDialogues);

        // Phase 1 - Eye of Pattern (Level 3)
        List<DialogueEntry> level3Phase1Start = new ArrayList<>();
        level3Phase1Start.add(new DialogueEntry("MasterOrdin", "First, we must employ the Eye of Pattern to identify ingredients with properties of raw power and strength.", "instructing"));
        level3Phase1Start.add(new DialogueEntry("MasterOrdin", "Look for ingredients that naturally enhance physical might - they often have distinctive colors and properties.", "instructing"));
        dialogueSequences.put("level3_phase1_start", level3Phase1Start);

        List<DialogueEntry> level3Phase1Middle = new ArrayList<>();
        level3Phase1Middle.add(new DialogueEntry("Runa", "Chaosa's reality distortions can only be overcome by pure, overwhelming force. Ingredients of strength are our best hope.", "warning"));
        level3Phase1Middle.add(new DialogueEntry("Tima", "So I need ingredients that enhance raw power?", "understanding"));
        level3Phase1Middle.add(new DialogueEntry("Runa", "Yes! Look for ingredients with solid, substantial qualities. They often appear in earthy, rich colors.", "helpful"));
        dialogueSequences.put("level3_phase1_middle", level3Phase1Middle);

        List<DialogueEntry> level3Phase1End = new ArrayList<>();
        level3Phase1End.add(new DialogueEntry("MasterOrdin", "Excellent identification! These ingredients indeed have properties of great strength. Now we must arrange them with the Hand of Balance.", "praising"));
        dialogueSequences.put("level3_phase1_end", level3Phase1End);

        // Phase 2 - Hand of Balance (Level 3)
        List<DialogueEntry> level3Phase2Start = new ArrayList<>();
        level3Phase2Start.add(new DialogueEntry("MasterOrdin", "The Hand of Balance teaches us to sort and arrange our ingredients with precision. For strength potions, the sequence must reflect building power.", "instructing"));
        level3Phase2Start.add(new DialogueEntry("MasterOrdin", "Arrange these ingredients from foundation to pinnacle - the essence of strength requires a proper progression.", "instructing"));
        dialogueSequences.put("level3_phase2_start", level3Phase2Start);

        List<DialogueEntry> level3Phase2Middle = new ArrayList<>();
        level3Phase2Middle.add(new DialogueEntry("MerchantBalz", "I once traded with warriors from the northern mountains. They used similar ingredients to enhance their strength before battle.", "reminiscing"));
        level3Phase2Middle.add(new DialogueEntry("Tima", "How did they prepare them?", "curious"));
        level3Phase2Middle.add(new DialogueEntry("MerchantBalz", "They were methodical about the progression - first the foundational elements, then building to the peak of power. The sequence amplified the strength manifold.", "explaining"));
        dialogueSequences.put("level3_phase2_middle", level3Phase2Middle);

        List<DialogueEntry> level3Phase2End = new ArrayList<>();
        level3Phase2End.add(new DialogueEntry("ScholarMerion", "Your arrangement is perfect! These grouped ingredients could create either a Strength potion or a Cold Resistance potion. The final decision will determine our success against Lord Chaosa.", "analyzing"));
        level3Phase2End.add(new DialogueEntry("MasterOrdin", "Now for the Mind of Unity - the final sacred principle that will complete our potion.", "instructing"));
        dialogueSequences.put("level3_phase2_end", level3Phase2End);

        // Phase 3 - Mind of Unity (Level 3)
        List<DialogueEntry> level3Phase3Start = new ArrayList<>();
        level3Phase3Start.add(new DialogueEntry("MasterOrdin", "The Mind of Unity teaches us that separate elements must be combined with intention and purpose. You must choose which potion to craft.", "instructing"));
        level3Phase3Start.add(new DialogueEntry("Tima", "I must decide between strength and cold resistance to counter Lord Chaosa's reality distortions.", "contemplative"));
        dialogueSequences.put("level3_phase3_start", level3Phase3Start);

        List<DialogueEntry> level3Phase3Decision = new ArrayList<>();
        level3Phase3Decision.add(new DialogueEntry("Runa", "Chaosa approaches! Reality twists and warps around him!", "urgent"));
        level3Phase3Decision.add(new DialogueEntry("MerchantBalz", "Cold cannot freeze chaos itself.", "warning"));
        level3Phase3Decision.add(new DialogueEntry("ScholarMerion", "When reality bends, only overwhelming force can cut through the distortion and reach the truth.", "advising"));
        dialogueSequences.put("level3_phase3_decision", level3Phase3Decision);

        List<DialogueEntry> level3Phase3End = new ArrayList<>();
        level3Phase3End.add(new DialogueEntry("MasterOrdin", "The potion is complete! Now you must face Lord Chaosa!", "encouraging"));
        dialogueSequences.put("level3_phase3_end", level3Phase3End);

        // Boss battle - Lord Chaosa success scenario
        List<DialogueEntry> level3BossSuccessDialogues = new ArrayList<>();
        level3BossSuccessDialogues.add(new DialogueEntry("LordChaosa", "REALITY IS MINE TO COMMAND! YOUR WORLD WILL DISSOLVE!", "attacking"));
        level3BossSuccessDialogues.add(new DialogueEntry("Tima", "Your chaos cannot stand against pure strength!", "confident"));
        level3BossSuccessDialogues.add(new DialogueEntry("LordChaosa", "IMPOSSIBLE! MY DISTORTIONS... FAILING!", "weakened"));
        level3BossSuccessDialogues.add(new DialogueEntry("Tima", "The Harmony of Order brings strength to break through your chaos!", "triumphant"));
        level3BossSuccessDialogues.add(new DialogueEntry("MasterOrdin", "Magnificent! Your mastery of all three principles allowed you to craft the perfect Strength potion!", "praising"));
        dialogueSequences.put("boss3_success", level3BossSuccessDialogues);

        // Boss battle - Lord Chaosa failure scenario
        List<DialogueEntry> level3BossFailureDialogues = new ArrayList<>();
        level3BossFailureDialogues.add(new DialogueEntry("LordChaosa", "REALITY IS MINE TO COMMAND! YOUR WORLD WILL DISSOLVE!", "attacking"));
        level3BossFailureDialogues.add(new DialogueEntry("Tima", "Your chaos ends here!", "confident"));
        level3BossFailureDialogues.add(new DialogueEntry("LordChaosa", "COLD CANNOT FREEZE THE FABRIC OF REALITY ITSELF!", "triumphant"));
        level3BossFailureDialogues.add(new DialogueEntry("MasterOrdin", "Retreat! The potion isn't effective! We must try again with a different approach!", "protecting"));
        dialogueSequences.put("boss3_failure", level3BossFailureDialogues);

        // Add in game completion dialogue
        List<DialogueEntry> gameCompletionDialogues = new ArrayList<>();
        gameCompletionDialogues.add(new DialogueEntry("ElderHarmony", "You've done it, Tima! All three chaos beasts have been defeated!", "excited"));
        gameCompletionDialogues.add(new DialogueEntry("MasterOrdin", "Your mastery of the Harmony of Order is complete. You are truly a master alchemist now.", "praising"));
        gameCompletionDialogues.add(new DialogueEntry("Tima", "I couldn't have done it without your guidance, Master Ordin, and the help of everyone in the village.", "grateful"));
        gameCompletionDialogues.add(new DialogueEntry("ScholarMerion", "Your journey has shown that order can always triumph over chaos when approached with wisdom and patience.", "thoughtful"));
        gameCompletionDialogues.add(new DialogueEntry("Runa", "The balance of nature is restored, and our lands can flourish once more.", "happy"));
        gameCompletionDialogues.add(new DialogueEntry("ElderHarmony", "The prophecy is fulfilled. Peace returns to our world, thanks to your courage and skill.", "relieved"));
        dialogueSequences.put("game_completion", gameCompletionDialogues);
    }
    
    
    
    
    
    /**
    * Get dynamic dialogue for Level 2 (Toxitar) based on player choices
    * @param phase The current phase (1, 2, or 3)
    * @param leftPotionType The potion type identified on the left
    * @param rightPotionType The potion type identified on the right
    * @return List of DialogueEntry objects with dynamic content focused on Toxitar
    */
    public List<DialogueEntry> getDynamicLevel2Dialogue(int phase, String leftPotionType, String rightPotionType) {
        List<DialogueEntry> dialogueSequence = new ArrayList<>();

        // Phase 1 - Eye of Pattern for Level 2
        if (phase == 1) {
            // Remove duplicate entries and make sure text is complete
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "The Eye of Pattern reveals natural sequences even in chaos. For Toxitar, we need ingredients that enhance movement and agility.", 
                "instructing"));
            dialogueSequence.add(new DialogueEntry("Runa", 
                "I've watched Toxitar's movements from afar. The poison it exudes affects everything it touches - unless you're quick enough to avoid it completely.", 
                "concerned"));
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "Look for ingredients that naturally enhance reflexes and movement - they often have distinctive green coloring and light properties.", 
                "instructing"));
        }
        // Phase 2 - Hand of Balance for Level 2
        else if (phase == 2) {
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "The Hand of Balance teaches us that even the swiftest ingredients must be properly arranged. Sort these carefully to maximize their potential.",
                "instructing"));

            dialogueSequence.add(new DialogueEntry("MerchantBalz", 
                "In my travels, I've met acrobats who used similar ingredients to perform incredible feats of agility and precision.",
                "reminiscing"));

            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "Indeed, Balz. The potency of dexterity-enhancing ingredients depends greatly on their arrangement.",
                "agreeing"));
        }
        // Phase 3 - Mind of Unity for Level 2
        else if (phase == 3) {
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "The Mind of Unity allows you to combine separate elements into a single, powerful whole. Now you must choose which potion to craft against Toxitar.", 
                "instructing"));

            // Left potion type dialogue customized for Toxitar
            if (leftPotionType.equals("Dexterity")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would greatly enhance your speed and reflexes - qualities needed to avoid Toxitar's poison clouds.", 
                    "analyzing"));
            } 
            else if (leftPotionType.equals("Strength")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would enhance your physical power, but remember that strength alone cannot protect against poison in the air.", 
                    "analyzing"));
            }
            else if (leftPotionType.equals("Fire Resistance")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would protect against heat and flames, but Toxitar's threat comes from poison, not fire.", 
                    "concerned"));
            }
            else if (leftPotionType.equals("Cold Resistance")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would shield against ice and cold, but offers little protection against Toxitar's poison.", 
                    "concerned"));
            }

            // Right potion type dialogue customized for Toxitar
            if (rightPotionType.equals("Dexterity")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion would allow you to move with incredible speed, potentially avoiding Toxitar's poison altogether.", 
                    "suggesting"));
            } 
            else if (rightPotionType.equals("Strength")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion gives raw power, but Toxitar's poison can affect even the strongest if they cannot avoid it.", 
                    "cautioning"));
            }
            else if (rightPotionType.equals("Fire Resistance")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion would be ineffective against Toxitar's poison, which isn't heat-based at all.", 
                    "warning"));
            }
            else if (rightPotionType.equals("Cold Resistance")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion offers no protection against the type of poison Toxitar produces.", 
                    "warning"));
            }

            // Add a specific hint about Toxitar
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "Toxitar's poison fills the air around it. Consider which quality would best help you avoid being poisoned at all.", 
                "guiding"));
        }

        return dialogueSequence;
    }

    
    
    
    
    
    
    /**
     * Get dynamic hints for Level 2 based on the current phase
     */
    public String getDynamicLevel2Hint(int phase) {
        switch(phase) {
            case 1:
                return "Look for ingredients with green coloring that enhance movement and reflexes. These will be key to avoiding Toxitar's poison.";
            case 2:
                return "Arrange the ingredients from lightest to heaviest. For dexterity potions, the proper sequence is crucial for achieving maximum agility.";
            case 3:
                return "Against poison that fills the air around you, the ability to avoid it completely is more valuable than trying to resist it. Which potion would give you that ability?";
            default:
                return "Focus on movement and agility. Toxitar's poison can only harm what it touches.";
        }
    }
    
    
    
    
    /**
    * Get dynamic boss battle outcome dialogue for Toxitar
    */
    public List<DialogueEntry> getToxitarBattleOutcomeDialogue(boolean success, String selectedPotion) {
        List<DialogueEntry> battleDialogues = new ArrayList<>();

        if (success) {
            // Dexterity potion was used successfully
            // Change "roaring" to "attacking" for Toxitar
            battleDialogues.add(new DialogueEntry("Toxitar", "POISON... FILLS... THE AIR!", "attacking"));
            battleDialogues.add(new DialogueEntry("Tima", "Your poison can't touch what it can't catch!", "determined"));
            battleDialogues.add(new DialogueEntry("Toxitar", "TOO FAST! CANNOT... POISON!", "weakened"));
            battleDialogues.add(new DialogueEntry("Tima", "The " + selectedPotion + " gives me the speed to evade your corruption!", "triumphant"));
            battleDialogues.add(new DialogueEntry("MasterOrdin", "Excellent choice with the " + selectedPotion + "! Your enhanced agility allows you to avoid the poison completely.", "praising"));
        } else {
            // Wrong potion was selected
            battleDialogues.add(new DialogueEntry("Toxitar", "POISON... FILLS... THE AIR!", "attacking"));
            battleDialogues.add(new DialogueEntry("Tima", "Your poison can't harm me!", "determined"));

            if (selectedPotion.contains("Strength")) {
                // Changed to "attacking" instead of "triumphant"
                battleDialogues.add(new DialogueEntry("Toxitar", "STRENGTH CANNOT FIGHT WHAT IT CANNOT TOUCH!", "attacking"));
            } else if (selectedPotion.contains("Fire")) {
                battleDialogues.add(new DialogueEntry("Toxitar", "MY POISON IS NOT FLAME TO BE RESISTED!", "attacking"));
            } else if (selectedPotion.contains("Cold")) {
                battleDialogues.add(new DialogueEntry("Toxitar", "ICE PROTECTION MEANS NOTHING AGAINST TOXIN!", "attacking"));
            } else {
                battleDialogues.add(new DialogueEntry("Toxitar", "YOUR POTION IS USELESS AGAINST MY POISON!", "attacking"));
            }

            battleDialogues.add(new DialogueEntry("MasterOrdin", "Retreat! The " + selectedPotion + " isn't effective against Toxitar's poison! We need a different approach!", "protecting"));
        }

        return battleDialogues;
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
    * @param leftPotionType The left potion type
    * @param rightPotionType The right potion type
    * @return List of DialogueEntry objects with dynamic content
    */
    public List<DialogueEntry> getDynamicDialogue(int phase, String leftPotionType, String rightPotionType) {
        List<DialogueEntry> dialogueSequence = new ArrayList<>();

        // Default to Level 1 (Flameclaw) dialogue

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
    
    
    
    /**
    * Get dynamic phase 2 end dialogue with the actual potion types
    */
    public List<DialogueEntry> getDynamicPhase2EndDialogue(String leftPotionType, String rightPotionType) {
        List<DialogueEntry> dialogueSequence = new ArrayList<>();

        // Create dynamic dialogue that references the exact potions identified
        dialogueSequence.add(new DialogueEntry("ScholarMerion", 
            "Fascinating arrangement! These two bases could create either a " + 
            leftPotionType + " potion or a " + 
            rightPotionType + " potion. Choose wisely when the time comes.", 
            "analyzing"));

        dialogueSequence.add(new DialogueEntry("MasterOrdin", 
            "And now for the final sacred ability: The Mind of Unity. " +
            "This will determine which potion you ultimately craft.", 
            "instructing"));

        return dialogueSequence;
    }
    
    
    
    /**
    * Get dynamic dialogue for Level 3 (Lord Chaosa) based on player choices
    * @param phase The current phase (1, 2, or 3)
    * @param leftPotionType The potion type identified on the left
    * @param rightPotionType The potion type identified on the right
    * @return List of DialogueEntry objects with dynamic content focused on Lord Chaosa
    */
    public List<DialogueEntry> getDynamicLevel3Dialogue(int phase, String leftPotionType, String rightPotionType) {
        List<DialogueEntry> dialogueSequence = new ArrayList<>();

        // Phase 1 - Eye of Pattern for Level 3
        if (phase == 1) {
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "The Eye of Pattern reveals natural sequences even in chaos. For Lord Chaosa, we need ingredients that enhance raw strength and power.", 
                "instructing"));
            dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                "Lord Chaosa's power bends reality itself. Only overwhelming force can cut through such distortions.", 
                "analyzing"));
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "Look for ingredients that naturally enhance physical might - they often have distinctive orange and brown coloring and solid properties.", 
                "instructing"));
        }
        // Phase 2 - Hand of Balance for Level 3
        else if (phase == 2) {
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "The Hand of Balance teaches us that even the mightiest ingredients must be properly arranged. Sort these carefully to maximize their potential.",
                "instructing"));

            dialogueSequence.add(new DialogueEntry("MerchantBalz", 
                "In my travels, I've met warriors who used similar ingredients to perform feats of incredible strength.",
                "reminiscing"));

            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "Indeed, Balz. The potency of strength-enhancing ingredients depends greatly on their arrangement.",
                "agreeing"));
        }
        // Phase 3 - Mind of Unity for Level 3
        else if (phase == 3) {
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "The Mind of Unity allows you to combine separate elements into a single, powerful whole. Now you must choose which potion to craft against Lord Chaosa.", 
                "instructing"));

            // Left potion type dialogue customized for Lord Chaosa
            if (leftPotionType.equals("Strength")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would greatly enhance your physical power - the raw force needed to break through Chaosa's reality distortions.", 
                    "analyzing"));
            } 
            else if (leftPotionType.equals("Cold Resistance")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would shield against ice and cold, but Chaosa's power isn't thermal in nature - it's reality itself being twisted.", 
                    "concerned"));
            }
            else if (leftPotionType.equals("Dexterity")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would enhance your speed and reflexes, but agility alone cannot counter the fundamental distortion of reality.", 
                    "concerned"));
            }
            else if (leftPotionType.equals("Fire Resistance")) {
                dialogueSequence.add(new DialogueEntry("ScholarMerion", 
                    "This " + leftPotionType + " potion would protect against heat and flames, but Lord Chaosa's power is far more fundamental than elemental fire.", 
                    "concerned"));
            }

            // Right potion type dialogue customized for Lord Chaosa
            if (rightPotionType.equals("Strength")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion would grant you incredible physical might, allowing you to cut through the reality distortions with pure force.", 
                    "suggesting"));
            } 
            else if (rightPotionType.equals("Cold Resistance")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion protects against ice and frost, but cannot freeze the chaos of warped reality.", 
                    "cautioning"));
            }
            else if (rightPotionType.equals("Dexterity")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion enhances speed, but when reality itself is the enemy, no amount of quickness will help you evade it.", 
                    "warning"));
            }
            else if (rightPotionType.equals("Fire Resistance")) {
                dialogueSequence.add(new DialogueEntry("Runa", 
                    "A " + rightPotionType + " potion would be ineffective against Lord Chaosa's reality warping, which has nothing to do with fire or heat.", 
                    "warning"));
            }

            // Add a specific hint about Lord Chaosa
            dialogueSequence.add(new DialogueEntry("MasterOrdin", 
                "Lord Chaosa's power bends the very fabric of reality. Consider what force could cut through such fundamental distortion.", 
                "guiding"));
        }

        return dialogueSequence;
    }
    
    
    
    /**
    * Get dynamic hints for Level 3 based on the current phase
    */
    public String getDynamicLevel3Hint(int phase) {
        switch(phase) {
            case 1:
                return "Look for ingredients with orange and brown coloring that indicate strength and solidity. These enhance raw power, which is key to fighting Lord Chaosa.";
            case 2:
                return "Arrange the ingredients from foundational to pinnacle. For strength potions, the proper sequence builds power progressively.";
            case 3:
                return "Against reality-warping powers, only raw overwhelming force can break through the distortions. Which potion would give you that ability?";
            default:
                return "Focus on strength and power. Lord Chaosa's reality distortions can only be overcome by overwhelming force.";
        }
    }
    
    
    
    /**
    * Get dynamic boss battle outcome dialogue for Lord Chaosa
    */
    public List<DialogueEntry> getLordChaosaBattleOutcomeDialogue(boolean success, String selectedPotion) {
        List<DialogueEntry> battleDialogues = new ArrayList<>();

        if (success) {
            // Strength potion was used successfully
            battleDialogues.add(new DialogueEntry("LordChaosa", "REALITY IS MINE TO COMMAND! YOUR WORLD WILL DISSOLVE!", "attacking"));
            battleDialogues.add(new DialogueEntry("Tima", "Your chaos cannot withstand true strength!", "determined"));
            battleDialogues.add(new DialogueEntry("LordChaosa", "IMPOSSIBLE! MY DISTORTIONS... FAILING!", "weakened"));
            battleDialogues.add(new DialogueEntry("Tima", "The " + selectedPotion + " gives me the power to break through your illusions!", "triumphant"));
            battleDialogues.add(new DialogueEntry("MasterOrdin", "Excellent choice with the " + selectedPotion + "! Your enhanced strength allows you to cut through the reality distortions!", "praising"));
        } else {
            // Wrong potion was selected
            battleDialogues.add(new DialogueEntry("LordChaosa", "REALITY IS MINE TO COMMAND! YOUR WORLD WILL DISSOLVE!", "attacking"));
            battleDialogues.add(new DialogueEntry("Tima", "Your chaos ends here!", "determined"));

            if (selectedPotion.contains("Cold")) {
                battleDialogues.add(new DialogueEntry("LordChaosa", "COLD CANNOT FREEZE THE FABRIC OF REALITY ITSELF!", "attacking"));
            } else if (selectedPotion.contains("Fire")) {
                battleDialogues.add(new DialogueEntry("LordChaosa", "FIRE CANNOT BURN THROUGH THE VOID BETWEEN REALITIES!", "attacking"));
            } else if (selectedPotion.contains("Dexterity")) {
                battleDialogues.add(new DialogueEntry("LordChaosa", "YOU CANNOT OUTRUN THE COLLAPSE OF REALITY!", "attacking"));
            } else {
                battleDialogues.add(new DialogueEntry("LordChaosa", "YOUR POTION IS USELESS AGAINST THE VOID!", "attacking"));
            }

            battleDialogues.add(new DialogueEntry("MasterOrdin", "Retreat! The " + selectedPotion + " isn't effective against Lord Chaosa's reality warping! We need a different approach!", "protecting"));
        }

        return battleDialogues;
    }
    
}