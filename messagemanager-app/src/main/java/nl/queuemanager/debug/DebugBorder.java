package nl.queuemanager.debug;

import javax.swing.border.Border;
import java.awt.*;
 
public class DebugBorder implements Border {
    private Border b;
 
    public DebugBorder(Border b) {
        this.b = b;
    }
 
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        b.paintBorder(c, g, x, y, width, height);
        Insets insets = b.getBorderInsets(c);
        Color layerColor = new Color(0.0f, 1.0f, 0.0f, 0.35f);
        g.setColor(layerColor);
        // top
        g.fillRect(x, y, width, insets.top);
        // left
        g.fillRect(x, y + insets.top, insets.left, height - insets.bottom - insets.top);
        // bottom
        g.fillRect(x, y + height - insets.bottom, width, insets.bottom);
        // right
        g.fillRect(x + width - insets.right, y + insets.top, insets.right, height - insets.bottom - insets.top);
    }
 
    public Insets getBorderInsets(Component c) {
        return b.getBorderInsets(c);
    }
 
    public boolean isBorderOpaque() {
        return b.isBorderOpaque();
    }
 
    public Border getDelegate() {
        return b;
    }
}