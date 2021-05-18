package com.keenwrite.quotes;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class EnglishQuotesListener extends EnglishBaseListener {

  private final StringBuilder builder = new StringBuilder();
  private int skipNextTerminals = 0;

  public String rewrittenText() {
    return this.builder.toString();
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    if (node.getSymbol().getType() == Token.EOF) {
      return;
    }

    if (this.skipNextTerminals > 0) {
      this.skipNextTerminals--;
      return;
    }

    this.builder.append(node.getText());
  }

  @Override
  public void enterApostrophe(EnglishParser.ApostropheContext ctx) {
    this.appendText("&apos;", ctx);
  }

  @Override
  public void enterOpeningSQuote(EnglishParser.OpeningSQuoteContext ctx) {
    this.appendText("&lsquo;", ctx);
  }

  @Override
  public void enterClosingSQuote(EnglishParser.ClosingSQuoteContext ctx) {
    this.appendText("&rsquo;", ctx);
  }

  @Override
  public void enterOpeningDQuote1(EnglishParser.OpeningDQuote1Context ctx) {
    this.appendText("&ldquo;", ctx);
  }

  @Override
  public void enterClosingDQuote1(EnglishParser.ClosingDQuote1Context ctx) {
    this.appendText("&rdquo;", ctx);
  }

  @Override
  public void enterOpeningDQuote2(EnglishParser.OpeningDQuote2Context ctx) {
    this.appendText("&ldquo;", ctx);
  }

  @Override
  public void enterClosingDQuote2(EnglishParser.ClosingDQuote2Context ctx) {
    this.appendText("&rdquo;", ctx);
  }

  @Override
  public void enterOpeningDQuote3(EnglishParser.OpeningDQuote3Context ctx) {
    this.appendText("&ldquo;", ctx);
  }

  @Override
  public void enterSPrime(EnglishParser.SPrimeContext ctx) {
    this.appendText("&prime;", ctx);
  }

  @Override
  public void enterDPrime(EnglishParser.DPrimeContext ctx) {
    this.appendText("&Prime;", ctx);
  }

  private void appendText(String text, ParserRuleContext ctx) {
    this.builder.append(text);

    this.skipNextTerminals += ctx.getChildCount();
  }
}
