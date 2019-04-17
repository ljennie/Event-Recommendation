package rpc;

import static org.junit.Assert.*;

import org.junit.Test;

public class randomTest {

	 @Test
	 public void testSubString(){
	   String str = new String("This is a unit test.");
	   assertEquals("units", str.substring(10, 14));
	 }	

}
