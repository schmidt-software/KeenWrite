/* Extended Backus-Naur form definition for Canadian English quotes */
grammar English;

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
  : '\u0009'            // Tab
  | '\u0020'            // Space
  | '\u00A0'            // No-break
  | '\u2000'..'\u200B'  // Various
  | '\u202F'            // Narrow no-break
  | '\u205F'            // Medium mathematical
  | '\u3000'            // Ideographic
  | '\uFEFF'            // Zero width no-break
  ;

CarriageReturn
  : '\r'
  ;
LineFeed
  : '\n'
  ;
Apostrophe
  : '\''
  ;
QuotationMark
  : '"'  // 22
  ;
OpeningParen
  : '('  // 28
  ;
ClosingParen
  : ')'  // 29
  ;
OpeningBrace
  : '{'  // 7B
  ;
ClosingBrace
  : '}'  // 7D
  ;
OpeningBrack
  : '['  // 5B
  ;
ClosingBrack
  : ']'  // 5D
  ;
Terminator
  : '!'  // 21
  | '.'  // 2E
  | '?'  // 3F
  ;
Joiner
  : ','  // 2C
  | ':'  // 3A
  | ';'  // 3B
  ;

/* As yet undefined characters for words, no control sequences (0x00 - 0x1F) */
Character
  : '\u0023'..'\u0026'
  | '\u002A'..'\u002B'
  | '\u002D'
  | '\u002F'..'\u0039'
  | '\u003C'..'\u003E'
  | '\u0040'..'\u005A'
  | '\u005C'
  | '\u005E'..'\u007A'
  | '\u007C'
  | '\u007E'..'\u009F'
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

apostrophe
  : Apostrophe
  ;
openingSQuote
  : Apostrophe
  ;
closingSQuote
  : Apostrophe
  ;
openingDQuote
  : QuotationMark
  ;
closingDQuote
  : QuotationMark
  ;
openingParen
  : '('
  ;
closingParen
  : ')'
  ;
openingBrack
  : '['
  ;
closingBrack
  : ']'
  ;
openingBrace
  : '{'
  ;
closingBrace
  : '}'
  ;
newLine
  : CarriageReturn
  | CarriageReturn? LineFeed
  ;
spaces
  : Space+
  ;

letters
  : Character+
  ;
contraction
  : letters (apostrophe letters)+    // y'all'll'nt've'd's
  | letters apostrophe               // thinkin'
  | apostrophe letters               // 'bout
  | apostrophe Character apostrophe  // fish 'n' chips
  ;
airQuote
  : openingDQuote words closingDQuote
  | openingSQuote words closingSQuote
  ;
parenthetical
  : openingParen words closingParen
  | openingBrack words closingBrack
  | openingBrace words closingBrace
  ;
word
  : letters
  | contraction
  | parenthetical
  | airQuote
  ;
words
  : word (spaces word)*
  ;

sentence
  : (words Joiner spaces)* (words Terminator)
  | openingDQuote sentence closingDQuote?
  | openingSQuote sentence closingSQuote?
  | openingParen sentence closingParen
  | openingBrack sentence closingBrack
  | openingBrace sentence closingBrace
  ;
sentences
  : sentence (spaces sentence)*
  | openingDQuote sentences closingDQuote?
  | openingSQuote sentences closingSQuote?
  | openingParen sentences closingParen
  | openingBrack sentences closingBrack
  | openingBrace sentences closingBrace
  ;

paragraph
  : sentences newLine newLine+
  ;

document
  : newLine* paragraph+ newLine* EOF?
  ;
