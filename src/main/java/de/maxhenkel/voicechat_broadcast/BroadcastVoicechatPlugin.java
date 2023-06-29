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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BroadcastVoicechatPlugin implements VoicechatPlugin {

    /**
     * Only OPs have the broadcast permission by default
     */
    public static Permission BROADCAST_PERMISSION = new Permission("voicechat_broadcast.broadcast", PermissionDefault.OP);

    private Set<UUID> mutedPlayers;

    public BroadcastVoicechatPlugin() {
        this.mutedPlayers = new HashSet<>();
    }

    /**
     * @return the unique ID for this voice chat plugin
     */
    @Override
    public String getPluginId() {
        return VoicechatBroadcast.PLUGIN_ID;
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
            // Don't send the audio to the player that is broadcasting or to muted players
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId()) || isPlayerMuted(onlinePlayer.getUniqueId())) {
                continue;
            }
            VoicechatConnection connection = api.getConnectionOf(onlinePlayer.getUniqueId());
            // Check if the player is actually connected to the voice chat
            if (connection == null) {
                continue;
            }
            // Send a static audio packet of the microphone data to the connection of each player
            api.sendStaticSoundPacketTo(connection, event.getPacket().toStaticSoundPacket());
        }
    }

    /**
     * Check if a player is muted
     *
     * @param playerUUID the UUID of the player to check
     * @return true if the player is muted, false otherwise
     */
    public boolean isPlayerMuted(UUID playerUUID) {
        return mutedPlayers.contains(playerUUID);
    }

    /**
     * Mute a player
     *
     * @param playerUUID the UUID of the player to mute
     */
    public void mutePlayer(UUID playerUUID) {
        mutedPlayers.add(playerUUID);
    }

    /**
     * Unmute a player
     *
     * @param playerUUID the UUID of the player to unmute
     */
    public void unmutePlayer(UUID playerUUID) {
        mutedPlayers.remove(playerUUID);
    }
    
    /**
     * Handle the command to mute/unmute the broadcast
     *
     * @param sender the command sender
     * @param command the command object
     * @param label the command label
     * @param args the command arguments
     * @return true if the command was handled, false otherwise
     */
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(BROADCAST_PERMISSION)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (isPlayerMuted(player.getUniqueId())) {
            unmutePlayer(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Broadcast unmuted.");
        } else {
            mutePlayer(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Broadcast muted.");
        }

        return true;
    }

}
