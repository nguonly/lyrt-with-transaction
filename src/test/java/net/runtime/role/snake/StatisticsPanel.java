package net.runtime.role.snake;

import javax.swing.*;
import java.awt.*;

/**
 * Created by nguonly on 10/30/15.
 */
public class StatisticsPanel extends JPanel {
    SnakeGame game;

    public StatisticsPanel(SnakeGame snakeGame){
        setPreferredSize(new Dimension(SnakeGame.COL_COUNT*SnakeGame.DOT_SIZE, 20));
        setBackground(Color.PINK);
        this.game = snakeGame;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLUE);
        g.setFont(SnakeGame.SMALL_FONT);
        FontMetrics metr = getFontMetrics(game.MEDIUM_FONT);

        g.drawString("Food Eaten: " + game.getFoodEaten(), 10, 15);

        String msgSpeed = "Speed : " + game.getSpeed();
        g.drawString(msgSpeed, this.getWidth() + 10 - metr.stringWidth(msgSpeed), 15);
    }
}
