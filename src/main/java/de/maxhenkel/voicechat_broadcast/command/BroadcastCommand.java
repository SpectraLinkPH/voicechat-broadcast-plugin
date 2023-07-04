package de.maxhenkel.voicechat_broadcast.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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

        // Check if the player already has the permission
        if (player.hasPermission("voicechat_broadcast.mute")) {
            player.sendMessage("You are already muted from broadcast.");
            return true;
        }

        // Execute the command to set the permission for the player
        String command = "lp user " + player.getName() + " permission set voicechat_broadcast.mute true";
        CommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console, command);

        // Refresh the player's permissions
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.recalculatePermissions();
            player.sendMessage("You have been muted from broadcast.");
        }, 20); // Delay the execution for 1 second (20 ticks)

        return true;
    }

    private boolean handleUnmuteCommand(Player player) {

        // Check if the player is currently muted
        if (!player.hasPermission("voicechat_broadcast.mute")) {
            player.sendMessage("You are not currently muted from broadcast.");
            return true;
        }

        // Execute the command to remove the permission for the player
        String command = "lp user " + player.getName() + " permission unset voicechat_broadcast.mute";
        CommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console, command);

        // Refresh the player's permissions
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.recalculatePermissions();
            player.sendMessage("You have been unmuted from broadcast.");
        }, 20); // Delay the execution for 1 second (20 ticks)

        // Remove the player's attachment
        playerAttachments.remove(player.getUniqueId());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null; // Implement tab completion logic here if needed
    }

    public PermissionAttachment getPlayerPermissionAttachment(UUID playerId) {
        return playerAttachments.get(playerId);
    }
}
