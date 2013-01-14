import java.math.BigInteger;


class Point{
	static int rho_radius;
	static int theta_radius;
	int i, j;
	BigInteger value;
	int votes;
	Point(int i, int j, BigInteger value){
		this.i=i;
		this.j=j;
		this.value=value;
	}
    public int hashCode(){
    		return votes;
    }
    public boolean equals(Object local_maxima){
    	Point lm= (Point)local_maxima;
    	if (Math.abs(this.i - lm.i) < rho_radius+1 && Math.abs(this.j -lm.j) < theta_radius+1 )
    		// if a local maxima is a neighbour of another local maxima it means that they are equal	
    		return true; 
    	else 
    		return false;
    }
    public String toString(){
    	return ("rho index: "+i+", theta index: "+j+", votes: "+votes);
    }
}