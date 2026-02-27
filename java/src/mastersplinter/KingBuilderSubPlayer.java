package mastersplinter;

import battlecode.common.*;

public class KingBuilderSubPlayer extends RobotSubPlayer {

    public KingBuilderSubPlayer(RobotController rc) { super(rc); }

    @Override
    public void step() throws GameActionException {
        // Promotion gate: must be survivable
        if (rc.isActionReady() && rc.canBecomeRatKing()) {
            int floor = cheeseFloor();
            if (rc.getAllCheese() >= PROMO_COST + floor) {
                rc.becomeRatKing();

                // bump king count best-effort
                int k = kingCount();
                Comms.writeKingCount(rc, Math.min(5, k + 1));
                if (k + 1 >= 2) Comms.setSecondKingBuilt(rc);

                System.out.println("NEW KING BUILT r=" + rc.getRoundNum() + " id=" + rc.getID());
                return;
            }
        }

        // If carrying, deliver
        if (rc.getRawCheese() > 0) {
            if (!deliverToNearestVisibleKing()) {
                MapLocation beacon = kingBeacon();
                if (beacon != null) Pathfinder.stepToward(rc, beacon);
            }
            rc.setIndicatorString("KB: deliver");
            return;
        }

        // After second king, don't camp; keep economy flowing
        if (Comms.secondKingBuilt(rc)) {
            new ExplorerSubPlayer(rc).step();
            return;
        }

        // Before second king: soft rally if we're near promotion window
        if (rc.getAllCheese() >= PROMO_ASSIST_CHEESE) {
            MapLocation beacon = kingBeacon();
            if (beacon != null) {
                int d2 = rc.getLocation().distanceSquaredTo(beacon);
                if (d2 <= 100) {
                    // hover/jitter near beacon without clogging
                    Direction j = directions[rand.nextInt(directions.length - 1)];
                    if (rc.canTurn(j)) rc.turn(j);
                    if (rc.canMoveForward()) rc.moveForward();
                    rc.setIndicatorString("KB: hover");
                    return;
                } else {
                    Pathfinder.stepToward(rc, beacon);
                    rc.setIndicatorString("KB: drift to beacon");
                    return;
                }
            }
        }

        // Otherwise explore
        new ExplorerSubPlayer(rc).step();
    }
}