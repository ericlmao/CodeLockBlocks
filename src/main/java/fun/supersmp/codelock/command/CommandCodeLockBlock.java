package fun.supersmp.codelock.command;

import fun.supersmp.codelock.core.Keys;
import fun.supersmp.codelock.core.Locale;
import games.negative.alumina.command.Command;
import games.negative.alumina.command.CommandProperties;
import games.negative.alumina.command.Context;
import games.negative.alumina.util.NBTEditor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandCodeLockBlock extends Command {

    public CommandCodeLockBlock() {
        super(CommandProperties.builder().name("codelockblock").aliases(List.of("codeblock")).smartTabComplete(true).build());

        injectSubCommand(CommandProperties.builder().name("togglealerts").playerOnly(true).build(), context -> {
            Player player = context.player().orElseThrow();

            boolean state = NBTEditor.has(player, Keys.MUTE_NOTIFICATIONS);
            if (state) {
                // Turn on alerts
                NBTEditor.remove(player, Keys.MUTE_NOTIFICATIONS);

                Locale.ALERTS_ON.send(player);
            } else {
                // Turn off alerts
                NBTEditor.set(player, Keys.MUTE_NOTIFICATIONS, PersistentDataType.BYTE, (byte) 1);

                Locale.ALERTS_OFF.send(player);
            }
        });
    }

    @Override
    public void execute(@NotNull Context context) {
        Locale.CODE_BLOCK_COMMAND.send(context.sender());
    }
}
