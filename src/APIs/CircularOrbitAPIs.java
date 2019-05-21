package APIs;

import circularOrbit.CircularOrbit;
import circularOrbit.PhysicalObject;
import graph.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import track.Track;

public class CircularOrbitAPIs {

  /**
   * calculate entropy of a circular orbit.
   * @param c the CircularOrbit to calculate entropy.
   * @param <L> circular orbit, L
   * @param <E> circular orbit, E
   * @return the entropy of the system.
   */
  public static <L extends PhysicalObject, E extends PhysicalObject>
      double getObjectDistributionEntropy(CircularOrbit<L, E> c) {
    Map<Track, Float> p = new HashMap<>();
    int sum = 0;
    for (E i : c) {
      Float tmp = p.getOrDefault(i.getR(), 0.0f);
      p.put(i.getR(), tmp + 1.0f);
      sum++;
    }
    final int tmp = sum;
    p.forEach((t, v) -> p.put(t, v / tmp));

    float entropy = 0;
    for (Float i : p.values()) {
      entropy -= i * Math.log(i);
    }
    return entropy;
  }

  /**
   * calculate logical distance between two object in a circular orbit.
   * @param c the circular orbit.
   * @param a object a
   * @param b object b
   * @param <L> CircularOrbit, L
   * @param <E> CircularOrbit, E
   * @return the logical distance of the two object. -1 if not connected. else the min-distance.
   */
  public static <L extends PhysicalObject, E extends PhysicalObject>
      int getLogicalDistance(CircularOrbit<L, E> c, PhysicalObject a, PhysicalObject b) {
    Graph<PhysicalObject> graph = c.getGraph();
    if (!graph.vertices().containsAll(Arrays.asList(a, b))) {
      return -1;
    }
    if (a == b) {
      return 0;
    } else {
      Set<PhysicalObject> que = new HashSet<>();
      int r = findNext(graph, a, b, que);
      que.clear();
      return r;
    }
  }

  private static <E extends PhysicalObject> int findNext(Graph<PhysicalObject> graph, E a, E b,
      Set<PhysicalObject> que) {
    que.add(a);
    Set<PhysicalObject> next = graph.targets(a).keySet();
    if (next.contains(b)) {
      return que.size();
    } else {
      Set<Integer> forcmp = new HashSet<>();

      for (PhysicalObject i : next) {
        if (que.contains(i)) {
          continue;
        }
        int r = findNext(graph, i, b, new HashSet<>(que));
        if (r > 0) {
          forcmp.add(r);
        }
      }
      if (forcmp.isEmpty()) {
        return -1;
      } else {
        return Collections.min(forcmp);
      }
    }
  }

  /**
   * calculate logical distance between two object in a circular orbit.
   * @param c the circular orbit.
   * @param e1 object a
   * @param e2 object b
   * @param <L> CircularOrbit, L
   * @param <E> CircularOrbit, E
   * @return the physical distance of the two object.
   */
  public static <L extends PhysicalObject, E extends PhysicalObject> double getPhysicalDistance(
      CircularOrbit<L, E> c, PhysicalObject e1, PhysicalObject e2) {
    double l1 = e1.getR().getRect()[0];
    double l2 = e2.getR().getRect()[0];
    return Math.sqrt(l1 * l1 + l2 * l2 - 2 * l1 * l2 * Math
        .cos(Math.toRadians(Math.abs(e1.getPos() - e2.getPos()))));
  }

  /**
   * get difference of two circular orbit.
   * @param c1 one CircularOrbit.
   * @param c2 another CircularOrbit.
   * @param <L> CircularOrbit, L
   * @param <E> CircularOrbit, E.
   * @return difference of the two CircularOrbit.
   */
  public static <L extends PhysicalObject, E extends PhysicalObject> Difference getDifference(
      CircularOrbit<L, E> c1, CircularOrbit<L, E> c2) {
    Set<E> setC1 = new TreeSet<>(E.getDefaultComparator());
    Set<E> setC2 = new TreeSet<>(E.getDefaultComparator());
    c1.forEach(setC1::add);
    c2.forEach(setC2::add);

    Map<Track, Integer> tracksC1 = new HashMap<>();
    Map<Track, Integer> tracksC2 = new HashMap<>();

    setC1.forEach(x ->
        tracksC1.put(x.getR(), tracksC1.containsKey(x.getR()) ? tracksC1.get(x.getR()) + 1 : 1));
    setC2.forEach(x ->
        tracksC2.put(x.getR(), tracksC2.containsKey(x.getR()) ? tracksC2.get(x.getR()) + 1 : 1));

    int m = Math.max(setC1.size(), setC2.size());

    int[] trackDif = new int[m];
    Iterator<Integer> itC1 = tracksC1.values().iterator();
    Iterator<Integer> itC2 = tracksC2.values().iterator();
    int i = 0;
    while (itC1.hasNext() || itC2.hasNext()) {
      trackDif[i++] = itC1.hasNext() && itC2.hasNext() ? itC1.next() - itC2.next() :
          itC1.hasNext() ? itC1.next() : -itC2.next();
    }

    Map<Track, Set<E>> objDif1 = new HashMap<>();
    Map<Track, Set<E>> objDif2 = new HashMap<>();

    for (E e : setC1) {
      if (!setC2.contains(e)) {
        Set<E> tmp = objDif1.get(e.getR());
        if (tmp == null) {
          tmp = new TreeSet<>(E.getDefaultComparator());
        }
        tmp.add(e);
        objDif1.put(e.getR(), tmp);
      } else if (!objDif1.containsKey(e.getR())) {
        objDif1.put(e.getR(), null);
      }
    }

    for (E e : setC2) {
      if (!setC1.contains(e)) {
        Set<E> tmp = objDif2.get(e.getR());
        if (tmp == null) {
          tmp = new TreeSet<>(E.getDefaultComparator());
        }
        tmp.add(e);
        objDif2.put(e.getR(), tmp);
      } else if (!objDif2.containsKey(e.getR())) {
        objDif2.put(e.getR(), null);
      }
    }

    return new Difference<>(tracksC1.size() - tracksC2.size(), trackDif,
        new ArrayList<>(objDif1.values()), new ArrayList<>(objDif2.values()));
  }

  /**
   * see std::find_if in C++.
   * @param col collections to find.
   * @param pred predicate to apply.
   * @param <E> col, type.
   * @return null if not found. else the first element meet the predicate.
   */
  @Nullable
  public static <E> E find_if(@NotNull Iterable<E> col, Predicate<E> pred) {
    for (E e : col) {
      if (pred.test(e)) {
        return e;
      }
    }
    return null;
  }

  /**
   * see std::transform in C++.
   * @param src Collection to transform from.
   * @param des Collection to transform to.
   * @param func function receive O, and cast it to R.
   * @param <O> src, type.
   * @param <R> des, type.
   */
  public static <O, R> void transform(@NotNull Collection<O> src, @NotNull Collection<R> des,
      Function<O, R> func) {
    des.clear();
    src.forEach(s -> des.add(func.apply(s)));
  }

  /**
   * transform an array to another type.
   * @param src array to transform from.
   * @param func function receive O, and cast it to R.
   * @param <O> src, type
   * @param <R> return value, type.
   * @return array of R.
   * @see CircularOrbitAPIs#transform(Collection, Collection, Function)
   */
  @NotNull
  @SuppressWarnings("unchecked")
  public static <O, R> R[] transform(@NotNull O[] src, Function<O, R> func) {
    var arr = new Object[src.length];
    for (int i = 0; i < src.length; i++) {
      arr[i] = func.apply(src[i]);
    }
    return (R[]) arr;
  }
}

class Difference<E extends PhysicalObject> {

  private final int trackDif;
  private final int[] trackNumDif;
  private final List<Set<E>> objDif1;
  private final List<Set<E>> objDif2;

  Difference(int trackDif, int[] trackNumDif, List<Set<E>> objDif1, List<Set<E>> objDif2) {
    this.trackDif = trackDif;
    this.trackNumDif = trackNumDif;
    this.objDif1 = objDif1;
    this.objDif2 = objDif2;
  }

  public int[] getTrackNumDif() {
    return trackNumDif.clone();
  }


  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("轨道数差异: ").append(getTrackDif());
    int m = Math.max(objDif1.size(), objDif2.size());

    for (int i = 0; i < m; i++) {

      s.append("\n轨道").append(i + 1).append("的物体数量差异: ").append(trackNumDif[i]);
      s.append("; 物体差异: {");
      if (i < objDif1.size()) {
        objDif1.get(i).forEach(x -> s.append(x.getName()).append(", "));
      }
      if (i < objDif1.size() && !objDif1.get(i).isEmpty()) {
        s.append("\b\b");
      }
      s.append("} - {");
      if (i < objDif2.size()) {
        objDif2.get(i).forEach(x -> s.append(x.getName()).append(", "));
      }
      if (i < objDif2.size() && !objDif2.get(i).isEmpty()) {
        s.append("\b\b");
      }
      s.append("}");
    }

    return s.toString();
  }

  List<Set<E>> getObjDif1() {
    return new ArrayList<>(objDif1);
  }

  List<Set<E>> getObjDif2() {
    return new ArrayList<>(objDif2);
  }

  public int getTrackDif() {
    return trackDif;
  }
}