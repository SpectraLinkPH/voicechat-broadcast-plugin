package de.maxhenkel.voicechat_broadcast;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class BroadcastVoicechatPlugin implements VoicechatPlugin {

    /**
     * Only OPs have the broadcast permission by default
     */
    public static Permission BROADCAST_PERMISSION = new Permission("voicechat_broadcast.broadcast", PermissionDefault.OP);

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

        // Cast the generic player object of the voice chat API to an actual Bukkit player
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

        // Adjust the volume of the broadcast
        float volume = 1.0f; // Adjust the volume level as desired (between 0.0f and 1.0f)
        Sound sound = event.getPacket().toSound();
        Location location = player.getLocation();
        
        // Play the broadcast sound at the player's location with the adjusted volume
        player.playSound(location, sound, SoundCategory.VOICE, volume, 1.0f);

        // Iterate over every player on the server
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            // Don't send the audio to the player that is broadcasting
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            
            // Adjust the volume level for each player as desired
            float playerVolume = 1.0f; // Adjust the volume level as desired (between 0.0f and 1.0f)
            
            // Play the broadcast sound at the online player's location with the adjusted volume
            onlinePlayer.playSound(location, sound, SoundCategory.VOICE, playerVolume, 1.0f);
        }
    }

}
