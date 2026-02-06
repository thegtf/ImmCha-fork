package mastersplinter;

import battlecode.common.*;

public final class Pathfinder {
    private Pathfinder() {}

    /**
     * Try to move toward target in a robust way:
     * - turn toward target
     * - if blocked, dig the next tile if possible
     * - else try nearby directions (left/right rotations)
     * - else jitter turn
     */
    public static void stepToward(RobotController rc, MapLocation target) throws GameActionException {
        if (target == null) return;

        MapLocation here = rc.getLocation();
        Direction dir = here.directionTo(target);

        // Turn toward target if we can
        if (rc.canTurn(dir)) rc.turn(dir);

        // Primary attempt
        if (tryMoveOrDig(rc, dir)) return;

        // Try side directions
        Direction left = dir.rotateLeft();
        Direction right = dir.rotateRight();
        Direction left2 = left.rotateLeft();
        Direction right2 = right.rotateRight();

        if (tryMoveOrDig(rc, left)) return;
        if (tryMoveOrDig(rc, right)) return;
        if (tryMoveOrDig(rc, left2)) return;
        if (tryMoveOrDig(rc, right2)) return;

        // Fallback: forward if possible
        if (rc.canMoveForward()) {
            rc.moveForward();
            return;
        }

        // Last resort: jitter to break deadlocks
        Direction j = RobotSubPlayer.directions[RobotSubPlayer.rand.nextInt(RobotSubPlayer.directions.length - 1)];
        if (rc.canTurn(j)) rc.turn(j);
    }

    /** Move if possible; otherwise dig dirt on the next tile if possible. */
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