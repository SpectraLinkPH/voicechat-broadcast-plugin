package de.maxhenkel.voicechat_broadcast.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BroadcastCommand implements CommandExecutor, TabCompleter {

    public static final String BROADCAST_MUTE_PERMISSION = "voicechat_broadcast.mute";

    private final Plugin plugin;
    private final Map<UUID, PermissionAttachment> playerAttachments;

    public BroadcastCommand(Plugin plugin) {
        this.plugin = plugin;
        this.playerAttachments = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /broadcast [mute|unmute]");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        if (subCommand.equals("mute")) {
            return handleMuteCommand(player);
        } else if (subCommand.equals("unmute")) {
            return handleUnmuteCommand(player);
        } else {
            player.sendMessage("Invalid sub-command. Usage: /broadcast [mute|unmute]");
            return true;
        }
    }

    private boolean handleMuteCommand(Player player) {
        if (player.hasPermission(BROADCAST_MUTE_PERMISSION)) {
            player.sendMessage("You already have the broadcast mute permission.");
            return true;
        }

        PermissionAttachment permissionAttachment = playerAttachments.get(player.getUniqueId());
        if (permissionAttachment != null) {
            player.removeAttachment(permissionAttachment);
        }

        permissionAttachment = player.addAttachment(plugin);
        Permission broadcastMutePermission = Bukkit.getPluginManager().getPermission(BROADCAST_MUTE_PERMISSION);
        if (broadcastMutePermission != null) {
            permissionAttachment.setPermission(broadcastMutePermission, true);
            player.recalculatePermissions();
            playerAttachments.put(player.getUniqueId(), permissionAttachment);

            player.sendMessage("You have been granted the broadcast mute permission.");
        } else {
            player.sendMessage("Failed to grant the broadcast mute permission.");
        }

        return true;
    }

    private boolean handleUnmuteCommand(Player player) {
        if (!player.hasPermission(BROADCAST_MUTE_PERMISSION)) {
            player.sendMessage("You do not have the broadcast mute permission.");
            return true;
        }

        PermissionAttachment permissionAttachment = playerAttachments.get(player.getUniqueId());
        if (permissionAttachment != null) {
            player.removeAttachment(permissionAttachment);
            player.recalculatePermissions();
            playerAttachments.remove(player.getUniqueId());

            player.sendMessage("You have been unmuted from broadcast.");

            return true;
        } else {
            player.sendMessage("You are not currently muted from broadcast.");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null; // Implement tab completion logic here if needed
    }

    public PermissionAttachment getPlayerPermissionAttachment(Player player) {
        return playerAttachments.get(player.getUniqueId());
    }

    public void addPlayerPermissionAttachment(Player player, PermissionAttachment attachment) {
        playerAttachments.put(player.getUniqueId(), attachment);
    }

    public void removePlayerPermissionAttachment(Player player) {
        playerAttachments.remove(player.getUniqueId());
    }
}
