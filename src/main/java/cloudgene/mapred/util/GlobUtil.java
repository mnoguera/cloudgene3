package cloudgene.mapred.util;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

public class GlobUtil {

    public static boolean isFileIncluded(String filename, List<String> includes, List<String> excludes) {
        // If includes is empty, consider all files as included
        boolean isIncluded = includes.isEmpty() || includes.stream().anyMatch(pattern -> matchPattern(filename, pattern));

        // If it doesn't match any include pattern, return false
        if (!isIncluded) {
            return false;
        }

        // If excludes is empty, no file is excluded
        boolean isExcluded = !excludes.isEmpty() && excludes.stream().anyMatch(pattern -> matchPattern(filename, pattern));

        // The file should be included if it is in the includes list and not in the excludes list
        return !isExcluded;
    }

    private static boolean matchPattern(String filename, String pattern) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        return matcher.matches(Path.of(filename));
    }

}
