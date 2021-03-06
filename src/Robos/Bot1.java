package Robos;

import robocode.Droid;
import robocode.HitByBulletEvent;
import robocode.MessageEvent;
import robocode.TeamRobot;
import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.geom.Rectangle2D;

public class Bot1 extends TeamRobot implements Droid{

	public void run() {
		out.println("Droid1 ready.");
	}

	public void onMessageReceived(MessageEvent e) {

		// Fire at a point
		if (e.getMessage() instanceof EnemyPosition) {
			EnemyPosition p = (EnemyPosition) e.getMessage();
			// Calculate x and y to target
			double dx = p.getX() - this.getX();
			double dy = p.getY() - this.getY();
			double bearing = p.getBearing();
			// Calculate angle to target
			double theta = Math.toDegrees(Math.atan2(dx, dy));

			// Turn gun to target
			turnGunRight(normalRelativeAngleDegrees(theta - getGunHeading()));
			// Fire hard!
			fire(3);
			double goalDirection = bearing-Math.PI/2*1;
			Rectangle2D fieldRect = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
			while (!fieldRect.contains(getX()+Math.sin(goalDirection)*120, getY()+ Math.cos(goalDirection)*120))
			{
				goalDirection += 1*.1;	//turn a little toward enemy and try again
			}
			double turn = robocode.util.Utils.normalRelativeAngle(goalDirection-getHeadingRadians());
			if (Math.abs(turn) > Math.PI/2)
			{
				turn = robocode.util.Utils.normalRelativeAngle(turn + Math.PI);
				back(100);
			}
			else
				ahead(100);
			turnRightRadians(turn);
		}
		else if(e.getMessage() instanceof robotColors) {
			robotColors teamColors = (robotColors) e.getMessage();
			setBodyColor(teamColors.bodyColor);
			setGunColor(teamColors.gunColor);
			setRadarColor(teamColors.radarColor);
			setScanColor(teamColors.scanColor);
			setBulletColor(teamColors.bulletColor);
		}
	}
	
	public void onHitByBullet(HitByBulletEvent e){
		if(e.getBearing() > 0) {
			turnRight(e.getBearing());
		}
		else {
        	turnLeft(-e.getBearing());	
		}
		fire(1);
		turnRight(90);
	    ahead(100);
	}
}

