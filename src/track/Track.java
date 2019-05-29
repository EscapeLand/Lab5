package track;

import circularOrbit.PhysicalObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * Immutable. represents a track in the circular orbit.
 *
 * @param <E> the type of the objects on the track. (deprecated)
 */
public class Track<E extends PhysicalObject> {

  /*
    RI: radius[0] >= radius[1]; radius.length == 2;
    AF: AF(radius[0], radius[1]) = the orbit, maybe a perfect circle or a ellipse.
   */
  private final double[] radius;
  private static ArrayList<double[]> pool;
  private static boolean disablePool = false;

  /**
   * the default comparator of two tracks.
   *
   * @see Arrays#compare(double[], double[])
   */
  public static final Comparator<Track> defaultComparator = (a, b) -> Arrays
      .compare(a.radius, b.radius);

  private static double[] unboxingCast_double(Double[] d) {
    double[] arr = new double[d.length];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = d[i];
    }
    return arr;
  }

  /**
   * Track with several args.
   *
   * @param track the radius. no larger than 2 elements.
   * @throws IllegalArgumentException if length of track != 1  or 2.
   */
  public Track(double... track) {
    double[] cache = null;
    if (!disablePool) {
      if (pool == null) {
        initialPool();
      }
      int i = ((int) track[0]);
      cache = pool.get(i == -1 ? 5 : i);
    }
    switch (track.length) {
      case 1:
        this.radius = cache == null ? new double[]{track[0], track[0]} : cache;
        break;
      case 2:
        if (cache != null) {
          this.radius = cache;
          break;
        }
        var max = Math.max(track[0], track[1]);
        var min = Math.min(track[0], track[1]);
        this.radius = new double[]{max, min};
        break;
      default:
        throw new IllegalArgumentException("length of radius: " + track.length);
    }
  }

  private static void initialPool() {
    pool = new ArrayList<>(6);
    pool.add(new double[]{0, 0});
    for (int i = 1; i < 5; i++) {
      pool.add(new double[]{i, i});
    }
    pool.add(new double[]{-1, -1});
  }

  /**
   * track with several args.
   *
   * @param track the radius. no larger than 2 elements.
   */
  public Track(Double... track) {
    this(unboxingCast_double(track));
  }

  public static void setDisablePool(boolean disablePool) {
    Track.disablePool = disablePool;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Track)) {
      return false;
    }
    Track<?> track = (Track<?>) o;
    return Arrays.equals(track.radius, radius);
  }

  @Override
  public int hashCode() {
    return Objects.hash(radius[0], radius[1]);
  }

  @Override
  public String toString() {
    if (radius[0] == radius[1]) {
      return String.valueOf(radius[1]);
    } else {
      return Arrays.toString(radius);
    }
  }


  /**
   * compare two tracks.
   *
   * @param a one track
   * @param b another track
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
     equal to, or greater than the second.
   * @see Track#defaultComparator
   * @see Comparator#compare(Object, Object)
   */
  public static int compare(Track a, Track b) {
    return defaultComparator.compare(a, b);
  }

  /**
   * get arg array of the track.
   *
   * @return radius of the track, length == 2. ({long half shaft, short half shaft})
   */
  public double[] getRect() {
    return radius.clone();
  }

  /**
   * get arg array of the track.
   *
   * @return radius of the track, length == 2. ({long half shaft, short half shaft})
   */
  public Double[] getRect_alt() {
    return new Double[]{radius[0], radius[1]};
  }
}