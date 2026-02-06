package mastersplinter;

import battlecode.common.*;

public class RatKingSubPlayer extends RobotSubPlayer {

    // King camping target
    private MapLocation kingTargetMine = null;

    public RatKingSubPlayer(RobotController rc) {
        super(rc);
    }

    

    @Override
    public void step() throws GameActionException {
        // Always publish our beacon
        writeKingBeacon(rc.getLocation());

        // KING SURVIVAL: respond to cats immediately
RobotInfo[] cats = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), Team.NEUTRAL);
if (cats.length > 0) {
    MapLocation catLoc = cats[0].getLocation();
    int d2 = rc.getLocation().distanceSquaredTo(catLoc);

    // If adjacent/attackable squares exist, attack
    for (Direction dir : directions) {
        MapLocation a = rc.getLocation().add(dir);
        if (rc.canAttack(a)) {
            rc.attack(a);
            break;
        }
    }

    // Place traps around self (kings can usually place—if not, these calls just fail safely)
    for (Direction dir : directions) {
        MapLocation t = rc.getLocation().add(dir);
        if (rc.canPlaceCatTrap(t)) { rc.placeCatTrap(t); break; }
        if (rc.canPlaceRatTrap(t)) { rc.placeRatTrap(t); break; }
    }

    // Run away if cat is within danger range
    if (d2 <= 20) { // tuneable
        Direction away = rc.getLocation().directionTo(catLoc).opposite();
        Pathfinder.tryMoveOrDig(rc, away);
        rc.setIndicatorString("KING: cat nearby -> evade/trap/attack");
        return;
    }
}

        // Escape hatch: if boxed in, dig any adjacent dirt first.
        boolean movedOrDug = false;
        for (Direction dir : directions) {
            MapLocation adj = rc.getLocation().add(dir);
            if (rc.canRemoveDirt(adj)) {
                rc.removeDirt(adj);
                movedOrDug = true;
                break;
            }
        }

        if (movedOrDug) {
            rc.setIndicatorString("KING digging out");
            return;
        }

        // Spawn babies (simple): spawn when cheap OR when bank is healthy.
        int currentCost = rc.getCurrentRatCost();
        boolean spawn = currentCost <= 10 || rc.getAllCheese() > currentCost + 200;

        // Promotion phase banking (optional): help reach 50 once, then stop banking after second king exists.
        if (!secondKingBuilt()) {
            int bank = rc.getAllCheese();
            if (bank >= PROMO_ASSIST_CHEESE && bank < PROMO_RALLY_CHEESE) {
                spawn = false;
                rc.setIndicatorString("KING BANKING to 50 | cheese=" + bank);
            }
        }

        // Build or pick up cheese near us
        MapLocation[] near = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        for (MapLocation loc : near) {
            if (spawn && rc.canBuildRat(loc)) {
                rc.buildRat(loc);
                break;
            }
            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                break;
            }
        }

        // Read squeaks and store mines in shared array (reuses your existing global logic in RobotPlayer)
        Message[] squeaks = rc.readSqueaks(rc.getRoundNum());
        for (Message msg : squeaks) {
            int raw = msg.getBytes();
            if (RobotPlayer.getSqueakType(raw) != RobotPlayer.SqueakType.CHEESE_MINE) continue;

            int encoded = RobotPlayer.getSqueakValue(raw);
            // Let RobotPlayer's static mine list handle de-dupe + shared writes
            RobotPlayer.kingReceiveMine(rc, encoded);
        }

        // Prefer to camp near mines (learn if sensed directly)
        MapInfo[] infos = rc.senseNearbyMapInfos();
        for (MapInfo info : infos) {
            if (info.hasCheeseMine()) {
                kingTargetMine = info.getMapLocation();
                break;
            }
        }
        if (kingTargetMine == null || rc.getRoundNum() % 20 == 0 || rc.getLocation().distanceSquaredTo(kingTargetMine) <= 2) {
            kingTargetMine = RobotPlayer.chooseNearestKnownMine(rc, rc.getLocation());
        }

        if (kingTargetMine != null) {
            rc.setIndicatorString("KING->MINE " + kingTargetMine);
            moveTowardWithDig(kingTargetMine);
        } else {
            rc.setIndicatorString("KING wandering");
            moveWanderWithDig();
        }
    }
}