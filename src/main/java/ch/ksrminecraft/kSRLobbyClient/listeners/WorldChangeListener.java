package ch.ksrminecraft.kSRLobbyClient.listeners;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import ch.ksrminecraft.kSRLobbyClient.utils.PluginMessageSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class WorldChangeListener implements Listener {

    private final KSRLobbyClient plugin;

    public WorldChangeListener(KSRLobbyClient plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 1 Sekunde (20 Ticks) warten, damit Proxy Connection stabil ist
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            sendWorld(player, "JOIN_DELAY");
        }, 20L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        sendWorld(event.getPlayer(), "WORLD_CHANGE");
    }

    private void sendWorld(Player player, String reason) {
        String world = player.getWorld().getName();
        plugin.debug("WORLD_UPDATE Trigger: " + reason + " -> " + player.getName() + "@" + world);
        PluginMessageSender.sendWorldUpdate(plugin, player, world);
    }
}
