package mastersplinter;

import battlecode.common.*;

public class ExpanderSubPlayer extends RobotSubPlayer {

    public ExpanderSubPlayer(RobotController rc) { super(rc); }

    @Override
    public void step() throws GameActionException {
        // 1) Promote ASAP when possible
        if (rc.isActionReady()
                && rc.getAllCheese() >= PROMO_RALLY_CHEESE
                && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            // NOTE: You can keep your global "secondKingBuilt" idea if you want,
            // but for multi-king expansion we don't want to disable future promotions.
            System.out.println("EXPANDER: NEW KING r=" + rc.getRoundNum() + " id=" + rc.getID());
            return;
        }

        // 2) If carrying cheese, deliver
        if (rc.getRawCheese() > 0) {
            deliverToNearestVisibleKing();
            MapLocation beacon = readKingBeacon();
            if (rc.getRawCheese() > 0 && beacon != null) Pathfinder.stepToward(rc, beacon);
            rc.setIndicatorString("EXPANDER: delivering");
            return;
        }

        // 3) If we are close to promotion threshold, drift toward beacon to create the 7-pack
        if (rc.getAllCheese() >= PROMO_ASSIST_CHEESE) {
            MapLocation beacon = readKingBeacon();
            if (beacon != null) {
                int d2 = rc.getLocation().distanceSquaredTo(beacon);

                // Stay in a loose ring (not a clog) within ~8 tiles
                if (d2 <= 64) {
                    Direction j = directions[rand.nextInt(directions.length - 1)];
                    if (rc.canTurn(j)) rc.turn(j);
                    if (rc.canMoveForward()) rc.moveForward();
                    rc.setIndicatorString("EXPANDER: hovering for 7-pack");
                    return;
                }

                Pathfinder.stepToward(rc, beacon);
                rc.setIndicatorString("EXPANDER: rallying to beacon");
                return;
            }
        }

        // 4) Otherwise, act like an explorer (keep income alive)
        new ExplorerSubPlayer(rc).step();
    }

    @Override
    public boolean deliverToNearestVisibleKing() throws GameActionException {
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
        }
        return false;
    }
}