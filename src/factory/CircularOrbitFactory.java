package factory;

import circularOrbit.CircularOrbit;
import exceptions.ExceptionGroup;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnnecessaryInterfaceModifier")
public interface CircularOrbitFactory {

  @Nullable
  public CircularOrbit createAndLoad(String loadFrom) throws ExceptionGroup;

  @Nullable
  public CircularOrbit create(String type);

  public static CircularOrbitFactory getDefault() {
    return new DefaultCircularOrbitFactory();
  }


  /**
   * read a file, as a list of String separated by \n. no blank line is included.
   *
   * @param path file path
   * @return list of String, separated by \n.
   */
  @Nullable List<String> read(String path);

  public void setReadingMethod(@NotNull Function<String, List<String>> readingMethod);

  boolean write(String path, List<String> content);

  void setWritingMethod(BiPredicate<String, List<String>> writingMethod);

  public static final Function<String, List<String>> readerStrategy = path -> {
    File file = new File(path);
    List<String> list = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String buf;
      while ((buf = reader.readLine()) != null) {
        list.add(buf);
      }
    } catch (IOException e) {
      list = null;
    }
    return list;
  };

  public static final Function<String, List<String>> channelStrategy_read = path -> {
    final int maxLine = 4096;
    ByteBuffer buf = ByteBuffer.allocate(maxLine);
    ByteBuffer line = ByteBuffer.allocate(maxLine);
    List<String> list = new ArrayList<>();
    try (FileChannel fc = new FileInputStream(path).getChannel()) {
      while (fc.read(buf) != -1) {
        buf.flip();
        for (int i = 0; i < buf.limit(); i++) {
          byte x = buf.get();
          if (x == '\n') {
            list.add(new String(line.array(), 0, line.position()));
            line.clear();
          } else {
            line.put(x);
          }
        }
        buf.clear();
      }
      if (line.position() != 0) {
        list.add(new String(line.array(), 0, line.position()));
      }
    } catch (IOException e) {
      list = null;
    }
    return list;
  };

  public static final Function<String, List<String>> scannerStrategy = path -> {
    List<String> list;
    try (Scanner scan = new Scanner(new FileInputStream(path))) {
      list = new ArrayList<>();
      while (scan.hasNext()) {
        list.add(scan.nextLine());
      }
    } catch (IOException e) {
      list = null;
    }
    return list;
  };

  public static final BiPredicate<String, List<String>> writerStrategy = (path, list) -> {
    try (FileWriter fw = new FileWriter(path)) {
      for (String s : list) {
        fw.write(s);
        fw.write('\n');
      }
    } catch (IOException e) {
      return false;
    }
    return true;
  };

  public static final BiPredicate<String, List<String>> channelStrategy_write = (path, list) -> {
    var crlf = ByteBuffer.wrap("\n".getBytes());
    crlf.put("\n".getBytes());
    try (FileChannel fc = new FileOutputStream(path).getChannel()) {
      for (String s : list) {
        var by = s.getBytes();
        ByteBuffer buf = ByteBuffer.wrap(by);
        buf.put(by).flip();
        fc.write(buf);
        fc.write(crlf.flip());
      }
    } catch (IOException e) {
      return false;
    }
    return true;
  };

  public static final BiPredicate<String, List<String>> streamStrategy = (path, list) -> {
    try (FileOutputStream fso = new FileOutputStream(path)) {
      for (String s : list) {
        fso.write(s.getBytes());
        fso.write("\n".getBytes());
      }
    } catch (IOException e) {
      return false;
    }
    return true;
  };
}
