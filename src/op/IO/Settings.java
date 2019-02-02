package op.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Settings {

	String DataFile;
	int tMAX;
	int NumNodes;
	int LastNode;
	int Source;
	String reader;
	
	public Settings(String ConfigFile) throws IOException{
		
		File file = new File(ConfigFile);
		 
		BufferedReader bufRdr  = new BufferedReader(new FileReader(file));
		String line = null;
		int lines = 6;
		String[][] readed = new String[lines][2];

		int row = 0;
		int col = 0;

		// read each line of text file
		while ((line = bufRdr.readLine()) != null && row < lines) {
			StringTokenizer st = new StringTokenizer(line, ":");
			while (st.hasMoreTokens()) {
				// get next token and store it in the array
				readed[row][col] = st.nextToken();
				col++;

			}
			col = 0;
			row++;

		}

		DataFile = readed[0][1];
		tMAX = Integer.parseInt(readed[1][1]);
		NumNodes = Integer.parseInt(readed[2][1]);
		Source = Integer.parseInt(readed[3][1]);
		LastNode=Integer.parseInt(readed[4][1]);		 
		reader = readed[5][1];
		
		
		
	}
	
	
}
