package net.runtime.role.snake;

import javax.swing.*;
import java.awt.*;

/**
 * Created by nguonly on 10/30/15.
 */
public class StatusPanel extends JPanel {

    private static final int STATISTICS_OFFSET = 1;

    private static final int CONTROLS_OFFSET = 20;

    private static final int MESSAGE_STRIDE = 15;

    private static final int SMALL_OFFSET = 10;

    private static final int LARGE_OFFSET = 20;

    public StatusPanel(){
        setPreferredSize(new Dimension(SnakeGame.COL_COUNT*SnakeGame.DOT_SIZE, 40));
        setBackground(Color.LIGHT_GRAY);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.setFont(SnakeGame.SMALL_FONT);

        //Draw the content for the statistics category.
        int drawY = STATISTICS_OFFSET;

        g.drawString("S : SpeedUp | F : SpeedDown | P : Pause Game | Enter : Reset", SMALL_OFFSET, drawY += MESSAGE_STRIDE);
        g.drawString("ArrowKey : Move", SMALL_OFFSET, drawY += MESSAGE_STRIDE);

    }
}
