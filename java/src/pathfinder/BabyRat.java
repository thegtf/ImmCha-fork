package pathfinder;

import battlecode.common.*;

public class BabyRat extends RobotSubPlayer {

    protected static MapLocation kingLoc = null;

    public static SqueakType[] squeakTypes = SqueakType.values();

    public static enum SqueakType {
        INVALID,
        ENEMY_RAT_KING,
        ENEMY_COUNT,
        CHEESE_MINE,
        CAT_FOUND,
    }

    MapLocation dest;
    BugNav bugNav;

    public BabyRat(RobotController rc) throws GameActionException {
        super(rc);
        // Save the location of the king who spawned us
        kingLoc = rc.getLocation();
        int destX = rc.readSharedArray(0);
        int destY = rc.readSharedArray(1);
        this.dest = new MapLocation(destX, destY);
        System.out.println("BabyRat spawned at " + kingLoc.toString() + " with destination " + dest.toString());
        this.bugNav = null;
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
        // Only uses lower 12 bits
        return rawSqueak % 4096;
    }

    public void doAction() throws GameActionException {
        if (bugNav == null) {
            bugNav = new BugNav(dest, rc);
        }

        boolean result = bugNav.move(rc);
        if (result == true) {
            System.out.println("BabyRat exiting bugNav at " + rc.getLocation().toString() + " heading towards destination " + dest.toString());
            bugNav = null;
        }
    }

}
