import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;




public class HoughServer {
	static int rho_radius;
	static int theta_radius;
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
	    System.out.println("HOUGH rho_range= "+rho_min+":"+rho_step+":"+rho_max+"  -->  "+((rho_max-rho_min)/rho_step+1));
	    System.out.format("HOUGH theta_range= %.0f:%.4f:3.14  -->  %d\n",theta_min,theta_step,(int)(theta_max/theta_step)+1);
	    int nb_rhos= ((rho_max-rho_min)/rho_step)+1;
	    int nb_thetas= (int)(theta_max/theta_step) +1;
	    BigInteger [][] rho_theta_space = new BigInteger[nb_rhos][nb_thetas];
	    for (int i=0;i< nb_rhos;i++)
	    	for (int j=0; j< nb_thetas; j++)
	    		rho_theta_space[i][j]=BigInteger.ONE;
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
	

	public HashMap<Point,LinkedList<BigInteger>> accumulate_n_blur (BigInteger [][] enc_img, 
			int rho_step, double theta_step, BigInteger nsquare){
		BigInteger[][] rho_theta_space=accumulate (enc_img,rho_step,theta_step,nsquare);
		//int w=enc_img.length;
		//int h=enc_img[0].length;
		long start_blurring = System.currentTimeMillis();
		Blur b=new Blur();
		BigInteger blurred[][] = b.filter(rho_theta_space, nsquare);
		long stop_blurring = System.currentTimeMillis();
		System.out.println("HOUGH blurring (with a kernel of radius 2) time(ms): "+(stop_blurring-start_blurring));
		// calculate the differences and put them in the data structure
		// the idea is to use blurring for some points and the original for other points 
		// in such a way to hinder the jig-saw attacks
		System.out.println("HOUGH calculating differences radius_rho= "+rho_radius+ " ,radius_theta= "+ theta_radius);
		long start_calculating_differences = System.currentTimeMillis();
		HashMap <Point, LinkedList<BigInteger>> points_differences = new HashMap<Point, LinkedList<BigInteger>>();
		BigInteger minusOne= new BigInteger("-1");
		int w=rho_theta_space.length;
		int h=rho_theta_space[0].length;
		for (int i=0; i<w; i++)
			for (int j=0; j<h; j++){
				Point p = new Point(i,j,rho_theta_space[i][j]);
				LinkedList<BigInteger >differences=new LinkedList<BigInteger>();
				//size = (2*rho_radius+1)*(2*theta_radius+1)-1);
				if((i+j)%2==0){
					for (int i1=i-rho_radius;i1<i+rho_radius+1; i1++)
						for (int j1=j-theta_radius;j1<j+theta_radius+1;j1++)
							if (i1>=0 && i1<rho_theta_space.length && j1>=0 && j1 <rho_theta_space[i1].length 
							&& !(i1==i && j1==j)){
								//differences.append( rho_theta_space[i][j]-rho_theta_space[i1][j1]);
								BigInteger c= blurred[i1][j1].modPow(minusOne, nsquare);
								differences.add( blurred[i][j].multiply(c).mod(nsquare) );
							}
				}
				else{
					for (int i1=i-rho_radius;i1<i+rho_radius+1; i1++)
						for (int j1=j-theta_radius;j1<j+theta_radius+1;j1++)
							if (i1>=0 && i1<rho_theta_space.length && j1>=0 && j1 <rho_theta_space[i1].length 
							&& !(i1==i && j1==j)){
								//differences.append( rho_theta_space[i][j]-rho_theta_space[i1][j1]);
								BigInteger c= rho_theta_space[i1][j1].modPow(minusOne, nsquare);
								differences.add( rho_theta_space[i][j].multiply(c).mod(nsquare) );
							}
				}
					
				points_differences.put(p, differences);
			}
		long stop_calculating_differences = System.currentTimeMillis();
		System.out.println("HOUGH calculating differences done, time(ms): "+
				(stop_calculating_differences-start_calculating_differences));
		return points_differences;
		}
}
