package de.maxhenkel.voicechat_broadcast;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat_broadcast.command.BroadcastCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public final class VoicechatBroadcast extends JavaPlugin {

    public static final String PLUGIN_ID = "voicechat_broadcast";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_ID);

    @Nullable
    private BroadcastVoicechatPlugin voicechatPlugin;

    @Override
    public void onEnable() {
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            voicechatPlugin = new BroadcastVoicechatPlugin();
            service.registerPlugin(voicechatPlugin);
            LOGGER.info("Successfully registered voice chat broadcast plugin");
        } else {
            LOGGER.info("Failed to register voice chat broadcast plugin");
        }

        // Register the BroadcastCommand
        registerCommand("podcast", new BroadcastCommand(this));

    }

    @Override
    public void onDisable() {
        if (voicechatPlugin != null) {
            getServer().getServicesManager().unregister(voicechatPlugin);
            LOGGER.info("Successfully unregistered voice chat broadcast plugin");
        }
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        getCommand(commandName).setExecutor(executor);
        if (executor instanceof TabCompleter) {
            getCommand(commandName).setTabCompleter((TabCompleter) executor);
        }
    }
}
