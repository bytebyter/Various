package Assign5;

import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Mesh {
	public static void main(String[] args) throws FileNotFoundException {
		try {	
			//Create the initial 4 bounding triangles
			//Upper left
			Rectangle.Double Ul=new Rectangle.Double();
			Ul.x=-2;Ul.y=-2;
			Ul.width=2;Ul.height=2;
			//Upper right
			Rectangle.Double Ur=new Rectangle.Double();
			Ur.x=0;Ur.y=-2;
			Ur.width=2;Ur.height=2;
			//Bottom left
			Rectangle.Double Bl=new Rectangle.Double();
			Bl.x=-2;Bl.y=0;
			Bl.width=2;Bl.height=2;
			//Bottom Right
			Rectangle.Double Br=new Rectangle.Double();
			Br.x=0;Br.y=0;
			Br.width=2;Br.height=2;

			//Perform the quad split on each rectangle.
			String [] Out=(splitMesh(Ul) + splitMesh(Ur)+splitMesh(Bl) + splitMesh(Br)).split(";");

			//Create the circle.node file.
			File file = new File("circle.node");
			// if file doesnt exists, then create it
			if (!file.exists()) { file.createNewFile(); }
			//Write the outputs of 1,000, 10,000, and 20,0000 factorial to the file.
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(Out.length + " 2" +" 0" + " 0");
			bw.newLine();
			for(int i=0; i<Out.length;i++){
				bw.write((i+1)+ " " +Out[i]);
				bw.newLine();
			}
			bw.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		try{
			Process p=Runtime.getRuntime().exec("chmod 750 *");
			Process p=Runtime.getRuntime().exec("./triangle -e " + "circle.node");
		}
		catch(Exception e){
			System.out.println("Triangle program is not in the correct location! Please copy it to the same folder as the java executable.");
			return;
		}
	}
	public static String splitMesh(Rectangle.Double r){
		boolean UlInside=false;boolean UrInside=false;
		boolean BlInside=false;boolean BrInside=false;
		//Use a simplified method of determining if the triangle lies within the circle.
		//Warning this method will only work with circles.
		if (Math.sqrt(Math.pow(r.x,2)+Math.pow(r.y,2))<=2) UlInside=true;						//Check upper left point
		if (Math.sqrt(Math.pow(r.x + r.width,2)+Math.pow(r.y,2))<=2) UrInside=true;				//Check upper right point.
		if (Math.sqrt(Math.pow(r.x,2)+Math.pow(r.y + r.height,2))<=2) BlInside=true;			//Check lower left point.
		if (Math.sqrt(Math.pow(r.x + r.width,2)+Math.pow(r.y + r.height,2))<=2) BrInside=true;	//Check lower right point.
		if (UlInside==true && UrInside==true && BlInside==true && BrInside==true){
			//No need to split, as the rectangle is fully inside the circle;
			return r.x + " " + r.y + ";" + (r.x + r.width) + " "+ r.y + ";" + 
			r.x + " " + (r.y+r.height) + ";" + (r.x + r.width) + " "+ (r.y+r.height) +";";
		}
		else if (UlInside==false && UrInside==false && BlInside==false && BrInside==false){
			//The rectangle is not inside the circle.
			return "";//discard of the rectangle;
		}
		else{
			//Attempt to split if < 0.1 then do not split
			if ((r.width/2) >=0.1){
				//Get the Upper left rectangle
				Rectangle.Double Ul=new Rectangle.Double();
				Ul.x=r.x;Ul.y=r.y; 
				Ul.height=r.height/2;
				Ul.width=r.width/2;
				//Get the Upper right rectangle.
				Rectangle.Double Ur=new Rectangle.Double();
				Ur.x=r.x+ (r.width/2);
				Ur.y=r.y; 
				Ur.height=r.height/2;
				Ur.width=r.width/2;
				//Get the Bottom left triangle.
				Rectangle.Double Bl=new Rectangle.Double();
				Bl.x=r.x;
				Bl.y=r.y+ (r.height/2); 
				Bl.height=r.height/2;
				Bl.width=r.width/2;
				//Get the Bottom right triangle.
				Rectangle.Double Br=new Rectangle.Double();
				Br.x=r.x+ (r.width/2);
				Br.y=r.y+ (r.height/2); 
				Br.height=r.height/2;
				Br.width=r.width/2;
				//Recursively acquire all of the necessary quads.
				return splitMesh(Ul) + splitMesh(Ur) + splitMesh(Bl) + splitMesh(Br);
			}
			else{//The rectangle is too small to split => return the rectangle.
				return r.x + " " + r.y + ";" + (r.x + r.width) + " "+ r.y + ";" + 
				r.x + " " + (r.y+r.height) + ";" + (r.x + r.width) + " "+ (r.y+r.height)
				+";";
			}
		}
	}


}
