package teacher07a;

import java.util.Random;

import battlecode.common.*;

public abstract class RobotSubPlayer {

    static Direction[] directions = Direction.values();
    static final Random rand = new Random(6147);
    Direction d;
    int steps;


    protected RobotController rc;
    final static int SHARED_ARRAY_LENGTH = 64;

    public RobotSubPlayer(RobotController rc) {
        this.rc = rc;
        d = rc.getDirection();
        steps = 0;
    }

    protected void setRandomDirection() {
        d = directions[rand.nextInt(directions.length-1)];
    }

        // Move in a straight line until we bump into something
    // then turn to a new direction
    public void moveRandom(RobotController rc) throws GameActionException {

        MapLocation nextLoc = rc.adjacentLocation(rc.getDirection());
        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
        }

        if (rc.canMoveForward() || steps < 6) {
            rc.moveForward();
            steps++;
        } else {
            d = directions[rand.nextInt(directions.length-1)];
            if (rc.canTurn()) {
                rc.turn(d);
            }
            rc.setIndicatorString("Blocked, turning " + d.toString());
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
