package com.marinodev.pixelartgenerator;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class UrlTextPane extends JTextPane {
    public UrlTextPane() {
        super();
        this.setEditable(false);
        this.addHyperlinkListener(new UrlHyperlinkListener());
        this.setContentType("text/html");
    }

    public UrlTextPane(String url) {
        super();
        this.setEditable(false);
        this.addHyperlinkListener(new UrlHyperlinkListener());
        this.setContentType("text/html");
        this.setText(url);
    }

    private static class UrlHyperlinkListener implements HyperlinkListener {
        @Override
        public void hyperlinkUpdate(final HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (final IOException | URISyntaxException e) {
                    throw new RuntimeException("Can't open URL", e);
                }
            }
        }
    }

    @Override
    public void setText(final String url) {
        super.setText("<html><body style=\"font-size: 8.5px;font-family: Tahoma, sans-serif\">" +
                "<a href=\"" + url + "\">" + url + "</a>" +
                "</body></html>");
    }
}