package Assign5;

import java.util.Random;

public class PointGeneration {

	public static void main(String[] args) {
		int start=63;
		System.out.println("Generating 250 points for Question 2 part 2.");
		System.out.println("Copy and paste points to the bottom of circle2d-outer.node,");
		System.out.println("then update number of nodes at top of file from 62 to 312.");
		Random rnd=new Random();
		Random negx=new Random();
		Random negy=new Random();
		for(int i=0;i<250;i++){
			double x=rnd.nextDouble();
			double y=rnd.nextDouble();
			if(Math.sqrt(Math.pow(x,2)+Math.pow(y, 2))>=1){
				while(Math.sqrt(Math.pow(x,2)+Math.pow(y, 2))>=1){
					x=rnd.nextDouble();
					y=rnd.nextDouble();
				}
			}
			if(negx.nextBoolean()) x=-x;
			if(negy.nextBoolean()) y=-y;
			System.out.println((i+start) + " " + x + " " + y);
		}

	}

}
