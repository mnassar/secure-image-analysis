import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;


public class HomomorphicServer {

	/**
	 * @param args
	 */
	static String enc_zeros_file="shared/enc_zeros.txt";
	static BigInteger zero=new BigInteger("0");
	static BigInteger one=new BigInteger("1");
	Paillier paillier; 

	public HomomorphicServer (){
		paillier = new Paillier();
	}
	public static void main(String[] args) {
		//new HomomorphicServer().get_encrypted_zeros(10);
		new HomomorphicServer().pre_compute_encrypted_zeros(400000);
		//new HomomorphicServer().pre_compute_encrypted_zeros(1);
	}

	public void pre_compute_encrypted_zeros(int nb){
		try {
			File file = new File(enc_zeros_file);
			if (!file.exists()){
				file.createNewFile();
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(file));	
			long start = System.currentTimeMillis();
			for (int i=0; i<nb; i++){
				out.write(paillier.Encryption(zero)+"\n");
				//System.out.println(i +" " + enc_0[i]);
			}
			long end = System.currentTimeMillis();
			System.out.format("HOMOMORPHIC time for encrypting %d zeros to the file %s: %d \n",nb,enc_zeros_file,end-start);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BigInteger[] get_encrypted_zeros(int nb){
		/* instantiating an object of Paillier cryptosystem*/
		BigInteger [] enc_0= new BigInteger[nb];
		long start = System.currentTimeMillis();
		for (int i=0; i<nb; i++){
			enc_0[i]=paillier.Encryption(new BigInteger("0"));
			//System.out.println(i +" " + enc_0[i]);
		}
		long end = System.currentTimeMillis();
		//System.out.println(end);
		//System.out.println(start);
		System.out.println("HOMOMORPHIC time for encrypting zeros for the image: "+(end-start));
		return enc_0;
		
	}
	public BigInteger get_encrypted_one(){
		return paillier.Encryption(one);
	}
	public BigInteger get_encrypted_zero(){
		return paillier.Encryption(zero);
	}
	int[][] decrypt_A(BigInteger[][] enc_Acc){
		System.out.println("HOMOM accumulator decryption ...");
		long start = System.currentTimeMillis();
		int [][] A= new int[enc_Acc.length][enc_Acc[0].length];
		//a:
		for(int x=0; x<enc_Acc.length ;x++){
			for (int y=0; y<enc_Acc[0].length; y++){
				A[x][y]=paillier.Decryption(enc_Acc[x][y]).intValue();
				//System.out.println(enc_Acc[x][y]);
				//System.out.println(A[x][y]);
				//break a;
				//A[x][y]=enc_A[x][y].intValue();
			}
		}
		long end = System.currentTimeMillis();
		System.out.format("HOMOM time for accumulator decryption %d ms\n", (end-start));
		return A; 
	}
}
