package op.Utilities;

public class CostDelegate {
	
	/**
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param multiplier 1 default, 2 decimals 10 , 3 decimals 100
	 * @param rounding 0 Truncate to multiplier decimals, 1 Round to multiplier decimals 2 Closest integer
	 * @return
	 */
	public static double euclideanCost(double x1, double y1, double x2, double y2, int multiplier, int rounding){
		double cost = Math.sqrt(Math.pow((x1 - x2), 2)	+ Math.pow((y1 - y2), 2));
		if (rounding == 0) {
			cost = Math.floor(cost*multiplier* 10.0) / 10.0;
		}else if(rounding == 1){
			cost = Math.round(cost*multiplier* 10.0) / 10.0;
		}
		
		else if(rounding == 2){
			cost = Math.round(cost*multiplier);
			
		}
		else if(rounding == 3){
			cost = Rounder.round6Dec(cost*multiplier);
			
		}
		return cost;
	}
}
