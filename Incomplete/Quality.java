package Assign5;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Quality {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String files[]=new String[]{"circle","circle2d-outer","circle2d"};
		
		for(int i=0;i<files.length;i++){
			try{
				Process p=Runtime.getRuntime().exec("./triangle -e " + files[i] +".node");
			}
			catch(Exception e){
				System.out.println("Triangle program is not in the correct location! Please copy it to the same folder as the java executable.");
				return;
			}
			try {
				int NumNodes=0;
				HashMap<Integer,Point.Double> Nodes=new HashMap<Integer,Point.Double>();
				Scanner nScanner=new Scanner(new File(files[i] +".1.node"));
				//Grab the number of Nodes
				if(nScanner.hasNextInt()) NumNodes=nScanner.nextInt();

				//Seek to begining of Node list.
				if(nScanner.hasNextInt()) nScanner.nextInt();
				if(nScanner.hasNextInt()) nScanner.nextInt();
				if(nScanner.hasNextInt()) nScanner.nextInt();
				else throw new FileNotFoundException();
			
				int Node=0;
				double xd=0,yd=0;	
				for(int j=0;j<NumNodes;j++){
					if(nScanner.hasNextInt()) Node=nScanner.nextInt();
					if(nScanner.hasNextDouble()) xd=nScanner.nextDouble();
					if(nScanner.hasNextDouble()) yd=nScanner.nextDouble();
					if(nScanner.hasNextInt()) nScanner.nextInt();
					else throw new FileNotFoundException();
					Nodes.put(Node, new Point.Double(xd,yd));
				}
				
				
				Scanner fScanner=new Scanner(new File(files[i] +".1.edge"));
				int NumEdges=0; double Min; double Max;
				int x=0; int y=0;double eLength;
				double qRms=0;
				
				//Grab the number of edges.
				if(fScanner.hasNextInt()) NumEdges=fScanner.nextInt();
				if(fScanner.hasNextInt()) fScanner.nextInt();
				else throw new FileNotFoundException();
				
				//Grab the first edge.
				if(fScanner.hasNextInt()) fScanner.nextInt();
				if(fScanner.hasNextInt()) x=fScanner.nextInt();
				if(fScanner.hasNextInt()) y=fScanner.nextInt();
				if(fScanner.hasNextInt()) fScanner.nextInt();
				else throw new FileNotFoundException();
				eLength=Math.sqrt(Math.pow(Nodes.get(x).x, 2) + Math.pow(Nodes.get(y).y, 2));
				Min=eLength;
				Max=eLength;
				
				for(int k=1;k<=3;k++) qRms=eLength * Math.pow(k,2);
				qRms=Math.sqrt(qRms/3);
				
				for(int j=0;j<NumEdges-1;j++){
					if(fScanner.hasNextInt()) fScanner.nextInt();
					if(fScanner.hasNextInt()) x=fScanner.nextInt();
					if(fScanner.hasNextInt()) y=fScanner.nextInt();
					if(fScanner.hasNextInt()) fScanner.nextInt();
					else throw new FileNotFoundException();
					double x2=Nodes.get(x).x; double y2=Nodes.get(y).y;
					eLength=Math.sqrt(Math.pow((double)x2, 2) + Math.pow((double)y2, 2));
					if (eLength<Min) Min=eLength;
					else if(eLength>Max) Max=eLength;
					
					for(int k=1;k<=3;k++) qRms=eLength * Math.pow(k,2);
					qRms=Math.sqrt(qRms/3);
				}
				qRms=Math.PI/Math.pow(qRms,2);
				System.out.println(files[i] + " Quality (lmax/lmin): " + (Max/Min));
				System.out.println(files[i] + " Quality (lmax/2*rcirc): " + (Max/2));
				System.out.println(files[i] + " Quality (RMS): " + qRms);
			}  
			catch (FileNotFoundException f) {
				System.out.println("Edge file access error!");
			}
		}
	}
}
