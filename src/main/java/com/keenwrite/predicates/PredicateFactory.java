/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.predicates;

import java.io.File;
import java.util.Collection;
import java.util.function.Predicate;

import static java.lang.String.join;
import static java.nio.file.FileSystems.getDefault;

/**
 * Provides a number of simple {@link Predicate} instances for various types
 * of string comparisons, including basic strings and file name strings.
 */
public class PredicateFactory {
  /**
   * Creates an instance of {@link Predicate} that matches a globbed file
   * name pattern.
   *
   * @param pattern The file name pattern to match.
   * @return A {@link Predicate} that can answer whether a given file name
   * matches the given glob pattern.
   */
  public static Predicate<File> createFileTypePredicate(
      final String pattern ) {
    final var matcher = getDefault().getPathMatcher(
        "glob:**{" + pattern + "}"
    );

    return file -> matcher.matches( file.toPath() );
  }

  /**
   * Creates an instance of {@link Predicate} that matches any file name from
   * a {@link Collection} of file name patterns. The given patterns are joined
   * with commas into a single comma-separated list.
   *
   * @param patterns The file name patterns to be matched.
   * @return A {@link Predicate} that can answer whether a given file name
   * matches the given glob patterns.
   */
  public static Predicate<File> createFileTypePredicate(
      final Collection<String> patterns ) {
    return createFileTypePredicate( join( ",", patterns ) );
  }

  /**
   * Creates an instance of {@link Predicate} that compares whether the given
   * {@code reference} string is contained by the comparator. Comparison is
   * case-insensitive. The test will also pass if the comparate is empty.
   *
   * @param comparator The string to check as being contained.
   * @return A {@link Predicate} that can answer whether the given string
   * is contained within the comparator, or the comparate is empty.
   */
  public static Predicate<String> createStringContainsPredicate(
      final String comparator ) {
    return comparate -> comparate.isEmpty() ||
        comparate.toLowerCase().contains( comparator.toLowerCase() );
  }

  /**
   * Creates an instance of {@link Predicate} that compares whether the given
   * {@code reference} string is starts with the comparator. Comparison is
   * case-insensitive.
   *
   * @param comparator The string to check as being contained.
   * @return A {@link Predicate} that can answer whether the given string
   * is contained within the comparator.
   */
  public static Predicate<String> createStringStartsPredicate(
      final String comparator ) {
    return comparate ->
        comparate.toLowerCase().startsWith( comparator.toLowerCase() );
  }
}