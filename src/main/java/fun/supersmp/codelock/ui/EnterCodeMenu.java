package fun.supersmp.codelock.ui;

import com.google.common.collect.Lists;
import fun.supersmp.codelock.core.Keys;
import fun.supersmp.codelock.core.Locale;
import games.negative.alumina.builder.ItemBuilder;
import games.negative.alumina.menu.ChestMenu;
import games.negative.alumina.menu.MenuButton;
import games.negative.alumina.util.IntList;
import games.negative.alumina.util.NBTEditor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EnterCodeMenu extends ChestMenu {

    private static final int CODE_CHARACTER_LIMIT = 5;

    private final Jukebox data;
    private final String code;
    private String current;
    public EnterCodeMenu(@NotNull Jukebox data, @Nullable String code) {
        this.code = (code == null ? "" : code);
        this.current = "";
        this.data = data;

        setRows(6);
        title();
        setCancelClicks(true);

        List<Integer> fillerSlots = IntList.getList(List.of("0-11", "15-20", "24-29", "33-39", "41-47", "49", "51-53"));
        for (int slot : fillerSlots) {
            addButton(MenuButton.builder().slot(slot).item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build()).build());
        }

        // 0-9 combination items
        addButton(MenuButton.builder().slot(12).item(new ItemBuilder(Material.RED_WOOL).setName("&a1").build()).action(new NumberClickHandler('1')).build());
        addButton(MenuButton.builder().slot(13).item(new ItemBuilder(Material.ORANGE_WOOL).setName("&a2").build()).action(new NumberClickHandler('2')).build());
        addButton(MenuButton.builder().slot(14).item(new ItemBuilder(Material.YELLOW_WOOL).setName("&a3").build()).action(new NumberClickHandler('3')).build());
        addButton(MenuButton.builder().slot(21).item(new ItemBuilder(Material.GREEN_WOOL).setName("&a4").build()).action(new NumberClickHandler('4')).build());
        addButton(MenuButton.builder().slot(22).item(new ItemBuilder(Material.BLUE_WOOL).setName("&a5").build()).action(new NumberClickHandler('5')).build());
        addButton(MenuButton.builder().slot(23).item(new ItemBuilder(Material.LIME_WOOL).setName("&a6").build()).action(new NumberClickHandler('6')).build());
        addButton(MenuButton.builder().slot(30).item(new ItemBuilder(Material.LIGHT_BLUE_WOOL).setName("&a7").build()).action(new NumberClickHandler('7')).build());
        addButton(MenuButton.builder().slot(31).item(new ItemBuilder(Material.PINK_WOOL).setName("&a8").build()).action(new NumberClickHandler('8')).build());
        addButton(MenuButton.builder().slot(32).item(new ItemBuilder(Material.WHITE_WOOL).setName("&a9").build()).action(new NumberClickHandler('9')).build());
        addButton(MenuButton.builder().slot(40).item(new ItemBuilder(Material.BLACK_WOOL).setName("&a0").build()).action(new NumberClickHandler('0')).build());

        // Backspace, Save, and Authorized Users buttons
        addButton(MenuButton.builder().slot(48).item(new ItemBuilder(Material.ARROW).setName("&4&l← &c&lBackspace").build()).action(new BackspaceClickHandler()).build());
        addButton(MenuButton.builder().slot(50).item(new ItemBuilder(Material.OAK_DOOR).setName("&2&l✔ &a&lSubmit").build()).action(new SubmitClickHandler()).build());
    }

    @RequiredArgsConstructor
    public class NumberClickHandler implements MenuButton.ClickAction {

        private final char character;

        @Override
        public void onClick(@NotNull MenuButton button, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (current.length() == CODE_CHARACTER_LIMIT) return;

            current += character;

            title();
        }
    }

    public class BackspaceClickHandler implements MenuButton.ClickAction {

        @Override
        public void onClick(@NotNull MenuButton button, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (current.isEmpty()) return;

            current = current.substring(0, current.length() - 1);
            title();
        }
    }

    public class SubmitClickHandler implements MenuButton.ClickAction {

        @Override
        public void onClick(@NotNull MenuButton button, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (current.isEmpty() || current.length() == CODE_CHARACTER_LIMIT) return;

            boolean unlocked = current.equals(code);
            if (!unlocked) {
                player.closeInventory();

                Locale.CODE_INCORRECT.send(player);
                return;
            }

            String ownerRaw = NBTEditor.get(data, Keys.OWNER, PersistentDataType.STRING);
            if (ownerRaw == null) return; // something went wrong!

            UUID uuid = player.getUniqueId();

            // I have to map and remap the authorized users because `authorized` is returned as an immutable list
            // and I cannot add the player's UUID to it!
            List<String> authorized = NBTEditor.getOrDefault(data, Keys.AUTHORIZED_USERS, PersistentDataType.LIST.strings(), Lists.newArrayList(ownerRaw));
            if (authorized.contains(uuid.toString())) return;

            List<UUID> mapped = authorized.stream().map(UUID::fromString).collect(Collectors.toCollection(Lists::newArrayList));
            mapped.add(uuid);

            List<String> remapped = mapped.stream().map(UUID::toString).collect(Collectors.toCollection(Lists::newArrayList));
            NBTEditor.set(data, Keys.AUTHORIZED_USERS, PersistentDataType.LIST.strings(), remapped);

            data.update(true);

            player.closeInventory();

            Locale.CODE_CORRECT.send(player);
        }
    }

    private void title() {
        updateTitle("Enter Code!" + (current.isEmpty() ? "" : " | " + current));
    }

}
