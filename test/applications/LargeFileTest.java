package applications;

import static org.junit.Assert.assertEquals;

import factory.CircularOrbitFactory;
import java.util.Objects;
import org.junit.Test;

public class LargeFileTest {

  private String getMethodName(){
    return Thread.currentThread() .getStackTrace()[2].getMethodName();
  }

  @Test
  public void testStellar_reader(){
    var cf = CircularOrbitFactory.getDefault();
    var start = System.currentTimeMillis();
    assertEquals(320001, Objects.requireNonNull(cf.read("input/StellarSystem_5.txt")).size());
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": " + (end - start) + "ms");
  }

  @Test
  public void testStellar_channel(){
    var cf = CircularOrbitFactory.getDefault();
    cf.setReadingMethod(CircularOrbitFactory.channelStrategy_read);
    var start = System.currentTimeMillis();
    assertEquals(320001, Objects.requireNonNull(cf.read("input/StellarSystem_5.txt")).size());
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": " + (end - start) + "ms");
  }

  @Test
  public void testStellar_scanner(){
    var cf = CircularOrbitFactory.getDefault();
    cf.setReadingMethod(CircularOrbitFactory.scannerStrategy);
    var start = System.currentTimeMillis();
    assertEquals(320001, Objects.requireNonNull(cf.read("input/StellarSystem_5.txt")).size());
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": " + (end - start) + "ms");
  }

  @Test
  public void testSocial_reader(){
    var cf = CircularOrbitFactory.getDefault();
    var start = System.currentTimeMillis();
    assertEquals(896581, Objects.requireNonNull(cf.read("input/SocialNetworkCircle_5.txt")).size());
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": "  + (end - start) + "ms");
  }

  @Test
  public void testSocial_channel(){
    var cf = CircularOrbitFactory.getDefault();
    cf.setReadingMethod(CircularOrbitFactory.channelStrategy_read);
    var start = System.currentTimeMillis();
    assertEquals(896581, Objects.requireNonNull(cf.read("input/SocialNetworkCircle_5.txt")).size());
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": "  + (end - start) + "ms");
  }

  @Test
  public void testSocial_scanner(){
    var cf = CircularOrbitFactory.getDefault();
    cf.setReadingMethod(CircularOrbitFactory.scannerStrategy);
    var start = System.currentTimeMillis();
    assertEquals(896581, Objects.requireNonNull(cf.read("input/SocialNetworkCircle_5.txt")).size());
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": "  + (end - start) + "ms");
  }
}
