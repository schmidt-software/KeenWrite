# Introduction

From a high level, the application architecture for converting Markdown documents is captured in the following figure:

``` diagram-graphviz
digraph {
  node [fontname = "Noto Sans" fontsize=6 height=.25 penwidth=.5];
  edge [fontname = "Noto Sans" fontsize=6  penwidth=.5 arrowsize=.5];
  node [shape=box color="{{keenwrite.palette.primary.light}}" fontcolor="{{keenwrite.palette.primary.dark}}"]
  edge [color="{{keenwrite.palette.grayscale.light}}" fontcolor="{{keenwrite.palette.grayscale.dark}}"]

  {{keenwrite.classes.processors.variable.definition}} ->   {{keenwrite.classes.processors.markdown}} [xlabel="{{keenwrite.graph.label.chain.next}}  "]
  {{keenwrite.classes.processors.markdown}} -> {{keenwrite.classes.processors.preview}} [xlabel="{{keenwrite.graph.label.chain.next}}  "]
  {{keenwrite.classes.processors.markdown}} -> Extensions [label="  contains"]

Extensions -> FencedBlockExtension
Extensions -> CaretExtension
Extensions -> ImageLinkExtension
Extensions -> TeXExtension
}
```

An extension is an addition to the Markdown parser, flexmark-java, that is used when converting the document's abstract syntax tree into an HTML document. The {{keenwrite.classes.processors.markdown}} contains both prepackaged and custom extensions.
