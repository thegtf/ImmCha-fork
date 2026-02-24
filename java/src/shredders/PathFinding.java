package shredders;

import battlecode.common.*;

public class PathFinding {


    public enum PathState {
        FOLLOW_WALL,
        MOVE_TO_TARGET,
    }

    public PathState ps = PathState.MOVE_TO_TARGET;

    public int bestDist;
    public MapLocation target = null;
    public Direction lastWallDir = null;
    public int startBugDist = Integer.MAX_VALUE;

    public void moveToTarget(RobotController rc, MapLocation target, MapLocation curr) throws GameActionException {
        
        if (target == null) {
            return; 
        }
        Direction d = rc.getLocation().directionTo(target);

        if (ps == PathState.FOLLOW_WALL) {
            int distNow = rc.getLocation().distanceSquaredTo(target);
            if (rc.canMove(d) && distNow <= bestDist) {
                ps = PathState.MOVE_TO_TARGET;
                lastWallDir = null;
            }
        }
        if (ps == PathState.MOVE_TO_TARGET) {
            if (rc.canMove(d)) {
                rc.move(d);
                return;
            }
                ps = PathState.FOLLOW_WALL;
                startBugDist = rc.getLocation().distanceSquaredTo(target);
                bestDist = startBugDist;
                lastWallDir = d;
                followWall(rc, target, curr);
                return;
            } 
            followWall(rc, target, curr);
        }


    public void followWall(RobotController rc, MapLocation target, MapLocation curr) throws GameActionException {
            Direction dir;
            
            if (lastWallDir != null) {
                dir = lastWallDir;
            } else {
                dir = rc.getLocation().directionTo(target);
            }

            // Rotate left until we find a valid move
            for (int i = 0; i < 8; i++) {
                     dir = dir.rotateLeft();
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                        lastWallDir = dir;

                        int newDist = rc.getLocation().distanceSquaredTo(target);
                        if (newDist < bestDist) bestDist = newDist;

                        if (newDist < startBugDist) {
                            ps = PathState.MOVE_TO_TARGET;
                            lastWallDir = null;
                        }
                        return; 
                    }
                }
                MapLocation here = rc.getLocation();
                MapLocation fwd = here.add(rc.getDirection());
                if (rc.canRemoveDirt(fwd)) {
                    rc.removeDirt(fwd);
                    return;
                }
                for (Direction digDir : Direction.values()) {
                if (digDir == Direction.CENTER) continue;
                MapLocation adj = here.add(digDir);
                if (rc.canRemoveDirt(adj)) {
                    rc.removeDirt(adj);
                    return;
                }
            }
            ps = PathState.MOVE_TO_TARGET;
            lastWallDir = null;
        }

                public void reset() {
                    ps = PathState.MOVE_TO_TARGET;
                    bestDist = Integer.MAX_VALUE;
                    target = null;
                    lastWallDir = null;
                    startBugDist = Integer.MAX_VALUE;
        }
    }

