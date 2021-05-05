grammar English;

document
 : newLine* paragraphs? newLine* EOF
 ;

paragraphs
 : paragraph ( newLine newLine+ paragraph )*
 ;

paragraph
 : sentence ( terminator+ Space* sentence )* terminator* Space*
 ;

sentence
 : atom ( separator+ atom )*
 ;

atom
 : word
 | height       // Match a height before a contraction, otherwise 5' would become a contraction
 | contraction  // Match a contraction before a quotation, otherwise 'n' would become a quotation
 | quotation
 | orphanedOpeningDQuote atom
 | ~( Apostrophe | QuotationMark | BackQuote )
 ;

contraction
 : word ( apostrophe word )+        // y'all'll'nt've'd's
 | apostrophe word                  // 'll, 've
 | apostrophe Digits word?          // '04, '20s
 | word apostrophe                  // thinkin'
 | apostrophe character apostrophe  // fish 'n' chips
 ;

// FIXME: can this only occur with back-quotes? If any double quote is supposed to be supported, simple use `openingDQuote`
orphanedOpeningDQuote
 : BackQuote BackQuote
 ;

quotation
 : openingSQuote innerQuotation closingSQuote    // '...'
 | openingDQuote1 innerQuotation closingDQuote1  // "..."
 | openingDQuote2 innerQuotation closingDQuote2  // ''...''
 | openingDQuote3 innerQuotation closingDQuote2  // ``...''
 ;

// Important that there is both a non-space to the right of the start-quote and one to the left end-quote
innerQuotation
 : non_space ( atom* non_space )?
 ;

non_space
 : height
 | contraction
 | quotation
 | ~( Space | NewLine )
 ;

height
 : number sPrime ( number dPrime )?
 | number dPrime
 ;

apostrophe
 : Apostrophe
 ;

openingSQuote
 : Apostrophe
 ;

closingSQuote
 : Apostrophe
 ;

openingDQuote1
 : QuotationMark
 ;

closingDQuote1
 : QuotationMark
 ;

openingDQuote2
 : Apostrophe Apostrophe
 ;

closingDQuote2
 : Apostrophe Apostrophe
 ;

openingDQuote3
 : BackQuote BackQuote
 ;

sPrime
 : Backslash? Apostrophe
 ;

dPrime
 : Backslash? QuotationMark
 | Apostrophe Apostrophe
 ;

terminator
 : ExclamationMark
 | FullStop
 | QuestionMark
 ;

character
 : Character
 | X
 ;

word
 : Word
 | Character
 | Digits
 | X
 ;

number
 : Decimal
 | Digits
 ;

separator
 : Space+
 | Comma
 | X
 | Other
 ;

newLine
 : Space* NewLine Space*
 ;

DEBUG
 : '//' ~[\r\n]* -> skip
 ;

Apostrophe      : '\'';
QuotationMark   : '"';
ExclamationMark : '!';
FullStop        : '.';
QuestionMark    : '?';
Comma           : ',';
Backslash       : '\\';
BackQuote       : '`';

Digits
 : [0-9]+
 ;

// The '.' needs to be part of the token, otherwise it conflicts with the full stop that eends a sentence
Decimal
 : Digits '.' Digits
 ;

X
 : [xX]
 ;

Character
 : [\p{Alpha}\p{General_Category=Other_Letter}]
 ;

Word
 : Character+
 ;

NewLine
 : '\r'? '\n'
 | '\r'
 ;

Space
 : [ \t]
 ;

Other
 : .
 ;

