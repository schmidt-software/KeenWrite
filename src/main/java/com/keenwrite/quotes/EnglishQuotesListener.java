package com.keenwrite.quotes;

import org.antlr.v4.runtime.tree.TerminalNode;

public class EnglishQuotesListener extends EnglishBaseListener {

  private final StringBuilder builder = new StringBuilder();
  private int skipNextTerminals = 0;

  public String rewrittenText() {
    return this.builder.toString();
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    if (this.skipNextTerminals > 0) {
      this.skipNextTerminals--;
      return;
    }

    this.builder.append(node.getText());

    System.out.println(node.getText().replace("\n", "\\n"));
  }
}
