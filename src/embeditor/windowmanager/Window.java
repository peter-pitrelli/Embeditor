/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor.windowmanager;

/**
 *
 * @author thommy
 */
public class Window
{

  private String id;

  protected Window(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }
}
