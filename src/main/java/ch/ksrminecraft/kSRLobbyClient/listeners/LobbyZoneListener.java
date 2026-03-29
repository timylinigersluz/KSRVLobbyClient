package ch.ksrminecraft.kSRLobbyClient.listeners;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import ch.ksrminecraft.kSRLobbyClient.model.CuboidZone;
import ch.ksrminecraft.kSRLobbyClient.utils.PluginMessageSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;
import java.util.UUID;

public class LobbyZoneListener implements Listener {

    private final KSRLobbyClient plugin;

    public LobbyZoneListener(KSRLobbyClient plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        updateZoneState(event.getPlayer(), "JOIN");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        updateZoneState(event.getPlayer(), "WORLD_CHANGE");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }

        if (event.getFrom().getWorld().equals(event.getTo().getWorld())
                && event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        updateZoneState(event.getPlayer(), "MOVE");
    }

    private void updateZoneState(Player player, String reason) {
        String worldName = player.getWorld().getName();
        UUID uuid = player.getUniqueId();

        Optional<CuboidZone> zoneOpt = plugin.getZoneConfigManager().getZoneForWorld(worldName);

        boolean allowed;
        boolean restricted;

        if (zoneOpt.isEmpty()) {
            allowed = true;
            restricted = false;

            plugin.debug("LobbyZoneListener: " + reason + " -> " + player.getName()
                    + "@" + worldName + " | keine Zone definiert -> erlaubt");
        } else {
            CuboidZone zone = zoneOpt.get();
            allowed = zone.contains(player.getLocation());
            restricted = true;

            plugin.debug("LobbyZoneListener: " + reason + " -> " + player.getName()
                    + "@" + worldName + " | zoneDefined=true | allowed=" + allowed);
        }

        boolean changed = plugin.updateLobbyCommandPermission(uuid, allowed, restricted, worldName);
        if (changed) {
            PluginMessageSender.sendLobbyZoneUpdate(plugin, player, worldName, allowed, restricted);
        }
    }
}