package commsplayer;
import battlecode.common.*;

public class Politician extends Robot {

    public Politician(RobotController r){
        super(r);
    }

    public void takeTurn() throws GameActionException{
        /*
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        if (tryMove(randomDirection()))
            System.out.println("I moved!");*/
        System.out.println("Attempting to set a flag");
        createMessage(7, 57);
    }

}