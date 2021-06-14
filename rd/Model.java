/**
*	Model.java
*
*	@author Michael Meadows
*	@version 3.0
*/

import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;

public class Model {

	//===== GLOBALS ===========================================================
	
	public static Random random = new Random();	
		
	// Experiment parameters
	public static final int SAMPLES = 100;
	public static final int INTERACTIONS = 500000;

	// Opinion Dynamics parameters
	public static int N;
	public static final boolean WITH_RD = true;
	
	public static final double E_OP_BOUND = 0.8;
	public static final double E_U = 0.1;
	public static double NE_U;
	public static boolean NE_U_RANDOMISED = true;
	public static final int NE_U_STEPS = 25;
	public static final double NE_U_MIN = 0.2;
	public static final double NE_U_MAX = 2.0;
	public static double PE;
	public static final int PE_STEPS = 15;
	public static final double PE_MAX = 0.3;
	
	public static double LAMBDA;
	public static double LAMBDA_STEPS = 5;
	
	public static double MIU_RA;
	public static final int MIU_RA_STEPS = 16;
	public static double MIU_RD;
	public static final int MIU_RD_STEPS = 16;
	
	// Clustering parameters
	public static int M;
	public static double MIU_KE; 
	public static final int MIU_KE_STEPS = 4;

	//===== MAIN ==============================================================
	
	public static void main (String [] args) {
	
		List agentList;
		
		double [][] yv = new double[MIU_RA_STEPS][MIU_RD_STEPS];
		double [][] ys = new double[MIU_RA_STEPS][MIU_RD_STEPS];
		
		double [] y = new double[SAMPLES];
		
		Agent a1, a2;		
	
		// ----- Parameter and loop setup -----
		
		N = 200;
		// for (N = 200; N <= 1000; N = N + 200) {
		// System.out.println("N: " + N);
		
		// MIU_KE = 0.5;
		// for (int miu_ke = 0; miu_ke <= MIU_KE_STEPS; miu_ke++) {
		// MIU_KE = (1.0 / (double)MIU_KE_STEPS) * (double)miu_ke;
		// System.out.println(" MIU_KE: " + MIU_KE);
		
		M = N;
		// M = 2;
		// for (M = 2; M <= 10; M++) {
		// System.out.println("  M: " + M);
		
		// PE = 0.0;
		// for (int pe = 0; pe < PE_STEPS; pe++) {
		// PE = (PE_MAX / (double)PE_STEPS) * (double)pe; 
		// System.out.println("   PE: " + PE);
		
		// NE_U = 1.0;
		// for (int u = 0; u < NE_U_STEPS; u++) {
		// NE_U = (NE_U_MAX / (double)NE_U_STEPS) * (double)u;
		// System.out.println("    U: " + NE_U);
		
		LAMBDA = 0.4;
		//for (int lambda = 0; lambda <= LAMBDA_STEPS; lambda++) {
		//LAMBDA = (1.0 / (double)LAMBDA_STEPS) * (double)lambda;
		//System.out.println("     LAMBDA: " + LAMBDA);		
		
		// MIU_RA = 0.5;
		for (int miu_ra = 0; miu_ra < MIU_RA_STEPS; miu_ra++) {
		MIU_RA = (0.8 / (double)MIU_RA_STEPS) * (double)miu_ra;
		System.out.println("      MIU_RA: " + MIU_RA);
		// MIU_RD = MIU_RA;
		// MIU_RD = 0.7;
		for (int miu_rd = 0; miu_rd < MIU_RD_STEPS; miu_rd++) {
		MIU_RD = (0.8 / (double)MIU_RD_STEPS) * (double)miu_rd;		
		System.out.println("       MIU_RD: " + MIU_RD);

		
		for (int sample = 0; sample < SAMPLES; sample++) {
		
		// ----- Initialise	-----
		Agent [] a = new Agent[N];
		for (int i = 0; i < a.length; i++)
			a[i] = new Agent(i, 0.0, 0.0);
		List clusteredList = cluster(a, M);		
		agentList = setOpinions(clusteredList);
		
		// ----- Interact -----
		
		for (int interaction = 0; interaction < INTERACTIONS; interaction++) {
			// Pick a random agent and random neighbour.
			a1 = (Agent)agentList.get(random.nextInt(N));
			a2 = a1.getNeighbour(random.nextInt(a1.getNumberOfNeighbours()));
			interact(a1, a2);
		}
		
		y[sample] = calculateY(agentList);
		
		} // SAMPLES loop
		
		// ----- Data -----

		double yCounter = 0.0;
		for (int i = 0; i < y.length; i++)
			yCounter += y[i];
		yv[miu_ra][miu_rd] = yCounter / (double)y.length;
		yCounter = 0.0;
		for (int i = 0; i < y.length; i++)
			yCounter += (y[i] - yv[miu_ra][miu_rd]) * (y[i] - yv[miu_ra][miu_rd]);
		ys[miu_ra][miu_rd] = Math.sqrt(yCounter / (double)y.length);

		} // MIU_RD loop
		} // MIU_RA loop	
		outputAreaGraph(((Double)LAMBDA).toString() + " yv.txt", yv);
		outputAreaGraph(((Double)LAMBDA).toString() + " ys.txt", ys);
		// } // LAMBDA loop
		// } // U loop
		// } // PE loop
		// } // M loop
		// } // MIU_KE loop
		// } // N loop
		
		// ----- Output -----
		
	} // main
	
	//===== CLUSTERING ========================================================
	
	// Takes an array of agents, and returns them as a list with neighbours 
	// defined by the clustering parameters.
	public static List cluster (Agent [] a, int m) {
	
		// Generate completely connected graph of size m.
		for (int i = 0; i < m; i++)
			for(int j = 0; j < m; j++)
				if (i != j)
					a[i].addNeighbour(a[j]);
		
		// Define Neighbours.
		for (int i = m; i < a.length; i++) {
			// Stage 1.
			for (int j = 0; j < m; j++) {
				if (MIU_KE < random.nextDouble()) {
					// Find jth active node.
					int k, counter = 0;
					for (k = 0; k < a.length; k++) {					
						if (a[k].isActive())
							if (counter == j)
								break;
							else
								counter++;
					}					
					if (a[k].isActive()) {
						a[k].addNeighbour(a[i]);
						a[i].addNeighbour(a[k]);
					}
				} else {
					int counter = 0;
					for (int k = 0; k < i; k++) 
						counter += a[k].getNumberOfNeighbours();
					int [] neighbours = new int[counter];
					counter = 0;
					for (int k = 0; k < i; k++)
						for (int l = 0; l < a[k].getNumberOfNeighbours(); l++) {
							neighbours[counter] = ((Agent)a[k].getNeighbour(l)).getID();
							counter++;
						}
					int target;
					boolean valid;
					do {
						valid = true;
						target = neighbours[random.nextInt(neighbours.length)];
						if (target == i) // Empty neighbours are stored as -1.
							valid = false;
						for (int k = 0; k < a[i].getNumberOfNeighbours(); k++)
							if (target == ((Agent)a[i].getNeighbour(k)).getID())
								valid = false;
					} while(!valid);
					
					a[target].addNeighbour(a[i]);
					a[i].addNeighbour(a[target]);
				}
			}
			
			// Stage 2 (Done in Agent constructor).
			// Stage 3.
			double normalisation = 0.0;
			for (int j = 0; j < i; j++)
				if (a[j].isActive())
					normalisation += 1.0 / a[j].getNumberOfNeighbours();
			normalisation = 1.0 / normalisation;
			
			double draw = random.nextDouble();
			for (int j = 0; j < i; j++) {
				if (a[j].isActive()) {
					draw -= (normalisation * (1.0 / a[j].getNumberOfNeighbours()));
					if (draw <= 0.0) {
						a[j].setActive(false);
						break;
					}
				}
			}
		}
		
		// Now that the agents are sorted, put them into a list and return.
		List l = new ArrayList();
		for (int i = 0; i < a.length; i++)
			l.add(a[i]);
		return l;

	} // cluster
	
	//===== SEEDING ===========================================================
	
	// Takes list of clustered agents, and returns them as a list with random 
	// opinions and fixed uncertainties.
	public static List setOpinions (List a) {
		
		double r = 0.0; // Fixed NE_U value.
		// Set agents as moderate.
		for (int i = 0; i < a.size(); i++) {
			r = (random.nextDouble() * (E_OP_BOUND * 2)) - E_OP_BOUND;
			((Agent)a.get(i)).setInitialOpinion(r);
			if (NE_U_RANDOMISED) {
				r = (random.nextDouble() * (NE_U_MAX - NE_U_MIN)) + NE_U_MIN;
				r += random.nextDouble();
				if (r > 2.0)
					r = 2.0;
			} else {
				r = NE_U;
			}
			((Agent)a.get(i)).setInitialUncertainty(r);
		}
		// Adjust a proportion of agents to be extremist.
		int exAgents = (int) ((double)N * PE);
		for (int i = 0; i < exAgents; i++) {
			do {
				r = random.nextDouble();
			} while (r < E_OP_BOUND); 
			if (0 == i % 2) {
				r *= -1; // Split the extremists as both +1 and -1.
			}
			((Agent)a.get(i)).makeExtremist(r);
		}
		
		return a;
	} // setOpinions
	
	//===== INTERACTION =======================================================
	
	// Handles the complete interaction between two agents.
	public static void interact (Agent Ai, Agent Aj) {
			
		double Xi = Ai.getOpinion();
		double Xj = Aj.getOpinion();				
		double Ui = Ai.getUncertainty();
		double Uj = Aj.getUncertainty();

		// Hij == Hji.  Therefore, H.
		double H = Math.min(Xj + Uj, Xi + Ui) - Math.max(Xj - Uj, Xi - Ui);
		double RAji = (H / Uj) - 1.0;
		double RAij = (H / Ui) - 1.0;
		// Gij == Gji.  Therefore, G. (G = H*-1). // Only used in RD.
		double G = Math.max(Xj - Uj, Xi - Ui) - Math.min(Xj + Uj, Xi + Ui);
		double RDji = (G / Uj) - 1.0;
		double RDij = (G / Ui) - 1.0;

		// Update
		boolean rdupdate = random.nextDouble() < LAMBDA;
		if (H > Uj) {
			Ai.setOpinion( Xi + (MIU_RA * RAji * (Xj - Xi)) );
			Ai.setUncertainty( Ui + (MIU_RA * RAji * (Uj - Ui)) );
		} else {
			if (WITH_RD && G > Uj && rdupdate) {
				Ai.setOpinion( Xi - (MIU_RD * RDji * (Xj - Xi)) );
				Ai.setUncertainty( Ui + (MIU_RD * RDji * (Uj - Ui)) );
			} else { 
				Ai.setOpinion(Xi); // For opinion history.
			}
		}
		if (H > Ui) {
			Aj.setOpinion( Xj + (MIU_RA * RAij * (Xi - Xj)) );
			Aj.setUncertainty( Uj + (MIU_RA * RAij * (Ui - Uj)) );
		} else {
			if (WITH_RD && G > Ui && rdupdate) {
				Aj.setOpinion( Xj - (MIU_RD * RDij * (Xi - Xj)) );
				Aj.setUncertainty( Uj + (MIU_RD * RDij * (Ui - Uj)) );
			} else { 
				Aj.setOpinion(Xj); // For opinion history.
			}
		}
		
	} // interact
	
	//===== TOOLS =============================================================

	// Calculates the y-value from a give agent list.
	public static double calculateY (List a) {
	
		int newPosEx = 0;
		int newNegEx = 0;
		int numberOfExtremists = 0;
		for (int i = 0; i < a.size(); i++) {
			if ( !((Agent)a.get(i)).wasInitExtreme() ) { // Don't count original extremists.
				if ( ((Agent)a.get(i)).getOpinion() > E_OP_BOUND)
					newPosEx++;
				if ( ((Agent)a.get(i)).getOpinion() < -E_OP_BOUND)
					newNegEx++;
			} else {
				numberOfExtremists++;
			}
		}	
		
		double pplus = (double)newPosEx / (double)(N - numberOfExtremists);
		double pminus = (double)newNegEx / (double)(N - numberOfExtremists);
		return (pplus * pplus) + (pminus * pminus);
		
	} // calculateY
	
	//===== OUTPUTTING ========================================================

	// Iterates through each agent and prints their history.  For assessing 
	// individual runs.
	public static void outputTrajectories (List l) {
	
		try {
			FileWriter f = new FileWriter("results traj.txt", true);
			BufferedWriter b = new BufferedWriter(f);

			int maxLength = -1;
			for (int i = 0; i < l.size(); i++)
				if (((Agent)l.get(i)).getNumberOfOpinions() > maxLength)
					maxLength = ((Agent)l.get(i)).getNumberOfOpinions();
			Agent a;
			for (int i = 0; i < l.size(); i++) {
				a = (Agent)l.get(i);
				b.write("Agent " + (a.getID()) + "\t");
				for (int j = 0; j < a.getNumberOfOpinions(); j++)
					b.write(a.getOpinion(j) + "\t");
				// "Square off" the right side of the table.
				for (int k = a.getNumberOfOpinions(); k < maxLength; k++)
					b.write(a.getOpinion() + "\t");
				b.newLine();
			}
			
			b.close();
			
		} catch (IOException e) {}
		
	} // outputTrajectories

	public static void outputDeffuantTrajectories (List l) {

		try {
			int width = -1;
			for (int i = 0; i < l.size(); i++)
				if (((Agent)l.get(i)).getNumberOfOpinions() > width)
					width = ((Agent)l.get(i)).getNumberOfOpinions();
					
			int updates[][] = new int[201][width];
			for(int i = 0; i < updates.length; i++)
				for(int j = 0; j < updates[i].length; j++)
					updates[i][j] = 0;
			BufferedImage graph = new BufferedImage(width, 201, BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < 201; y++)
				for (int x = 0; x < width; x++)
					graph.setRGB(x, y, generateColour(255,255,255));
			Agent a;
			double op, u;
			op = 0.0;
			u = 0.0;
			for (int i = 0; i < l.size(); i++) {
				a = (Agent)l.get(i);
				int currentY = 0;
				int prevY = convertOpToY(a.getOpinion(0));
				double uncertainty = 0;
				for (int j = 0; j < width; j++) {
					if (j < a.getNumberOfOpinions()) {
						currentY = convertOpToY(a.getOpinion(j));
						uncertainty = a.getUncertainty(j);
					}
						
					if (prevY < currentY) {;
						do {
							prevY++;
							graph = updateRGB(graph, j, prevY, uncertainty);
						} while (prevY < currentY - 1);
					}
					if (prevY > currentY) {
						do {
							prevY--;
							graph = updateRGB(graph, j, prevY, uncertainty);
						} while (prevY > currentY + 1);
					}
					graph = updateRGB(graph, j, currentY, uncertainty);
					prevY = currentY;
				}
			}				
			File f = new File("graph.png");
			ImageIO.write(graph, "PNG", f);
		} catch (Exception e) {}
		
	} // outputDeffuantTrajectories
	public static int generateColour(int r, int g, int b) {
		return ((r << 16) | (g << 8) | b);
	} // generateColour
	public static int convertOpToY(double o) {
		o = o * 100;
		int hardO = (int)o;
		hardO = (hardO - 100) * -1;
		return hardO;
	} // convertOpToY
	public static BufferedImage updateRGB(BufferedImage bi, int x, int y, double u) {
		double uncertaintyMax = 0.5;
		double uncertaintyHalf = uncertaintyMax / 2.0;
		double uncertaintyEdge = uncertaintyHalf / 3.0;
		int r, g;
		double modifier = 255.0;
		if (u >= uncertaintyHalf) {
			g = 255;
		} else {
			if (u < uncertaintyEdge) {
				g = 0;
			} else {
				g = (int)(((u - uncertaintyEdge) / (uncertaintyHalf - uncertaintyEdge)) * modifier);
			}
		}	
		if (u <= uncertaintyHalf) {
			r = 255;
		} else {
			if (u > uncertaintyMax - uncertaintyEdge) {
				r = 0;
			} else {
				r = (int)(((u - uncertaintyHalf) / (uncertaintyHalf - uncertaintyEdge)) * -modifier);
			}
		}
		bi.setRGB(x, y, generateColour(r,g,0));
		return bi;
	} // updateRGB
	
	// Outputs given y-values values to file s. Must be rectangular data.
	public static void outputAreaGraph(String s, String [] xAxis, String [] yAxis, double y[][]) {
	
		try {
			FileWriter f = new FileWriter(s);
			BufferedWriter b = new BufferedWriter(f);
			
			if (yAxis.length != y.length || yAxis.length == 0 || y.length == 0)
				System.out.println("outputAreaGraph is broken (2).");
			for (int i = 0; i < yAxis.length; i++)
				if (xAxis.length != y[i].length || xAxis.length == 0 || y[i].length == 0)
					System.out.println("outputAreaGraph is broken (1).");
				
			/*
			// Format and output data.
			// yAxis \ xAxis xAxis[0] xAxis[1] xAxis[2] ...
			// yAxis[0]			 x   	x		x
			// yAxis[1]			 x    	x   	x
			// yAxis[2]			 x    	x    	x
			// ...			
			*/				
			for (int i = 0; i < xAxis.length; i++)
				b.write("\t" + xAxis[i]);
			for (int i = 0; i < yAxis.length; i++) {
				b.newLine();
				b.write(yAxis[i]);
				for (int j = 0; j < y[i].length; j++) {
					b.write("\t" + ((Double)y[i][j]).toString());
				}
			}
			b.close();
			
		} catch (IOException e) {}
		
	} // outputAreaGraph

	// Outputs given y-values values to file s.
	public static void outputAreaGraph(String s, double y[][]) {
	
		try {
			FileWriter f = new FileWriter(s);
			BufferedWriter b = new BufferedWriter(f);
							
			for (int i = 0; i < y.length; i++) {
				for (int j = 0; j < y[i].length; j++) {
					b.write(((Double)y[i][j]).toString() + "\t");
				}
				b.newLine();
			}
			b.close();
			
		} catch (IOException e) {}
		
	} // outputAreaGraph
	
	// Outputs given y-values to a given file name.
	public static void outputString (String s, String toOutput) {
	
		try {
			FileWriter f = new FileWriter(s);
			BufferedWriter b = new BufferedWriter(f);
			
			b.write(toOutput);
			b.close();
			
		} catch (IOException e) {}
		
	} // outputString
	
} // Model