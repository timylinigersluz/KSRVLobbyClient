package ch.ksrminecraft.kSRLobbyClient.listeners;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

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

            if ("ECHO_REPLY".equals(subChannel)) {
                String replyMessage = in.readUTF();
                player.sendMessage("§a[Proxy-Reply] " + replyMessage);

                plugin.debug("ECHO_REPLY empfangen für " + player.getName() + ": " + replyMessage);
            } else {
                plugin.debug("Unbekannter SubChannel empfangen: " + subChannel);
            }

        } catch (Exception e) {
            plugin.getLogger().severe("[KSRLobbyClient] Fehler beim Verarbeiten der PluginMessage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
