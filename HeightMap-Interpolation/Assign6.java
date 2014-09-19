import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Scanner;


public class Assign6 {
	static class Point3d{
		private double x,y,z;
		public Point3d(){x=0;y=0;z=0;}
		public Point3d(double cx, double cy,double cz){x=cx;y=cy;z=cz;}
		public void setX(double x){ this.x=x;}
		public void setY(double y){this.y=y;}
		public void setZ(double z){this.z=z;}
		public double getX(){ return x;}
		public double getY(){ return y;}
		public double getZ(){ return z;}
	}
	static class pix{
		private byte r,g,b,a;
		public pix(int R, int G, int B, int A){ r=(byte)R; g=(byte)G; b=(byte)B;a=(byte)A; }
		public pix(byte R,byte G, byte B, byte A){r=R; G=g ;b=B; a=A;}
		public byte R(){ return r;}
		public void R(byte R){ r=R;}
		public void R(int R){r=(byte)R;}
		public byte G(){ return g;}
		public void G(byte G){ r=G;}
		public void G(int G){r=(byte)G;}
		public byte B(){ return b;}
		public void B(byte B){ r=B;}
		public void B(int B){r=(byte)B;}
		public byte A(){ return a;}
		public void A(byte A){ r=A;}
		public void A(int A){r=(byte)A;}
	}
	public static void main(String [] args){

		System.out.println("Generating Points for Question 1.");
		for(Point3d pnt:Interpolate(createTriangle(0.5,0.5,1,9.5,2.5,5,4.5,9.5,10),10,10)){
			System.out.println(pnt.getX()+ ","+ pnt.getY() + ","+pnt.getZ());
		}

		System.out.println("Generating Points for Question 2.");
		System.out.println("Points will be outputed to Q2gridsAll.txt");
		System.out.println("If the program exits during this step please chmod");
		System.out.println("this directory as so chmod 750 *");
		genPointFiles(genNodeFile(new File("points.txt")));
		System.out.println("Height maps generated see output directory for bmp");
		System.out.println(	"files in the following format widthxheight.bmp");
		
	}
	/**
	 * Outputs a list of interpolated points to a file and performs interpolation on the selected  input points
	 * @param pntsIn A list of non-interpolated points.
	 */
	public static void genPointFiles(ArrayList<Point3d> pntsIn){
		try{
			//Generates the necessary points for delaunay triangles.
			Scanner gridS= new Scanner(new File("Q2Grid.1.ele"));
			ArrayList<Point3d> Triangles=new ArrayList<Point3d>();
			int tCount=gridS.nextInt();
			gridS.nextInt();
			gridS.nextInt();
			//Generates a list of triangles from the input points.
			for (int i=0;i<tCount;i++){
				gridS.nextInt();
				Triangles.add(pntsIn.get(gridS.nextInt()-1));
				Triangles.add(pntsIn.get(gridS.nextInt()-1));
				Triangles.add(pntsIn.get(gridS.nextInt()-1));

			}

			//Write the nodes to a file.
			File file = new File("Q2GridsAll.txt");
			if (!file.exists()) { file.createNewFile(); }
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			//Interpolates the delaunay triangles.
			ArrayList<Point3d>PointsA=new ArrayList<Point3d>();
			ArrayList<Point3d>PointsB=new ArrayList<Point3d>();
			ArrayList<Point3d>PointsC=new ArrayList<Point3d>();
			for(int i=0;i<Triangles.size();i+=3){

				PointsA.addAll(OptimizedInterpolate(new Point3d[]{Triangles.get(i),Triangles.get(i+1),Triangles.get(i+2)},30,20));
				PointsB.addAll(OptimizedInterpolate(new Point3d[]{Triangles.get(i),Triangles.get(i+1),Triangles.get(i+2)},120,80));
				PointsC.addAll(OptimizedInterpolate(new Point3d[]{Triangles.get(i),Triangles.get(i+1),Triangles.get(i+2)},480,320));

			}
			
			//Writes a 30x20 matrix to the point file and a bmp.
			bw.write("Points 30x20");
			bw.newLine();
			for(Point3d pnt:PointsA){
				bw.write(pnt.getX() + " "+ pnt.getY() + " " + pnt.getZ());
				bw.newLine();
			}
			//Writes a redscale height map bmp of the input matrix.
			genBMP(30,20,PointsA,"30x20.bmp");
			//Attempt at linear interpolation and marching squares.
			//genBMPb(30,20,PointsC,"120x80C.bmp");
			
			//Writes a 120x80 matrix to the point file and bmp file.
			bw.newLine();
			bw.write("Points 120x80");
			bw.newLine();
			for(Point3d pnt:PointsB){
				bw.write(pnt.getX() + " "+ pnt.getY() + " " + pnt.getZ());
				bw.newLine();
			}
			genBMP(120,80,PointsB,"120x80.bmp");
			//Attempt at linear interpolation and marching squares.
			//genBMPb(120,80,PointsC,"120x80C.bmp");

			//Writes a 480x320 matrix to the point file and bmp file.
			bw.newLine();
			bw.write("Points 480x320");
			bw.newLine();
			for(Point3d pnt:PointsC){
				bw.write(pnt.getX() + " "+ pnt.getY() + " " + pnt.getZ());
				bw.newLine();
			}
			genBMP(480,320,PointsC,"480x320.bmp");
			//Attempt at linear interpolation and marching squares.
			//genBMPb(480,320,PointsC,"480x320C.bmp");
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("File Access Error!");
			System.out.println("Exiting Program...");
		}
	}
	/**
	 * Generates a list of delaunay triangle points.
	 * @param in The input file containing a list of points.
	 * @return An arraylist of 3d points that make up the delaunay triangles formed in the input file.
	 */
	public static ArrayList<Point3d> genNodeFile(File in){

		try {
			//Generate a list of 3dPoints for the input file.
			Scanner sPnts=new Scanner(in);
			ArrayList<Point3d> pnts=new ArrayList<Point3d>();
			int count=0;
			while(sPnts.hasNextDouble()){
				pnts.add(new Point3d(sPnts.nextDouble(),sPnts.nextDouble(),sPnts.nextDouble()));
				count++;
			}
			//Write the nodes to a file.
			File file = new File("Q2Grid.node");
			if (!file.exists()) { file.createNewFile(); }
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(count + " 2 0 0");
			bw.newLine();
			for(int i=0;i<pnts.size();i++){
				bw.write((i+1) + " " + pnts.get(i).getX() + " " + pnts.get(i).getY());
				bw.newLine();
			}
			bw.close();

			//Generate a delaunay triangulation matrix.
			Process p=Runtime.getRuntime().exec("chmod 750 *");
			p=Runtime.getRuntime().exec("./triangle " + "Q2Grid.node");



			return pnts;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("File Access Error");
			System.out.println("Exiting Program...");
			System.exit(0);
		}
		return new ArrayList<Point3d>();
	}

	/**
	 * Create a triangle with the given 9 points.
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @param x3
	 * @param y3
	 * @param z3
	 * @return A 3d point array representing the given triangle.
	 */
	public static Point3d [] createTriangle(double x1,double y1,double z1,double 
			x2,double y2, double z2,double x3,double y3, double z3){
		//Create a 3d triangle out of the given 9 points.
		Point3d [] Points=new Point3d[3];
		Points[0]=new Point3d(x1,y1,z1);
		Points[1]=new Point3d(x2,y2,z2);
		Points[2]=new Point3d(x3,y3,z3);
		return Points;
	}

	/**
	 * An interpolation function.
	 * @param Triangle
	 * @param gridX The grids x size.
	 * @param gridY The grids y size.
	 * @return An arrayList of interpolated Points.
	 */
	public static ArrayList<Point3d> Interpolate(Point3d [] Triangle,int gridX,int gridY){
		ArrayList<Point3d> Points=new ArrayList<Point3d>();

		//Get the X coordinates from the input triangle.
		double Xa= Triangle[0].getX();
		double Xb= Triangle[1].getX();
		double Xc= Triangle[2].getX();

		//Get the Y coordinates from the input triangle.
		double Ya= Triangle[0].getY();
		double Yb= Triangle[1].getY();
		double Yc= Triangle[2].getY();

		//Get the Z coordinates from the input triangle.
		double Za= Triangle[0].getZ();
		double Zb= Triangle[1].getZ();
		double Zc= Triangle[2].getZ();

		//Add points for the outside of the triangle to the point list.
		Points.add(new Point3d(Xa,Ya,Za));
		Points.add(new Point3d(Xb,Yb,Zb));
		Points.add(new Point3d(Xc,Yc,Zc));

		//Calculate the inverse matrix
		double Scalar= Math.abs(1/(((Xa-Xc) * (Yb-Yc))-((Xb-Xc)*(Ya-Yc))));
		double [][] invMatrix =new double [2][2];
		invMatrix[0][0]=Scalar * (Yb-Yc);
		invMatrix[1][0]=Scalar * -(Xb-Xc);
		invMatrix[0][1]=Scalar * -(Ya-Yc);
		invMatrix[1][1]=Scalar * (Xa-Xc);

		if(Triangle.length!=3) throw new InvalidParameterException();
		else{
			for(int Xp=0;Xp <= gridX;Xp++){
				for(int Yp=0;Yp <= gridY;Yp++){

					//Calculate the weights for each coordinate;
					double Wa=(invMatrix[0][0] * (Xp-Xc)) + (invMatrix[1][0] * (Yp-Yc));
					double Wb=(invMatrix[0][1] * (Xp-Xc)) + (invMatrix[1][1] * (Yp-Yc));
					double Wc=1-Wa-Wb;
					if(Wa>=0 && Wb>=0 && Wc>=0){
						double Zp=(Za*Wa) + (Zb*Wb) + (Zc*Wc);
						Points.add(new Point3d(Xp,Yp,Zp));
					}
				}
			}
		}
		return Points;
	}
	
	/**
	 * An optimized interpolation function.
	 * @param Triangle
	 * @param gridX The grids x size.
	 * @param gridY The grids y size.
	 * @return An arrayList of interpolated Points.
	 */
	public static ArrayList<Point3d> OptimizedInterpolate(Point3d [] Triangle,int gridX,int gridY){
		ArrayList<Point3d> Points=new ArrayList<Point3d>();
		int left,top,right,bottom;

		//Get the X coordinates from the input triangle.
		double Xa= Triangle[0].getX() * ((double)gridX/150);
		double Xb= Triangle[1].getX() * ((double)gridX/150);
		double Xc= Triangle[2].getX() * ((double)gridX/150);
		if (Xa<Xb && Xa<Xc) left=(int)Xa;
		else if(Xb<Xa && Xb<Xc) left=(int)Xb;
		else left=(int)Xc;
		if(Xa>Xb && Xa>Xc) right=(int)Xa;
		else if(Xb>Xa && Xb>Xc) right=(int)Xb;
		else right=(int)Xc;

		//Get the Y coordinates from the input triangle.
		double Ya= Triangle[0].getY() * ((double)gridY/100);
		double Yb= Triangle[1].getY() * ((double)gridY/100);
		double Yc= Triangle[2].getY() * ((double)gridY/100);
		if (Ya<Yb && Ya<Yc) top=(int)Ya;
		else if(Yb<Ya && Yb<Yc) top=(int)Yb;
		else top=(int)Yc;
		if(Ya>Yb && Ya>Yc) bottom=(int)Ya;
		else if(Yb>Ya && Yb>Yc) bottom=(int)Yb;
		else bottom=(int)Yc;

		//Get the Z coordinates from the input triangle.
		double Za= Triangle[0].getZ();
		double Zb= Triangle[1].getZ();
		double Zc= Triangle[2].getZ();

		//Add points for the outside of the triangle to the point list.
		Points.add(new Point3d(Xa,Ya,Za));
		Points.add(new Point3d(Xb,Yb,Zb));
		Points.add(new Point3d(Xc,Yc,Zc));

		//Calculate the inverse matrix
		double Scalar= Math.abs(1/(((Xa-Xc) * (Yb-Yc))-((Xb-Xc)*(Ya-Yc))));
		double [][] invMatrix =new double [2][2];
		invMatrix[0][0]=Scalar * (Yb-Yc);
		invMatrix[1][0]=Scalar * -(Xb-Xc);
		invMatrix[0][1]=Scalar * -(Ya-Yc);
		invMatrix[1][1]=Scalar * (Xa-Xc);

		if(Triangle.length!=3) throw new InvalidParameterException();
		else{
			for(int Xp=left;Xp <= right;Xp++){
				for(int Yp=top;Yp <= bottom;Yp++){

					//Calculate the weights for each coordinate;
					double Wa=(invMatrix[0][0] * (Xp-Xc)) + (invMatrix[1][0] * (Yp-Yc));
					double Wb=(invMatrix[0][1] * (Xp-Xc)) + (invMatrix[1][1] * (Yp-Yc));
					double Wc=1-Wa-Wb;

					double Zp=(Za*Wa) + (Zb*Wb) + (Zc*Wc);
					Points.add(new Point3d(Xp,Yp,Zp));

				}
			}
		}
		return Points;
	}

	/**
	 * Generate a redscaled height map bmp from a list of interpolated points.
	 * @param GridX The Grid's X Size.
	 * @param GridY The Grid's Y Size.
	 * @param pnts A list of interpolated points.
	 * @param fBmp The name of the output file.
	 */
	public static void genBMP(int GridX,int GridY,ArrayList<Point3d> pnts,String fBmp){	
		try{
			//Open a data output stream.
			DataOutputStream os = new DataOutputStream(new FileOutputStream(fBmp));
			//Write the bmp header.
			os.write(new byte []{66,77});
			os.writeInt(Integer.reverseBytes(54+(GridX*GridY*4)));
			os.write(new byte[]{0,0,0,0});
			os.writeInt(Integer.reverseBytes(54));
			os.writeInt(Integer.reverseBytes(40));
			os.writeInt(Integer.reverseBytes(GridX));
			os.writeInt(Integer.reverseBytes(GridY));
			os.writeShort(Short.reverseBytes((short) 1));
			os.writeShort(Short.reverseBytes((short) 32));
			os.writeInt(0);
			os.writeInt(Integer.reverseBytes(GridX*GridY*4));
			os.writeInt(Integer.reverseBytes(2835));
			os.writeInt(Integer.reverseBytes(2835));
			os.writeInt(0);
			os.writeInt(0);
			//Create an empty pixel grid.
			pix [][] buf=new pix[GridX][GridY];
			for(int y=GridY-1;y>=0;y--){
				for(int x=0;x<GridX;x++){
					buf[x][y]=new pix(0,0,0,0);
				}
			}


			//Finds the min/max height of the 3d point data.
			int min=(int)pnts.get(0).getZ();
			int max=(int)pnts.get(0).getZ();;
			for(Point3d pnt:pnts){
				if (pnt.getZ()<min) min=(int)pnt.getZ();
				else if(pnt.getZ()>max) max=(int) pnt.getZ();
			}
			
			//Calculate the range.
			int Range=Math.abs(min)+Math.abs(max);
			//Calculate the adjustment.
			int Adj=Math.abs(min);
			for(Point3d pnt:pnts){
				//Calculate a value for the redscale based on adjusted z coordinate.
				byte RGB=(byte)(255*(double)((double)pnt.getZ()+Adj)/(double)Range);
				//Set the pixel to the redscale value.
				if( (int)pnt.getX()<buf.length &&(int)pnt.getY()<buf[0].length){
					buf[(int)pnt.getX()][(int)pnt.getY()]=new pix(RGB,0,0,0);
				}
			}
			//Write the output buffer to the bmp file.
			for(int y=GridY-1;y>=0;y--){
				for(int x=0;x<GridX;x++){
					os.write(new byte[]{buf[x][y].B(),buf[x][y].G(),buf[x][y].R(),0});
				}
			}
			os.close();//Close the file.
		}
		catch(Exception e){
			e.printStackTrace();	
		}

	}

	/**
	 * Generates a bitmap after performing linear interpolation.
	 * @param GridX The Grids x size.
	 * @param GridY The Grids y Size.
	 * @param pnts A list of points containing point data to interpolate.
	 * @param fBmp The bmp output filename.
	 */
	public static void genBMPb(int GridX,int GridY,ArrayList<Point3d> pnts,String fBmp){	
		try{

			DataOutputStream os = new DataOutputStream(new FileOutputStream(fBmp));
			//Write the bmp header.
			os.write(new byte []{66,77});
			os.writeInt(Integer.reverseBytes(54+(GridX*GridY*4)));
			os.write(new byte[]{0,0,0,0});
			os.writeInt(Integer.reverseBytes(54));
			os.writeInt(Integer.reverseBytes(40));
			os.writeInt(Integer.reverseBytes(GridX));
			os.writeInt(Integer.reverseBytes(GridY));
			os.writeShort(Short.reverseBytes((short) 1));
			os.writeShort(Short.reverseBytes((short) 32));
			os.writeInt(0);
			os.writeInt(Integer.reverseBytes(GridX*GridY*4));
			os.writeInt(Integer.reverseBytes(2835));
			os.writeInt(Integer.reverseBytes(2835));
			os.writeInt(0);
			os.writeInt(0);
			Point3d[][] Grid=new Point3d[GridX][GridY];
			//Align all of the points to a 2d matrix
			for(Point3d pnt:pnts){
				if(pnt.getX()<GridX && pnt.getY()<GridY)Grid[(int)pnt.getX()][(int)pnt.getY()]=pnt;
			}
			
			//Draw the lines to a bitmap file
			for(int x=0;x<GridX;x++){
				for(int y=0;y<GridY;y++){
					if(Grid[x][y]==null) Grid[x][y]=new Point3d(x,y,0);
				}
			}
			//Get the contour of the 2d matrix.
			ArrayList<Point3d> Lines=getContour(GridX,GridY,100,Grid);
			//Free the grid for later line drawing
			Grid=new Point3d[GridX][GridY];
			//Get the points for the contour lines, and set the program to draw at these points.
			for(int i=0;i<Lines.size();i+=2){
				ArrayList<Point3d> Line=createLine((int)Lines.get(i).getX(),(int)Lines.get(i).getY()
				,(int)Lines.get(i+1).getX(),(int)Lines.get(i+1).getY());
				for(Point3d pnt:Line){
					if (pnt.getX()>=0 && pnt.getY()>=0){
						if((int)pnt.getX()<GridX && (int)pnt.getY()<GridY)Grid[(int)pnt.getX()][(int)pnt.getY()].setZ(1);
					}
					else{}
				}
			}
			
			//Draw the lines to a bitmap file
			for(int x=0;x<GridX;x++){
				for(int y=0;y<GridY;y++){
					if (Grid[x][y]!=null){
						if (Grid[x][y].getZ()!=0) os.write(new byte[]{(byte) 255,0,0,0});
						else os.write(new byte[]{(byte) 255,(byte)255,(byte)255,0});
					}
					else os.write(new byte[]{(byte) 255,(byte)255,(byte)255,0});
						
				}
			}
			os.close();
		}
		catch(Exception e){
			e.printStackTrace();	
		}

	}
	/**
	 * Performs linear interpolation and returns a list of points representing the contour.
	 * @param GridX The Grid's X Size.
	 * @param GridY The Grid's Y Size.
	 * @param Threshold The threshold.
	 * @param in A 2d input grid representing the points that need to be interpolated.
	 * @return A list of points represneting the contour.
	 */
	public static ArrayList<Point3d> getContour(int GridX, int GridY,double Threshold,Point3d [][] in){
		ArrayList<Point3d> pnts=new ArrayList<Point3d>();
		for(int x=0;x<GridX;x++){
			for(int y=0;y<GridY-1;y++){
				//Get the "Isovalue" for the current x,y pair.
				double Za=in[x][y].getZ();
				//Get the "Isovalue" for the pair (x,y+1)
				double Zb=in[x][y+1].getZ();
				double T=0;
				//Perform T calculation if necessary flip Za and Zb.
				//p=p0+(p1-p0)t => t= (p-p0)/(p1-p0)
				if(Za>Zb) T=(Threshold-Zb)/(Za-Zb);
				else T=(Threshold-Za)/(Zb-Za);
		
				Point3d addMe=new Point3d(0,0,0);
				//Get the x coordinate.
				//interpolated point px= p1x+((p2x-p21x)*T)
				if(in[x][y].getX()>in[x][y+1].getX())addMe.setX((in[x][y].getX()-in[x][y+1].getX())*T);
				else addMe.setX((in[x][y+1].getX()-in[x][y].getX())*T);
				//Get the y coordinate
				//Interpolate point py=p1y+((p2y-p1y)*T)
				if(in[x][y].getY()>in[x][y+1].getY())addMe.setY((in[x][y].getY()-in[x][y+1].getY())*T);
				else addMe.setY((in[x][y+1].getY()-in[x][y].getY())*T);
				addMe.setX(in[x][y].getX()+addMe.getX());
				addMe.setY(in[x][y].getY()+addMe.getY());
				pnts.add(addMe); //Add the point to the contour point list.
				
				//Debugging info
				//System.out.println(Za + " " + Zb);
			}
		}
		return pnts;
	}
	/**
	 * Creates a line given a beginning (x,y) coordinate and and ending (x,y) coordinate
	 * @param bx Beginning Points x coordinate
	 * @param by Beginning Points y Coordinate.
	 * @param ex Ending Points x Coordinate.
	 * @param ey Ending Points y Coordinate.
	 * @return A set of points representing the line.
	 */
	public static ArrayList<Point3d> createLine(int bx, int by, int ex, int ey){
		ArrayList<Point3d> pnts=new ArrayList<Point3d>();
		//Calculate slope values.
		int SlopeX= ex-bx;
		int SlopeY=ey-by;
		//Swap x values if necessary for the for loop. 
		int temp=bx;
		if (bx>ex) { bx=ex; ex=temp;}
		int y= 0;

		//Y=mx+b
		//Y=m(x-bx)+b
		//x must be adjusted so that the begining x points sets the begining y point
		//to b.
		//Calculate the y value at each point x given the initial starting value for y.
		for(int x=bx;x<ex;x++){
			y= (int)((double)0.5*(double)(SlopeY/SlopeX)*(x-bx)+(double)by);
			pnts.add(new Point3d(x,y,0));
			//Debugging info.
			//System.out.println(bx+" "+by+" "+ex+ " "+ey+ " "+ x+ " " + y);
		}
		return pnts;
	}
}
