package net.runtime.role.snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by nguonly on 10/20/15.
 */
public class BoardPanel extends JLayeredPane{
    final int DOT_SIZE = SnakeGame.DOT_SIZE;

    private SnakeGame game;
    private Board board;
    private Router router;
    private Snake snake;

    JPanel foodPanel = new JPanel();

    public BoardPanel(SnakeGame game){
        addKeyListener(new TAdapter());
        setBackground(Color.BLACK);

        setFocusable(true);

        this.game = game;
        this.snake = game.snake;
        this.board = game.board;
        this.router = game.router;

        setPreferredSize(new Dimension(board.ROW_COUNT*DOT_SIZE, board.COL_COUNT*DOT_SIZE));
        setOpaque(true); //force background to be painted

        add(foodPanel);
        foodPanel.setBackground(Color.RED);

    }

    public Board getBoard(){
        return this.board;
    }

    public void positionFood(){
        foodPanel.setBounds(board.foodCell.col * DOT_SIZE, board.foodCell.row*DOT_SIZE, DOT_SIZE, DOT_SIZE);
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        drawGrid(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g){
        if(router.gameOver){
            drawText(g, "Game Over");
        }else if(game.isPause()){
            //drawText(g, "Game Pause");
        }

        positionFood();

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawGrid(Graphics g){
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0, 0, getWidth(), getHeight());

        for(int x=0; x<board.ROW_COUNT; x++){
            for(int y=0; y<board.COL_COUNT; y++){
                if(board.cells[x][y].type == Cell.CELL_TYPE_OBSTACLE){
                    g.setColor(Color.GRAY);
                    g.fillRect(y*DOT_SIZE, x*DOT_SIZE, DOT_SIZE, DOT_SIZE);
                }
                else if (board.cells[x][y].type == Cell.CELL_TYPE_FOOD){
//                    g.setColor(Color.RED);
//                    g.fillRect(y*DOT_SIZE, x*DOT_SIZE, DOT_SIZE, DOT_SIZE);
                }
                else if(board.cells[x][y].type == Cell.CELL_TYPE_SNAKE_NODE){

                    if(x == snake.head.row && y==snake.head.col) {
                        g.setColor(Color.GREEN);
                        g.fillRect(y * DOT_SIZE, x * DOT_SIZE, DOT_SIZE, DOT_SIZE);
                    }else{
                        g.setColor(Color.ORANGE);
                        g.fillRect(y * DOT_SIZE + 1, x * DOT_SIZE + 1, DOT_SIZE - 2, DOT_SIZE - 2);
                    }
                }
                else {
                    g.setColor(Color.GRAY);
                    g.drawLine(x * DOT_SIZE, 0, x * DOT_SIZE, getHeight());
                    g.drawLine(0, y * DOT_SIZE, getWidth(), y * DOT_SIZE);
                }
            }
        }
    }

    private void drawText(Graphics g, String message) {
        FontMetrics metr = getFontMetrics(game.MEDIUM_FONT);

        g.setColor(Color.white);
        g.setFont(game.MEDIUM_FONT);
        g.drawString(message, (this.getWidth() - metr.stringWidth(message)) / 2, this.getHeight() / 2);
    }

    private class TAdapter extends KeyAdapter{
        public void keyPressed(KeyEvent e){
            int key = e.getKeyCode();

            if ((key == KeyEvent.VK_LEFT) && router.getDirection()!=Router.DIRECTION_RIGHT) {
                router.setDirection(Router.DIRECTION_LEFT);
            }

            if ((key == KeyEvent.VK_RIGHT) && router.getDirection()!=Router.DIRECTION_LEFT) {
                router.setDirection(Router.DIRECTION_RIGHT);
            }

            if ((key == KeyEvent.VK_UP && router.getDirection()!=Router.DIRECTION_DOWN) ) {
                router.setDirection(Router.DIRECTION_UP);
            }

            if ((key == KeyEvent.VK_DOWN && router.getDirection()!=Router.DIRECTION_UP) ) {
                router.setDirection(Router.DIRECTION_DOWN);
            }

            if(key == KeyEvent.VK_P){
                game.pause();
            }

            if(key == KeyEvent.VK_F){
                game.increaseSpeed();
            }

            if(key == KeyEvent.VK_S){
                game.decreaseSpeed();
            }

            if(key == KeyEvent.VK_ENTER){
                game.reset();
            }

        }
    }
}
