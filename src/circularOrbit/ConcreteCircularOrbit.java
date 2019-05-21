package circularOrbit;

import static APIs.CircularOrbitAPIs.find_if;
import static APIs.CircularOrbitAPIs.getObjectDistributionEntropy;
import static APIs.CircularOrbitAPIs.transform;
import static APIs.CircularOrbitHelper.alert;
import static APIs.CircularOrbitHelper.generatePanel;
import static circularOrbit.PhysicalObject.getDefaultComparator;
import static exceptions.GeneralLogger.info;
import static factory.PhysicalObjectFactory.produce;

import APIs.CircularOrbitHelper;
import exceptions.GeneralLogger;
import graph.Graph;
import java.awt.Color;
import java.awt.FlowLayout;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import track.Track;

/**
 * Mutable. an implement of CircularOrbit.
 *
 * @param <L> the type of the center.
 * @param <E> type of the objects on object
 */
public abstract class ConcreteCircularOrbit<L extends PhysicalObject, E extends PhysicalObject>
    implements CircularOrbit<L, E> {

  /*
      RI: for each object, its track is in the set tracks;
      AF: AF(relationship) = the relationship graph of the objects.
        AF(centre) = the center of the circular orbit.
        AF(objects) = the sum of the objects on the orbit.
     */
  private final Graph<PhysicalObject> relationship = Graph.empty();
  private L centre = null;

  protected final Set<E> objects = new TreeSet<>(getDefaultComparator());
  protected final Set<Track> tracks = new HashSet<>();

  private Class<E> eClass;
  @SuppressWarnings({"unused", "FieldCanBeLocal"})
  private Class<L> lClass;
  private static Method refresh;
  protected Consumer<CircularOrbit> end;

  static {
    try {
      refresh = Class.forName("APIs.CircularOrbitHelper")
          .getDeclaredMethod("refresh", CircularOrbit.class, boolean.class);
      refresh.setAccessible(true);
    } catch (NoSuchMethodException | ClassNotFoundException e) {
      GeneralLogger.severe(e);
      System.exit(1);
    }
  }

  protected ConcreteCircularOrbit(Class<L> lClass, Class<E> eClass) {
    this.lClass = lClass;
    this.eClass = eClass;
  }

  @Override
  public boolean addTrack(double[] r) throws IllegalArgumentException {
    assert r.length > 0;
    if (r[0] < 0 && r[0] != -1) {
      throw new IllegalArgumentException(
          "warning: radius cannot be negative while not equal to -1. ");
    }
    info("addTrack", new String[]{Arrays.toString(r)});
    return tracks.add(new Track<>(r));
  }

  @Override
  public boolean removeTrack(double[] r) {
    assert r.length > 0;
    Track<E> tmp = new Track<>(r);
    var b = tracks.remove(tmp);
    var it = objects.iterator();
    while (it.hasNext()) {
      var e = it.next();
      if (e.getR().equals(tmp)) {
        assert b;
        relationship.remove(e);
        it.remove();
      }
    }
    info("removeTrack", new String[]{Arrays.toString(r)});
    return b;
  }

  @Override
  public L changeCentre(L newCenter) {
    if (Objects.equals(centre, newCenter)) {
      return centre;
    }
    info("changeCentre", new String[]{newCenter.toString()});
    L prev = centre;
    centre = newCenter;
    return prev;
  }

  /**
   * test if a track is exist in the circular orbit.
   *
   * @param r the radius of the orbit.
   * @return true if the track exist.
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  protected boolean findTrack(double[] r) {
    var tmp = new Track(r);
    return tracks.contains(tmp);
  }

  @Override
  public boolean moveObject(E obj, double[] to) {
    assert obj != null;
    assert to != null;
    if (!objects.contains(obj)) {
      return false;
    }
    var tmp = new Track<>(to);
    obj.setR(tmp);
    addTrack(to);

    info("moveObject", new String[]{obj.toString(), Arrays.toString(to)});
    return true;
  }

  @Override
  public boolean removeObject(E obj) {
    assert obj != null;
    info("removeObject", new String[]{obj.toString()});
    relationship.remove(obj);
    return objects.remove(obj);
  }

  @Override
  public void setRelation(PhysicalObject a, PhysicalObject b, float val) {
    assert a != null;
    assert b != null;
    assert !a.equals(b);
    relationship.add(a);
    relationship.add(b);
    relationship.set(a, b, val);
    info("setRelation", new String[]{a.toString(), b.toString(), String.valueOf(val)});
  }

  @NotNull
  @Override
  public Graph<PhysicalObject> getGraph() {
    return relationship;
  }

  @Override
  @Nullable
  public PhysicalObject query(@NotNull String objName) {
    info("query", new String[]{objName});
    final String name = objName.trim();
    if (centre.getName().equals(name)) {
      return centre;
    }
    return find_if(objects, e -> e.getName().equals(objName));
  }

  @Override
  public L center() {
    return centre;
  }

  @Override
  public Set<Double[]> getTracks() {
    Set<Double[]> ret = new TreeSet<>(
        Comparator.comparingDouble((Double[] a) -> a[0]).thenComparingDouble(a -> a[1]));
    transform(tracks, ret, Track::getRect_alt);
    return ret;
  }

  @Override
  public JFrame process(Consumer<CircularOrbit> end) {
    JFrame frame = new JFrame(getClass().getSimpleName());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.setLayout(null);
    return frame;
  }

  /**
   * add User Interface controls on the frame.
   *
   * @param frame where to add controls.
   * @return a panel that includes the control added.
   */
  protected JPanel test(JFrame frame) {
    end = CircularOrbitHelper.frame == null ? null : c -> {
      try {
        refresh.invoke(CircularOrbitHelper.frame, c, true);
      } catch (IllegalAccessException | InvocationTargetException e) {
        GeneralLogger.severe(e);
        System.exit(1);
      }
    };
    JPanel common = new JPanel();
    common.setLayout(new FlowLayout(FlowLayout.CENTER, 336, 8));
    common.setBorder(BorderFactory.createLineBorder(Color.decode("#673ab7"), 1, true));
    frame.add(common);

    JPanel trackOP = generatePanel("Track Operation");
    final JPanel objOP = generatePanel("Object Operation");
    final JPanel misc = generatePanel("Miscellaneous");
    var ops = new String[]{"Add", "Remove"};

    JComboBox<String> cmbOps = new JComboBox<>(ops);
    JTextField trackNum = new JTextField(8);
    JButton trackExec = new JButton("Execute");

    trackExec.addActionListener(e -> {
      double d;
      try {
        d = Double.valueOf(trackNum.getText().trim());
      } catch (NumberFormatException ex) {
        trackNum.setText("1");
        return;
      }
      switch (cmbOps.getSelectedIndex()) {
        case 0:
          addTrack(new double[]{d});
          checkRep();
          break;
        case 1:
          removeTrack(new double[]{d});
          break;
        default: assert false;
      }
      end.accept(this);
    });

    trackOP.add(cmbOps);
    trackOP.add(trackNum);
    trackOP.add(trackExec);
    common.add(trackOP);

    JComboBox<String> objOps = new JComboBox<>(ops);
    Set<Track> tmp = new TreeSet<>(Track.defaultComparator);
    transform(getTracks(), tmp, Track::new);
    JComboBox<Track> cmbTrackIndex = new JComboBox<>(tmp.toArray(new Track[0]));
    cmbTrackIndex.setEditable(true);
    JButton objExec = new JButton("Execute");

    objExec.addActionListener(e -> {
      switch (objOps.getSelectedIndex()) {
        case 0: {
          var form = CircularOrbitHelper.promptForm(frame, "Add object", E.hintForUser(eClass));
          if (form == null) {
            return;
          }
          var p = produce(eClass, form);
          addObject(eClass.cast(p));
          checkRep();                 //post condition
          break;
        }
        case 1:
          var p = cmbTrackIndex.getSelectedItem();
          if (!(p instanceof Track)) {
            cmbTrackIndex.setSelectedIndex(0);
            return;
          }
          Track r = (Track) p;
          String name = CircularOrbitHelper.prompt(frame, "name of the object",
              "Which object to remove? ", null);
          if (name == null) {
            return;
          }
          E o = find_if(objects, i -> i.getR().equals(r) && i.getName().equals(name));
          if (o == null) {
            alert(frame, "Delete object", name + "do not exist. ");
            return;
          }
          removeObject(o);
          break;
        default:
          return;
      }
      end.accept(this);
    });

    objOP.add(objOps);
    objOP.add(cmbTrackIndex);
    objOP.add(objExec);
    common.add(objOP);

    JButton btnEntropy = new JButton("Entropy");
    JButton btnLog = new JButton("Log Panel");
    JLabel lblResult = new JLabel("");
    btnEntropy.addActionListener(e -> lblResult.setText(
        String.valueOf(getObjectDistributionEntropy(this))));
    btnLog.addActionListener(e -> {
      var logPanel = CircularOrbitHelper.logPanel(frame);
      CircularOrbitHelper.frame.setVisible(false);
      logPanel.setVisible(true);
      CircularOrbitHelper.frame.setVisible(true);
    });
    misc.add(btnEntropy);
    misc.add(lblResult);
    misc.add(btnLog);
    common.add(misc);

    common.setBounds(8, 8, 336, 224);
    return common;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  @NotNull
  public Iterator<E> iterator() {
    return objects.iterator();
  }

  @Override
  public boolean addObject(E newObject) {
    assert newObject != null;
    info("addObject", new String[]{newObject.toString()});
    tracks.add(newObject.getR());
    return objects.add(newObject);
  }

  /**
   * get copy of objects on a track.
   * @param r the radius of the track
   * @return copy of the collection in which objects are on the given track.
   */
  @NotNull
  protected Set<E> getObjectsOnTrack(Track r) {
    assert r != null;
    final Set<E> ret = new TreeSet<>(E.getDefaultComparator());
    forEach(e -> {
      if (e.getR().equals(r)) {
        ret.add(eClass.cast(e));
      }
    });
    return ret;
  }

  @Override
  @NotNull
  public Set<E> getObjectsOnTrack(double[] r) {
    assert r.length > 0;
    return getObjectsOnTrack(new Track(r));
  }

  @Override
  @NotNull
  public Set<E> getObjectsOnTrack(Double[] r) {
    assert r.length > 0;
    return getObjectsOnTrack(new Track(r));
  }

  @Override
  public int size() {
    return objects.size();
  }

  protected void clearEmptyTrack() {
    tracks.removeIf(t -> find_if(objects, (E e) -> e.getR().equals(t)) == null);
  }
}