package mastersplinter;

import battlecode.common.*;
import java.util.ArrayList;

public class RobotPlayer {

    public static enum SqueakType {
        INVALID,
        ENEMY_RAT_KING,
        ENEMY_COUNT,
        CHEESE_MINE,
        CAT_FOUND,
    }

    public static SqueakType[] squeakTypes = SqueakType.values();

    // Global mine registry (king-side)
    public static int numMines = 0;
    public static ArrayList<Integer> mineLocs = new ArrayList<>();

    // ---- Entry point ----
    public static void run(RobotController rc) {
    RobotSubPlayer brain = null;

    while (true) {
        try {
            // Re-evaluate brain when type changes (promotion -> king)
            if (rc.getType().isRatKingType()) {
                if (!(brain instanceof RatKingSubPlayer)) {
                    brain = new RatKingSubPlayer(rc);
                }
            } else {
                if (brain == null || (brain instanceof RatKingSubPlayer)) {
                    brain = chooseBrain(rc);
                }
            }

            brain.step();

        } catch (GameActionException e) {
            System.out.println("GameActionException in RobotPlayer:");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception in RobotPlayer:");
            e.printStackTrace();
        } finally {
            Clock.yield();
        }
    }
}

    private static RobotSubPlayer chooseBrain(RobotController rc) {
    if (rc.getType().isRatKingType()) return new RatKingSubPlayer(rc);

    int mod = (int)(rc.getID() % 10);

    // 1/10 expander
    if (mod == 0) return new ExpanderSubPlayer(rc);

    // 3/10 fighters
    if (mod == 1 || mod == 2 || mod == 3) return new FighterSubPlayer(rc);

    // 1/10 king builders
    if (mod == 4) return new KingBuilderSubPlayer(rc);

    // rest explorers
    return new ExplorerSubPlayer(rc);
}

    // ---- Shared comms helpers reused by subplayers ----
    public static int toInteger(MapLocation loc) {
        return (loc.x << 6) | loc.y;
    }

    public static int getSqueak(SqueakType type, int value) {
        switch (type) {
            case ENEMY_RAT_KING:
                return (1 << 12) | value;
            case ENEMY_COUNT:
                return (2 << 12) | value;
            case CHEESE_MINE:
                return (3 << 12) | value;
            case CAT_FOUND:
                return (4 << 12) | value;
            default:
                return value;
        }
    }

    public static SqueakType getSqueakType(int rawSqueak) {
        return squeakTypes[rawSqueak >> 12];
    }

    public static int getSqueakValue(int rawSqueak) {
        return rawSqueak % 4096;
    }

    // ---- King-side mine storage (called by RatKingSubPlayer) ----
    public static void kingReceiveMine(RobotController rc, int encodedLoc) throws GameActionException {
        if (mineLocs.contains(encodedLoc)) return;

        mineLocs.add(encodedLoc);

        // We are storing x,y directly in shared array like your lectureplayer approach.
        // encodedLoc passed in is (x<<6)|y, so decode it:
        int x = encodedLoc >> 6;
        int y = encodedLoc & 63;

        // Write in shared array as pairs
        int idx = numMines;
        rc.writeSharedArray(2 * idx + 2, x);
        rc.writeSharedArray(2 * idx + 3, y);

        numMines++;
        System.out.println("KING stored mine #" + idx + " at (" + x + "," + y + ")");
    }

    public static MapLocation chooseNearestKnownMine(RobotController rc, MapLocation here) throws GameActionException {
        MapLocation best = null;
        int bestD2 = Integer.MAX_VALUE;

        int limit = Math.min(numMines, 20);
        for (int i = 0; i < limit; i++) {
            int x = rc.readSharedArray(2 * i + 2);
            int y = rc.readSharedArray(2 * i + 3);
            if (x == 0 && y == 0) continue;
            MapLocation m = new MapLocation(x, y);
            int d2 = here.distanceSquaredTo(m);
            if (d2 < bestD2) {
                bestD2 = d2;
                best = m;
            }
        }
        return best;
    }
}