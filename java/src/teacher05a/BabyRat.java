package teacher05a;

import battlecode.common.*;

public abstract class BabyRat extends RobotSubPlayer {

    protected static MapLocation kingLoc = null;

    public static SqueakType[] squeakTypes = SqueakType.values();

    public static enum SqueakType {
        INVALID,
        ENEMY_RAT_KING,
        ENEMY_COUNT,
        CHEESE_MINE,
        CAT_FOUND,
    }

    public BabyRat(RobotController rc) {
        super(rc);
        // Save the location of the king who spawned us
        kingLoc = rc.getLocation();
    }

    public static BabyRat createToggle(RobotController rc) throws GameActionException {
        int numRats = rc.readSharedArray(0);
        if ((numRats % 2) == 0) {
            return new CheeseFinder(rc);
        } else {
            return new CatAttacker(rc);
        }
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

    public abstract void doAction() throws GameActionException;

}
