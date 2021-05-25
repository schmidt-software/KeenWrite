package com.keenwrite.quotes;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class EnglishQuotesListenerTest {

  @Test
  public void test() {
    // "'I'm trouble.'"
    // &ldquo;&lsquo;I&apos;m trouble.&rsquo;&rdquo;
    // &ldquo;&apos;I&lsquo;m trouble.&rsquo;&rdquo;

    EnglishParser parser = EnglishParserTest.parser("\"'I\\'m trouble.'\"");
    EnglishQuotesListener listener = new EnglishQuotesListener();
    ParseTreeWalker.DEFAULT.walk(listener, parser.document());

    System.out.println(listener.rewrittenText());
  }

  @Test
  public void testRewrittenText() throws IOException {
    try( final var reader = EnglishParserTest.openResource( "smartypants.txt" ) ) {
      String line;

      while( ((line = reader.readLine()) != null) ) {
        if (line.trim().isBlank() || line.trim().startsWith("#")) {
          continue;
        }

        String expected = reader.readLine();

        try {
          EnglishQuotesListener listener = new EnglishQuotesListener();
          ParseTreeWalker.DEFAULT.walk(listener, EnglishParserTest.parser(line).document());

          assertEquals(expected, listener.rewrittenText(), "Failed: " + line);
        }
        catch (Exception e) {
          fail("Could not parse: " + line);
        }
      }
    }
  }
}
