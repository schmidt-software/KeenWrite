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
package com.scrivenvar.service;

import java.util.Iterator;
import java.util.List;

/**
 * Defines how settings and options can be retrieved.
 *
 * @author White Magic Software, Ltd.
 */
public interface Settings extends Service {

  /**
   * Returns a setting property or its default value.
   *
   * @param property The property key name to obtain its value.
   * @param defaultValue The default value to return iff the property cannot be
   * found.
   *
   * @return The property value for the given property key.
   */
  public String getSetting( String property, String defaultValue );

  /**
   * Returns a setting property or its default value.
   *
   * @param property The property key name to obtain its value.
   * @param defaultValue The default value to return iff the property cannot be
   * found.
   *
   * @return The property value for the given property key.
   */
  public int getSetting( String property, int defaultValue );

  /**
   * Returns a list of property names that begin with the given prefix. The
   * prefix is included in any matching results. This will return keys that
   * either match the prefix or start with the prefix followed by a dot ('.').
   * For example, a prefix value of <code>the.property.name</code> will likely
   * return the expected results, but <code>the.property.name.</code> (note the
   * extraneous period) will probably not.
   *
   * @param prefix The prefix to compare against each property name.
   *
   * @return The list of property names that have the given prefix.
   */
  public Iterator<String> getKeys( final String prefix );

  /**
   * Convert the generic list of property objects into strings.
   *
   * @param property The property value to coerce.
   * @param defaults The defaults values to use should the property be unset.
   *
   * @return The list of properties coerced from objects to strings.
   */
  public List<String> getStringSettingList( String property, List<String> defaults );

  /**
   * Converts the generic list of property objects into strings.
   *
   * @param property The property value to coerce.
   *
   * @return The list of properties coerced from objects to strings.
   */
  public List<String> getStringSettingList( String property );

  /**
   * Changes key's value. This will clear the old value before setting the
   * new value so that the old value is erased, not changed into a list.
   *
   * @param key The property key name to obtain its value.
   * @param value The new value to set.
   */
  public void putSetting( String key, String value );
}
