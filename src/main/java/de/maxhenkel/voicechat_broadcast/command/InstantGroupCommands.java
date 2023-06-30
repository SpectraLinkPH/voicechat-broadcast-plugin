package de.maxhenkel.voicechat_broadcast;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class InstantGroupCommand implements CommandExecutor {

    private final BroadcastVoicechatPlugin plugin;

    public InstantGroupCommand(BroadcastVoicechatPlugin plugin) {
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

        VoicechatConnection playerConnection = voicechatServerApi.getVoicechatConnection(player.getUniqueId());

        if (playerConnection == null) {
            player.sendMessage("Voice chat not connected");
            return true;
        }

        Group group;

        if (playerConnection.isInGroup()) {
            group = playerConnection.getGroup();
        } else {
            group = voicechatServerApi.groupBuilder()
                    .setName(EnhancedGroups.CONFIG.instantGroupName)
                    .setType(Group.Type.OPEN)
                    .build();
        }

        List<Player> nearbyPlayers = player.getWorld().getPlayers();
        
        for (Player nearbyPlayer : nearbyPlayers) {
            VoicechatConnection connection = voicechatServerApi.getVoicechatConnection(nearbyPlayer.getUniqueId());
            if (connection != null && !connection.isInGroup()) {
                connection.setGroup(group);
            }
        }

        return true;
    }
}
