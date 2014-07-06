package featureSelection;

import java.util.ArrayList;

public class IWDobjects {
	//each iwd has velocity, soilcontent,visitedNoseList
	double velocity;
	double soilContent;
	double cva;
	ArrayList<Integer> visitedPathList = new ArrayList<Integer>();
	public IWDobjects(double velocity, double soilContent,
			ArrayList<Integer> visitedPathList,double cva) {
		this.velocity = velocity;
		this.soilContent = soilContent;
		this.visitedPathList = visitedPathList;
		this.cva= cva;
	}
	
	
	

}
