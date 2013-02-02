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
import java.util.Random;

import Program.GCParserCommon;
import Program.GCParserClient;
import Program.PrivateInputProvider;
import Program.PrivateInputsFile;
import Program.ProgClient;
import Program.Program;


public class ServerB extends Server {
	static final BigInteger maxInt = new BigInteger(String.valueOf(Integer.MAX_VALUE));
	static BigInteger paillier_n;
	int[][] rho_theta_space;
	Random rand; 
	Paillier paillier;
	int vb[][][];
	BigInteger wb[][][];
	public ServerB(){
		rand = new Random();
		paillier = new Paillier();
        System.out.println("SERVERB Paillier public key: ");
        System.out.println("  n: "+paillier.n);
        System.out.println("  nsquare: "+paillier.nsquare);
        System.out.println("  g: "+paillier.getG());
	}
	

	public BigInteger[][][] blind_n_permute(BigInteger[][] e_rho_theta_space_a, Paillier paillierA) {
		// just for testing we dont permute 
		int rhos = rho_theta_space.length;
		int thetas = rho_theta_space[0].length;
		vb= new int [rhos][thetas][9]; //this will stay at server B 
		BigInteger [][][] e_va = new BigInteger [rhos][thetas][9]; // this will be transferred back to A 
		for (int i=0; i<rhos; i++){
			for (int j=0; j<thetas; j++){
				// choose r from a big enough range (e.g 10 * maxRand client * ImagSize  )
				// Image size =376740, maxRand used at client is 10 => 10**7 
				int r = rand.nextInt((int)Math.pow(10, 7));
				BigInteger big_r = new BigInteger(String.valueOf(r));
				// calculate the encryption of r with key of A 
				BigInteger e_r =paillierA.Encryption(big_r);
				int c=0;
				for (int x=-1; x<2;x++){
					for (int y=-1; y<2; y++){
						if (i+x<0 || i+x>rhos-1 || j+y<0 || j+y> thetas-1){
							e_va[i][j][c]=paillierA.Encryption(big_r); // this gives different values for the borders 
							vb[i][j][c]=-r;
							//System.out.println("border"+vb[i][j][c]);
						}
						else{ 	
							e_va[i][j][c]=e_rho_theta_space_a[i+x][j+y].multiply(e_r).mod(paillierA.nsquare);
							vb[i][j][c]=this.rho_theta_space[i+x][j+y]-r;
							//System.out.println(this.rho_theta_space[i+x][j+y]+" ,"+vb[i][j][c]);
						}
						
						c++;
					}
				}
			}
		}
		return e_va;
	}
	
	public BigInteger[][][] encrypt_vb() {
		// encrypt the rho theta space using Paillier
		int nb_rhos=vb.length;
		int nb_thetas=vb[0].length;
		BigInteger e_vb[][][]=new BigInteger[nb_rhos][nb_thetas][9];
		for (int i=0; i<nb_rhos;i++){
			for (int j=0; j<nb_thetas;j++){
				int c=0;
				for (int x: vb[i][j]){
					String s = String.valueOf(x);
					BigInteger b = new BigInteger(s);
					e_vb[i][j][c]= this.paillier.Encryption(b);
					c++;
				}
			}
		}
		return e_vb;
	}


	public void decrypt_wb(BigInteger[][][] e_wb) {
		int rhos = e_wb.length; 
		int thetas = e_wb[0].length;
		wb = new BigInteger[rhos][thetas][9];
		for (int i=0; i<rhos; i++){
			for (int j=0; j<thetas; j++){
				int c=0;
				for (BigInteger x: e_wb[i][j]){
					wb[i][j][c]=paillier.Decryption(x);
					c++;
				}
					
			}
		}
		
	}
	void store_wb(){
		try
	      {
	         FileOutputStream fileOut =
	         new FileOutputStream("input/wb.ser");
	         ObjectOutputStream out =
	                            new ObjectOutputStream(fileOut);
	         out.writeObject(wb);
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
	public static void main( String []args) {
		BigInteger [][][] wb=null; 
		try
		{
			// prepare the gc input 
			File inputB =  create_gc_input_file();
			ProgClient.serverIPname = "localhost";
			Program.iterCount = 1;
			File cirFile = new File( "MyCircuits/locmax.cir" );
			GCParserCommon com = new GCParserCommon(cirFile,new PrivateInputsFile(new FileInputStream(inputB)));
			GCParserClient client = new GCParserClient(com);
			client = new GCParserClient(com); 
			client.run();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private static int[] grab_int(BigInteger[] wb_i_j) {
		int []int_wb = new int [9];
		int c=0;
		for (BigInteger big: wb_i_j){
			if (big.max(maxInt).intValue()==Integer.MAX_VALUE){
				int_wb[c] = Integer.parseInt(big.toString());
			}
			else{
				int_wb[c]=-Integer.parseInt(big.multiply(new BigInteger("-1")).mod(paillier_n).toString());
			}
			//System.out.println(int_wb[c]);
			c++;
		}
		return int_wb;
	}


	public static File create_gc_input_file() {
		BigInteger [][][] wb=null; 
		File temp=null;
		try{
	         FileInputStream fileIn =
	                          new FileInputStream("input/wb.ser");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         wb = (BigInteger[][][]) in.readObject();
	         rhos = wb.length; 
	         thetas= wb[0].length;
	         paillier_n=(BigInteger)in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException e1)	      {
	         e1.printStackTrace();
	      }catch(ClassNotFoundException e2)	      {
	          e2.printStackTrace();
	       }
		try {
			temp = new File("input/inputB.txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
			for(int i = 0; i <rhos; i++){
				for (int j=0; j<thetas;j++){
					int[] wbi = grab_int(wb[i][j]);
					for (int c=0;c<9 ;c++)
						bw.write(String.format("b%ds%ds%d %d\r\n",i,j,c,wbi[c]));
				}
			}
			bw.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return temp;
	}

}
