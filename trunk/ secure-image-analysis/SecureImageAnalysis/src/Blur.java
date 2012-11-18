/*

*/

import java.awt.image.*;
import java.math.BigInteger;



/**
 * This class re-uses code from the package com.jhlabs.image; 
 * fetched from Internet under the following copyright: 
 * Copyright 2005 Huxtable.com. All rights reserved. @author Jerry Huxtable
 * I adapted it to my context 
 *  
 */
public class Blur {

	//static final long serialVersionUID = 5377089073023183684L;

	protected int radius;
	protected Kernel kernel;
	static int kernel_multiplier=6;
	
	public static void main(String [] args){
		Blur b = new Blur();
		float[] matrix = b.kernel.getKernelData( null );
    	for (int i=0; i<matrix.length; i++){
    		System.out.print(kernel_multiplier*matrix[i]+" ");
    	}
		//b.filter(src, nsquare)
		/*Blur b = new Blur();
    	float[] matrix = b.kernel.getKernelData( null );
    	for (int i=0; i<matrix.length; i++){
    		System.out.print(matrix[i]+" ");
    	}
    	System.out.println();
    	float src[][]= {{1,1,1,1,1},{1,1,1,1,1},{1,1,2,1,1},{1,1,1,1,1},{1,1,1,1,1}};
       	for (int x=0;x<5;x++){
    		for (int y=0;y<5;y++)
    			System.out.print(src[x][y]+" ");
    		System.out.println();
    	}
    	System.out.println();
    	float out [][]=b.filter(src);
 

    	for (int x=0;x<5;x++){
    		for (int y=0;y<5;y++)
    			System.out.print(out[x][y]+" ");
    		System.out.println();
    	}*/
    }
	
	/**
	 * Construct a Gaussian filter
	 */
	public Blur() {
		this(2);
	}

	/**
	 * Construct a Gaussian filter
	 * @param radius blur radius in pixels
	 */
	public Blur(int radius) {
		setRadius(radius);
	}

	/**
	 * Set the radius of the kernel, and hence the amount of blur. The bigger the radius, the longer this filter will take.
	 * @param radius the radius of the blur in pixels.
	 */
	public void setRadius(int radius) {
		this.radius = radius;
		kernel = makeKernel(radius);
	}
	
	/**
	 * Get the radius of the kernel.
	 * @return the radius
	 */
	public int getRadius() {
		return radius;
	}
	/**
	 * Make a Gaussian blur kernel.
	 */
	public static Kernel makeKernel(int r) {
		float radius=r;
		int rows = r*2+1;
		float[] matrix = new float[rows];
		float sigma = radius/3;
		float sigma22 = 2*sigma*sigma;
		float sigmaPi2 = (float) (2*Math.PI*sigma);
		float sqrtSigmaPi2 = (float)Math.sqrt(sigmaPi2);
		float radius2 = radius*radius;
		float total = 0;
		int index = 0;
		for (int row = -r; row <= r; row++) {
			float distance = row*row;
			if (distance > radius2)
				matrix[index] = 0;
			else
				matrix[index] = (float)Math.exp(-(distance)/sigma22) / sqrtSigmaPi2;
			total += matrix[index];
			index++;
		}
		for (int i = 0; i < rows; i++){
			matrix[i] /= total;
		}
		return new Kernel(rows, 1, matrix);
	}
	
	/**
	 * Apply the blurring kernel to the source 
	 */
    public BigInteger[][] filter( BigInteger [][] src, BigInteger nsquare) {
    	System.out.println("HOUGH/BLUR Gaussian kernel multiplier: "+kernel_multiplier);
    	float[] matrix = kernel.getKernelData( null );
    	int cols = kernel.getWidth();
		int cols2 = cols/2;
    	int[] imatrix = new int[cols];
    	for (int i = 0; i < cols; i++){
			imatrix[i] =(int) (matrix[i]*kernel_multiplier);
		}
    	int w= src.length; //nb of rows 
    	int h= src[0].length; // nb of cols 
    	BigInteger [][] blurred= new BigInteger[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				blurred[x][y]=BigInteger.ONE;
				int moffset = cols2;
				for (int col = -cols2; col <= cols2; col++) {
					int f = imatrix[moffset+col];
					int iy = y+col;
					if ( iy < 0 ) {
						iy = 0;
					} else if ( iy >= h) {
						iy = h-1;
					}
					//blurred[x][y]+=f*src[x][iy];
					BigInteger c=src[x][iy].modPow(new BigInteger(f+""), nsquare);
					blurred[x][y]=blurred[x][y].multiply(c).mod(nsquare);
				}
			}
		}
	 	
    	/*for (int x=0;x<5;x++){
    		for (int y=0;y<5;y++)
    			System.out.print(blurred[x][y]+" ");
    		System.out.println();
    	}
    	System.out.println();*/		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x< w; x++) {
				int moffset = cols2;
				for (int col = -cols2; col <= cols2; col++) {
					int f = imatrix[moffset+col];
					int ix = x+col;
					if ( ix < 0 ) {
						ix = 0;
					} else if ( ix >= w) {
						ix = w-1;
					}
					//blurred[x][y]+=f*src[ix][y];
					BigInteger c=src[ix][y].modPow(new BigInteger(f+""), nsquare);
					blurred[x][y]=blurred[x][y].multiply(c).mod(nsquare);
				}
			}
		}
		// just for testing 
		// decrypt and print
		/*Paillier paillier=new Paillier();
		for (int i=0; i<src.length;i++){
			for (int j=0;j<src[0].length;j++)
				System.out.print(paillier.Decryption(src[i][j]).intValue()+" ");
			System.out.println();
		}
		System.out.println();
		for (int i=0; i<blurred.length;i++){
			for (int j=0;j<blurred[0].length;j++)
				System.out.print(paillier.Decryption(blurred[i][j]).intValue()+" ");
			System.out.println();
		}*/
		return blurred;
    }


	public String toString() {
		return "Blur/Gaussian Blur...";
	}

}