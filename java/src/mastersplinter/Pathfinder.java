package mastersplinter;

import battlecode.common.*;

public final class Pathfinder {
    private Pathfinder() {}

    public static void stepToward(RobotController rc, MapLocation target) throws GameActionException {
        if (target == null) return;

        MapLocation here = rc.getLocation();
        Direction dir = here.directionTo(target);

        if (rc.canTurn(dir)) rc.turn(dir);

        if (tryMoveOrDig(rc, dir)) return;

        Direction left = dir.rotateLeft();
        Direction right = dir.rotateRight();

        if (tryMoveOrDig(rc, left)) return;
        if (tryMoveOrDig(rc, right)) return;

        Direction left2 = left.rotateLeft();
        Direction right2 = right.rotateRight();

        if (tryMoveOrDig(rc, left2)) return;
        if (tryMoveOrDig(rc, right2)) return;

        if (rc.canMoveForward()) rc.moveForward();
    }

    public static boolean tryMoveOrDig(RobotController rc, Direction dir) throws GameActionException {
        MapLocation next = rc.getLocation().add(dir);

        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        if (rc.canRemoveDirt(next)) {
            rc.removeDirt(next);
            return true;
        }
        return false;
    }
}