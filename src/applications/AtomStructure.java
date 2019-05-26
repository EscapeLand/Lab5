package applications;

import static APIs.CircularOrbitHelper.generatePanel;
import static exceptions.GeneralLogger.info;
import static factory.PhysicalObjectFactory.produce;

import APIs.Tools;
import circularOrbit.CircularOrbit;
import circularOrbit.ConcreteCircularOrbit;
import circularOrbit.PhysicalObject;
import exceptions.ExceptionGroup;
import exceptions.LogicErrorException;
import factory.CircularOrbitFactory;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import track.Track;

public final class AtomStructure extends ConcreteCircularOrbit<Kernel, Electron> {

  private final Caretaker caretaker = new Caretaker();

  public AtomStructure() {
    super(Kernel.class, Electron.class);
  }

  @Override
  public boolean loadFromFile(String path) throws ExceptionGroup {
    Pattern[] patterns = {
        Pattern.compile("ElementName\\s?::= ([A-Z][a-z]{0,2})"),
        Pattern.compile("NumberOfElectron\\s?::= ((?:\\d+[/;]?)+)"),
        Pattern.compile("NumberOfTracks\\s?::= (\\d+)")
    };
    ExceptionGroup exs = new ExceptionGroup();

    var cf = CircularOrbitFactory.getDefault();
    var txt = cf.read(path);
    if (txt == null) {
      exs.join(new IllegalArgumentException("reading " + path + " failed. returned. "));
      throw exs;
    }
    txt.sort(String::compareTo);

    boolean[] flag = new boolean[]{false, false, false};
    for (int i = 0; i < 3; i++) {
      final int tmp = i;
      txt.forEach(s -> {
        Matcher m = patterns[tmp].matcher(s);
        if (!m.find() || m.groupCount() != 1) {
          return;
        }
        var match = m.group(1);
        if (flag[tmp]) {
          exs.join(new LogicErrorException("repetitive label in " + s));
          return;
        }
        switch (tmp) {
          case 0:
            changeCentre(new Kernel(match));
            break;
          case 1: {
            String[] tae = match.split("[/;]");
            if (tae.length % 2 == 1) {
              exs.join(new LogicErrorException("lack of an arg. the last arg ("
                  + tae[tae.length - 1] + ") is ignored. "));
            }
            assert objects.isEmpty();
            for (int j = 0; j < tae.length; j += 2) {
              int n;
              try {
                n = Integer.valueOf(tae[j + 1]);
                if (n < 0) {
                  throw new IllegalArgumentException();
                }
                for (int k = 0; k < n; k++) {
                  //noinspection StatementWithEmptyBody
                  while (!addObject((Electron) produce(Electron.class, new String[]{tae[j]}))) {
                  }
                }
              } catch (IllegalArgumentException e) {
                exs.join(new IllegalArgumentException("cannot parse electron number: "
                    + tae[j] + '/' + tae[j + 1] + ". continued. "));
              }
            }
            break;
          }
          case 2: {
            int n;
            try {
              n = Integer.valueOf(match);
              if (tracks.size() != n) {
                exs.join(new IllegalArgumentException("track number != tracks.size ("
                    + n + "!=" + tracks.size() + "). continued. "));
                return;
              }
            } catch (IllegalArgumentException e) {
              exs.join(e);
              return;
            }

            for (int j = 1; j <= Integer.valueOf(match); j++) {
              try {
                addTrack(new double[]{j});
              } catch (IllegalArgumentException e) {
                exs.join(e);
              }
            }
            break;
          }
          default: assert false;
        }
        flag[tmp] = true;
      });
    }

    if (!flag[0]) {
      exs.join(new LogicErrorException("ElementName is not set. returned. "));
    }
    if (!flag[1]) {
      exs.join(new LogicErrorException("NumberOfElectron is not set. returned. "));
    }
    if (!flag[2]) {
      exs.join(new LogicErrorException("NumberOfTracks is not set. returned. "));
    }

    if (!exs.isEmpty()) {
      throw exs;
    } else {
      return true;
    }
  }

  @Override
  public void checkRep() {
  }

  /**
   * transit a number of electron.
   * @param from transit from.
   * @param to transit to.
   * @param number how many electrons to transit.
   * @return true if success.
   * @apiNote only AtomStructure can transit.
   */
  private boolean transit(double[] from, double[] to, int number) {
    if (from == to) {
      return false;
    }
    if (!findTrack(from) || !findTrack(to)) {
      return false;
    }
    //TODO boolean up = to[1] > from[0];
    boolean up = to[0] > from[0];
    Track transFrom = new Track(from);

    caretaker.setMementos(from, to, saveMemento(from, to));

    for (int i = 0; i < number; i++) {
      Electron e = Tools.find_if(this,
          t -> t.getR().equals(transFrom) && t.isGround() == up);
      if (e == null || !moveObject(e, to)) {
        recover(from, to);
        return false;
      }
      e.switchState(!up);
    }
    info(
        Arrays.toString(from), Arrays.toString(to), String.valueOf(number));
    return true;
  }

  @Override
  public JFrame process(Consumer<CircularOrbit> refresh) {
    var frame = super.process(refresh);
    var spc = this.test(frame);

    frame.setBounds(1000, 232, 364, spc.getY() + spc.getHeight() + 48);
    frame.setVisible(true);
    return frame;
  }

  @Override
  public List<String> asList() {
    throw new RuntimeException("not implement");
  }

  @Override
  protected JPanel test(JFrame frame) {
    var par = super.test(frame);
    JPanel spec = new JPanel();
    spec.setBounds(8, par.getY() + par.getHeight() + 8, 336, 84);
    spec.setLayout(new FlowLayout(FlowLayout.CENTER, 336, 8));
    spec.setBorder(BorderFactory.createLineBorder(Color.decode("#e91e63"), 1, true));
    frame.add(spec);

    JPanel panel = generatePanel("Transit");
    spec.add(panel);
    panel.setBounds(8, 176, 336, 32);

    Set<Track> tmp = new TreeSet<>(Track.defaultComparator);
    Tools.transform(getTracks(), tmp, Track::new);
    JComboBox<Track> cmbS1 = new JComboBox<>(tmp.toArray(new Track[0]));
    JComboBox<Track> cmbS2 = new JComboBox<>(tmp.toArray(new Track[0]));
    cmbS2.setSelectedIndex(1);
    JButton btnTransit = new JButton("Transit");
    JTextField txtNum = new JTextField("  1");

    panel.add(cmbS1);
    panel.add(btnTransit);
    panel.add(cmbS2);
    panel.add(txtNum);

    btnTransit.addActionListener(e -> {
      Track from = (Track) cmbS1.getSelectedItem();
      Track to = (Track) cmbS2.getSelectedItem();
      assert from != null && to != null;
      try {
        if (transit(from.getRect(), to.getRect(), Integer.valueOf(txtNum.getText().trim()))) {
          end.accept(this);
        }
      } catch (NumberFormatException ex) {
        txtNum.setText("  1");
      }

    });

    return spec;
  }

  /**
   * recover the atom system when transit failed.
   *
   * @param from transit from
   * @param to transit to
   * @see AtomStructure#transit(double[], double[], int)
   */
  private void recover(double[] from, double[] to) {
    var rec = caretaker.getMementos(from, to);
    if (rec == null) {
      throw new RuntimeException(
          "cannot recover: " + Arrays.toString(from) + "->" + Arrays.toString(to));
    }
    removeTrack(from);
    removeTrack(to);
    addTrack(from);
    addTrack(to);
    objects.addAll(rec.getFrom());
    objects.addAll(rec.getTo());
    caretaker.destroyMementos(from, to);
  }

  /**
   * Originator.
   *
   * @param from transit from
   * @param to transit to.
   * @return Memento of the origin state.
   */
  private Memento<Electron> saveMemento(double[] from, double[] to) {
    return new Memento<>(getObjectsOnTrack(from), getObjectsOnTrack(to));
  }

  static final class Memento<E extends PhysicalObject> {

    private final Set<E> from;
    private final Set<E> to;

    Set<E> getFrom() {
      return from;
    }

    public Set<E> getTo() {
      return to;
    }

    Memento(Set<E> from, Set<E> to) {
      this.from = from;
      this.to = to;
    }
  }
}

@SuppressWarnings("CheckStyle")
final class Caretaker {

  final class Pair {

    final double[] first;
    final double[] second;

    Pair(double[] first, double[] second) {
      this.first = first;
      this.second = second;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Pair)) {
        return false;
      }
      Pair pair = (Pair) o;
      return Arrays.equals(first, pair.first)
          && Arrays.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
      int result = Arrays.hashCode(first);
      result = 31 * result + Arrays.hashCode(second);
      return result;
    }
  }

  private final Map<Pair, AtomStructure.Memento<Electron>> mementos = new HashMap<>();

  @Nullable
  AtomStructure.Memento<Electron> getMementos(double[] from, double[] to) {
    return mementos.get(new Pair(from, to));
  }

  void setMementos(double[] from, double[] to, AtomStructure.Memento<Electron> mementos) {
    this.mementos.put(new Pair(from, to), mementos);
  }

  void destroyMementos(double[] from, double[] to) {
    mementos.remove(new Pair(from, to));
  }
}

@SuppressWarnings("CheckStyle")
final class Electron extends PhysicalObject {

  private ElectronState state = new Ground();
  @SuppressWarnings("unused")
  public static String[] hint = new String[]{"Radius"};

  Electron(double r) {
    super("e", new double[]{r}, 360 * Math.random());
  }

  void switchState(boolean ground) {
    if (ground) {
      state = new Ground();
    } else {
      state = new Excited();
    }
  }

  boolean isGround() {
    return state.isGround();
  }

  @NotNull
  @Override
  public String toString() {
    return "Electron{" + getR().toString()
        + ", " + state.toString()
        + "}";
  }

  @Override
  public Electron clone() {
    Electron e = new Electron(R_init.getRect()[0]);
    e.setR(getR());
    e.state = state.isGround() ? new Ground() : new Excited();
    return e;
  }
}

@SuppressWarnings({"unused", "CheckStyle"})
final class Kernel extends PhysicalObject {

  private int proton;
  private int neutron;
  public String[] hint = new String[]{"Name"};

  Kernel(String name) {
    super(name, new double[]{0}, 0);
  }

  @Override
  public PhysicalObject clone() {
    return new Kernel(getName());
  }

  public int getProton() {
    return proton;
  }

  public void setProton(int proton) {
    this.proton = proton;
  }

  public int getNeutron() {
    return neutron;
  }

  public void setNeutron(int neutron) {
    this.neutron = neutron;
  }
}