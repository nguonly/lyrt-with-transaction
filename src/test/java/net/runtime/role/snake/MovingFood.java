package net.runtime.role.snake;

import net.runtime.role.registry.RegistryManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by nguonly on 2/8/16.
 */
public class MovingFood implements ActionListener {
    final static int DELAY = 500;

    Timer foodTimer;

    public MovingFood(){
        foodTimer = new Timer(DELAY, this);
        foodTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        startMovingFood();
    }

    public void startMovingFood(){
        //get the player
        BoardPanel boardPanel = (BoardPanel) RegistryManager.getInstance().getRootPlayer(null, this);
        if(boardPanel == null) return;//prevent from Runtime exception when unbound occurs
        Board board = boardPanel.getBoard();

        while(true){
            //get next cell
            Cell foodCell = board.getFoodCell();
            Cell nextCell = getRandomCell(board, foodCell);
            if(nextCell.getType() == Cell.CELL_TYPE_EMPTY){
                //Reset old food cell
                board.setCellType(foodCell.getRow(), foodCell.getCol(), Cell.CELL_TYPE_EMPTY);

                //Set new food cell
                board.setFoodCell(nextCell.getRow(), nextCell.getCol());
                break;
            }
        }
        //Position the food on board panel
        boardPanel.positionFood();
    }

    private Cell getRandomCell(Board board, Cell currentPosition) {
        //next direction
        int direction = (int)(Math.random()*4);

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

        Cell[][] cells = board.getCells();
        return cells[row][col];
    }

}
