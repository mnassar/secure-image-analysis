import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;

import javax.imageio.ImageIO;


public class Client {
	final String enc_zeros_file="shared/enc_zeros.txt";
	// to be used for encrypting the 1s in the image
	// by multiplying it by a fresh encryption of 0
	final BigInteger cnst_enc_1; 
	final BigInteger nsquare;
	//Paillier paillier; 
	// The client has an image 
	// he wants to encrypt using homomorphic encryption with 
	// the help of the homomorphic server 
	// The homomorphic server pre-generates an array of encrypted 0s 
	HomomorphicServer homom_server; 
	HoughServer hough_server;
	int rho_discretisation_coeff;
	int theta_discretisation_coeff;
	int threshold;
	int rho_radius;
	int theta_radius;
	int kernel_multiplier; 
	String results_path;
	String img_path;
	String img_out_path;
	
	public Client (int rho_discretisation_coeff, int theta_discretisation_coeff, int threshold, 
			int rho_radius, int theta_radius, int kernel_multiplier, 
			String results_path, String img_path, String img_out_path){
		 this.rho_discretisation_coeff =rho_discretisation_coeff  ;
		 this.theta_discretisation_coeff=theta_discretisation_coeff;
		 this.threshold=threshold;
		 this.rho_radius=rho_radius;
		 this.theta_radius=theta_radius;
		 this.kernel_multiplier=kernel_multiplier; 
		 this.results_path=results_path;
		 this.img_path=img_path;
		 this.img_out_path=img_out_path;
		 homom_server= new HomomorphicServer();
		 hough_server= new HoughServer(rho_radius, theta_radius, kernel_multiplier);
		 nsquare= homom_server.paillier.nsquare;
		 cnst_enc_1= homom_server.get_encrypted_one();
		 Point.rho_radius=rho_radius;
		 Point.theta_radius=theta_radius;
	
		 
	}
	public void run(){
		long start = System.currentTimeMillis();
		BigInteger[][] enc_img=encrypt_image(img_path);
		long end = System.currentTimeMillis();
		System.out.format("CLIENT time for image encryption %d ms\n", (end-start));
		// prepare some meta data 
		int w=enc_img.length;
		int h=enc_img[0].length;
		int rho_step=rho_discretisation_coeff*1;
		double theta_step=theta_discretisation_coeff*Math.atan(Math.min(1./w,1./h));
		int rho_min=-w;
		double theta_min=0; 
		System.out.println("CLIENT sends encrypted image to the hough server");
		BigInteger enc_0= homom_server.get_encrypted_zero();
		ShapesDataStructure enc_hashmap= 
				hough_server.accumulate_n_blur(enc_img,rho_step,theta_step,nsquare,enc_0);
		System.out.println("CLIENT received encrypted hashmap from the hough server");
		System.out.println("CLIENT sends encrypted hashmap to the homomoprphic server");
		HashSet<Point> local_maxima_array =homom_server.find_local_maximas(enc_hashmap,threshold);
		Iterator<Point> iterator = local_maxima_array.iterator();
		int count_lines=0;
		while (iterator.hasNext()){
			Point p= iterator.next();
			System.out.format("CLIENT %d) %d,%d:\t %d \t %.4f \t %d\n",
					++count_lines,p.i,p.j, rho_min+p.i*rho_step,theta_min+p.j*theta_step, p.votes);
		}
		System.out.println("CLIENT number of lines final: "+count_lines);
		// reconstruct image / shapes 
		draw(local_maxima_array, w, h, rho_min, rho_step, theta_min,theta_step);
	}
	
	public void draw(HashSet<Point> local_maxima_array, int w, int h, 
			int rho_min, int rho_step, double theta_min, double theta_step){
	
		BufferedImage img_reconstructed = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int [] rgbArray= new int[w*h];
		for (int p=0; p<rgbArray.length;p++)
			rgbArray[p]=0xffffffff;
		img_reconstructed.setRGB(0, 0, w, h, rgbArray, 0, w);
		Iterator<Point> iterator = local_maxima_array.iterator();
		while (iterator.hasNext()){
			Point p= iterator.next();
			int rho = rho_min+p.i*rho_step;
			double theta= theta_min+p.j*theta_step;
			// it theta between 1.56 and 1.58 we consider a line y=cte
			if (theta>(Math.PI/2)-0.01 && theta<(Math.PI/2)+0.01 && rho>=0 && rho<=h ){
				for (int x=0;x<w;x++){
					img_reconstructed.setRGB(x, rho, 0xff000000);
				}
			}
			
			else if ((theta<theta_step && rho>0 && rho<w) || 
					(theta>Math.PI - theta_step && rho<0 && rho >-w)) { //x=cte 
					for (int y=0;y<h;y++){
						img_reconstructed.setRGB(Math.abs(rho), y, 0xff000000);
					}
				}
			else {// normal case 
				for (int x=0;x<w;x++){
					// calculate y
					int y=(int) Math.ceil((rho - x*Math.cos(theta))/Math.sin(theta));
					// we can increase the thickness of the line by coloring the 8 pixels around
					// this helps when comparing the equality of two pictures tolerating some thickness difference
					//for (int tx=x-2; tx<x+3;tx++)
					//	for (int ty=y-2; ty<y+3;ty++)
					if (y>=0 && y<h && x>=0 && x<w )
						img_reconstructed.setRGB(x, y, 0xff000000);
				}
		}
					
				
		}
		File outputfile = new File(img_out_path);
		try {
			ImageIO.write(img_reconstructed, "png", outputfile);
			System.out.println("CLIENT the found lines are drawn to "+img_out_path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
	}
	public static void main (String[] args){
		//config variables
		
		String img_path="img/SingleLine.png";
		String img_out_path="img/SingleLineReconstructed.png";
		int rho_radius=1;
		int theta_radius=1;
		int threshold = 100;
		int theta_discretisation_coeff=100; //bigger means bigger theta step (less thetas)
		int rho_discretisation_coeff=100; // bigger means bigger rho step (less rhos)
		int kernel_multiplier=6;
		String results_path = null;
		new Client(rho_discretisation_coeff, theta_discretisation_coeff, threshold, 
									rho_radius, theta_radius, kernel_multiplier, 
									results_path, img_path, img_out_path).run();
		
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

	
	
}
