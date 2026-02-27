package teacher08a;

import battlecode.common.*;
import java.util.List;
import java.util.Set;

import java.util.ArrayList;
import java.util.HashSet;

public class CatAttacker extends BabyRat {

    public static enum State {
        FIND_CAT, // explore and attack cat
        REPORT_BACK, // explore randomly and squeak to other babyrats about cat
    }

    public static State currentState;
    int senseInterval = 5;
    MapLocation catLoc;
    BugNav bugNav;

    public CatAttacker(RobotController rc) {
        super(rc);
        currentState = State.FIND_CAT;
        bugNav = null;
        rc.setIndicatorString("Cat attacker reporting for duty");
        setRandomDirection();
        catLoc = null;
    }

    public void doAction() throws GameActionException {
        switch (currentState) {
            case FIND_CAT:
                runFindCat();
                break;
            case REPORT_BACK:
                runReportBack();
                break;
        }
    }

    public void runFindCat() throws GameActionException {
        // search for cheese

        MapLocation here = rc.getLocation();

        RobotInfo[] nearbyInfos = rc.senseNearbyRobots();

        System.out.println("Sensed " + nearbyInfos.length + " robots");
        for (RobotInfo info : nearbyInfos) {
            if (info.getType().isCatType()) {
                catLoc = info.getLocation();
                break;
            } else if (info.getType().isBabyRatType()) {
                // We found another baby rat, listen for squeaks
                // on where to find a cat
                Message[] squeakMessages = rc.readSqueaks(rc.getRoundNum());
                System.out.println("Received " + squeakMessages.length + " messages");

                for (Message m : squeakMessages) {
                    int msg = m.getBytes();
                    if (getSqueakType(msg) == SqueakType.CAT_FOUND) {
                        int encodedLoc = getSqueakValue(msg);
                        catLoc = new MapLocation(getX(encodedLoc), getY(encodedLoc));
                        System.out.println("Received cat " + catLoc.toString());
                        break;
                    }
                }

            }
            
        }

        if (catLoc != null) {
            Direction toCat = here.directionTo(catLoc);
            System.out.println("Found cat at " + catLoc.toString());
            if (rc.canTurn(toCat) && (d != toCat)) {
                d = toCat;
                rc.turn(toCat);
            }
        }

        MapLocation nextLoc = rc.adjacentLocation(rc.getDirection());
        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
        }

        if (catLoc != null) {

            // Yes, tight loops
            while (rc.canAttack(catLoc)) {
                rc.attack(catLoc);
                System.out.println("Attacking cat at " + catLoc.toString());
                rc.setIndicatorString("Victory or death!");
            }
            int dice = RobotSubPlayer.rand.nextInt(10);
            if (dice > 8) {
                rc.setIndicatorString("Victory or death!");
                System.out.println("Reporting back with dice " + Integer.valueOf(dice).toString());
                currentState = State.REPORT_BACK;
                return;
            }
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            rc.setIndicatorString("Finding cat.");
        } else {
            if (gettingUnstuck && (catLoc != null)) {
                // If we are stuck while getting unstuck, just turn back to the cat
                gettingUnstuck = false;
                d = here.directionTo(catLoc);
                rc.setIndicatorString("Unstuck from finding cat, turning to " + d.toString());
            } else {
                d = directions[rand.nextInt(directions.length-1)];
                gettingUnstuck = true;
                rc.setIndicatorString("Stuck while finding cat, turning to " + d.toString());
            }
            if (rc.canTurn()) {
                rc.turn(d);
            }
            rc.setIndicatorString("Blocked while finding cat, turning " + d.toString());
        }

    }
    public void runReportBack() throws GameActionException {

        // if (bugNav != null) {
        //     boolean result = bugNav.move(rc);
        //     if (!result) {
        //         // false means we should keep doing bugnav next time
        //         return;
        //     } else {
        //         // else we are better off walking straight to king
        //         bugNav = null;
        //     }
        // }

        MapLocation here = rc.getLocation();

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        boolean squeaked = false;
        boolean receivedMineLoc = false;

        for (RobotInfo robotInfo : nearbyRobots) {
            if (robotInfo.getTeam() != rc.getTeam()) {
                continue;
            }
            if (robotInfo.getType().isBabyRatType()) {
                // we found another baby rat, squeak or read squeaks,
                // based on whether we have a mine location to go to

                if (catLoc != null) {
                    int msgByte = getSqueak(SqueakType.CAT_FOUND, toInteger(catLoc));
                    rc.squeak(msgByte);
                    System.out.println("From " + here.toString() + " Sent a squeak " + msgByte + " for cat at " + catLoc.toString());
                    // go back to finding cheese mode
                    // if we have squeaked to at least one other baby rat
                    squeaked = true;
                    currentState = State.FIND_CAT;
                }
            }
        }

        RobotInfo forwardRobot = null;
        MapLocation forwardLoc = rc.adjacentLocation(rc.getDirection());
        if (rc.canSenseLocation(forwardLoc)) {
            forwardRobot = rc.senseRobotAtLocation(forwardLoc);
        }

        if (rc.canRemoveDirt(forwardLoc)) {
            rc.removeDirt(forwardLoc);
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            rc.setIndicatorString("Returning to king.");
        } else if (forwardRobot != null) {
            if (forwardRobot.getType().isBabyRatType()) {
                // simply skip this turn and wait for the other robot to move
                return;
            }
        } else {
            MapInfo forwardInfo = rc.senseMapInfo(forwardLoc);
            if (forwardInfo.isWall() && bugNav == null) {
                bugNav = new BugNav(kingLoc, rc);
            }

            rc.setIndicatorString("Blocked while returning to king, turning " + d.toString());
            return;
        }
        
    }
    
}
