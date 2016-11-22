/*
 * Copyright 2016 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivenvar.definition;

import java.util.List;

/**
 * Convenience class that provides a clearer API for obtaining list elements.
 *
 * @author White Magic Software, Ltd.
 */
public final class Lists {

  private Lists() {
  }

  /**
   * Returns the first item in the given list, or null if not found.
   *
   * @param <T> The generic list type.
   * @param list The list that may have a first item.
   *
   * @return null if the list is null or there is no first item.
   */
  public static <T> T getFirst( final List<T> list ) {
    return getFirst( list, null );
  }

  /**
   * Returns the last item in the given list, or null if not found.
   *
   * @param <T> The generic list type.
   * @param list The list that may have a last item.
   *
   * @return null if the list is null or there is no last item.
   */
  public static <T> T getLast( final List<T> list ) {
    return getLast( list, null );
  }

  /**
   * Returns the first item in the given list, or t if not found.
   *
   * @param <T> The generic list type.
   * @param list The list that may have a first item.
   * @param t The default return value.
   *
   * @return null if the list is null or there is no first item.
   */
  public static <T> T getFirst( final List<T> list, final T t ) {
    return isEmpty( list ) ? t : list.get( 0 );
  }

  /**
   * Returns the last item in the given list, or t if not found.
   *
   * @param <T> The generic list type.
   * @param list The list that may have a last item.
   * @param t The default return value.
   *
   * @return null if the list is null or there is no last item.
   */
  public static <T> T getLast( final List<T> list, final T t ) {
    return isEmpty( list ) ? t : list.get( list.size() - 1 );
  }

  /**
   * Returns true if the given list is null or empty.
   *
   * @param <T> The generic list type.
   * @param list The list that has a last item.
   *
   * @return true The list is empty.
   */
  public static <T> boolean isEmpty( final List<T> list ) {
    return list == null || list.isEmpty();
  }
}
