package mastersplinter;

import battlecode.common.*;

public class KingBuilderSubPlayer extends RobotSubPlayer {

    public KingBuilderSubPlayer(RobotController rc) {
        super(rc);
    }

    @Override
    public void step() throws GameActionException {
        // 1) Try to promote ASAP when conditions exist.
        // NOTE: Any baby can become king; builder just focuses on creating the moment.
        if (rc.isActionReady() && rc.getAllCheese() >= PROMO_RALLY_CHEESE && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            markSecondKingBuilt(); // tell the swarm “2K economy mode is on”
            System.out.println("NEW KING BUILT r=" + rc.getRoundNum() + " id=" + rc.getID());
            return;
        }

        // 2) Economy mode after second king: do NOT camp. Only return if carrying cheese.
        if (secondKingBuilt()) {
            if (rc.getRawCheese() > 0) {
                deliverToNearestKingIfPossible();
                // if still carrying, drift toward beacon; otherwise next turn you'll explore via Explorer brain in main
                MapLocation beacon = readKingBeacon();
                if (rc.getRawCheese() > 0 && beacon != null) moveTowardWithDig(beacon);
            } else {
                // Builders act like explorers after second king exists
                new ExplorerSubPlayer(rc).step();
            }
            return;
        }

        // 3) Before second king: builder creates clump window without freezing the economy.
        // If carrying, deliver quickly.
        if (rc.getRawCheese() > 0) {
            deliverToNearestKingIfPossible();
            MapLocation beacon = readKingBeacon();
            if (rc.getRawCheese() > 0 && beacon != null) moveTowardWithDig(beacon);
            return;
        }

        // If we're close to promotion (team cheese >= 45), drift toward king beacon and hover nearby.
        if (rc.getAllCheese() >= PROMO_ASSIST_CHEESE) {
            MapLocation beacon = readKingBeacon();
            if (beacon != null) {
                int d2 = rc.getLocation().distanceSquaredTo(beacon);
                // If inside a loose ring, do light jitter to avoid gridlock
                if (d2 <= 8) {
                    rc.setIndicatorString("BUILDER hovering near king for promo");
                    // micro-jitter: turn random; step if possible
                    Direction j = directions[rand.nextInt(directions.length - 1)];
                    if (rc.canTurn(j)) rc.turn(j);
                    if (rc.canMoveForward()) rc.moveForward();
                    return;
                }
                rc.setIndicatorString("BUILDER rallying to king");
                moveTowardWithDig(beacon);
                return;
            }
        }

        // Otherwise, contribute as explorer (keeps economy alive)
        new ExplorerSubPlayer(rc).step();
    }

    private void deliverToNearestKingIfPossible() throws GameActionException {
        int raw = rc.getRawCheese();
        if (raw <= 0) return;

        // Try to find a nearby friendly king (covers 2-king world without multi-beacon yet)
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
            rc.setIndicatorString("BUILDER delivered " + raw);
        }
    }
}
