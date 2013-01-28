import java.math.BigInteger;
import java.util.Random;


public class ServerA extends Server{
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
				int r = rand.nextInt(10000);
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
}
