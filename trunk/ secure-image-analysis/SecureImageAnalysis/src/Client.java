import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;




public class Client {
	//config variables
	static String enc_zeros_file="shared/enc_zeros.txt";
	static String img_path="img/SingleLine.png";
	static int rho_radius=1;
	static int theta_radius=1;
	static int threshold = 100;
	static int theta_discretisation_coeff=1000;
	static int rho_discretisation_coeff=1;
	static int kernel_multiplier=6;
	
	HomomorphicServer homom_server; 
	HoughServer hough_server;
	BigInteger nsquare;
	// to be used for encrypting the 1s in the image
	// by multiplying it by a fresh encryption of 0
	final BigInteger cnst_enc_1; 
	//Paillier paillier; 
	// The client has an image 
	// he wants to encrypt using homomorphic encryption with 
	// the help of the homomorphic server 
	// The homomorphic server pre-generates an array of encrypted 0s 
	public Client (){
		 homom_server= new HomomorphicServer();
		 hough_server= new HoughServer();
		 nsquare=homom_server.paillier.nsquare;
		 cnst_enc_1= homom_server.get_encrypted_one();
		 Point.rho_radius=rho_radius;
		 Point.theta_radius=theta_radius;
		 HoughServer.rho_radius=rho_radius;
		 HoughServer.theta_radius=theta_radius;
		 Blur.kernel_multiplier= kernel_multiplier;
		 //paillier = new Paillier();
	}
	public static void main(String [] args){
		Client client = new Client();
		long start = System.currentTimeMillis();
		BigInteger[][] enc_img=client.encrypt_image(img_path);
		long end = System.currentTimeMillis();
		System.out.format("CLIENT time for image encryption %d ms\n", (end-start));
		
		
		
		/*
		 BigInteger [][] enc_A=client.hough_server.accumulate(enc_img,rho_step,theta_step, 
				client.nsquare);
		
		//System.out.println("main"+enc_A[0][0]);
		//decrypt the accumulator 
		
		System.out.println("CLIENT receives encrypted accumulator from the hough server");
		System.out.println("CLIENT //todo: permute enc_A");
		System.out.println("CLIENT sends encrypted accumulator to the homomoprphic server");
		int[][] A = client.homom_server.decrypt_A(enc_A);
		System.out.println("CLIENT receives decrypted accumulator from the homomorphic server");
		System.out.println("CLIENT //todo: reverse permutation");
		//for(int i=0;i<A.length;i++){
		//	for(int j=0;j<A[0].length;j++)
		//		System.out.print(A[i][j]+" ");
		//	System.out.println();	
		//}
		//thresholding and local maximization 
		long start_local_maximization = System.currentTimeMillis();
	    int count_lines=0;
		double threshold=0.7;
	    int thresh = (int) (threshold * maxValue(A));
	    System.out.println("CLIENT local maximization");
	    HashSet<LocalMaxima> local_maxima_array = new HashSet<LocalMaxima>();
	    System.out.println("CLIENT rho_local_maximization_radius "+rho_radius);
	    System.out.println("CLIENT theta_local_maximization_radius "+theta_radius);
	    System.out.println("CLIENT threshold_votes "+thresh);
	    System.out.println("CLIENT Detected straight lines:");
	    System.out.println("CLIENT i,j: \t\t rho \t theta \t\t votes");
	    for (int i=0;i< A.length;i++)
	    	for (int j=0; j< A[0].length; j++){
	    		// thresholding + local maximisation 
	    		if (A[i][j]>thresh && is_local_maxima_rectangle(A,i,j,rho_radius, theta_radius)){
	    			if (!local_maxima_array.contains(new LocalMaxima(i,j,A[i][j]))){
	    				System.out.format("CLIENT %d) %d,%d:\t %d \t %.4f \t %d\n",
	    					++count_lines,i,j, rho_min+i*rho_step,theta_min+j*theta_step, A[i][j]);
	    				local_maxima_array.add(new LocalMaxima(i,j,A[i][j]));
	    			}
	    		}
	    	}
	    long stop_local_maximization = System.currentTimeMillis();
	    System.out.println("CLIENT local maximization time(ms): "
	    		+(stop_local_maximization-start_local_maximization));
		 */

		// prepare some meta data 
		int w=enc_img.length;
		int h=enc_img[0].length;
		int rho_step=rho_discretisation_coeff*1;
		double theta_step=theta_discretisation_coeff*Math.atan(Math.min(1./w,1./h));
		int rho_min=-w;
		double theta_min=0; 
		System.out.println("CLIENT sends encrypted image to the hough server");
		HashMap<Point, LinkedList<BigInteger>> enc_hashmap=(HashMap<Point, LinkedList<BigInteger>>) 
				client.hough_server.accumulate_n_blur(enc_img,rho_step,theta_step,client.nsquare);
		System.out.println("CLIENT received encrypted hashmap from the hough server");
		System.out.println("CLIENT sends encrypted hashmap to the homomoprphic server");
		HashSet<Point> local_maxima_array =client.homom_server.find_local_maximas(enc_hashmap,threshold);
		Iterator<Point> iterator = local_maxima_array.iterator();
		int count_lines=0;
		while (iterator.hasNext()){
			Point p= iterator.next();
			System.out.format("CLIENT %d) %d,%d:\t %d \t %.4f \t %d\n",
					++count_lines,p.i,p.j, rho_min+p.i*rho_step,theta_min+p.j*theta_step, p.votes);
		}
	}
	
	BigInteger[][] encrypt_image(String img_in){
		
		BufferedImage img = null;
		BigInteger[][] enc_img=null;
		try {
			System.out.println("CLIENT Image to be analysed "+img_in);
			System.out.println("CLIENT encrypts the image using pre-computed zeros and a constant one");
			img=ImageIO.read(new File(img_in));
			int w = img.getWidth(null);
			int h = img.getHeight(null);
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(enc_zeros_file)));
			
			//BigInteger enc_0 = homom_server.get_encrypted_zero();
			//BigInteger enc_1 = paillier.Encryption(new BigInteger("1"));
			//BigInteger enc_0 = paillier.Encryption(new BigInteger("0"));
			//System.out.println(enc_1);
			//System.out.println(enc_0);
			//BigInteger[] enc_0s= new BigInteger [(int) (h*w)];
			//enc_0s=homom_server.get_encrypted_zeros(h*w);
			// only one encrypted one is sufficient 
			enc_img=new BigInteger[w][h];
			for (int x=0; x<w; x++)
				for (int y=0; y<h; y++){
					// Get a pixel
					int p = img.getRGB(x, y);
					String line = br.readLine();
					//System.out.println(line+" end");
					BigInteger enc_0= new BigInteger(line);
					if (p!=-1){ //p==-1 is for white (0xffffffff) white is the background (0s)
						//System.out.format("pixel %x\n",p);
						enc_img[x][y]= cnst_enc_1.multiply(enc_0).mod(nsquare);
						//enc_img[x][y] = enc_1;
						//enc_img[x][y] = new BigInteger("1");
					}
					else {
						enc_img[x][y] = enc_0;
						//enc_img[x][y] = new BigInteger("0");
					}
				}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}; 
		return enc_img;
	}

	static boolean is_local_maxima_rectangle(int [][] A, int i, int j, int r_rho, int r_theta){
		for (int i1=i-r_rho;i1<i+r_rho+1; i1++)
			for (int j1=j-r_theta;j1<j+r_theta+1;j1++)
				if (i1>=0 && i1<A.length && j1>=0 && j1 <A[i1].length && !(i1==i && j1==j))
					if (A[i][j]<A[i1][j1])
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
    		if (Math.abs(this.i - lm.i) < rho_radius && Math.abs(this.j -lm.j) < theta_radius )
    		// if a local maxima is a neighbour of another local maxima it means that they are equal	
    			return true; 
    		else 
    			return false;
    	}
    	public String toString(){
    		return ("rho index: "+i+", theta index: "+j+", votes: "+votes);
    	}
    }
	
	private static int maxValue(int[][] a) {
	    int max = a[0][0];
	    for (int i = 0; i < a.length; i++)
	    	for (int j = 0; j < a[0].length; j++){
	            if (a[i][j] > max) {
	                    max = a[i][j];
	            }
	    }
	    return max;
	}
}
