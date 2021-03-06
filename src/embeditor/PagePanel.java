/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

/**
 *
 * @author thommy
 */
public class PagePanel extends JPanel {

    public JButton bSave = new JButton("Save");
    public JButton bCancel = new JButton("Cancel");
    public List<FileObject> files = new LinkedList<FileObject>();
    private Rectangle currentEditing;
    private Rectangle creating;

    public PagePanel() {
        this.setPreferredSize(new Dimension(1024, 768));
        this.setMinimumSize(new Dimension(1024, 768));
        
        bSave.setVisible(false);
        bCancel.setVisible(false);
        this.add(bSave);
        this.add(bCancel);
        this.setVisible(true);
    }

    public void setCreating(Rectangle r)
    {
        this.creating = r;
        this.repaint();
    }
    
    public void setCurrentEditing(Rectangle r) {
        this.currentEditing = r;
        if (r == null) {
            bSave.setVisible(false);
            //bCancel.setVisible(false);
        } else {
            this.setMinimumSize(new Dimension(r.width, r.height+bSave.getHeight()));
            bSave.setLocation(new Point(r.x + r.width + 10, r.y));
            //bCancel.setLocation(new Point(r.x, r.y + r.height + 25));
            bSave.setVisible(true);
            //bCancel.setVisible(true);
        }
        this.repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (FileObject f : files) {
            g.drawImage(f.screenshot, f.position.x, f.position.y, null);
        }
        if (currentEditing != null) {
            g.setColor(Color.RED);
            g.drawRect(currentEditing.x-2, currentEditing.y-2, currentEditing.width+4, currentEditing.height+4);
        }
        if (creating != null)
        {
            g.setColor(Color.GREEN);
            g.drawRect(creating.x, creating.y, creating.width, creating.height);
        }
    }
}
