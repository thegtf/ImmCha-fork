package mastersplinter;

import battlecode.common.*;
import java.util.Random;

public abstract class RobotSubPlayer {
    protected final RobotController rc;

    protected static final Random rand = new Random(6147);
    protected static final Direction[] directions = Direction.values();

    // ===== Level 5 knobs =====
    protected static final int SAFETY_WINDOW = 10;          // rounds of king-upkeep buffer
    protected static final int PROMO_COST = 50;
    protected static final int PROMO_ASSIST_CHEESE = 45;

    // baby caps to stop spawn-cost explosion
    protected static final int BABY_CAP_1K = 14;
    protected static final int BABY_CAP_2K = 18;
    protected static final int BABY_CAP_3K = 22;

    protected RobotSubPlayer(RobotController rc) {
        this.rc = rc;
    }

    public abstract void step() throws GameActionException;

    protected int kingCount() throws GameActionException {
        return Comms.readKingCount(rc);
    }

    protected int cheeseFloor() throws GameActionException {
        return 2 * kingCount() * SAFETY_WINDOW;
    }

    protected boolean belowFloor() throws GameActionException {
        return rc.getAllCheese() < cheeseFloor();
    }

    protected int babyCap() throws GameActionException {
        int k = kingCount();
        if (k <= 1) return BABY_CAP_1K;
        if (k == 2) return BABY_CAP_2K;
        return BABY_CAP_3K;
    }

    protected MapLocation kingBeacon() throws GameActionException {
        return Comms.readKingBeacon(rc);
    }

    protected void writeBeaconIfKing() throws GameActionException {
        if (rc.getType().isRatKingType()) Comms.writeKingBeacon(rc);
    }

    /** Deliver carried cheese to nearest visible allied king. */
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

    /** Cheap wander: random turn, dig if blocked, forward if possible. */
    protected void wanderWithDig() throws GameActionException {
        Direction d = directions[rand.nextInt(directions.length - 1)];
        if (rc.canTurn(d)) rc.turn(d);

        MapLocation fwd = rc.adjacentLocation(rc.getDirection());
        if (!rc.canMoveForward() && rc.canRemoveDirt(fwd)) {
            rc.removeDirt(fwd);
            return;
        }
        if (rc.canMoveForward()) rc.moveForward();
    }
}