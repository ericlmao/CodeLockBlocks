package fun.supersmp.codelock.core;

import fun.supersmp.codelock.CodeLockPlugin;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;

@UtilityClass
public class Keys {

    public NamespacedKey OWNER = new NamespacedKey(CodeLockPlugin.instance(), "owner");
    public NamespacedKey CODE = new NamespacedKey(CodeLockPlugin.instance(), "code");
    public NamespacedKey AUTHORIZED_USERS = new NamespacedKey(CodeLockPlugin.instance(), "authorized_users");
    public NamespacedKey MUTE_NOTIFICATIONS = new NamespacedKey(CodeLockPlugin.instance(), "mute-notifications");
}
