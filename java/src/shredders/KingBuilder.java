package shredders;

import battlecode.common.*;

public class KingBuilder extends BabyRat {

    public final PathFinding pf = new PathFinding();

    // Must match RatKing.java
    private static final int SA_KING_X = 1;
    private static final int SA_KING_Y = 2;

    // ===== TUNING KNOBS =====
    private static final int PROMO_ASSIST_CHEESE = 50;
    private static final int PROMO_CHEESE = 50;

    // How far from king to place the promotion site (avoid intersecting king 3×3)
    private static final int SITE_OFFSET = 5;

    // If a site seems invalid (can’t promote), switch after this many rounds
    private static final int SITE_TIMEOUT = 45;

    // Internal state
    private MapLocation site = null;
    private int siteChosenRound = -9999;
    private int siteIndex = 0;

    public KingBuilder(RobotController rc) {
        super(rc);
    }

    @Override
    public void doAction() throws GameActionException {
        MapLocation king = readKingBeacon();

        // 0) If we can promote, do it immediately
        if (rc.isActionReady() && rc.getAllCheese() >= PROMO_CHEESE && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            System.out.println("KING BUILT by " + rc.getID() + " at round " + rc.getRoundNum());
            return;
        }

        // 1) If carrying cheese, deliver (stops king starvation)
        if (rc.getRawCheese() > 0) {
            if (deliverToVisibleKing()) {
                rc.setIndicatorString("KB: delivered");
                return;
            }
            if (king != null) {
                pf.moveToTarget(rc, king, rc.getLocation());
                rc.setIndicatorString("KB: returning w/cheese");
            } else {
                moveRandom(rc);
                rc.setIndicatorString("KB: no beacon roam");
            }
            return;
        }

        // 2) If not in promo window, behave like a normal worker
        if (king == null || rc.getAllCheese() < PROMO_ASSIST_CHEESE) {
            // pick up adjacent cheese if possible
            for (Direction dir : directions) {
                MapLocation loc = rc.getLocation().add(dir);
                if (rc.canPickUpCheese(loc)) {
                    rc.pickUpCheese(loc);
                    rc.setIndicatorString("KB: picked");
                    return;
                }
            }
            moveRandom(rc);
            rc.setIndicatorString("KB: roam");
            return;
        }

        // 3) Promo window: form a hard pack at a chosen site near king
        chooseOrRotateSite(king);

        int d2 = rc.getLocation().distanceSquaredTo(site);

        // Move to site
        if (d2 > 0) {
            pf.moveToTarget(rc, site, rc.getLocation());
            rc.setIndicatorString("KB: packing -> " + site);
            return;
        }

        // 4) We are ON the site: hold position, attempt promote each turn
        // Count allies in 3×3 around us (distanceSquared <= 2 approximates the 3×3)
        int pack = rc.senseNearbyRobots(2, rc.getTeam()).length;

        // If we have a pack but still can’t promote, site is likely invalid (impassable/cat overlap/etc)
        if (pack >= 7 && !rc.canBecomeRatKing() && (rc.getRoundNum() - siteChosenRound) >= 8) {
            // force rotation sooner
            siteChosenRound -= (SITE_TIMEOUT / 2);
        }

        rc.setIndicatorString("KB: HOLD site pack=" + pack + " canPromote=" + rc.canBecomeRatKing());
    }

    private void chooseOrRotateSite(MapLocation king) throws GameActionException {
        // Candidate sites around the king
        // We cycle through these if site invalid
        MapLocation[] candidates = new MapLocation[] {
                new MapLocation(king.x + SITE_OFFSET, king.y),
                new MapLocation(king.x - SITE_OFFSET, king.y),
                new MapLocation(king.x, king.y + SITE_OFFSET),
                new MapLocation(king.x, king.y - SITE_OFFSET),
                new MapLocation(king.x + SITE_OFFSET, king.y + SITE_OFFSET),
                new MapLocation(king.x + SITE_OFFSET, king.y - SITE_OFFSET),
                new MapLocation(king.x - SITE_OFFSET, king.y + SITE_OFFSET),
                new MapLocation(king.x - SITE_OFFSET, king.y - SITE_OFFSET)
        };

        if (site == null) {
            site = candidates[Math.floorMod(rc.getID(), candidates.length)];
            siteChosenRound = rc.getRoundNum();
            siteIndex = 0;
            return;
        }

        // Rotate site if we've been trying too long
        if (rc.getRoundNum() - siteChosenRound >= SITE_TIMEOUT) {
            siteIndex = (siteIndex + 1) % candidates.length;
            site = candidates[siteIndex];
            siteChosenRound = rc.getRoundNum();
        }
    }

    private boolean deliverToVisibleKing() throws GameActionException {
        RobotInfo[] friends = rc.senseNearbyRobots(18, rc.getTeam());
        for (RobotInfo r : friends) {
            if (r.getType().isRatKingType()) {
                MapLocation k = r.getLocation();
                int raw = rc.getRawCheese();
                if (raw > 0 && rc.canTransferCheese(k, raw)) {
                    rc.transferCheese(k, raw);
                    return true;
                }
            }
        }
        return false;
    }

    private MapLocation readKingBeacon() throws GameActionException {
        int x = rc.readSharedArray(SA_KING_X);
        int y = rc.readSharedArray(SA_KING_Y);
        if (x == 0 && y == 0) return null;
        return new MapLocation(x, y);
    }
}