package net.runtime.role.snake;

import net.runtime.role.registry.RegistryManager;

/**
 * Created by nguonly on 2/8/16.
 */
public class Obstacle {
    public void defineObstacle(){
        Board board = (Board) RegistryManager.getInstance().getRootPlayer(null, this);

        for(int x=0; x<5; x++){
            board.setObstacle(x+5, 15);
        }

        for(int y=0; y<4; y++){
            board.setObstacle(3, y+5);
        }

        board.setObstacle(20, 20);
        board.setObstacle(20, 21);

        board.setObstacle(2, 2);
    }

    public void clearAllObstacles(){
        Board board = (Board)RegistryManager.getInstance().getRootPlayer(null, this);
        Cell food = board.getFoodCell();
        for(int row=1;row<board.getRowCount()-1;row++){
            for(int col=1;col<board.getRowCount()-1;col++){
                board.setCellType(row, col, Cell.CELL_TYPE_EMPTY);
            }
        }

        //set food back
        board.setFoodCell(food.getRow(), food.getCol());
    }
}
