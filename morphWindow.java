import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class morphWindow extends JFrame {

    private ImageObj morphImg;

    private float[][][] moveX;
    private float[][][] moveY;

    private int count = 0, Frames;

    private gridPoint[][] startGP;
    private gridPoint[][] endGP;
    private gridPoint[][] morphGP;
    private BufferedImage startBim, endBim;
    private Container c;
    private Triangle[][][] startTris;
    private Triangle[][][] endTris;
    private Triangle[][][] currTris;
    public static Boolean export = false;

    // Timer to call changePoints every second
    private Timer timer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (count != Frames + 1) { // move points into position
                morph();
                count++;
                MorphTools.currentAlpha = (float)count/(float)(Frames);

            }
            else { // end timer when finished
                timer.stop();
            }
        }
    });

    public morphWindow(ImageObj startImg, ImageObj endImg, int speed, int frames) {
        // initialize the amount of time to morph
        this.Frames = frames;
        timer.setDelay(speed);
        count = 0;
        startBim = startImg.getImage();
        endBim = endImg.getImage();

        // Get grid points to start and end at
        startGP = startImg.getGridPoints();
        endGP = endImg.getGridPoints();

        // Get Triangles used
        startTris = startImg.getTriangles();
        endTris = endImg.getTriangles();
        currTris = new Triangle[JMorph.controlPoints+1][JMorph.controlPoints+1][2];

        // Create a new ImageObj to transform
        morphImg = new ImageObj(startImg.getImage());

        // Set initial grid points to be the same as the Source Image
        morphImg.setGridPoints(startGP);
        morphGP = morphImg.getGridPoints();

        // arrays to hold the distance each point needs to travel in X & Y directions
        moveX = new float[JMorph.controlPoints+1][JMorph.controlPoints+1][this.Frames +1];
        moveY = new float[JMorph.controlPoints+1][JMorph.controlPoints+1][this.Frames +1];

        // fill in travel distances arrays
        for (int i = 1; i < JMorph.controlPoints + 1; i++) {
            for (int j = 1; j < JMorph.controlPoints + 1; j++) {
                for (int k = 0; k < this.Frames +1; k++) {
                    float startX = startGP[i][j].getX();
                    float endX = endGP[i][j].getX();
                    Color clear = new Color(0,0,0,0f);
                    morphImg.setColor(clear); // make control points invisible
                    morphImg.isMorphing = true; // used so lines don't get drawn
                    morphImg.repaint();
                    moveX[i][j][k] =  startX + ((endX - startX) / this.Frames)*k;
                    float startY = startGP[i][j].getY();
                    float endY = endGP[i][j].getY();
                    moveY[i][j][k] = startY + ((endY - startY) / this.Frames)*k;
                }
            }
        }

        buildDisplay();
        this.pack();
        this.setVisible(true);
    }

    // Function to build display of new window
    public void buildDisplay () {
        c = this.getContentPane();
        c.add(morphImg);
        count = 0;
        MorphTools.currentAlpha = .1f;
        timer.start();
    }

    // function to actually move the grid points
    public void morph() {

        // Move Triangles
        for (int i = 1; i < JMorph.controlPoints+1; i++) { // go through rows
            for (int j = 1; j < JMorph.controlPoints+1; j++) { // go through columns
                morphGP[i][j].setX(moveX[i][j][count]);
                morphGP[i][j].setY(moveY[i][j][count]);
            }
        }

        morphImg.setGridPoints(morphGP); // Set gridPoints
        morphImg.createTriangles(); // Create Triangles at new gridPoints
        currTris = morphImg.getTriangles();

        // For each triangle, warp
        for (int i = 0; i < JMorph.controlPoints + 1; i++) {
            for (int j = 0; j < JMorph.controlPoints + 1; j++) {
                for (int k = 0; k < startTris[i][j].length; k++) {
                  MorphTools.warpTriangle(endBim, morphImg.getImage(), endTris[i][j][k], currTris[i][j][k], null, null);
                }
            }
        }
        if (export) {
            BufferedImage image = (BufferedImage)createImage(startBim.getWidth(), startBim.getHeight());
            paint(image.getGraphics());
            try{
                File f = new File("morphFrame" + count + ".jpeg");
                ImageIO.write(image, "jpeg", f);
            }
            catch (IOException i){ System.out.println("Export failed.."); }
        }
        morphImg.repaint();
    }
}
