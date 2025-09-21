package ch.ksrminecraft.kSRLobbyClient.commands;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EchoTestCommand implements CommandExecutor {

    private final KSRLobbyClient plugin;

    public EchoTestCommand(KSRLobbyClient plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl ausführen.");
            return true;
        }

        // Permission-Check
        if (!player.hasPermission("ksrlobbyclient.echotest")) {
            player.sendMessage("§cKeine Berechtigung für /echotest.");
            return true;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ECHO_START");

        player.sendPluginMessage(plugin, "ksr:lobby", out.toByteArray());

        plugin.debug("ECHO_START gesendet von " + player.getName());
        player.sendMessage("§aEchoTest gesendet an Proxy!");

        return true;
    }
}
