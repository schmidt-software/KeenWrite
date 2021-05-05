package com.keenwrite.quotes;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class EnglishParserTest {

  String[][] tests = {
      {
          "The Roaring '20s had the best music, no?",
          "The Roaring &apos;20s had the best music, no?"
      },
      {
          "Took place in '04, yes'm!",
          "Took place in &apos;04, yes&apos;m!"
      },
      {
          "I don't like it: I love's it!",
          "I don&apos;t like it: I love&apos;s it!"
      },
      {
          "We'd've thought that pancakes'll be sweeter there.",
          "We&apos;d&apos;ve thought that pancakes&apos;ll be sweeter there."
      },
      {
          "She'd be coming o'er when the horse'd gone to pasture...",
          "She&apos;d be coming o&apos;er when the horse&apos;d gone to pasture..."
      },
      {
          "'Twas and 'tis whate'er lay 'twixt dawn and dusk 'n River Styx.",
          "&apos;Twas and &apos;tis whate&apos;er lay &apos;twixt dawn and dusk &apos;n River Styx."
      },
      {
          "Didn' get th' message.",
          "Didn&apos; get th&apos; message."
      },
      {
          "Namsayin', y'know what I'ma sayin'?",
          "Namsayin&apos;, y&apos;know what I&apos;ma sayin&apos;?"
      },
      {
          "Salt 'n' vinegar, fish-'n'-chips, sugar 'n' spice!",
          "Salt &apos;n&apos; vinegar, fish-&apos;n&apos;-chips, sugar &apos;n&apos; spice!"
      },
      {
          "She stood 5\\'7\\\".",
          "She stood 5&prime;7&Prime;."
      },
      {
          "It's 4'11\" away.",
          "It&apos;s 4&prime;11&Prime; away."
      },
      {
          "Alice's friend is 6'3\" tall.",
          "Alice&apos;s friend is 6&prime;3&Prime; tall."
      },
      {
          "Bob's table is 5'' × 4''.",
          "Bob&apos;s table is 5&Prime; × 4&Prime;."
      },
      {
          "What's this -5.5'' all about?",
          "What&apos;s this -5.5&Prime; all about?"
      },
      {
          "+7.9'' is weird.",
          "+7.9&Prime; is weird."
      },
      {
          "Foolscap? Naw, I use 11.5\"x14.25\" paper!",
          "Foolscap? Naw, I use 11.5&Prime;x14.25&Prime; paper!"
      },
      {
          "An angular measurement, 3° 5' 30\" means 3 degs, 5 arcmins, and 30 arcsecs.",
          "An angular measurement, 3° 5&prime; 30&Prime; means 3 degs, 5 arcmins, and 30 arcsecs."
      },
      {
          "``I am Sam''",
          "&ldquo;I am Sam&rdquo;"
      },
      {
          "``Sam's away today''",
          "&ldquo;Sam&apos;s away today&rdquo;"
      },
      {
          "``Sam's gone!",
          "&ldquo;Sam&apos;s gone!"
      },
      {
          "``5'10\" tall 'e was!''",
          "&ldquo;5&prime;10&Prime; tall &apos;e was!&rdquo;"
      },
      {
          "\"'I'm trouble.'\"",
          "&ldquo;&lsquo;I&apos;m trouble.&rsquo;&rdquo;"
      },
      {
          "'\"Trouble's my name.\"'",
          "&lsquo;&ldquo;Trouble&apos;s my name.&ldquo;&lsquo;"
      },
      {
          "\\\"What?\\\"",
          "&ldquo;What?&rdquo;"
      },
      {
          "\"I am Sam\"",
          "&ldquo;I am Sam&rdquo;"
      },
      {
          "\"...even better!\"",
          "&ldquo;...even better!&rdquo;"
      },
      {
          "\"It was so,\" said he.",
          "&ldquo;It was so,&rdquo; said he."
      },
      {
          "\"She said, 'Llamas'll languish, they'll--",
          "&ldquo;She said, &lsquo;Llamas&apos;ll languish, they&apos;ll--"
      },
      {
          "With \"air quotes\" in the middle.",
          "With &ldquo;air quotes&rdquo; in the middle."
      },
      {
          "With--\"air quotes\"--and dashes.",
          "With--&ldquo;air quotes&rdquo;--and dashes."
      },
      {
          "\"Not \"quite\" what you expected?\"",
          "&ldquo;Not &ldquo;quite&rdquo; what you expected?&rdquo;"
      },
      {
          "\"'Here I am,' said Sam\"",
          "&ldquo;&lsquo;Here I am,&rsquo; said Sam&rdquo;"
      },
      {
          "'\"Here I am,\" said Sam'",
          "&lsquo;&ldquo;Here I am,&rdquo;, said Sam&rsquo;"
      },
      {
          "'Hello, \"Dr. Brown,\" what's your real name?'",
          "&lsquo;Hello, &ldquo;Dr. Brown,&rdquo; what's your real name?&rsquo;"
      },
      {
          "\"'Twas, t'wasn't thy name, 'twas it?\" said Jim \"the Barber\" Brown.",
          "&ldquo;&apos;Twas, t&apos;wasn&apos;t thy name, &apos;twas it?&rdquo; said Jim &ldquo;the Barber&rdquo; Brown."
      },
      {
          "'I am Sam'",
          "&lsquo;I am Sam&rsquo;"
      },
      {
          "'It was so,' said he.",
          "&lsquo;It was so,&rsquo; said he."
      },
      {
          "'...even better!'",
          "&lsquo;...even better!&rsquo;"
      },
      {
          "With 'quotes' in the middle.",
          "With &lsquo;quotes&rsquo; in the middle."
      },
      {
          "With--'imaginary'--dashes.",
          "With--&lsquo;imaginary&rsquo;--dashes."
      },
      {
          "'Not 'quite' what you expected?'",
          "&lsquo;Not &lsquo;quite&rsquo; what you expected?&rsquo;"
      },
      {
          "''Cause I don't like it, 's why,' said Pat.",
          "&lsquo;&apos;Cause I don't like it, &apos;s why,&rsquo; said Pat."
      },
      {
          "'It's a beautiful day!'",
          "&lsquo;It&apos;s a beautiful day!&rsquo;"
      },
      {
          "'He said, 'Thinkin'.'",
          "&lsquo;He said, &lsquo;Thinkin&rsquo;.&rsquo;"
      },
      {
          "Sam's Sams' and the Ross's roses' thorns were prickly.",
          "Sam&apos;s Sams&apos; and the Ross&apos;s roses&apos; thorns were prickly."
      },
      {
          "\"I heard she said, 'That's Sam's',\" said the Sams' cat.",
          "&ldquo;I heard she said, &lsquo;That&apos;s Sam&apos;s&rsquo;,&rdquo; said the Sams&apos; cat."
      },
      {
          "\"'Janes' said, ''E'll be spooky, Sam's son with the jack-o'-lantern!'\" said the O'Mally twins'---y'know---ghosts in unison.",
          "&ldquo;&lsquo;Janes&apos; said, &lsquo;&apos;E&apos;ll be spooky, Sam&apos;s son with the jack-o&apos;-lantern!&rsquo;&rdquo; said the O&apos;Mally twins&apos;---y&apos;know---ghosts in unison."
      },
      {
          "'He's at Sams'",
          "&lsquo;He&apos; at Sams&rsquo;"
      },
      {
          "\\\"Hello!\\\"",
          "&ldquo;Hello!&rdquo;"
      },
      {
          "ma'am",
          "ma&apos;am"
      },
      {
          "'Twas midnight",
          "&apos;Twas midnight"
      },
      {
          "\\\"Hello,\\\" said the spider. \\\"'Shelob' is my name.\\\"",
          "&ldquo;Hello,&rdquo; said the spider. &ldquo;&lsquo;Shelob&rsquo; is my name.&rdquo;"
      },
      {
          "'A', 'B', and 'C' are letters.",
          "&lsquo;A&rsquo; &lsquo;B&rsquo; and &lsquo;C&rsquo; are letters."
      },
      {
          "'Oak,' 'elm,' and 'beech' are names of trees. So is 'pine.'",
          "&lsquo;Oak,&rsquo; &lsquo;elm,&rsquo; and &lsquo;beech&rsquo; are names of trees. So is &lsquo;pine.&rsquo;"
      },
      {
          "'He said, \\\"I want to go.\\\"' Were you alive in the 70's?",
          "&lsquo;He said, &ldquo;I want to go.&rdquo;&rsquo; Were you alive in the 70&apos;s?"
      },
      {
          "\\\"That's a 'magic' sock.\\\"",
          "&ldquo;That&apos;s a &lsquo;magic&rsquo; sock.&rdquo;"
      },
      {
          "Website! Company Name, Inc. (\\\"Company Name\\\" or \\\"Company\\\") recommends reading the following terms and conditions, carefully:",
          "Website! Company Name, Inc. (&ldquo;Company Name&rdquo; or &ldquo;Company&rdquo;) recommends reading the following terms and conditions, carefully:"
      },
      {
          "Website! Company Name, Inc. ('Company Name' or 'Company') recommends reading the following terms and conditions, carefully:",
          "Website! Company Name, Inc. (&lsquo;Company Name&rsquo; or &lsquo;Company&rsquo;) recommends reading the following terms and conditions, carefully:"
      },
      {
          "Workin' hard",
          "Workin&apos; hard"
      },
      {
          "'70s are my favorite numbers,' she said.",
          "&lsquo;70s are my favorite numbers,&rsquo; she said."
      },
      {
          "'70s fashion was weird.",
          "&apos;70s fashion was weird."
      },
      {
          "12\\\" record, 5'10\\\" height",
          "12&Prime; record, 5&prime;10&Prime; height"
      },
      {
          "Model \\\"T2000\\\"",
          "Model &ldquo;T2000&rdquo;"
      },
      {
          "iPad 3's battery life is not great.",
          "iPad 3&apos;s battery life is not great."
      },
      {
          "Book 'em, Danno. Rock 'n' roll. 'Cause 'twas the season.",
          "Book &apos;em, Danno. Rock &apos;n&apos; roll. &apos;Cause &apos;twas the season."
      },
      {
          "'85 was a good year. (The entire '80s were.)",
          "&apos;85 was a good year. (The entire &apos;80s were.)"
      }
  };

  @Test
  public void testDumpTokens() {
    String text = "music,";
    List<Token> tokens = tokens(text);

    for (Token t : tokens) {
      System.out.printf("%-15s '%s'\n", EnglishLexer.VOCABULARY.getSymbolicName(t.getType()), t.getText());
    }
  }

  @Test
  public void tokenizeSmartypantsExamplesSuccessfully() {
    for (String[] test : tests) {
      List<Token> tokens = tokens(test[0]);

      for (Token t : tokens) {
        if (t.getType() == EnglishLexer.Other) {
          fail("Stumbled upon an unknown char in the lexer: " + t.getText());
        }
      }
    }
  }

  @Test
  public void parseSmartypantsExamplesSuccessfully() {
    for (String[] test : tests) {
      try {
        parser(test[0]).document();
      }
      catch (Exception e) {
        fail("Could not parse: " + test[0]);
      }
    }
  }

  private static List<Token> tokens(String text) {
    EnglishLexer lexer = new EnglishLexer(CharStreams.fromString(text));

    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    tokenStream.fill();

    return tokenStream.getTokens();
  }

  private static EnglishParser parser(String text) {
    EnglishLexer lexer = new EnglishLexer(CharStreams.fromString(text));
    EnglishParser parser = new EnglishParser(new CommonTokenStream(lexer));

    // Remove error listeners that possibly try to recover from syntax errors
    parser.removeErrorListeners();

    // On any syntax error, we'll let an exception be thrown by using BailErrorStrategy
    parser.setErrorHandler(new BailErrorStrategy());

    return parser;
  }
}
