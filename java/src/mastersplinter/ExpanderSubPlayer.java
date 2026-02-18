package mastersplinter;

import battlecode.common.*;

public class ExpanderSubPlayer extends RobotSubPlayer {

    public ExpanderSubPlayer(RobotController rc) { super(rc); }

    @Override
    public void step() throws GameActionException {
        // Try promotion if we are allowed + can afford buffer
        if (rc.isActionReady() && rc.canBecomeRatKing()) {
            int floor = cheeseFloor();
            if (rc.getAllCheese() >= PROMO_COST + floor) {
                rc.becomeRatKing();

                int k = kingCount();
                Comms.writeKingCount(rc, Math.min(5, k + 1));
                if (k + 1 >= 2) Comms.setSecondKingBuilt(rc);

                System.out.println("EXPAND -> NEW KING r=" + rc.getRoundNum() + " id=" + rc.getID());
                return;
            }
        }

        // Delivery stability
        if (rc.getRawCheese() > 0) {
            if (!deliverToNearestVisibleKing()) {
                MapLocation beacon = kingBeacon();
                if (beacon != null) Pathfinder.stepToward(rc, beacon);
            }
            rc.setIndicatorString("EXPAND: deliver");
            return;
        }

        // Otherwise just explore (don’t camp)
        new ExplorerSubPlayer(rc).step();
    }
}