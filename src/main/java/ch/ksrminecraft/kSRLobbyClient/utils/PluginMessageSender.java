package ch.ksrminecraft.kSRLobbyClient.utils;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

public class PluginMessageSender {

    public static void sendWorldUpdate(KSRLobbyClient plugin, Player player, String world) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("WORLD_UPDATE");
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF(world);

            byte[] payload = out.toByteArray();
            plugin.debug("WORLD_UPDATE sending: " + player.getName()
                    + " uuid=" + player.getUniqueId()
                    + " world=" + world
                    + " bytes=" + payload.length);

            player.sendPluginMessage(plugin, KSRLobbyClient.CHANNEL, payload);
        } catch (Exception e) {
            plugin.getLogger().severe("[KSRLobbyClient] Fehler beim WORLD_UPDATE: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
