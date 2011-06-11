/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor;

import embeditor.windowmanager.Window;
import embeditor.windowmanager.WindowManager;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author thommy
 */
public class Embeditor implements ActionListener, MouseListener {

    private WindowManager wm = WindowManager.getInstance();
    private PagePanel pp;
    private FileObject currentEdit;
    private Window editWindow;

    public void run() throws FileNotFoundException, IOException, InterruptedException {
        JFrame mf = new JFrame("Test");
        mf.setMinimumSize(new Dimension(1024, 768));
        pp = new PagePanel();
        mf.setContentPane(pp);
        pp.bSave.addActionListener(this);
        pp.bCancel.addActionListener(this);
        pp.addMouseListener(this);
        mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Program oocalc = new Program();
        oocalc.command = "oocalc";
        oocalc.fileendig = "ods";
        oocalc.preOffset = new Point(40, 188);
        oocalc.postOffset = new Point(20, 30);
        oocalc.template = new File("/tmp/sample.ods");
        oocalc.windowstring = "<file> - OpenOffice";
        mf.setVisible(true);
        this.runProgram(oocalc, new Rectangle(100, 100, 400, 300));
    }

    public void editFile(FileObject result) throws InterruptedException, IOException
    {
        this.editFile(result, null);
    }
    
    public void editFile(FileObject result, Rectangle r) throws InterruptedException, IOException {
        currentEdit = result;
        if (r == null)
        {
            r = new Rectangle(
                    currentEdit.position.x,
                    currentEdit.position.y,
                    currentEdit.screenshot.getWidth(),
                    currentEdit.screenshot.getHeight()
                    );
        }
        Point off = pp.getLocationOnScreen();
        r.setLocation(r.x+off.x, r.y+off.y);
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
                    System.out.println("Window: "+r);
                    if (!r.equals(old)) {
                        Point off = pp.getLocationOnScreen();
                        System.out.println("offset: "+off);
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
        }.run();
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

    @Override
    public void mouseClicked(MouseEvent me) {
        for (FileObject o : this.pp.files) {
            Rectangle r = new Rectangle(o.position.x, o.position.y,
                    o.screenshot.getWidth(), o.screenshot.getHeight());
            if (r.contains(me.getPoint())) {

                try {
                    this.pp.files.remove(o);
                    this.pp.repaint();
                    this.editFile(o);
                    break;
                } catch (InterruptedException ex) {
                    Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }
}
