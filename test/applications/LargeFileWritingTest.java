package applications;

import factory.CircularOrbitFactory;
import java.util.List;
import org.junit.Test;

public class LargeFileWritingTest {

  private static List<String> sList;
  private static List<String> cList;

  static {
    var cf = CircularOrbitFactory.getDefault();
    StellarSystem s = (StellarSystem) cf.createAndLoad("input/StellarSystem_5.txt", true);
    assert s != null;
    sList = s.asList();

    SocialNetworkCircle c = (SocialNetworkCircle) cf
        .createAndLoad("input/SocialNetworkCircle_5.txt", true);
    assert c != null;
    cList = c.asList();
  }

  private String getMethodName() {
    return Thread.currentThread().getStackTrace()[2].getMethodName();
  }

  @Test
  public void testStellar_writer() {
    var cf = CircularOrbitFactory.getDefault();
    cf.setWritingMethod(CircularOrbitFactory.writerStrategy);
    var start = System.currentTimeMillis();
    cf.write("out/txt/stellar_writer", sList);
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": " + (end - start) + "ms");
  }

  @Test
  public void testStellar_channel() {
    var cf = CircularOrbitFactory.getDefault();
    cf.setWritingMethod(CircularOrbitFactory.channelStrategy_write);
    var start = System.currentTimeMillis();
    cf.write("out/txt/stellar_channel", sList);
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": " + (end - start) + "ms");
  }

  @Test
  public void testStellar_stream() {
    var cf = CircularOrbitFactory.getDefault();
    cf.setWritingMethod(CircularOrbitFactory.streamStrategy);
    var start = System.currentTimeMillis();
    cf.write("out/txt/stellar_stream", sList);
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": " + (end - start) + "ms");
  }

  @Test
  public void testSocial_writer() {
    var cf = CircularOrbitFactory.getDefault();
    cf.setWritingMethod(CircularOrbitFactory.writerStrategy);
    var start = System.currentTimeMillis();
    cf.write("out/txt/social_writer", cList);
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": " + (end - start) + "ms");
  }

  @Test
  public void testSocial_channel() {
    var cf = CircularOrbitFactory.getDefault();
    cf.setWritingMethod(CircularOrbitFactory.channelStrategy_write);
    var start = System.currentTimeMillis();
    cf.write("out/txt/social_channel", cList);
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": " + (end - start) + "ms");
  }

  @Test
  public void testSocial_stream() {
    var cf = CircularOrbitFactory.getDefault();
    cf.setWritingMethod(CircularOrbitFactory.streamStrategy);
    var start = System.currentTimeMillis();
    cf.write("out/txt/social_stream", cList);
    var end = System.currentTimeMillis();
    System.out.println(getMethodName() + ": " + (end - start) + "ms");
  }
}
