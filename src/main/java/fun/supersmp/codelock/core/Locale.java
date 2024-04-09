package fun.supersmp.codelock.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import fun.supersmp.codelock.CodeLockPlugin;
import games.negative.alumina.logger.Logs;
import games.negative.alumina.message.Message;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
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
import java.util.Objects;

public enum Locale {
    BLOCK_PLACE_NOTIFY("<gold><b>ATTENTION!</b> <gray>You have placed a block that can be locked with a secret access code! <newline><newline><i>Shift + Left-Click the block to open the code menu!"),
    CODE_CANNOT_BE_EMPTY("<gold><b>ATTENTION!</b> <gray>The code that you have entered cannot be empty!"),
    CODE_SAVED("<gold><b>ATTENTION!</b> <gray>The code that you have entered has been saved!"),
    CODE_SAME("<gold><b>ATTENTION!</b> <gray>The code you entered was the same as your current code. No changes were saved."),
    CODE_INCORRECT("<gold><b>ATTENTION!</b> <gray>The code that you have entered is <red>incorrect</red>. Please try again!"),
    CODE_CORRECT("<gold><b>ATTENTION!</b> <gray>The code that you have entered is <green>correct</green>. You have been granted access!"),

    ALERTS_ON("<gold><b>ATTENTION!</b> <gray>You have toggled alerts <green>on</green>!"),
    ALERTS_OFF("<gold><b>ATTENTION!</b> <gray>You have toggled alerts <red>off</red>!"),

    CODE_BLOCK_COMMAND("<newline><gold><b> CODELOCK <white>BLOCKS</white></gold> <newline> <gray><i> By Negative Games <newline><newline><yellow> /codeblock <white>togglealerts<white> <dark_gray>-</dark_gray> <gray>Toggle place notifications."),

    CODE_LENGTH("<gold><b>ATTENTION!</b> <gray>The code that you have entered is <red>too short</red>. Please try again!"),

    ;

    private final String content;
    private Message message;

    Locale(@NotNull String content) {
        this.content = content;
        this.message = Message.of(content);
    }

    public static void init(@NotNull CodeLockPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        validateFile(file);

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        boolean changed = false;
        for (Locale entry : values()) {
            if (config.isSet(entry.name())) continue;

            config.set(entry.name(), entry.content);
            changed = true;
        }

        if (changed) saveFile(file, config);

        for (Locale entry : values()) {
            entry.message = new Message(Objects.requireNonNull(config.getString(entry.name())));
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


    /**
     * Sends a message to a specified audience with optional placeholders.
     *
     * @param audience     the audience to send the message to
     * @param placeholders the optional placeholders to be replaced in the message
     */
    public void send(@NotNull Audience audience, @Nullable String... placeholders) {
        message.send(audience, placeholders);
    }

    /**
     * Sends a message to a collection of audiences.
     *
     * @param iterable the collection of audiences to send the message to
     * @param <T> the type of iterable must extend Iterable<? extends Audience>
     * @throws NullPointerException if the iterable is null
     */
    public <T extends Iterable<? extends Audience>> void send(T iterable) {
        message.send(iterable);
    }

    /**
     * Broadcasts a message to all players on the server.
     *
     * @param placeholders an array of optional placeholders to replace in the message (nullable)
     */
    public void broadcast(@Nullable String... placeholders) {
        message.broadcast(placeholders);
    }

    /**
     * Returns the message as a component with optional placeholders.
     * @param audience the audience to send the message to
     * @param placeholders the optional placeholders to be replaced in the message
     * @return the message as a component
     */
    @NotNull
    public Component asComponent(@Nullable Audience audience, @Nullable String... placeholders) {
        return message.asComponent(audience, placeholders);
    }

}