//import java.awt.Image;
import java.awt.image.BufferedImage;
//import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Hough {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Reading an image file 
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File("img/singleLine.png"));
		    int w = img.getWidth(null);
		    int h = img.getHeight(null);
		    System.out.println("x_min " +img.getMinX()); 
		    System.out.println("y_min " +img.getMinY()); 
		    System.out.println("width "+img.getWidth()); 
		    System.out.println("height "+img.getHeight()); 
		    
		    //get all the rgbs: 
		    //int[] rgbs = new int[img.getWidth()*img.getHeight()];
		    //img.getRGB(img.getMinX(), img.getMinY(), w, h, rgbs, 0, w);
		    //int count =0;
		    
		    // populate the rhos and thetas 
		    int rho_step=1; 
		    int rho_max=(int) Math.sqrt(w*w+h*h);
		    double theta_step=Math.atan(Math.min(1./w,1./h));
		    double theta_max=Math.PI;
		    System.out.println("rho_step "+rho_step);
		    System.out.println("rho_max "+rho_max);
		    System.out.println("theta_min "+theta_step);
		    System.out.println("theta_max "+theta_max);
		    int [] rhos = new int [2*rho_max+1];
		    double [] thetas = new double [(int) Math.ceil(theta_max/theta_step)+1];
		    for (int i=0;i< rhos.length;i++){
		    	rhos[i]=-rho_max+i*rho_step;
		    	//System.out.println(rhos[i]);
		    }
		    for (int i=0;i< thetas.length;i++){
		    	thetas[i]=i*theta_step;
		    	//System.out.println(thetas[i]+" "+ Math.cos(thetas[i])+" "+Math.sin(thetas[i]));
		    }
		    thetas[thetas.length-1]=Math.PI;
		    //System.out.println(Math.PI);
		    int [][] rho_theta_space = new int[rhos.length][thetas.length];
		    for (int i=0;i< rhos.length;i++)
		    	for (int j=0; j< thetas.length; j++)
		    		rho_theta_space[i][j]=0;
		    
		    //look for lines
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
		    				
		    				
		    				rho_theta_space[rho+rho_max][t]++;
		    			}
		    		}
		    	}
		    
		    //output
		    for (int i=0;i< rhos.length;i++)
		    	for (int j=0; j< thetas.length; j++)
		    		if (rho_theta_space[i][j]>300)
		    			System.out.println(" " + (i-rho_max) + " " + thetas[j] + " : "+ rho_theta_space[i][j]);
		    		
		    		
		    
		    		
		    //System.out.println(count);
		    //System.out.println(rgbs.length);
		    } catch (IOException e) {
		    	System.out.println(e.getMessage());
		}
		

	}

}
