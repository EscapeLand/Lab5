package exceptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class GeneralLogger {

  private static final Logger warning;
  private static final Logger info;

  static {
    warning = Logger.getLogger("CircularOrbit.GeneralExceptionLogger");
    info = Logger.getLogger("CircularOrbit.GeneralInfoLogger");
    info.setLevel(Level.OFF);
    info.getParent().getHandlers()[0].setLevel(Level.SEVERE);
    try {
      File lp = new File("log/");
      if (!lp.exists() && !lp.mkdir()) {
        throw new IOException("cannot mkdir: log/");
      }
      FileHandler fhI = new FileHandler("log/info.log", true);
      FileHandler fhW = new FileHandler("log/warning.log", true);

      Formatter fm = new java.util.logging.Formatter() {
        @Override
        public String format(LogRecord record) {
          return record.getInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
              + "\t" + record.getLevel() + "\n" + record.getMessage() + "\n";
        }
      };

      fhI.setFormatter(fm);
      fhW.setFormatter(fm);
      info.addHandler(fhI);
      warning.addHandler(fhW);

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * log a exception as warning.
   *
   * @param e the exception to log.
   */
  public static void warning(Exception e) {
    Consumer<Exception> warn = ex -> warning.warning(
        ex.getClass().getSimpleName() + ": " + ex.getStackTrace()[0] + ", " + ex.getMessage());
    if (e instanceof ExceptionGroup) {
      ((ExceptionGroup) e).forEach(warn);
    } else {
      warn.accept(e);
    }
  }

  /**
   * log an operation and its args.
   *
   * @param args operation args.
   */
  public static void info(Object... args) {
    if (info.getLevel() == Level.OFF) {
      return;
    }
    StringBuilder s = new StringBuilder();
    s.append(Thread.currentThread().getStackTrace()[2].getMethodName());
    for (Object arg : args) {
      s.append(' ').append(arg.toString());
    }
    info.info(s.toString());
  }

  /**
   * log a exception as severe.
   *
   * @param e the exception.
   */
  public static void severe(Exception e) {
    warning.severe(e.getClass().getSimpleName()
        + ": " + e.getStackTrace()[0] + ", " + e.getMessage());
  }

  /**
   * load info from a file.
   *
   * @param path log path of info.
   * @return parsed info list.
   * @throws IOException if error when reading file.
   */
  public static List<List> loadInfo(String path) throws IOException {
    InfoParser ifp = new InfoParser();

    File ifFile = new File(path);

    try (BufferedReader read = new BufferedReader(new FileReader(ifFile))) {
      for (String buf = read.readLine(); buf != null; buf = read.readLine()) {
        buf += "\t" + read.readLine();
        ifp.addLogs(buf.trim().split("\\t+"));
      }
    }

    return ifp.getLogs();
  }

  /**
   * load warnings from a file.
   *
   * @param path log path of exceptions.
   * @return parsed exception list.
   * @throws IOException if error when load files.
   */
  public static List<List> loadWarning(String path) throws IOException {
    WarningParser wnp = new WarningParser();

    File wnFile = new File(path);

    try (BufferedReader read = new BufferedReader(new FileReader(wnFile))) {
      for (String buf = read.readLine(); buf != null; buf = read.readLine()) {
        buf += "\t" + read.readLine();
        wnp.addLogs(buf.trim().split("\\t+"));
      }
    }

    return wnp.getLogs();
  }

  public static void close() {
    warning.getHandlers()[0].close();
    info.getHandlers()[0].close();
  }

  static class InfoParser {

    private final List<List> logs = new ArrayList<>();

    @SuppressWarnings("unchecked")
    void addLogs(String[] log) {
      List list = new ArrayList();
      list.add(LocalDateTime.parse(log[0]));
      list.add(Level.parse(log[1]));
      list.addAll(Arrays.asList(log).subList(2, log.length));
      logs.add(list);
    }

    List<List> getLogs() {
      return logs;
    }
  }

  static class WarningParser {

    private final List<List> logs = new ArrayList<>();

    @SuppressWarnings("unchecked")
    void addLogs(String[] log) {
      List list = new ArrayList();
      list.add(LocalDateTime.parse(log[0]));
      list.add(Level.parse(log[1]));
      list.add(log[2].substring(0, log[2].length() - 1));
      list.add(String.join(" ", Arrays.asList(log).subList(3, log.length)));
      logs.add(list);
    }

    List<List> getLogs() {
      return logs;
    }
  }
}

