package ch.ksrminecraft.kSRLobbyClient.listeners;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import ch.ksrminecraft.kSRLobbyClient.utils.PluginMessageSender;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

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

                case "TPHERE_PREPARE": {
                    String requestId = in.readUTF();
                    String senderUuidStr = in.readUTF();
                    String targetUuidStr = in.readUTF();
                    String targetName = in.readUTF();

                    plugin.debug("TPHERE_PREPARE empfangen: requestId=" + requestId
                            + " senderUuid=" + senderUuidStr
                            + " targetUuid=" + targetUuidStr
                            + " targetName=" + targetName);

                    UUID senderUuid;
                    try {
                        senderUuid = UUID.fromString(senderUuidStr);
                    } catch (IllegalArgumentException ex) {
                        plugin.getLogger().warning("[KSRLobbyClient] TPHERE_PREPARE: ungültige sender UUID -> " + senderUuidStr);
                        PluginMessageSender.sendTphereResult(plugin, player, requestId, false,
                                "Teleport-Vorbereitung fehlgeschlagen: ungültige Sender-UUID.");
                        return;
                    }

                    Player senderPlayer = Bukkit.getPlayer(senderUuid);
                    if (senderPlayer == null || !senderPlayer.isOnline()) {
                        plugin.getLogger().warning("[KSRLobbyClient] TPHERE_PREPARE: Sender nicht online -> " + senderUuid);
                        PluginMessageSender.sendTphereResult(plugin, player, requestId, false,
                                "Teleport-Vorbereitung fehlgeschlagen: ausführender Spieler nicht online.");
                        return;
                    }

                    Location senderLocation = senderPlayer.getLocation().clone();
                    plugin.storePendingTphereLocation(requestId, senderLocation);

                    PluginMessageSender.sendTpherePrepared(plugin, senderPlayer, requestId);
                    break;
                }

                case "TPHERE_EXECUTE": {
                    String requestId = in.readUTF();
                    String targetUuidStr = in.readUTF();

                    plugin.debug("TPHERE_EXECUTE empfangen: requestId=" + requestId
                            + " targetUuid=" + targetUuidStr);

                    UUID targetUuid;
                    try {
                        targetUuid = UUID.fromString(targetUuidStr);
                    } catch (IllegalArgumentException ex) {
                        plugin.getLogger().warning("[KSRLobbyClient] TPHERE_EXECUTE: ungültige target UUID -> " + targetUuidStr);
                        PluginMessageSender.sendTphereResult(plugin, player, requestId, false,
                                "Teleport fehlgeschlagen: ungültige Zielspieler-UUID.");
                        plugin.removePendingTphereLocation(requestId);
                        return;
                    }

                    Location targetLocation = plugin.getPendingTphereLocation(requestId);
                    if (targetLocation == null || targetLocation.getWorld() == null) {
                        plugin.getLogger().warning("[KSRLobbyClient] TPHERE_EXECUTE: keine gespeicherte Position gefunden -> " + requestId);
                        PluginMessageSender.sendTphereResult(plugin, player, requestId, false,
                                "Teleport fehlgeschlagen: keine Zielposition gefunden.");
                        plugin.removePendingTphereLocation(requestId);
                        return;
                    }

                    Player targetPlayer = Bukkit.getPlayer(targetUuid);
                    if (targetPlayer == null || !targetPlayer.isOnline()) {
                        plugin.getLogger().warning("[KSRLobbyClient] TPHERE_EXECUTE: Zielspieler nicht online -> " + targetUuid);
                        PluginMessageSender.sendTphereResult(plugin, player, requestId, false,
                                "Teleport fehlgeschlagen: Zielspieler nicht online.");
                        plugin.removePendingTphereLocation(requestId);
                        return;
                    }

                    Player finalTargetPlayer = targetPlayer;
                    Location finalTargetLocation = targetLocation.clone();

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        boolean success;
                        try {
                            success = finalTargetPlayer.teleport(finalTargetLocation);
                        } catch (Exception ex) {
                            plugin.getLogger().severe("[KSRLobbyClient] TPHERE_EXECUTE: Exception beim Teleport von "
                                    + finalTargetPlayer.getName() + ": " + ex.getMessage());
                            ex.printStackTrace();
                            success = false;
                        }

                        if (success) {
                            plugin.debug("TPHERE_EXECUTE: teleport success -> " + finalTargetPlayer.getName()
                                    + " to "
                                    + finalTargetLocation.getWorld().getName()
                                    + " "
                                    + finalTargetLocation.getBlockX() + ","
                                    + finalTargetLocation.getBlockY() + ","
                                    + finalTargetLocation.getBlockZ());

                            PluginMessageSender.sendTphereResult(plugin, finalTargetPlayer, requestId, true,
                                    finalTargetPlayer.getName() + " wurde erfolgreich zu dir teleportiert.");
                        } else {
                            plugin.getLogger().warning("[KSRLobbyClient] TPHERE_EXECUTE: teleport fehlgeschlagen für "
                                    + finalTargetPlayer.getName());

                            PluginMessageSender.sendTphereResult(plugin, finalTargetPlayer, requestId, false,
                                    "Teleport fehlgeschlagen.");
                        }

                        plugin.removePendingTphereLocation(requestId);
                    });
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