package learning;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class TrainingAssertTest {
	
	@Test
	public void assert_true() throws Exception {
		assertTrue(true);
	}
	
	@Test
	public void not_called() throws Exception {
		
	}
	
	
	@Test
	public void assert_false() throws Exception {
		assertFalse(false);
	}
	
	@Test
	public void not_called2() throws Exception {
		
	}

}
