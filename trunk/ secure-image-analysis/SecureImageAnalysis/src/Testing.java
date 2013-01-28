import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/*this class run a client with 
 * different set of configuration parameters 
 * and output to a different file and figure
 * The encryption/decryption parameters are set in the Paillier class 
 * We also assume large number of pre-encrypted zeros are in the file shared/enc_zeros.txt
 * this file helps the client encrypting the image
 */
public class Testing {

	/**
	 * @param args
	 */
	//config variables
	
	
	public static void main(String[] args) {
		String img_path="img/SingleLine.png";
		System.out.println("start");
		// kernel_multiplier is a parameter for blurring (to transform 
		//blurring coefficients from floats into integers
		int kernel_multiplier=6; 
		long start = System.currentTimeMillis();
		int exp_nb=0;
		for (int rho_discretisation_coeff=100; 
				rho_discretisation_coeff>=1 ;
				rho_discretisation_coeff/=10){
			for (int theta_discretisation_coeff=100;
					theta_discretisation_coeff>=1;
					theta_discretisation_coeff/=10){
				for (int threshold=300; threshold>=100; threshold-=100){
					for (int rho_radius: new int[]{1,2,3}){
						int theta_radius=rho_radius;
						System.out.println("experiment nb: "+exp_nb+"/81");
						System.out.format("%d %d %d %d\n", rho_discretisation_coeff, theta_discretisation_coeff, threshold, rho_radius);
						//output file: 
						String out_file="out/exp"+exp_nb+".txt";
						//output image: 
						String img_out_path="img/SingleLineReconstructed"+exp_nb+".png";
						Client cl = new Client(rho_discretisation_coeff, theta_discretisation_coeff, threshold, 
								rho_radius, theta_radius, kernel_multiplier, out_file, img_path, img_out_path);
						// redirect the standard output to out_file
						try {
							PrintStream ps = new PrintStream(new FileOutputStream(out_file));
							PrintStream both_ps= new TeeStream(
									ps,
									System.out);
							System.setOut(both_ps);
							cl.run();
							ps.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						exp_nb++;		
					}
				}
			}
		}
		long end = System.currentTimeMillis();
		//System.setOut(System.out);
		System.out.println("total experiments time (s): "+ ((end-start)/1000));
	}
}