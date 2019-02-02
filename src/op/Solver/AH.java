package op.Solver;

import java.util.ArrayList;
import op.IO.DH;
import op.Utilities.Rounder;

/**
 * Main Logic of the algorithm
 * @author Daniel Duque, Leonardo Lozano
 *
 */
public class AH {
		
	
	public static int ArrivingPaths ;
	public static int unkwonBound = 100;
	public static double alpha = 0.5;
	public static double decimal = 0.0;
	public static long tnow;
	public static long cpuTimeLimit;
	public static long cpuBudget;
	public static boolean optimality;
	private DH data;
	
	public static Thread[] threads ;
	
	/**
	 * Constructor of the algorithm handler. All variables are initialized
	 */
	public AH(DH d) {
		tnow = System.currentTimeMillis();
		optimality = true;
		cpuBudget = 3600000;
		cpuTimeLimit = tnow +cpuBudget ;
		data = d;
		threads = new Thread[DH.numThreads+1];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread();
		}
		
	}


	public void runPulse(double shift) throws InterruptedException{
		
		
		DH.calDualBound();
		int[] pathBM = new int[DH.n];
		for (int i = 0; i < pathBM.length; i++) {
			pathBM[i] = -1;
		}
		//setvisit();
		GM.Barrier = DH.tMax;
		GM.boundStep = (int) (10);//DataHandler.tMax*0.01);
		decimal = Rounder.round6Dec((GM.Barrier/GM.boundStep)-Math.floor(GM.Barrier/GM.boundStep));
		ArrivingPaths = 0;
		int Index=0;
//		System.out.println("vamos hasta "+ DH.tMax*0.2);
		while(GM.Barrier>=DH.tMax*0.2){
		Index=(int) Math.ceil((GM.Barrier/GM.boundStep));	
			for (int i = 0; i < DH.n; i++) {
				if(i!= DH.source && i!= DH.sink){
					GM.customers[i].pulseBound(GM.Barrier, 0 , pathBM, 0 , i ,0);
					GM.OracleBound[i][Index]=GM.OraclePrimalBound[i];
				}
			}
			
//			System.out.println("BARRIER: "+GM.Barrier + " Recurso: "+ (DH.tMax-GM.Barrier )+" Index: "+Index + "  EXE time " + (System.currentTimeMillis()-tnow)/1000.0);
			
			
			GM.OracleBestBound=GM.PrimalBound;
			GM.Barrier-=GM.boundStep;
		}
//		GM.OracleBound[DH.source][0] = Double.POSITIVE_INFINITY;
//		GM.OracleBound[DH.source][1] = Double.POSITIVE_INFINITY;
//		GM.OracleBound[DH.source][2] = Double.POSITIVE_INFINITY;
		
		GM.Barrier+=GM.boundStep;
		GM.PrimalBound=-1;
		
		int[] path = new int[DH.n];
		for (int i = 0; i < path.length; i++) {
			path[i] = -1;
		}

		GM.customers[0].pulse( 0.0, 0.0, path, 0, 0.0, 0);
		System.out.println("FO:"+GM.PrimalBound + "/EXEtime:/" +(System.currentTimeMillis()-tnow)/1000.0 + "/paths/"+ AH.ArrivingPaths+"/optimal/"+optimality);
	}
	
	


	
	
}
