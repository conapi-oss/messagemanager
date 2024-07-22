package nl.queuemanager.ui;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class EditorPaneWithHyperlinks extends JEditorPane {
    private static final long serialVersionUID = 1L;

    public EditorPaneWithHyperlinks(String htmlBody) {
        //super("text/html", "<html><body style=\"" + getStyle() + "\">" + htmlBody + "</body></html>");
        super("text/html", "<html><body >" + htmlBody + "</body></html>");
        addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try{
                    //OPEN THE LINK
                    Desktop.getDesktop().browse(e.getURL().toURI());
                }
                catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });

        setEditable(false);
        setBorder(null);
    }

    /*static StringBuffer getStyle() {
        // for copying style
        JLabel label = new JLabel();
        Font font = label.getFont();
        Color color = label.getBackground();

        // create some css from the label's font
        StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
        style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
        style.append("font-size:" + font.getSize() + "pt;");
        style.append("background-color: rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+");");
        return style;
    }*/
}