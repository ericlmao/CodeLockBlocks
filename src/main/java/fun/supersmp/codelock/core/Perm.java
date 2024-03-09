package fun.supersmp.codelock.core;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Perm extends Permission {

    private static final String PREFIX = "codelock";

    // Add permissions here
    public static final Perm ADMIN = new Perm("admin");

    public Perm(@NotNull String name, @Nullable String description, @Nullable PermissionDefault defaultValue, @Nullable Map<String, Boolean> children) {
        super(PREFIX + "." + name, description, defaultValue, children);

        Bukkit.getPluginManager().addPermission(this);
    }

    public Perm(@NotNull String name) {
        this(name, null, null, null);
    }

    public Perm(@NotNull String name, @Nullable String description) {
        this(name, description, null, null);
    }

    public Perm(@NotNull String name, @Nullable String description, @Nullable PermissionDefault defaultValue) {
        this(name, description, defaultValue, null);
    }

    public Perm(@NotNull String name, @Nullable String description, @Nullable Map<String, Boolean> children) {
        this(name, description, null, children);
    }

    public Perm(@NotNull String name, @Nullable PermissionDefault defaultValue) {
        this(name, null, defaultValue, null);
    }

    public Perm(@NotNull String name, @Nullable Map<String, Boolean> children) {
        this(name, null, null, children);
    }


}
