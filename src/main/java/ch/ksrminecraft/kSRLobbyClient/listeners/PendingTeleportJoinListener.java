package ch.ksrminecraft.kSRLobbyClient.listeners;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PendingTeleportJoinListener implements Listener {

    private final KSRLobbyClient plugin;

    public PendingTeleportJoinListener(KSRLobbyClient plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.debug("PendingTeleportJoinListener: prüfe pending teleport für " + player.getName());
            plugin.tryExecutePendingTeleport(player);
        }, 20L);
    }
}