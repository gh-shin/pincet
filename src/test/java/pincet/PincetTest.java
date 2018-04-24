package pincet;

import org.junit.Ignore;
import org.junit.Test;

public class PincetTest {

  @Test
  @Ignore
  public void mainTest() throws InterruptedException {
    System.setProperty(PincetArgs.TYPE.getKey(), "daemon");
    Pincet.main(null);
    System.out.println("!!!!!!!!!! running success.");
    Pincet.hold();
  }
}