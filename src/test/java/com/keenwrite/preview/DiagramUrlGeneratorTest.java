package com.keenwrite.preview;

import org.junit.jupiter.api.Test;

import static com.keenwrite.preview.DiagramUrlGenerator.toUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Responsible for testing that images sent to the diagram server will render.
 */
class DiagramUrlGeneratorTest {
  private final static String SERVER_NAME = "kroki.io";

  // @formatter:off
  private final static String[] DIAGRAMS = new String[]{
    "graphviz",
    "digraph G {Hello->World; World->Hello;}",
    "https://kroki.io/graphviz/svg/eJxLyUwvSizIUHBXqPZIzcnJ17ULzy_KSbFWAFO6dmBB61oAE9kNww==",

    "blockdiag",
    """
      blockdiag {
        Kroki -> generates -> "Block diagrams";
        Kroki -> is -> "very easy!";

        Kroki [color = "greenyellow"];
        "Block diagrams" [color = "pink"];
        "very easy!" [color = "orange"];
      }
      """,
    "https://kroki.io/blockdiag/svg/eJxdzDEKQjEQhOHeU4zpPYFoYesRxGJ9bwghMSsbUYJ4d10UCZbDfPynolOek0Q8FsDeNCestoisNLmy-Qg7R3Blcm5hPcr0ITdaB6X15fv-_YdJixo2CNHI2lmK3sPRA__RwV5SzV80ZAegJjXSyfMFptc71w=="
  };
  // @formatter:on

  /**
   * Test that URL encoding works with Kroki's server.
   */
  @Test
  public void test_Generation_TextDiagram_UrlEncoded() {
    // Use a map of pairs if this test needs more complexity.
    for( int i = 0; i < DIAGRAMS.length / 3; i += 3 ) {
      final var name = DIAGRAMS[ i ];
      final var text = DIAGRAMS[ i + 1 ];
      final var expected = DIAGRAMS[ i + 2 ];
      final var actual = toUrl( SERVER_NAME, name, text );

      assertEquals( expected, actual );
    }
  }
}
