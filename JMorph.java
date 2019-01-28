import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Hashtable;

import javax.imageio.*;


public class JMorph extends JFrame {

    // Instance variables
    private BufferedImage image, newImage;    // the image
    private ImageObj img1, img2, origimg1, origimg2;          // a component in which to display left image

    // Integers
    static int controlPoints = 10, speed = 500, frames = 25;
    private int imgSize = 450;

    private float trans1 = .5f;
    private float trans2 = .5f;
    private float red1= 0;
    private float red2 = 0;
    private float green1 = 0;
    private float green2 = 0;
    private float blue1 = 0;
    private float blue2 = 0;

    private gridPoint currGP, otherGP; // Current point being moved / not moved

    // Main Panels
    private JPanel img1Panel;
    private JPanel midPanel;
    private JPanel img2Panel;

    private JSlider intensity1Slider, intensity2Slider;
    private JSlider transparency1Slider, transparency2Slider;


    private JSlider resolutionSlider, framesSlider, speedSlider;

    private JComboBox color1Box, color2Box;

    // Constructor for the frame
    public JMorph() {
        super("Morpher");    // call JFrame constructor
        this.buildMenu();        // helper method to build menus
        this.buildComponents();        // helper method to set up components
        this.buildDisplay();        // Lay out the components on the
    }

    // Function to build Menu used to import images and exit program
    private void buildMenu() {

        final JFileChooser fc = new JFileChooser(".");
        JMenuBar bar = new JMenuBar();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setJMenuBar(bar);
        JMenu importMenu = new JMenu("Import");
        JMenuItem openImg1 = new JMenuItem("Import Left Image");
        JMenuItem openImg2 = new JMenuItem("Import Right Image");
        JMenuItem reset = new JMenuItem("Reset");
        JMenuItem exit = new JMenuItem("Exit");
        JMenuItem help = new JMenu("Help");

        openImg1.addActionListener(new ActionListener() { // load image1
            @Override
            public void actionPerformed(ActionEvent e) {
                openImage(img1, fc);
            }
        });

        openImg2.addActionListener(new ActionListener() { // load image2
            @Override
            public void actionPerformed(ActionEvent e) {
                openImage(img2, fc);
            }
        });

        // Button to reset images to originals
        reset.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        img1.setImage(origimg1.getImage());
                        img1.showImage();
                        red1 = 0;
                        green1 = 0;
                        blue1 = 0;
                        transparency1Slider.setValue(5);
                        intensity1Slider.setValue(5);
                        color1Box.setSelectedIndex(0);
                        img1.reset();

                        img2.setImage(origimg2.getImage());
                        img2.showImage();
                        red2 = 0;
                        green2 = 0;
                        blue2 = 0;
                        transparency2Slider.setValue(5);
                        intensity2Slider.setValue(5);
                        color2Box.setSelectedIndex(0);
                        img2.reset();

                        resolutionSlider.setValue(10);
                        framesSlider.setValue(26);
                        speedSlider.setValue(500);
                    }
                }
        );

        // Button to exit the program
        exit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }
                }
        );

        help.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // **** TO DO: *****
                // Set up Instructions
            }
        }));

        importMenu.add(openImg1);
        importMenu.add(openImg2);
        bar.add(importMenu);
        bar.add(reset);
        bar.add(exit);
        bar.add(help);
    }

    //  Allocate components (these are all instance vars of this frame object)
    //  and set up action listeners for each of them
    //  This is called once by the constructor
    private void buildComponents() {
        // Read in Default Images
        img1 = new ImageObj(readImage("pics/rsz_devito.jpg"));
        img1.setColor(new Color(red1, green1, blue1, trans1));
        img2 = new ImageObj(readImage("pics/rsz_koala.jpg"));
        img2.setColor(new Color(red2, green2, blue2, trans2));

        origimg1 = new ImageObj(readImage("pics/rsz_devito.jpg"));
        origimg2 = new ImageObj(readImage("pics/rsz_koala.jpg"));

        // initialize current grid point off the image
        currGP = new gridPoint(-1, -1);
        otherGP = new gridPoint(-1, -1);

        // Add Motion Listeners to Images
        // Determine if a Control Point is clicked on
        img1.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                findPointClicked(e, img1);
            }
            // No point is being clicked in
            public void mouseReleased(MouseEvent e) {
                reset();
            }
        });
        // Use rubber banding to drag control point
        img1.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                rubberBand(img1, e);
            }
        });
        // Determine if a Control Point is clicked on
        img2.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                findPointClicked(e, img2);
            }
            // No point is being clicked in
            public void mouseReleased(MouseEvent e) {
                reset();
            }
        });
        // Use rubber banding to drag control point
        img2.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                rubberBand(img2, e);
            }
        });

        // Allocate Panels
        img1Panel = new JPanel();
        midPanel = new JPanel(new GridLayout(7, 1));
        midPanel.setMaximumSize(new Dimension(300, imgSize + 200));
        midPanel.setMinimumSize(new Dimension(300, imgSize + 200));
        img2Panel = new JPanel();

        // Create Panels
        createimagePanel(1);
        createMidPanel();
        createimagePanel(2);
    }

    // reset grid point so it is no longer chosen
    public void reset() {
        otherGP.reset();
        currGP.reset();
    }

    // used to allow gridPoints to be drug around
    public void rubberBand(ImageObj img, MouseEvent e) {
        if (currGP.border().contains(e.getX(), e.getY())) {
            currGP.setX(e.getX());
            currGP.setY(e.getY());
            img.repaint();
        }
    }

    // Function to set current point being dragged
    public void findPointClicked(MouseEvent e, ImageObj img) {
        ImageObj currentImage, otherImage;
        if (img == img1) { // set img to current img being clicked
            currentImage = img1;
            otherImage = img2;
        }
        else {
            currentImage = img2;
            otherImage = img1;
        }

        // ***** Need to fix so that .gridPoint isn't be accessed
        for (int i = 0; i < currentImage.gridPoints.length; i++) {
            for (int j = 0; j < currentImage.gridPoints[i].length; j++) {
                if (currentImage.gridPoints[i][j].clickedinPoint(e.getX(), e.getY())) {
                    currGP = currentImage.gridPoints[i][j]; // set currGP to point being dragged
                    currGP.choosePoint(); // mark it as being dragged
                    otherGP = otherImage.gridPoints[i][j]; // set otherGP to corresponding point
                    otherGP.choosePoint(); // mark it as being dragged
                    img1.repaint();
                    img2.repaint();
                }
            }
        }

    }

    // This helper method adds all components to the content pane of the
    // JFrame object.  Specific layout of components is controlled here
    private void buildDisplay() {
        // Add panels and image data component to the JFrame
        Container c = this.getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.X_AXIS));
        c.setPreferredSize(new Dimension(imgSize * 2 + 300, imgSize + 200));
        c.add(img1Panel);
        c.add(midPanel);
        c.add(img2Panel);
    }

    // Function to create panels used to hold image and their controls
    public void createimagePanel(int n) {
        GridLayout withTitle = new GridLayout(2, 1);

        // Colors for drop box
        String[] colors = {"Black", "Green", "White", "Blue", "Pink"};
        JLabel colorLabel = new JLabel("Color: ", SwingConstants.RIGHT);

        // Table for intensity slider
        Hashtable intensityTable = new Hashtable();
        intensityTable.put(0, new JLabel("Dark"));
        intensityTable.put(5, new JLabel("Original"));
        intensityTable.put(10, new JLabel("Light"));

        if (n == 1) {

            // Create slider for image intensity
            JLabel intensity1Label = new JLabel("Start Image Intensity", SwingConstants.CENTER);
            intensity1Slider = new JSlider(JSlider.HORIZONTAL, 0,10, 5);
            intensity1Slider.setLabelTable(intensityTable);
            intensity1Slider.setPaintLabels(true);
            intensity1Slider.setMajorTickSpacing(2);
            intensity1Slider.setMinorTickSpacing(5);
            intensity1Slider.setPaintTicks(true);
            intensity1Slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    if (!source.getValueIsAdjusting()) {
                        img1.updateBrightness((source.getValue()/(float)5));
                    }
                }
            });

            // create panel for intensity slider
            JPanel intensity1Panel = new JPanel();
            intensity1Panel.setLayout(withTitle);
            intensity1Panel.add(intensity1Label);
            intensity1Panel.add(intensity1Slider);

            // Slider that changes the transparency of the control points
            JLabel transparency1Label = new JLabel("Grid Transparency", SwingConstants.CENTER);
            transparency1Slider = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
            transparency1Slider.setMinorTickSpacing(2);
            transparency1Slider.setPaintTicks(true);
            transparency1Slider.addChangeListener(
                    new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            JSlider source = (JSlider) e.getSource();
                            if (!source.getValueIsAdjusting()) {
                                int val = source.getValue();
                                trans1 = (float) val / 10;
                                img1.setColor(new Color(red1, green1, blue1, trans1));
                                img1.repaint();
                            }
                        }
                    }
            );

            // Create panel for transparency slider
            JPanel transparency1Panel = new JPanel(); // panel for transparency slider
            transparency1Panel.setLayout(withTitle);
            transparency1Panel.add(transparency1Label, BorderLayout.NORTH);
            transparency1Panel.add(transparency1Slider, BorderLayout.SOUTH);

            // Create drop down box for colors
            color1Box = new JComboBox(colors);
            color1Box.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int selection = color1Box.getSelectedIndex();
                    switch (selection)
                    {
                        case 0: // black
                            red1 = 0;
                            green1 = 0;
                            blue1 = 0;
                            break;
                        case 1: // green
                            red1 = 0;
                            green1 = 1;
                            blue1 = 0;
                            break;
                        case 2: // white
                            red1 = 1;
                            green1 = 1;
                            blue1 = 1;
                            break;
                        case 3: // blue
                            red1 = 0;
                            green1 = 0;
                            blue1 = 1;
                            break;
                        case 4: //pink
                            red1 = 1;
                            green1 = .411f;
                            blue1 = .705f;
                            break;
                        default: break;
                    }
                    img1.setColor(new Color(red1, green1, blue1, trans1));
                }
            });

            // Create panel for color drop down box
            JPanel color1Panel = new JPanel();
            color1Panel.setLayout(new GridLayout(1, 2));
            color1Panel.add(colorLabel);
            color1Panel.add(color1Box);

            // Create panel for image controls
            JPanel imgControls = new JPanel(new GridLayout(2, 1));
            imgControls.setPreferredSize(new Dimension(imgSize, 200));
            imgControls.add(intensity1Panel); // Add intensity to image controls

            // Create panel for grid point controls
            JPanel pointControls = new JPanel(new GridLayout(1, 2));
            pointControls.add(transparency1Panel);
            pointControls.add(color1Panel);
            imgControls.add(pointControls); // Add point controls to image controls

            // Add elements to Panel for image 1
            img1Panel.add(img1);
            img1Panel.add(imgControls);


        } else if (n == 2) {

            // Create slider for image intensity
            JLabel intensity2Label = new JLabel("End Image Intensity", SwingConstants.CENTER);
            intensity2Slider = new JSlider(JSlider.HORIZONTAL, 0,10, 5);
            intensity2Slider.setLabelTable(intensityTable);
            intensity2Slider.setPaintLabels(true);
            intensity2Slider.setMajorTickSpacing(2);
            intensity2Slider.setMinorTickSpacing(5);
            intensity2Slider.setPaintLabels(true);
            intensity2Slider.setPaintTicks(true);
            intensity2Slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    if (!source.getValueIsAdjusting()) {
                        img2.updateBrightness((source.getValue()/(float)5));
                    }
                }
            });

            // Create panel for intensity slider
            JPanel intensity2Panel = new JPanel();
            intensity2Panel.setLayout(withTitle);
            intensity2Panel.add(intensity2Label);
            intensity2Panel.add(intensity2Slider);

            // Slider that changes the transparency of the control points
            JLabel transparency2Label = new JLabel("Grid Transparency", SwingConstants.CENTER);
            transparency2Slider = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
            transparency2Slider.setMinorTickSpacing(2);
            transparency2Slider.setPaintTicks(true);
            transparency2Slider.setPaintLabels(true); // why don't they show up omg
            transparency2Slider.addChangeListener(
                    new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            JSlider source = (JSlider) e.getSource();
                            if (!source.getValueIsAdjusting()) {
                                int val = source.getValue();
                                trans2 = (float) val / 10;
                                img2.setColor(new Color(red2, green2, blue2, trans2));
                                img2.repaint();
                            }
                        }
                    }
            );

            // Create panel for transparency slider
            JPanel transparency2Panel = new JPanel(); // panel for transparency slider
            transparency2Panel.setLayout(withTitle);
            transparency2Panel.add(transparency2Label, BorderLayout.NORTH);
            transparency2Panel.add(transparency2Slider, BorderLayout.SOUTH);

            // Create drop down box for colors
            color2Box = new JComboBox(colors);
            color2Box.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int selection = color2Box.getSelectedIndex();
                    switch (selection)
                    {
                        case 0: // black
                            red2 = 0;
                            green2 = 0;
                            blue2 = 0;
                            break;
                        case 1: // green
                            red2 = 0;
                            green2 = 1;
                            blue2 = 0;
                            break;
                        case 2: // white
                            red2 = 1;
                            green2 = 1;
                            blue2 = 1;
                            break;
                        case 3: // blue
                            red2 = 0;
                            green2 = 0;
                            blue2 = 1;
                            break;
                        case 4: //pink
                            red2 = 1;
                            green2 = .411f;
                            blue2 = .705f;
                            break;
                        default:
                            break;
                    }
                    img2.setColor(new Color(red2, green2, blue2, trans2));
                }
            });

            // Create panel for color drop down box
            JPanel color2Panel = new JPanel();
            color2Panel.setLayout(new GridLayout(1, 2));
            color2Panel.add(colorLabel);
            color2Panel.add(color2Box);

            // Create panel for image controls
            JPanel imgControls = new JPanel(new GridLayout(2, 1));
            imgControls.setPreferredSize(new Dimension(imgSize, 200));
            imgControls.add(intensity2Panel); // Add intensity to image controls

            // Create panel for grid point controls
            JPanel pointControls = new JPanel(new GridLayout(1, 2));
            pointControls.add(transparency2Panel);
            pointControls.add(color2Panel);
            imgControls.add(pointControls); // Add point controls to image controls

            // Add elements to panel for image 1
            img2Panel.add(img2);
            img2Panel.add(imgControls);
        }
    }


    public void createMidPanel() {
        GridLayout withTitle = new GridLayout(2,1);

        // Slider that adjusts amount of control points
        JLabel resolutionLabel = new JLabel("Resolution", SwingConstants.CENTER);
        resolutionSlider = new JSlider(JSlider.HORIZONTAL, 1, 19, 10);
        Hashtable resTable = new Hashtable();
        resTable.put(1, new JLabel("2x2"));
        resTable.put(7, new JLabel("8x8"));
        resTable.put(13, new JLabel("14x14"));
        resTable.put(19, new JLabel("20x20"));
        resolutionSlider.setLabelTable(resTable);
        resolutionSlider.setPaintLabels(true);
        resolutionSlider.setMinorTickSpacing(3);
        resolutionSlider.setPaintTicks(true);
        resolutionSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                    controlPoints = source.getValue();
                    img1.updateResolution(controlPoints);
                    img2.updateResolution(controlPoints);
                }
            }
        });

        // Create panel for resolution slider
        JPanel resPanel = new JPanel(); // panel for resolution slider
        resPanel.setLayout(withTitle);
        resPanel.add(resolutionLabel, BorderLayout.NORTH);
        resPanel.add(resolutionSlider, BorderLayout.SOUTH);

        // Create button to swap images
        JButton swap = new JButton("Swap Images");
        swap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Make Images Swap

                ImageObj tmp = new ImageObj(origimg1.getImage()); // Temp img1
                float redTmp = red1;
                float greenTmp = green1;
                float blueTmp = blue1;
                int transTmp = transparency1Slider.getValue();
                int intenseTmp = intensity1Slider.getValue();
                int colorTmp = color1Box.getSelectedIndex();

                img1.setImage(origimg2.getImage());
                origimg1.setImage(origimg2.getImage());
                img1.showImage();
                red1 = red2;
                green1 = green2;
                blue1 = blue2;
                transparency1Slider.setValue(transparency2Slider.getValue());
                intensity1Slider.setValue(intensity2Slider.getValue());
                color1Box.setSelectedIndex(color2Box.getSelectedIndex());

                img2.setImage(tmp.getImage());
                origimg2.setImage(tmp.getImage());
                img2.showImage();
                red2 = redTmp;
                green2 = greenTmp;
                blue2 = blueTmp;
                transparency2Slider.setValue(transTmp);
                intensity2Slider.setValue(intenseTmp);
                color2Box.setSelectedIndex(colorTmp);

                img1.repaint();
                img2.repaint();
            }
        });

        // Slider that adjusts the number of Frames drawn
        JLabel framesLabel = new JLabel("Frames", SwingConstants.CENTER);
        framesSlider = new JSlider(JSlider.HORIZONTAL, 1, 51, 26);
        Hashtable framesTable = new Hashtable();
        framesTable.put(1, new JLabel("1"));
        framesTable.put(11, new JLabel("10"));
        framesTable.put(21, new JLabel("20"));
        framesTable.put(31, new JLabel("30"));
        framesTable.put(41, new JLabel("40"));
        framesTable.put(51, new JLabel("50"));
        framesSlider.setLabelTable(framesTable);
        framesSlider.setPaintLabels(true);
        framesSlider.setMajorTickSpacing(5);
        framesSlider.setPaintTicks(true);
        framesSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    frames = source.getValue();
                }
            }
        });

        // Create panel for frame slider
        JPanel framePanel = new JPanel(); // panel for frames per second slider
        framePanel.setLayout(withTitle);
        framePanel.add(framesLabel, BorderLayout.NORTH);
        framePanel.add(framesSlider, BorderLayout.SOUTH);

        // Slider that adjusts morph speed
        JLabel speedLabel = new JLabel("Morph Speed", SwingConstants.CENTER);
        speedSlider = new JSlider(JSlider.HORIZONTAL, 300,1000,650);
        Hashtable speedTable = new Hashtable(); // Labels for speed slider
        speedTable.put(300, new JLabel("Faster"));
        speedTable.put(634, new JLabel("Fast"));
        speedTable.put(867, new JLabel("Slow"));
        speedTable.put(1000, new JLabel("Slower") );
        speedSlider.setLabelTable( speedTable );
        speedSlider.setPaintLabels(true);
        speedSlider.setInverted(true);
        speedSlider.setMajorTickSpacing(111);
        speedSlider.setPaintTicks(true);
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    speed = source.getValue();
                }
            }
        });

        // Create panel for speed slider
        JPanel speedPanel = new JPanel(); // panel for speed slider
        speedPanel.setLayout(withTitle);
        speedPanel.add(speedLabel, BorderLayout.NORTH);
        speedPanel.add(speedSlider, BorderLayout.SOUTH);


        // Create checkbox option to export images
        JCheckBox export = new JCheckBox("Export Morph to Video");
        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                morphWindow.export = true;
            }
        });

        // Button to preview morph
        JButton previewButton = new JButton("Preview Morph");
        previewButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // Open new window for transform
                        gridPoint[][] temp1 = img1.gridPoints;
                        gridPoint[][] temp2 = img2.gridPoints;
                        new previewWindow(img1, img2, speed, frames);
                        img1.gridPoints = temp1;
                        img2.gridPoints = temp2;
                    }
                }
        );

        // Button to start morph
        JButton startButton = new JButton("Start Morph");
        startButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        gridPoint[][] temp1 = img1.gridPoints;
                        gridPoint[][] temp2 = img2.gridPoints;
                        new morphWindow(img1, img2, speed, frames);
                        img1.gridPoints = temp1;
                        img2.gridPoints = temp2;
                    }
                }
        );

        // Add elements to middle panel
        midPanel.add(resPanel);
        midPanel.add(swap);
        midPanel.add(framePanel);
        midPanel.add(speedPanel);
        midPanel.add(export);
        midPanel.add(previewButton);
        midPanel.add(startButton);
    }


    // This method reads an Image object from a file indicated by
    // the string provided as the parameter.  The image is converted
    // here to a BufferedImage object, and that new object is the returned
    // value of this method.
    // The media tracker in this method can throw an exception
    public BufferedImage readImage(String file) {
        Image image = Toolkit.getDefaultToolkit().getImage(file);
        MediaTracker tracker = new MediaTracker(new Component() {
        });
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
        }
        BufferedImage bim = new BufferedImage
                (image.getWidth(this), image.getHeight(this),
                        BufferedImage.TYPE_INT_RGB);
        Graphics2D big = bim.createGraphics();
        big.drawImage(image, 0, 0, this);
        return bim;
    }

    public void openImage(ImageObj currentImage, JFileChooser fc) {
        int returnVal = fc.showOpenDialog(JMorph.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                image = ImageIO.read(file);
            } catch (IOException e1) {
            }

            // Load Images into image frame
            currentImage.setImage(image);
            currentImage.showImage();
        }
    }

    // The main method allocates the "window" and makes it visible.
    // The window closing event is handled by an anonymous inner (adapter)
    // class
    // Command line arguments are ignored.
    public static void main(String[] argv) {
        JFrame frame  = new JMorph();
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
    }
}
