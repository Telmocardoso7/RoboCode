package Robos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.CustomEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import standardOdometer.*; 

public class OdometerSA extends AdvancedRobot {

	
	//Class attributes.
	private RobotStatus robotStatus;
	private Odometer odometer = new Odometer("IsRacing", this);
	
	private long startingTime;
	private boolean running = false, 
			lockState1 = false, 
			lockState2 = false, 
			lockState3 = false, 
			lockState4 = false, 
			finishing = false;

    private class Position<T> {
	    T x, y, distance, angle;
	    Position(T x, T y, T distance, T angle) {
	        this.x = x;
	        this.y = y;
	        this.distance = distance;
	        this.angle = angle;
	    }
    }
    
    private List<Position<Double>> listOfpositions = new ArrayList<Position<Double>>();	
    private List<Double> listOfquadrants = new ArrayList<Double>();


	private double totaldistance = 0, lastX = 0,lastY = 0;
	private int quadrant = 0;
	
	
	@Override
	public void run()
	{
		this.lockState1 = true; //By default the first state to be executed.
		while(true)
		{
			if(this.running)
			{
				long currentTime = (new Date().getTime())/1000; //getting the current time from when this method has been called.
				if((currentTime - this.startingTime) > 10) //This method will execute after 10 seconds, from robot hitting (18,18) position.
				{
					this.addCustomEvent(odometer); //Starting the odometer from library.
					
					firstState();
					secondState();
					thirdState();
					fourthState();
				}
			}
			else
			{
				goTo(18, 18);
			}
			execute();
		
		}
	}
	
	public void onCustomEvent(CustomEvent ev)
	{
		Condition cd = ev.getCondition();
		if (cd.getName().equals("IsRacing"))
			this.odometer.getRaceDistance();
	}
	

	//Starting moving to a specific point.
	private void goTo(double x, double y) 
	{
		/* Transform our coordinates into a vector */
		x -= getX();
		y -= getY();
	 
		/* Calculate the angle to the target position */
		double angleToTarget = Math.atan2(x, y);
	 
		/* Calculate the turn required get there */
		double targetAngle = Utils.normalRelativeAngle(angleToTarget - getHeadingRadians());
	 
		/* 
		 * The Java Hypot method is a quick way of getting the length
		 * of a vector. Which in this case is also the distance between
		 * our robot and the target location.
		 */
		double distance = Math.hypot(x, y);
	 
		/* This is a simple method of performing set front as back */
		double turnAngle = Math.atan(Math.tan(targetAngle));
		setTurnRightRadians(turnAngle);
		if(targetAngle == turnAngle) {
			setAhead(distance);
		} else {
			setBack(distance);
		}
	}
	
	
	@Override
	public void onStatus(StatusEvent event)
	{
		this.robotStatus = event.getStatus(); //saving the current robot status, in a attribute global variable.
		
		double currentX = getX();
		double currentY = getY();
		
		//################REGION BEGGINS##################
		//Personal odometer code.
		double distance = distanceBetween2Points(this.lastX, this.lastY, currentX, currentY); 
		if(this.running)
		{
			this.totaldistance += distance;
			this.lastX = currentX;
			this.lastY = currentY;
			if(this.finishing)
			{
				out.println("Moved " + this.totaldistance + "meters during this race");
				this.totaldistance = 0;
				this.finishing = false;
			}
		}
		//################REGION ENDS####################
		
		//When the robot hits position (18,18), is necessary to save the current time, and change running flag to true, in order to execute all robot states.
		if(currentX == 18 && currentY == 18 && !this.running){
			this.startingTime = (new Date().getTime())/1000; 
			this.running = true;  
		}
	}
	
	//Getting the distance between 2 points, in order to be used in my personal odometer.
	public double distanceBetween2Points(double x1,double y1,double x2, double y2){
		return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		//Starts detecting other robots, only when this flag has been set to true.
		if(this.running)
		{

			double angleToEnemy = e.getBearing();
			double angleToEnemyRadians = e.getBearingRadians();
	
	        // Calculate the angle to the scanned robot
	      	double angle = Math.toRadians((this.robotStatus.getHeading() + angleToEnemy) % 360);
	
	        // Calculate the coordinates of the robot
	      	double enemyX = (this.robotStatus.getX() + Math.sin(angle) * e.getDistance());
	      	double enemyY = (this.robotStatus.getY() + Math.cos(angle) * e.getDistance());
	      	double enemyDistance = e.getDistance();
	      	
	      	//Saving enemy position variables in a list of object class.
	      	//Only enemy positions from different quadrants, that were never reached will be saved.
	      	if((getQuadrant(getX(), getY()) != getQuadrant(enemyX, enemyY)) && (!listOfquadrants.contains((double)getQuadrant(enemyX, enemyY))))
				this.listOfpositions.add(new Position<Double>(enemyX, enemyY, enemyDistance, angleToEnemyRadians));
		}
	}
	
	
	private void firstState()
	{
		if(this.lockState1)
		{
			turnRadarRight(360); //Performing a new scanning to search other robots.
			if(getRadarTurnRemaining() == 0)
			{
				this.lockState1 = false;
				this.lockState2 = true;
			}
		}
	}
	
	
	
	private void secondState()
	{
		if(this.lockState2)
		{
		
			move(); //Moving to the nearest robot point.
			if(getDistanceRemaining() == 0)
			{
				if(this.quadrant != 3)
					this.lockState3 = true;
				else
					this.lockState4 = true;
				this.lockState2 = false;
			}
		}
		
	}
	
	
	private void thirdState()
	{
		if(this.lockState3)
		{
			turn(); //changing direction according the nearest robot
			if(getTurnRemaining() == 0)
			{
				this.lockState3 = false;
				this.lockState1 = true;
				this.listOfpositions.clear(); //Cleaning positions list.
			}
		}
	}
	
	
	private void fourthState()
	{
		if(this.lockState4)
		{
			this.running = false; //Going back to the initial point.
			if(getDistanceRemaining() == 0)
			{
				//Cleaning old lists.
				this.listOfpositions.clear();
				this.listOfquadrants.clear();
				//adjusting states to a new cycle.
				this.lockState4 = false;
				this.lockState1 = true;
				this.finishing = true;
			}
		}
	}
	
	private void turn()
	{
		double min_distance = this.listOfpositions.get(0).distance;
		double angle = this.listOfpositions.get(0).angle;
		for (Position<Double> position: this.listOfpositions) {
			if(position.distance < min_distance)
			{	
				min_distance = position.distance;
				angle = position.angle;
			}
		}
	
		double radians = getHeadingRadians() + angle - getRadarHeadingRadians();
		
		turnRightRadians(Utils.normalRelativeAngle(radians));
	}
	
	private void move()
	{
	
		double min_distance = this.listOfpositions.get(0).distance;
	    double x = this.listOfpositions.get(0).x;
		double y = this.listOfpositions.get(0).y;
		for (Position<Double> position: this.listOfpositions) {
			if(position.distance < min_distance)
			{	
				min_distance = position.distance;
				x = position.x;
				y = position.y;
			}
		}
		
		this.quadrant = getQuadrant(x, y);
		listOfquadrants.add((double)this.quadrant);
	    if(this.quadrant == 1)
	    {
	    	goTo(x-(2*getWidth()), y+(2*getHeight()));
	    }
	    if (this.quadrant == 2)
	    {
	    	goTo(x+(2*getWidth()), y+(2*getHeight()));
	    }
	    if (this.quadrant == 3)
	    {
	    	goTo(x+(2*getWidth()), y-(2*getHeight()));
	    }	
	}
	
	private int getQuadrant(double x, double y)
	{
		double height = getBattleFieldHeight()/2;
		double width = getBattleFieldWidth()/2;
		
		if((x < width) && (y > height))
			return 1;
		else if((x > width) && (y > height))
			return 2;
		else if((x > width) && (y < height))
			return 3;
		else 
			return 0;
	}
	
	
	
}
