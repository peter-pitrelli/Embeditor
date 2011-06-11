/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor;

import embeditor.windowmanager.Window;
import embeditor.windowmanager.WindowManager;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author thommy
 */
public class Embeditor implements ActionListener, MouseListener, MouseMotionListener {

    private WindowManager wm = WindowManager.getInstance();
    private PagePanel pp;
    private FileObject currentEdit;
    private Window editWindow;
    private List<Program> programs = new LinkedList<Program>();

    public void run() throws FileNotFoundException, IOException, InterruptedException {
        JFrame mf = new JFrame("Test");
        mf.setMinimumSize(new Dimension(1024, 768));
        pp = new PagePanel();
        mf.setContentPane(pp);
        pp.bSave.addActionListener(this);
        pp.bCancel.addActionListener(this);
        pp.addMouseListener(this);
        pp.addMouseMotionListener(this);
        mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mf.setVisible(true);


        Program oocalc = new Program();
        oocalc.name = "Lyx";
        oocalc.command = "lyx";
        oocalc.fileendig = "lyx";
        oocalc.preOffset = new Point(40, 188);
        oocalc.postOffset = new Point(20, 30);
        oocalc.template = new File("templates/sample.lyx");
        oocalc.windowstring = "<file>";
        this.programs.add(oocalc);

        oocalc = new Program();
        oocalc.name = "Tabellenkalkulation";
        oocalc.command = "oocalc";
        oocalc.fileendig = "ods";
        oocalc.preOffset = new Point(40, 188);
        oocalc.postOffset = new Point(20, 30);
        oocalc.template = new File("templates/sample.ods");
        oocalc.windowstring = "<file> - ";
        this.programs.add(oocalc);
    }

    public void editFile(FileObject result) throws InterruptedException, IOException {
        this.editFile(result, null);
    }

    public void editFile(FileObject result, Rectangle r) throws InterruptedException, IOException {
        currentEdit = result;
        if (r == null) {
            r = new Rectangle(
                    currentEdit.position.x,
                    currentEdit.position.y,
                    currentEdit.screenshot.getWidth(),
                    currentEdit.screenshot.getHeight());
        }
        Point off = pp.getLocationOnScreen();
        r.setLocation(r.x + off.x, r.y + off.y);
        Program p = result.program;
        Runtime.getRuntime().exec(new String[]{
                    p.command, currentEdit.file.getAbsolutePath()
                });
        do {
            editWindow = wm.findWindow(p.windowstring.replace("<file>", currentEdit.file.getName()));
            Thread.sleep(500);
        } while (editWindow == null);
        wm.setWindowRect(editWindow, p.getOuterRectangle(r));
        wm.setAbove(editWindow, true);
        new Thread() {

            @Override
            public void run() {
                Rectangle old = null;
                System.out.println("Thread started");
                while (editWindow != null && wm.isWindow(editWindow)) {
                    Rectangle r = wm.getWindowRect(editWindow);
                    System.out.println("Window: " + r);
                    if (!r.equals(old)) {
                        Point off = pp.getLocationOnScreen();
                        System.out.println("offset: " + off);
                        pp.setCurrentEditing(
                                new Rectangle(r.x - off.x, r.y - off.y, r.width, r.height));
                        old = r;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("Thread stopped");
                pp.setCurrentEditing(null);
            }
        }.start();
    }

    public void runProgram(Program p, Rectangle r) throws FileNotFoundException, IOException, InterruptedException {
        currentEdit = new FileObject();
        currentEdit.program = p;
        currentEdit.position = r.getLocation();
        currentEdit.file = new File("tmp" + Math.random() + p.fileendig);
        FileInputStream in = new FileInputStream(p.template);
        FileOutputStream out = new FileOutputStream(currentEdit.file);
        while (in.available() > 0) {
            out.write(in.read());
        }
        out.close();
        in.close();
        this.editFile(currentEdit, r);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        new Embeditor().run();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource().equals(pp.bSave)) {
            Rectangle inner = currentEdit.program.getInnerRectangle(wm.getWindowRect(editWindow));
            currentEdit.screenshot = wm.getScreenshot(inner);
            Point off = pp.getLocationOnScreen();
            currentEdit.position = new Point(inner.x - off.x, inner.y - off.y);
            wm.sendClose(editWindow);
            while (wm.isWindow(editWindow)) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.pp.setCurrentEditing(null);
            this.pp.files.add(currentEdit);
            this.pp.repaint();
            currentEdit = null;
            editWindow = null;
        }
    }

    private FileObject findByPoint(Point p) {
        for (FileObject o : this.pp.files) {
            Rectangle r = new Rectangle(o.position.x, o.position.y,
                    o.screenshot.getWidth(), o.screenshot.getHeight());
            if (r.contains(p)) {
                return o;
            }
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        FileObject clicked = findByPoint(me.getPoint());
        if (clicked != null) {
            try {
                this.editFile(clicked);
            } catch (InterruptedException ex) {
                Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private FileObject moving = null;
    private boolean creating = false;
    private Point offset = null;

    @Override
    public void mousePressed(MouseEvent me) {
        moving = findByPoint(me.getPoint());
        if (moving != null) {
            Point p = me.getPoint();
            offset = new Point(p.x - moving.position.x, p.y - moving.position.y);
        } else {
            creating = true;
            offset = me.getPoint();
            pp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        moving = null;
        if (creating) {
            pp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            Rectangle r = new Rectangle(offset.x, offset.y, me.getPoint().x - offset.x, me.getPoint().y - offset.y);
            pp.setCreating(null);
            creating = false;
            Program p = selectProgramDialog();
            if (p != null) {
                try {
                    this.runProgram(p, r);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        if (moving != null) {
            moving.position.move(me.getX() - offset.x, me.getY() - offset.y);
            pp.repaint();
        } else if (creating) {
            Rectangle r = new Rectangle(offset.x, offset.y, me.getPoint().x - offset.x, me.getPoint().y - offset.y);
            pp.setCreating(r);
        }
    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }

    private Program selectProgramDialog() {
        JComboBox cb = new JComboBox();
        for (Program p : programs) {
            cb.addItem(p);
        }
        if (JOptionPane.showConfirmDialog(pp, cb, "Bitte auswählen", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            return (Program) cb.getSelectedItem();
        }
        return null;
    }
}
