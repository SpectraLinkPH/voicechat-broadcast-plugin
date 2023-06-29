import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BroadcastVoicechatPlugin extends JavaPlugin implements VoicechatPlugin, CommandExecutor {

    public static Permission BROADCAST_PERMISSION = new Permission("voicechat_broadcast.broadcast", PermissionDefault.OP);
    public static Permission MUTE_PERMISSION = new Permission("voicechat_broadcast.mute", PermissionDefault.OP);

    private Set<UUID> mutedPlayers;

    public BroadcastVoicechatPlugin() {
        mutedPlayers = new HashSet<>();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginCommand("mutebroadcast").setExecutor(this);
    }

    @Override
    public String getPluginId() {
        return "voicechat_broadcast";
    }

    @Override
    public void initialize(VoicechatApi api) {
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) {
            return;
        }
        if (!(event.getSenderConnection().getPlayer().getPlayer() instanceof Player player)) {
            return;
        }

        if (!player.hasPermission(BROADCAST_PERMISSION)) {
            return;
        }

        Group group = event.getSenderConnection().getGroup();
        if (group == null) {
            return;
        }

        if (!group.getName().strip().equalsIgnoreCase("broadcast")) {
            return;
        }

        event.cancel();

        VoicechatServerApi api = event.getVoicechat();

        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            if (mutedPlayers.contains(onlinePlayer.getUniqueId())) {
                continue;
            }
            VoicechatConnection connection = api.getConnectionOf(onlinePlayer.getUniqueId());
            if (connection == null) {
                continue;
            }
            api.sendStaticSoundPacketTo(connection, event.getPacket().toStaticSoundPacket());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(MUTE_PERMISSION)) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (mutedPlayers.contains(player.getUniqueId())) {
            mutedPlayers.remove(player.getUniqueId());
            sender.sendMessage("You have unmuted the broadcast.");
        } else {
            mutedPlayers.add(player.getUniqueId());
            sender.sendMessage("You have muted the broadcast.");
        }

        return true;
    }
}
