/**
*	Agent.java
*
*	@author Michael Meadows
*	@version 3.0
*/

import java.util.*;

public class Agent {

	public static final double OPINION_MIN = -1.0;
	public static final double OPINION_MAX = 1.0;

	public static final double UNCERTAINTY_MIN = 0.001;
	public static final double UNCERTAINTY_MAX = 2.0;

	private List opinion, uncertainty, neighbour;
	private int id;
	private boolean active, initExtreme;

	public Agent(int i, double o, double u) {
		this.opinion = new ArrayList();
		this.uncertainty = new ArrayList();
		this.neighbour = new ArrayList();
		this.setOpinion(o);
		this.setUncertainty(u);
		this.setID(i);
		this.setActive(true);
		this.setInitExtreme(false);
	}

	public double getUncertainty() { 
		if (this.uncertainty.size() < 1) {
			// System.out.println("Error: Agent.java - getUncertainty(): uncertainty.size() < 1");
			return 0.0;
		} else {
			return ((Double)this.uncertainty.get(this.uncertainty.size()-1)).doubleValue(); 
		}
	} // getUncertainty
	public double getUncertainty(int i) { 
		if (i < 0) {
			// System.out.println("Error: Agent.java - getUncertainty(int i): i < 0");
			return 0.0;
		} else {
			if (this.uncertainty.size() <= i) {
				// System.out.println("Error: Agent.java - getUncertainty(int i): uncertainty.size() <= i");
				return 0.0;
			} else { 
				return ((Double)this.uncertainty.get(i)).doubleValue(); 
			}
		}
	} // getUncertainty
	public void setUncertainty(double u) { 
		if (u < UNCERTAINTY_MIN) {
			// System.out.println("Error: Agent.java - setUncertainty(double u): u < UNCERTAINTY_MIN");
			u = UNCERTAINTY_MIN;
		} else {
			if (u > UNCERTAINTY_MAX) {
				// System.out.println("Error: Agent.java - setUncertainty(double u): u > UNCERTAINTY_MAX");
				u = UNCERTAINTY_MAX;
			}
		}
		this.uncertainty.add(u); 				
	} // setUncertainty	
	public int getNumberOfUncertainties() { 
		return this.uncertainty.size(); 
	} // getNumberOfUncertainties
	public void setInitialUncertainty(double u) { 
		this.uncertainty.set(0, u); 
	} // setInitialUncertainty	
	
	public double getOpinion() { 
		if (this.opinion.size() < 1) {
			// System.out.println("Error: Agent.java - getOpinion(): opinion.size() < 1");
			return 0.0;
		} else {
			return ((Double)this.opinion.get(this.opinion.size()-1)).doubleValue(); 
		}
	} // getOpinion
	public double getOpinion(int i) { 
		if (i < 0) {
			// System.out.println("Error: Agent.java - getOpinion(int i): i < 0");
			return 0.0;
		} else {
			if (this.opinion.size() <= i) {
				// System.out.println("Error: Agent.java - getOpinion(int i): opinion.size() <= i");
				return 0.0;
			} else { 
				return ((Double)this.opinion.get(i)).doubleValue(); 
			}
		}
	} // getOpinion
	public void setOpinion(double o) { 
		if (o < OPINION_MIN) {
			// System.out.println("Error: Agent.java - setOpinion(double o): o < OPINION_MIN");
			o = OPINION_MIN;
		} else {
			if (o > OPINION_MAX) {
				// System.out.println("Error: Agent.java - setOpinion(double o): o > OPINION_MAX");
				o = OPINION_MAX;
			}
		}
		this.opinion.add(o); 				
	} // setOpinion	
	public int getNumberOfOpinions() { 
		return this.opinion.size(); 
	} // getNumberOfOpinions
	public void setInitialOpinion(double u) { 
		this.opinion.set(0, u); 
	} // setInitialOpinion	
	
	public Agent getNeighbour(int i) { return (Agent)this.neighbour.get(i); }
	public void addNeighbour(Agent a) { this.neighbour.add(a); }
	public int getNumberOfNeighbours() { return this.neighbour.size(); } 
	public boolean hasNeighbour(Agent a) {
		for (int i = 0; i < this.neighbour.size(); i++) 
			if (((Agent)this.neighbour.get(i)).getID() == a.getID())
				return true;
		return false;
	}
	
	public int getID() { return this.id; }
	public void setID(int i) { this.id = i; }
	
	public boolean isActive() { return this.active; }
	public void setActive(boolean b) { this.active = b; }

	public void makeExtremist(double i) {
		this.opinion = new ArrayList();
		this.uncertainty = new ArrayList();
		this.setOpinion(i);
		this.setUncertainty(Model.E_U);
		this.setInitExtreme(true);
	}
	public boolean wasInitExtreme() { return this.initExtreme; }
	public void setInitExtreme(boolean b) { this.initExtreme = b; }
	
} // Agent