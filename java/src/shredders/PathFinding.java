package shredders;

import battlecode.common.*;

public class PathFinding {


    public static enum PathState {
        FOLLOW_WALL,
        MOVE_TO_TARGET,
    }

    public static PathState ps = PathState.MOVE_TO_TARGET;

    static int bestDist;
    static MapLocation target = null;
    static Direction lastWallDir = null;
    static int startBugDist = Integer.MAX_VALUE;

    public static void moveToTarget(RobotController rc, MapLocation target, MapLocation curr) throws GameActionException {
        
        if (target == null) {
            return; 
        }
        Direction d = rc.getLocation().directionTo(target);

        if (ps == PathState.MOVE_TO_TARGET) {
            if (rc.canMove(d)) {
                rc.move(d);
            } else {
                ps = PathState.FOLLOW_WALL;
                startBugDist = rc.getLocation().distanceSquaredTo(target);
                bestDist = startBugDist;
                lastWallDir = d;

                followWall(rc, target, curr);
            } 
        } else {
            followWall(rc, target, curr);
        }
        
    }


    public static void followWall(RobotController rc, MapLocation target, MapLocation curr) throws GameActionException {
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
            }

                public static void reset() {
                    ps = PathState.MOVE_TO_TARGET;
                    bestDist = Integer.MAX_VALUE;
                    target = null;
                    lastWallDir = null;
                    startBugDist = Integer.MAX_VALUE;
        }
    }
    

