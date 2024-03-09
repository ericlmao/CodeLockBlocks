package fun.supersmp.codelock.core;

import games.negative.alumina.logger.Logs;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Util {

    /**
     * Adjusts the location of the door based on its half.
     *
     * @param location the current location of the door. Cannot be null.
     * @param door the door object whose half is used to adjust the location. Cannot be null.
     * @return the adjusted location of the door. If the half is BOTTOM, returns the location moved one block down with y-coordinate subtracted by 1.
     * Otherwise, returns the location moved two blocks down with y-coordinate subtracted by 2.
     * @throws NullPointerException if either the location or door parameter is null.
     */
    @NotNull
    public Location adjustDoorLocation(@NotNull Location location, @NotNull Door door) {
        Bisected.Half half = door.getHalf();

        boolean isBottom = half == Bisected.Half.BOTTOM;

        return location.subtract(0, isBottom ? 1 : 2, 0);
    }

    /**
     * Adjusts the location of the gate by subtracting 1 from the y-coordinate of the given location.
     *
     * @param location the current location of the gate. Cannot be null.
     * @return the adjusted location of the gate with a decreased y-coordinate by 1.
     * @throws NullPointerException if the location parameter is null.
     */
    @NotNull
    public static Location adjustGateLocation(@NotNull Location location) {
        Location one = location.subtract(0, 1, 0);
        if (one.getBlock().getState() instanceof Jukebox)
            return one;

        Location two = location.subtract(0, 1, 0);
        if (two.getBlock().getState() instanceof Jukebox)
            return two;

        return one;
    }

    /**
     * Adjusts the location of the trap door based on its facing direction.
     *
     * @param location the current location of the trap door. Cannot be null.
     * @param door the trap door object whose facing direction is used to adjust the location. Cannot be null.
     * @return the adjusted location of the trap door based on its facing direction. If the facing direction is NORTH,
     * returns the location moved one block to the south. If the facing direction is EAST, returns the location moved one block to the west.
     * If the facing direction is SOUTH, returns the location moved one block to the north. If the facing direction is WEST,
     * returns the location moved one block to the east.
     * @throws NullPointerException if either the location or door parameter is null.
     */
    @NotNull
    public static Location adjustTrapDoorLocation(@NotNull Location location, @NotNull TrapDoor door) {
        BlockFace opposing = door.getFacing().getOppositeFace();
        return location.getBlock().getRelative(opposing).getLocation();
    }
}
