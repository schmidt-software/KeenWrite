package com.scrivenvar.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class TestProperties {

  public static void main( final String args[] ) throws ConfigurationException, IOException {
    final String p = ""
      + "file.ext.definition.yaml=*.yml,*.yaml\n"
      + "filter.file.ext.definition=${file.ext.definition.yaml}\n";

    try( final StringReader r = new StringReader( p ) ) {

      PropertiesConfiguration config = new PropertiesConfiguration();
      config.read( r );

      System.out.println( config.getList( "filter.file.ext.definition" ) );
      System.out.println( config.getString( "filter.file.ext.definition" ) );
      System.out.println( Arrays.toString( config.getStringArray( "filter.file.ext.definition" ) ) );
    }
  }
}
