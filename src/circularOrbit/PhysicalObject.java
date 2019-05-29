package circularOrbit;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import track.Track;

/**
 * Mutable. represents the objects on an orbit (or as a center).
 */
public abstract class PhysicalObject {

  /*
    RI: none.
    AF: AF(name, R, pos) = an object at now time .
      AF(name, radiusInit, posInit) = an object when its initialed.
   */
  private final String name;
  protected final Track radiusInit;
  protected final double posInit;
  private final int hashCache;
  private Track R;
  private double pos;
  /**
   * how many objects with random pos have been created.
   */
  private static int num = 0;

  /**
   * an object with track, pos, and name.
   * @param name name of the object
   * @param r the radius of its track.
   * @param pos the pos of which the object take on the orbit.
   */
  protected PhysicalObject(String name, double[] r, double pos) {
    this.name = name;
    this.radiusInit = this.R = new Track(r);
    this.posInit = this.pos = pos;
    this.hashCache = Objects.hash(name, radiusInit, posInit);
  }

  /**
   * generate a object with random pos.
   *
   * @param name name of the object
   * @param r the radius of its track.
   */
  protected PhysicalObject(String name, double[] r) {
    this(name, r, num < 9 ? 40 * num + ThreadLocalRandom.current().nextDouble(40)
        : ThreadLocalRandom.current().nextDouble(360));
    num++;
  }

  public final Track getR() {
    return R;
  }

  /**
   * set R.
   * @param r radius of the new track.
   */
  public final void setR(double... r) {
    setR(new Track(r));
  }

  /**
   * set R.
   * @param r radius of the new track.
   */
  public final void setR(Double... r) {
    setR(new Track(r));
  }

  /**
   * set R.
   * @param r radius of the new track.
   */
  public void setR(Track r) {
    R = r;
  }

  public final double getPos() {
    return pos;
  }

  public final void setPos(double pos) {
    this.pos = pos;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + '{' + R
        + ", " + pos + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PhysicalObject)) {
      return false;
    }
    PhysicalObject that = (PhysicalObject) o;
    return Double.compare(that.posInit, posInit) == 0
        && getName().equals(that.getName())
        && radiusInit.equals(that.radiusInit);
  }

  @Override
  public int hashCode() {
    return this.hashCache;
  }

  @Override
  public abstract PhysicalObject clone();

  /**
   * get default comparator of PhysicalObject.
   * @return a comparator of two physical objects.
   * @see Double#compare(double, double)
   * @see Track#compare(Track, Track)
   */
  public static Comparator<PhysicalObject> getDefaultComparator() {
    return (o1, o2) -> {
      var r = Track.compare(o1.radiusInit, o2.radiusInit);
      if (r == 0) {
        return Double.compare(o1.posInit, o2.posInit);
      } else {
        return r;
      }
    };
  }

  /**
   * get hint strings in sub class with reflection.
   *
   * @param tyClass Class Object of T.
   * @param <T> a PhysicalObject
   * @return hint strings with array.
   */
  static <T extends PhysicalObject> String[] hintForUser(Class<T> tyClass) {
    try {
      var f = tyClass.getDeclaredField("hint");
      f.setAccessible(true);
      return (String[]) f.get(null);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(tyClass.getSimpleName() + ".hint do not exist. ");
    } catch (IllegalAccessException e) {
      throw new RuntimeException(tyClass.getSimpleName() + ".hint is not public. ");
    }
  }
}

