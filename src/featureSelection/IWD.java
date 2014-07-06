package featureSelection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IWD {
	
	File fl;
	public IWD(File fl) {
		this.fl = fl;
	}
	int noIWD ;
	//int [] velocity = new  int[noIWD];
	double [][] pathsoil;
	
	
	public void IWDalgorithm(int featureNum,HashMap<Integer, Double> hmInfogain, HashMap<Integer, Double> hmChiSquare, HashMap<Integer, Double> hmCfs) throws IOException {
		//declare pathsoil matrix
		pathsoil= new double[featureNum][featureNum];
		FileWriter fw = new FileWriter("out.txt");
		//initialize pathsoil matrix
		for (int i = 0; i < pathsoil.length; i++) {
			for (int j = 0; j < pathsoil.length; j++) {
				if(i==j){
					pathsoil[i][j]=0;
				}
				else {
					pathsoil[i][j]=1000;
				}
			}
		}
		double cvaMaxTotal=0.0;
		ArrayList<Integer> globalBest;
	//for m number of generation
	for (int m = 0; m < 30; m++) {
			//create IWd
			IWDobjects iwd ;
			ArrayList<IWDobjects> iwdn= new ArrayList<IWDobjects>();

			//iterate for i iwd
		for (int i = 0; i < 50; i++) {
				//initailize iwd
				double velocity=20;
				double soilContent=0;
				double cva=0.0;
				ArrayList<Integer> temp = new ArrayList<Integer>();
				Random r = new Random();
				Integer  IWDpos= genRandomNumber(0, pathsoil.length-1, r);
				//update iwd visited node list
				temp.add(IWDpos);
				//System.out.println("Initial IWDPos="+IWDpos);	
				//find probablity of ith iwd to move from feature i to j where j is not in the visitedPathList
				//sub=subset 
				for (int sub = 0; sub < 10; sub++) {
				//move iwd from one feature to another till subset size
				Integer newPos=nextPosition(temp,velocity);
				//iwdn.get(i).visitedPathList.add(newPos);
				temp.add(newPos);
				//iwdn.get(i).visitedPathList=temp;
				//get ranking of new feature
				Double rank=hmInfogain.get(newPos+1);
				Double chi= hmChiSquare.get(newPos+1);
				Double cfs= getCfs(hmCfs, newPos+1);
				ArrayList<Double> velSC = new ArrayList<Double>();
				velSC=updateIWD(velocity,temp,soilContent,rank,chi,cfs);	
				velocity=velSC.get(0);
				soilContent=velSC.get(1);
			
				}//end for subset size
			//System.out.println("\n#############################################################");
//			for (int k = 0; k < iwdn.get(i).visitedPathList.size(); k++) {
//				System.out.print(iwdn.get(i).visitedPathList.get(k)+"-->");
//			}
			
			
			///create reduced dataset based on selected features
			reducedDataset(temp);
			//run libsvm and get CVA
			double CVA=runLibSVM();
			//iwdn.get(i).cva=CVA;
			//System.out.println(CVA);
			//update iwd objects
			//System.out.println("**********************Path soil matrix for iwd**********************");
//			for (int j = 0; j < pathsoil.length; j++) {
//				for (int j2 = 0; j2 < pathsoil.length; j2++) {
//					System.out.print(pathsoil[j][j2]+"  ");
//				}
//				System.out.println();
//				}
			iwd= new IWDobjects(velocity, soilContent, temp,CVA);
			iwdn.add(iwd);
		
			}//end of one generation
			
			
			//do best path soil update
				//select iwd having maximum cva
				double cvaMax=0;
				int bestCVAnumber=0;
				for (int i = 0; i < iwdn.size(); i++) {
					if(cvaMax<iwdn.get(i).cva){cvaMax=iwdn.get(i).cva;bestCVAnumber=i;}
					
				}
				//update pathsoil of corresponding iwd
				bestPathSoilUpdate(iwdn.get(bestCVAnumber));
				
				
				fw.write("best Cva for genaration"+m+" is ="+cvaMax+"\n");
				fw.write("best path for genaration"+m+" is =");
				System.out.println("best Cva for genaration"+m+" is ="+cvaMax);
				System.out.print("best path for genaration"+m+" is =");
				ArrayList<Integer> bestPath=iwdn.get(bestCVAnumber).visitedPathList;
				for (int o = 0; o < bestPath.size(); o++) {
				System.out.print(bestPath.get(o)+1+"--->");
				fw.write(bestPath.get(o)+1+"--->");
				}
				System.out.println();
				fw.write("\n");
			//write file for global best
			globalbestSvmFile(iwdn.get(bestCVAnumber).visitedPathList, m);
//			for (int j = 0; j < pathsoil.length; j++) {
//			for (int j2 = 0; j2 < pathsoil.length; j2++) {
//				System.out.print(pathsoil[j][j2]+"  ");
//			}
//			System.out.println();
//			}
			///store total best
			if(cvaMaxTotal<cvaMax){
				cvaMaxTotal=cvaMax;
				globalBest=iwdn.get(bestCVAnumber).visitedPathList;
			}
			
			System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		}//end for generation
	System.out.println(">>>>>>>>>>>>>>>> global best CVA="+cvaMaxTotal);
	fw.close();
	}
	
	public ArrayList<Integer> readData() throws IOException {
		FileReader fr = new FileReader(fl);
		BufferedReader br = new BufferedReader(fr);
		String line;
		int numberOfAttributes=0;
		int numberOfInstance=0;
		while ((line=br.readLine())!=null) {
			numberOfAttributes=(line.split(",").length)-1;
			numberOfInstance++;
		}
		ArrayList<Integer> value = new ArrayList<Integer>();
		System.out.println(numberOfAttributes+"  "+ numberOfInstance);
		value.add(numberOfAttributes);
		value.add(numberOfInstance);
		fr.close();
		br.close();
		return value;
	}
	public int genRandomNumber(Integer start,Integer end,Random r) {
		
		//get the range, casting to long 
	    long range = (long)end- (long)start + 1;
	    // compute a fraction of the range, 0 <= frac < range
	    long fraction = (long)(range * r.nextDouble());
	    int randomNumber =  (int)(fraction + start); 
	    return randomNumber;
	}
	public Integer nextPosition(ArrayList<Integer> visitedPathList,double velocity) {
		
		
		Integer next = 0;
		Integer currentPos=visitedPathList.get(visitedPathList.size()-1);
		
		
		//calculate g(soil(i,j))
		
		//calculate sum of f(soil(i,k))
		double summationSoil=0;
		for (int i = 0; i < pathsoil.length; i++) {
			int visited=0;

			for (int j = 0; j < visitedPathList.size(); j++) {
				//get visited
				if(visitedPathList.get(j)==i){
					visited=1;
					break;}
				
				
			}
			if(visited==1){summationSoil+=0;}
			else{
			double gsoil=pathsoil[currentPos][i];
			double fSoili_j=1/(0.5+gsoil);
			summationSoil+=fSoili_j;
			}
		}
		//System.out.println("sumSoil="+summationSoil);
		   //find min(soil(i,l))
		
		double min=0;
		for (int i = 0; i < pathsoil.length; i++) {
			for (int j = 0; j < pathsoil.length; j++) {
				if(min>pathsoil[i][j]){
					min=pathsoil[i][j];
				}
			}
		}
		//if min <0 transform the whole matrix
		if(min<0){
			
			for (int i = 0; i < pathsoil.length; i++) {
				for (int j = 0; j < pathsoil.length; j++) {
					if(i!=j){pathsoil[i][j]=pathsoil[i][j]-min;}
				}
			}
			
		}
		
		
		//System.out.println(min);
		ArrayList<Double> probArray = new ArrayList<Double>();
		double gSoil=0;
		for (int i = 0; i < pathsoil.length; i++) {
			//if feature(i) is in visited list prob=0;else calculate prob
			
			///check i is in visited list or not
			int visited=0;

			for (int j = 0; j < visitedPathList.size(); j++) {
				//get visited
				if(visitedPathList.get(j)==i){
					visited=1;
					break;}
				
				
			}
			if(visited==1){
				//System.out.println("feature"+i+"is visited");
				probArray.add(i, 0.0);
			}
			else{
				
				//System.out.println("feature"+i+"is not visited");
				gSoil=pathsoil[currentPos][i];
				//System.out.println("gsoil="+gSoil);
				//calculate fsoil
				double fSoili_j=1/(0.5+gSoil);
				//System.out.println("fsoili_j"+fSoili_j);
				double probi_j=fSoili_j/summationSoil;
				probArray.add(i, probi_j);
				
//				if(min<0){
//					gSoil=calculateSoil_iK(currentPos, i,velocity)-min;
//					double fSoili_j=1/(0.5+gwdbcSoil);
//					//System.out.println("fsoili_j"+fSoili_j);
//					double probi_j=fSoili_j/summationSoil;
//					probArray.add(i, probi_j);
//				}
			}
			
			
		}
	
		///###############################
//		for (int i = 0; i < probArray.size(); i++) {
//			System.out.print(probArray.get(i)+"   ");
//		}
//		System.out.println();
		//calculate next number
			//sort probablity array in incresing order
			
		double sumOfProb=0.0;
		for (int i = 0; i < probArray.size(); i++) {
			//System.out.println(probArray.get(i));
			sumOfProb+=probArray.get(i);
		}
		//System.out.println("sumofProp="+sumOfProb);
		//normalize probablity
		for (int i = 0; i < probArray.size(); i++) {
			probArray.set(i, probArray.get(i)/sumOfProb);
		}
		//store index before sorting
		Double[] Array = new Double[probArray.size()];
		for (int i = 0; i < probArray.size(); i++) {
			Array[i]=probArray.get(i);
		}
		ArrayIndexComparator comparator = new ArrayIndexComparator(Array);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);

		//System.out.print("sorted ");
		Collections.sort(probArray);
//		for (int i = 0; i < probArray.size(); i++) {
//			System.out.print(probArray.get(i)+"  ");
//		}
//		System.out.println();
		///generate random double between 0 t0 1
		Random r1 = new Random();
		double rand=(r1.nextDouble());
		//System.out.println("rand="+rand);
		Double prob=0.0;
		
		for (int i = 0; i < probArray.size(); i++) {
			prob+=probArray.get(i);
			//System.out.print(prob+"  ");
			if(prob>rand){next=i;break;}
		}
		//System.out.println();
		
		
		next = indexes[next];
		//System.out.println("next feature ="+next);
		
		return next;
	}
	public double calculateSoil_iK(Integer currentPos,Integer newPos,double velocity) {
		///calculate deltavelIWD
		
		double initialVelocity=velocity;
		int av=1;
		int cv=1;
		double bv=0.01;
		int alpha=2;
		double deltaVelIWD=av/(bv+(cv*Math.pow(pathsoil[currentPos][newPos], 2*alpha)));
		//System.out.println(deltaVelIWD);
		double newVelIWD=deltaVelIWD+initialVelocity;
		//System.out.println("newVel="+newVelIWD);

		double time=1/newVelIWD;
		//System.out.println("time="+time);
		
		int as=1,bs=1;
		double cs=0.01;
		int theta=2;
		double deltaSoilIWD= as/(bs+cs*(Math.pow(time, 2*theta)));

		//System.out.println("deltaSoilIWD="+deltaSoilIWD);

		//calculate soil(i,j)

		double rhoN=0.9;
		double rhoNot=1-rhoN;
		double newsoil = (rhoNot*pathsoil[currentPos][newPos])-rhoN*deltaSoilIWD;
		//System.out.println("newsoil="+newsoil);
		double soilIWD=0+deltaSoilIWD;
		//System.out.println("soilIWD="+soilIWD);
		return newsoil;
	}
	public ArrayList<Double> updateIWD(double vel,ArrayList<Integer> visitedPathList,double soilContent, Double rank, Double chi, Double cfs ) {
		//get iwd initial velocity and position
		//System.out.println(rank+"  "+chi+"  "+cfs);
		double weight=0.5*rank+0.3*chi+0.2*cfs;
		double velocity=vel;
		Integer currentPos=visitedPathList.get(visitedPathList.size()-1);
		Integer prevPos=visitedPathList.get(visitedPathList.size()-2);
		double soil=soilContent;
		//System.out.println("vel="+velocity+"prevPos="+prevPos+"currPos="+currentPos);
		//update velocity
		int av=1;
		int cv=1;
		double bv=0.01;
		double deltaVelIWD=velocity+av/(bv+(cv*Math.pow(pathsoil[prevPos][currentPos], 2)));
		//System.out.println(deltaVelIWD);
		
		///////////////////////////////////////////
		
		double time=1/(weight+deltaVelIWD);
		
		//calculate soil
		int as=1,bs=1;
		double cs=0.01;
		int theta=2;
		double deltaSoilIWD= soil+as/(bs+cs*(Math.pow(time, 2*theta)));
		//System.out.println(deltaSoilIWD);
		
		//update soil matrix
		double updateSoil;
		double rhoN=0.9;
		double rhoNot=1-rhoN;
		double newsoil = (rhoNot*pathsoil[prevPos][currentPos])-rhoN*deltaSoilIWD;
		//System.out.println(newsoil);
		
		pathsoil[prevPos][currentPos]=newsoil;
		ArrayList<Double> returnArray = new ArrayList<Double>();
		returnArray.add(deltaVelIWD);
		//iwd.velocity=;
		returnArray.add(deltaSoilIWD);
		return returnArray;
		
		
		
}
	public void reducedDataset(ArrayList<Integer> visitedPathList) throws IOException {
		
		FileReader fr = new FileReader(fl);
		FileWriter fw = new FileWriter("outLibSVMformat.txt");
		BufferedReader br = new BufferedReader(fr);
		String line;
		Integer i=1;
		while ((line=br.readLine())!=null) {
		    
			fw.write(line.split(",")[0]+" ");
			//System.out.println(line.split(",").length);
			//select only those features that are in visited path list
			for (Integer j = 0; j <= visitedPathList.size()-1; j++) {
				//System.out.print(iwd.visitedPathList.get(j));
				Integer k=j+1;
				fw.write(k.toString());
				fw.append(":"+line.split(",")[visitedPathList.get(j)+1]+" ");
			}//System.out.println();
			//fw.append(i.toString());
			i++;
			fw.append("\n");
		}
		fr.close();
		fw.close();
	}
	public Double runLibSVM() throws IOException {
		Process p;
		///home/kiran/Desktop/IWD/outLibSVMformat.txt
		//C:\\libsvm\\windows\\output.txt
		//-s 0 -t 1 -d 0 -r 0 -c 10 -v 3 /home/kiran/Desktop/IWD/outLibSVMformat.txt"
//		p = Runtime.getRuntime().exec("svm-train -s 0 -t 0 -d 3 -r 0 -c 100 -v 10 -q /home/kiran/Desktop/IWD/outLibSVMformat.txt" );
		p = Runtime.getRuntime().exec("svm-train -s 0 -t 2 -c 32768 -g 0.00048828125 -v 10 -q /home/shameek/workspace/IWD/outLibSVMformat.txt" );

		BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
		
		String line; 
		//line=reader.readLine();
		String CVA="";
		while((line=reader.readLine())!=null){
			
			//System.out.println(line);
			if(line.startsWith("Cross")){CVA+=line.split("=")[1];}
		}
		//System.out.println("CVA= "+CVA);
		Double returnCVA=Double.parseDouble(CVA.substring(0,CVA.length()-1));
		reader.close();
		
		//System.out.println(returnCVA);
		return returnCVA;
		
	}
	public void bestPathSoilUpdate(IWDobjects iwd) {
		//update soil(i,j) of each visited paths
		double rhoIWD=0.9;
		double rhoS=1+rhoIWD;
		Integer nIB=iwd.visitedPathList.size();
		double soilIWD=iwd.soilContent;
		//System.out.println("visited ath size="+iwd.visitedPathList.size());
		for (int i = 0; i < iwd.visitedPathList.size()-1; i++) {
			//System.out.println(iwd.visitedPathList.get(i)+","+iwd.visitedPathList.get(i+1));
			pathsoil[iwd.visitedPathList.get(i)][iwd.visitedPathList.get(i+1)]=(rhoS*pathsoil[iwd.visitedPathList.get(i)][iwd.visitedPathList.get(i+1)])-rhoIWD*(1/(nIB-1)*soilIWD);
		
		
		}
		
	}
	public  HashMap<Integer, Double> readInfoGain(File ranking) throws IOException {
		FileReader fr = new FileReader(ranking);
		BufferedReader br = new BufferedReader(fr);
		String line;
		HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
		while ((line=br.readLine())!=null) {
			
			if(line.startsWith(" 0")){
				
				hm.put(Integer.parseInt(line.split("\\s+")[2]), Double.parseDouble(line.split("\\s+")[1]));
				//System.out.println(line.split("\\s+")[2]);
				}
			
		}
		//iterate over hm
		//System.out.println("  "+hm.get(377));
		//for (Integer key : hm.keySet()) {
			
		//}
		fr.close();
		return hm;
		
		
	}
	public  HashMap<Integer, Double> readChiSquare(File chisquare) throws  IOException {
		FileReader fr = new FileReader(chisquare);
		BufferedReader br = new BufferedReader(fr);
		String line;
		HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
		Pattern p = Pattern.compile("^(\\d+.*|-\\d+.*|\\s+\\d)");
		Pattern capture1 = Pattern.compile("(\\d+\\.\\d+)\\s+(\\d+)");
		Pattern capture2 = Pattern.compile("^\\s+(\\d)\\s+(\\d+)\\s+");
		Matcher m;
		while ((line=br.readLine())!=null) {
			m=p.matcher(line);
			if(m.find()){//System.out.println(line);
			//System.out.println(line.split("\\s+")[0]);
			m=capture1.matcher(line);
			Integer key=0;Double value=0.0;
				if(m.find()){//System.out.println(m.group());
					key=Integer.parseInt(m.group(2));
					value=Double.parseDouble(m.group(1));
				}
			hm.put(key, value);
			m=capture2.matcher(line);
				if(m.find()){//System.out.println(m.group(2));
					key=Integer.parseInt(m.group(2).trim());
					value=Double.parseDouble(m.group(1).trim());
			
				}
			hm.put(key, value);
			}

		}
		//System.out.println(hm.get(864));
		//normalized hashmap
		double summ=0.0;
		for (Integer key : hm.keySet()) {
		summ+=hm.get(key);
		}
		//System.out.println(summ);
		for (Integer key : hm.keySet()) {
		hm.put(key, hm.get(key)/summ);
		}
		//System.out.println(hm.entrySet());
		fr.close();
		br.close();
		return hm;
	}
	public  HashMap<Integer, Double> readCfs(File cfs) throws  IOException {
		FileReader fr = new FileReader(cfs);
		BufferedReader br = new BufferedReader(fr);
		String line;
		HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
		while ((line=br.readLine())!=null) {
			
			if(line.startsWith("Selected attributes")){
				
				for (int i = 0; i <line.split(":")[1].split(",").length; i++) {
					hm.put(Integer.parseInt(line.split(":")[1].split(",")[i].trim()), 0.8);	
				}
				
				}
			
		}
		//System.out.println(hm.entrySet());
		fr.close();
		br.close();
		return hm;
	}
	public double getCfs(HashMap<Integer, Double> hmCfs,Integer pos) {
		double ret=0.0;
		//iterate over hm and if key matches input return value else return 0.2
		
		for (Integer key : hmCfs.keySet()) {
			if(key==pos){ret=hmCfs.get(key);}
			else{ret=0.2;}
			//hmCfs.put(key, hm.get(key)/summ);
			}
		
		
		
		
		return 	ret;
	}
	public void globalbestSvmFile(ArrayList<Integer> visitedPathList,Integer gen) throws IOException {
		
			
			FileReader fr = new FileReader(fl);
			File folder = new File("outputAll");
			folder.mkdir();
			FileWriter fw = new FileWriter("/home/shameek/workspace/IWD/outputAll/"+gen+"_outLibSVMformat.txt");
			BufferedReader br = new BufferedReader(fr);
			String line;
			Integer i=1;
			while ((line=br.readLine())!=null) {
			    
				fw.write(line.split(",")[0]+" ");
				//System.out.println(line.split(",").length);
				//select only those features that are in visited path list
				for (Integer j = 0; j <= visitedPathList.size()-1; j++) {
					//System.out.print(iwd.visitedPathList.get(j));
					Integer k=j+1;
					fw.write(k.toString());
					fw.append(":"+line.split(",")[visitedPathList.get(j)+1]+" ");
				}//System.out.println();
				//fw.append(i.toString());
				i++;
				fw.append("\n");
			}
			fr.close();
			fw.close();
		
	}
}
