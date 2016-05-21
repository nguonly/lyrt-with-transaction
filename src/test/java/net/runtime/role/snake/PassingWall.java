package net.runtime.role.snake;


import net.runtime.role.registry.RegistryManager;

/**
 * Created by nguonly on 11/27/15.
 */
public class PassingWall {
    public Cell getNextCell(Cell currentPosition) {
        //Router router = (Router)getRootPlayer();
        Router router = (Router) RegistryManager.getInstance().getRootPlayer(null, this);
        Board board = router.getBoard();

        int row = currentPosition.getRow();
        int col = currentPosition.getCol();

        if (router.getDirection() == Router.DIRECTION_RIGHT) {
            col++;
            if(col==board.getColCount()-1) col=1;
        } else if (router.getDirection() == Router.DIRECTION_LEFT) {
            col--;
            if(col == 0) col = board.getColCount() - 2;
        } else if (router.getDirection() == Router.DIRECTION_UP) {
            row--;
            if(row == 0) row = board.getRowCount() - 2;
        } else if (router.getDirection() == Router.DIRECTION_DOWN) {
            row++;
            if(row == board.getRowCount()-1) row = 1;
        }

        return  router.getBoard().getCells()[row][col];
    }
}
