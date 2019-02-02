package op.Solver;

import java.util.ArrayList;

import op.Graph.*;
import op.IO.DH;
import op.Utilities.Rounder;


/**
 * This class contains the structures for the ESPPRC heuristic
 * 
 * @author Daniel
 * 
 */
public class GM {

	
	public static Node[] customers;

	public static double boundStep =5;
	public static double minBarrier;

	public static int[][] visited;
	
	public static double PrimalBound;
	public static double DualBound;

	public static double Barrier;

	public static double[][] OracleBound;
	public static double[] boundingVector;
	public static double vectorPrimalBound;
	public static double[] OraclePrimalBound;
	public static double OracleBestBound;

	public static FinalNode finalNode;

	public static double maxCost = 0;
	public static double minCost = Double.POSITIVE_INFINITY;
	public static double maxTWA = 0;
	public static double minTWA = Double.POSITIVE_INFINITY;
	public static double maxLoad = 0;
	public static double minLoad = Double.POSITIVE_INFINITY;

	private int numNodes;
	private int Cd;
	private int Ct;

	public static boolean phaseII = false;

	

	
	public GM(int numNodes) {
		
		this.numNodes = numNodes;
		// nodeList = new Hashtable<Integer, VertexPulse>(numNodes);
		Cd = 0;
		Ct = 0;
		customers = new Node[numNodes];
	
		visited = new int[numNodes][DH.numThreads+1];

		PrimalBound = 0;
		boundingVector = new double[5001];
		OracleBound = new double[numNodes][5001];
		OraclePrimalBound = new double[numNodes];
		OracleBestBound = 0;
		for (int i = 1; i < numNodes; i++) {
			OraclePrimalBound[i] = 0;
		}

		

		// routeSize = new int[DataHandler.k];

	}

	public int getNumNodes() {
		return numNodes;
	}

	public boolean addVertex(Node v) {
		customers[v.getID()] = v;
		if (v.id != 0) {
			if (v.tw_a < minTWA) {
				minTWA = v.tw_a;
			}
			if (v.tw_a > maxTWA) {
				maxTWA = v.tw_a;
			}
			if (v.score < minLoad) {
				minLoad = v.score;
			}
			if (v.score > maxLoad) {
				maxLoad = v.score;
			}
		}
		return true;
	}
	
	public void setRound2(double pbound){
		
		PrimalBound = pbound;

		OracleBound = new double[numNodes][501];
		OraclePrimalBound = new double[numNodes];
		OracleBestBound = pbound;
		for (int i = 1; i < numNodes; i++) {
			OraclePrimalBound[i] = 0;
		}
		phaseII =true;
	}

	public Node[] getCustomers() {
		return customers;
	}

	
}
