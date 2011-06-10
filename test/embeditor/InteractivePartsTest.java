/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author thommy
 */
public class InteractivePartsTest {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {

            Program oocalc = new Program();
            oocalc.command = "/usr/lib/libreoffice/program/soffice.bin";
            oocalc.fileendig = "ods";
            oocalc.preOffset = new Point(40, 188);
            oocalc.postOffset = new Point(20, 30);
            oocalc.template = new File("/tmp/sample.ods");
            oocalc.windowstring = "LibreOffice";

            Embeditor e = new Embeditor();
            FileObject fo = e.runProgram(oocalc, new Rectangle(100, 100, 500, 400));
            while (JOptionPane.showConfirmDialog(null, new ImageIcon(fo.screenshot)) == JOptionPane.YES_OPTION){
                e.editFile(fo);
            }
    }
}
