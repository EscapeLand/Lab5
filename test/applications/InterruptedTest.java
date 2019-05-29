package applications;

import static java.lang.Thread.sleep;

import factory.CircularOrbitFactory;
import org.junit.Test;

public class InterruptedTest {

  {
    try {
      sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void Stellar_ctor_large() {

    var cf = CircularOrbitFactory.getDefault();
    StellarSystem s = (StellarSystem) cf.createAndLoad("input/StellarSystem_5.txt", true);
    assert s != null;
    var sList = s.asList();
    System.out.println(sList.size());
  }

  @SuppressWarnings("unused")
  @Test
  public void Social_ctor_large() {
    var cf = CircularOrbitFactory.getDefault();
    SocialNetworkCircle c = (SocialNetworkCircle) cf
        .createAndLoad("input/SocialNetworkCircle_5.txt", true);
    assert c != null;
    var cList = c.asList();
    System.out.println(cList.size());
  }
}
