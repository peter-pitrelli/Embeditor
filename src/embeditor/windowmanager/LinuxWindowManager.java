/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor.windowmanager;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author thommy
 */
public class LinuxWindowManager extends WindowManager {

    private String wmctrl(String[] args) throws IOException {
        String[] cmd = new String[args.length + 1];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        cmd[0] = "wmctrl";
        System.arraycopy(args, 0, cmd, 1, args.length);

        Process p;
        p = Runtime.getRuntime().exec(cmd);
        if (p == null) {
            throw new RuntimeException("Process could not be started");
        } else {
            InputStream is = p.getInputStream();
            int i;
            do {
                i = is.read();
                if (0 <= i && i <= 255) {
                    result.write(i);
                }
            } while (i >= 0);
        }
        return result.toString();
    }

    @Override
    public Window findWindow(String name) {
        try {
            String output = wmctrl(new String[]{
                        "-l", "-p"
                    });
            Matcher matcher = Pattern.compile("(0x[^\\s]+)\\s+\\d+\\s+(\\d+)\\s+[^\\s]+\\s+(.*)\\n").matcher(output);
            while (matcher.find()) {
                if (matcher.group(3).contains(name)) {
                    return new Window(matcher.group(1));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LinuxWindowManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return null;
    }

    @Override
    public void moveWindow(Window w,
            int x,
            int y) {
        try {
            this.wmctrl(new String[]{
                        "-i", "-r", w.getId(), "-e", "0," + x + "," + y + ",-1,-1"
                    });
        } catch (IOException ex) {
            Logger.getLogger(LinuxWindowManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void resizeWindow(Window w, int width, int h) {
        try {
            this.wmctrl(new String[]{
                        "-i", "-r", w.getId(), "-e", "0,-1,-1," + width + "," + h
                    });
        } catch (IOException ex) {
            Logger.getLogger(LinuxWindowManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setWindowRect(Window w, Rectangle r) {
        try {
            this.wmctrl(new String[]{
                        "-i", "-r", w.getId(), "-e", "0," + r.x + "," + r.y + "," + r.width + "," + r.height
                    });
        } catch (IOException ex) {
            Logger.getLogger(LinuxWindowManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Rectangle getWindowRect(Window w) {
        try {
            String output = wmctrl(new String[]{
                        "-l", "-G"
                    });
            Matcher matcher = Pattern.compile(w.getId() + "\\s+\\d+\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(.*)\\n").matcher(output);
            while (matcher.find()) {
                // Values workaround works for Gnome3 if you are not near to 0 0
                return new Rectangle(Integer.parseInt(matcher.group(1)),// - 2,
                        Integer.parseInt(matcher.group(2)),// - 123 + 75,
                        Integer.parseInt(matcher.group(3)),
                        Integer.parseInt(matcher.group(4)));
            }
        } catch (IOException ex) {
            Logger.getLogger(LinuxWindowManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return null;
    }

    @Override
    public Window findByPID(int pid) {
        try {
            String output = wmctrl(new String[]{
                        "-l", "-p"
                    });
            Matcher matcher = Pattern.compile("(0x[^\\s]+)\\s+\\d+\\s+(\\d+)\\s+[^\\s]+\\s+(.*)\\n").matcher(output);
            while (matcher.find()) {
                if (matcher.group(2).equals("" + pid)) {
                    return new Window(matcher.group(1));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LinuxWindowManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return null;
    }

    @Override
    public void sendClose(Window w) {
        try {
            this.wmctrl(new String[]{
                        "-i", "-c", w.getId()
                    });
        } catch (IOException ex) {
            Logger.getLogger(LinuxWindowManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isWindow(Window w) {
        try {
            String output = wmctrl(new String[]{
                        "-l"
                    });
            Matcher matcher = Pattern.compile("(0x[^\\s]+)\\s+(.*)\\n").matcher(output);
            while (matcher.find()) {
                if (matcher.group(1).equals(w.getId())) {
                    return true;
                }
            }
            return false;
        } catch (IOException ex) {
            Logger.getLogger(LinuxWindowManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public void setAbove(Window w, boolean above) {
        try {
            this.wmctrl(new String[]{
                        "-i", "-r", w.getId(), "-b", above ? "add,above" : "remove,above"
                    });
        } catch (IOException ex) {
            Logger.getLogger(LinuxWindowManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
