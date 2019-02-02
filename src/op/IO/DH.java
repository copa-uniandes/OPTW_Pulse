package op.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import op.Graph.FinalNode;
import op.Graph.Node;
import op.Solver.GM;
import op.Utilities.CostDelegate;
import op.Utilities.Rounder;

public class DH {

	public static final int numLabels = 10;
	public static Random r = new Random(0);
	public static final double infinity = Double.POSITIVE_INFINITY;
	
	public static String inputFile;
	

	public static int numThreads = 4;

	public static int NumArcs;
	static int LastNode;
	
	public static int[][] Arcs;
	public static double[] distList;
	public static ArrayList<Integer>[] fitNodesIn_ij;
	public static ArrayList<Double>[] latestArrival;
	public static double[] timeList;
	public static double[] scoreList;
	public static double[][] t_ij;
	public static int[][] arcExists;
	//static ArrayList<EdgePulse> Arcs;
	//static double speed = 1.0;
	
	
	/**
	 * Number of nodes
	 */
	public static int n;
	
	/**
	 * Time constraint
	 */
	public static double tMax; 


	public static int source; 
	public static int sink; 
	

	
	public static double[] score;// score
	public static double[] minArcVal;
	public static int[] service;//service duration
	public static double[] tw_a;//time window begin
	public static double[] tw_b;//time window end
	
	public static double[] x; 
	public static double[] y;
	
	
	private Settings configFile;
	
	
	private GM G;
	
	
	public DH(Settings instances) {
		configFile = instances;
		inputFile = instances.DataFile;
		n = instances.NumNodes;
		source = instances.Source;
		sink = instances.LastNode;
		
	}
	
	
	
	
	double[] duales;
	private void genDua2() {
		duales = new double[n];
		for (int i = 0; i < n; i++) {
			duales[i] = 0;//r.nextDouble()*30;
		}
		duales[source]= 0;
		duales[sink]= 0;

	}
	
	/**
	 * Read a op instance
	 * Several reader are available depending on intance type
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void readInstance() throws NumberFormatException, IOException {
		
		if (configFile.reader.equals("cmt")
				|| configFile.reader.equals("solomon")
				|| configFile.reader.equals("cordeau")) {
			n = n + 1;
		}
		//genDual();
		tMax = configFile.tMAX;
		x = new double[n];
		y = new double[n];
		score = new double[n];
		service = new int[n];
		minArcVal = new double[n];
		for (int i = 0; i < minArcVal.length; i++) {
			minArcVal[i] = Double.POSITIVE_INFINITY;
		}
		tw_a = new double[n];
		tw_b = new double[n];

		File file = new File(inputFile);
		BufferedReader bufRdr = new BufferedReader(new FileReader(file));

		G = new GM(n);
		String reader = configFile.reader;
		int multiplier = 1;
		int roundingStyle = 0;
		if (reader.equals("cmt")) {
			readCMT(bufRdr);
			multiplier = 1;
			roundingStyle = 1;
		} else if (reader.equals("op")) {
			readOP(bufRdr);
			multiplier = 1;
			roundingStyle = 1;
		} else if (reader.equals("tp")) {
			readOP(bufRdr);
			multiplier = 100;
			roundingStyle = 2;
		} else if (reader.equals("solomon")) {
			readSolomon(bufRdr);
			multiplier = 1;
			roundingStyle = 0;
		}  else if (reader.equals("cordeau")) {
			readCordeau(bufRdr);
			multiplier = 1;
			roundingStyle = 3;
		} else {
			System.err.println("Invalid format");
		}

		int arcos = (n) * (n) - (n);
		
		distList = new double[arcos];
		scoreList = new double[arcos];
		timeList = new double[arcos];
		fitNodesIn_ij = new ArrayList[arcos];
		latestArrival = new ArrayList[arcos];
		Arcs = new int[arcos][2];
		t_ij = new double[n][n];
		arcExists = new int [n][n];
		for (int i = 0; i < t_ij.length; i++) {
			for (int j = 0; j < t_ij[0].length; j++) {
				t_ij[i][j] = Double.POSITIVE_INFINITY;
				if (i != j && i != sink && j != source && !(i == source && j == sink)) {
					double d_ij = CostDelegate.euclideanCost(x[i], y[i], x[j], y[j], multiplier, roundingStyle);
					t_ij[i][j] = d_ij + service[i];
				} else if (i == j) {
					t_ij[i][j] = 0;
				}
			}
		}
		
		int arc = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if(i!=j && i!= sink && j != source){
					double d_ij = CostDelegate.euclideanCost(x[i], y[i], x[j], y[j], multiplier, roundingStyle);
					t_ij[i][j] = d_ij + service[i];
					double viaje = t_ij[source][i] + t_ij[i][j] + t_ij[j][sink];
					if ((i == source && (i != j)) || ((i != j) && tw_a[i] + service[i] + d_ij <= tw_b[j])) {
						boolean nodeTest = false;
						fitNodesIn_ij[arc] = new ArrayList<Integer>();
						latestArrival[arc] = new ArrayList<Double>();
						for (int k = 0; k < n; k++) {
							if (k != sink && k != source && k != j && k != i) {
								boolean added = false;
								double latest = Math.min(tw_b[k],Rounder.round6Dec(tw_a[j]- t_ij[k][j]));
								if (latest >= tw_a[k]) {
									latest = Math.min(tw_b[i], latest - t_ij[i][k]);
									if (latest >= tw_a[i]) {
										fitNodesIn_ij[arc].add(k);
										String key = "A" + arc + "N" + k;
										latestArrival[arc].add(latest);
										added = true;
									}
								}
								double arrival_node = tw_b[i] + DH.t_ij[i][k];
								if (arrival_node <= DH.tw_b[k]) {
									arrival_node = Math.max(arrival_node, DH.tw_a[k]);
									if (arrival_node + DH.t_ij[k][j] <= DH.tw_a[j]) {
//										if (added) {
//											fitNodesIn_ij[arc].add(k);
//										}
//										
										//nodeTest = true;
									}
								}
							}
						}

						if (!nodeTest && viaje <= tMax) {
							sortingArc = arc;
							Sort(fitNodesIn_ij[arc]);
							// System.out.println(fitNodesIn_ij[arc]);
							// System.out.println("entra: " + i +" " + j);
							arcExists[i][j] = 1;
							distList[arc] = d_ij;
							Arcs[arc][0] = i;
							Arcs[arc][1] = j;
							timeList[arc] = d_ij + service[i];
							t_ij[i][j] = d_ij + service[i];
							
							if (d_ij < minArcVal[j]) {
								minArcVal[j] = d_ij;
							}
							scoreList[arc] = score[j];
							int a1 = arc;
							G.customers[i].magicIndex.add(a1);
							if (j == sink) {
								G.customers[i].arcToDepot = a1;
							}

							// System.out.println(fitNodesIn_ij[arc].size());
							arc++;
						}
					}
				}

			}
		}

		NumArcs =arc;

		
	}

	private void readCordeau(BufferedReader bufRdr) throws IOException {
		String line = null; //READ Num Nodes
		StringTokenizer t = null;
		
		String[] rowData = new String[50];
		int indexCosa = 0;
		int custumerNumber = 0;
	
		while (custumerNumber<n-1) {
			indexCosa=0;
			rowData= new String[50];
			line = bufRdr.readLine();
			t = new StringTokenizer(line, " ");
			while (t.hasMoreTokens()) {
				rowData[indexCosa] = t.nextToken();
				indexCosa++;
			}
			x[custumerNumber] = Double.parseDouble(rowData[1]);
			y[custumerNumber] = Double.parseDouble(rowData[2]);
			service[custumerNumber] = (int) (Double.parseDouble(rowData[3]));
			score[custumerNumber] = (int) (Double.parseDouble(rowData[4]));
			tw_a[custumerNumber] = (int) (Double.parseDouble(rowData[indexCosa-2]));
			tw_b[custumerNumber] = (int) (Double.parseDouble(rowData[indexCosa-1]));
			G.addVertex(new Node(custumerNumber,score[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tw_b[custumerNumber]));
			if (custumerNumber == source) {
				G.addVertex(new FinalNode(sink,score[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tw_b[custumerNumber]));
				x[sink]=x[custumerNumber];
				y[sink]=y[custumerNumber];
				score[sink]=score[custumerNumber];
				tw_a[sink]=tw_a[custumerNumber];
				tw_b[sink] = tw_b[custumerNumber];
			}
			custumerNumber++;
		}
		
		
	}



	private void readSolomon(BufferedReader bufRdr) throws IOException {
		
		String line = null; //READ Num Nodes
		StringTokenizer t = null;
		
		String[] rowData = new String[7];
		int indexCosa = 0;
		
		
		int custumerNumber = 0;
	
		while (custumerNumber<n-1) {
			indexCosa=0;
			rowData= new String[7];
			line = bufRdr.readLine();
			t = new StringTokenizer(line, " ");
			while (t.hasMoreTokens()) {
				rowData[indexCosa] = t.nextToken();
				indexCosa++;
			}
			x[custumerNumber] = Double.parseDouble(rowData[1]);
			y[custumerNumber] = Double.parseDouble(rowData[2]);
			service[custumerNumber] = (int) (Double.parseDouble(rowData[6]));
			score[custumerNumber] = (int) (Double.parseDouble(rowData[3]));
			tw_a[custumerNumber] = (int) (Double.parseDouble(rowData[4]));
			tw_b[custumerNumber] = (int) (Double.parseDouble(rowData[5]));
//			if(custumerNumber!=0 ){
//				if(r.nextDouble()<0.5){
//					double newA = (0.3*tw_b[custumerNumber]+0.7*tw_a[custumerNumber]);
//					tw_a[custumerNumber] = newA;
//				}else{
//					double newA = (0.4*tw_b[custumerNumber]-0.6*tw_a[custumerNumber])/2.0;
//					tw_b[custumerNumber] = newA;
//				}
//			}
			G.addVertex(new Node(custumerNumber,score[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tw_b[custumerNumber]));
			if (custumerNumber == source) {
				G.addVertex(new FinalNode(sink,score[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tw_b[custumerNumber]));
				x[sink]=x[custumerNumber];
				y[sink]=y[custumerNumber];

				score[sink]=score[custumerNumber];
				tw_a[sink]=tw_a[custumerNumber];
				tw_b[sink] = tw_b[custumerNumber];
			}
			custumerNumber++;
		}
		
	}




	private void readOP(BufferedReader bufRdr) throws IOException {
		if(configFile.Source == configFile.LastNode){
			System.err.println("OP reader dont read VRP/TSP intances");
		}
		String line = bufRdr.readLine();
		//line = bufRdr.readLine();
		StringTokenizer t = new StringTokenizer(line, "\t");
		
		String[] strRead = null;
		int indexCosa = 0;
		int custumerNumber = 0;
		
		while (custumerNumber<n) {
			indexCosa=0;
			strRead= new String[10];
			line = bufRdr.readLine();
			t = new StringTokenizer(line, "\t");
			while (t.hasMoreTokens()) {
				strRead[indexCosa] = t.nextToken();
				indexCosa++;
			}
			x[custumerNumber] = Double.parseDouble(strRead[0]);
			y[custumerNumber] = Double.parseDouble(strRead[1]);

			score[custumerNumber] = Double.parseDouble(strRead[2]);
			tw_a[custumerNumber]= 0;
			tw_b[custumerNumber]= tMax;
			if(custumerNumber==sink){
				G.addVertex(new FinalNode(custumerNumber,score[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tMax));
				
			}else{
				G.addVertex(new Node(custumerNumber,score[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tw_b[custumerNumber]));
			}
			custumerNumber++;
		}
	}




	private void readCMT(BufferedReader bufRdr) throws IOException {
		if(configFile.Source != configFile.LastNode){
			System.err.println("CMT reader dont read op intances");
		}
		String line = bufRdr.readLine();
		//line = bufRdr.readLine();
		StringTokenizer t = new StringTokenizer(line, " ");
		
		String[] strRead = null;
		int indexCosa = 0;
		int custumerNumber = 0;
		
		while (custumerNumber<n-1) {
			indexCosa=0;
			strRead= new String[10];
			line = bufRdr.readLine();
			t = new StringTokenizer(line, " ");
			while (t.hasMoreTokens()) {
				strRead[indexCosa] = t.nextToken();
				indexCosa++;
			}
			x[custumerNumber] = Double.parseDouble(strRead[0]);
			y[custumerNumber] = Double.parseDouble(strRead[1]);
			if(strRead[2]!=null)
				score[custumerNumber] = Double.parseDouble(strRead[2]);
			
			tw_a[custumerNumber]= 0;
			tw_b[custumerNumber]= tMax;
			if(custumerNumber==source){
				G.addVertex(new Node(custumerNumber,score[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tw_b[custumerNumber]));
				G.addVertex(new FinalNode(sink,score[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tw_b[custumerNumber]));
				x[sink]=x[custumerNumber];
				y[sink]=y[custumerNumber];
				score[sink]=score[custumerNumber];
				tw_a[sink]=tw_a[custumerNumber];
				tw_b[sink]=tw_b[custumerNumber];
			}else{
				G.addVertex(new Node(custumerNumber,score[custumerNumber],service[custumerNumber], tw_a[custumerNumber],tw_b[custumerNumber]));
			}
			custumerNumber++;
		}
		
	}




	public static void calDualBound() {
		GM.DualBound = 0;
		for (int i = 0; i < NumArcs; i++) {
			if (timeList[i] != 0 && scoreList[i] / timeList[i] >= GM.DualBound) {
				GM.DualBound = scoreList[i] / timeList[i];
			}
		}
	}


	int sortingArc = -1;
	public GM getGraph() {
		return G;
	}
	
	private void Sort(ArrayList<Integer> set) {
		QS(set, 0, set.size() - 1);
	}

	public int colocar(ArrayList<Integer> e, int b, int t) {
		int i;
		int pivote;
		double valor_pivote;
		int temp;
		double temp2;

		pivote = b;
		//valor_pivote = DataHandler.pi[e[pivote].id] ;
		
		valor_pivote = latestArrival[sortingArc].get(pivote) ;
		for (i = b + 1; i <= t; i++) {
			
			if (  latestArrival[sortingArc].get(i) > valor_pivote) {
				pivote++;
				temp = e.get(i);
				e.set(i, e.get(pivote));
				e.set(pivote,temp);
				temp2 = latestArrival[sortingArc].get(i);
				latestArrival[sortingArc].set(i, latestArrival[sortingArc].get(pivote));
				latestArrival[sortingArc].set(pivote,temp2);
			}
		}
		temp =  e.get(b);
		e.set(b, e.get(pivote));
		e.set(pivote,temp);
		temp2 = latestArrival[sortingArc].get(b);
		latestArrival[sortingArc].set(b, latestArrival[sortingArc].get(pivote));
		latestArrival[sortingArc].set(pivote,temp2);
		
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
}
