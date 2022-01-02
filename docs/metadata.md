# Document metadata

Document metadata is information about a document. Metadata often includes
a title, author name, copyright date, and keywords.

# Custom metadata

The following screenshot shows example metadata preferences:

![Metadata screenshot](images/screenshots/09.png)

The **Key** column lists metadata names and the **Value** column lists
the metadata content for each corresponding **Key**. The content may
include references to variable definitions. When the document is typeset,
the values for the variables will be substituted upon export.

When the document is exported as XHTML, the header will include the
keys and values conforming to the XHTML specification. For example:

``` html
<head>
  <title>Document Title</title>
  <meta content="science, nature" name="keywords"/>
  <meta content="Penn Surnom" name="author"/>
  <meta content="4311" name="count"/>
</head>
```

# Special metadata

When exporting the document, note the following special metadata:

* **author** -- Included as PDF metadata
* **byline** -- Replaces author in PDF metadata (e.g., for pen names)
* **count** -- Total word count in document, automatically included
* **keywords** -- Included as PDF metadata
* **title** -- Included as a `<title>` tag, rather than a `<meta>` tag

