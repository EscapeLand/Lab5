package applications;

import static APIs.CircularOrbitHelper.generatePanel;

import APIs.CircularOrbitAPIs;
import APIs.CircularOrbitHelper;
import circularOrbit.CircularOrbit;
import circularOrbit.ConcreteCircularOrbit;
import circularOrbit.PhysicalObject;
import exceptions.ExceptionGroup;
import exceptions.GeneralLogger;
import exceptions.LogicErrorException;
import factory.PhysicalObjectFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StellarSystem extends ConcreteCircularOrbit<FixedStar, Planet> {

  private Thread loop;
  private double time = 0;
  private double timeSpan = 1e16;
  private Runnable update;
  private static Method run;

  static {
    try {
      run = Class.forName("APIs.CircularOrbitHelper").getDeclaredMethod(
          "run", StellarSystem.class);
      run.setAccessible(true);

    } catch (NoSuchMethodException | ClassNotFoundException e) {
      GeneralLogger.severe(e);
      System.exit(1);
    }
  }

  /**
   * a StellarSystem, with the track object is planet, and central object is stellar.
   */
  public StellarSystem() {
    super(FixedStar.class, Planet.class);
    update = () -> {
      try {
        run.invoke(CircularOrbitHelper.frame, this);
      } catch (IllegalAccessException | InvocationTargetException e) {
        GeneralLogger.severe(e);
        System.exit(1);
      }
    };
  }

  /**
   * start a new thread to update the User Interface.
   */
  public void start() {
    loop = new Thread(update);
    loop.start();
  }

  @Override
  public boolean loadFromFile(String path) throws ExceptionGroup {
    File file = new File(path);
    ExceptionGroup exs = new ExceptionGroup();
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      for (String buffer = reader.readLine(); buffer != null; buffer = reader.readLine()) {
        try {
          if (buffer.isEmpty()) {
            continue;
          }
          Matcher m = Pattern.compile("([a-zA-Z]+)\\s?::=\\s?<(.*)>").matcher(buffer);
          if (!m.find() || m.groupCount() != 2) {
            throw new IllegalArgumentException(
                "regex: (" + buffer + "), didn't match, continued. ");
          }
          switch (m.group(1)) {
            case "Stellar": {
              String[] list = m.group(2).split(",");
              if (list.length != 3) {
                throw new IllegalArgumentException("regex: Stellar: not 3 args. continued. ");
              }

              FixedStar f = new FixedStar(list[0], Double.valueOf(list[1]),
                  Double.valueOf(list[2]));
              changeCentre(f);
              break;
            }
            case "Planet": {
              String[] list = m.group(2).split(",");

              if (list.length != 8) {
                exs.join(new IllegalArgumentException("regex: Planet: not 8 args. continued. "));
              }
              if (query(list[0]) != null) {
                throw new LogicErrorException(list[0] + " already exist. continued. ");
              }
              PhysicalObject p = PhysicalObjectFactory.produce(Planet.class, list);
              assert p instanceof Planet;
              if (!addObject(new PlanetarySystem((Planet) p))) {
                throw new LogicErrorException(list[0] + " already exist. continued. ");
              }
              break;
            }
            default:
              throw new IllegalArgumentException(
                  "regex: unexpected label: " + m.group(1) + " continued. ");
          }
        } catch (IllegalArgumentException | LogicErrorException e) {
          exs.join(e);
        }
      }
    } catch (IOException e) {
      exs.join(e);
      throw exs;
    }

    if (center() == null) {
      exs.join(new LogicErrorException("no Central Object. returned. "));
    }
    if (!exs.isEmpty()) {
      throw exs;
    } else {
      return true;
    }
  }

  @Override
  public JFrame process(Consumer<CircularOrbit> refresh) {
    JFrame frame = super.process(refresh);
    this.test(frame);

    frame.setBounds(1000, 232, 388, 512);
    frame.setVisible(true);
    return frame;
  }

  @Override
  protected JPanel test(JFrame frame) {
    var par = super.test(frame);
    JPanel spec = new JPanel();
    par.setSize(new Dimension(352, par.getHeight()));
    spec.setBounds(8, par.getY() + par.getHeight() + 8, 352, 224);
    spec.setLayout(new FlowLayout(FlowLayout.CENTER, 336, 8));
    spec.setBorder(BorderFactory.createLineBorder(Color.decode("#e91e63"), 1, true));
    frame.add(spec);

    JPanel pnlTimeAt = generatePanel("State at Time");
    JLabel lblTimeAt = new JLabel("Time at: ");
    JTextField txtTimeAt = new JTextField(8);
    txtTimeAt.setText(String.valueOf(time));
    JButton btnTimeApply = new JButton("Apply");
    pnlTimeAt.add(lblTimeAt);
    pnlTimeAt.add(txtTimeAt);
    pnlTimeAt.add(btnTimeApply);
    spec.add(pnlTimeAt);

    JPanel pnlCalc = generatePanel("Physical Distance");
    JLabel lblCalc = new JLabel("Distance between ");
    final JLabel lblAnd = new JLabel(" and ");
    JLabel lblRes = new JLabel();
    JTextField txtA = new JTextField("Neptune");
    JTextField txtB = new JTextField("Mercury");
    JButton btnCalc = new JButton("=");
    btnCalc.addActionListener(e -> {
      lblCalc.setVisible(false);
      PhysicalObject o1 = query(txtA.getText().trim());
      PhysicalObject o2 = query(txtB.getText().trim());
      if (o1 instanceof Planet && o2 instanceof Planet) {
        lblRes.setText(String.valueOf(CircularOrbitAPIs.getPhysicalDistance(this, o1, o2)));
      } else {
        lblRes.setText("Didn't match. ");
      }
    });
    pnlCalc.add(lblCalc);
    pnlCalc.add(txtA);
    pnlCalc.add(lblAnd);
    pnlCalc.add(txtB);
    pnlCalc.add(btnCalc);
    pnlCalc.add(lblRes);
    spec.add(pnlCalc);

    final JPanel pnlCtrl = generatePanel("Controls");
    JButton btnReset = new JButton("Reset");
    JButton btnPause = new JButton("Pause");
    final JButton btnTimeSpanApply = new JButton("Apply");
    JTextField txtTimeSpan = new JTextField(4);
    txtTimeSpan.setText(String.valueOf(timeSpan));
    btnReset.addActionListener(e -> {
      this.reset();
      btnPause.setText("Resume");
      end.accept(this);
    });
    btnPause.addActionListener(e -> {
      switch (btnPause.getText()) {
        case "Resume":
          start();
          btnPause.setText("Pause");
          break;
        case "Pause":
          loop.interrupt();
          btnPause.setText("Resume");
          break;
        default: assert false;
      }
    });
    btnTimeSpanApply.addActionListener(e -> {
      try {
        this.setTimeSpan(Double.valueOf(txtTimeSpan.getText().trim()));
      } catch (NumberFormatException ex) {
        txtTimeSpan.setText(String.valueOf(timeSpan));
      }
    });
    btnTimeApply.addActionListener(e -> {
      try {
        setTime(Double.valueOf(txtTimeAt.getText().trim()));
        btnPause.setText("Resume");
      } catch (NumberFormatException ex) {
        txtTimeAt.setText(String.valueOf(time));
        return;
      }
      end.accept(this);
    });
    pnlCtrl.add(btnReset);
    pnlCtrl.add(btnPause);
    pnlCtrl.add(txtTimeSpan);
    pnlCtrl.add(btnTimeSpanApply);
    spec.add(pnlCtrl);

    return spec;
  }

  @Override
  public boolean addObject(@NotNull Planet newObject) {
    if (tracks.contains(newObject.getR())) {
      return false;
    } else {
      return super.addObject(newObject);
    }
  }

  @Override
  public boolean removeObject(@NotNull Planet obj) {
    return super.removeTrack(obj.getR().getRect());
  }

  /**
   * update the system, as it is at ({@code time += timeSpan}).
   */
  public void nextTime() {
    this.time += timeSpan;
    forEach(p -> p.nextTime(time));
  }

  /**
   * calculate the stellar system at a specific time.
   *
   * @param time the time to calculate.
   */
  private void setTime(double time) {
    if (loop != null) {
      loop.interrupt();
    }
    this.time = time;
    forEach(p -> p.nextTime(time));
  }

  /**
   * reset the system to its initial state.
   */
  void reset() {
    setTime(0);
  }

  /**
   * set how much time is covered in 60ms. the greater, the faster the planets runs.
   *
   * @param timeSpan time span.
   */
  void setTimeSpan(double timeSpan) {
    this.timeSpan = timeSpan;
  }

  @Override
  public void checkRep() {
    var tracks = getTracks();
    tracks.forEach(doubles -> {
      assert getObjectsOnTrack(doubles).size() <= 1;
    });
    //NOTE: the planet may revolution in the stellar???
    //var center = center();
    //double radius = center == null ? 0 : center.radius;
    double r = 0;
    for (Planet e : objects) {
      assert e.getR().getRect()[1] > r;
      r = e.getR().getRect()[0] + e.radius;
    }
  }
}

@SuppressWarnings("unused")
final class FixedStar extends PhysicalObject {

  public final double radius;
  public final double mass;
  public static String[] hint = new String[]{"Name", "Stellar radius", "Mass"};

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FixedStar)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    FixedStar fixedStar = (FixedStar) o;
    return fixedStar.getR().equals(getR())
        && Double.compare(fixedStar.mass, mass) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getR(), mass);
  }

  @Override
  public FixedStar clone() {
    return new FixedStar(getName(), radius, mass);
  }

  FixedStar(String name, double radius, double mass) {
    super(name, new double[]{radius}, 0);
    this.radius = radius;
    this.mass = mass;
  }

  @Override
  public String toString() {
    return getName();
  }
}

@SuppressWarnings("unused")
class Planet extends PhysicalObject {

  private final String color;
  private final Form form;

  /**
   * radius of the planet.
   */
  public final double radius;
  public final double v;
  public static String[] hint = new String[]{"Name", "Form", "Color", "Planet radius",
      "Revolution radius", "Revolution speed", "Direction", "Position"};

  enum Form {
    Solid, Liquid, Gas
  }

  enum Dir {
    CW, CCW
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Planet)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Planet planet = (Planet) o;
    return Objects.equals(planet.getR(), getR())
        && Double.compare(planet.v, v) == 0
        && getColor().equals(planet.getColor())
        && getForm() == planet.getForm();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getColor(), getForm(), getR(), v);
  }

  @Override
  public Planet clone() {
    var tmp = new Planet(getName(), getForm(), getColor(), radius, getR().getRect(), v, getDir(),
        pos_init);
    tmp.setPos(getPos());
    return tmp;
  }

  void nextTime(double time) {
    setPos(pos_init + v * time);
  }

  Form getForm() {
    return form;
  }

  String getColor() {
    return color;
  }

  Dir getDir() {
    return v < 0 ? Dir.CW : Dir.CCW;
  }

  /**
   * a planet.
   * @param name name of the planet
   * @param form form of the planet
   * @param color color of the planet
   * @param radius radius of the planet itself
   * @param track radius of the planet orbit radius
   * @param v radius of its revolution speed
   * @param dir direction of its revolution
   * @param pos init pos of the planet.
   */
  Planet(String name, Form form, String color, double radius,
      double[] track, double v, Dir dir, double pos) {
    super(name, track, pos);
    this.color = color;
    this.form = form;
    this.radius = radius;
    this.v = (dir == Dir.CW ? -1 : 1) * Math.abs(v / track[0]);
  }

  @Override
  public String toString() {
    return getName();
  }
}

@SuppressWarnings("unused")
final class PlanetarySystem extends Planet {

  private Set<Planet> satellites = new TreeSet<>(PhysicalObject.getDefaultComparator());
  public static String[] hint = new String[]{"Name", "Form", "Color", "Planet radius",
      "Revolution radius", "Revolution speed", "Direction", "Position"};

  PlanetarySystem(@NotNull Planet center) {
    super(center.getName(), center.getForm(), center.getColor(), center.radius,
        center.getR().getRect(), center.v, center.getDir(), center.getPos());
  }

  public boolean addSatellite(@NotNull Planet satellite) {
    return satellites.add(satellite);
  }

  @NotNull
  public Planet[] satellites() {
    return satellites.toArray(new Planet[0]);
  }

  @Nullable
  public Planet query(@NotNull String name) {
    return CircularOrbitAPIs.find_if(satellites, (Planet p) -> p.getName().equals(name));
  }
}