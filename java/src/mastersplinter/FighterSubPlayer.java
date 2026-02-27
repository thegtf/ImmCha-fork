package mastersplinter;

import battlecode.common.*;

public class FighterSubPlayer extends RobotSubPlayer {

    public FighterSubPlayer(RobotController rc) { super(rc); }

    @Override
    public void step() throws GameActionException {
        MapLocation beacon = kingBeacon();

        RobotInfo[] cats = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), Team.NEUTRAL);
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), rc.getTeam().opponent());

        if (enemies.length > 0) Comms.squeakEnemyCount(rc, enemies.length);

        // if carrying cheese, deliver first (stability)
        if (rc.getRawCheese() > 0) {
            if (!deliverToNearestVisibleKing()) {
                if (beacon != null) Pathfinder.stepToward(rc, beacon);
                else wanderWithDig();
            }
            rc.setIndicatorString("FTR: delivered/return");
            return;
        }

        // cat behavior
        if (cats.length > 0) {
            MapLocation catLoc = cats[0].getLocation();
            int d2 = rc.getLocation().distanceSquaredTo(catLoc);

            Direction away = rc.getLocation().directionTo(catLoc).opposite();
            if (d2 >= 10) Comms.squeakCatDir(rc, rc.getLocation().directionTo(catLoc));

            // trap placement (cheap)
            for (Direction dir : directions) {
                MapLocation t = rc.getLocation().add(dir);
                if (rc.canPlaceCatTrap(t)) { rc.placeCatTrap(t); break; }
                if (rc.canPlaceRatTrap(t)) { rc.placeRatTrap(t); break; }
            }

            Pathfinder.tryMoveOrDig(rc, away);
            rc.setIndicatorString("FTR: evade cat");
            return;
        }

        // attack adjacent if possible
        for (Direction dir : directions) {
            MapLocation loc = rc.getLocation().add(dir);
            if (rc.canAttack(loc)) { rc.attack(loc); break; }
        }

        // guard near beacon if we have one
        if (beacon != null) {
            int d2 = rc.getLocation().distanceSquaredTo(beacon);
            if (d2 > 100) {
                Pathfinder.stepToward(rc, beacon);
                rc.setIndicatorString("FTR: return guard");
                return;
            }
        }

        // roam/patrol
        wanderWithDig();
        rc.setIndicatorString("FTR: patrol");
    }
}