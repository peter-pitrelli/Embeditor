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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

/**
 *
 * @author thommy
 */
public class Embeditor extends TransferHandler implements ActionListener, MouseListener, MouseMotionListener
{

  private WindowManager wm = WindowManager.getInstance();
  private PagePanel pp;
  private FileObject currentEdit;
  private FileObject selectedFileObject;
  private Window editWindow;
  private JPopupMenu objectMenu;
  private JPopupMenu programsMenu;
  private Rectangle selectedRectangle;
  private JPopupMenu pageMenu;
  private int currentPage = 1;
  private File currentFolder = new File("default/");
  private List<Program> programs = new LinkedList<Program>();

  private void buildMenu()
  {
    objectMenu = new JPopupMenu();
    JMenuItem moveUp = new JMenuItem("up");
    moveUp.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent ae)
      {
        Embeditor.this.changeZpos(selectedFileObject, true);
      }
    });
    objectMenu.add(moveUp);
    JMenuItem moveDown = new JMenuItem("down");
    moveUp.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent ae)
      {
        Embeditor.this.changeZpos(selectedFileObject, false);
      }
    });
    objectMenu.add(moveDown);
    JMenuItem delete = new JMenuItem("delete");
    delete.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent ae)
      {
        Embeditor.this.deleteObject(selectedFileObject);
      }
    });
    objectMenu.add(delete);

    programsMenu = new JPopupMenu();
    for (final Program p : this.programs)
    {
      JMenuItem tmp = new JMenuItem(p.name);
      tmp.addActionListener(new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent ae)
        {
          try
          {
            Embeditor.this.runProgram(p, Embeditor.this.selectedRectangle);
          }
          catch (FileNotFoundException ex)
          {
            Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
          }
          catch (IOException ex)
          {
            Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
          }
          catch (InterruptedException ex)
          {
            Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      });
      programsMenu.add(tmp);
    }

    pageMenu = new JPopupMenu();
    JMenuItem next = new JMenuItem("next");
    next.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent ae)
      {
        Embeditor.this.loadPage(currentPage++);
      }
    });
    pageMenu.add(next);
    JMenuItem prev = new JMenuItem("previous");
    prev.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent ae)
      {
        if (currentPage > 1)
        {
          Embeditor.this.loadPage(currentPage--);
        }
      }
    });
    pageMenu.add(prev);
  }

  private File getScreenshotFile(File f)
  {
    return new File(f.getAbsolutePath() + ".png");
  }

  private File getScreenshotFile(FileObject o)
  {
    return new File(getObjectFile(o).getAbsolutePath() + ".png");
  }

  private File getObjectFile(FileObject o)
  {
    return new File(currentFolder, "" + currentPage + "/" + o.toString());
  }

  private void deleteObject(FileObject o)
  {
    this.pp.files.remove(o);
    if (o.file.exists())
    {
      o.file.delete();
    }
    if (getScreenshotFile(o).exists())
    {
      getScreenshotFile(o).delete();
    }
    if (getObjectFile(o).exists())
    {
      getObjectFile(o).delete();
    }
    this.pp.repaint();
  }

  public void changeZpos(FileObject o, boolean up)
  {
    int i = pp.files.indexOf(o);
    if (i >= 0)
    {
      if (up && i < pp.files.size() - 1)
      {
        FileObject tmp = pp.files.get(i + 1);
        pp.files.set(i + 1, o);
        pp.files.set(i, tmp);
      }
      else
      {
        if (!up && i > 0)
        {
          FileObject tmp = pp.files.get(i - 1);
          pp.files.set(i - 1, o);
          pp.files.set(i, tmp);
        }
      }
      pp.repaint();
    }
  }

  private void savePage()
  {
    for (FileObject f : pp.files)
    {
      try
      {
        FileObject.writeToFile(f, getObjectFile(f), null);
      }
      catch (FileNotFoundException ex)
      {
        Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (IOException ex)
      {
        Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  private void loadPage(int page)
  {
    this.savePage();
    this.pp.files.clear();
    File folder = new File(this.currentFolder, "" + page + "/");
    File dataFolder = new File(folder, "data/");
    if (folder.isDirectory() && folder.exists())
    {
      for (File f : folder.listFiles())
      {
        if (f.isFile() && !f.getAbsolutePath().endsWith(".png"))
        {
          try
          {
            this.pp.files.add(FileObject.readFromFile(f, getScreenshotFile(f)));
          }
          catch (FileNotFoundException ex)
          {
            Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
          }
          catch (IOException ex)
          {
            Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
          }
          catch (ClassNotFoundException ex)
          {
            Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }
    }
    else
    {
      folder.mkdir();
      dataFolder.mkdir();
    }
    pp.repaint();
  }

  public void run() throws FileNotFoundException, IOException, InterruptedException, TooManyListenersException
  {
    this.loadPrograms();
    this.buildMenu();



    JFrame mf = new JFrame("Test");
    mf.setMinimumSize(new Dimension(1024, 768));
    pp = new PagePanel();
    mf.setContentPane(pp);
    pp.bSave.addActionListener(this);
    pp.bCancel.addActionListener(this);
    pp.addMouseListener(this);
    pp.addMouseMotionListener(this);
    DropTarget dt = new DropTarget();
    pp.setDropTarget(dt);
    dt.addDropTargetListener(new DropTargetListener()
    {

      @Override
      public void dragEnter(DropTargetDragEvent dtde)
      {
      }

      @Override
      public void dragOver(DropTargetDragEvent dtde)
      {
      }

      @Override
      public void dropActionChanged(DropTargetDragEvent dtde)
      {
      }

      @Override
      public void dragExit(DropTargetEvent dte)
      {
      }

      @Override
      public void drop(DropTargetDropEvent e)
      {
        Transferable tr = e.getTransferable();
        if (tr.isDataFlavorSupported(DataFlavor.stringFlavor))
        {
          try
          {
            e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            String files = (String) tr.getTransferData(DataFlavor.stringFlavor);
            Embeditor.this.importFile(files);
            e.getDropTargetContext().dropComplete(true);
          }
          catch (UnsupportedFlavorException ex)
          {
            Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
          }
          catch (Exception ex)
          {
            Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        else
        {
          System.out.println("Unsupported data flavor");
        }
      }
    });
    mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mf.addWindowListener(new WindowListener()
    {

      @Override
      public void windowOpened(WindowEvent we)
      {
      }

      @Override
      public void windowClosing(WindowEvent we)
      {
        Embeditor.this.savePage();
      }

      @Override
      public void windowClosed(WindowEvent we)
      {
      }

      @Override
      public void windowIconified(WindowEvent we)
      {
      }

      @Override
      public void windowDeiconified(WindowEvent we)
      {
      }

      @Override
      public void windowActivated(WindowEvent we)
      {
      }

      @Override
      public void windowDeactivated(WindowEvent we)
      {
      }
    });

    this.currentFolder.mkdir();
    this.currentPage = 1;
    this.loadPage(1);

    mf.setVisible(true);



  }

  private void importFile(String files) throws FileNotFoundException, IOException, InterruptedException
  {
    Matcher m = Pattern.compile("file://(.*)\\.([^\\.\r\n]*)").matcher(files);
    if (m.find())
    {
      String filename = m.group(1);
      String extension = m.group(2);
      if (extension.equals("desktop"))
      {
        addProgramDialog(new File(filename + "." + extension));
      }
      else
      {
        for (Program p : this.programs)
        {
          if (p.fileendig.equals(extension))
          {
            Program tmp = new Program();
            tmp.command = p.command;
            tmp.fileendig = p.fileendig;
            tmp.name = p.name;
            tmp.postOffset = p.postOffset;
            tmp.preOffset = p.preOffset;
            //TODO: OS Specific
            tmp.template = new File(filename + "." + extension);
            tmp.windowstring = p.windowstring;
            this.runProgram(tmp, new Rectangle(10, 10, 300, 300));
            return;
          }
        }
        JOptionPane.showMessageDialog(this.pp, "The File Format : " + extension + " is not supported by any program");
      }
    }
  }

  private void loadPrograms()
  {
    Program oocalc = new Program();
    oocalc.name = "Lyx";
    oocalc.command = "lyx";
    oocalc.fileendig = "lyx";
    oocalc.preOffset = new Point(2, 120);
    oocalc.postOffset = new Point(2, 30);
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

    oocalc = new Program();
    oocalc.name = "GNU Paint";
    oocalc.command = "gpaint";
    oocalc.fileendig = "bmp";
    oocalc.preOffset = new Point(100, 188);
    oocalc.postOffset = new Point(20, 30);
    oocalc.template = new File("templates/sample.bmp");
    oocalc.windowstring = "<file>";
    this.programs.add(oocalc);
  }

  public void editFile(FileObject result) throws InterruptedException, IOException
  {
    this.editFile(result, null);
  }

  public void editFile(FileObject result, Rectangle r) throws InterruptedException, IOException
  {
    currentEdit = result;
    if (r == null)
    {
      r = new Rectangle(
        currentEdit.position.x,
        currentEdit.position.y,
        currentEdit.screenshot.getWidth(),
        currentEdit.screenshot.getHeight());
    }
    Point off = pp.getLocationOnScreen();
    r.setLocation(r.x + off.x, r.y + off.y);
    Program p = result.program;
    Runtime.getRuntime().exec(new String[]
      {
        p.command, currentEdit.file.getAbsolutePath()
      });
    do
    {
      editWindow = wm.findWindow(p.windowstring.replace("<file>", currentEdit.file.getName()));
      Thread.sleep(500);
    }
    while (editWindow == null);
    wm.setWindowRect(editWindow, p.getOuterRectangle(r));
    wm.setAbove(editWindow, true);
    r = wm.getWindowRect(editWindow);
    if (r != null)
    {
      off = pp.getLocationOnScreen();
      pp.setCurrentEditing(
        new Rectangle(r.x - off.x, r.y - off.y, r.width, r.height));
    }
    new Thread()
    {

      @Override
      public void run()
      {
        Rectangle old = null;
        while (editWindow != null && wm.isWindow(editWindow))
        {
          Rectangle r = wm.getWindowRect(editWindow);
          if (r != null && !r.equals(old))
          {
            Point off = pp.getLocationOnScreen();
            pp.setCurrentEditing(
              new Rectangle(r.x - off.x, r.y - off.y, r.width, r.height));
            old = r;
          }
          try
          {
            Thread.sleep(200);
          }
          catch (InterruptedException ex)
          {
            Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        pp.setCurrentEditing(null);
      }
    }.start();
  }

  public void runProgram(Program p, Rectangle r) throws FileNotFoundException, IOException, InterruptedException
  {
    currentEdit = new FileObject();
    currentEdit.program = p;
    currentEdit.position = r.getLocation();
    currentEdit.file = new File(this.currentFolder, "" + this.currentPage + "/data/file" + Math.random() + "." + p.fileendig);
    FileInputStream in = new FileInputStream(p.template);
    FileOutputStream out = new FileOutputStream(currentEdit.file);
    while (in.available() > 0)
    {
      out.write(in.read());
    }
    out.close();
    in.close();
    this.editFile(currentEdit, r);
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, TooManyListenersException
  {
    new Embeditor().run();
  }

  @Override
  public void actionPerformed(ActionEvent ae)
  {
    if (ae.getSource().equals(pp.bSave))
    {
      Rectangle inner = currentEdit.program.getInnerRectangle(wm.getWindowRect(editWindow));
      currentEdit.screenshot = wm.getScreenshot(inner);
      Point off = pp.getLocationOnScreen();
      currentEdit.position = new Point(inner.x - off.x, inner.y - off.y);
      wm.sendClose(editWindow);
      while (wm.isWindow(editWindow))
      {
        try
        {
          Thread.sleep(200);
        }
        catch (InterruptedException ex)
        {
          Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      this.pp.setCurrentEditing(null);
      this.pp.files.add(currentEdit);
      this.pp.repaint();
      try
      {
        FileObject.writeToFile(currentEdit, getObjectFile(currentEdit), getScreenshotFile(currentEdit));
      }
      catch (FileNotFoundException ex)
      {
        Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (IOException ex)
      {
        Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
      }
      currentEdit = null;
      editWindow = null;
    }
  }

  private FileObject findByPoint(Point p)
  {
    //TODO: Iterate backwards to get upper most image
    for (int i = this.pp.files.size() - 1; i >= 0; i--)
    {
      FileObject o = this.pp.files.get(i);
      Rectangle r = new Rectangle(o.position.x, o.position.y,
        o.screenshot.getWidth(), o.screenshot.getHeight());
      if (r.contains(p))
      {
        return o;
      }
    }
    return null;
  }

  @Override
  public void mouseClicked(MouseEvent me)
  {
    FileObject clicked = findByPoint(me.getPoint());
    if (me.getButton() == MouseEvent.BUTTON3)
    {
      if (clicked != null)
      {
        this.selectedFileObject = clicked;
        this.objectMenu.show(pp, me.getX(), me.getY());
      }
      else
      {
        this.pageMenu.show(pp, me.getX(), me.getY());

      }
    }
    else
    {
      if (clicked != null)
      {
        try
        {
          this.editFile(clicked);
        }
        catch (InterruptedException ex)
        {
          Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
          Logger.getLogger(Embeditor.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }
  private FileObject moving = null;
  private boolean creating = false;
  private Point offset = null;

  @Override
  public void mousePressed(MouseEvent me)
  {
    if (me.getButton() == 1)
    {
      moving = findByPoint(me.getPoint());
      if (moving != null)
      {
        Point p = me.getPoint();
        offset = new Point(p.x - moving.position.x, p.y - moving.position.y);
      }
      else
      {
        creating = true;
        offset = me.getPoint();
        pp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent me)
  {
    moving = null;
    if (creating)
    {
      pp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      Rectangle r = new Rectangle(offset.x, offset.y, me.getPoint().x - offset.x, me.getPoint().y - offset.y);
      pp.setCreating(null);
      creating = false;
      this.selectedRectangle = r;
      this.programsMenu.show(pp, me.getX(), me.getY());
    }
  }

  @Override
  public void mouseEntered(MouseEvent me)
  {
  }

  @Override
  public void mouseExited(MouseEvent me)
  {
  }

  @Override
  public void mouseDragged(MouseEvent me)
  {
    if (moving != null)
    {
      moving.position.move(me.getX() - offset.x, me.getY() - offset.y);
      pp.repaint();
    }
    else
    {
      if (creating)
      {
        Rectangle r = new Rectangle(offset.x, offset.y, me.getPoint().x - offset.x, me.getPoint().y - offset.y);
        pp.setCreating(r);
      }
    }
  }

  @Override
  public void mouseMoved(MouseEvent me)
  {
  }

  private void ReadFromDesktopFile(File file, Program p) throws FileNotFoundException, IOException
  {
    BufferedReader fis = new BufferedReader(new FileReader(file));
    String line = fis.readLine();
    if (line.equals("[Desktop Entry]"))
    {
      line = fis.readLine();
      while (line != null && !line.startsWith("["))
      {

        Matcher m = Pattern.compile("([^=]*)=(.*)").matcher(line);
        if (m.find() && m.groupCount() == 2)
        {
          String name = m.group(1);
          String value = m.group(2);
          if (name.equals("Name"))
          {
            p.name = value;
          }
          else
          {
            if (name.equals("Exec"))
            {
              p.command = value;
            }
          }
        }
        line = fis.readLine();
      }
    }
  }
  
  private void addProgramDialog(File file) throws FileNotFoundException, IOException
  {
    Program p = new Program();
    this.ReadFromDesktopFile(file, p);
    if (p.name==null){
      p.name = JOptionPane.showInputDialog(pp, "Wie heisst das Programm?");
    }
    
  }
}
