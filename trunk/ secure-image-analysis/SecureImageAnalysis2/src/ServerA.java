import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
				// choose r from a big enough range (here the same range as chosen in serverB)
				int r = rand.nextInt((int)Math.pow(10,7));
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
	static int rhos; 
	static int thetas; 
	static int threshold=100;
	public static void main(String [] args) {
		
		// prepare the gc input 
		File inputA =  create_gc_input_file();
		PrivateInputProvider pip=null;
		try {
			pip = new PrivateInputsFile(new FileInputStream(inputA));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		// prepare the fc circuit 
		Wire.labelBitLength = 80;
	    // create a circuit with a suitable size for inputs 
	    File cirFile = create_gc_circuit(); 
	   
	    
	    long start_garbling= System.currentTimeMillis();
	    GCParserCommon com = new GCParserCommon(cirFile,pip);
	    GCParserServer server = new GCParserServer(com);
	    String detected_lines = "";
	    server.run();
	    HashMap<String, BigInteger> output;
		try {
			output = (HashMap<String, BigInteger>) server.getOutputValues();
			 for (String flag: output.keySet()){
			    	if (output.get(flag).intValue()==1){
			    		detected_lines +=(flag+"\r\n");	
			    	}
			    }
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	    long stop_garbling= System.currentTimeMillis();
	    // write the results to the file 
		try {
			File results =  new File("results/"+rhos+"_"+thetas+".txt");
			BufferedWriter rw = new BufferedWriter(new FileWriter(results));
			rw.write(detected_lines);
			rw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	   
		System.out.println("SERVERA Garbled Circuits time (ms) "+(stop_garbling-start_garbling));
		System.out.println("Results are in file results/"+rhos+"_"+thetas+".txt");
		
	}
	


	private static File create_gc_circuit() {
		File circuit = new File( "MyCircuits/locmax.cir" );
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(circuit));

			for (int i=0; i<rhos ;i++){
				for (int j=0; j<thetas; j++){
					for (int c=0;c<9;c++){
						bw.write(String.format(".input b%ds%ds%d 1 64\r\n",i,j,c)); 
						bw.write(String.format(".input a%ds%ds%d 2 64\r\n",i,j,c));
					}
				}
			}
			for (int i=0; i<rhos ;i++){
				for (int j=0; j<thetas; j++){
					bw.write(String.format(".output flag%ds%d\r\n",i,j)); 
				}
			}
			for (int i=0; i<rhos ;i++){
				for (int j=0; j<thetas; j++){
					for (int c=0;c<9;c++){
						bw.write(String.format("c%ds%ds%d add a%ds%ds%d b%ds%ds%d\r\n",i,j,c,i,j,c,i,j,c));
					}
				}
			}
			for (int i=0; i<rhos ;i++){
				for (int j=0; j<thetas; j++){
					bw.write(String.format("max%ds%ds1 max c%ds%ds0 c%ds%ds1\r\n",i,j,i,j,i,j));
					for (int c=2;c<9;c++){
						bw.write(String.format("max%ds%ds%d max max%ds%ds%d c%ds%ds%d\r\n",i,j,c,i,j,(c-1),i,j,c));
					}
					bw.write(String.format("flag%ds%ds1 equ max%ds%ds8 c%ds%ds4\r\n",i,j,i,j,i,j));
					bw.write(String.format("flag%ds%ds2 gtu c%ds%ds4 %d:64\r\n",i,j,i,j,threshold));
					bw.write(String.format("flag%ds%d and flag%ds%ds1 flag%ds%ds2\r\n",i,j,i,j,i,j));
				}
			}
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return circuit;
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


	public static File create_gc_input_file() {
		BigInteger [][][] wa=null; 
		File temp=null;
		try{
	         FileInputStream fileIn =
	                          new FileInputStream("input/wa.ser");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         wa = (BigInteger[][][]) in.readObject();
	         rhos = wa.length; 
	         thetas= wa[0].length;
	         paillier_n=(BigInteger)in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException e1)	      {
	         e1.printStackTrace();
	      }catch(ClassNotFoundException e2)	      {
	          e2.printStackTrace();
	       }
		try {
			temp = new File("input/inputA.txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
			for(int i = 0; i <rhos; i++){
				for (int j=0; j<thetas;j++){
					int[] wai = grab_int(wa[i][j]);
					for (int c=0;c<9 ;c++)
						bw.write(String.format("a%ds%ds%d %d\r\n",i,j,c,wai[c]));
				}
			}
			bw.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return temp;
	}
}
