
import java.util.*;
import java.io.*;

public class WumpusWorld {
	public static final int PIT_FLAG 		= 1;
	public static final int WUMPUS_FLAG 	= 2;
	public static final int GOLD_FLAG 		= 4;
	public static final int BREEZE_FLAG 	= 8;
	public static final int STENCH_FLAG 	= 16;
	public static final int GLITTER_FLAG 	= 32;
	public static final int BUMP_FLAG 		= 64;
	public static final int SCREAM_FLAG 	= 128;

	private static final int FACING_NORT	= 1;
	private static final int FACING_EAST	= 2;
	private static final int FACING_WEST	= 3;
	private static final int FACING_SOUTH	= 4;

	/*
	 * Actions
	 */
	public static final int GO_FORWARD		= 1;
	public static final int TURN_LEFT		= 2;
	public static final int TURN_RIGHT		= 3;
	public static final int GRAB			= 4;
	public static final int SHOOT			= 5;
	public static final int CLIMB			= 6;

	public static final String STR_GO_FORWARD	= "Avanzar";
	public static final String STR_TURN_LEFT	= "Izquierda";
	public static final String STR_TURN_RIGHT	= "Derecha";
	public static final String STR_GRAB			= "Agarrar";
	public static final String STR_SHOOT		= "Disparar";
	public static final String STR_CLIMB		= "Escalar";


	/*
	 * Perceptions
	 */
	private int 	perceptions = 0;

	private double	actionCost	= 1.0;
	private double	outWithGold	= 1000.0;
	private double 	killCost	= 10000.0;
	
	private int 	worldDimensionX;
	private int 	worldDimensionY;
	private double 	pitProbability;

	private int 	agentPosX 	= 0;
	private int 	agentPosY 	= 0;
	private int 	agentDir 	= FACING_EAST;
	private boolean agentSafe 	= true;
	private double	agentScore	= 0.0;
	private boolean agentArrow	= true;
	private boolean agentGold	= false;
	private boolean agentOut	= false;

	private int world[];
	
	PrintStream out = System.out;
	Scanner reader;

	public WumpusWorld(int worldDimensionX, 
	                   int worldDimensionY, 
	                   double pitProbability, 
	                   double actionCost, 
	                   double outWithGold, 
	                   double killCost) {

		this.worldDimensionX = worldDimensionX;
		this.worldDimensionY = worldDimensionY;
		this.pitProbability = pitProbability;
		this.actionCost = actionCost;
		this.outWithGold = outWithGold;
		this.killCost = killCost;
		
		world = new int[worldDimensionX * worldDimensionY];
		Arrays.fill(world, 0);
		for (int i = 1; i < world.length; i++) {
			if (Math.random() < pitProbability) {
				world[i] |= PIT_FLAG;

				int s1 = i - worldDimensionX;
				int s2 = i + worldDimensionX;
				int s3 = i - 1;
				int s4 = i + 1;

				if (s1 >= 0) 			world[s1] |= BREEZE_FLAG;
				if (s2 < world.length) 	world[s2] |= BREEZE_FLAG;
				if (s3 >= 0 && (i % worldDimensionX != 0)) world[s3] |= BREEZE_FLAG;
				if (s4 < world.length && (i % worldDimensionX != worldDimensionX-1)) world[s4] |= BREEZE_FLAG;
			}
		}
		
		int wumpusPos = (int)(Math.random() * (world.length - 1)) + 1;
		world[wumpusPos] |= WUMPUS_FLAG;
		int s1 = wumpusPos - worldDimensionX;
		int s2 = wumpusPos + worldDimensionX;
		int s3 = wumpusPos - 1;
		int s4 = wumpusPos + 1;

		if (s1 >= 0) 			world[s1] |= STENCH_FLAG;
		if (s2 < world.length) 	world[s2] |= STENCH_FLAG;
		if (s3 >= 0 && (wumpusPos % worldDimensionX != 0)) world[s3] |= STENCH_FLAG;
		if (s4 < world.length && (wumpusPos % worldDimensionX != worldDimensionX-1)) world[s4] |= STENCH_FLAG;
		
		int goldPos = (int)(Math.random() * (world.length - 1)) + 1;
		world[goldPos] |= GOLD_FLAG;
		world[goldPos] |= GLITTER_FLAG;
		
		perceptions |= world[0];
		
		reader = new Scanner(System.in);
	}
	
	public void makeAction(int action) {
		perceptions = 0;
		switch (action) {
			case GO_FORWARD : goForward(); break;
			case TURN_LEFT 	: turnLeft(); break;
			case TURN_RIGHT : turnRight(); break;
			case GRAB		: grab(); break;
			case SHOOT		: shoot(); break;
			case CLIMB		: climb(); break;
		}
		agentScore -= actionCost;
		perceptions |= world[agentPosY*worldDimensionX + agentPosX];

		if ((perceptions & PIT_FLAG) != 0 || (perceptions & WUMPUS_FLAG) != 0) {
			agentScore -= killCost;
			agentSafe = false;
			agentOut = true;
		}
	}
	
	public void makeAction(String action) {
		if (STR_GO_FORWARD.equals(action)) makeAction(GO_FORWARD);
		else if (STR_TURN_LEFT.equals(action)) makeAction(TURN_LEFT);
		else if (STR_TURN_RIGHT.equals(action)) makeAction(TURN_RIGHT);
		else if (STR_GRAB.equals(action)) makeAction(GRAB);
		else if (STR_SHOOT.equals(action)) makeAction(SHOOT);
		else if (STR_CLIMB.equals(action)) makeAction(CLIMB);
	}
	
	private void goForward() {
		switch(agentDir) {
			case FACING_NORT : {
				if (agentPosY == worldDimensionY-1) perceptions |= BUMP_FLAG;
				else agentPosY++;
			} break;
			case FACING_EAST : {
				if (agentPosX == worldDimensionX-1) perceptions |= BUMP_FLAG;
				else agentPosX++;
			} break;
			case FACING_WEST : {
				if (agentPosX == 0) perceptions |= BUMP_FLAG;
				else agentPosX--;
			} break;
			case FACING_SOUTH : {
				if (agentPosY == 0) perceptions |= BUMP_FLAG;
				else agentPosY--;
			} break;
		}
	}
	
	private void turnLeft() {
		switch (agentDir) {
			case FACING_NORT : agentDir = FACING_WEST; break;
			case FACING_EAST : agentDir = FACING_NORT; break;
			case FACING_SOUTH: agentDir = FACING_EAST; break;
			case FACING_WEST : agentDir = FACING_SOUTH; break;
		}
	}

	private void turnRight() {
		switch (agentDir) {
			case FACING_NORT : agentDir = FACING_EAST; break;
			case FACING_EAST : agentDir = FACING_SOUTH; break;
			case FACING_SOUTH: agentDir = FACING_WEST; break;
			case FACING_WEST : agentDir = FACING_NORT; break;
		}
	}

	private void grab() {
		if ((world[agentPosY*worldDimensionX + agentPosX] & GOLD_FLAG) != 0) {
			agentGold = true;
			world[agentPosY*worldDimensionX + agentPosX] &= (0xFFFFFFFF ^ GOLD_FLAG);
			world[agentPosY*worldDimensionX + agentPosX] &= (0xFFFFFFFF ^ GLITTER_FLAG);
		}
	}

	private void shoot() {
		if (!agentArrow) return;
		
		int start = 0, end = 0, arrowX = agentPosX, arrowY = agentPosY, dx=0, dy=0;
		switch (agentDir) {
			case FACING_NORT : start = agentPosY; end=worldDimensionY; dx=0; dy=1; break;
			case FACING_EAST : start = agentPosX; end = worldDimensionX; dx=1; dy=0; break;
			case FACING_SOUTH: start = 0; end = agentPosY; dx=0; dy=-1; break;
			case FACING_WEST : start = 0; end = agentPosX; dx=-1; dy=0; break;
		}
		
		boolean wumpusKilled = false;
		int pos = start;
		while (++pos < end) {
			arrowX += dx;
			arrowY += dy;
			
			int arrowPos = arrowY * worldDimensionX + arrowX;
			if ((world[arrowPos] & WUMPUS_FLAG) != 0) {
				perceptions |= SCREAM_FLAG;
				wumpusKilled = true;
			}

			if (wumpusKilled) {
				world[arrowPos] &= (0xFFFFFFFF ^ WUMPUS_FLAG);
				
				int s1 = arrowPos - worldDimensionX;
				int s2 = arrowPos + worldDimensionX;
				int s3 = arrowPos - 1;
				int s4 = arrowPos + 1;

				if (s1 >= 0) 			world[s1] &= (0xFFFFFFFF ^ STENCH_FLAG);
				if (s2 < world.length) 	world[s2] &= (0xFFFFFFFF ^ STENCH_FLAG);
				if (s3 >= 0 && (arrowPos % worldDimensionX != 0)) world[s3] &= (0xFFFFFFFF ^ STENCH_FLAG);
				if (s4 < world.length && (arrowPos % worldDimensionX != worldDimensionX-1)) world[s4] &= (0xFFFFFFFF ^ STENCH_FLAG);
			}
		}
		agentArrow = false;
	}
	
	private void climb() {
		if (agentPosX == 0 && agentPosY == 0) {
			if (agentGold) agentScore += outWithGold;
			agentOut = true;
		}
	}
	
	public double getAgentScore() {return agentScore;}
	public boolean isAgentOut() {return agentOut;}
	public boolean isAgentOk() {return agentSafe;}

	public String getPerceptions() {
		boolean stench = (perceptions & STENCH_FLAG) != 0;
		boolean breeze = (perceptions & BREEZE_FLAG) != 0;
		boolean glitter = (perceptions & GLITTER_FLAG) != 0;
		boolean bump = (perceptions & BUMP_FLAG) != 0;
		boolean scream = (perceptions & SCREAM_FLAG) != 0;
		
		String hedor, brisa, resplandor, golpe, grito;
		
		hedor = ((stench) ? "hedor(si)" : "hedor(no)");
		brisa = ((breeze) ? "brisa(si)" : "brisa(no)");
		resplandor = ((glitter) ? "resplandor(si)" : "resplandor(no)");
		golpe = ((bump) ? "golpe(si)" : "golpe(no)");
		grito = ((scream) ? "grito(si)" : "grito(no)");

		return "percepcion("+hedor+","+brisa+","+resplandor+","+golpe+","+grito+")";
	}

	public void printWorld() {
/*		FormatStringBuffer fb = new FormatStringBuffer("%-2i");
		out.println("Agent Position: ("+(agentPosX+1)+", "+(agentPosY+1)+")");
		
		String direction="";
		switch (agentDir) {
			case FACING_WEST : direction = "FACING_WEST"; break;
			case FACING_NORT : direction = "FACING_NORT"; break;
			case FACING_EAST : direction = "FACING_EAST"; break;
			case FACING_SOUTH: direction = "FACING_SOUTH"; break;
		}
		
		out.println("Agent Direction: "+direction);
		for (int i = worldDimensionY-1; i >= 0; i--) {
			out.print(fb.format(i+1)+"|");
			for (int j = 0; j < worldDimensionX; j++) {
				out.print(FormatStringBuffer.intToBinary(world[i*worldDimensionX+j], 6)+"|");
			}
			out.println("");
		}
		out.print("  ");
		for (int j = 0; j < worldDimensionX; j++) {
			out.print("         "+fb.format(j+1)+"  ");
		}
		out.println("");*/
	}
	
	public final void setOut(PrintStream out) {
		this.out = out;
	}

	public final PrintStream getOut() {
		return out;
	}
	
	public void readAction() {
		String action = reader.nextLine();
		makeAction(action);
	}
	
	public String readLine() {
		return reader.nextLine();
	}
	
	public static void main(String args[]) {
		int n = 100;
		if ( args.length == 1 ) {
			try {
				n = Integer.parseInt(args[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("SIMULATION_STARTED");
		
		for (int i = 0; i < n; i++) {

			WumpusWorld world = new WumpusWorld(10, 
								10, 
								0.08, 
								1, 
								1000, 
								1000);

			System.out.println("EPISODE_STARTED");
			
			while ( world.isAgentOk() & !world.isAgentOut() ) {
				System.out.println( world.getPerceptions() );
				world.readAction();
			}
			System.out.println("EPISODE_ENDED, AGENT_SCORE=" + world.agentScore);
			String line = world.readLine();
			if ( !"START_SIMULATION_REQUEST".equals(line) ) break;
		}
	}
}