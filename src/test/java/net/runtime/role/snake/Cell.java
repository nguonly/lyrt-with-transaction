package net.runtime.role.snake;

/**
 * Created by nguonly on 10/19/15.
 */
public class Cell {
    final static int CELL_TYPE_EMPTY = 0,
            CELL_TYPE_FOOD = 10,
            CELL_TYPE_SNAKE_NODE = 20,
            CELL_TYPE_OBSTACLE = 30;
    int row, col;
    int type;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow(){
        return row;
    }

    public int getCol(){
        return col;
    }

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public String toString(){
        return "Cell[" + row + "][" + col + "] = " + type;
    }
}
