# Fenced Divs

This section describes the syntax to generate HTML `div` elements. The
syntax is known as a _fenced div_.

# Basic Syntax

A fenced div has the following basic syntax:

``` markdown
::: name
Content
:::
```

To start a fenced div, begin a line with at least three colons (`:::`),
followed by at least one space, followed by any word. Content may follow
immediately on the next line. Terminate the fenced div with at least
three colons. The terminating colons needn't match in number to the starting
colons, but it's a good idea to maintain symmetry.

The HTML that is generated from the above fenced div will resemble:

``` html
<div class="name">
<p>Content</p>
</div>
```

# Extended Syntax

A fenced div may use an extended syntax. The extended syntax can provide
a unique identifier, multiple class names, and key/value data pairs. For
example:

``` markdown
::: {#poem-01 .stanza author="Emily Dickinson" year=1890}
Because I could not stop for Death —
He kindly stopped for me —
The Carriage held but just Ourselves —
And Immortality.
:::
```

The above snippet produces:

``` html
<div id="poem-01" class="stanza" data-author="Emily Dickinson" data-year="1890">
<p>Because I could not stop for Death —
He kindly stopped for me —
The Carriage held but just Ourselves —
And Immortality.</p>
</div>
```

Note that when using the extended syntax, class styles must be prefixed with
a period (e.g., `.stanza` in the example).

# Nested Syntax

Fenced divs may be nested, such as in the following example:

``` markdown
::: poem
:::::: stanza
Because I could not stop for Death —
He kindly stopped for me —
The Carriage held but just Ourselves —
And Immortality.
::::::
:::
```

The above example produces:

``` html
<div class="poem"><div class="stanza">
<p>Because I could not stop for Death —
He kindly stopped for me —
The Carriage held but just Ourselves —
And Immortality.</p>
</div></div>
```

