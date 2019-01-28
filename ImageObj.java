/*****************************************************************
 This is a helper object, which could reside in its own file, that
 extends JLabel so that it can hold a BufferedImage
 I've added the ability to apply image processing operators to the
 image and display the result
 ***************************************************************************/

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class ImageObj extends JLabel {

    private Graphics2D big;

    // instance variable to hold the buffered image
    private BufferedImage bim=null;
    private BufferedImage origBim=null;
    private BufferedImage filteredbim=null;
    private Triangle[][][] morphTriangles;
    public Boolean isMorphing = false;
    static int numPoints = JMorph.controlPoints;
    private int imgSize = 450;

    gridPoint[][] gridPoints = new gridPoint[numPoints+2][numPoints+2]; // Array of grid points
    private gridPoint GP; //Current grid point
    float wSpacing = (float) imgSize / (numPoints + 1);
    float hSpacing = (float) imgSize / (numPoints + 1);

    private boolean initialized = false; // Boolean to show if grid was initialized

    //  tell the paint component method what to draw
    private boolean showFiltered=false;

    private Color color;

    // Default constructor
    public ImageObj() { }

    // This constructor stores a buffered image passed in as a parameter
    public ImageObj(BufferedImage img) {
        origBim = img;
        // Resize Image
        BufferedImage resized = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);

        // Set Scale ratios
        double scaleWidth = (double) imgSize / (double) img.getWidth();
        double scaleHeight = (double) imgSize / (double) img.getHeight();

        // Scale Image
        Graphics2D g2d = resized.createGraphics();
        g2d.scale(scaleWidth, scaleHeight);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        bim = resized;
        filteredbim = new BufferedImage
                (bim.getWidth(), bim.getHeight(), BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(bim.getWidth(), bim.getHeight()));
        initializeGrid();
        this.repaint();
    }

    // This mutator changes the image by resetting what is stored
    // The input parameter img is the new image;  it gets stored as an
    //      instance variable
    public void setImage(BufferedImage img) {
        if (img == null) {
            return;
        }

        origBim = img;

        // Resize Image
        BufferedImage resized = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);

        // Set Scale ratios
        double scaleWidth = (double) imgSize / (double) img.getWidth();
        double scaleHeight = (double) imgSize / (double) img.getHeight();

        // Scale Image
        Graphics2D g2d = resized.createGraphics();
        g2d.scale(scaleWidth, scaleHeight);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        bim = resized;
        filteredbim = new BufferedImage
                (bim.getWidth(), bim.getHeight(), BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(bim.getWidth(), bim.getHeight()));
        showFiltered = false;
        this.repaint();
    }

    // accessor to get a handle to the buffered image object stored here
    public BufferedImage getImage() {
        return bim;
    }

    //  show current image by a scheduled call to paint()
    public void showImage() {
        if (bim == null) return;
        showFiltered=false;
        this.repaint();
    }

    public void updateResolution(int controlPoints) {
        numPoints = JMorph.controlPoints;
        gridPoints = new gridPoint[controlPoints+2][controlPoints+2];

        wSpacing = (float) imgSize / (numPoints+1);
        hSpacing = (float) imgSize / (numPoints+1);

        initializeGrid();
        repaint();
    }

    // Function to initialize all grid points
    public void initializeGrid() {
        float yPos = 0;
        // Initialize numPoints x numPoints gridPoints
        for (int i = 0; i < numPoints+2; i++) {
            float xPos = 0;
            for (int j = 0; j < numPoints+2; j++) {
                GP = new gridPoint(xPos, yPos); // Create new grid point
                this.gridPoints[i][j] = GP; // Add grid point to array
                GP.reset();
                xPos += wSpacing; // Increment X spacing
                //assignNeighbors(i, j); // assign gridPoint's neighbors
                gridPoints[i][j] = GP; // Add grid point to array

            }
            yPos += hSpacing; // Increment Y spacing
        }
    }

    //  get a graphics context and show either filtered image or
    //  regular image
    public void paintComponent(Graphics g) {
        big = (Graphics2D) g;
        if (showFiltered)
            big.drawImage(filteredbim, 0, 0, this);
        else
            big.drawImage(bim, 0, 0, this);

        // If not initialized, create grid points
        if (!initialized) {
            initializeGrid(); // create grid points and assign neighbors
            initialized = true;
        }
        assignNeighbors(); // assign each gridPoints neighbors
        drawGridPoints(); // draw each gridPoint
        drawLines(); // draw lines connecting gridPoints
        createTriangles();
    }

    // used to assign each gridPoints neighbors
    public void assignNeighbors() {
        for (int i = 0; i < numPoints + 2; i++) {
            for (int j = 0; j < numPoints + 2; j++) {
                GP = gridPoints[i][j];
                if (i == 0 & j == 0) { // top left corner
                    GP.N = null;
                    GP.S = gridPoints[i + 1][j];
                    GP.E = gridPoints[i][j + 1];
                    GP.W = null;
                    GP.SE = gridPoints[i + 1][j + 1];
                    GP.NW = null;
                } else if (i == 0 & j == ((numPoints + 2) - 1)) { // top right corner
                    GP.N = null;
                    GP.S = gridPoints[i + 1][j];
                    GP.E = null;
                    GP.W = gridPoints[i][j - 1];
                    GP.SE = null;
                    GP.NW = null;
                } else if (i == 0) { // top row
                    GP.N = null;
                    GP.S = gridPoints[i + 1][j];
                    GP.E = gridPoints[i][j + 1];
                    GP.W = gridPoints[i][j - 1];
                    GP.SE = gridPoints[i + 1][j + 1];
                    GP.NW = null;
                } else if (i == numPoints + 2 - 1 & j == 0) { // bottom left
                    GP.N = gridPoints[i - 1][j];
                    GP.S = null;
                    GP.E = gridPoints[i][j + 1];
                    GP.W = null;
                    GP.SE = null;
                    GP.NW = null;
                } else if (i == numPoints + 2 - 1 & j == numPoints + 2 - 1) { // bottom right
                    GP.N = gridPoints[i - 1][j];
                    GP.S = null;
                    GP.E = null;
                    GP.W = gridPoints[i][j - 1];
                    GP.SE = null;
                    GP.NW = gridPoints[i - 1][j - 1];
                } else if (i == numPoints + 2 - 1) { // bottom row
                    GP.N = gridPoints[i - 1][j];
                    GP.S = null;
                    GP.E = gridPoints[i][j + 1];
                    GP.W = gridPoints[i][j - 1];
                    GP.SE = null;
                    GP.NW = gridPoints[i - 1][j - 1];
                } else if (j == 0) { // left wall, not top or bottom
                    GP.N = gridPoints[i - 1][j];
                    GP.S = gridPoints[i + 1][j];
                    GP.E = gridPoints[i][j + 1];
                    GP.W = null;
                    GP.SE = gridPoints[i + 1][j + 1];
                    GP.NW = null;
                } else if (j == numPoints + 2 - 1) { // right wall, not top or bottom
                    GP.N = gridPoints[i - 1][j];
                    GP.S = gridPoints[i + 1][j];
                    GP.E = null;
                    GP.W = gridPoints[i][j - 1];
                    GP.SE = null;
                    GP.NW = gridPoints[i - 1][j - 1];
                } else { // middle
                    GP.N = gridPoints[i - 1][j];
                    GP.S = gridPoints[i + 1][j];
                    GP.E = gridPoints[i][j + 1];
                    GP.W = gridPoints[i][j - 1];
                    GP.SE = gridPoints[i + 1][j + 1];
                    GP.NW = gridPoints[i - 1][j - 1];
                }
            }
        }

    }


    // Draw grid points on image
    public void drawGridPoints() {
        for (int r = 0; r < numPoints+2; r++) {
            for (int c = 0; c < numPoints+2; c++) {
                if (!gridPoints[r][c].isChosen()) {
                    gridPoints[r][c].drawPoint(big, color);
                }
                else { // the grid point being clicked on is red
                    big.setColor(Color.RED);
                    big.fillOval(gridPoints[r][c].getX() - 5, gridPoints[r][c].getY() - 5, 10, 10);
                }
            }
        }
    }

    // Reset grid on Image
    public void reset() {
        initialized = false;
        repaint();
    }

    // draws lines from gridPoints to Neighbors
    public void drawLines() {
        // draw lines to neighbors
        for (int i = 0; i < numPoints+2; i++) {
            for (int j = 0; j < numPoints+2; j++) {

                big.setStroke(new BasicStroke(1));
                big.setColor(color);

                if(isMorphing) {
                    Color clear = new Color(1,0,0,0f);
                    big.setColor(clear);
                }

                if (gridPoints[i][j].N != null) {
                    int startX = gridPoints[i][j].getX();
                    int startY = gridPoints[i][j].getY();
                    int finalX = gridPoints[i][j].N.getX();
                    int finalY = gridPoints[i][j].N.getY();
                    big.drawLine(startX, startY, finalX, finalY);
                }
                if (gridPoints[i][j].S != null) {
                    int startX = gridPoints[i][j].getX();
                    int startY = gridPoints[i][j].getY();
                    int finalX = gridPoints[i][j].S.getX();
                    int finalY = gridPoints[i][j].S.getY();
                    big.drawLine(startX, startY, finalX, finalY);
                }
                if (gridPoints[i][j].E != null) {
                    int startX = gridPoints[i][j].getX();
                    int startY = gridPoints[i][j].getY();
                    int finalX = gridPoints[i][j].E.getX();
                    int finalY = gridPoints[i][j].E.getY();
                    big.drawLine(startX, startY, finalX, finalY);
                }
                if (gridPoints[i][j].W != null) {
                    int startX = gridPoints[i][j].getX();
                    int startY = gridPoints[i][j].getY();
                    int finalX = gridPoints[i][j].W.getX();
                    int finalY = gridPoints[i][j].W.getY();
                    big.drawLine(startX, startY, finalX, finalY);
                }
                if (gridPoints[i][j].SE != null) {
                    int startX = gridPoints[i][j].getX();
                    int startY = gridPoints[i][j].getY();
                    int finalX = gridPoints[i][j].SE.getX();
                    int finalY = gridPoints[i][j].SE.getY();
                    big.drawLine(startX, startY, finalX, finalY);
                }
                if (gridPoints[i][j].NW != null) {
                    int startX = gridPoints[i][j].getX();
                    int startY = gridPoints[i][j].getY();
                    int finalX = gridPoints[i][j].NW.getX();
                    int finalY = gridPoints[i][j].NW.getY();
                    big.drawLine(startX, startY, finalX, finalY);
                }
            }
        }
    }

    // Function to return the current array of grid points
    public gridPoint[][] getGridPoints() {
        return gridPoints;
    }
    // Function to set current array of grid points
    public void setGridPoints(gridPoint[][] GP) {
        for (int i = 0; i < GP.length; i++) {
            for (int j = 0; j < GP[i].length; j++) {
                gridPoints[i][j] = GP[i][j];
                gridPoints[i][j].setX(GP[i][j].X());
                gridPoints[i][j].setY(GP[i][j].Y());
            }
        }
        initialized = true;
    }

    public void setColor(Color drawColor) {
        color = drawColor;
        repaint();
    }

    // Function to increase the image's intensity
    public void updateBrightness(float newBrightness){
        RescaleOp op = new RescaleOp(newBrightness, 0, null);
        bim = op.filter(origBim, null);
        this.repaint();
    }

    public void createTriangles() {
        morphTriangles = new Triangle[numPoints+2][numPoints+2][2];
        for (int i = 0; i < numPoints+1; i++){
            for (int j = 0; j < numPoints+1; j++){
                morphTriangles[i][j][0] = new Triangle(gridPoints[i][j].getX(), gridPoints[i][j].getY(),gridPoints[i+1][j].getX(), gridPoints[i+1][j].getY(),gridPoints[i+1][j+1].getX(),gridPoints[i+1][j+1].getY());
                morphTriangles[i][j][1] = new Triangle(gridPoints[i][j].getX(), gridPoints[i][j].getY(),gridPoints[i][j+1].getX(), gridPoints[i][j+1].getY(),gridPoints[i+1][j+1].getX(),gridPoints[i+1][j+1].getY());
            }
        }
    }

    public Triangle[][][] getTriangles(){
        return morphTriangles;
    }
}
