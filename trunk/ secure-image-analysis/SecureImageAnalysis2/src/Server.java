
public class Server {
	int [][] rho_theta_space;
	void hough(int[][] img, int rho_step, int theta_step){
		int w= img.length; 
		int h= img[0].length; 
	    int rho_max=(int) Math.sqrt(w*w+h*h);
	    int rho_min=-w;
	    double theta_min=Math.atan(Math.min(1./w,1./h));
	    int nb_rhos=((rho_max-rho_min)/rho_step) +1;
	    int nb_thetas=(int) (Math.PI/(theta_min*theta_step))+1;
	    System.out.println("number of different rhos "+nb_rhos);
	    System.out.println("numbner of different thetas "+nb_thetas);
		rho_theta_space=new int[nb_rhos][nb_thetas]; 
		for (int x=0; x<w; x++){
			for (int y=0; y<h; y++){
				int p = img[x][y];
				for (int t=0; t<nb_thetas; t++){
					double theta = t*theta_min*theta_step;
					if (theta >Math.PI)
						theta=Math.PI;
					int rho =  (int) Math.ceil(y*Math.sin(theta)+x*Math.cos(theta));
					rho_theta_space[(rho-rho_min)/rho_step][t]+=p;
				}
			}
		}
	}
}