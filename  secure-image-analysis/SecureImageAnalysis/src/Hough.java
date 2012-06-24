import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.imageio.ImageIO;


public class Hough {

	/**
	 * @param args
	 */
	static int [][] rho_theta_space;
	static int [] rhos; 
	static double [] thetas;
	static int rho_dim=100;
	static int theta_dim=100;
	static int threshold=200;
	static int w;
	static int h;
	static BufferedImage img = null; 
	static HashSet<LocalMaxima> local_maxima_array;
	public static void main(String[] args) {
		//Reading an image file 
		try {
			long start_initialization = System.currentTimeMillis();
		    img = ImageIO.read(new File("img/singleLine.png"));
		    w = img.getWidth(null);
		    h = img.getHeight(null);
		    System.out.println("x_min " +img.getMinX()); 
		    System.out.println("y_min " +img.getMinY()); 
		    System.out.println("width "+img.getWidth()); 
		    System.out.println("height "+img.getHeight()); 
		    
		    //get all the rgbs: 
		    //int[] rgbs = new int[img.getWidth()*img.getHeight()];
		    //img.getRGB(img.getMinX(), img.getMinY(), w, h, rgbs, 0, w);
		    //int count =0;
		    
		    // initialize the rhos and thetas 
		    int rho_step=1;
		    int rho_min=-w;
		    int rho_max=(int) Math.sqrt(w*w+h*h);
		    double theta_step=Math.atan(Math.min(1./w,1./h));
		    double theta_max=Math.PI; // theta_min=0
		    System.out.println("rho_range= "+rho_min+":"+rho_step+":"+rho_max+"  -->  "+(rho_max-rho_min)/rho_step);
		    System.out.format("theta_range= "+"0:%.4f:3.14  -->  %d\n",theta_step,(int)(theta_max/theta_step));
		    rhos = new int [((rho_max-rho_min)/rho_step)+1];
		    thetas = new double [(int) Math.round(theta_max/theta_step)+1];
		    for (int i=0;i< rhos.length;i++){
		    	rhos[i]=rho_min+i*rho_step;
		    }
		    for (int i=0;i< thetas.length;i++){
		    	thetas[i]=i*theta_step;
		    	//System.out.println(thetas[i]+" "+ Math.cos(thetas[i])+" "+Math.sin(thetas[i]));
		    }
		    //thetas[thetas.length-1]=Math.PI;
		    //System.out.println(Math.PI);
		    rho_theta_space = new int[rhos.length][thetas.length];
		    for (int i=0;i< rhos.length;i++)
		    	for (int j=0; j< thetas.length; j++)
		    		rho_theta_space[i][j]=0;
		    
		    local_maxima_array = new HashSet<LocalMaxima>();
		    System.out.println("rho_local_maximization_radius "+rho_dim);
		    System.out.println("theta_local_maximization_radius "+theta_dim);
		    System.out.println("threshold_votes "+threshold);
		    System.out.println("Detected straight lines:");
		    System.out.println("i,j: \t\t rho \t theta \t\t votes");
		    
		    long stop_initialization = System.currentTimeMillis();
		    //look for lines
		    long start_populating = System.currentTimeMillis();
		    for (int x=0; x<w; x++)
		    	for (int y=0; y<h; y++){
		    		// Get a pixel
		    		int p = img.getRGB(x, y);
		    		if (p!=-1){ //p==-1 is for white (0xffffffff)
		    			//System.out.format("pixel %x\n",p);
		    			//count++;
		    			
		    			for (int t=0; t<thetas.length; t++){
		    				//calculate rho 
		    				int rho =  (int) Math.round(y*Math.sin(thetas[t])+x*Math.cos(thetas[t]));
		    				//System.out.println(rho);
		    				//increment the rho_tetha_space
		    				rho_theta_space[(rho-rho_min)/rho_step][t]++;
		    			}
		    		}
		    	}
		    long stop_populating = System.currentTimeMillis();
		    
		    //output
		    long start_local_maximization = System.currentTimeMillis();
		    for (int i=0;i< rhos.length;i++)
		    	for (int j=0; j< thetas.length; j++){
		    		// thresholding + local maximisation 
		    		if (rho_theta_space[i][j]>threshold && is_local_maxima_rectangle(i,j,rho_dim, theta_dim)){
		    			if (!local_maxima_array.contains(new LocalMaxima(i,j,rho_theta_space[i][j]))){
		    				System.out.format("%d,%d:\t %d \t %.4f \t %d\n",i,j, rhos[i],thetas[j], rho_theta_space[i][j]);
		    				local_maxima_array.add(new LocalMaxima(i,j,rho_theta_space[i][j]));
		    			}
		    		}
		    	}
		    long stop_local_maximization = System.currentTimeMillis();
		    System.out.println("initialisation time(ms): "+(stop_initialization-start_initialization));
		    System.out.println("populating time(ms): "+(stop_populating-start_populating));
		    System.out.println("local maximization time(ms): "+(stop_local_maximization-start_local_maximization));
		    //for (LocalMaxima local_maxima: local_maxima_array){
		    //	System.out.println(local_maxima);
		    //}
		    //draw the found lines in a figure and store it  
		    draw();
		    
		    } catch (IOException e) {
		    	System.out.println(e.getMessage());
		}
		

	}
	static boolean is_local_maxima(int i, int j){
		boolean cond1=true,cond2=true,cond3=true,cond4=true,cond5=true,cond6=true,cond7=true,cond8=true;
		if (j>1)
			cond1=rho_theta_space[i][j] > rho_theta_space[i][j-1];
		if (i>1)
			cond2=rho_theta_space[i][j] >rho_theta_space[i-1][j];
		if (i<rho_theta_space.length-1)
			cond3=rho_theta_space[i][j] > rho_theta_space[i+1][j];
		if (j<rho_theta_space[i].length-1)
			cond4=rho_theta_space[i][j] > rho_theta_space[i][j+1];
		if (i<rho_theta_space.length-1 && j<rho_theta_space[i].length-1)
			cond5=rho_theta_space[i][j] > rho_theta_space[i+1][j+1];
		if (i>0 && j>0)
			cond6=rho_theta_space[i][j] > rho_theta_space[i-1][j-1];
		if (i<rho_theta_space.length-1 && j>0)
			cond7=rho_theta_space[i][j] > rho_theta_space[i+1][j-1];
		if(i>0 && j<rho_theta_space[i].length-1)
			cond8=rho_theta_space[i][j] > rho_theta_space[i-1][j+1];
			
		if (cond1 && cond2 && cond3 && cond4 && cond5 && cond6 && cond7 && cond8)
			return true;
		else return false;
	}
	static boolean is_local_maxima_rectangle(int i, int j, int r_rho, int r_theta){
		for (int i1=i-r_rho;i1<i+r_rho+1; i1++)
			for (int j1=j-r_theta;j1<j+r_theta+1;j1++)
				if (i1>=0 && i1<rho_theta_space.length && j1>=0 && j1 <rho_theta_space[i1].length)
					if (rho_theta_space[i][j]<rho_theta_space[i1][j1])
						return false; 
		return true; 
	}
	static class LocalMaxima{
    	int i; 
    	int j; 
    	int votes;
    	LocalMaxima(int i, int j, int votes){
    		this.i=i; 
    		this.j=j; 
    		this.votes=votes; 
    	}
    	public int hashCode(){
    		return votes;
    	}
    	public boolean equals(Object local_maxima){
    		LocalMaxima lm= (LocalMaxima)local_maxima;
    		if (Math.abs(this.i - lm.i) < rho_dim && Math.abs(this.j -lm.j) < theta_dim )
    		// if a local maxima is a neighbour of another local maxima it means that they are equal	
    			return true; 
    		else 
    			return false;
    	}
    	public String toString(){
    		return ("rho: "+rhos[i]+", theta: "+thetas[j]+", votes: "+votes);
    	}
    }
	static void draw(){
		BufferedImage img_reconstructed = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int [] rgbArray= new int[w*h];
		for (int p=0; p<rgbArray.length;p++)
			rgbArray[p]=0xffffffff;
		img_reconstructed.setRGB(0, 0, w, h, rgbArray, 0, w);
		for (LocalMaxima local_maxima: local_maxima_array)
			for (int x=0;x<w;x++){
				// calculate y 
				int y = (int) Math.round((rhos[local_maxima.i] - x*Math.cos(thetas[local_maxima.j]))
						/Math.sin(thetas[local_maxima.j]));
				// we can increase the thickness of the line by coloring the 8 pixels around
				// this helps when comparing the equality of two pictures tolerating some thickness difference
				for (int tx=x-2; tx<x+3;tx++)
					for (int ty=y-2; ty<y+3;ty++)
						if (ty>=0 && ty<h && tx>=0 && tx<w )
							img_reconstructed.setRGB(tx, ty, 0xff000000);
			}
		File outputfile = new File("img/singleLineReconstructed.png");
		try {
			ImageIO.write(img_reconstructed, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(" the two figures are equals with one pixel tolerance? " +
				equals_one_pixel_pre(img,img_reconstructed));
		System.out.println(" the two figures are equals? " +
				equals(img,img_reconstructed));
	}

	static boolean equals(BufferedImage one, BufferedImage two){
		// this method compares 1 black pixel of figure one to the same pixel 
		// in figure two, if it is not black the equality fails
		int[] rgbArray_one=new int[w*h];
		int[] rgbArray_two=new int[w*h];
		one.getRGB(0, 0, w, h, rgbArray_one, 0, w);
		two.getRGB(0, 0, w, h, rgbArray_two, 0, w);
		for (int p=0;p<w*h;p++)
			if (rgbArray_one[p]==0xff000000)
				if(rgbArray_two[p]!=0xff000000){
					return false;
				}
		return true;
	}
	static boolean equals_one_pixel_pre (BufferedImage one, BufferedImage two){
		// this method compares 1 black pixel of figure one to 5 pixels in figure two
		// same, right, left, up and done, if all of them are white the equality fails
		int[] rgbArray_one=new int[w*h];
		int[] rgbArray_two=new int[w*h];
		one.getRGB(0, 0, w, h, rgbArray_one, 0, w);
		two.getRGB(0, 0, w, h, rgbArray_two, 0, w);
		for (int p=0;p<w*h;p++)
			if (rgbArray_one[p]==0xff000000)
				if(rgbArray_two[p]!=0xff000000) 
					if (p+1 < rgbArray_one.length && rgbArray_two[p+1]!=0xff000000)
						if (p-1>0 && rgbArray_two[p-1]!=0xff000000)
							if (p+w < rgbArray_one.length && rgbArray_two[p+w]!=0xff000000)
				 				if (p-w>0 && rgbArray_two[p-w]!=0xff000000)
				 					return false;
		return true;
	}
}
