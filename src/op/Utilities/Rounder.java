package op.Utilities;

public class Rounder {
	public static final double deviation = 0.0000000000;
	
	
	public static double  round6Dec( double rounded) {
		return (Math.round(rounded*1000000)/1000000.0);
		//return rounded;
	}
}
