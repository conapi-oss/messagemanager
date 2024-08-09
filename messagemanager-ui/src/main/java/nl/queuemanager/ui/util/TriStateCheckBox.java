package nl.queuemanager.ui.util;

import javax.swing.*;
import java.awt.*;

public class TriStateCheckBox extends JCheckBox {
    public enum State { UNCHECKED, CHECKED, THIRD_STATE }
    private State state;
    private boolean isAdjusting = false;

    public TriStateCheckBox(String text) {
        super(text);
        setState(State.UNCHECKED); // Initialize state

        addItemListener(e -> {
            if (!isAdjusting) {
                isAdjusting = true;
                if (state == State.UNCHECKED) {
                    setState(State.CHECKED);
                } else if (state == State.CHECKED) {
                    setState(State.THIRD_STATE);
                } else {
                    setState(State.UNCHECKED);
                }
                isAdjusting = false;
            }
        });
    }

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        if (this.state != newState) {
            State oldState = this.state;
            this.state = newState;
            updateTriState();
            firePropertyChange("state", oldState, newState);
        }
    }

    private void updateTriState() {
        isAdjusting = true;
        switch (state) {
            case UNCHECKED:
                setSelected(false);
                setText(getText().replace(" (Inverse)", ""));
                break;
            case CHECKED:
                setSelected(true);
                setText(getText().replace(" (Inverse)", ""));
                break;
            case THIRD_STATE:
                setSelected(false);  // Changed this to false
                setText(getText() + " (Inverse)");
                break;
        }
        isAdjusting = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == State.THIRD_STATE) {
            drawThirdState(g);
        }
    }

    private void drawThirdState(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2));

            Icon icon = UIManager.getIcon("CheckBox.icon");
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();

            int x = getInsets().left;
            int y = (getHeight() - iconHeight) / 2;

            int xPadding = 5;
            g2.drawLine(x + xPadding, y + xPadding,
                    x + iconWidth - xPadding, y + iconHeight - xPadding);
            g2.drawLine(x + xPadding, y + iconHeight - xPadding,
                    x + iconWidth - xPadding, y + xPadding);
        } finally {
            g2.dispose();
        }
    }
}
