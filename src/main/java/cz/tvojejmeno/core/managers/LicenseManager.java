package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LicenseManager {

    private final Main plugin;

    public LicenseManager(Main plugin) {
        this.plugin = plugin;
    }

    public void addLicense(Player player, String licenseType) {
        // Asynchronně, aby to neseklo server
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO rp_licenses (uuid, license_type) VALUES (?, ?)");
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, licenseType);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean hasLicense(Player player, String licenseType) {
        // POZOR: Toto je blokující operace (volá DB). V GUI to nevadí, ale v kritickém kódu opatrně.
        // Pro optimalizaci by se měly licence načítat při Joinu do cache (stejně jako postava).
        // Pro teď to uděláme napřímo pro jednoduchost.
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM rp_licenses WHERE uuid = ? AND license_type = ?");
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, licenseType);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getLicenses(UUID uuid) {
        List<String> licenses = new ArrayList<>();
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT license_type FROM rp_licenses WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                licenses.add(rs.getString("license_type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return licenses;
    }
}