package featureSelection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainFeatureSelection {

	
	public static void main(String[] args) {
		/*File fl = new File("newcolon.csv");
		File ranking = new File("infogain");
		File chisquare= new File("chisquare");
		File cfs = new File("cfs");*/
		File fl = new File("fault/fault.csv");
		File ranking = new File("fault/fault_infogain");
		File chisquare= new File("fault/fault_chisqr");
		File cfs = new File("fault/fault_cfs");
		IWD iw = new IWD(fl);
		try {
			int numberOfFeatures=iw.readData().get(0);
			System.out.println(numberOfFeatures);
			HashMap<Integer, Double> hmInfogain=iw.readInfoGain(ranking);
			HashMap<Integer, Double> hmChiSquare=iw.readChiSquare(chisquare);
			HashMap<Integer, Double> hmCfs=iw.readCfs(cfs);
			iw.IWDalgorithm(numberOfFeatures,hmInfogain,hmChiSquare,hmCfs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
