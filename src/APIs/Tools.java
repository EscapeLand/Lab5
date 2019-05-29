package APIs;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Tools {

  private Tools() {
  }

  /**
   * see std::find_if in C++.
   *
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
   *
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
   *
   * @param src array to transform from.
   * @param func function receive O, and cast it to R.
   * @param <O> src, type
   * @param <R> return value, type.
   * @return array of R.
   * @see Tools#transform(Collection, Collection, Function)
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

  private static ExecutorService fixedPool;

  /**
   * separate a task on a large list to multi-thread task.
   *
   * @param list the list of args for multi-threading.
   * @param consumer task on each block.
   * @param threshold size on each block.
   * @param <E> class of list's elements.
   * @apiNote better use for-loop in consumer, since for-loop is better for random-access
     collections.
   */
  public static <E> void assignThread(List<E> list, Consumer<List<E>> consumer, int threshold)
      throws InterruptedException {
    if (fixedPool == null) {
      fixedPool = Executors.newFixedThreadPool(160);
      System.out.println("thread pool initialed: 160");
    }
    final int amount = list.size();
    if (amount < threshold) {
      consumer.accept(list);
    } else {
      int threadNum = (int) Math.floor(amount / threshold);
      int pos = 0;
      CountDownLatch latch = new CountDownLatch(threadNum);
      for (int i = 0; i < threadNum - 1; i++) {
        var sub = list.subList(pos, pos + threshold);
        fixedPool.execute(() -> {
          consumer.accept(sub);
          latch.countDown();
        });
        pos += threshold;
      }
      var rest = list.subList(pos, amount);
      fixedPool.execute(() -> {
        consumer.accept(rest);
        latch.countDown();
      });
      latch.await();
    }
  }
}
