package net.runtime.role.snake;


import net.runtime.role.registry.RegistryManager;

/**
 * Created by nguonly on 10/19/15.
 */
public class Router {
    //public static final int DIRECTION_NONE = 0, DIRECTION_RIGHT = 1, DIRECTION_LEFT = -1, DIRECTION_UP = 2, DIRECTION_DOWN = -2;
    public static final int DIRECTION_NONE = -1, DIRECTION_RIGHT = 0, DIRECTION_LEFT = 1, DIRECTION_UP = 2, DIRECTION_DOWN = 3;
    private Snake snake;
    private Board board;
    private int direction;
    boolean gameOver;

    public Router(Snake snake, Board board) {
        this.snake = snake;
        this.board = board;

        persistSnakeToBoard();
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public void update() {
        if (!gameOver) {
            if (direction != DIRECTION_NONE) {

                //Cell nextCell = getNextCell(snake.head);
                Cell nextCell = RegistryManager.getInstance().invokeRole(null, this, "getNextCell", Cell.class,
                        new Class[]{Cell.class}, new Object[]{snake.getHead()});

                if (checkCrash(nextCell)) {
                    setDirection(DIRECTION_NONE);
                    gameOver = true;
                } else {
                    snake.move(nextCell);
                    if (nextCell.type == Cell.CELL_TYPE_FOOD) {
                        snake.grow();
                        board.generateFood();

                    }
                }
            }
            persistSnakeToBoard();
        }
    }

    public boolean checkCrash(Cell nextCell){
        return (nextCell.getType() != Cell.CELL_TYPE_EMPTY && nextCell.getType() != Cell.CELL_TYPE_FOOD);
    }

    public Cell getNextCell(Cell currentPosition) {
        int row = currentPosition.getRow();
        int col = currentPosition.getCol();

        if (direction == Router.DIRECTION_RIGHT) {
            col++;
        } else if (direction == Router.DIRECTION_LEFT) {
            col--;
        } else if (direction == Router.DIRECTION_UP) {
            row--;
        } else if (direction == Router.DIRECTION_DOWN) {
            row++;
        }

        return board.getCells()[row][col];
    }

    public void persistSnakeToBoard(){
        for(Cell cell: snake.snakePartList){
            board.cells[cell.row][cell.col].type = Cell.CELL_TYPE_SNAKE_NODE;
        }
    }

    public void reset(){
        board.reset();
        snake.reset();
        gameOver = false;
        setDirection(Router.DIRECTION_NONE);
        board.generateFood();
    }

    public Board getBoard(){
        return this.board;
    }
}
