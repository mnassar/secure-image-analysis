import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;

import javax.imageio.ImageIO;


public class Client {

	/**
	 * @param args
	 */
	static int[][] img1;
	static int [][] img2;
	static int rho_step=100; // if bigger means less rhos; 
	static int theta_step=1000;  // if bigger means less thetas;
	static int nb_rhos;
	static int nb_thetas;
	
	public static void main(String[] args) {
		Client cl=new Client();
		String img_in="img/SingleLine.png";
		Random rand = new Random();
	    BufferedImage img=null;
		try {
			img = ImageIO.read(new File(img_in));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    System.out.println("Image to be analysed "+img_in);
	    long start_split = System.currentTimeMillis();
	    cl.split_image(img, rand);
	    long stop_split = System.currentTimeMillis();
	    System.out.println("time for image splitting (ms) "+(stop_split-start_split));
	    ServerA serverA = new ServerA();
	    ServerB serverB = new ServerB();
	  
	    
	    long start_populating = System.currentTimeMillis();
	    serverA.rho_theta_space = serverA.hough(img1,rho_step,theta_step);
		long stop_populating = System.currentTimeMillis();
	    System.out.println("SERVERA hough populating time(ms) "+(stop_populating-start_populating));
	    
	    start_populating = System.currentTimeMillis();
	    serverB.rho_theta_space = serverB.hough(img2,rho_step,theta_step);
	    stop_populating = System.currentTimeMillis();
	    System.out.println("SERVERB hough populating time(ms) "+(stop_populating-start_populating));
	    
	    long start_encryption= System.currentTimeMillis();
	    BigInteger e_rho_theta_spaceA[][]=serverA.encrypt_rho_theta_space();
	    long stop_encryption = System.currentTimeMillis();
	    System.out.println("SERVERA encryption rho_theta_space(ms) "+(stop_encryption-start_encryption));
	    
	    long start_blind= System.currentTimeMillis();
	    BigInteger e_va [][][] = serverB.blind_n_permute(e_rho_theta_spaceA, serverA.paillier);
	    long stop_blind= System.currentTimeMillis();
	    System.out.println("SERVERB blind(ms) "+(stop_blind-start_blind));
	    
	    long start_decryption= System.currentTimeMillis();
	    serverA.decrypt_va(e_va); 
	    long stop_decryption= System.currentTimeMillis();
	    System.out.println("SERVERA decrypt va(ms) "+(stop_decryption-start_decryption));
	    
	    start_encryption= System.currentTimeMillis();
	    BigInteger e_vb[][][]=serverB.encrypt_vb();
	    stop_encryption = System.currentTimeMillis();
	    System.out.println("SERVERB encryption vb(ms) "+(stop_encryption-start_encryption));
	    
	    start_blind= System.currentTimeMillis();
	    BigInteger e_wb [][][] = serverA.blind_n_permute(e_vb, serverB.paillier);
	    stop_blind= System.currentTimeMillis();
	    System.out.println("SERVERA blind(ms) "+(stop_blind-start_blind));
	    
	    start_decryption= System.currentTimeMillis();
	    serverB.decrypt_wb(e_wb); 
	    stop_decryption= System.currentTimeMillis();
	    System.out.println("SERVERB decrypt wb(ms) "+(stop_decryption-start_decryption));
	    
	    //test(serverA, serverB, e_va.length, e_va[0].length);
	    // garbled circuits
	    // The circuit is already prepared in MyCircuit/locmax.cir
	    serverA.store_wa();
	    serverB.store_wb();
	    garble();
	    	    
	}
	
	public void split_image(BufferedImage img, Random rand){
		int w = img.getWidth(null);
	    int h = img.getHeight(null);
	    int rho_max=(int) Math.sqrt(w*w+h*h);
	    int rho_min=-w;
	    double theta_min=Math.atan(Math.min(1./w,1./h));
	    nb_rhos=((rho_max-rho_min)/rho_step) +1;
	    nb_thetas=(int) (Math.PI/(theta_min*theta_step))+1;
	    
	    System.out.println("number of different rhos "+nb_rhos);
	    System.out.println("numbner of different thetas "+nb_thetas);
	    System.out.println("x_min " +img.getMinX()); 
	    System.out.println("y_min " +img.getMinY()); 
	    System.out.println("width "+img.getWidth()); 
	    System.out.println("height "+img.getHeight()); 
	    img1 = new int[w][h];
	    img2 = new int[w][h];
	    for (int x=0; x<w; x++){
	    	for (int y=0; y<h; y++){
	    		int p = img.getRGB(x, y);
	    		if (p==0xff000000)
	    			p=1;
	    		else
	    			p=0;
	    		// choose a max value suitable for blinding 
	    		//(e.g. 10 times the max value of the clear data)
	    		// here the clear data are 0 or 1 
	    		int r= rand.nextInt(10);
	    		img1[x][y]=r; 
	    		img2[x][y]=p-r; 
	    		
	    		//if (p!=-1){
	    		//System.out.println(p+" "+r+" "+(p-r));
	    			//System.out.println(0xff000000);
	    		//}
	    		
	    	}
	    }
	}
	static void test (ServerA serverA, ServerB serverB, int rhos, int thetas ){

	    
		BigInteger maxInt = new BigInteger(String.valueOf(Integer.MAX_VALUE));
		//System.out.println(maxInt);
		int nb_lines_above_th=0;
		int nb_lines=0;
		HashSet<Point> hashset= new HashSet<Point>();
		for (int i=0; i<rhos; i++){
			for (int j=0; j<thetas; j++){

				BigInteger a= serverA.wa[i][j][4];
				BigInteger b= serverB.wb[i][j][4];
				int ai; 
				if (a.max(maxInt).intValue()==Integer.MAX_VALUE){
					ai = Integer.parseInt(a.toString());
				}
				else{
					ai=-Integer.parseInt(a.multiply(new BigInteger("-1")).mod(serverA.paillier.n).toString());
				}
				int bi;
				if (b.max(maxInt).intValue()==Integer.MAX_VALUE){
					bi = Integer.parseInt(b.toString());
				}
				else{
					bi=-Integer.parseInt(b.multiply(new BigInteger("-1")).mod(serverB.paillier.n).toString());
				}
				int votes  = ai+bi;
				if (votes >100 ){
					nb_lines_above_th++;
					// is local maximum ? 
					int c=0;
					boolean local=true;
					for (BigInteger x: serverA.wa[i][j]){
						BigInteger y=serverB.wb[i][j][c++];
						int xi; 
						if (x.max(maxInt).intValue()==Integer.MAX_VALUE){
							xi = Integer.parseInt(x.toString());
						}
						else{
							xi=-Integer.parseInt(x.multiply(new BigInteger("-1")).mod(serverA.paillier.n).toString());
						}
						int yi;
						if (y.max(maxInt).intValue()==Integer.MAX_VALUE){
							yi = Integer.parseInt(y.toString());
						}
						else{
							yi=-Integer.parseInt(y.multiply(new BigInteger("-1")).mod(serverB.paillier.n).toString());
						}
						int nei  = xi+yi;
						if (votes < nei){
							local=false;
							break;
						}
					}
					if (local){
						System.out.format("%d %d %d\n",i,j,votes);
						nb_lines++;
						hashset.add(new Point(i,j,votes));
						//System.out.println("inital votes (before blinding) "+	(serverA.rho_theta_space[i][j]+serverB.rho_theta_space[i][j]));
					}
				}
				//System.out.format("                     votes:%d \n",votes);
			}
		}
		System.out.println("number of lines above threshold "+nb_lines_above_th);
		System.out.println("number of lines "+nb_lines);
		System.out.println("number of lines after tie break " + hashset.size());
	}
	
	static void garble(){

	    final Runtime runtime = Runtime.getRuntime();
	    final String classpath="-classpath .;bin;..\\GCParserModified\\dist\\GCParser.jar;" +
	    		"\"C:\\Program Files\\Java\\jdk1.7.0_04\\jre\\lib\\rt.jar\";" +
	    		"\"C:\\Users\\NASSAR\\workspace\\GCParserModified\\extlibs\\jargs.jar\";"+
	    		"\"C:\\Users\\NASSAR\\workspace\\GCParserModified\\extlibs\\commons-io-1.4.jar\";";
	    long start_garbling= System.currentTimeMillis();
	    Thread gcserver =  new Thread() {
	    	public void run() {

	    		try {
	    			// serverA starts the GC server 
	    			Process gcServer = runtime.exec("java -Xmx2048M " +
	    					classpath+
	    					" ServerA input/wa.ser");
	    			
	    			BufferedReader reader = new BufferedReader(new InputStreamReader(gcServer.getInputStream()));
	    			String line = "";
	    			
	    			while((line = reader.readLine()) != null) {
	    				System.out.println(line);
	    			} 
	    			reader.close();
	    		}catch (IOException  e) {
	    			e.printStackTrace();
	    		} 
	    	}
	    };
	    gcserver.start();
	    // here we can wait a secong before starting the client 
	    Thread gcclient =new Thread() {
	    	public void run() {
	    		try {
	    			// serverB starts the GC client 
	    			Process gcClient = runtime.exec(" java -Xmx2048M " +
	    					classpath +
	    					" ServerB input/wb.ser");
	    			
	    			BufferedReader reader = new BufferedReader(new InputStreamReader(gcClient.getInputStream()));
	    			String line = "";
	    			while((line = reader.readLine()) != null) {
	    				System.out.println(line);
	    			} 
	    			reader.close();
	    			
	    		}catch (IOException e) {
	    			e.printStackTrace();
	    		} 
	    	}
	    };
	    gcclient.start();
	    
	   
	    try {
	    	gcclient.join();
	    	gcserver.join();
		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    long stop_garbling= System.currentTimeMillis();
	    System.out.println("SERVERA/SERBERB Garbled Circuits time (ms) "+(stop_garbling-start_garbling));
	    System.out.println("Results are in file results/"+nb_rhos+"_"+nb_thetas+".txt");
	    int count =0; 
	    try {
	    	BufferedReader br=  new BufferedReader(new FileReader("results/"+nb_rhos+"_"+nb_thetas+".txt"));
	    	String line;
	    	while ((line = br.readLine())!=null){
	    		System.out.println(line);
	    		count++;
	    	}
	    } catch (IOException e) {
			e.printStackTrace();
		}
	    System.out.println("number of detected lines: "+count);
		
	    // ToDo; later 
	    // as output of garbled circuit, add the number of votes for local maximas  
	    // so we can do tie break based on number of votes

	}
}
