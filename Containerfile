FROM alpine:latest

RUN apk --update add --no-cache fontconfig curl
RUN rm -rf /var/cache

# Download fonts.
ENV FONT_DIR=/usr/share/fonts/user
RUN mkdir -p $FONT_DIR
WORKDIR $FONT_DIR

ADD "https://fonts.google.com/download?family=Roboto" "roboto.zip"
ADD "https://fonts.google.com/download?family=Inconsolata" "inconsolata.zip"
ADD "https://github.com/adobe-fonts/source-serif/releases/download/4.004R/source-serif-4.004.zip" "source-serif.zip"
ADD "https://github.com/googlefonts/Libre-Baskerville/blob/master/fonts/ttf/LibreBaskerville-Bold.ttf" "LibreBaskerville-Bold.ttf"
ADD "https://github.com/googlefonts/Libre-Baskerville/blob/master/fonts/ttf/LibreBaskerville-Italic.ttf" "LibreBaskerville-Italic.ttf"
ADD "https://github.com/googlefonts/Libre-Baskerville/blob/master/fonts/ttf/LibreBaskerville-Regular.ttf" "LibreBaskerville-Regular.ttf"
ADD "https://www.omnibus-type.com/wp-content/uploads/Archivo-Narrow.zip" "archivo-narrow.zip"

# Unpack fonts (prior to ConTeXt).
RUN unzip -j -o roboto.zip "*.ttf"
RUN unzip -j -o inconsolata.zip "*.ttf"
RUN unzip -j -o source-serif.zip "source-serif-4.004/OTF/SourceSerif4-*.otf"
RUN unzip -j -o archivo-narrow.zip "Archivo-Narrow/otf/*.otf"
RUN rm -f roboto.zip
RUN rm -f inconsolata.zip
RUN rm -f source-serif.zip
RUN rm -f archivo-narrow.zip

# Update system font cache.
RUN fc-cache -f -v

WORKDIR "/opt"

# Download themes.
ADD "https://github.com/DaveJarvis/keenwrite-themes/releases/latest/download/theme-pack.zip" "theme-pack.zip"
RUN unzip theme-pack.zip

# Download ConTeXt.
ADD "http://lmtx.pragma-ade.nl/install-lmtx/context-linuxmusl.zip" "context.zip"
RUN unzip context.zip -d context
RUN rm -f context.zip

# Install ConTeXt.
WORKDIR "context"
RUN sh install.sh

# Configure environment to find ConTeXt.
ENV PROFILE=/etc/profile
ENV CONTEXT_HOME=/opt/context
RUN echo "export CONTEXT_HOME=\"$CONTEXT_HOME\"" >> $PROFILE
RUN echo "export PATH=\"\$PATH:\$CONTEXT_HOME/tex/texmf-linuxmusl/bin\"" >> $PROFILE
RUN echo "export OSFONTDIR=\"/usr/share/fonts//\""
RUN echo "PS1=\"docker:\\w\\\$ \\"" >> $PROFILE

# Trim the fat.
RUN source $PROFILE
RUN rm -rf $CONTEXT_HOME/tex/texmf-context/doc
RUN find . -type f -name "*.pdf" -exec rm {} \;

# Prepare to process text files.
WORKDIR "/root"

ENTRYPOINT ["/bin/sh"]

