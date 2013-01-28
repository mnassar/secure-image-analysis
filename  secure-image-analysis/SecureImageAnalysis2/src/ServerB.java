import java.math.BigInteger;
import java.util.Random;


public class ServerB extends Server{
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
				// choose r 
				int r = rand.nextInt(Integer.MAX_VALUE/2);
				BigInteger big_r = new BigInteger(String.valueOf(r));
				// calculate the encryption of r with key of A 
				BigInteger e_r =paillierA.Encryption(big_r);
				int c=0;
				for (int x=-1; x<2;x++){
					for (int y=-1; y<2; y++){
						if (i+x<0 || i+x>rhos-1 || j+y<0 || j+y> thetas-1){
							e_va[i][j][c]=paillierA.Encryption(big_r); // this gives different values for the borders 
							vb[i][j][c]=-r;
						}
						else{ 	
							e_va[i][j][c]=e_rho_theta_space_a[i+x][j+y].multiply(e_r).mod(paillierA.nsquare);
							vb[i][j][c]=this.rho_theta_space[i+x][j+y]-r;
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

}
