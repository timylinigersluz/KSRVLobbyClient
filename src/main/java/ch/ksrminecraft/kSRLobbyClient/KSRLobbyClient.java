package ch.ksrminecraft.kSRLobbyClient;

import ch.ksrminecraft.kSRLobbyClient.commands.EchoTestCommand;
import ch.ksrminecraft.kSRLobbyClient.listeners.WorldChangeListener;
import ch.ksrminecraft.kSRLobbyClient.listeners.PluginMessageListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KSRLobbyClient extends JavaPlugin {

    public static final String CHANNEL = "ksr:lobby";
    private boolean debugEnabled = false;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // erzeugt config.yml, falls sie fehlt
        debugEnabled = getConfig().getBoolean("debug", false);

        // PluginMessage-Channel registrieren
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, CHANNEL, new PluginMessageListenerImpl(this));

        // Listener registrieren
        Bukkit.getPluginManager().registerEvents(new WorldChangeListener(this), this);

        // Commands registrieren
        if (getCommand("echotest") != null) {
            getCommand("echotest").setExecutor(new EchoTestCommand(this));
            getLogger().info("[KSRLobbyClient] Command /echotest registriert.");
        } else {
            getLogger().warning("[KSRLobbyClient] Command /echotest konnte nicht registriert werden!");
        }

        getLogger().info("[KSRLobbyClient] Plugin aktiviert! Debug=" + debugEnabled);
        getLogger().info("[KSRLobbyClient] PluginMessage-Channel registriert: \"" + CHANNEL + "\"");
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, CHANNEL);
        getLogger().info("[KSRLobbyClient] Deaktiviert! Channel war: \"" + CHANNEL + "\"");
    }

    public void debug(String msg) {
        if (debugEnabled) {
            getLogger().info("[KSRLobbyClient-DEBUG] " + msg);
        }
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}