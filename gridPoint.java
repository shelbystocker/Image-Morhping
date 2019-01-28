import javax.swing.*;
import java.awt.*;

public class gridPoint extends JPanel {

    private float x,y;
    private Boolean isDragging = false;

    gridPoint N, S, E, W, SE, NW; // neighbors

    // Constructor to create initial grid point
    public gridPoint(float x1, float y1) {
        this.x = x1;
        this.y = y1;
    }

    // Functions to get X & Y as a float
    public float X() { return this.x; }
    public float Y() { return this.y; }

    // Functions to set X & Y
    public void setX(float newX) { x = newX; }
    public void setY(float newY) { y = newY; }

    // Functions to get X & Y as an int
    public int getX() { return (int)x; }
    public int getY() { return (int)y; }

    // When point is clicked on, isDragging is true
    public void choosePoint() {
        isDragging = true;
    }

    // When another point is chosen, this one isn't dragging
    public void reset() {
        isDragging = false;
    }

    // Return if the point is the point being dragged
    public boolean isChosen() {
        return isDragging;
    }

    // Draw the grid point
    public void drawPoint(Graphics2D img, Color drawColor) {
        if (this.N==null | this.S==null | this.E==null | this.W==null) {
        }
        else {
            img.setPaint(drawColor);
            img.fillOval((int)x - 5, (int)y - 5, 10, 10);
        }
    }

    // Function to determine if the point is clicked in
    public boolean clickedinPoint(int clickX, int clickY) {
        if (clickX <= x + 5 && clickX >= x - 5) {
            if (clickY <= y + 5 && clickY >= y- 5) {
                return true;
            }
        }
        return false;
    }

    // Create border to stop grid from overlapping
    public Polygon border() {
        if (N != null && E != null && SE != null && S != null && W != null && NW != null) {
            int[] xCords = {N.getX(), E.getX(), SE.getX(), S.getX(), W.getX(), NW.getX()};
            int[] yCords = {N.getY(), E.getY(), SE.getY(), S.getY(), W.getY(), NW.getY()};
            return new Polygon(xCords, yCords, xCords.length);
        }
            return null;
    }


}
