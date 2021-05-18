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
 : atom+ ( separator+ atom+ )*
 ;

atom
 : height       // Match a height before a contraction, otherwise 5' would become a contraction
 | contraction  // Match a contraction before a quotation, otherwise 'n' would become a quotation
 | quotation
 | orphanedOpeningQuote atom
 | word
 | ~( Apostrophe | QuotationMark | BackQuote )
 ;

contraction
 : word ( apostrophe word )+        // y'all'll'nt've'd's
 | apostrophe word                  // 'll, 've
 | apostrophe Digits word?          // '04, '20s
 | word apostrophe                  // thinkin'
 | apostrophe character apostrophe  // fish 'n' chips
 ;

quotation
 : openingSQuote innerQuotation closingSQuote    // '...'
 | openingDQuote1 innerQuotation closingDQuote1  // "..."
 | openingDQuote2 innerQuotation closingDQuote2  // ''...''
 | openingDQuote3 innerQuotation closingDQuote2  // ``...''
 ;

innerQuotation
 :  restricted_atom ( atom* restricted_atom )?
 ;

restricted_atom
 : height
// | contraction  //   ,---> fails: "'I'm trouble.'"
// | quotation    //  /
 | quotation      //  \
 | contraction    //   '---> fails: "'Twas, t'wasn't thy name, 'twas it?" said Jim "the Barber" Brown.
 | word
 | ~( Space | NewLine | Apostrophe | QuotationMark | BackQuote | Backslash )
 ;

height
 : number sPrime ( number dPrime )?
 | number dPrime
 ;

orphanedOpeningQuote
 : openingDQuote1
 | openingDQuote2
 | openingDQuote3
 ;

apostrophe
 : Backslash? Apostrophe
 ;

openingSQuote
 : Backslash? Apostrophe
 ;

closingSQuote
 : Backslash? Apostrophe
 ;

openingDQuote1
 : Backslash? QuotationMark
 ;

closingDQuote1
 : Backslash? QuotationMark
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

