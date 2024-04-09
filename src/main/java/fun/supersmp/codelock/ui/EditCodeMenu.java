package fun.supersmp.codelock.ui;

import fun.supersmp.codelock.core.Keys;
import fun.supersmp.codelock.core.Locale;
import games.negative.alumina.builder.ItemBuilder;
import games.negative.alumina.menu.ChestMenu;
import games.negative.alumina.menu.MenuButton;
import games.negative.alumina.util.IntList;
import games.negative.alumina.util.ItemUpdater;
import games.negative.alumina.util.NBTEditor;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class EditCodeMenu extends ChestMenu {

    private static final int CODE_CHARACTER_LIMIT = 5;

    private final Jukebox data;
    private final List<UUID> users;
    private final String original;
    private String code;
    private final boolean isBedrockUser;
    private MenuButton display = null;
    public EditCodeMenu(@NotNull Jukebox data, @NotNull List<UUID> users, @Nullable String code, boolean isBedrockUser) {
        this.code = (code == null ? "" : code);
        this.original = code;
        this.data = data;
        this.users = users;
        this.isBedrockUser = isBedrockUser;

        setRows(6);
        title();
        setCancelClicks(true);

        // Only add the "display" button if the user is a Bedrock user
        // due to differences in packets sent to the client
        if (this.isBedrockUser) {
            display = MenuButton.builder().slot(25).item(new ItemBuilder(Material.PAPER).setName("&f ").build()).build();
            addButton(display);

            updateDisplayItem();
        }

        List<Integer> fillerSlots = IntList.getList(List.of("0-11", "15-20", "24-29", "33-39", "41-46", "48", "50", "52-53"));
        for (int slot : fillerSlots) {
            // Probably a better way to do this,
            // but this is straightforward & quick.
            if (this.isBedrockUser && slot == 25) continue;

            addButton(MenuButton.builder().slot(slot).item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build()).build());
        }

        // 0-9 combination items
        addButton(MenuButton.builder().slot(12).item(new ItemBuilder(Material.RED_WOOL).setName("<green>1").build()).action(new NumberClickHandler('1')).build());
        addButton(MenuButton.builder().slot(13).item(new ItemBuilder(Material.ORANGE_WOOL).setName("<green>2").build()).action(new NumberClickHandler('2')).build());
        addButton(MenuButton.builder().slot(14).item(new ItemBuilder(Material.YELLOW_WOOL).setName("<green>3").build()).action(new NumberClickHandler('3')).build());
        addButton(MenuButton.builder().slot(21).item(new ItemBuilder(Material.GREEN_WOOL).setName("<green>4").build()).action(new NumberClickHandler('4')).build());
        addButton(MenuButton.builder().slot(22).item(new ItemBuilder(Material.BLUE_WOOL).setName("<green>5").build()).action(new NumberClickHandler('5')).build());
        addButton(MenuButton.builder().slot(23).item(new ItemBuilder(Material.LIME_WOOL).setName("<green>6").build()).action(new NumberClickHandler('6')).build());
        addButton(MenuButton.builder().slot(30).item(new ItemBuilder(Material.LIGHT_BLUE_WOOL).setName("<green>7").build()).action(new NumberClickHandler('7')).build());
        addButton(MenuButton.builder().slot(31).item(new ItemBuilder(Material.PINK_WOOL).setName("<green>8").build()).action(new NumberClickHandler('8')).build());
        addButton(MenuButton.builder().slot(32).item(new ItemBuilder(Material.WHITE_WOOL).setName("<green>9").build()).action(new NumberClickHandler('9')).build());
        addButton(MenuButton.builder().slot(40).item(new ItemBuilder(Material.BLACK_WOOL).setName("<green>0").build()).action(new NumberClickHandler('0')).build());

        // Backspace, Save, and Authorized Users buttons
        addButton(MenuButton.builder().slot(47).item(new ItemBuilder(Material.ARROW).setName("<dark_red><bold>←</dark_red> <red><bold>Backspace").build()).action(new BackspaceClickHandler()).build());
        addButton(MenuButton.builder().slot(49).item(new ItemBuilder(Material.WRITABLE_BOOK).setName("<dark_green><bold>✔</dark_green> <green><bold>Save Code").build()).action(new SaveClickHandler()).build());
        addButton(MenuButton.builder().slot(51).item(new ItemBuilder(Material.PLAYER_HEAD).setName("<dark_aqua><bold>✯</dark_aqua> <aqua><bold>Authorized Users").build()).action(new AuthorizedUsersMenuClickHandler()).build());
    }

    @RequiredArgsConstructor
    public class NumberClickHandler implements MenuButton.ClickAction {

        private final char character;

        @Override
        public void onClick(@NotNull MenuButton button, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (code == null) code = "";

            if (code.length() == CODE_CHARACTER_LIMIT) return;

            code += character;

            if (isBedrockUser) {
                updateDisplayItem();
                return;
            }

            title();
        }
    }

    public class BackspaceClickHandler implements MenuButton.ClickAction {

        @Override
        public void onClick(@NotNull MenuButton button, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (code == null || code.isEmpty()) return;

            code = code.substring(0, code.length() - 1);

            if (isBedrockUser) {
                updateDisplayItem();
                return;
            }

            title();
        }
    }

    public class SaveClickHandler implements MenuButton.ClickAction {

        @Override
        public void onClick(@NotNull MenuButton button, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (code.isEmpty()) {
                Locale.CODE_CANNOT_BE_EMPTY.send(player);
                return;
            }

            if (code.length() != CODE_CHARACTER_LIMIT) {
                Locale.CODE_LENGTH.send(player);
                return;
            }

            if (original != null && original.equals(code)) {
                player.closeInventory();

                Locale.CODE_SAME.send(player);
                return;
            }

            NBTEditor.set(data, Keys.CODE, PersistentDataType.STRING, code);
            NBTEditor.remove(data, Keys.AUTHORIZED_USERS);

            data.update(true);

            player.closeInventory();

            Locale.CODE_SAVED.send(player);
        }
    }

    public class AuthorizedUsersMenuClickHandler implements MenuButton.ClickAction {

        @Override
        public void onClick(@NotNull MenuButton button, @NotNull Player player, @NotNull InventoryClickEvent event) {
            new AuthorizedUsersMenu(data, users, code, isBedrockUser).open(player);
        }
    }

    private void title() {
        updateTitle("Enter Code!" + ((code == null || code.isEmpty()) ? "" : " | " + code));
    }
    
    private void updateDisplayItem() {
        display.updateItem(itemStack -> {
            ItemUpdater.of(itemStack, meta -> {
                if (code == null || code.isEmpty()) {
                    meta.displayName(Component.text(" ").color(NamedTextColor.WHITE));
                    return;
                }

                // Split all characters of "code" into an array
                char[] chars = code.toCharArray();

                TextComponent.Builder builder = Component.text();
                for (char aChar : chars) {
                    builder.append(Component.text(aChar).color(NamedTextColor.GREEN));
                }

                meta.displayName(builder.build());
            });

            return itemStack;
        });
        refreshButton(display.getSlot());
    }
}
