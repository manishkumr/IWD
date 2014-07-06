package featureSelection;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class TestComparison {
	public static void main(String[] args) throws IOException {
	Double[] Array={0.03,0.50,0.0,0.03,0.44};
	ArrayIndexComparator comparator = new ArrayIndexComparator(Array);
	Integer[] indexes = comparator.createIndexArray();
	Arrays.sort(indexes, comparator);
	//Collections.sort(Array);
	
	for (int i = 0; i < indexes.length; i++) {
		System.out.print(indexes[i]+",");
	}
	
	}

}
