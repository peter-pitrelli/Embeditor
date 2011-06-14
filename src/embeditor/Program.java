/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.Serializable;

/**
 *
 * @author thommy
 */
public class Program implements Serializable{
    protected String name;
    protected String command;
    protected String windowstring;
    protected Point preOffset;
    protected Point postOffset;
    protected String fileendig;
    protected File template;
    
    /**
     * Returns the outer rectangle (the window bounds)
     * so that the editable size corresponds to the
     * inner rectangle
     * 
     * @param innerRectangle
     * @return 
     */
    public Rectangle getOuterRectangle(Rectangle innerRectangle)
    {
        return new Rectangle(innerRectangle.x-preOffset.x, innerRectangle.y-preOffset.y,
                innerRectangle.width+preOffset.x+postOffset.x, innerRectangle.height+preOffset.y+postOffset.y);
    }
    
    public Rectangle getInnerRectangle(Rectangle outerRectangle)
    {
     return new Rectangle(outerRectangle.x+preOffset.x, outerRectangle.y+preOffset.y,
                outerRectangle.width-preOffset.x-postOffset.x, outerRectangle.height-preOffset.y-postOffset.y);
      
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
