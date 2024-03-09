package fun.supersmp.codelock.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import fun.supersmp.codelock.CodeLockPlugin;
import games.negative.alumina.logger.Logs;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public enum Locale {
    BLOCK_PLACE_NOTIFY("<gold><b>ATTENTION!</b> <gray>You have placed a block that can be locked with a secret access code! <newline><newline><i>Shift + Left-Click the block to open the code menu!"),
    CODE_CANNOT_BE_EMPTY("<gold><b>ATTENTION!</b> <gray>The code that you have entered cannot be empty!"),
    CODE_SAVED("<gold><b>ATTENTION!</b> <gray>The code that you have entered has been saved!"),
    CODE_SAME("<gold><b>ATTENTION!</b> <gray>The code you entered was the same as your current code. No changes were saved."),
    CODE_INCORRECT("<gold><b>ATTENTION!</b> <gray>The code that you have entered is <red>incorrect</red>. Please try again!"),
    CODE_CORRECT("<gold><b>ATTENTION!</b> <gray>The code that you have entered is <green>correct</green>. You have been granted access!"),

    ALERTS_ON("<gold><b>ATTENTION!</b> <gray>You have toggled alerts <green>on</green>!"),
    ALERTS_OFF("<gold><b>ATTENTION!</b> <gray>You have toggled alerts <red>off</red>!"),

    CODE_BLOCK_COMMAND("<newline><gold><b> CODELOCK <white>BLOCKS</white></gold> <newline> <gray><i> By Negative Games <newline><newline><yellow> /codeblock <white>togglealerts<white> <dark_gray>-</dark_gray> <gray>Toggle place notifications.");

    private String content;

    Locale(@NotNull String... defMessage) {
        this.content = String.join("\n", defMessage);
    }

    public static void init(@NotNull CodeLockPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        validateFile(file);

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        boolean changed = false;
        for (Locale entry : values()) {
            if (config.isSet(entry.name())) continue;

            List<String> message = List.of(entry.content.split("\n"));
            config.set(entry.name(), message);
            changed = true;
        }

        if (changed) saveFile(file, config);

        for (Locale entry : values()) {
            entry.content = String.join("\n", config.getStringList(entry.name()));
        }
    }

    private static void saveFile(@NotNull File file, @NotNull FileConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            Logs.SEVERE.print("Could not save messages.yml file!", true);
        }
    }

    private static void validateFile(@NotNull File file) {
        if (!file.exists()) {
            boolean dirSuccess = file.getParentFile().mkdirs();
            if (dirSuccess) Logs.INFO.print("Created new plugin directory file!");

            try {
                boolean success = file.createNewFile();
                if (!success) return;

                Logs.INFO.print("Created messages.yml file!");
            } catch (IOException e) {
                Logs.SEVERE.print("Could not create messages.yml file!", true);
            }
        }
    }


    public void send(@NotNull CommandSender sender, @Nullable String... placeholders) {
        MiniMessage mm = MiniMessage.miniMessage();

        Map<String, String> placeholderMap = Maps.newHashMap();

        Component component = mm.deserialize(sender instanceof Player ? PlaceholderAPI.setPlaceholders((Player) sender, content) : PlaceholderAPI.setPlaceholders(null, content));
        if (placeholders != null) {
            Preconditions.checkArgument(placeholders.length % 2 == 0, "Placeholders must be in key-value pairs.");

            for (int i = 0; i < placeholders.length; i += 2) {
                placeholderMap.put(placeholders[i], placeholders[i + 1]);
            }
        }

        for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
            component = component.replaceText(TextReplacementConfig.builder().matchLiteral(entry.getKey()).replacement(entry.getValue()).build());
        }

        sender.sendMessage(component);
    }

    public <T extends Iterable<? extends CommandSender>> void send(T iterable, @Nullable String... placeholders) {
        for (CommandSender sender : iterable) {
            send(sender, placeholders);
        }
    }

    public void broadcast(@Nullable String... placeholders) {
        send(Bukkit.getOnlinePlayers(), placeholders);
    }

}