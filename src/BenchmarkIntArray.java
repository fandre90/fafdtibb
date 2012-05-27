import java.util.Date;

import org.apache.hadoop.io.VIntWritable;


public class BenchmarkIntArray {
	public static void main(String[] args) {
		int[] array = new int[10];
		Date start = new Date();
		for(int i=0; i<10000000; ++i) {
			array[5] += 1;
			array[9] += 1;
			array[6] += 1;
			array = new int[10];
		}
		Date stop = new Date();
		System.out.println("1: " + (stop.getTime() - start.getTime()));
		int[] array2 = new int[10];
		Date start2 = new Date();
		for(int i=0; i<10000000; ++i) {
			array2[5] += 1;
			array2[9] += 1;
			array2[6] += 1;
			for(int j=0; j<array2.length; ++j) {
				array2[j] = 0;
			}
		}
		Date stop2 = new Date();
		System.out.println("2: " + (stop2.getTime() - start2.getTime()));
		VIntWritable[] array3 = new VIntWritable[10];
		for(int i=0; i<array3.length; ++i) {
			array3[i] = new VIntWritable(0);
		}
		Date start3 = new Date();
		for(int i=0; i<10000000; ++i) {
			array3[5].set(array3[5].get() + 1);
			array3[9].set(array3[9].get() + 1);
			array3[6].set(array3[6].get() + 1);
			for(int j=0; j<array3.length; ++j) {
				array3[j].set(0);
			}
		}
		Date stop3 = new Date();
		System.out.println("3: " + (stop3.getTime() - start3.getTime()));
	}
}
