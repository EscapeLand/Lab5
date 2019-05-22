package circularOrbit;

import exceptions.ExceptionGroup;
import graph.Graph;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a System with several circular orbit.
 * @param <L> center object type.
 * @param <E> object on track.
 */
@SuppressWarnings("UnnecessaryInterfaceModifier")
public interface CircularOrbit<L extends PhysicalObject, E extends PhysicalObject> extends
    Iterable<E> {

  /**
   * check RI.
   */
  void checkRep();

  /**
   * add R track.
   *
   * @param r add R track with radius of radius.
   * @return if R track with radius radius has already exist, return false; else true.
   */
  public boolean addTrack(double[] r) throws IllegalArgumentException;

  /**
   * remove R track, and apparently the objects on the tract are removed together.
   *
   * @param r the track with radius radius to remove
   * @return if R track with radius radius has already exist, return true. else false.
   */
  public boolean removeTrack(double[] r);

  /**
   * change the center of the System.
   * @param newCenter change the center object to newObject.
   * @return the previous center object.
   */
  public L changeCentre(@Nullable L newCenter);

  /**
   * add a object to the system.
   * @param newObject add R object to circular orbit.
   * @return if the object has already exist, return false; else true.
   */
  public boolean addObject(@NotNull E newObject);

  /**
   * move a object from one track to another.
   * @param obj which to move.
   * @param to move to. -1, or other positive double.
   * @return true if success, false if no track has radius equals to from or to.
   */
  public boolean moveObject(E obj, double[] to);

  /**
   * remove a object from the system.
   * @param obj remove R object from circular orbit.
   * @return if the object has already exist, return true; else false.
   */
  public boolean removeObject(@NotNull E obj);

  /**
   * set relation between two different objects.
   * @param a begin of relation.
   * @param b end of relation
   * @param val weight of the relation. if val == 0, it means remove the relation.
   */
  public void setRelation(PhysicalObject a, PhysicalObject b, float val);

  /**
   * get graph of the relations.
   * @return return the relation graph of the circular orbit.
   */
  public @NotNull Graph<PhysicalObject> getGraph();

  /**
   * load the system from a file.
   * @param path infers R text file with regulated input.
   * @return true if the load is complete with no error; else false.
   * @throws ExceptionGroup when cannot open the file inferred by path.
   */
  @SuppressWarnings("SameReturnValue")
  public boolean loadFromFile(String path) throws ExceptionGroup;

  /**
   * find R object with its name.
   *
   * @param name name of the object, either L or E.
   * @return the object.
   */
  @Nullable
  public PhysicalObject query(@NotNull String name);

  /**
   * get a set of tracks of the system.
   * @return all the tracks in circular orbit.
   */
  public Set<Double[]> getTracks();

  /**
   * get a copy of the collection in which objects are in the give track.
   * @param r the radius of the track
   * @return the set of objects.
   */
  @NotNull
  public Set<E> getObjectsOnTrack(double[] r);

  /**
   * get a copy of the collection in which objects are in the give track.
   * @param r the radius of the track
   * @return copy of the collection in which objects are on the given track.
   */
  @NotNull
  public Set<E> getObjectsOnTrack(Double[] r);


  /**
   * get the center of the system.
   * @return the center object.
   */
  @Nullable
  public L center();


  @NotNull Iterator<E> iterator();

  /**
   * process user operation.
   *
   * @param end what to do when end operation. (must be CircularOrbitHelper.refresh)
   * @return JFrame to control the CircularOrbit
   */
  public JFrame process(Consumer<CircularOrbit> end);

  /**
   * return the number of objects in system.
   * @return the number of objects on tracks. (center is NOT included)
   */
  int size();

  /**
   * transform a CircularOrbit to a list, to save as file, etc.
   * @return a list contains all the info of the CircularOrbit.
   */
  List<String> asList();
}
