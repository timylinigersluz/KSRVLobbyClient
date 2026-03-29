package ch.ksrminecraft.kSRLobbyClient.listeners;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.UUID;

public class PluginMessageListenerImpl implements PluginMessageListener {

    private final KSRLobbyClient plugin;

    public PluginMessageListenerImpl(KSRLobbyClient plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("ksr:lobby")) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subChannel = in.readUTF();

            switch (subChannel) {
                case "ECHO_REPLY": {
                    String replyMessage = in.readUTF();
                    player.sendMessage("§a[Proxy-Reply] " + replyMessage);
                    plugin.debug("ECHO_REPLY empfangen für " + player.getName() + ": " + replyMessage);
                    break;
                }

                case "TP_WORLD": {
                    String uuidStr = in.readUTF();
                    String worldName = in.readUTF();

                    UUID uuid;
                    try {
                        uuid = UUID.fromString(uuidStr);
                    } catch (IllegalArgumentException ex) {
                        plugin.getLogger().warning("[KSRLobbyClient] TP_WORLD: ungültige UUID -> " + uuidStr);
                        return;
                    }

                    plugin.debug("TP_WORLD empfangen: uuid=" + uuid + " world=" + worldName);

                    plugin.setPendingTeleport(uuid, worldName);

                    Player target = Bukkit.getPlayer(uuid);
                    if (target != null) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            plugin.debug("TP_WORLD delayed execution for " + target.getName() + " -> " + worldName);
                            plugin.tryExecutePendingTeleport(target);
                        }, 10L);
                    } else {
                        plugin.debug("TP_WORLD: Spieler noch nicht verfügbar -> pending gespeichert");
                    }
                    break;
                }

                default:
                    plugin.debug("Unbekannter SubChannel empfangen: " + subChannel);
            }

        } catch (Exception e) {
            plugin.getLogger().severe("[KSRLobbyClient] Fehler beim Verarbeiten der PluginMessage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}