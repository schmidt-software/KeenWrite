/* Extended Backus--Naur form definition for English quotes and quotations */
grammar Simple;

/* **************************************************************************
 *
 * Lexer terminal tokens (variables must begin with an uppercase letter)
 *
 * *************************************************************************/

/* Unicode private use area (unused by grammar) */
Private
  : '\uE000'..'\uF8FF'
  ;

/* Word, sentence, and (optional) aposiopesis separators */
Space
  : '\u0009'            /* Tab */
  | '\u0020'            /* Space */
  | '\u00A0'            /* No-break */
  | '\u2000'..'\u200B'  /* Various */
  | '\u202F'            /* Narrow no-break */
  | '\u205F'            /* Medium mathematical */
  | '\u3000'            /* Ideographic */
  | '\uFEFF'            /* Zero width no-break */
  ;

CarriageReturn
  : '\r'
  ;
LineFeed
  : '\n'
  ;
QuotationMark
  : '"'
  ;
Terminator
  : '!'  // 21
  | '.'  // 2E
  | ':'  // 3A
  | ';'  // 3B
  | '?'  // 3F
  ;

/* Word characters as yet defined, no control sequences (0x00 - 0x1F) */
Character
  : '\u0023'..'\u002D'
  | '\u002F'..'\u0039'
  | '\u003C'..'\u003E'
  | '\u0040'..'\u009F'
  | '\u00A1'..'\u1FFF'
  | '\u200C'..'\u202E'
  | '\u2030'..'\u205E'
  | '\u2060'..'\u2FFF'
  | '\u3001'..'\uDFFF'
  | '\uF900'..'\uFEFF'
  ;

/* **************************************************************************
 *
 * Parser rules
 *
 * *************************************************************************/

openingQuote
  : QuotationMark
  ;
closingQuote
  : QuotationMark
  ;

word
  : Character+
  ;
words
  : word (Space+ word)*
  ;

newLine
  : CarriageReturn? LineFeed
  | CarriageReturn
  ;
endParagraph
  : newLine newLine+
  ;

airQuote
  : openingQuote words closingQuote
  ;
clauseQuote
  : openingQuote clause ',' closingQuote
  ;
sentencesQuote
  : openingQuote sentences closingQuote Space*
  ;

clause
  : words (',' | ';' | ':')?
  | airQuote
  | clauseQuote
  ;
sentence
  : (clause Space*)+ Terminator
  ;
sentences
  : (sentence Space*)+
  ;

paragraph
  : (sentences | sentencesQuote)+ endParagraph
  ;
document
  : newLine* paragraph+ newLine* EOF?
  ;

