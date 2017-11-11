# Introduction

This document describes how to use the application.

# Variable Definitions

Variable definitions provide a way to insert key names having associated values into a document. The variable names and values are declared inside an external file using the [YAML](http://www.yaml.org/) file format. Simply put, variables are written in the file as follows:

```
key: value
```

Any number of variables can be defined, in any order:

```
key_1: Value 1
key_2: Value 2
```

Variables can reference other variables by enclosing the key name within dollar symbols:

```
key: Value
key_1: $key$ 1
key_2: $key$ 2
```

Variables can use a nested structure to help group related information:

```
novel:
  title: Book Title
  author: Author Name
  isbn: 978-3-16-148410-0
```

Use a period to reference nested keys, such as:

```
novel:
  author: Author Name
copyright:
  owner: $novel.author$
```

Save the variable definitions in a file having an extension of `.yaml` or `.yml`.

# Document Editing

The application's purpose is to completely separate the document's content from its presentation. To achieve this, documents are composed using a [plain text](http://spec.commonmark.org/0.28/) format.

## Create Document

Start a new document as follows:

1. Start the application.
1. Click **File → New** to create an empty document to edit.
1. Click **File → Open** to open a variable definition file.
1. Change **Source Files** to **Definition Files** to list definition files.
1. Browse to and select a file saved with a `.yaml` or `.yml` extension.
1. Click **Open**.

The variable definitions appear in the variable definition pane under the heading of **Definitions**.

## Edit Document

Edit the document as normal. Notice how the preview pane updates as new content is added. The toolbar shows various icons that perform different formatting operations. Try them to see how they appear in the preview pane. Other operations not shown on the toolbar include:

* Struck text (enclose the words within `~~` and `~~`)
* Horizontal rule (use `---` on an otherwise empty line).

The preview pane shows one way to interpret and format the document, but many other presentations are possible.

## Insert Variable

Let's assume that the variable definitions loaded into the application include:

```
novel:
  title: Diary of $novel.author$
  author: Anne Frank
```

To reference a variable, type in the key name enclosed within dollar symbols, such as:

```
The novel "$novel.title$" is one of the most widely read books in the world.
```

The preview pane shows:

> The novel "Diary of Anne Frank" is one of the most widely read books in the world.

As it is laborious to type in variable names, it is possible to inject the variable name using autocomplete. Accomplish this as follows:

1. Create a new file.
1. Type in a partial variable value, such as **Dia**.
1. Press `Ctrl+Space` (hold down the `Control` key and tap the spacebar).

The editor shows:

```
$novel.title$
```

The preview pane shows:

```
Diary of Anne Frank
```

The variable name is inserted into the document and the preview pane shows the variable's value.

