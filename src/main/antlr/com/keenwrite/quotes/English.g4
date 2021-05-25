grammar English;

document
 : nl* paragraphs? nl* EOF
 ;

paragraphs
 : paragraph ( nl nl+ paragraph )*
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
 | orphanedOpeningQuote ~( Space | NewLine )
 | word
 | ~( Apostrophe | QuotationMark | BackQuote )
 ;

contraction
 : verbContraction
 | outerContraction
 | endedContraction
 | beganContraction
 | innerContraction
 | apostrophe Digits word? // '04, '20s
 ;

// ''Cause I don't like it, 's why,' said Pat.
quotation
 : openingSQuote innerQuotation closingSQuote    // '...'
 | openingDQuote1 innerQuotation closingDQuote1  // "..."
 | openingDQuote2 innerQuotation closingDQuote2  // ''...''
 | openingDQuote3 innerQuotation closingDQuote2  // ``...''
 ;

innerQuotation
 :  restrictedAtom ( atom* restrictedAtom )?
 ;

restrictedAtom
 : height
 | contraction
 | quotation
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

word
 : letter+
 | number
 ;

letter
 : A | B | C | D | E | F | G | H | I | J | J | K | L | M | N | O | P | Q | R | S | T | U | V | W | X | Y | Z
 | OtherLetter
 ;

nl
 : Space* NewLine Space*
 ;

verbContraction
 : apostrophe ( D | L L | M | R E | S | V E )
 ;

outerContraction
 : apostrophe N apostrophe
 ;

endedContraction
 : A I N apostrophe
 | A N apostrophe
 | B U R L I N apostrophe
 | C A S apostrophe
 | D I D N apostrophe
 | D O A N apostrophe
 | D O I N apostrophe
 | F O apostrophe
 | G E R R I N apostrophe
 | G O N apostrophe
 | I apostrophe
 | I M A apostrophe
 | M O apostrophe
 | N A M S A Y I N apostrophe
 | S A Y I N apostrophe
 | O apostrophe
 | O L apostrophe
 | O apostrophe T H apostrophe
 | P O apostrophe
 | T apostrophe
 | T H apostrophe
 ;

beganContraction
 : apostrophe C A U S E
 | apostrophe A P O R T H
 | apostrophe B O U T
 | apostrophe B O U T C H A
 | apostrophe B O U T C H U
 | apostrophe C H O O
 | apostrophe D I L L O
 | apostrophe E apostrophe L L
 | apostrophe E R E
 | apostrophe E
 | apostrophe E apostrophe S
 | apostrophe F R A I D
 | apostrophe F R O
 | apostrophe H O
 | apostrophe K A Y
 | apostrophe L O
 | apostrophe N
 | apostrophe N E A T H
 | apostrophe N O T H E R
 | apostrophe O N N A
 | apostrophe P O N
 | apostrophe S B L O O D
 | apostrophe S C U S E
 | apostrophe S F A R
 | apostrophe S F O O T
 | apostrophe S U P
 | apostrophe T
 | apostrophe T A I N T
 | apostrophe T A I N apostrophe T
 | apostrophe T I S
 | apostrophe T I S N apostrophe T
 | apostrophe T S H A L L
 | apostrophe T W A S
 | apostrophe T W A S N apostrophe T
 | apostrophe T W E E N
 | apostrophe T W E R E
 | apostrophe T W E R E N apostrophe T
 | apostrophe T W I L L
 | apostrophe T W I X T
 | apostrophe T W O N apostrophe T
 | apostrophe T W O U apostrophe D
 | apostrophe T W O U apostrophe D N apostrophe T
 | apostrophe T W O U L D
 | apostrophe T W O U L D N apostrophe T
 | apostrophe U M
 | apostrophe V E
 | apostrophe Z A T
 ;

innerContraction
 : A B O V E S apostrophe D
 | A F T E R apostrophe T
 | A apostrophe I G H T
 | A I N apostrophe T
 | A I N apostrophe T C H A
 | A L L apostrophe S
 | A N D apostrophe S
 | A apostrophe N apostrophe T
 | A N apostrophe T
 | A N Y B O D Y apostrophe L L
 | A N Y B O D Y apostrophe S
 | A R E N apostrophe C H U
 | A R E N apostrophe T
 | A apostrophe R I G H T
 | A S apostrophe T
 | A T apostrophe S
 | B A I N apostrophe T
 | B E A N apostrophe T
 | B E F O R E apostrophe T
 | B E N apostrophe T
 | B E T T E R apostrophe N
 | B E T T E R N apostrophe T
 | B I S N apostrophe T
 | B apostrophe L O N G
 | B O apostrophe S apostrophe N
 | B R apostrophe E R
 | B U T apostrophe S
 | B Y apostrophe R
 | B Y apostrophe T
 | C A I N apostrophe T
 | C A L L apostrophe T
 | C A M apostrophe S T
 | C A N N apostrophe T
 | C A apostrophe N apostrophe T
 | C A N apostrophe T
 | C A N apostrophe T C H A
 | C A N apostrophe T apostrophe V E
 | C A N apostrophe V E
 | C A P apostrophe N
 | C A S N apostrophe T
 | C H apostrophe I L L
 | C apostrophe M E R E
 | C apostrophe M I N
 | C apostrophe M O N
 | C O L apostrophe S
 | C O U L D N apostrophe T
 | C O U L D N apostrophe T apostrophe V E
 | C O U L D N apostrophe V E
 | C O U L D apostrophe V E
 | C U D N apostrophe T
 | D A M F I D O N apostrophe T
 | D A M N F I D O N apostrophe T
 | D A R E D N apostrophe T
 | D A R E N apostrophe T
 | D A S N apostrophe T
 | D A S S N apostrophe T
 | D A T apostrophe S
 | D E R E apostrophe S
 | D E R apostrophe S
 | D I D N apostrophe T
 | D I D N apostrophe T C H A
 | D I D N apostrophe T C H Y A
 | D I apostrophe N apostrophe T
 | D I N apostrophe T
 | D O E S N apostrophe T
 | D O E S apostrophe T
 | D O N apostrophe T
 | D O N apostrophe T C H A
 | D O apostrophe T
 | D O T H N apostrophe T
 | D U D N apostrophe T
 | D U N apostrophe T
 | D U R S E N apostrophe T
 | D U R S N apostrophe T
 | D U R S T N apostrophe T
 | D apostrophe Y A
 | D apostrophe Y apostrophe A L L
 | D apostrophe Y E
 | D apostrophe Y E R
 | D apostrophe Y O U
 | E apostrophe E N
 | E apostrophe E R
 | E V E R Y B O D Y apostrophe S
 | E V E R Y O N E apostrophe S
 | E V apostrophe R Y
 | F A R apostrophe S
 | F O apostrophe C apostrophe S apostrophe L E
 | F O apostrophe C apostrophe S L E
 | F O apostrophe C apostrophe S T L E
 | F O R apostrophe T
 | F apostrophe R E V E R
 | F apostrophe R E X A M P L E
 | G apostrophe B Y E
 | G apostrophe D A Y
 | G apostrophe H E A D
 | G I apostrophe S
 | G I V apostrophe N
 | G apostrophe N I G H T
 | G apostrophe W A N
 | H A D N apostrophe T
 | H A D N apostrophe T apostrophe V E
 | H A D apostrophe V E
 | H A I N apostrophe T
 | H A apostrophe N apostrophe T
 | H A N apostrophe T
 | H A apostrophe P E N C E
 | H A apostrophe P E N N I E S
 | H A apostrophe P E N N Y
 | H A apostrophe P apostrophe O R T H
 | H A apostrophe P O R T H
 | H A apostrophe P apostrophe O R T H S
 | H A S N apostrophe T
 | H A S apostrophe T
 | H A V E N apostrophe T
 | H A V E apostrophe T
 | H A V N apostrophe T
 | H E A V apostrophe N
 | H E apostrophe D
 | H E apostrophe D apostrophe V E
 | H E apostrophe L
 | H E apostrophe L L
 | H E apostrophe L L apostrophe V E
 | H E R E apostrophe L L
 | H E R E apostrophe R E
 | H E R E apostrophe S
 | H E R apostrophe S
 | H E apostrophe S
 | H E apostrophe S N apostrophe T
 | H E apostrophe V E
 | H O W apostrophe D
 | H O W apostrophe L L
 | H O W apostrophe M
 | H O W apostrophe R E
 | H O W apostrophe S
 | H O W apostrophe T
 | H O W apostrophe V E
 | I apostrophe D
 | I apostrophe D HYPHEN A
 | I apostrophe D A
 | I D N apostrophe T
 | I apostrophe D N apostrophe T apostrophe V E
 | I apostrophe D apostrophe V E
 | I apostrophe F A I T H
 | I F apostrophe N
 | I F apostrophe T
 | I apostrophe L
 | I apostrophe L L
 | I apostrophe L L apostrophe V E
 | I apostrophe M
 | I apostrophe M apostrophe A
 | I apostrophe M HYPHEN A
 | I apostrophe M A
 | I apostrophe M apostrophe A
 | I apostrophe M A
 | I apostrophe M M A
 | I apostrophe N
 | I N apostrophe S
 | I apostrophe N apostrophe T
 | I N apostrophe T
 | I N T O apostrophe T
 | I apostrophe S
 | I apostrophe S
 | I apostrophe S E
 | I S N apostrophe T
 | I S apostrophe T
 | I T apostrophe D
 | I T apostrophe D apostrophe V E
 | I T apostrophe L L
 | I T apostrophe S
 | I T apostrophe S N apostrophe T
 | I apostrophe V E
 | I apostrophe V E N apostrophe T
 | L E T apostrophe S
 | L I apostrophe L
 | L I T T L apostrophe U N
 | M A apostrophe A M
 | M A Y N apostrophe T
 | M A Y apostrophe T
 | M A Y apostrophe V E
 | M apostrophe D E A R
 | M I G H T N apostrophe T
 | M I G H T N apostrophe T apostrophe V E
 | M I G H T apostrophe V E
 | M apostrophe L A D
 | M apostrophe L A D I E S
 | M apostrophe L A D Y
 | M apostrophe L O R D
 | M apostrophe L O R D S
 | M N G apostrophe T
 | M O R E apostrophe N
 | M U S apostrophe N apostrophe T
 | M U S N apostrophe T
 | M U S T N apostrophe T
 | M U S T N apostrophe T apostrophe V E
 | M U S T apostrophe V E
 | N E E D N apostrophe T
 | N E E apostrophe N apostrophe T
 | N E apostrophe E R
 | N E apostrophe E R HYPHEN D O HYPHEN W E L L
 | N E V E R apostrophe V E
 | N O B O D Y apostrophe D
 | N O B O D Y apostrophe S
 | N O B O D Y apostrophe V E
 | N O R apostrophe E A S T E R
 | N O T apostrophe V E
 | N apostrophe T
 | O apostrophe C L O C K
 | O apostrophe E R
 | O apostrophe E R H E A D
 | O apostrophe E R L O A D
 | O apostrophe E R L O A D S
 | O apostrophe E R L O O K
 | O apostrophe E R L O O K S
 | O I apostrophe L L
 | O I apostrophe V E
 | O apostrophe L A N T E R N
 | O apostrophe L A N T E R N S
 | O N E apostrophe S
 | O N apostrophe T
 | O T H E R apostrophe N
 | O U G H T N apostrophe T
 | O U G H T N apostrophe T apostrophe V E
 | P apostrophe A P S
 | P E N N apostrophe O R T H
 | P E N apostrophe O R T H
 | P E O P L E apostrophe D
 | P O apostrophe B O Y
 | P O W apostrophe R
 | P apostrophe R apostrophe A P S
 | P apostrophe R A P S
 | P R A Y apostrophe R
 | P apostrophe R H A P S
 | P U D D apostrophe N apostrophe H E A D
 | R apostrophe C O O N
 | R U N HYPHEN O apostrophe HYPHEN T H E HYPHEN M I L L
 | S A M E apostrophe S
 | S E E apostrophe T
 | S E apostrophe N N I G H T
 | S E V apostrophe N
 | S H A L L N apostrophe T
 | S H A L L apostrophe S
 | S H A L L apostrophe V E
 | S H A apostrophe N apostrophe T
 | S H A N apostrophe T
 | S H apostrophe D
 | S H E apostrophe D
 | S H E apostrophe D apostrophe V E
 | S H E apostrophe L
 | S H E apostrophe L L
 | S H E apostrophe L L apostrophe V E
 | S H E apostrophe S
 | S H E apostrophe V E
 | S H O U L D N apostrophe T
 | S H O U L D N apostrophe T apostrophe V E
 | S H O U L D apostrophe V E
 | S apostrophe L O N G
 | S apostrophe M A T T E R
 | S apostrophe M O R E
 | S apostrophe M O R E S
 | S O M E B O D Y apostrophe D
 | S O M E B O D Y apostrophe S
 | S O M E O N E apostrophe S
 | S O M E T H I N G apostrophe S
 | S O R T apostrophe V E
 | S O apostrophe S
 | T H apostrophe A R E
 | T H apostrophe A R T
 | T H A T apostrophe D
 | T H A T apostrophe D apostrophe V E
 | T H A T apostrophe L L
 | T H A T apostrophe L L apostrophe V E
 | T H A T apostrophe R E
 | T H A T apostrophe S
 | T H A T apostrophe V E
 | T H E M apostrophe S
 | T H E R E apostrophe D
 | T H E R E apostrophe L L
 | T H E R E apostrophe R E
 | T H E R E apostrophe S
 | T H E R E apostrophe V E
 | T H E S E apostrophe R E
 | T H E S E apostrophe V E
 | T H E Y apostrophe D
 | T H E Y apostrophe D A
 | T H E Y apostrophe D apostrophe V E
 | T H E Y apostrophe L
 | T H E Y apostrophe L L
 | T H E Y apostrophe L L apostrophe V E
 | T H E Y apostrophe R E
 | T H E Y apostrophe S
 | T H E Y apostrophe V E
 | T H apostrophe I M M O R T A L L
 | T H I S apostrophe D
 | T H I S apostrophe L L
 | T H I S apostrophe S
 | T H I S apostrophe V E
 | T H O S E apostrophe R E
 | T H O S E apostrophe V E
 | T H O apostrophe T
 | T H O U apostrophe D S T
 | T H O U apostrophe L T
 | T H O U apostrophe R T
 | T H O U apostrophe S T
 | T O P S apostrophe L
 | T O apostrophe T
 | T O apostrophe V E
 | T W A S N apostrophe T
 | T W O P E N N apostrophe O R T H S
 | T apostrophe Y E
 | U N T O apostrophe T
 | U P O N apostrophe T
 | U S E D N apostrophe T
 | U S E N apostrophe T
 | U S apostrophe S
 | V I E W apostrophe T
 | W A D N apostrophe T
 | W A I T apostrophe L L
 | W A apostrophe N apostrophe T
 | W A N apostrophe T
 | W A R N apostrophe T
 | W A S N apostrophe T
 | W A S apostrophe T
 | W A Z N apostrophe T
 | W E apostrophe D
 | W E apostrophe D apostrophe V E
 | W E apostrophe L
 | W E apostrophe L L
 | W E apostrophe L L apostrophe V E
 | W E apostrophe R E
 | W E R E N apostrophe T
 | W E apostrophe S
 | W E apostrophe V E
 | W E apostrophe V E N apostrophe T
 | W H A T apostrophe D
 | W H A T E apostrophe E R
 | W H A T E V E R apostrophe S
 | W H A T apostrophe L L
 | W H A T apostrophe M
 | W H A T apostrophe R E
 | W H A T apostrophe S
 | W H A T apostrophe V E
 | W H E N apostrophe D
 | W H E N E apostrophe E R
 | W H E N apostrophe L L
 | W H E N apostrophe S
 | W H E R E apostrophe D
 | W H E R E apostrophe E R
 | W H E R E apostrophe M
 | W H E R E apostrophe R E
 | W H E R E apostrophe S
 | W H E R E apostrophe V E
 | W H I C H apostrophe D
 | W H I C H apostrophe L L
 | W H I C H apostrophe R E
 | W H I C H apostrophe S
 | W H I C H apostrophe V E
 | W H O apostrophe D
 | W H O apostrophe D A
 | W H O apostrophe D apostrophe V E
 | W H O E apostrophe E R
 | W H O apostrophe L L
 | W H O apostrophe M
 | W H O M apostrophe R E
 | W H O apostrophe R E
 | W H O apostrophe S
 | W H O apostrophe V E
 | W H Y apostrophe D
 | W H Y apostrophe M
 | W H Y N apostrophe T
 | W H Y apostrophe R E
 | W H Y apostrophe S
 | W I L L N apostrophe T
 | W I L L apostrophe V E
 | W I T H apostrophe T
 | W O L L N apostrophe T
 | W O apostrophe N apostrophe T
 | W O N apostrophe T
 | W O N apostrophe T apostrophe V E
 | W O O apostrophe T
 | W O R N apostrophe T
 | W O U apostrophe D
 | W O U L D N apostrophe T
 | W O U L D N apostrophe T A
 | W O U L D N apostrophe T apostrophe V E
 | W O U L D apostrophe V E
 | W U D N apostrophe T
 | Y apostrophe A D
 | Y apostrophe A I N apostrophe T
 | Y apostrophe A L L
 | Y A apostrophe L L
 | Y apostrophe A L L apostrophe D
 | Y apostrophe A L L apostrophe D apostrophe V E
 | Y apostrophe A L L apostrophe L L
 | Y apostrophe A L L apostrophe R E
 | Y apostrophe A L L S E L F
 | Y apostrophe A L L S E L V E S
 | Y apostrophe A L L apostrophe V E
 | Y apostrophe A R E
 | Y apostrophe A V E
 | Y E apostrophe D
 | Y E apostrophe L L
 | Y apostrophe E R E
 | Y E apostrophe R E
 | Y E S T E R E apostrophe E N
 | Y E T apostrophe S
 | Y E apostrophe V E
 | Y apostrophe E V E R
 | Y apostrophe K N E W
 | Y apostrophe K N O W
 | Y O U apostrophe D
 | Y O U apostrophe D N apostrophe T apostrophe V E
 | Y O U apostrophe D apostrophe V E
 | Y O U apostrophe L
 | Y O U apostrophe L L
 | Y O U apostrophe L L apostrophe V E
 | Y O U apostrophe R E
 | Y O U apostrophe R E N apostrophe T
 | Y O U R S apostrophe D
 | Y O U R S apostrophe L L
 | Y O U R S apostrophe V E
 | Y O U apostrophe S
 | Y O U apostrophe S E
 | Y O U apostrophe V E
 | Y O U apostrophe V E N apostrophe T
 | Y O apostrophe V E
 | Y apostrophe S E apostrophe
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
HYPHEN          : '-';

Digits
 : [0-9]+
 ;

// The '.' needs to be part of the token, otherwise it conflicts with the full stop that eends a sentence
Decimal
 : Digits '.' Digits
 ;

A : [aA];
B : [bB];
C : [cC];
D : [dD];
E : [eE];
F : [fF];
G : [gG];
H : [hH];
I : [iI];
J : [jJ];
K : [kK];
L : [lL];
M : [mM];
N : [nN];
O : [oO];
P : [pP];
Q : [qQ];
R : [rR];
S : [sS];
T : [tT];
U : [uU];
V : [vV];
W : [wW];
X : [xX];
Y : [yY];
Z : [zZ];

OtherLetter
 : [\p{Alpha}\p{General_Category=Other_Letter}]
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

