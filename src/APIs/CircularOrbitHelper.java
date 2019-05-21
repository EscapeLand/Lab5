package APIs;

import static APIs.CircularOrbitAPIs.transform;
import static exceptions.GeneralLogger.info;
import static exceptions.GeneralLogger.loadInfo;
import static exceptions.GeneralLogger.loadWarning;
import static exceptions.GeneralLogger.warning;
import static java.lang.Thread.interrupted;

import applications.StellarSystem;
import circularOrbit.CircularOrbit;
import circularOrbit.PhysicalObject;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import exceptions.ExceptionGroup;
import exceptions.GeneralLogger;
import factory.CircularOrbitFactory;
import factory.DefaultCircularOrbitFactory;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import track.Track;


public class CircularOrbitHelper<L extends PhysicalObject, E extends PhysicalObject> extends
    JFrame {

  private final mxGraph graph;
  private final Object parent;
  private double scale;
  private final double length;
  private final Map<Double[], Double[]> radiusMap = new TreeMap<>(
      Comparator.comparingDouble((Double[] a) -> a[0]).thenComparingDouble(a -> a[1]));
  private final Map<PhysicalObject, Object> cells;
  private final Set<Object> circles = new HashSet<>();
  public static CircularOrbitHelper frame;

  {
    cells = new TreeMap<>(PhysicalObject.getDefaultComparator());
  }

  private CircularOrbitHelper(@NotNull CircularOrbit<L, E> c,
      @SuppressWarnings("SameParameterValue") int length) {
    super(c.toString());
    graph = new mxGraph();
    parent = graph.getDefaultParent();
    this.length = length;
    mxGraphComponent graphComponent = new mxGraphComponent(graph);
    getContentPane().add(graphComponent);

    refresh(c, false);
  }

  @SuppressWarnings("unchecked")
  private static <L extends PhysicalObject, E extends PhysicalObject> void visualize(
      CircularOrbit<L, E> c) {
    if (frame != null) {
      frame.setVisible(true);
      return;
    }
    frame = new CircularOrbitHelper<>(c, 784);
    if (c instanceof StellarSystem) {
      //((StellarSystem) c).register(() -> frame.run((StellarSystem) c));
      ((StellarSystem) c).start();
    }
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    info("visualize", new String[]{c.toString()});
    c.process(c1 -> frame.refresh(c1, true));
  }

  /**
   * pop a inputBox.
   * @param owner owner of the dialog.
   * @param title title of the dialog.
   * @param msg msg to show.
   * @param def default text in the inputBox.
   * @return user input. null if dialog is closed.
   */
  @Nullable
  public static String prompt(@Nullable JFrame owner, String title, String msg,
      @Nullable String def) {
    final StringBuffer p = new StringBuffer();
    var f = new Object() {
      boolean flag = false;
    };
    class PromptDialog extends JDialog {

      private PromptDialog() {
        super(owner, title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(400, 200, 368, 128);
        Container panel = getContentPane();
        panel.setLayout(null);
        JLabel lbl;
        JTextField txt;
        JButton btn;
        panel.add(lbl = new JLabel(msg));
        panel.add(txt = new JTextField(def));
        panel.add(btn = new JButton("OK"));

        lbl.setBounds(8, 8, 256, 24);
        txt.setBounds(8, 40, 256, 24);
        btn.setBounds(288, 40, 56, 24);

        if (def != null) {
          txt.setCaretPosition(def.length());
        }

        ActionListener act = e -> {
          p.append(txt.getText());
          f.flag = true;
          this.dispose();
        };
        btn.addActionListener(act);

        txt.registerKeyboardAction(act,
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
            JComponent.WHEN_FOCUSED);

        setModal(true);
      }
    }

    var dialog = new PromptDialog();
    dialog.setVisible(true);
    return f.flag ? p.toString() : null;
  }

  /**
   * pop up a form to gather user input.
   * @param owner owner of the dialog.
   * @param title title of the dialog.
   * @param form array of questions.
   * @return array of answers.
   */
  @Nullable
  public static String[] promptForm(@Nullable JFrame owner, String title, @NotNull String[] form) {
    class Flag {
      boolean flag = false;
    }

    JTextField[] formArray = new JTextField[form.length];
    String[] input = new String[form.length];
    Flag f = new Flag();

    class PromptDialog extends JDialog {

      private PromptDialog() {
        super(owner, title);
        int y = 8;
        Container panel = getContentPane();
        panel.setLayout(null);

        for (int i = 0; i < form.length; i++) {
          JLabel lbl;
          panel.add(lbl = new JLabel(form[i]));
          panel.add(formArray[i] = new JTextField());
          lbl.setBounds(8, y, 256, 24);
          y += 32;
          formArray[i].setBounds(8, y, 256, 24);
          y += 32;
        }

        JButton btn = new JButton("OK");
        panel.add(btn);
        btn.setBounds(200, y, 56, 24);
        setBounds(400, 200, 292, y + 68);

        btn.addActionListener(e -> {
          for (int i = 0; i < formArray.length; i++) {
            input[i] = formArray[i].getText();
          }
          f.flag = true;
          this.dispose();
        });
        setModal(true);
      }
    }

    PromptDialog dialog = new PromptDialog();
    dialog.setVisible(true);
    return f.flag ? input : null;
  }

  public static void alert(@Nullable JFrame owner, String title, String msg) {
    JOptionPane.showMessageDialog(owner, msg, title, JOptionPane.ERROR_MESSAGE);
  }

  void refresh(CircularOrbit<L, E> c, boolean refreshCircle) {
    Set<Double[]> tracks = c.getTracks();
    Consumer<PhysicalObject> addCell = x -> {
      if (x != null) {
        cells.put(x, label(x));
      }
    };

    scale = length / 2 / tracks.size();
    radiusMap.clear();
    tracks.forEach(f -> {
      if (f[0] > 0) {
        var tmp = (radiusMap.size() + 1) * scale;
        radiusMap.put(f, new Double[]{tmp, tmp * f[1] / f[0]});
      }

    });
    assert radiusMap.size() <= tracks.size();

    tracks.clear();

    setSize((int) (length * 1.1), (int) (length * 1.1));

    graph.getModel().beginUpdate();

    graph.removeCells(cells.values().toArray(), true);
    cells.clear();

    if (refreshCircle) {
      graph.removeCells(circles.toArray());
      circles.clear();
    }

    radiusMap.values().forEach(d -> circles.add(circle(d)));

    try {
      addCell.accept(c.center());
      c.forEach(addCell);
      final var pg = c.getGraph().edges();
      pg.forEach((e, f) -> {
        PhysicalObject a = (PhysicalObject) e[0];
        PhysicalObject b = (PhysicalObject) e[1];
        if (Track.compare(a.getR(), b.getR()) <= 0) {
          line(cells.get(a), cells.get(b), f.toString());
        }
      });
    } finally {
      graph.getModel().endUpdate();
    }
  }

  @SuppressWarnings("unused")
  private void run(@NotNull StellarSystem s) {
    info("run", new String[]{s.toString()});
    Map<PhysicalObject, double[]> current = new HashMap<>();
    cells.forEach((p, o) -> current.put(p, xy(p)));

    while (!interrupted()) {
      s.nextTime();
      cells.forEach((p, c) -> {
        var now = xy(p);
        double x = now[0];
        double y = now[1];
        var tmp = current.get(p);
        x -= tmp[0];
        y -= tmp[1];
        tmp[0] = now[0];
        tmp[1] = now[1];
        graph.moveCells(new Object[]{c}, x, y);
      });
      try {
        Thread.sleep(60);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  private Object circle(Double[] r) {
    if (r[0] < 0) {
      return null;
    }
    final var style = "shape=ellipse;fillColor=none;"
        + "movable=0;resizable=0;editable=0;connectable=0;";
    double baseX = length / 2 - r[0] + 16;
    double baseY = length / 2 - r[1] + 16;
    return graph.insertVertex(parent, null, "", baseX, baseY, 2 * r[0], 2 * r[1], style);
  }

  private double[] xy(PhysicalObject p) {
    final double[] ret = new double[2];
    Double[] r;
    if (p.getR().getRect()[0] == -1) {
      r = new Double[]{scale * (radiusMap.size() + 1), scale * (radiusMap.size() + 1)};
    } else {
      r = radiusMap.get(p.getR().getRect_alt());
    }
    if (r == null) {
      r = new Double[]{0.0, 0.0};
    }
    ret[0] = length / 2.0 + r[0] * Math.cos(Math.toRadians(p.getPos())) + 16;
    ret[1] = length / 2.0 + r[1] * Math.sin(Math.toRadians(p.getPos())) + 16;
    return ret;
  }

  private Object label(@NotNull PhysicalObject p) {
    final var style = "shape=ellipse;movable=0;resizable=0;editable=0;connectable=0;";
    var tmp = xy(p);
    String name = p.getName();
    Font font = getContentPane().getComponent(0).getFont();
    FontMetrics fm = getContentPane().getComponent(0).getFontMetrics(font);
    double x = tmp[0];
    double y = tmp[1];
    double w = fm.stringWidth(name) * 1.4;
    return graph.insertVertex(parent, p.toString(), p.getName(), x - w / 2, y - w / 2, w, w, style);
  }

  @SuppressWarnings("UnusedReturnValue")
  private Object line(Object a, Object b, String label) {
    String style =
        "edgeStyle=none;rounded=0;orthogonalLoop=1;jettySize=auto;dashed=1;endArrow=none;endFill=0;"
            +
            "editable=0;bendable=0;movable=0;jumpStyle=none;";
    return graph.insertEdge(parent, null, label, a, b, style);
  }

  /**
   * pop up the log panel.
   * @param owner owner of the dialog.
   * @return the panel object. (JDialog)
   */
  @NotNull
  public static JDialog logPanel(JFrame owner) {
    final String info = "log/info.log";
    final String warning = "log/warning.log";

    class LogPanel extends JDialog {

      @SuppressWarnings("unchecked")
      private LogPanel() {
        super(owner, "Log");
        setBounds(280, 112, 600, 560);
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);

        final var ref = new Object() {
          List<List> list;
        };
        try {
          ref.list = loadInfo(info);
        } catch (IOException e) {
          alert(owner, "Error", "error when loading log. ");
          System.exit(1);
        }

        JPanel query = generatePanel("Query(Regex supported)");
        JComboBox<String> cmbInfo = new JComboBox<>(new String[]{"Instance", "Level", "Operation"});
        JComboBox<String> cmbWarn = new JComboBox<>(
            new String[]{"Instance", "Level", "Exception", "Class/Method"});
        JTextField txtQuery = new JTextField(32);
        JButton btnQuery = new JButton("Query");

        query.add(cmbInfo);
        query.add(cmbWarn);
        query.add(txtQuery);
        query.add(btnQuery);
        cmbWarn.setVisible(false);

        JPanel file = generatePanel("Select File");
        JComboBox<String> cmbFile = new JComboBox<>(new String[]{"INFO", "WARNING"});
        JLabel lblFile = new JLabel(info);
        cmbFile.addItemListener(e -> {
          try {
            switch (cmbFile.getSelectedIndex()) {
              case 0:
                lblFile.setText(info);
                ref.list = loadInfo(info);
                cmbInfo.setVisible(true);
                cmbWarn.setVisible(false);
                break;
              case 1:
                lblFile.setText(warning);
                ref.list = loadWarning(warning);
                cmbInfo.setVisible(false);
                cmbWarn.setVisible(true);
                break;
              default: assert false;
            }
          } catch (IOException ex) {
            alert(owner, "Error", "error when loading log. ");
            System.exit(1);
          }

        });
        file.add(cmbFile);
        file.add(lblFile);

        JList<String> lstQuery = new JList<>();
        JScrollPane scpList = new JScrollPane(lstQuery);
        scpList.setPreferredSize(new Dimension(564, 320));

        ActionListener act = e -> {
          int q;
          switch (cmbFile.getSelectedIndex()) {
            case 0:
              q = cmbInfo.getSelectedIndex();
              break;
            case 1:
              q = cmbWarn.getSelectedIndex();
              break;
            default:
              return;
          }

          List<String> res = new ArrayList<>();

          ref.list.forEach(l -> {
            var obj = l.get(q);
            String reg = txtQuery.getText();
            if (reg.charAt(0) != '^') {
              reg = ".*" + reg;
            }
            if (reg.charAt(reg.length() - 1) != '$') {
              reg += ".*";
            }
            try {
              if ((obj.toString().matches("(?i)" + reg))) {
                List<String> tmp = new ArrayList<>(l.size());
                transform(l, tmp, Object::toString);
                res.add(String.join(" ", tmp));
              }
            } catch (IllegalArgumentException ex) {
              res.add(ex.getLocalizedMessage());
            }

          });

          lstQuery.setListData(res.toArray(new String[0]));
        };
        btnQuery.addActionListener(act);
        txtQuery.registerKeyboardAction(act,
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
            JComponent.WHEN_FOCUSED);
        add(file);
        add(query);
        add(scpList);
      }
    }

    return new LogPanel();
  }

  /**
   * generate a styled panel.
   * @param title title of the object.
   * @return JPanel, panel object.
   */
  public static JPanel generatePanel(String title) {
    JPanel n = new JPanel();
    n.setBorder(BorderFactory.createTitledBorder(title));
    n.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 8));
    return n;
  }

  /**
   * visualize the circular orbit and show up a control panel.
   * @param args main args
   */
  public static void main(String[] args) {
    CircularOrbitFactory factory = new DefaultCircularOrbitFactory();
    CircularOrbit s;
    Properties prop = new Properties();
    String last = null;

    while (true) {
      try {
        InputStream in = new BufferedInputStream(new FileInputStream("history.properties"));
        prop.load(in);
        last = prop.getProperty("lastLoad");
        in.close();
      } catch (IOException e) {
        System.out.println("INFO: property not load. continued. ");
        info("property not load. continued. ");
      } finally {
        if (last == null) {
          last = "input/";
        }
        last = prompt(null,
            "Load From", "input the path of the config file. ", last);
      }

      if (last == null) {
        info("User exit. ");
        return;
      }

      try {
        s = factory.createAndLoad(last);
      } catch (ExceptionGroup exs) {
        alert(null, "Errors in profile", exs.getMessage());
        warning(exs);
        continue;
      } catch (RuntimeException e) {
        alert(null, "Error", e.getMessage());
        warning(e);
        continue;
      }

      if (s == null) {
        alert(null, "Error", "failed to create circular orbit. ");
        info("Failed to create CircularOrbit when scanning " + last);
        continue;
      }

      try {
        FileOutputStream outFile = new FileOutputStream("history.properties");
        prop.setProperty("lastLoad", last);
        prop.store(outFile, "History");
        outFile.close();
      } catch (IOException e) {
        warning(e);
        continue;
      }
      //noinspection unchecked
      visualize(s);
      break;
    }
    GeneralLogger.close();
  }
}