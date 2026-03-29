package ch.ksrminecraft.kSRLobbyClient.commands;

import ch.ksrminecraft.kSRLobbyClient.KSRLobbyClient;
import ch.ksrminecraft.kSRLobbyClient.utils.PluginMessageSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinGameCommand implements CommandExecutor {

    private final KSRLobbyClient plugin;

    public JoinGameCommand(KSRLobbyClient plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl ausführen.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cBitte gib ein Ziel an. Beispiel: /joingame mm");
            return true;
        }

        String routeId = args[0].toLowerCase();
        plugin.debug("JoinGameCommand: " + player.getName() + " -> " + routeId);

        PluginMessageSender.sendJoinGameRequest(plugin, player, routeId);
        return true;
    }
}