/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor.windowmanager;

import java.awt.Rectangle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author thommy
 */
public class LinuxWindowManagerTest
{
  
  public LinuxWindowManagerTest()
  {
  }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  @AfterClass
  public static void tearDownClass() throws Exception
  {
  }
  
  @Before
  public void setUp()
  {
  }
  
  @After
  public void tearDown()
  {
  }

  /**
   * Test of findWindow method, of class LinuxWindowManager.
   */
  @Test
  public void testFindWindow()
  {
    System.out.println("findWindow");
    LinuxWindowManager instance = new LinuxWindowManager();
    Window result = instance.findWindow("NetBeans");
    assertNotNull(result);
    System.out.println("NetBeansWindow is :"+result.getId());
  }
  
  @Test
  public void testGetWindowRect(){
    System.out.println("findWindow");
    LinuxWindowManager instance = new LinuxWindowManager();
    Window result = instance.findWindow("NetBeans");
    assertNotNull(result);
    assertNotNull(instance.getWindowRect(result));
    System.out.println("Dimensions: "+instance.getWindowRect(result));
  }

  /**
   * Test of moveWindow method, of class LinuxWindowManager.
   */
  @Test
  public void testMoveWindow() throws InterruptedException
  {
    System.out.println("moveWindow");
    LinuxWindowManager instance = new LinuxWindowManager();
    Window w = instance.findWindow("NetBeans");
    int x = 37;
    int y = 75;
    instance.moveWindow(w, x, y);
    Thread.sleep(1000);
    Rectangle r = instance.getWindowRect(w);
    assertEquals(x, r.x);
    assertEquals(y, r.y);
    x = 12;
    y = 27;
    instance.moveWindow(w, x, y);
    Thread.sleep(1000);
    r = instance.getWindowRect(w);
    assertEquals(x, r.x);
    assertEquals(y, r.y);
    x = 102;
    y = 207;
    instance.moveWindow(w, x, y);
    Thread.sleep(1000);
    r = instance.getWindowRect(w);
    assertEquals(x, r.x);
    assertEquals(y, r.y);
  }

  /**
   * Test of resizeWindow method, of class LinuxWindowManager.
   */
  @Test
  public void testResizeWindow()
  {
    System.out.println("resizeWindow");
    LinuxWindowManager instance = new LinuxWindowManager();
    Window w = instance.findWindow("NetBeans");
    int x = 1200;
    int y = 800;
    instance.resizeWindow(w, x, y);
    Rectangle r = instance.getWindowRect(w);
    assertEquals(x, r.width);
    assertEquals(y, r.height);
  }

}
