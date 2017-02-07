package com.nuvolect.deepdive.util;

import org.apache.commons.io.filefilter.*;

import java.io.FileFilter;
import java.util.Collection;

/**
 * Created by mattkraus on 12/20/16.
 */
public class OmniFileFilter {

    /**
     * Finds files within a given directory (and optionally its subdirectories)
     * which match an array of extensions.
     *
     * @param directory  the directory to search in
     * @param extensions an array of extensions, ex. {"java","xml"}. If this
     *                   parameter is {@code null}, all files are returned.
     * @param recursive  if true all subdirectories are searched as well
     * @return an collection of java.io.File with the matching files
     */
    public static Collection<OmniFile> listFiles(
        final OmniFile directory, final String[] extensions, final boolean recursive) {

        IOFileFilter filter;

        if (extensions == null) {
            filter = TrueFileFilter.INSTANCE;
        } else {
            final String[] suffixes = toSuffixes(extensions);
            filter = new SuffixFileFilter(suffixes);
        }
        return listFiles(directory, filter,
            recursive ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * Converts an array of file extensions to suffixes for use
     * with IOFileFilters.
     *
     * @param extensions an array of extensions. Format: {"java", "xml"}
     * @return an array of suffixes. Format: {".java", ".xml"}
     */
    private static String[] toSuffixes(final String[] extensions) {
        final String[] suffixes = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            suffixes[i] = "." + extensions[i];
        }
        return suffixes;
    }

    /**
     * Finds files within a given directory (and optionally its
     * subdirectories). All files found are filtered by an IOFileFilter.
     * <p>
     * If your search should recurse into subdirectories you can pass in
     * an IOFileFilter for directories. You don't need to bind a
     * DirectoryFileFilter (via logical AND) to this filter. This method does
     * that for you.
     * <p>
     * An example: If you want to search through all directories called
     * "temp" you pass in <code>FileFilterUtils.NameFileFilter("temp")</code>
     * <p>
     * Another common usage of this method is find files in a directory
     * tree but ignoring the directories generated CVS. You can simply pass
     * in <code>FileFilterUtils.makeCVSAware(null)</code>.
     *
     * @param directory  the directory to search in
     * @param fileFilter filter to apply when finding files.
     * @param dirFilter  optional filter to apply when finding subdirectories.
     *                   If this parameter is {@code null}, subdirectories will not be included in the
     *                   search. Use TrueFileFilter.INSTANCE to match all directories.
     * @return an collection of java.io.File with the matching files
     * @see FileFilterUtils
     * @see NameFileFilter
     */
    public static Collection<OmniFile> listFiles(
        final OmniFile directory, final IOFileFilter fileFilter, final IOFileFilter dirFilter) {

        validateListFilesParameters(directory, fileFilter);

        final IOFileFilter effFileFilter = setUpEffectiveFileFilter(fileFilter);
        final IOFileFilter effDirFilter = setUpEffectiveDirFilter(dirFilter);

        //Find files
        final Collection<OmniFile> files = new java.util.LinkedList<OmniFile>();
        innerListFiles(files, directory,
            FileFilterUtils.or(effFileFilter, effDirFilter), false);
        return files;
    }

    /**
     * Validates the given arguments.
     * <ul>
     * <li>Throws {@link IllegalArgumentException} if {@code directory} is not a directory</li>
     * <li>Throws {@link NullPointerException} if {@code fileFilter} is null</li>
     * </ul>
     *
     * @param directory  The File to test
     * @param fileFilter The IOFileFilter to test
     */
    private static void validateListFilesParameters(final OmniFile directory, final IOFileFilter fileFilter) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter 'directory' is not a directory: " + directory);
        }
        if (fileFilter == null) {
            throw new NullPointerException("Parameter 'fileFilter' is null");
        }
    }

    /**
     * Returns a filter that accepts files in addition to the {@link OmniFile} objects accepted by the given filter.
     *
     * @param fileFilter a base filter to add to
     * @return a filter that accepts files
     */
    private static IOFileFilter setUpEffectiveFileFilter(final IOFileFilter fileFilter) {
        return FileFilterUtils.and(fileFilter, FileFilterUtils.notFileFilter(DirectoryFileFilter.INSTANCE));
    }

    /**
     * Returns a filter that accepts directories in addition to the {@link OmniFile} objects accepted by the given filter.
     *
     * @param dirFilter a base filter to add to
     * @return a filter that accepts directories
     */
    private static IOFileFilter setUpEffectiveDirFilter(final IOFileFilter dirFilter) {
        return dirFilter == null ? FalseFileFilter.INSTANCE : FileFilterUtils.and(dirFilter,
            DirectoryFileFilter.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * Finds files within a given directory (and optionally its
     * subdirectories). All files found are filtered by an IOFileFilter.
     *
     * @param files                 the collection of files found.
     * @param directory             the directory to search in.
     * @param filter                the filter to apply to files and directories.
     * @param includeSubDirectories indicates if will include the subdirectories themselves
     */
    private static void innerListFiles(final Collection<OmniFile> files, final OmniFile directory,
                                       final IOFileFilter filter, final boolean includeSubDirectories) {

        final OmniFile[] found = directory.listFiles((FileFilter) filter);

        if (found != null) {
            for (final OmniFile file : found) {
                if (file.isDirectory()) {
                    if (includeSubDirectories) {
                        files.add(file);
                    }
                    innerListFiles(files, file, filter, includeSubDirectories);
                } else {
                    files.add(file);
                }
            }
        }
    }
}
