package op.Graph;

import op.IO.DH;
import op.Utilities.Rounder;



public class Label {
	
	
	public double score;
	public double time;
	public int[] visited;
	public int[] path;
	public int pLength;
	public int end;
	public String key;
	public boolean improved = false;
	//public PendingEvent lastEvent;
	//public ArrayList<Integer> unreach;
	public Label(double nScore, double nTime, int[] nVisited, int[] nPath, int nPLength , int nEnd , String nKey) {
		score = nScore;
		time = nTime;
		visited = nVisited.clone();
		path = nPath.clone();
		end = nEnd;
		key = nKey;
		pLength = nPLength;
		//TODO Mirar lo del 2-opt aqui!
		/*if(pLength>3 && pLength<6 ){
			checkTwoOptImprovement2();
		}*/
	}
	public  boolean checkTwoOptImprovement2(){ 
	
	
		
		boolean breakTwoOpt = false;
		for (int i = 1; i < pLength-1  && !breakTwoOpt; i++) {
			for (int j =  i+1; j < pLength-1  && !breakTwoOpt; j++) {
				if ( checkFeasibility( i, j)) { 
					breakTwoOpt = true;
				}
				// }
			}
		}
	if(breakTwoOpt){
		improved = true;
		return true;
	}

	return false;		
			
			
			
	}


	public boolean  checkFeasibility( int i, int j) {
		
		int[] copyPath = path.clone();
		copyPath[i] = path[j];
		copyPath[j] = path[i];
		double cumTime = 0.0;
		
		for (int k = 1; k <pLength; k++) {
			//System.out.println("aloo:" + DH.t_ij[copyPath[k-1]][copyPath[k]]);
			cumTime =  Math.max(DH.tw_a[copyPath[k]], Rounder.round6Dec(cumTime + DH.t_ij[copyPath[k-1]][copyPath[k]]));
			if(cumTime> DH.tw_b[copyPath[k]])
			{ 
				return false;
			} 
		}
		if(time<cumTime){
			return false;
		}
		time = cumTime;
		path = copyPath;
		
		return true;
	}

	
	@Override
	public String toString() {
		return "end: "+ end + " ; " + time + " ; " + score;
	}
}