package de.maxhenkel.voicechat_broadcast;

import de.maxhenkel.voicechat.api.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GroupCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Usage: /creategroup <name> [password] [persistent]");
            return true;
        }

        String name = args[0];
        String password = args.length >= 2 ? args[1] : null;
        boolean persistent = args.length >= 3 && Boolean.parseBoolean(args[2]);

        VoicechatApi voicechatApi = VoicechatApi.getInstance();

        if (voicechatApi == null) {
            player.sendMessage("Error: Voicechat API not available.");
            return true;
        }

        VoicechatServerApi voicechatServerApi = voicechatApi.getServerApi();

        if (voicechatServerApi == null) {
            player.sendMessage("Error: Voicechat server API not available.");
            return true;
        }

        Group.Builder groupBuilder = voicechatServerApi.groupBuilder()
                .setName(name)
                .setPassword(password)
                .setPersistent(persistent)
                .setType(Group.Type.ISOLATED); // Set the group type here

        Group group = groupBuilder.build();

        voicechatServerApi.getVoicechatConnection(player.getUniqueId()).setGroup(group);

        player.sendMessage("Group created successfully.");

        return true;
    }
}
