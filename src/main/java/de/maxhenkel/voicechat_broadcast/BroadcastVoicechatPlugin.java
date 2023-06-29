import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

public class BroadcastVoicechatPlugin implements VoicechatPlugin {

    public static Permission BROADCAST_PERMISSION = new Permission("voicechat_broadcast.broadcast", PermissionDefault.OP);
    public static Permission MUTE_PERMISSION = new Permission("voicechat_broadcast.mute", PermissionDefault.OP);

    private Inventory muteInventory;

    public BroadcastVoicechatPlugin() {
        muteInventory = Bukkit.createInventory(null, 9, "Mute Broadcast");
        initializeMuteInventory();
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

    private void initializeMuteInventory() {
        ItemStack muteAllItem = createItem(Material.REDSTONE_BLOCK, "Mute All");
        ItemStack unmuteAllItem = createItem(Material.EMERALD_BLOCK, "Unmute All");

        muteInventory.setItem(0, muteAllItem);
        muteInventory.setItem(1, unmuteAllItem);
    }

    private ItemStack createItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }

    private void openMuteInventory(Player player) {
        player.openInventory(muteInventory);
    }

    private void muteAllPlayers() {
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (onlinePlayer != null && !onlinePlayer.hasPermission(BROADCAST_PERMISSION) && !onlinePlayer.hasPermission(MUTE_PERMISSION)) {
                mutedPlayers.add(onlinePlayer.getUniqueId());
            }
        }
    }

    private void unmuteAllPlayers() {
        mutedPlayers.clear();
    }

    private void togglePlayerMute(Player player) {
        if (mutedPlayers.contains(player.getUniqueId())) {
            mutedPlayers.remove(player.getUniqueId());
        } else {
            mutedPlayers.add(player.getUniqueId());
        }
    }

    private boolean isPlayerMuted(Player player) {
        return mutedPlayers.contains(player.getUniqueId());
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

        if (isPlayerMuted(player)) {
            return;
        }

        event.cancel();

        VoicechatServerApi api = event.getVoicechat();

        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            VoicechatConnection connection = api.getConnectionOf(onlinePlayer.getUniqueId());
            if (connection == null) {
                continue;
            }
            api.sendStaticSoundPacketTo(connection, event.getPacket().toStaticSoundPacket());
        }
    }
}
