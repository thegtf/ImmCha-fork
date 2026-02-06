package mastersplinter;

import battlecode.common.*;

public class FighterSubPlayer extends RobotSubPlayer {

    public FighterSubPlayer(RobotController rc) { super(rc); }

    @Override
    public void step() throws GameActionException {
        // Sense threats
        RobotInfo[] cats = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), Team.NEUTRAL);
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), rc.getTeam().opponent());
        MapLocation beacon = readKingBeacon();

        // Comms: enemy count
        if (enemies.length > 0) {
            Comms.squeakEnemyCount(rc, enemies.length);
        }

        // If cat is close: run away hard
        if (cats.length > 0) {
            MapLocation catLoc = cats[0].getLocation();
            int d2 = rc.getLocation().distanceSquaredTo(catLoc);
            Direction away = rc.getLocation().directionTo(catLoc).opposite();

// Guard the king: stay within ~8 tiles of beacon so we can intercept cats/enemies near king.
if (beacon != null) {
    int d2Beacon = rc.getLocation().distanceSquaredTo(beacon);

    // If too far, move toward beacon (digging if needed)
    if (d2 > 64) {
        Pathfinder.stepToward(rc, beacon);
        rc.setIndicatorString("FIGHTER: returning to guard king");
        return;
    }

    // If carrying cheese, deliver while guarding
    if (rc.getRawCheese() > 0) {
        if (!deliverToNearestVisibleKing()) {
            Pathfinder.stepToward(rc, beacon);
        }
        rc.setIndicatorString("FIGHTER: delivered while guarding");
        return;
    }
}

            // Comms: cat direction (only if not right on top of us)
            if (d2 >= 10) {
                Comms.squeakCatDir(rc, rc.getLocation().directionTo(catLoc));
            }

            // Place traps if possible (prefer cat traps)
            for (Direction dir : directions) {
                MapLocation t = rc.getLocation().add(dir);
                if (rc.canPlaceCatTrap(t)) { rc.placeCatTrap(t); break; }
                if (rc.canPlaceRatTrap(t)) { rc.placeRatTrap(t); break; }
            }

            // Move away (with dig)
            Pathfinder.tryMoveOrDig(rc, away);
            rc.setIndicatorString("FIGHTER: cat nearby, evading + trapping");
            return;
        }

        // If enemies nearby: attack adjacent squares
        for (Direction dir : directions) {
            MapLocation loc = rc.getLocation().add(dir);
            if (rc.canAttack(loc)) {
                rc.attack(loc);
                rc.setIndicatorString("FIGHTER: attacked");
                break;
            }
        }

        // Rat carry/throw micro (cheap)
        if (rc.canThrowRat()) rc.throwRat();
        for (Direction dir : directions) {
            MapLocation loc = rc.getLocation().add(dir);
            if (rc.canCarryRat(loc)) {
                rc.carryRat(loc);
                break;
            }
        }

        // Still contribute to economy if empty
        if (rc.getRawCheese() > 0) {
            beacon = readKingBeacon();
            if (beacon != null) Pathfinder.stepToward(rc, beacon);
            return;
        }

        // Default: patrol/wander
        moveWanderWithDig();
        rc.setIndicatorString("FIGHTER: patrolling");
    }
}