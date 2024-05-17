package fun.supersmp.codelock.ui;

import com.google.common.collect.Lists;
import fun.supersmp.codelock.core.Keys;
import games.negative.alumina.builder.ItemBuilder;
import games.negative.alumina.menu.MenuButton;
import games.negative.alumina.menu.PaginatedMenu;
import games.negative.alumina.util.IntList;
import games.negative.alumina.util.NBTEditor;
import games.negative.alumina.util.Tasks;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthorizedUsersMenu extends PaginatedMenu {

    private final Jukebox data;
    private final List<UUID> authorized;
    private final String code;
    public AuthorizedUsersMenu(@NotNull Jukebox data, @NotNull List<UUID> authorized, @Nullable String code) {
        super("Authorized Users", 5);
        setCancelClicks(true);

        this.data = data;
        this.authorized = authorized;
        this.code = code;

        List<Integer> fillerSlots = IntList.getList(List.of("0-9", "17-18", "26-27", "35-44"));
        for (int slot : fillerSlots) {
            addButton(MenuButton.builder().slot(slot).item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build()).build());
        }

        ItemStack next = new ItemBuilder(Material.ARROW).setName("<green><b>Next Page <dark_green><b>→").build();
        setNextPageButton(MenuButton.builder().slot(26).item(next).action((button, player, event) -> changePage(player, page + 1)).build());

        ItemStack previous = new ItemBuilder(Material.ARROW).setName("<dark_red><b>← <red><b>Previous Page").build();
        setPreviousPageButton(MenuButton.builder().slot(18).item(previous).action((button, player, event) -> changePage(player, page - 1)).build());

        setPaginatedSlots(IntList.getList(List.of("10-16", "19-25", "28-34")));

        Collection<MenuButton> buttons = generatePaginatedButtons(authorized, input -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(input);
            String name = player.getName();
            if (name == null) return null;

            ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD);
            builder.setSkullOwner(player);
            builder.setName("<yellow>" + name);
            builder.setLore("<gray>Click to remove this user");

            return MenuButton.builder().item(builder.build()).action(new RemoveUserClickHandler(input)).build();
        });

        setPaginatedButtons(buttons);
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent event) {
        Tasks.run(() -> new EditCodeMenu(data, authorized, code).open(player), 2);
    }

    @RequiredArgsConstructor
    public class RemoveUserClickHandler implements MenuButton.ClickAction {

        private final UUID uuid;

        @Override
        public void onClick(@NotNull MenuButton button, @NotNull Player player, @NotNull InventoryClickEvent event) {
            authorized.remove(uuid);

            List<String> mapped = authorized.stream().map(UUID::toString).collect(Collectors.toCollection(Lists::newArrayList));

            NBTEditor.set(data, Keys.AUTHORIZED_USERS, PersistentDataType.LIST.strings(), mapped);
            data.update(true);

            player.closeInventory();
        }
    }
}
