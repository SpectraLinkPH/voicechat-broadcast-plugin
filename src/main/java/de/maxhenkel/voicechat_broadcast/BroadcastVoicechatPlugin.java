package de.maxhenkel.voicechat_broadcast;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class BroadcastVoicechatPlugin implements VoicechatPlugin {

    /**
     * Only OPs have the broadcast permission by default
     */
    public static Permission BROADCAST_PERMISSION = new Permission("voicechat_broadcast.broadcast", PermissionDefault.OP);
    public static Permission VOLUME_PERMISSION = new Permission("voicechat_broadcast.volume", PermissionDefault.OP);

    private double broadcastVolume = 1.0;

    /**
     * @return the unique ID for this voice chat plugin
     */
    @Override
    public String getPluginId() {
        return "voicechat_broadcast.PLUGIN_ID";
    }

    /**
     * Called when the voice chat initializes the plugin.
     *
     * @param api the voice chat API
     */
    @Override
    public void initialize(VoicechatApi api) {

    }

    /**
     * Called once by the voice chat to register all events.
     *
     * @param registration the event registration
     */
    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    /**
     * This method is called whenever a player sends audio to the server via the voice chat.
     *
     * @param event the microphone packet event
     */
    private void onMicrophone(MicrophonePacketEvent event) {
        // The connection might be null if the event is caused by other means
        if (event.getSenderConnection() == null) {
            return;
        }
        // Cast the generic player object of the voice chat API to an actual bukkit player
        // This object should always be a bukkit player object on bukkit based servers
        if (!(event.getSenderConnection().getPlayer().getPlayer() instanceof Player player)) {
            return;
        }

        // Check if the player has the broadcast permission
        if (!player.hasPermission(BROADCAST_PERMISSION)) {
            return;
        }

        Group group = event.getSenderConnection().getGroup();

        // Check if the player sending the audio is actually in a group
        if (group == null) {
            return;
        }

        // Only broadcast the voice when the group name is "broadcast"
        if (!group.getName().strip().equalsIgnoreCase("broadcast")) {
            return;
        }

        // Cancel the actual microphone packet event that people in that group or close by don't hear the broadcaster twice
        event.cancel();

        VoicechatServerApi api = event.getVoicechat();

        // Iterating over every player on the server
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            // Don't send the audio to the player that is broadcasting
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            VoicechatConnection connection = api.getConnectionOf(onlinePlayer.getUniqueId());
            // Check if the player is actually connected to the voice chat
            if (connection == null) {
                continue;
            }
            // Apply volume adjustment to the microphone packet
            StaticSoundPacket packet = event.getPacket().toStaticSoundPacket();
            packet.setVolume(packet.getVolume() * broadcastVolume);
            // Send the adjusted audio packet to the connection of each player
            api.sendStaticSoundPacketTo(connection, packet);
        }
    }

    /**
     * Handles the command for adjusting the broadcast volume
     *
     * @param sender  the command sender
     * @param command the command
     * @param label   the command label
     * @param args    the command arguments
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean handleVolumeCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(VOLUME_PERMISSION)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            return false;
        }

        try {
            double volume = Double.parseDouble(args[0]);

            if (volume < 0.0 || volume > 1.0) {
                player.sendMessage(ChatColor.RED + "Volume must be a value between 0.0 and 1.0.");
                return true;
            }

            broadcastVolume = volume;
            player.sendMessage(ChatColor.GREEN + "Broadcast volume set to " + volume);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("broadcastvolume")) {
            return handleVolumeCommand(sender, command, label, args);
        }
        return false;
    }
}
