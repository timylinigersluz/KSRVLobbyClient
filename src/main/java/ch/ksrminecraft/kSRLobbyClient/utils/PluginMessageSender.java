package ch.ksrminecraft.kSRLobbyClient.utils;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

import java.util.UUID;

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

    public static void sendJoinGameRequest(KSRLobbyClient plugin, Player player, String routeId) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("JOIN_GAME_REQUEST");
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF(routeId);

            byte[] payload = out.toByteArray();
            plugin.debug("JOIN_GAME_REQUEST sending: player=" + player.getName()
                    + " uuid=" + player.getUniqueId()
                    + " route=" + routeId
                    + " bytes=" + payload.length);

            player.sendPluginMessage(plugin, KSRLobbyClient.CHANNEL, payload);
        } catch (Exception e) {
            plugin.getLogger().severe("[KSRLobbyClient] Fehler beim JOIN_GAME_REQUEST: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendTeleportAck(KSRLobbyClient plugin, Player player, UUID uuid) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("TP_WORLD_ACK");
            out.writeUTF(uuid.toString());

            byte[] payload = out.toByteArray();
            plugin.debug("TP_WORLD_ACK sending: player=" + player.getName()
                    + " uuid=" + uuid
                    + " bytes=" + payload.length);

            player.sendPluginMessage(plugin, KSRLobbyClient.CHANNEL, payload);
        } catch (Exception e) {
            plugin.getLogger().severe("[KSRLobbyClient] Fehler beim TP_WORLD_ACK: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendLobbyZoneUpdate(KSRLobbyClient plugin, Player player, String worldName, boolean allowed, boolean restricted) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("LOBBY_ZONE_UPDATE");
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF(worldName);
            out.writeBoolean(allowed);
            out.writeBoolean(restricted);

            byte[] payload = out.toByteArray();
            plugin.debug("LOBBY_ZONE_UPDATE sending: player=" + player.getName()
                    + " uuid=" + player.getUniqueId()
                    + " world=" + worldName
                    + " allowed=" + allowed
                    + " restricted=" + restricted
                    + " bytes=" + payload.length);

            player.sendPluginMessage(plugin, KSRLobbyClient.CHANNEL, payload);
        } catch (Exception e) {
            plugin.getLogger().severe("[KSRLobbyClient] Fehler beim LOBBY_ZONE_UPDATE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}