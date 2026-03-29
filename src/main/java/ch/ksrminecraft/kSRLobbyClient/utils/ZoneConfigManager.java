package ch.ksrminecraft.kSRLobbyClient.utils;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import ch.ksrminecraft.kSRLobbyClient.model.CuboidZone;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ZoneConfigManager {

    private final KSRLobbyClient plugin;
    private final Map<String, CuboidZone> zonesByWorld = new HashMap<>();

    public ZoneConfigManager(KSRLobbyClient plugin) {
        this.plugin = plugin;
    }

    public void load() {
        zonesByWorld.clear();

        ConfigurationSection root = plugin.getConfig().getConfigurationSection("lobby-command-zones");
        if (root == null) {
            plugin.debug("ZoneConfigManager: keine lobby-command-zones definiert -> überall erlaubt");
            return;
        }

        for (String worldName : root.getKeys(false)) {
            ConfigurationSection worldSection = root.getConfigurationSection(worldName);
            if (worldSection == null) {
                continue;
            }

            ConfigurationSection pos1 = worldSection.getConfigurationSection("pos1");
            ConfigurationSection pos2 = worldSection.getConfigurationSection("pos2");

            if (pos1 == null || pos2 == null) {
                plugin.getLogger().warning("[KSRLobbyClient] ZoneConfigManager: pos1/pos2 fehlt für Welt " + worldName + " -> ignoriert");
                continue;
            }

            int x1 = pos1.getInt("x");
            int y1 = pos1.getInt("y");
            int z1 = pos1.getInt("z");

            int x2 = pos2.getInt("x");
            int y2 = pos2.getInt("y");
            int z2 = pos2.getInt("z");

            CuboidZone zone = new CuboidZone(worldName, x1, y1, z1, x2, y2, z2);
            zonesByWorld.put(worldName.toLowerCase(Locale.ROOT), zone);

            plugin.debug("ZoneConfigManager: Zone geladen für Welt " + worldName + " -> " + zone);
        }

        plugin.debug("ZoneConfigManager: geladene Welten mit Zonen = " + zonesByWorld.keySet());
    }

    public Optional<CuboidZone> getZoneForWorld(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(zonesByWorld.get(worldName.toLowerCase(Locale.ROOT)));
    }

    public boolean hasZoneForWorld(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return false;
        }
        return zonesByWorld.containsKey(worldName.toLowerCase(Locale.ROOT));
    }

    public Set<String> getConfiguredWorlds() {
        return Collections.unmodifiableSet(zonesByWorld.keySet());
    }
}