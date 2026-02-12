package shredders;

import battlecode.common.*;

public class KingBuilder extends BabyRat {

    // Must match RatKing shared-array beacon
    private static final int SA_KING_X = 1;
    private static final int SA_KING_Y = 2;

    // Promotion thresholds (rules)
    private static final int PROMO_COST = 50;
    private static final int RALLY_START = 45;

    // How close to king we try to pack (3x3 around the builder)
    private static final int HOLD_RADIUS2 = 2; // distanceSquared <= 2 is inside tight 3x3-ish

    public KingBuilder(RobotController rc) {
        super(rc);
    }

        @Override
    public void doAction() throws GameActionException {

        // Promote immediately if possible
        if (rc.isActionReady() && rc.getAllCheese() >= PROMO_COST && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            System.out.println("PROMOTION: new RatKing by id=" + rc.getID() + " r=" + rc.getRoundNum());
            return;
        }

        // Only engage when we're close to promo window
        int global = rc.getAllCheese();
        if (global < RALLY_START) {
            rc.setIndicatorString("KB idle g=" + global);
            return;
        }

        MapLocation king = readKingBeacon();
        if (king == null) {
            rc.setIndicatorString("KB no beacon");
            return;
        }

        // === Camp tile selection (critical fix) ===
        // We camp 4 tiles away from the king center so the 3x3 around the builder can be valid.
        Direction side = ((rc.getID() & 1) == 0) ? Direction.EAST : Direction.WEST;
        MapLocation camp = king;
        camp = camp.add(side).add(side).add(side).add(side); // 4 steps

        // Move to camp (one-step lite)
        if (!rc.getLocation().equals(camp)) {
            stepToward(camp);
        }

        // Diagnostics: pack size in ~3x3 around the builder
        int pack = rc.senseNearbyRobots(2, rc.getTeam()).length;
        rc.setIndicatorString("KB camp pack=" + pack + " g=" + global + " can=" + rc.canBecomeRatKing());

        if (rc.getLocation().equals(camp) && pack < 7 && (rc.getRoundNum() % 8 == 0)) {
        // tiny nudge to avoid perfect deadlocks
            roam();
        }
    }

    private MapLocation readKingBeacon() throws GameActionException {
        int x = rc.readSharedArray(SA_KING_X);
        int y = rc.readSharedArray(SA_KING_Y);
        if (x == 0 && y == 0) return null;
        return new MapLocation(x, y);
    }

    private boolean runFromNearbyCat() throws GameActionException {
        RobotInfo[] cats = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), Team.NEUTRAL);
        if (cats.length == 0) return false;

        MapLocation catLoc = cats[0].getLocation();
        int d2 = rc.getLocation().distanceSquaredTo(catLoc);

        // If cat is close-ish, run away
        if (d2 <= 25) {
            Direction away = catLoc.directionTo(rc.getLocation());
            MapLocation next = rc.getLocation().add(away);

            if (rc.canRemoveDirt(next)) {
                rc.removeDirt(next);
                return true;
            }
            if (rc.canMove(away)) {
                rc.move(away);
                return true;
            }
        }
        return false;
    }

    private void stepToward(MapLocation target) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        MapLocation next = rc.getLocation().add(dir);

        if (rc.canRemoveDirt(next)) {
            rc.removeDirt(next);
            return;
        }
        if (rc.canMove(dir)) {
            rc.move(dir);
            return;
        }
        roam();
    }

    private void driftAwayFrom(MapLocation king) throws GameActionException {
        // If already far enough, just roam.
        int d2 = rc.getLocation().distanceSquaredTo(king);
        if (d2 >= 100) { // ~10 tiles away
            roam();
            return;
        }

        Direction away = king.directionTo(rc.getLocation());
        MapLocation next = rc.getLocation().add(away);

        if (rc.canRemoveDirt(next)) {
            rc.removeDirt(next);
            return;
        }
        if (rc.canMove(away)) {
            rc.move(away);
            return;
        }
        roam();
    }

    private void roam() throws GameActionException {
        // ultra-light random move
        Direction dir = directions[rand.nextInt(directions.length)];
        MapLocation next = rc.getLocation().add(dir);

        if (rc.canRemoveDirt(next)) {
            rc.removeDirt(next);
            return;
        }
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}