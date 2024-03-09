package fun.supersmp.codelock.struct;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record CodeLockBlock(@NotNull UUID owner, @Nullable String code, @NotNull List<UUID> authorized) {
}
