package op.Solver;

public class PulseTask implements Runnable{

	
	
	double pTime;
	double pScore;
	int[] path;
	int pathLength;
	double pDist;
	int thread;
	int head;
	
	public PulseTask(int nHead, double nTime, double nScore , int[] nPath, int nLength, double nDist, int nThread) {
		head = nHead;
		pTime = nTime;
		pScore = nScore;
		path = nPath.clone();
		pathLength = nLength;
		pDist = nDist;
		thread = nThread;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			GM.customers[head].pulse(pTime, pScore, path, pathLength, pDist, thread);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
