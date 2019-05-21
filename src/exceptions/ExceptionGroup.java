package exceptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ExceptionGroup extends RuntimeException implements Iterable<Exception> {

  private List<Exception> exs = new ArrayList<>();

  /**
   * add an exception to ExceptionGroup.
   * @param ex the exception to join.
   * @return true if success.
   */
  public boolean join(Exception ex) {
    if (ex instanceof ExceptionGroup) {
      return merge((ExceptionGroup) ex);
    } else {
      return exs.add(ex);
    }
  }

  @Override
  public String getMessage() {
    StringBuilder s = new StringBuilder();
    exs.forEach(e -> s.append(e.getMessage()).append('\n'));
    return s.toString();
  }

  @Override
  public String getLocalizedMessage() {
    StringBuilder s = new StringBuilder();
    exs.forEach(e -> s.append(e.getLocalizedMessage()).append('\n'));
    return s.toString();
  }

  @Override
  public void printStackTrace() {
    exs.forEach(Exception::printStackTrace);
  }

  public int size() {
    return exs.size();
  }

  public Exception get(int index) {
    return exs.get(index);
  }

  /**
   * merge two exception group.
   * @param other another Exception group.
   * @return true if success.
   */
  public boolean merge(ExceptionGroup other) {
    if (equals(other)) {
      return false;
    } else {
      return exs.addAll(other.exs);
    }
  }

  public void clear() {
    exs.clear();
  }

  public boolean isEmpty() {
    return exs.isEmpty();
  }

  @NotNull
  @Override
  public Iterator<Exception> iterator() {
    return exs.iterator();
  }
}
