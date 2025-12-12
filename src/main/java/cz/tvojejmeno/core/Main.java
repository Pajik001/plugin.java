package cz.tvojejmeno.core;

import cz.tvojejmeno.core.commands.*;
import cz.tvojejmeno.core.listeners.*;
import cz.tvojejmeno.core.managers.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    private DatabaseManager databaseManager;
    private CharacterManager characterManager;
    private CurrencyManager currencyManager;
    private BackpackManager backpackManager;
    private DropManager dropManager;
    private LicenseManager licenseManager;
    private ShopManager shopManager;
    private AnimalManager animalManager;
    private StableManager stableManager;
    private LockManager lockManager;
    private MedicalManager medicalManager;
    private FactionManager factionManager;
    private FolderManager folderManager;
    private BountyManager bountyManager;
    
    // NOVÉ
    private ChatBubbleManager chatBubbleManager;
    private NeedsManager needsManager;
    private CKManager ckManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        this.characterManager = new CharacterManager(this);
        this.currencyManager = new CurrencyManager(this);
        this.backpackManager = new BackpackManager(this);
        this.dropManager = new DropManager(this);
        this.licenseManager = new LicenseManager(this);
        this.shopManager = new ShopManager(this);
        this.animalManager = new AnimalManager(this);
        this.stableManager = new StableManager(this);
        this.lockManager = new LockManager(this);
        this.medicalManager = new MedicalManager(this);
        this.factionManager = new FactionManager(this);
        this.folderManager = new FolderManager(this);
        this.bountyManager = new BountyManager(this);
        
        this.chatBubbleManager = new ChatBubbleManager(this);
        this.needsManager = new NeedsManager(this);
        this.ckManager = new CKManager(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new InteractionListener(this), this); // Opravený constructor
        pm.registerEvents(new FactionListener(this), this);
        pm.registerEvents(new MedicalListener(this), this);
        pm.registerEvents(new CustomItemListener(this), this);
        
        // ... (Zbytek registrací listenerů) ...
        pm.registerEvents(new PlayerConnectionListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new WalletListener(this), this);
        pm.registerEvents(this.shopManager, this);
        pm.registerEvents(new NPCListener(this), this);
        pm.registerEvents(new MovementListener(this), this);
        pm.registerEvents(new GunListener(), this);
        pm.registerEvents(new DropListener(dropManager), this);
        pm.registerEvents(new CombatListener(), this);
        pm.registerEvents(new AnimalListener(this), this);
        pm.registerEvents(this.stableManager, this);
        pm.registerEvents(new ContractListener(this), this);
        pm.registerEvents(new BountyListener(this), this);
        pm.registerEvents(new DeathListener(this), this);

        // COMMANDS
        InteractionCommand intCmd = new InteractionCommand(this);
        if (getCommand("lock") != null) getCommand("lock").setExecutor(intCmd);
        if (getCommand("unlock") != null) getCommand("unlock").setExecutor(intCmd);
        if (getCommand("key") != null) getCommand("key").setExecutor(intCmd);
        if (getCommand("revive") != null) getCommand("revive").setExecutor(intCmd);
        
        if (getCommand("char") != null) getCommand("char").setExecutor(new CharacterCommand(this));
        if (getCommand("f") != null) getCommand("f").setExecutor(new FactionCommand(this));
        
        // ... (Zbytek příkazů) ...
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) databaseManager.close();
    }

    public static Main getInstance() { return instance; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public CharacterManager getCharacterManager() { return characterManager; }
    public FactionManager getFactionManager() { return factionManager; }
    public LockManager getLockManager() { return lockManager; }
    public MedicalManager getMedicalManager() { return medicalManager; }
    public ChatBubbleManager getChatBubbleManager() { return chatBubbleManager; }
    public NeedsManager getNeedsManager() { return needsManager; }
    public CKManager getCkManager() { return ckManager; }
    public CurrencyManager getCurrencyManager() { return currencyManager; }
    public BackpackManager getBackpackManager() { return backpackManager; }
    public LicenseManager getLicenseManager() { return licenseManager; }
    public StableManager getStableManager() { return stableManager; }
    public AnimalManager getAnimalManager() { return animalManager; }
    public BountyManager getBountyManager() { return bountyManager; }
    public FolderManager getFolderManager() { return folderManager; }
    public ShopManager getShopManager() { return shopManager; }
}