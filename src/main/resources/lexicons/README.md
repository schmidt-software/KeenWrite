# Lexicons

This directory contains lexicons used for spell checking. Each lexicon
file contains tab-delimited word-frequency pairs.

Compiling a high-quality list of correctly spelled words requires the
following steps:

1. Download a unigram frequency list for all words for a given language.
1. Download a high-quality source list of correctly spelled words.
1. Filter the unigram frequency list using all words in the source list.
1. Sort the filtered list by the frequency in descending order.

The latter steps can be accomplished as follows:

    # Extract unigram and frequency based on existence in source lexicon.
    for i in $(cat source-lexicon.txt); do
      grep -m 1 "^$i"$'\t' unigram-frequencies.txt;
    done > filtered.txt

    # Sort numerically (-n) using column two (-k2) in reverse order (-r).
    sort -n -k2 -r filtered.txt > en.txt

There may be more efficient ways to filter the data, which takes a few hours
to complete (on modern hardware).

# Resources

There are numerous sources of word and frequency lists available, including:

* https://storage.googleapis.com/books/ngrams/books/datasetsv3.html
* https://github.com/hermitdave/FrequencyWords/
* https://github.com/neilk/wordfrequencies

