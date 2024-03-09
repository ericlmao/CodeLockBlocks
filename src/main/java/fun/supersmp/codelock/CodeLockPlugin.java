package fun.supersmp.codelock;

import fun.supersmp.codelock.command.CommandCodeLockBlock;
import fun.supersmp.codelock.core.Locale;
import fun.supersmp.codelock.listener.PlayerListener;
import games.negative.alumina.AluminaPlugin;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class CodeLockPlugin extends AluminaPlugin {

    private static CodeLockPlugin instance;

    @Override
    public void load() {
        instance = this;
    }

    @SneakyThrows
    @Override
    public void enable() {
        Locale.init(this);

        registerCommand(new CommandCodeLockBlock());
        registerListeners(new PlayerListener());
    }

    @Override
    public void disable() {

    }

    @NotNull
    public static CodeLockPlugin instance() {
        return instance;
    }

}
