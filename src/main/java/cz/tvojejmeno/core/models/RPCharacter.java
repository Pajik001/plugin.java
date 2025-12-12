package cz.tvojejmeno.core.models;

import java.util.UUID;

public class RPCharacter {
    private final UUID uuid;
    private String rpName;
    private String origin;
    private int age;
    private boolean hasCharacter;

    // Potřeby (0-100)
    private double thirst;
    private double sleep;
    private int toilet;
    
    // Ekonomika a Daně
    private int taxDebt;
    private int playtimeMinutes;

    public RPCharacter(UUID uuid, String rpName, String origin, int age, boolean hasCharacter) {
        this.uuid = uuid;
        this.rpName = rpName;
        this.origin = origin;
        this.age = age;
        this.hasCharacter = hasCharacter;
        
        // Defaulty
        this.thirst = 100.0;
        this.sleep = 100.0;
        this.toilet = 0;
        this.taxDebt = 0;
        this.playtimeMinutes = 0;
    }

    // Gettery a Settery
    public String getFullName() { return rpName + " z " + origin; }
    public String getOrigin() { return origin; }
    public int getAge() { return age; }
    public boolean hasCharacter() { return hasCharacter; }

    public double getThirst() { return thirst; }
    public void setThirst(double thirst) { this.thirst = Math.max(0, Math.min(100, thirst)); }

    public double getSleep() { return sleep; }
    public void setSleep(double sleep) { this.sleep = Math.max(0, Math.min(100, sleep)); }

    public int getToilet() { return toilet; }
    public void setToilet(int toilet) { this.toilet = Math.max(0, Math.min(100, toilet)); }
    
    public int getTaxDebt() { return taxDebt; }
    public void setTaxDebt(int debt) { this.taxDebt = debt; }
    
    public int getPlaytimeMinutes() { return playtimeMinutes; }
    public void addPlaytime() { this.playtimeMinutes++; }
}