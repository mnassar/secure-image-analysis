import java.math.BigInteger;




public class HoughServer {
	static int rho_radius;
	static int theta_radius;
	public static void main(String[] args) {

	}

	public BigInteger[][] accumulate (BigInteger [][] enc_img, 
			int rho_step, double theta_step, BigInteger nsquare, BigInteger enc_0){
	
		
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
	    int nb_thetas= (int)(theta_max/theta_step)+1;
	    
	    BigInteger [][] rho_theta_space = new BigInteger[nb_rhos][nb_thetas];
	    for (int i=0;i< nb_rhos;i++)
	    	for (int j=0; j< nb_thetas; j++)
	    		rho_theta_space[i][j]=enc_0;
	    		//rho_theta_space[i][j]=BigInteger.ONE;
	    long stop_initialization = System.currentTimeMillis();
	    System.out.println("HOUGH initialisation time(ms): "+(stop_initialization-start_initialization));
	    //accumulate
	    long start_populating = System.currentTimeMillis();
	    for (int x=0; x<w; x++)
	    	for (int y=0; y<h; y++){
	    		// Get a pixel
	    		BigInteger p = enc_img[x][y];
	    		//System.out.println(p);
	    		int theta_index=0;
	    		
    			while (theta_index<nb_thetas){
    				double t=theta_min+ theta_index*theta_step;
    				if (t>Math.PI)
    					t=Math.PI;
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
    			 //for (int i=0;i< nb_rhos;i++)
    			   // 	for (int j=0; j< nb_thetas; j++)
    			    //		System.out.println(i+" "+j+" "+rho_theta_space[i][j]);
    			    		//System.out.println(theta_index +" "+nb_thetas);
    			
	    	}
	    long stop_populating = System.currentTimeMillis();
	    System.out.println("HOUGH populating time(ms): "+(stop_populating-start_populating));
	    
	    //for (int x=0; x<nb_rhos; x++)
	    //	for (int y=0; y<nb_thetas; y++)
	    //		System.out.println(rho_theta_space[x][y]);
	    return rho_theta_space;
	}
	

	public ShapesDataStructure accumulate_n_blur (BigInteger [][] enc_img, 
			int rho_step, double theta_step, BigInteger nsquare, BigInteger enc_0){
		BigInteger[][] rho_theta_space=accumulate (enc_img,rho_step,theta_step,nsquare, enc_0);
		//int w=enc_img.length;
		//int h=enc_img[0].length;
		long start_blurring = System.currentTimeMillis();
		Blur b=new Blur();
		BigInteger blurred[][] = b.filter(rho_theta_space, nsquare, enc_0);
		long stop_blurring = System.currentTimeMillis();
		System.out.println("HOUGH blurring (with a kernel of radius 2) time(ms): "+(stop_blurring-start_blurring));
		// calculate the differences and put them in the data structure
		// the idea is to use blurring for some points and the original for other points 
		// in such a way to hinder the jig-saw attacks
		System.out.println("HOUGH calculating differences radius_rho= "+rho_radius+ " ,radius_theta= "+ theta_radius);
		
		BigInteger minusOne= new BigInteger("-1");
		int ws=rho_theta_space.length;
		int hs=rho_theta_space[0].length;
		// preparing the data structure for storing the differences
		long start_preparating_differences = System.currentTimeMillis();
		BigInteger [][] points_differences  = new BigInteger [ws*hs][((2*rho_radius+1)*(2*theta_radius+1))-1];
		Point points [] = new Point [ws*hs];
		long stop_preparating_differences = System.currentTimeMillis();
		System.out.println("HOUGH preparating data structure for differences, time(ms): "+
				(stop_preparating_differences- start_preparating_differences));

	    //for (int x=0; x<rho_theta_space.length; x++)
	    //	for (int y=0; y<rho_theta_space[0].length; y++)
	    //		System.out.println(rho_theta_space[x][y]);
		long start_calculating_differences = System.currentTimeMillis();
		int count=0;
		
		
		for (int i=0; i<ws; i++)
			for (int j=0; j<hs; j++){
				Point p = new Point(i,j,rho_theta_space[i][j]);
				points[count]=p;
				//size = (2*rho_radius+1)*(2*theta_radius+1)-1);
				if((i+j)%2==0){
					int counta=0;
					for (int i1=i-rho_radius;i1<i+rho_radius+1; i1++)
						for (int j1=j-theta_radius;j1<j+theta_radius+1;j1++)
							if (i1>=0 && i1<ws && j1>=0 && j1 <hs && !(i1==i && j1==j)){
								//differences.append( rho_theta_space[i][j]-rho_theta_space[i1][j1]);
								if (!blurred[i1][j1].equals(blurred[i][j])){
									BigInteger c= blurred[i1][j1].modPow(minusOne, nsquare);
									points_differences[count][counta]=blurred[i][j].multiply(c).mod(nsquare);
									//System.out.println(points_differences[count][counta]);//==BigInteger.ONE){
								}
								//System.out.println(ws);
								//System.out.println(hs);	
								//System.out.println(points_differences[count][counta]);//==BigInteger.ONE){
								//System.out.println(i+" "+j+" "+rho_theta_space[i][j]);
								//System.out.println(i1+" "+j1+" "+rho_theta_space[i1][j1]);
								//System.out.println();
								//}
								counta++;
							}
				}
				else{
					int counta=0;
					for (int i1=i-rho_radius;i1<i+rho_radius+1; i1++)
						for (int j1=j-theta_radius;j1<j+theta_radius+1;j1++)
							if (i1>=0 && i1<ws && j1>=0 && j1 <hs && !(i1==i && j1==j)){
								//differences.append( rho_theta_space[i][j]-rho_theta_space[i1][j1]);
								if (!rho_theta_space[i1][j1].equals(rho_theta_space[i][j])){
									BigInteger c= rho_theta_space[i1][j1].modPow(minusOne, nsquare);
									points_differences[count][counta]=rho_theta_space[i][j].multiply(c).mod(nsquare);
									//System.out.println(points_differences[count][counta]);
								}
								counta++;
							}
							
				}
					
				//for (BigInteger g: points_differences[count])
				//	if (g!=null)
				//		System.out.println(g);
				count++;
				//points_differences.put(p, differences);
			}
		long stop_calculating_differences = System.currentTimeMillis();
		System.out.println("HOUGH calculating differences done, time(ms): "+
				(stop_calculating_differences-start_calculating_differences));
		return new ShapesDataStructure(points, points_differences);
		}
}
