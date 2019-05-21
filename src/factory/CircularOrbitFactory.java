package factory;

import circularOrbit.CircularOrbit;
import exceptions.ExceptionGroup;
import org.jetbrains.annotations.Nullable;

public interface CircularOrbitFactory {

  @Nullable
  public CircularOrbit createAndLoad(String loadFrom) throws ExceptionGroup;

  @Nullable
  public CircularOrbit create(String type);
}
