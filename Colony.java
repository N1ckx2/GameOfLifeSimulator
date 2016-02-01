import java.awt.*;
import java.io.*;

public class Colony
{
    private boolean grid[][][]; //grid that holds the colony and all of its generations
    private int age[][]; //indicates the age of each cell
    private boolean initial[][]; //stores the initial generation
    private int maxRow, maxCol, maxGen, curGen, actualGen;
    //maxRow and maxCol indicate the size of the grid; maxGen indicates the number of generations to be stored
    //curGen stores the current generation in the grid; actualGen stores the current generation out of the bounds of the grid
    final private Color[] COLOUR = {Color.BLACK, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED, Color.BLACK}; //stores the colours
    private boolean reverse; //if the program is in reverse or not
    final private int CELL_LENGTH = 5; //physical size of each cell on screen

    public Colony (double density, int rows, int cols) //constructor
    {
        //Initialize variables
        reverse = false;
        maxGen = 2048;
        curGen = 0;
        actualGen = 0;
        grid = new boolean [maxGen][rows][cols];
        initial = new boolean [rows][cols];
        age = new int[rows][cols];
        maxRow = grid[0].length;
        maxCol = grid[0][0].length;

        //fill in cells randomly
        for (int row = 0 ; row < maxRow ; row++)
        {
            for (int col = 0; col < maxCol; col++)
            {
                initial[row][col] = coinFlip(density); //stores initial state
                grid[curGen][row][col] = initial[row][col]; //copies onto grid
                age[row][col] = 0; //all cells start out age age 0
            }
        }
    }


    public void show (Graphics g) //shows each cell
    {
        for (int row = 0 ; row < maxRow ; row++)
            for (int col = 0 ; col < maxCol ; col++)
            {
                //changes the colour based on the age: blue is 1 cycle, green is 2, then yellow, orange, red, then black
                if (grid [curGen][row][col] && !reverse) //when not in reverse and if alive
                    g.setColor (COLOUR[age[row][col] < COLOUR.length ? age[row][col] : COLOUR.length-1]);
                else if (grid[curGen][row][col]) //if it is in reverse, shows only black
                    g.setColor(Color.BLACK);
                else //if the cell isn't alive
                    g.setColor (Color.white);
                g.fillRect (col * CELL_LENGTH + 2, row * CELL_LENGTH + 2, CELL_LENGTH, CELL_LENGTH); // draw life form
            }
        g.setColor(Color.BLACK); //text should be black
        g.drawString("Generation " + actualGen, 530, 620);
    }

    public boolean live (int row, int col) //checks if the cell here will live, die, or be created according to rules
    {
        int startRow = (row == 0 ? row : row - 1);
        int startCol = (col == 0 ? col : col - 1);
        int endRow = (row + 1 == maxRow ? row : row + 1);
        int endCol = (col + 1 == maxCol ? col : col + 1); //these four variables determines the starting and ending position
        int counter = 0;
        // also ensure that when the loop searches, it doesn't go out of the array bounds
        for (int r = startRow; r <= endRow ; r++)
            for (int c = startCol; c <= endCol; c++)
                if ((r != row || c != col) && grid[curGen][r][c])
                    counter++;

        if ((counter == 2 && grid[curGen][row][col]) || counter == 3) //if 3 surrounding, it will create life
            return true; //if two surrounding, it will maintain life
        else //if less than two, or greater than three, or if value is 2 but no life
            return false;
    }


    public void advance ()
    {
        reverse = false; //program is going normally
        boolean[][] temp = new boolean[maxRow][maxCol]; //creates a temporary arraty to store the new positions
        for (int r = 0 ; r < maxRow ; r++)
        {
            for (int c = 0; c < maxCol; c++)
            {
                temp[r][c] = live(r, c); //judges if alive or dead
                age[r][c] = (temp[r][c] ? age[r][c]+1 : 0); //checks if the cell has been alive before, and if so adds 1 to its age
            }
        }
        curGen = (curGen+1)%maxGen; //increments current gen, makes it overwrite old grids if it goes out of bounds
        actualGen ++; //increments the generation of the colony
        grid[curGen] = temp; //assigns grid to temp
    }

    public void reverse (int counter)
    { //if the program is going backward
        if (counter < maxGen && curGen == 0)
        { //to stop the reversal when the program reaches the starting point of the colony
            for (int r = 0 ; r < maxRow ; r++)
                for (int c = 0; c < maxCol; c++)
                    grid[curGen][r][c] = initial[r][c];
        }
        else if (curGen != counter%maxGen+1)
        { //as long as the reverse hasn't come back to the place where it started (Since finite array loops around)
            actualGen--;
            curGen--;
            if (curGen < 0) curGen += maxGen;
            reverse = true;
            for (int r = 0 ; r < maxRow ; r++)
                for (int c = 0; c < maxCol; c++)
                    age[r][c] = 0; //all ages are 0 because when going backward because will be black anyway
        }
    }

    public void eradicate(int startRow, int startCol, int endRow, int endCol)
    {
        startRow = (startRow/CELL_LENGTH > 125 ? 125 : startRow/CELL_LENGTH);
        endRow = (endRow/CELL_LENGTH > 125 ? 125 : endRow/CELL_LENGTH);
        startCol = (startCol/CELL_LENGTH > 125 ? 125 : startCol/CELL_LENGTH);
        endCol = (endCol/CELL_LENGTH > 125 ? 125 : endCol/CELL_LENGTH); //gets the correct starting and ending cell based on boundaries

        for (int r = startRow; r < endRow ; r++)
            for (int c = startCol; c < endCol; c++)
                if (grid[curGen][c][r])
                    grid[curGen][c][r] = coinFlip(0.15); //eradicates with 85% probability
        //the coordinates are flipped on purpose because of how the coordinate system works
    }

    public void populate(int startRow, int startCol, int endRow, int endCol)
    {
        startRow = (startRow/CELL_LENGTH > 125 ? 125 : startRow/CELL_LENGTH);
        endRow = (endRow/CELL_LENGTH > 125 ? 125 : endRow/CELL_LENGTH);
        startCol = (startCol/CELL_LENGTH > 125 ? 125 : startCol/CELL_LENGTH);
        endCol = (endCol/CELL_LENGTH > 125 ? 125 : endCol/CELL_LENGTH);//gets the correct starting and ending cell based on boundaries

        for (int r = startRow; r < endRow ; r++)
            for (int c = startCol; c < endCol; c++)
                grid[curGen][c][r] = coinFlip(0.85); //populates with 85% probability
        //the coordinates are flipped on purpose because of how the coordinate system works
    }

    public void save (String filename) throws IOException
    { //default method to save the current generation file
        if (filename == null) return; //if no file name is selected
        else if (filename.indexOf(".") == -1) filename = filename + ".txt"; //adds extension if none is present

        //initialize filewriter and printwriter
        FileWriter fw = new FileWriter (filename);
        PrintWriter fileout = new PrintWriter (fw);

        for (int r = 0 ; r < maxRow ; r++)
        {
            for (int c = 0; c < maxCol; c++)
                fileout.print(grid[curGen][r][c] ? "1" : "0"); // write the value to file
            fileout.println();
        }
        fileout.close (); // close file
    }

    public void save (String filename, int choice) throws IOException
    { //overloaded save method in case user wants to save a specific generation
        if (filename == null) return; //if no file name is selected
        else if (filename.indexOf(".") == -1) filename = filename + ".txt";//adds extension if none is present

        //initialize filewriter and printwriter
        FileWriter fw = new FileWriter (filename);
        PrintWriter fileout = new PrintWriter (fw);

        for (int r = 0 ; r < maxRow ; r++)
        {
            for (int c = 0; c < maxCol; c++)
                fileout.print((choice == -1 ? initial[r][c] : grid[choice%maxGen][r][c]) ? "1" : "0"); // write a value to file
            fileout.println();
        }
        fileout.close (); // close file
    }

    public void load(String filename) throws IOException
    { //load method to load a premade pattern

        //reset all the variables:
        for (int g = 0;  g < maxGen ; g++)
            for (int r = 0; r < maxRow; r++)
                for (int c = 0; c < maxCol; c++)
                {
                    initial[r][c] = false;
                    grid[g][r][c] = false;
                    age[r][c] = 0;
                }
        curGen = 0;
        actualGen = 0;

        //initialize the required items objects
        FileReader fr = new FileReader (filename);
        BufferedReader filein = new BufferedReader (fr);
        String line = "";

        //load file
        for (int r = 0 ; (line = filein.readLine ()) != null && r < maxRow; r++)
        {//as long as line isn't null and is within array bounds
            for (int c = 0 ; c < line.length() && c < maxCol; c++)  //whole line as long as it doesn't exceed array bounds
            {
                initial[r][c] = (line.charAt(c) == '1'); //checks for 1s
                grid[curGen][r][c] = initial[r][c]; //
            }
        }
        filein.close (); // close file
    }

    public boolean coinFlip(double d) { return Math.random() < d;} //method used for probability
    public int getMaxGen() {return maxGen;} //getter for maxGen
}
