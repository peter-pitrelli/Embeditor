/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.imageio.ImageIO;

/**
 *
 * @author thommy
 */
public class FileObject implements Serializable {

    static FileObject readFromFile(File f) throws FileNotFoundException, IOException, ClassNotFoundException {
        Object o = new ObjectInputStream(new FileInputStream(f)).readObject();
        if (o instanceof FileObject) {
            FileObject fo = (FileObject) o;
            File img = new File(f.getAbsolutePath() + ".png");
            if (img.exists()) {
                fo.screenshot = ImageIO.read(img);
            }
            return fo;
        }
        return null;
    }

    static void writeToFile(FileObject o, File f) throws FileNotFoundException, IOException
    {
        writeToFile(o,f,true);
    }
    
    static void writeToFile(FileObject o, File f, boolean saveScreenshot) throws FileNotFoundException, IOException {
        BufferedImage tmp = o.screenshot;
        if (o.screenshot != null) {
            if (saveScreenshot) {
                ImageIO.write(o.screenshot, "png", new File(f.getAbsolutePath() + ".png"));
            }
            o.screenshot = null;
        }
        new ObjectOutputStream(new FileOutputStream(f)).writeObject(o);
        o.screenshot = tmp;
    }
    protected File file;
    protected BufferedImage screenshot;
    protected Point position;
    protected Program program;

    public String toString() {
        return this.file.getName() + "-" + this.program.name;
    }
}
