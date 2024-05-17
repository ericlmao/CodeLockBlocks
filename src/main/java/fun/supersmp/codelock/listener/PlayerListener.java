package fun.supersmp.codelock.listener;

import com.google.common.collect.Lists;
import fun.supersmp.codelock.core.Keys;
import fun.supersmp.codelock.core.Locale;
import fun.supersmp.codelock.core.Perm;
import fun.supersmp.codelock.core.Util;
import fun.supersmp.codelock.struct.CodeLockBlock;
import fun.supersmp.codelock.ui.EditCodeMenu;
import fun.supersmp.codelock.ui.EnterCodeMenu;
import games.negative.alumina.util.NBTEditor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerListener implements Listener {

    private final FloodgateApi api;

    public PlayerListener() {
        this.api = FloodgateApi.getInstance();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        Location location = adjustLocation(block.getLocation(), block.getBlockData());
        if (location == null) return;

        Jukebox jukebox = parse(location);
        if (jukebox == null) return;

        String current = NBTEditor.get(jukebox, Keys.OWNER, PersistentDataType.STRING);
        if (current != null && !player.getUniqueId().equals(UUID.fromString(current))) {
            // The user cannot place a code lock on a jukebox that is already owned by someone else
            // this function is primarily for the use-case of fence-gates with a two-high gateway
            event.setCancelled(true);
            return;
        }

        NBTEditor.set(jukebox, Keys.OWNER, PersistentDataType.STRING, player.getUniqueId().toString());
        jukebox.update(true);

        if (hasNotificationsMuted(player)) return;

        Locale.BLOCK_PLACE_NOTIFY.send(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Location location = adjustLocation(block.getLocation(), block.getBlockData());
        if (location == null) return;

        Jukebox jukebox = parse(location);
        if (jukebox == null) return;

        CodeLockBlock data = parseCodeLockBlock(jukebox);
        if (data == null) return;

        UUID uuid = player.getUniqueId();
        if (!isOwner(data, uuid) && !player.hasPermission(Perm.ADMIN)) {
            event.setCancelled(true);
            return;
        }

        // Remove the data from the jukebox
        NBTEditor.remove(jukebox, Keys.OWNER);
        NBTEditor.remove(jukebox, Keys.CODE);
        NBTEditor.remove(jukebox, Keys.AUTHORIZED_USERS);
        jukebox.update(true);
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        Location location = adjustLocation(block.getLocation(), block.getBlockData());
        if (location == null) return;

        Jukebox jukebox = parse(location);
        if (jukebox == null) return;

        CodeLockBlock data = parseCodeLockBlock(jukebox);
        if (data == null) return;

        UUID uuid = player.getUniqueId();
        if (player.hasPermission(Perm.ADMIN) || data.authorized().contains(uuid)) {
            if (!isOwner(data, uuid)) return;

            boolean notHoldingAir = !player.getInventory().getItemInMainHand().getType().isAir();

            // The owner must be sneaking, left-clicking, and holding nothing to open the code menu
            if (!player.isSneaking() || !event.getAction().equals(Action.LEFT_CLICK_BLOCK) || notHoldingAir) return;

            // we're going to open the code menu to modify the code and authorized users
            new EditCodeMenu(jukebox, data.authorized(), data.code()).open(player);
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);

        new EnterCodeMenu(jukebox, data.code()).open(player);
    }

    /**
     * Adjusts the location based on the type of BlockData.
     *
     * @param location the current location of the block. Cannot be null.
     * @param data the BlockData object representing the block. Cannot be null.
     * @return the adjusted location of the block based on its type.
     * If the block is of type Door, returns the location adjusted by Util.adjustDoorLocation().
     * If the block is of type
     * Gate,
     * returns the location adjusted by Util.adjustGateLocation().
     * If the block is of type TrapDoor, returns the location adjusted by Util.adjustTrapDoorLocation().
     * Otherwise, returns
     * null.
     * @throws NullPointerException if either the location or data parameter is null.
     */
    @Nullable
    private Location adjustLocation(@NotNull Location location, @NotNull BlockData data) {
        Location loc = location.clone();

        if (data instanceof Door door) {
            // Door specific logic
            return Util.adjustDoorLocation(loc, door);
        }

        if (data instanceof Gate) {
            // Gate specific logic
            return Util.adjustGateLocation(loc);
        }

        if (data instanceof TrapDoor door) {
            // TrapDoor specific logic
            return Util.adjustTrapDoorLocation(loc, door);
        }

        return null;
    }

    /**
     * Parses a Location object and returns a Jukebox object if the block at the location is a Jukebox.
     *
     * @param location the location to parse. Cannot be null.
     * @return a Jukebox object if the block at the location is a Jukebox, otherwise null.
     * @throws NullPointerException if the location parameter is null.
     */
    @Nullable
    private Jukebox parse(@NotNull Location location) {
        Block block = location.getBlock();
        BlockState state = block.getState();

        if (state instanceof Jukebox jukebox) {
            return jukebox;
        }
        return null;
    }

    /**
     * Parses a Jukebox block and returns a CodeLockBlock object.
     *
     * @param block the Jukebox block to parse. Cannot be null.
     * @return a CodeLockBlock object if the block has an owner, otherwise null.
     * @throws NullPointerException if the block parameter is null.
     */
    @Nullable
    private CodeLockBlock parseCodeLockBlock(@NotNull Jukebox block) {
        String ownerRaw = NBTEditor.get(block, Keys.OWNER, PersistentDataType.STRING);
        if (ownerRaw == null) return null; // we REQUIRE an owner

        UUID owner = UUID.fromString(ownerRaw);

        String code = NBTEditor.get(block, Keys.CODE, PersistentDataType.STRING); // we do not require a code

        List<String> authorizedRaw = NBTEditor.getOrDefault(block, Keys.AUTHORIZED_USERS, PersistentDataType.LIST.strings(), Lists.newArrayList(ownerRaw));
        List<UUID> authorizedUsers = authorizedRaw.stream().map(UUID::fromString).collect(Collectors.toCollection(Lists::newArrayList));
        if (!authorizedUsers.contains(owner)) authorizedUsers.add(owner); // Ensure the owner is always authorized!

        return new CodeLockBlock(owner, code, authorizedUsers);
    }

    /**
     * Checks if the given UUID is the owner of the CodeLockBlock.
     *
     * @param data the CodeLockBlock object representing the block data. Cannot be null.
     * @param uuid the UUID of the player to check if they are the owner. Cannot be null.
     * @return true if the UUID is the owner of the CodeLockBlock, false otherwise.
     * @throws NullPointerException if either the data or uuid parameter is null.
     */
    private boolean isOwner(@NotNull CodeLockBlock data, @NotNull UUID uuid) {
        return data.owner().equals(uuid);
    }

    /**
     * Checks if the player has notifications muted.
     *
     * @param player the player to check. Cannot be null.
     * @return true if the player has notifications muted, false otherwise.
     * @throws NullPointerException if the player parameter is null.
     */
    private boolean hasNotificationsMuted(@NotNull Player player) {
        return NBTEditor.has(player, Keys.MUTE_NOTIFICATIONS, PersistentDataType.BYTE);
    }
}
