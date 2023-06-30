package de.maxhenkel.voicechat_broadcast.command;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat_broadcast.BroadcastVoicechatPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class InstantGroupCommands implements CommandExecutor {

    private final BroadcastVoicechatPlugin plugin;

    public InstantGroupCommands(BroadcastVoicechatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        VoicechatApi voicechatApi = plugin.getVoicechatApi();
        VoicechatServerApi voicechatServerApi = voicechatApi.getServerApi();

        if (voicechatServerApi == null) {
            player.sendMessage("Voice chat not connected");
            return true;
        }

        VoicechatConnection playerConnection = voicechatServerApi.getConnection(player.getUniqueId());

        if (playerConnection == null) {
            player.sendMessage("Voice chat not connected");
            return true;
        }

        Group group;

        if (playerConnection.isInGroup()) {
            group = playerConnection.getGroup();
        } else {
            group = voicechatServerApi.groupBuilder()
                    .setName("broadcast")
                    .setType(Group.Type.OPEN)
                    .build();
        }

        List<Player> nearbyPlayers = player.getWorld().getPlayers();

        for (Player nearbyPlayer : nearbyPlayers) {
            VoicechatConnection connection = voicechatServerApi.getConnection(nearbyPlayer.getUniqueId());
            if (connection != null && !connection.isInGroup()) {
                connection.setGroup(group);
            }
        }

        return true;
    }
}
