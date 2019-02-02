package op.Utilities;

import java.util.BitSet;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class BinaryOperations {
	
	
	
	public static long  toBinaryNum (int[] binaryVector){
		
//		BitSet a;
//		HexBinaryAdapter a2 ;
//		a2.
		
		
		return 1;
		
	}
	
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static void main(String[] args) {
		byte[] b = {1,0,1,0,1,0,0,0,1,1};
		
		String aa = bytesToHex(b);
		System.out.println(aa);
		
	}

}
