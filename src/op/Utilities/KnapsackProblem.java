package op.Utilities;

import java.util.ArrayList;

import gurobi.*;

public class KnapsackProblem {
	
	
	private GRBEnv env;
	private GRBModel model;
	
	private GRBVar[] x;
	private GRBConstr ctr;
	private double RHS;
	
	
	public KnapsackProblem(int n, double[] prize, double[] resource , double nRHS ) throws GRBException {
		env = new GRBEnv(null);
		env.set(GRB.IntParam.OutputFlag, 0);
		model = new GRBModel(env);
		x = new GRBVar[n];
		RHS = nRHS;
		createProblem(prize, resource);
	}


	private void createProblem(double[] prize, double[] resource) throws GRBException {
		//Variables
		for (int i = 0; i < x.length; i++) {
			x[i] = model.addVar(0, 1, -prize[i], GRB.BINARY, "x_"+i);
		}
		model.update();
		
		GRBLinExpr xpr = new GRBLinExpr();
		for (int i = 0; i < x.length; i++) {
			xpr.addTerm(resource[i], x[i]);
		}
		ctr = model.addConstr(xpr, GRB.LESS_EQUAL, RHS, "knapsackCtr");
		
		model.update();
	}
	
	public double solveKnapsackProblem(double nRHS) throws GRBException{
		
		ctr.set(GRB.DoubleAttr.RHS, nRHS);
		model.update();
		model.optimize();
		System.out.println(model.get(GRB.IntAttr.Status));
		double objVal = model.get(GRB.DoubleAttr.ObjVal);
		
		return objVal;
		
	}
	
	public void closeModel() throws GRBException{
		model.dispose();
		env.dispose();
	}
	
	
}
