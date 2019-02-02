package op.Graph;


import java.util.ArrayList;
import java.util.Random;



import op.IO.DH;
import op.Solver.AH;
import op.Solver.GM;
import op.Solver.PulseTask;
import op.Utilities.Rounder;

public class Node implements Cloneable{
	
	
	
	public int id;
	
	public double score; //Demand
	public int service;//Service Time
	public double tw_a;//Beginning of the TW
	public double tw_b;//End of the TW
	public double tw_w;//time window width
	public ArrayList<Integer> magicIndex;
	
	//Variables Pulso
	boolean FirstTime=true;
	public int arcToDepot;
//	int maxLabels = 20;
//	int numLabels = 0;
//	public Label[] labels= new Label[maxLabels];
//	
//	public ArrayList<Integer> unreachable;
	/**
	 * Constructor of a node
	 * @param i id
	 * @param d	score
	 * @param s service time
	 * @param a TW begining
	 * @param b TW end
	 */
	public Node(int i, double d, int s , double a, double b) {
		id = i;
		score = d;
		service = s;
		tw_a = a;
		tw_b = b;	
		tw_w = b-a;
		magicIndex = new ArrayList<>();
//		labels = new Label[maxLabels];
//		unreachable = new ArrayList<Integer>();
		//
	}
	
	//3114071968
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////7
	/**
	 * Pulse function for the OPTW. If the current node is the depot, multiple threads are launched.
	 * If not, the propagation follows the recursive method. 
	 * @param pTime
	 * @param pScore
	 * @param path
	 * @param pathLength
	 * @param pDist
	 * @param thread The thead number in with the pulse is traveling.
	 * @throws InterruptedException
	 */
	public void pulse(double pTime, double pScore, int[] path, int pathLength, double pDist, int thread) throws InterruptedException {
		if (this.FirstTime == true) {
			this.FirstTime = false;
			this.Sort(this.magicIndex);
		}
		if (pTime < this.tw_a) {
			pTime = this.tw_a;
		}
		if(System.currentTimeMillis()<=AH.cpuTimeLimit){
			if ((GM.visited[id][thread] == 0 && (pScore + boundForPulse(pTime)) > GM.PrimalBound)) {
				GM.visited[id][thread] = 1;
				path[pathLength] = id;
				pathLength++;
				if (!checkTwoOptImprovement(pTime, path, pathLength)) {
					for (int i = 0; i < magicIndex.size(); i++) {
	
						double newPTime = 0;
						double newPScore = 0;
						double newPDist = 0;
						int Head = DH.Arcs[magicIndex.get(i)][1];
	
						newPTime = Rounder.round6Dec(pTime + DH.timeList[magicIndex.get(i)]);
						newPScore = (pScore + DH.scoreList[magicIndex.get(i)]);
						newPDist = (pDist + DH.distList[magicIndex.get(i)]);
						int arcToDepot = GM.customers[Head].arcToDepot;
						double timeHead_Deapot = Head == DH.sink ? 0 : DH.timeList[arcToDepot];
						if (newPTime <= GM.customers[Head].tw_b && newPTime + timeHead_Deapot <= DH.tMax ) {
	
							if (id != DH.source) {
								GM.customers[Head].pulse(newPTime, newPScore, path, pathLength, newPDist, thread);
							}
							else{
								if(checkNodesIn_ij(magicIndex.get(i), pTime, id, Head,thread,pathLength)==false){	
									boolean stopLooking = false;
									for (int j = 1; j < AH.threads.length; j++) {
										if(!AH.threads[j].isAlive()){
											AH.threads[j] = new Thread(new PulseTask(Head, newPTime, newPScore, path, pathLength, newPDist, j));
											AH.threads[j].start();
											stopLooking = true;
											j = 1000;
										}
									}
									if (!stopLooking) {
										AH.threads[1].join();
										AH.threads[1] = new Thread(new PulseTask(Head, newPTime, newPScore, path, pathLength, newPDist, 1));
										AH.threads[1].start();
									}
								}
							}
						}
					}
					if(id == DH.source){
						for (int i = 1; i < AH.threads.length; i++) {
							AH.threads[i].join();
						}
					}
		
				}
				pathLength--;
				path[pathLength] = -1;
				GM.visited[id][thread] = 0;

			}
		}else{
			AH.optimality = false;
		}
	}


	/**
	 * Checks if given an arrival time to the tail node of arc, it is possible to visit another node an reach head node of arc on time( before tw_a of head) 
	 * @param arc evaluated arc
	 * @param pTime arrival time to tail node of arc
	 * @return true if a node fits (prune), false other wise.
	 */
	private boolean checkNodesIn_ij(int arc, double pTime, int v_i, int v_j, int thread, int pLen) {
				
		for (int i = 0; i < DH.fitNodesIn_ij[arc].size(); i++) {
			double lastest = DH.latestArrival[arc].get(i);
			if(pTime<=lastest){
				int node = DH.fitNodesIn_ij[arc].get(i);
				
				if(GM.visited[node][thread]==0){
//					if (thread==0) {
//						System.out.println("EN BMMMMM Podaaaaaaaaaaaaaaaaa "+v_i+" a "+v_j+" pLen: "+pLen);
//							
//					}
//					System.out.println("EN BMMMMM Podaaaaaaaaaaaaaaaaa "+v_i+" a "+v_j+" pLen: "+pLen);
					return true;
				}//else{
				//	System.out.println("pudo haber podado pero ya lo hab�a visitado");
				//}
			}else{
				return false;
			}
		}
		return false;
	}

	/**
	 * This method aims to prune pulse that have a path suitable for a profitablle and feasible 2 opt Swap.
	 * The method can check all swaps between the last added node and the path or just the two last nodes added.
	 * In the paper this is called soft dominance
	 * @param pTime Time of the path
	 * @param path current path
	 * @param pLength current path length
	 * @return true if the is a better permutation with the same node, false other wise.
	 */
	private boolean checkTwoOptImprovement(double pTime, int[] path, int pLength){ 
		if(pLength<4){
			return false;
		}
		boolean breakTwoOpt = false;//1Math.max(1, pLength-3)
		for (int i = Math.max(1, pLength-3); i < pLength-1  && !breakTwoOpt; i++) {
			for (int j = Math.max(i+1, pLength-2); j < pLength-1  && !breakTwoOpt; j++) {
				if (checkFeasibility(path, i, j, pLength, pTime)) {
					breakTwoOpt = true;
				}
			}
		}
		return breakTwoOpt;
	}


	public  static boolean  checkFeasibility(int[] path, int i, int j, int pLength, double oldTime) {
		
		int[] copyPath = path.clone();
		copyPath[i] = path[j];
		copyPath[j] = path[i];
		double cumTime = 0.0;
		
		for (int k = 1; k <pLength; k++) {
			cumTime =  Math.max(DH.tw_a[copyPath[k]], Rounder.round6Dec(cumTime + DH.t_ij[copyPath[k-1]][copyPath[k]]));
			if(cumTime> DH.tw_b[copyPath[k]]){ 
				return false;
			} if(DH.arcExists[copyPath[k-1]][copyPath[k]]==0){
				return false;
			}
		}
		if(oldTime<=cumTime){
			return false;
		}
		return true;
	}

	/**
	 * Same as checkTowOptImprovement but for the BM
	 * the difference is the starting time at the feasibility check
	 * @param pTime Time of the path
	 * @param path current path
	 * @param pLength current path length
	 * @return true if there is a better permutation with the same node, false other wise.
	 */
	private boolean checkTwoOptImprovement2(double pTime, int[] path, int pLength){ 
		if(pLength<4){
			return false;
		}
		boolean breakTwoOpt = false;//1Math.max(1, pLength-3)  Math.max(i+1, pLength-2)
		for (int i = Math.max(1, pLength-3); i < pLength-1  && !breakTwoOpt; i++) {
			for (int j =  Math.max(i+1, pLength-2); j < pLength-1  && !breakTwoOpt; j++) {
				if (checkFeasibility2(path, i, j, pLength, pTime)) {
					breakTwoOpt = true;
				}
			}
		}
	if(breakTwoOpt){
		return true;
	}

	return false;		
			
			
			
	}


	public  static boolean  checkFeasibility2(int[] path, int i, int j, int pLength, double oldTime) {
		int[] copyPath = path.clone();
		copyPath[i] = path[j];
		copyPath[j] = path[i];
		double cumTime = Math.max(DH.tw_a[copyPath[0]], GM.Barrier);
		
		for (int k = 1; k <pLength; k++) {
			cumTime =  Math.max(DH.tw_a[copyPath[k]], Rounder.round6Dec(cumTime + DH.t_ij[copyPath[k-1]][copyPath[k]]));
			if(cumTime> DH.tw_b[copyPath[k]]){ 
				return false;
			} if(DH.arcExists[copyPath[k-1]][copyPath[k]]==0){
				return false;
			}
		}
		if(oldTime<=cumTime){
			return false;
		}
		return true;
	}

	private double boundForPulse(double time) {
		double Bound = 0;
		if (time < GM.Barrier) {
			Bound = Double.POSITIVE_INFINITY;
		} else {
			int Index = calculateIndex(time , GM.boundStep);
			Bound = GM.OracleBound[this.id][Index];
		}
		return Bound;
	}


	public void pulseBound(double pTime, double pScore, int[] path, int pathLength, int Root, double pulseDist) {
		if(this.FirstTime==true){
			this.FirstTime=false;
			this.Sort(this.magicIndex);
		}
		if(pTime<this.tw_a){
			pTime=this.tw_a;
		}	
		if(System.currentTimeMillis()<=AH.cpuTimeLimit){
			if(GM.visited[id][0]==0 && pTime < tw_b && (pScore+boundForBounding(pTime, Root))>GM.OraclePrimalBound[Root]){
				GM.visited[id][0] = 1;
				path[pathLength] = id;
				pathLength++;
				if (!checkTwoOptImprovement2(pTime, path, pathLength)) {
					for (int i = 0; i < magicIndex.size(); i++) {
						double NewPTime = 0;
						double nPulseScore = 0;
						double NewPDist = 0;
						int Head = DH.Arcs[magicIndex.get(i)][1];
						NewPTime=Rounder.round6Dec(pTime+DH.timeList[magicIndex.get(i)]);
						//NewPTime=PTime+DataHandler.timeList[magicIndex.get(i)];
						nPulseScore = (pScore + DH.scoreList[magicIndex.get(i)]);
						NewPDist = (pulseDist + DH.distList[magicIndex.get(i)]);
						
						int arcToDepot = GM.customers[Head].arcToDepot;
						double timeHead_Deapot = Head==DH.sink?0:DH.timeList[arcToDepot]; 
						if (NewPTime <= GM.customers[Head].tw_b  && NewPTime +timeHead_Deapot  <= DH.tMax ) {
	//					if(checkNodesIn_ij(magicIndex.get(i), pTime, id, Head,0,pathLength)==false){
								GM.customers[Head].pulseBound(NewPTime, nPulseScore, path, pathLength, Root, NewPDist);
	//						}
						}
					}
				}
				pathLength--;
				path[pathLength] = -1;
				GM.visited[id][0] = 0;
			}
		}else{
			AH.optimality = false;
		}
	}

	private double boundForBounding(double time, int root) {
	
		double Bound = 0;
		//&& id>= root
		if (time < GM.Barrier + GM.boundStep ) {
		//	int Index = calculateIndex(time , GraphManager.boundStep);
		//	Bound = GraphManager.OracleBound[this.id][Index];
			Bound = Double.POSITIVE_INFINITY;
//			Bound = 100;
		} else {
			int Index = calculateIndex(time , GM.boundStep);
			Bound =  GM.OracleBound[this.id][Index];
		}
		return Bound;
	}


	/**
	 * 
	 * @param pTime
	 * @param pScore
	 * @param path
	 * @param pathLength
	 * @return true if the incoming path is dominated, false other wise
	 */
//	private boolean checkLabels(double pTime, double pScore, int[] path,int pathLength, int[] visited) {
//		
//		int[] nVisited = visited.clone();
//		nVisited[id] = 1;
//		
//		for (int j = 0; j < DH.n; j++) {
//			double timeProof_j = Math.max(DH.tw_a[j], Rounder.round6Dec(pTime + DH.t_ij[id][j]));
//			double timeProof_depot = Rounder.round6Dec(timeProof_j + DH.t_ij[j][DH.sink]);
//			if (timeProof_j > DH.tw_b[j] || timeProof_depot > DH.tw_b[DH.sink]) {
//				nVisited[j] = 1;
//				
//			}
//		}
//
//		for (int i = 0; i < numLabels; i++) {
//			Label l = labels[i];
//			if (pScore <= l.score && pTime >= l.time && pathLength >= l.pLength) {
//				boolean stop = false;
//				for (int k = 0; k < l.pLength && !stop; k++) {
//					if (nVisited[l.path[k]] == 0) {
//						stop = true;
//					}
//				}
//				if (stop == false) {
//					return true;
//				}
//
//			}
///*
//			else if (pScore >= l.score && pTime <= l.time
//					&& pathLength <= l.pLength) {
//				boolean stop = false;
//				for (int k = 0; k < pathLength && !stop; k++) {
//					if (l.visited[path[k]] == 0) {
//						stop = true;
//					}
//				}
//				if (stop == false) {
//					labels.remove(i);
//					i--;
//				}
//			}*/
//		}
//		
//		
//		return false;
//	}
//	


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////7
	
	/**********************************************************************************************************************************************************************/
	/**********************************************************************************************************************************************************************/
	/**********************************************************************************************************************************************************************/
	/**********************************************************************************************************************************************************************/


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////7

	private int calculateIndex(double time, double boundStep) {
		if (AH.decimal == 0) {
			return ((int) Math.floor(time / GM.boundStep));
		}
		double decimalPart = (time / GM.boundStep) - Math.floor(time / GM.boundStep);
		int index = ((int) Math.floor(time / GM.boundStep));
		index = decimalPart>=AH.decimal && decimalPart<1?index+1:index;
		return index;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////7


	public int  getID()
	{
		return id;
	}
	

	public String toString(){
		
		return id+"";
		}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	public void autoSort(){
		Sort(this.magicIndex);
	}
	private void Sort(ArrayList<Integer> set) {
		QS(set, 0, set.size() - 1);
	}

	public int colocar(ArrayList<Integer> e, int b, int t) {
		int i;
		int pivote;
		double valor_pivote;
		int temp;

		pivote = b;
		//valor_pivote = DataHandler.pi[e[pivote].id] ;
		valor_pivote = DH.scoreList[e.get(pivote)]/DH.distList[e.get(pivote)] ;
		for (i = b + 1; i <= t; i++) {
			if ( DH.scoreList[e.get(i)]/DH.distList[e.get(i)]> valor_pivote) {
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

	public void QS(ArrayList<Integer> e, int b, int t) {
		int pivote;
		if (b < t) {
			pivote = colocar(e, b, t);
			QS(e, b, pivote - 1);
			QS(e, pivote + 1, t);
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////7




}

