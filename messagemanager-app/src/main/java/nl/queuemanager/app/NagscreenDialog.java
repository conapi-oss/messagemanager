package nl.queuemanager.app;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

public class NagscreenDialog extends JDialog {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final int desiredWidth = 450;
    private final int desiredHeight = 450;

    private final Timer timer;

    private int secondsRemaining = 11;

    private final String baseTitle = "Download Message Manager 4";

    public NagscreenDialog(JFrame parent) {
        setTitle(baseTitle);
        setModal(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        Point centerPoint = new Point(
                parent.getX() + parent.getWidth() / 2,
                parent.getY() + parent.getHeight() / 2);

        setBounds((int)(centerPoint.getX() - desiredWidth/2),
                (int)(centerPoint.getY() - desiredHeight/2),
                desiredWidth, desiredHeight);

        getContentPane().setLayout(new BorderLayout(0, 0));

        try {
            URL url = getClass().getResource("/nagscreen/index.html");
            JEditorPane editorPane = new JEditorPane(url);
            editorPane.setEditable(false);
            editorPane.setOpaque(false);
            editorPane.addHyperlinkListener(openHyperLink);

            getContentPane().add(editorPane, BorderLayout.CENTER);
        } catch (IOException e) {
            logger.severe(e.toString());
        }

        timer = new Timer(1000, tick);
        timer.start();
    }

    private ActionListener tick = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            secondsRemaining--;
            if(secondsRemaining == 0) {
                timer.stop();
                setTitle(baseTitle);
                setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            } else {
                setTitle(String.format("(%d) %s", secondsRemaining, baseTitle));
            }
        }
    };

    private HyperlinkListener openHyperLink = new HyperlinkListener() {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    if(e.getDescription().equals("#close")) {
                        if(secondsRemaining == 0) {
                            NagscreenDialog.this.setVisible(false);
                        }
                    } else {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    }
                }
                catch (IOException | URISyntaxException ex) {
                    logger.severe(ex.toString());
                }
            }
        }
    };

    private String readResource(String name) {
        try {
            InputStream inputStream = getClass().getResourceAsStream(name);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = inputStream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            // StandardCharsets.UTF_8.name() > JDK 7
            return result.toString("UTF-8");
        } catch (IOException e) {
            logger.severe(e.toString());
            return e.toString();
        }
    }

}
