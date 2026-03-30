package ch.ksrminecraft.kSRLobbyClient;

import ch.ksrminecraft.kSRLobbyClient.commands.EchoTestCommand;
import ch.ksrminecraft.kSRLobbyClient.commands.JoinGameCommand;
import ch.ksrminecraft.kSRLobbyClient.listeners.LobbyZoneListener;
import ch.ksrminecraft.kSRLobbyClient.listeners.PendingTeleportJoinListener;
import ch.ksrminecraft.kSRLobbyClient.listeners.PluginMessageListenerImpl;
import ch.ksrminecraft.kSRLobbyClient.listeners.WorldChangeListener;
import ch.ksrminecraft.kSRLobbyClient.utils.PluginMessageSender;
import ch.ksrminecraft.kSRLobbyClient.utils.ZoneConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KSRLobbyClient extends JavaPlugin {

    public static final String CHANNEL = "ksr:lobby";
    private boolean debugEnabled = false;

    private final Map<UUID, String> pendingTeleports = new ConcurrentHashMap<>();

    // requestId -> gespeicherte Sender-Position für TPHERE
    private final Map<String, Location> pendingTphereLocations = new ConcurrentHashMap<>();

    // Spielerstatus für /lobby-/hub-Zonen
    private final Map<UUID, Boolean> lobbyCommandAllowed = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> lobbyCommandRestricted = new ConcurrentHashMap<>();
    private final Map<UUID, String> lobbyCommandWorld = new ConcurrentHashMap<>();

    private ZoneConfigManager zoneConfigManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        debugEnabled = getConfig().getBoolean("debug", false);

        this.zoneConfigManager = new ZoneConfigManager(this);
        this.zoneConfigManager.load();

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, CHANNEL, new PluginMessageListenerImpl(this));

        Bukkit.getPluginManager().registerEvents(new WorldChangeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PendingTeleportJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LobbyZoneListener(this), this);

        if (getCommand("echotest") != null) {
            getCommand("echotest").setExecutor(new EchoTestCommand(this));
            getLogger().info("[KSRLobbyClient] Command /echotest registriert.");
        } else {
            getLogger().warning("[KSRLobbyClient] Command /echotest konnte nicht registriert werden!");
        }

        if (getCommand("joingame") != null) {
            getCommand("joingame").setExecutor(new JoinGameCommand(this));
            getLogger().info("[KSRLobbyClient] Command /joingame registriert.");
        } else {
            getLogger().warning("[KSRLobbyClient] Command /joingame konnte nicht registriert werden!");
        }

        getLogger().info("[KSRLobbyClient] Plugin aktiviert! Debug=" + debugEnabled);
        getLogger().info("[KSRLobbyClient] PluginMessage-Channel registriert: \"" + CHANNEL + "\"");
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, CHANNEL);

        pendingTeleports.clear();
        pendingTphereLocations.clear();
        lobbyCommandAllowed.clear();
        lobbyCommandRestricted.clear();
        lobbyCommandWorld.clear();

        getLogger().info("[KSRLobbyClient] Deaktiviert! Channel war: \"" + CHANNEL + "\"");
    }

    public ZoneConfigManager getZoneConfigManager() {
        return zoneConfigManager;
    }

    public boolean updateLobbyCommandPermission(UUID uuid, boolean allowed, boolean restricted, String worldName) {
        Boolean oldAllowed = lobbyCommandAllowed.put(uuid, allowed);
        Boolean oldRestricted = lobbyCommandRestricted.put(uuid, restricted);
        String oldWorld = lobbyCommandWorld.put(uuid, worldName);

        boolean changed = oldAllowed == null
                || oldRestricted == null
                || oldWorld == null
                || oldAllowed != allowed
                || oldRestricted != restricted
                || !oldWorld.equalsIgnoreCase(worldName);

        if (changed) {
            debug("lobbyPermission.update " + uuid
                    + " -> allowed=" + allowed
                    + ", restricted=" + restricted
                    + ", world=" + worldName);
        }

        return changed;
    }

    public void setPendingTeleport(UUID uuid, String worldName) {
        pendingTeleports.put(uuid, worldName);
        debug("pendingTeleports.put " + uuid + " -> " + worldName);
    }

    public void tryExecutePendingTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        String worldName = pendingTeleports.get(uuid);

        if (worldName == null || worldName.isBlank()) {
            debug("tryExecutePendingTeleport: kein pending teleport für " + player.getName());
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            getLogger().warning("[KSRLobbyClient] PendingTeleport: Welt nicht gefunden -> " + worldName);
            return;
        }

        boolean success = player.teleport(world.getSpawnLocation());
        if (success) {
            debug("PendingTeleport: teleport success for " + player.getName() + " -> " + worldName);
            pendingTeleports.remove(uuid);
            PluginMessageSender.sendTeleportAck(this, player, uuid);
        } else {
            getLogger().warning("[KSRLobbyClient] PendingTeleport: teleport fehlgeschlagen für "
                    + player.getName() + " -> " + worldName);
        }
    }

    public void storePendingTphereLocation(String requestId, Location location) {
        if (requestId == null || requestId.isBlank() || location == null) {
            return;
        }

        pendingTphereLocations.put(requestId, location.clone());
        debug("pendingTphereLocations.put " + requestId
                + " -> " + location.getWorld().getName()
                + " "
                + location.getBlockX() + ","
                + location.getBlockY() + ","
                + location.getBlockZ());
    }

    public Location getPendingTphereLocation(String requestId) {
        Location location = pendingTphereLocations.get(requestId);
        return location == null ? null : location.clone();
    }

    public void removePendingTphereLocation(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return;
        }
        pendingTphereLocations.remove(requestId);
        debug("pendingTphereLocations.remove " + requestId);
    }

    public void debug(String msg) {
        if (debugEnabled) {
            getLogger().info("[KSRLobbyClient-DEBUG] " + msg);
        }
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}