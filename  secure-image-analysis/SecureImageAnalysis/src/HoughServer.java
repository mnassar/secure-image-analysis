import java.math.BigInteger;




public class HoughServer {
	public static void main(String[] args) {

	}

	public BigInteger[][] accumulate (BigInteger [][] enc_img, 
			int rho_step, double theta_step, BigInteger nsquare){
	
		
		System.out.println("HOUGH accumulating ... ");
		long start_initialization = System.currentTimeMillis();
	    // initialize and discretize the rhos and thetas 
		int w=enc_img.length;
		int h=enc_img[0].length;
	    int rho_min=-w;
	    int rho_max=(int) Math.sqrt(w*w+h*h);
	    double theta_min=0;
	    double theta_max=Math.PI; 
	    System.out.println("HOUGH rho_range= "+rho_min+":"+rho_step+":"+rho_max+"  -->  "+(rho_max-rho_min)/rho_step);
	    System.out.format("HOUGH theta_range= %.0f:%.4f:3.14  -->  %d\n",theta_min,theta_step,(int)(theta_max/theta_step));
	    int nb_rhos= ((rho_max-rho_min)/rho_step)+1;
	    int nb_thetas= (int)(theta_max/theta_step) +1;
	    BigInteger [][] rho_theta_space = new BigInteger[nb_rhos][nb_thetas];
	    for (int i=0;i< nb_rhos;i++)
	    	for (int j=0; j< nb_thetas; j++)
	    		rho_theta_space[i][j]=new BigInteger("1");
	    long stop_initialization = System.currentTimeMillis();
	    System.out.println("HOUGH initialisation time(ms): "+(stop_initialization-start_initialization));
	    //accumulate
	    long start_populating = System.currentTimeMillis();
	    for (int x=0; x<w; x++)
	    	for (int y=0; y<h; y++){
	    		// Get a pixel
	    		BigInteger p = enc_img[x][y];
	    		int theta_index=0;	
    			for (double t=theta_min; t<theta_max; t=t+theta_step){
    				//calculate rho 
    				int rho =  (int) Math.ceil(y*Math.sin(t)+x*Math.cos(t));
    				//System.out.println(rho_min+" "+ rho_max+" "+ rho+" "+t);
    				//System.out.println(rho);
    				//increment the rho_tetha_space
    				int rho_index=(rho-rho_min)/rho_step;
    				rho_theta_space[rho_index][theta_index]=
    						rho_theta_space[rho_index][theta_index].multiply(p).mod(nsquare);
    				//System.out.println(p);
    				//System.out.println(rho_index+" "+theta_index+" "+rho_theta_space[rho_index][theta_index]);
    				//rho_theta_space[rho_index][theta_index]=rho_theta_space[rho_index][theta_index].add(p);
    				theta_index++;
    			}
    			//System.out.println(theta_index +" "+nb_thetas);
	    	}
	    long stop_populating = System.currentTimeMillis();
	    System.out.println("HOUGH populating time(ms): "+(stop_populating-start_populating));
	    return rho_theta_space;
	}
}
