package mastersplinter;

import battlecode.common.*;
import java.util.ArrayList;

public final class Comms {
    private Comms() {}

    // Squeak types encoded in the top 4 bits (>>12)
    public enum SqueakType {
        INVALID,
        ENEMY_RAT_KING,
        ENEMY_COUNT,
        CHEESE_MINE,
        CAT_FOUND,
    }

    public static final SqueakType[] TYPES = SqueakType.values();

    // Shared array layout:
    // [0]=kingX, [1]=kingY
    // [2..] mine list as (x,y) pairs: mine i => [2*i+2]=x, [2*i+3]=y
    public static final int SA_KING_X = 0;
    public static final int SA_KING_Y = 1;

    // Keep a local de-dupe list on the king side (callers pass their own list)
    public static void kingWriteBeacon(RobotController rc, MapLocation loc) throws GameActionException {
        rc.writeSharedArray(SA_KING_X, loc.x);
        rc.writeSharedArray(SA_KING_Y, loc.y);
    }

    public static MapLocation readBeacon(RobotController rc) throws GameActionException {
        int x = rc.readSharedArray(SA_KING_X);
        int y = rc.readSharedArray(SA_KING_Y);
        if (x == 0 && y == 0) return null;
        return new MapLocation(x, y);
    }

    // loc.x,loc.y in 0..63 => pack into 12 bits
    public static int packLoc(MapLocation loc) {
        return (loc.x << 6) | (loc.y & 63);
    }

    public static MapLocation unpackLoc(int packed) {
        int x = packed >> 6;
        int y = packed & 63;
        return new MapLocation(x, y);
    }

    public static int makeSqueak(SqueakType type, int value) {
        return (type.ordinal() << 12) | (value & 0xFFF);
    }

    public static SqueakType squeakType(int raw) {
        return TYPES[raw >> 12];
    }

    public static int squeakValue(int raw) {
        return raw & 0xFFF;
    }

    /** Baby helper: send a mine location. */
    public static void squeakMine(RobotController rc, MapLocation mine) throws GameActionException {
        rc.squeak(makeSqueak(SqueakType.CHEESE_MINE, packLoc(mine)));
    }

    /** Baby helper: send a cat direction (ordinal). */
    public static void squeakCatDir(RobotController rc, Direction toCat) throws GameActionException {
        rc.squeak(makeSqueak(SqueakType.CAT_FOUND, toCat.ordinal()));
    }

    /** Baby helper: send nearby enemy count (<= 4095). */
    public static void squeakEnemyCount(RobotController rc, int count) throws GameActionException {
        rc.squeak(makeSqueak(SqueakType.ENEMY_COUNT, Math.min(count, 4095)));
    }

    /**
     * King helper: read squeaks this round and store new mines in shared array.
     * Pass in your king-side de-dupe list + mine count holder.
     */
    public static int kingReceiveMines(RobotController rc, ArrayList<Integer> seenPackedMines, int numMines) throws GameActionException {
        Message[] squeaks = rc.readSqueaks(rc.getRoundNum());
        for (Message msg : squeaks) {
            int raw = msg.getBytes();
            if (squeakType(raw) != SqueakType.CHEESE_MINE) continue;

            int packed = squeakValue(raw);
            if (seenPackedMines.contains(packed)) continue;

            seenPackedMines.add(packed);
            MapLocation m = unpackLoc(packed);

            rc.writeSharedArray(2 * numMines + 2, m.x);
            rc.writeSharedArray(2 * numMines + 3, m.y);
            numMines++;
        }
        return numMines;
    }
}