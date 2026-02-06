package mastersplinter;

import battlecode.common.*;
import java.util.Random;

public abstract class RobotSubPlayer {
    protected final RobotController rc;

    // Shared constants / utilities
    protected static final Random rand = new Random(6147);
    protected static final Direction[] directions = Direction.values();

    // Promotion thresholds
    protected static final int PROMO_RALLY_CHEESE = 50;
    protected static final int PROMO_ASSIST_CHEESE = 45;

    // Shared array layout (keep compatible with your current code)
    // [0]=kingX, [1]=kingY
    // [2..] mine list: (x,y) pairs: index i stored at [2*i+2], [2*i+3]
    protected static final int SA_KING_X = 0;
    protected static final int SA_KING_Y = 1;
    protected static final int SA_MINES_BASE = 2;
    // Flag: has a second king been built? (0/1)
    protected static final int SA_SECOND_KING_FLAG = 20;

    protected boolean deliverToNearestVisibleKing() throws GameActionException {
    int raw = rc.getRawCheese();
    if (raw <= 0) return false;

    RobotInfo[] friends = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), rc.getTeam());
    MapLocation best = null;
    int bestD2 = Integer.MAX_VALUE;

    for (RobotInfo r : friends) {
        if (!r.getType().isRatKingType()) continue;
        int d2 = rc.getLocation().distanceSquaredTo(r.getLocation());
        if (d2 < bestD2) {
            bestD2 = d2;
            best = r.getLocation();
        }
    }

    if (best != null && rc.canTransferCheese(best, raw)) {
        rc.transferCheese(best, raw);
        return true;
    }
    return false;
}

    protected RobotSubPlayer(RobotController rc) {
        this.rc = rc;
    }

    /** One turn of behavior. */
    public abstract void step() throws GameActionException;

    // ---------- Shared helpers ----------
    protected MapLocation readKingBeacon() throws GameActionException {
        int x = rc.readSharedArray(SA_KING_X);
        int y = rc.readSharedArray(SA_KING_Y);
        if (x == 0 && y == 0) return null;
        return new MapLocation(x, y);
    }

    protected void writeKingBeacon(MapLocation loc) throws GameActionException {
        rc.writeSharedArray(SA_KING_X, loc.x);
        rc.writeSharedArray(SA_KING_Y, loc.y);
    }

    protected boolean secondKingBuilt() throws GameActionException {
        return rc.readSharedArray(SA_SECOND_KING_FLAG) != 0;
    }

    protected void markSecondKingBuilt() throws GameActionException {
        rc.writeSharedArray(SA_SECOND_KING_FLAG, 1);
    }

    protected void moveTowardWithDig(MapLocation target) throws GameActionException {
        if (target == null) return;
        Direction dir = rc.getLocation().directionTo(target);
        MapLocation next = rc.getLocation().add(dir);

        if (rc.canTurn(dir)) rc.turn(dir);

        if (!rc.canMove(dir) && rc.canRemoveDirt(next)) {
            rc.removeDirt(next);
            return;
        }

        if (rc.canMove(dir)) {
            rc.move(dir);
            return;
        }

        if (rc.canMoveForward()) rc.moveForward();
    }

    protected void moveWanderWithDig() throws GameActionException {
        Direction pick = directions[rand.nextInt(directions.length - 1)];
        if (rc.canTurn(pick)) rc.turn(pick);

        MapLocation fwd = rc.adjacentLocation(rc.getDirection());
        if (!rc.canMoveForward() && rc.canRemoveDirt(fwd)) {
            rc.removeDirt(fwd);
            return;
        }
        if (rc.canMoveForward()) rc.moveForward();
    }
}

