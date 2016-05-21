package net.runtime.role.snake;

/**
 * Created by nguonly on 10/19/15.
 */
public class Board {
    final int ROW_COUNT, COL_COUNT;
    Cell[][] cells;

    Cell foodCell;

    public Board(int rowCount, int columnCount) {
        ROW_COUNT = rowCount;
        COL_COUNT = columnCount;

        reset();
    }

    public void generateFood() {
        while(true) {
            int row = (int) (Math.random() * ROW_COUNT);
            int column = (int) (Math.random() * COL_COUNT);

            //Prevent food from generating on snake body or obstacle
            if(cells[row][column].type == Cell.CELL_TYPE_EMPTY) {
                cells[row][column].type = Cell.CELL_TYPE_FOOD;
                foodCell = cells[row][column];
                break;
            }
        }
    }

    public void reset(){
        cells = new Cell[ROW_COUNT][COL_COUNT];
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COL_COUNT; column++) {
                cells[row][column] = new Cell(row, column);
                cells[row][column].type = Cell.CELL_TYPE_EMPTY;
                if(row==0 || row==ROW_COUNT-1 || column==0 || column==COL_COUNT-1)
                    cells[row][column].type = Cell.CELL_TYPE_OBSTACLE;
            }
        }
    }

    public Cell getFoodCell(){
        return foodCell;
    }

    public void setFoodCell(int row, int col){
        cells[row][col].setType(Cell.CELL_TYPE_FOOD);
        foodCell = cells[row][col];
    }

    public void setObstacle(int row, int col){
        cells[row][col].type = Cell.CELL_TYPE_OBSTACLE;
    }

    public void setCellType(int row, int col, int type){
        cells[row][col].type = type;
    }

    public Cell[][] getCells(){
        return cells;
    }

    public int getRowCount(){
        return ROW_COUNT;
    }

    public int getColCount(){
        return COL_COUNT;
    }
}
