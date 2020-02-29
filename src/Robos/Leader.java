package Robos;

import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

public class Leader extends TeamRobot {
	public void run() {
		while (true) {
			setTurnRadarRight(10000);
			ahead(1);
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if (isTeammate(e.getName())) {
			return;
		}
		double enemyBearing = this.getHeading() + e.getBearing();
		double enemyX = getX() + e.getDistance() * Math.sin(Math.toRadians(enemyBearing));
		double enemyY = getY() + e.getDistance() * Math.cos(Math.toRadians(enemyBearing));
		double bearing = e.getBearing();

		try {
			broadcastMessage(new EnemyPosition(enemyX, enemyY, e.getBearing()));
			double theta = Math.toDegrees(Math.atan2(enemyX - this.getX(), enemyY - this.getY()));
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
		} catch (IOException ex) {
			out.println("Unable to send order: ");
			ex.printStackTrace(out);
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		turnLeft(90 - e.getBearing());
	}
}

