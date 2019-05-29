package factory;

import applications.AtomStructure;
import applications.SocialNetworkCircle;
import applications.StellarSystem;
import circularOrbit.CircularOrbit;
import exceptions.ExceptionGroup;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultCircularOrbitFactory implements CircularOrbitFactory {

  private Function<String, List<String>> readingMethod = readerStrategy;
  private BiPredicate<String, List<String>> writingMethod = streamStrategy;

  @Override
  @Nullable
  public CircularOrbit createAndLoad(String loadFrom, boolean ignoreException)
      throws ExceptionGroup {
    CircularOrbit c = create(loadFrom);
    if (c == null) {
      return null;
    }
    try {
      c.loadFromFile(loadFrom);
    } catch (ExceptionGroup exceptions) {
      if (!ignoreException) {
        throw exceptions;
      }
    }
    if (!ignoreException) {
      c.checkRep();
    }
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

  @Override
  @Nullable
  public List<String> read(String path) {
    var list = readingMethod.apply(path);
    if (list != null) {
      list.removeIf(String::isBlank);
    }
    return list;
  }

  @Override
  public void setReadingMethod(
      @NotNull Function<String, List<String>> readingMethod) {
    this.readingMethod = readingMethod;
  }

  @Override
  public boolean write(String path, List<String> content) {
    return writingMethod.test(path, content);
  }

  @Override
  public void setWritingMethod(BiPredicate<String, List<String>> writingMethod) {
    this.writingMethod = writingMethod;
  }
}
