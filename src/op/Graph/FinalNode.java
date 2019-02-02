package op.Graph;

import java.util.ArrayList;

import op.IO.DH;
import op.Solver.AH;
import op.Solver.GM;



public class FinalNode extends Node {
	
	
	
	public int id;
	
	public double score; //Demand
	public int service;//Service Time
	public double tw_a;//Beginning of the TW
	public double tw_b;//End of the TW
	public double tw_w;//time window width
	public ArrayList<Integer> magicIndex;
	
	
	
	//Variables Pulso
	public ArrayList Path;
	public double PathTime;
	public double PathLoad;
	public double PathCost;
	double PathDist;

	
	public FinalNode(int i, double d, int s , double a, double b)  {
		super(i, 0, 0, a, b);
		id = i;
		score = d;
		service = s;
		tw_a = a;
		tw_b = b;	
		tw_w = b-a;
		magicIndex = new ArrayList<>();
		Path= new ArrayList();
		
	}
	
		/**
		 * Over ride pulse method for the final node.
		 */
		public synchronized void pulse(double PTime, double PCost, int[] path, int pLength, 	double PDist, int thread) {
			AH.ArrivingPaths++;
			if (PTime <= tw_b) {
				if (PCost > GM.PrimalBound) {
					GM.PrimalBound = PCost;
					this.PathTime = PTime;
					this.PathCost = PCost;
					this.PathDist = PDist;
					this.Path.clear();
					for (int i = 0; i <pLength; i++) {
						this.Path.add(path[i]);
					}
					this.Path.add(id);
//					System.out.println("llego con score" + PCost + " tiempo " + PTime + "   pLen: "+ pLength +"/EXEtime:/" +(System.currentTimeMillis()-AH.tnow)/1000.0 );
				}
			}
	
		}




	/**
	 * Over ride pulseBound method for the FinalNode
	 */
	public void pulseBound(double PTime, double PCost,  int[] path, int pathLength, int Root, double PDist) {
		if ( (PTime) <= tw_b) {
			if (PCost > GM.OraclePrimalBound[Root]) {
				GM.OraclePrimalBound[Root] = PCost;
//				if (PCost > GM.PrimalBound) {
//					GM.PrimalBound = PCost;
//				}
			}	
		}
	}

	
	public String toString(){
			return id+ "";
	}
		
	private void SortF(ArrayList<Double> set) {
		QSF(set, 0, set.size() - 1);
	}

	public int colocarF(ArrayList<Double> e, int b, int t) {
		int i;
		int pivote;
		double valor_pivote;
		double temp;

		pivote = b;
		//valor_pivote = DataHandler.pi[e[pivote].id] ;
		valor_pivote = e.get(pivote) ;
		for (i = b + 1; i <= t; i++) {
			if (  e.get(i) < valor_pivote) {
				pivote++;
				temp = e.get(i);
				e.set(i, e.get(pivote));
				e.set(pivote,temp);
			}
		}
		temp =  e.get(b);
		e.set(b, e.get(pivote));
		e.set(pivote,temp);
		return pivote;
	}

	public void QSF(ArrayList<Double> e, int b, int t) {
		int pivote;
		if (b < t) {
			pivote = colocarF(e, b, t);
			QSF(e, b, pivote - 1);
			QSF(e, pivote + 1, t);
		}
	}


}

