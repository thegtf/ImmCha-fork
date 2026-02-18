package mastersplinter;

import battlecode.common.*;
import java.util.ArrayList;

public final class Comms {
    private Comms() {}

    // ===== Shared array layout =====
    // [0]=kingX, [1]=kingY  (primary beacon)
    // [2]=kingCount (best effort)
    // [3]=flags (bit 0 = secondKingBuilt)
    // [4]=numMines
    // [5..] mines as pairs: [5 + 2*i]=x, [6 + 2*i]=y

    public static final int SA_KING_X = 0;
    public static final int SA_KING_Y = 1;
    public static final int SA_KING_COUNT = 2;
    public static final int SA_FLAGS = 3;
    public static final int SA_NUM_MINES = 4;
    public static final int SA_MINES_BASE = 5;

    private static final int FLAG_SECOND_KING = 1;

    // ===== Squeaks =====
    // top 4 bits => type, low 12 bits => value
    public enum SqueakType {
        INVALID,
        ENEMY_COUNT,
        CHEESE_MINE,
        CAT_DIR
    }

    public static final SqueakType[] TYPES = SqueakType.values();

    public static int makeSqueak(SqueakType type, int value) {
        return (type.ordinal() << 12) | (value & 0xFFF);
    }

    public static SqueakType squeakType(int raw) {
        return TYPES[(raw >>> 12) & 0xF];
    }

    public static int squeakValue(int raw) {
        return raw & 0xFFF;
    }

    // pack 0..63,0..63 into 12 bits
    public static int packLoc(MapLocation loc) {
        return ((loc.x & 63) << 6) | (loc.y & 63);
    }

    public static MapLocation unpackLoc(int packed) {
        int x = (packed >>> 6) & 63;
        int y = packed & 63;
        return new MapLocation(x, y);
    }

    // ===== Beacon =====
    public static void writeKingBeacon(RobotController rc) throws GameActionException {
        MapLocation here = rc.getLocation();
        rc.writeSharedArray(SA_KING_X, here.x);
        rc.writeSharedArray(SA_KING_Y, here.y);
    }

    public static MapLocation readKingBeacon(RobotController rc) throws GameActionException {
        int x = rc.readSharedArray(SA_KING_X);
        int y = rc.readSharedArray(SA_KING_Y);
        if (x == 0 && y == 0) return null;
        return new MapLocation(x, y);
    }

    // Legacy aliases (in case other files call them)
    public static void writeKing(RobotController rc) throws GameActionException { writeKingBeacon(rc); }
    public static MapLocation readKing(RobotController rc) throws GameActionException { return readKingBeacon(rc); }

    // ===== Flags / king count =====
    public static int readKingCount(RobotController rc) throws GameActionException {
        int k = rc.readSharedArray(SA_KING_COUNT);
        if (k <= 0) return 1;
        if (k > 5) return 5;
        return k;
    }

    public static void writeKingCount(RobotController rc, int k) throws GameActionException {
        rc.writeSharedArray(SA_KING_COUNT, Math.min(5, Math.max(1, k)));
    }

    public static boolean secondKingBuilt(RobotController rc) throws GameActionException {
        return (rc.readSharedArray(SA_FLAGS) & FLAG_SECOND_KING) != 0;
    }

    public static void setSecondKingBuilt(RobotController rc) throws GameActionException {
        int f = rc.readSharedArray(SA_FLAGS);
        rc.writeSharedArray(SA_FLAGS, f | FLAG_SECOND_KING);
    }

    // ===== Mine list =====
    public static int readNumMines(RobotController rc) throws GameActionException {
        int n = rc.readSharedArray(SA_NUM_MINES);
        if (n < 0) return 0;
        return n;
    }

    public static void writeNumMines(RobotController rc, int n) throws GameActionException {
        rc.writeSharedArray(SA_NUM_MINES, Math.max(0, n));
    }

    public static void tryStoreMine(RobotController rc, MapLocation mine, ArrayList<Integer> seenPacked) throws GameActionException {
        int packed = packLoc(mine);
        if (seenPacked.contains(packed)) return;

        int n = readNumMines(rc);
        // prevent writing past shared array limits (safe guard)
        int idxX = SA_MINES_BASE + 2 * n;
        int idxY = idxX + 1;
        if (idxY >= 64) return;

        seenPacked.add(packed);
        rc.writeSharedArray(idxX, mine.x);
        rc.writeSharedArray(idxY, mine.y);
        writeNumMines(rc, n + 1);
    }

    public static MapLocation getMineByIndex(RobotController rc, int i) throws GameActionException {
        int n = readNumMines(rc);
        if (i < 0 || i >= n) return null;
        int idxX = SA_MINES_BASE + 2 * i;
        int idxY = idxX + 1;
        int x = rc.readSharedArray(idxX);
        int y = rc.readSharedArray(idxY);
        return new MapLocation(x, y);
    }

    public static MapLocation nearestKnownMine(RobotController rc, MapLocation from) throws GameActionException {
        int n = readNumMines(rc);
        MapLocation best = null;
        int bestD2 = Integer.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            MapLocation m = getMineByIndex(rc, i);
            if (m == null) continue;
            int d2 = from.distanceSquaredTo(m);
            if (d2 < bestD2) {
                bestD2 = d2;
                best = m;
            }
        }
        return best;
    }

    // ===== Squeak helpers =====
    public static void squeakMine(RobotController rc, MapLocation mine) throws GameActionException {
        rc.squeak(makeSqueak(SqueakType.CHEESE_MINE, packLoc(mine)));
    }

    public static void squeakEnemyCount(RobotController rc, int count) throws GameActionException {
        rc.squeak(makeSqueak(SqueakType.ENEMY_COUNT, Math.min(4095, Math.max(0, count))));
    }

    public static void squeakCatDir(RobotController rc, Direction dirToCat) throws GameActionException {
        rc.squeak(makeSqueak(SqueakType.CAT_DIR, dirToCat.ordinal()));
    }
}