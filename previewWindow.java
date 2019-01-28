import javax.swing.*;
import java.awt.*;

//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class previewWindow extends JFrame {

    private ImageObj morphImg;

    private float[][][] moveX;
    private float[][][] moveY;

    private int count = 0, frames;

    private gridPoint[][] startGP;
    private gridPoint[][] endGP;
    private gridPoint[][] morphGP;

    private Container c;

    // Timer to call changePoints every second
    private Timer timer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (count != frames + 1) { // move points into position
                changePoints();
                count++;
            }
            else { // end timer when finished
                timer.stop();
            }
        }
    });

    public previewWindow(ImageObj startImg, ImageObj endImg, int speed, int frames) {
        // initialize the amount of time to morph
        this.frames = frames;
        timer.setDelay(speed);

        // Get grid points to start and end at
        startGP = startImg.getGridPoints();
        endGP = endImg.getGridPoints();

        // Create a new ImageObj to transform
        morphImg = new ImageObj(startImg.getImage());
        morphImg.wSpacing = startImg.wSpacing;
        morphImg.hSpacing = startImg.hSpacing;

        // Set initial grid points to be the same as the Source Image
        morphImg.setGridPoints(startGP);
        morphGP = morphImg.getGridPoints();

        // arrays to hold the distance each point needs to travel in X & Y directions
        moveX = new float[JMorph.controlPoints+1][JMorph.controlPoints+1][this.frames +1];
        moveY = new float[JMorph.controlPoints+1][JMorph.controlPoints+1][this.frames +1];

        // fill in travel distances arrays
        for (int i = 1; i < JMorph.controlPoints + 1; i++) {
            for (int j = 1; j < JMorph.controlPoints + 1; j++) {

                for (int k = 0; k < this.frames +1; k++) {
                    float startX = startGP[i][j].getX();
                    float endX = endGP[i][j].getX();

                    moveX[i][j][k] =  startX + ((endX - startX) / this.frames) * k;

                    float startY = startGP[i][j].getY();
                    float endY = endGP[i][j].getY();

                    moveY[i][j][k] = startY + ((endY - startY) / this.frames) * k;
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
        timer.start();
    }

    // function to actually move the grid points
    public void changePoints() {
        for (int i = 1; i < JMorph.controlPoints+1; i++) { // go through rows
            for (int j = 1; j < JMorph.controlPoints+1; j++) { // go through columns
                morphGP[i][j].setX(moveX[i][j][count]);
                morphGP[i][j].setY(moveY[i][j][count]);
            }
        }
        morphImg.repaint();
    }
}
