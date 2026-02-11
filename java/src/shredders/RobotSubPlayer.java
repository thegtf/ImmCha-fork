package shredders;

import java.util.Random;

import battlecode.common.*;

public abstract class RobotSubPlayer {

static final Direction[] directions = {
    Direction.NORTH,
    Direction.NORTHEAST,
    Direction.EAST,
    Direction.SOUTHEAST,
    Direction.SOUTH,
    Direction.SOUTHWEST,
    Direction.WEST,
    Direction.NORTHWEST
};

    static final Random rand = new Random(6147);
    static Direction d = null;


    protected RobotController rc;
    final static int SHARED_ARRAY_LENGTH = 64;

    public RobotSubPlayer(RobotController rc) {
        this.rc = rc;
    }

        // Move in a straight line until we bump into something
    // then turn to a new direction
    public static void moveRandom(RobotController rc) throws GameActionException {

        if (d == null) {
            d = directions[rand.nextInt(directions.length)];
        }
        if (rc.canTurn(d)) {
            rc.turn(d);

        if (rc.canMoveForward()) {
            rc.moveForward();
            return;
            }
                Direction tryDir = d;
    for (int i = 0; i < 8; i++) {
        tryDir = tryDir.rotateLeft();
        if (rc.canTurn(tryDir)) rc.turn(tryDir);
        if (rc.canMoveForward()) {
            rc.moveForward();
            d = tryDir;
            return;
        }
    }

    // Fully stuck — re-roll for next turn
    d = directions[rand.nextInt(directions.length)];
    }
}

    
    // public void writeRatKingCount(int ratKingCount) throws GameActionException {
    //     rc.writeSharedArray(0, ratKingCount);
    // }

    // public void writeRatKingLocation(int ratKingIndex, MapLocation kingLoc) throws GameActionException {
    //     rc.writeSharedArray(SHARED_ARRAY_LENGTH - ratKingIndex - 1, toInteger(kingLoc));
    // }

    // All robot subplayers must implement this method
    public abstract void doAction() throws GameActionException;

        public static int getFirstInt(int loc) {
        // extract 10 smallest place value bits from toInteger(loc)
        return loc % 1024;
    }

    public static int getLastInt(int loc) {
        // extract bits with place values >= 2^10 from toInteger(loc)
        return loc >> 10;
    }

    public static int toInteger(MapLocation loc) {
        return (loc.x << 6) | loc.y;
    }

    public static int getX(int encodedLoc) {
        return encodedLoc >> 6;
    }

    public static int getY(int encodedLoc) {
        return encodedLoc % 64;
    }
    
}

