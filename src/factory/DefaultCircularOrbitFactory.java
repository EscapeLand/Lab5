package factory;

import applications.AtomStructure;
import applications.SocialNetworkCircle;
import applications.StellarSystem;
import circularOrbit.CircularOrbit;
import exceptions.ExceptionGroup;
import org.jetbrains.annotations.Nullable;

public class DefaultCircularOrbitFactory implements CircularOrbitFactory {

  @Override
  @Nullable
  public CircularOrbit createAndLoad(String loadFrom) throws ExceptionGroup {
    CircularOrbit c = create(loadFrom);
    if (c == null) {
      return null;
    }
    c.loadFromFile(loadFrom);
    c.checkRep();
    return c;
  }

  @Override
  @Nullable
  public CircularOrbit create(String type) {
    if (type.contains("StellarSystem")) {
      return new StellarSystem();
    } else if (type.contains("AtomicStructure")) {
      return new AtomStructure();
    } else if (type.contains("SocialNetworkCircle")) {
      return new SocialNetworkCircle();
    } else {
      return null;
    }
  }
}
