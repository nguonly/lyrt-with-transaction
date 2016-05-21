package net.runtime.role.snake;

import java.util.LinkedList;

/**
 * Created by nguonly on 10/19/15.
 */
public class Snake {
    LinkedList<Cell> snakePartList = new LinkedList<>();
    Cell head;
    Cell initPos;
    Board board;
    int foodEaten;

    public Snake(Board board, int row, int col) {
        this.board = board;
        initPos = new Cell(row, col);
        reset();
    }

    public Cell getHead(){
        return head;
    }

    public void grow() {
        snakePartList.add(head);
        foodEaten++;
    }

    public void move(Cell nextCell) {
        Cell tail = snakePartList.removeLast();

        tail.type = Cell.CELL_TYPE_EMPTY;

        head = nextCell;
        snakePartList.addFirst(head);
    }

    public void reset(){
        snakePartList.clear();

        head = board.cells[initPos.row][initPos.col];

        snakePartList.add(head);

        Cell body = board.cells[head.row][head.col-1];
        snakePartList.add(body);

        foodEaten = 0;
    }

    public int getFoodEaten(){
        return foodEaten;
    }
}
