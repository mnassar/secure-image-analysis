import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;



public class HomomorphicServer {

	/**
	 * @param args
	 */
	static String enc_zeros_file="shared/enc_zeros.txt";
	Paillier paillier; 

	public HomomorphicServer (){
		paillier = new Paillier();
	}
	public static void main(String[] args) {
		//new HomomorphicServer().get_encrypted_zeros(10);
		//new HomomorphicServer().pre_compute_encrypted_zeros(400000);
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
				out.write(paillier.Encryption(BigInteger.ZERO)+"\n");
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
			enc_0[i]=paillier.Encryption(BigInteger.ZERO);
			//System.out.println(i +" " + enc_0[i]);
		}
		long end = System.currentTimeMillis();
		//System.out.println(end);
		//System.out.println(start);
		System.out.println("HOMOMORPHIC time for encrypting zeros for the image: "+(end-start));
		return enc_0;
		
	}
	public BigInteger get_encrypted_one(){
		return paillier.Encryption(BigInteger.ONE);
	}
	public BigInteger get_encrypted_zero(){
		return paillier.Encryption(BigInteger.ZERO);
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
	
	
	
	public HashSet<Point> find_local_maximas(ShapesDataStructure enc_hashmap, int thresh) {
		System.out.println("HOMOM local maximization, threshold="+thresh);
		long start_local_maximization = System.currentTimeMillis();
		Point[] points = enc_hashmap.points;
		BigInteger[][] gradiants = enc_hashmap.gradiants;
		BigInteger maxInt = new BigInteger(Integer.MAX_VALUE+""); 
		HashSet<Point> local_maxima_array= new HashSet<Point>();
		int nb_of_lines=0;	
		int nb_of_lines_passing_threshold=0;
		int count=0;
		for (Point p:points){
			int votesd = paillier.Decryption(p.value).intValue();
			if (votesd > thresh){
				nb_of_lines_passing_threshold++;
				boolean is_local_maxima=true;
				for (BigInteger d: gradiants[count]){
					if (d != null){
						BigInteger dd= paillier.Decryption(d);
						if (!(maxInt.intValue()==dd.max(maxInt).intValue())){// it means the d is negative 
							is_local_maxima=false;
							break;
						}
					}
				}
					
				if (is_local_maxima){
					p.votes=votesd;
					local_maxima_array.add(p);
					nb_of_lines++; 
				}
			}
			count++;
		}
		System.out.println("HOMOM number of lines passing the threshold  "+ nb_of_lines_passing_threshold);
		System.out.println("HOMOM number of lines before tie break  "+ nb_of_lines);
		long stop_local_maximization = System.currentTimeMillis();
		System.out.println("HOMOM local maximization time(ms): "
				+(stop_local_maximization-start_local_maximization));
		System.out.println("HOMOM send results to client");
		return local_maxima_array;
	}
}
