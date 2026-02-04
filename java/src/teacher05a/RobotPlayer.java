package teacher05a;

import battlecode.common.*;
import teacher02b.BabyRat;
import teacher02b.CatAttacker;
import teacher02b.CheeseFinder;

import java.util.ArrayList;
import java.util.Random;

public class RobotPlayer {

    public static MapLocation mineLoc = null;
    public static int numMines = 0;
    public static ArrayList<Integer> mineLocs = new ArrayList<>();

    static MapLocation kingLoc = null;

    static BabyRat brc;
    static boolean babyRatToggle;

    public static void run(RobotSubPlayer rc) {

        // Only baby rats will pay attention to this
        currentState = State.FIND_CHEESE;

        if (rc.get)
        if (rc.getType().isBabyRatType()) {
            if (rc.getID() % 2 == 0) {
                brc = new CheeseFinder();
            } else {
                brc = new CatAttacker();
            }
        }

        while (true) {
        try {
            if (rc.getType().isRatKingType()) {
                runRatKing(rc);
            } else {
                if (kingLoc == null) {
                    kingLoc = rc.getLocation();
                }
                brc.doSomething(rc);
                
                switch (currentState) {
                    case FIND_CHEESE:
                        runFindCheese(rc);
                        break;
                    case RETURN_TO_KING:
                        runReturnToKing(rc);
                        break;
                    case MINE_CHEESE:
                        runMineCheese(rc);
                        break;
                }
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException in RobotPlayer:");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in RobotPlayer:");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }

    public static void runFindCheese(RobotSubPlayer rc) throws GameActionException {
        // search for cheese
        MapInfo[] nearbyInfos = rc.senseNearbyMapInfos();

        System.out.println("Sensed " + nearbyInfos.length + " tiles");
        MapLocation cheeseLoc = null;
        for (MapInfo info : nearbyInfos) {
            MapLocation loc = info.getMapLocation();
            if (info.getCheeseAmount() > 0) {
                Direction toCheese = rc.getLocation().directionTo(loc);

                if (rc.canTurn(toCheese)) {
                    rc.turn(toCheese);
                    cheeseLoc = info.getMapLocation();
                    break;
                }
            }
            if (info.hasCheeseMine()) {
                mineLoc = info.getMapLocation();
                System.out.println("Found a cheese mine at " + mineLoc.toString());
                rc.setIndicatorString("Found a cheese mine at " + mineLoc.toString());
            }
            if (rc.canRemoveDirt(loc)) {
                rc.removeDirt(loc);
            }
        }

        goForthAndCheese(rc, cheeseLoc);        
    }


    
}
