# Building

The lexicon files are retrieved from SymSpell in the parent directory:

svn export \
  https://github.com/wolfgarbe/SymSpell/trunk/SymSpell.FrequencyDictionary/ lexicons

The lexicons and bigrams are both space-separated, but parsing a
tab-delimited file is easier, so change them to tab-separated files.
