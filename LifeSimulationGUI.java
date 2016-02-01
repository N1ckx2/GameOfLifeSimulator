/**
 * Nicholas Vadivelu
 * 23 December 2015
 * ICS4U1
 * Conway's Game of Life
 */
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;  // Needed for ActionListener
import java.io.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener; //need these two for listeners
import javax.swing.filechooser.FileNameExtensionFilter; //need this for getting appropriate files


public class LifeSimulationGUI extends JFrame //main gui class
{
    private Colony colony = new Colony (0.5, 125, 125); //this creates a colony with 50% density, that is 125 x 125
    private Timer t; //timer to perform actions
    private boolean populate = true, drawRect = false, reverse = false;
    //populate determines if the mouse selection populates or eradicates; drawRect is for mouse selection; reverse says if the program is going backward or forward
    private int startX, startY, endX, endY; //mouse coordinates for the rectangle
    private static LifeSimulationGUI window; //the JFrame that contains all the components
    private JFileChooser chooser; //allows you to save and load files
    private JButton rewindBtn, simulateBtn, stepBtn, saveBtn, loadBtn;
    //rewindBtn allows you to reverse; simulateBtn allows you to simulate or stop; saveBtn saves files, loadBtn loads files
    private Movement moveColony; //allows the program to advance or reverse colony
    private int delay; //the delay between each movement
    private JSpinner speed; //allows user to set the delay
    private JComboBox<String> popOrErad, saveGen;
    //popOrErad allows user to choose populate or eradicate ; saveGen allows you to choose which generation to save
    //======================================================== constructor
    public LifeSimulationGUI ()
    {
        //Greet User
        JOptionPane.showMessageDialog(this, "<html><p>Welcome to Nicholas Vadivelu's Conway's Game of Life Simulator!!<p>" + "" +
                "<p>The colour of the cell determines its age. Blue means one</p>" +
                "<p>generation old, green means two, then yellow, orange, red,</p>" +
                "<p>and finally black means over 5 generations old.</p>" +
                "<p>You may populate or eradicate life forms by selecting an</p>" +
                "<p>option from the combobox then dragging over an area using your mouse.</p>" +
                "<p>Going in reverse has a limit, due to memory limitations.</p>" +
                "<p>Hover over each element for more information</p><p></html>");

        //Initialize Components and Variables
        BtnListener btnListener = new BtnListener (); // listener for all buttons
        moveColony = new Movement ();
        JToolBar toolbar = new JToolBar(); //toolBar to hold all components
        popOrErad = new JComboBox<>();
        saveGen = new JComboBox<> ();
        chooser = new JFileChooser("./");
        speed = new JSpinner(new SpinnerNumberModel(201, 1, 9999, 10)); //to select delay between each movement
        delay = (int) speed.getValue();
        simulateBtn = new JButton ("Simulate");
        saveBtn = new JButton ("Save");
        loadBtn = new JButton ("Load");
        rewindBtn = new JButton("Reverse");
        stepBtn = new JButton("Step");

        //Add Items to Combo Boxes
        popOrErad.addItem("Populate");
        popOrErad.addItem("Eradicate");
        saveGen.addItem("<choose generation>");
        saveGen.addItem("Current Colony");
        saveGen.addItem("Initial Colony");

        //Add Listeners to Components
        popOrErad.addItemListener(new ItemListener()
        { public void itemStateChanged(ItemEvent e) { populate = e.getItem().equals("Populate");}}); //checks if the mouse should populate or eradicate
        simulateBtn.addActionListener (btnListener);
        loadBtn.addActionListener (btnListener);
        saveBtn.addActionListener (btnListener);

        //add tool tips to components
        simulateBtn.setToolTipText("Press this button to start the simulation. Press again to stop.");
        rewindBtn.setToolTipText("Press this button to reverse the simulation. Press again to play forward.");
        stepBtn.setToolTipText("Press this button to advance the program one generation per click.");
        speed.setToolTipText("Use this Spinner to set the delay (ms). You may type in your own value");
        saveGen.setToolTipText("Use this ComboBox to select which generation to save.");
        saveBtn.setToolTipText("Press this button to save the selected generation as a .txt file.");
        loadBtn.setToolTipText("Press this button to load a pre-made colony from a .txt file.");
        popOrErad.setToolTipText("Use this ComboBox to modify whether selecting an area with the mouse populates or eradicates.");

        // Set up Content Panes
        JPanel content = new JPanel ();        // Create a content pane
        content.setLayout (new BorderLayout ()); // Use BorderLayout for panel
        DrawArea board = new DrawArea (625, 625); //where the colony will be displayed
        board.addMouseListener(new ClickListener());
        board.addMouseMotionListener(new DragListener()); //listens for drags and clicks

        // Add components to Toolbar
        toolbar.add(simulateBtn);
        toolbar.add(rewindBtn);
        toolbar.add(stepBtn);
        toolbar.addSeparator();
        toolbar.add(new JLabel("Delay: ")); //to inform user about the spinner
        toolbar.add(speed);
        toolbar.addSeparator();
        toolbar.add(saveGen);
        toolbar.add(saveBtn);
        toolbar.add(loadBtn);
        toolbar.addSeparator();
        toolbar.add(popOrErad);

        //add content to panel
        content.add (toolbar, "North");
        content.add (board, "South"); // Output area

        // Set this window's attributes.
        setContentPane (content);
        pack ();
        setTitle ("Life Simulation");
        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo (null);           // Center window.
    }


    class BtnListener implements ActionListener //so program responds to buttons
    {
        public void actionPerformed (ActionEvent e)
        {
            if (e.getActionCommand ().equals ("Simulate")) //starts the simulation
            { // ActionListener for timer
                t = new Timer (delay, moveColony); // set up Movement to run in delay intervals
                t.start (); // start simulation
                simulateBtn.setText("Stop"); //this is so the user can stop the simulation

                //The following components are active only when the program starts
                stepBtn.addActionListener(this);
                rewindBtn.addActionListener(this);
                speed.addChangeListener(new ChangeListener()
                { //to check what the delay should be
                    public void stateChanged(ChangeEvent e)
                    {
                        t.stop(); //stops timer
                        delay = (int) speed.getValue();
                        t = new Timer (delay, moveColony); // sets new interval
                        t.start (); //starts again
                    }
                });
            } else if (e.getActionCommand().equals("Stop"))
            {
                t.stop(); //stops the simulation
                simulateBtn.setText("Simulate"); //allows the user to start again
            } else if (e.getActionCommand ().equals ("Save"))
            {
                String choice = (String) saveGen.getSelectedItem(); //gets the user's selection for generation
                int value = chooser.showSaveDialog(window); //records what the user chooses
                if(value == JFileChooser.APPROVE_OPTION) //if the user chooses to save
                {
                    try
                    {
                        if (choice.equals(saveGen.getItemAt(0)) || choice.equals(saveGen.getItemAt(1))) //if untouched, or current generation is selected, proceed as normal
                            colony.save(chooser.getSelectedFile().getAbsolutePath());
                        else if (choice.equals(saveGen.getItemAt(2)))
                            colony.save(chooser.getSelectedFile().getAbsolutePath(), -1); //if the user wants the initial state, -1
                        else //otherwise, the user will have chosen a generation to save
                            colony.save(chooser.getSelectedFile().getAbsolutePath(), Integer.parseInt((String) saveGen.getSelectedItem()));
                    }
                    catch (IOException i) {JOptionPane.showMessageDialog(window, "File could not be saved!", "Save Error", 0); }
                }
            } else if (e.getActionCommand ().equals ("Load")) //if the user wants to load a file
            {
                chooser.setFileFilter(new FileNameExtensionFilter("*.txt", "txt", "text")); //only allows text files
                int value = chooser.showOpenDialog(window); //records the user's button choice
                if(value == JFileChooser.APPROVE_OPTION) //if they selected an item
                {
                    try { colony.load(chooser.getSelectedFile().getPath()); } //loads the item
                    catch (IOException i) { JOptionPane.showMessageDialog(window, "File could not be loaded!", "Load Error", 0);}
                }
            } else if (e.getActionCommand().equals("Reverse")) //if the user wants the program to reverse
            {
                reverse = true;
                rewindBtn.setText("Forward"); //allows user to make the program go forward again
            } else if (e.getActionCommand().equals("Forward"))
            {
                reverse = false;
                rewindBtn.setText("Reverse"); //allows the user to reverse again
            } else if (e.getActionCommand().equals("Step")) //takes one step at a time
            {
                if (moveColony.getCounter() > 0) {
                    t.stop(); //stops the timer
                    simulateBtn.setText("Simulate"); //the simulate text will allow the user to play the program
                    moveColony.actionPerformed(new ActionEvent(new Object(), 0, "")); //steps the program forward
                }
            }
            repaint ();            // refresh display of colony
        }
    }


    class DrawArea extends JPanel
    {
        public DrawArea (int width, int height)
        {
            this.setPreferredSize (new Dimension (width, height)); // size
        }

        public void paintComponent (Graphics g)
        {
            colony.show (g); // display current state of colony
            if (drawRect && (endX != startX || endY != startY))
            {//if a rectange should be drawn the mouse is not where it started
                g.setColor(new Color(0, 158, 200, 120));
                int x = (startX < endX ? startX : endX);
                int width = Math.abs(endX - startX);
                int y = (startY < endY ? startY : endY);
                int height = Math.abs(endY - startY);
                g.fillRect(x, y, width, height); //draw the appropriate rectangle
            }
        }
    }


    class Movement implements ActionListener
    {
        private int counter = 0, currentGen = 0; //to keep track of how many times this action was called

        public void actionPerformed (ActionEvent event)
        {
            if (reverse)
            {
                colony.reverse(counter); // go back to previous generation
                currentGen--;
            }
            else
            {
                colony.advance(); //advances colony
                currentGen++; //th
                counter = currentGen;
                saveGen.addItem(Integer.toString(currentGen)); //adds generation to the counter
                if (counter > colony.getMaxGen())
                    saveGen.removeItemAt(3); //removes earlier items that have not been stored
            }
            repaint (); // refresh screen
        }
        public int getCounter() {return counter;} // getter method for counter
    }

    class ClickListener extends MouseAdapter //if the mouse clicks on board
    {
        public void mousePressed(MouseEvent e)
        {
            startX = e.getX();
            startY = e.getY(); //gets the coordinates where the mouse started
        }
        public void mouseReleased(MouseEvent e)
        {
            drawRect = false;
            endX = (e.getX() >= 0 ? e.getX() : 0);
            endY = (e.getY() >= 0 ? e.getY() : 0);
            int x1 = (startX < endX ? startX : endX);
            int x2 = (startX < endX ? endX : startX);
            int y1 = (startY < endY ? startY : endY);
            int y2 = (startY < endY ? endY : startY); //assigns the appropriate starting and ending position
            if (populate)
                colony.populate(x1, y1, x2, y2);
            else
                colony.eradicate(x1, y1, x2, y2); //populates or eradicates
            startX = 0;
            startY = 0;
            endX = 0;
            endY = 0;
        }
    }

    class DragListener extends MouseAdapter  //as the mouse drags on board
    {
        public void mouseDragged (MouseEvent e)
        {
            drawRect = true;
            endX = e.getX();
            endY = e.getY(); //updates the mouse coordinates
            repaint();
        }
    }


    //======================================================== method main
    public static void main (String[] args)
    {
        window = new LifeSimulationGUI();
        window.setVisible (true);
    }
}