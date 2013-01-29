import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

import Program.GCParserCommon;
import Program.GCParserServer;
import Program.PrivateInputProvider;
import Program.PrivateInputsFile;
import YaoGC.Wire;



public class ServerA extends Server {
	static final BigInteger maxInt = new BigInteger(String.valueOf(Integer.MAX_VALUE));
	static BigInteger paillier_n;
	int[][] rho_theta_space;
	Paillier paillier;
	Random rand;
	BigInteger [][][] va;
	BigInteger [][][] wa;
	public ServerA(){
		rand=new Random();
		paillier = new Paillier();
        System.out.println("SERVERA Paillier public key: ");
        System.out.println("  n: "+paillier.n);
        System.out.println("  nsquare: "+paillier.nsquare);
        System.out.println("  g: "+paillier.getG());
        	
	}
	

	public BigInteger[][] encrypt_rho_theta_space() {
		// encrypt the rho theta space using Paillier
		
		int nb_rhos=rho_theta_space.length;
		int nb_thetas=rho_theta_space[0].length;
		BigInteger e_rho_theta_space[][]=new BigInteger[nb_rhos][nb_thetas];
		for (int i=0; i<nb_rhos;i++){
			for (int j=0; j<nb_thetas;j++){
				BigInteger p= new BigInteger(String.valueOf(rho_theta_space[i][j]));
				e_rho_theta_space[i][j]= this.paillier.Encryption(p);
			}
		}
		return e_rho_theta_space;
	}
	public void decrypt_va(BigInteger[][][] e_va) {
		int rhos = e_va.length; 
		int thetas = e_va[0].length;
		va = new BigInteger[rhos][thetas][9];
		for (int i=0; i<rhos; i++){
			for (int j=0; j<thetas; j++){
				int c=0;
				for (BigInteger x: e_va[i][j]){
					va[i][j][c]=paillier.Decryption(x);
					c++;
				}
					
			}
		}
	}
	public BigInteger[][][] blind_n_permute(BigInteger[][][] e_vb,
			Paillier paillierB) {
		int rhos = e_vb.length;
		int thetas = e_vb[0].length;
		wa= new BigInteger [rhos][thetas][9]; //this will stay at server A 
		BigInteger [][][] e_wb = new BigInteger [rhos][thetas][9]; // this will be transferred back to B 
		for (int i=0; i<rhos; i++){
			for (int j=0; j<thetas; j++){
				// choose r
				int r = rand.nextInt(10);
				BigInteger big_r = new BigInteger (String.valueOf(r));
				BigInteger minus_r = new BigInteger(String.valueOf(-r));
				// calculate the encryption of r with key of A 
				BigInteger e_r =paillierB.Encryption(big_r);
				int c=0;
				for (BigInteger x: e_vb[i][j]){
					e_wb[i][j][c]=x.multiply(e_r).mod(paillierB.nsquare);
					wa[i][j][c]=va[i][j][c].add(minus_r).mod(paillier.n);
					c++;
				}
			}
		}
		return e_wb;
		
	}
	void store_wa(){
		try
	      {
	         FileOutputStream fileOut =
	         new FileOutputStream("input/wa.ser");
	         ObjectOutputStream out =
	                            new ObjectOutputStream(fileOut);
	         out.writeObject(wa);
	         out.writeObject(paillier.n);
	         out.close();
	         fileOut.close();
	      }catch(IOException i)
	      {
	          i.printStackTrace();
	      }
	}

	public static void main(String [] args) {
		BigInteger [][][] wa=null; 
		
		try
	      {
	         FileInputStream fileIn =
	                          new FileInputStream("input/wa.ser");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         wa = (BigInteger[][][]) in.readObject();
	         paillier_n=(BigInteger)in.readObject();
	         System.out.println(paillier_n);
	         in.close();
	         fileIn.close();
	      }catch(IOException e1)
	      {
	         e1.printStackTrace();
	      }catch(ClassNotFoundException e2)
	      {
	          e2.printStackTrace();
	       }
		// prepare the GC 
		Wire.labelBitLength = 80;
	    PrivateInputProvider pip;
	    File cirFile = new File( "MyCircuits/locmax.cir" );
	    GCParserCommon com = new GCParserCommon(cirFile,null);
	    GCParserServer server = new GCParserServer(com);
	    String detected_lines = "";
	    for(int i = 0; i < wa.length; i++){
	    	for (int j=0; j< wa[0].length;j++){
	    		int[] wai = grab_int(wa[i][j]);
	    		/*System.out.println(i+" "+j);
	    		for (int x: wai)
	    			System.out.print(x+" ");
	    		System.out.println();*/
	    		try {
	    			File temp = new File("input/inputA.txt");
	    			BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
	    			String input=""; 
	    			for (int c=0;c<9 ;c++)
	    				input+=String.format("a%d %d\r\n",(c+1),wai[c]);
	    			bw.write(input);
	    			bw.close();
	    			com.setPIP(new PrivateInputsFile(new FileInputStream(temp)));
	    			server = new GCParserServer(com);
	    			server.run();
	    			HashMap<String, BigInteger> output = (HashMap<String, BigInteger>) server.getOutputValues();
	    			
	    			if (output.get("flag").intValue()==1){
	    				detected_lines +=(i+ " "+j+"\r\n");
	    			}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		} 
	    	}
	    }
	    // write the results to the file 
		try {
			File results =  new File("results/"+wa.length+"_"+wa[0].length+".txt");
			BufferedWriter rw = new BufferedWriter(new FileWriter(results));
			rw.write(detected_lines);
			rw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private static int[] grab_int(BigInteger[] wa_i_j) {
		int []int_wa = new int [9];
		int c=0;
		for (BigInteger big: wa_i_j){
			if (big.max(maxInt).intValue()==Integer.MAX_VALUE){
				int_wa[c] = Integer.parseInt(big.toString());
			}
			else{
				int_wa[c]=-Integer.parseInt(big.multiply(new BigInteger("-1")).mod(paillier_n).toString());
			}
			//System.out.println(int_wa[c]);
			c++;
			
		}
		
		return int_wa;
	}
}
