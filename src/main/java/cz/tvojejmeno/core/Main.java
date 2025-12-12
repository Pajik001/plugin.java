package cz.tvojejmeno.core;

import cz.tvojejmeno.core.commands.*;
import cz.tvojejmeno.core.listeners.*;
import cz.tvojejmeno.core.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    // Managers
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
    private ChatBubbleManager chatBubbleManager;
    private NeedsManager needsManager;
    private CKManager ckManager;
    private GunManager gunManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // 1. Inicializace Managerů (Pořadí je důležité)
        this.databaseManager = new DatabaseManager(this);
        this.characterManager = new CharacterManager(this); // Načítá postavy
        this.needsManager = new NeedsManager(this); // Potřeby (musí mít přístup k postavám)
        
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
        this.ckManager = new CKManager(this);
        this.gunManager = new GunManager();

        // 2. Registrace Listenerů
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new InteractionListener(this), this);
        pm.registerEvents(new FactionListener(this), this);
        pm.registerEvents(new MedicalListener(this), this);
        pm.registerEvents(new CustomItemListener(this), this);
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

        // 3. Registrace Příkazů
        InteractionCommand intCmd = new InteractionCommand(this);
        registerCmd("lock", intCmd);
        registerCmd("unlock", intCmd);
        registerCmd("key", intCmd);
        registerCmd("revive", intCmd);
        
        registerCmd("char", new CharacterCommand(this));
        registerCmd("f", new FactionCommand(this));
        registerCmd("eco", new EconomyCommand(this));
        registerCmd("zvire", new AnimalCommand(this));
        registerCmd("ukazatlicence", new LicenseCommand(this));
        registerCmd("core", new NPCCommand());
        registerCmd("mobdrop", new DropCommand(dropManager));
        registerCmd("adminitems", new AdminItemsCommand(this));
        
        RPCommands rpCmd = new RPCommands(this);
        registerCmd("me", rpCmd);
        registerCmd("do", rpCmd);
        registerCmd("poop", rpCmd); 
        
        // 4. Spuštění smyčky pro potřeby
        startNeedsLoop();

        getLogger().info("§aSuperSurvivalCore zapnut!");
    }

    private void registerCmd(String name, org.bukkit.command.CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().warning("Chyba: Příkaz '" + name + "' není v plugin.yml!");
        }
    }

    private void startNeedsLoop() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (needsManager != null) needsManager.tick(p);
            }
        }, 20L, 20L);
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
    public GunManager getGunManager() { return gunManager; }


}