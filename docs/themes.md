# Themes

The application provides bundled themes and the ability to add custom
themes. This document describes the interplay between bundled themes
and building your own theme.

A theme is a set of styles, similar to cascading style sheet classes,
that instruct the user interface on how to apply colours, fonts, spacing,
highlights, drop-shadows, gradients, and so forth.

For more information on CSS, see the [W3C CSS tutorial](https://www.w3.org/Style/Examples/011/firstcss).

# Order

The order that stylesheets are applied matters so that stylesheets can
override styles defined previously. The application's user interface
is made up of the following stylesheets, applied in the order listed:

* **scene.css** --- Defines toolbar styling.
* **markdown.css** --- Defines text editor styling.
* **themes/theme_name.css** --- Bundled theme selected in preferences.
* **custom.css** --- User-defined file set in preferences.

# Customization

Create a custom theme as follows:

1. Start the application.
1. Click **File → New** to create a new file.
1. Click **File → Save As** to rename the file.
1. Save the file as `custom.css`.
1. Change the content to the following:
``` css
.root {
  -fx-base: rgb( 30, 30, 30 );
  -fx-background: -fx-base;
}
```

Next, apply the theme as follows:

1. Click **Edit → Preferences** to open the preferences dialog.
1. Click **Themes** to view the theme options.
1. Click **Browse** to select a custom theme file.
1. Browse to and select `custom.css`, created previously.
1. Click **Open**.
1. Click **Apply**.

The user interface immediately changes to a dark mode. Continue:

1. Click **OK** to close the dialog.
1. Change the **rgb** numbers in **custom.css** from `30` to `60`.
1. Click **File → Save** to save the CSS file.

The user interface immediately changes colour.

# Classes

When creating your own theme, there many classes that can be styled. The
previous section showed how to set up a rudimentary theme. Instead, start
with a template that already has a number of classes defined so that you
can tweak them to your taste. Accomplish this as follows:

1. Visit the [themes](https://github.com/DaveJarvis/keenwrite/tree/master/src/main/resources/com/keenwrite/themes) repository directory
1. Click one of the themes (e.g., `haunted_grey.css`).
1. Click **Raw**.
1. Copy the entire text.
1. Return to `custom.css`.
1. Delete the contents.
1. Paste the copied text.
1. Save the file.

To see how the CSS styles are applied to the text editor, open
[markdown.css](https://github.com/DaveJarvis/keenwrite/blob/master/src/main/resources/com/keenwrite/editor/markdown.css), which is also in the repository.

# Modena

The basic theme used by the application is _Modena Light_. Typically we
only need to override a few classes to completely change the application's
look and feel. For a full listing of available styles see the OpenJDK's
[Modena CSS file](https://github.com/openjdk/jfx/blob/master/modules/javafx.controls/src/main/resources/com/sun/javafx/scene/control/skin/modena/modena.css).

# JavaFX CSS

The [Java CSS Reference Guide](https://openjfx.io/javadoc/11/javafx.graphics/javafx/scene/doc-files/cssref.html) is exhaustive. In addition to showing many
differences between JavaFX CSS and W3C CSS, the guide introduces numerous
helpful functions for manipulating colours and gradients using existing
colour definitions.

# RichTextFX

The application uses RichTextFX to render the text editor. Styling various
text editor classes can require using the prefix `-rtfx` instead of the
regular JavaFX `-fx`.

# Submit

Send in your themes! If you have a theme you'd like to contribute to the
project, or improvements to an existing theme, do pass it along. Either open a new issue in the [issue tracker](https://github.com/DaveJarvis/keenwrite/issues) that contains the CSS file or submit a pull request.

