package org.cedar.onestop.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

  public static <T> List<T> addOrInit(List<T> list, T item) {
    var result = new ArrayList<T>();
    if (list != null && !list.isEmpty()) {
      result.addAll(list);
    }
    if (item != null) {
      result.add(item);
    }
    return result;
  }

  /**
   *
   * @param list list to truncate
   * @param maxListSize list size limit
   * @param mostRecentAdditions if true, returned list reflects end of original list as opposed to start
   * @param <T> list object type
   * @return truncated list of T objects
   * @throws IllegalArgumentException if maxListSize is less than or equal to 0
   */
  public static <T> List<T> truncateList(List<T> list, int maxListSize, boolean mostRecentAdditions) {
    if (maxListSize <= 0) {
      throw new IllegalArgumentException("Attempted to make a list of size [ " + maxListSize + " ]. " +
          "Expected a size limit greater than 0.");
    }

    var result = new ArrayList<T>();
    if (list != null && !list.isEmpty()) {
      var size = list.size();
      if(size <= maxListSize) {
        result.addAll(list);
      }
      else {
        var fromIndex = mostRecentAdditions ? size - maxListSize : 0;
        var toIndex = mostRecentAdditions ? size : maxListSize;
        result.addAll(list.subList(fromIndex, toIndex));
      }
    }
    return result;
  }
}
