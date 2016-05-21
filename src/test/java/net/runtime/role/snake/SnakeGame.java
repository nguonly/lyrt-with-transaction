package net.runtime.role.snake;


import net.runtime.role.actor.Compartment;
import net.runtime.role.evolution.FileWatcher;
import net.runtime.role.helper.DumpHelper;
import net.runtime.role.registry.RegistryManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by nguonly on 10/30/15.
 */
public class SnakeGame extends JFrame implements ActionListener{
    final static int ROW_COUNT = 30;
    final static int COL_COUNT = 30;
    final static int DOT_SIZE = 15;
    final int DELAY = 200;
    final int DELAY_STEP = 10;

    static final Font MEDIUM_FONT = new Font("Tahoma", Font.BOLD, 16);

    /**
     * The small font to draw with.
     */
    static final Font SMALL_FONT = new Font("Tahoma", Font.BOLD, 12);

    int speed = 0;

    BoardPanel boardPanel;
    StatusPanel statusPanel;
    StatisticsPanel statisticsPanel;

    Timer timer;

    Board board;
    Snake snake;
    Router router;

    public SnakeGame(Board board, Snake snake, Router router){
        this.board = board;
        this.snake = snake;
        this.router = router;
        //boardPanel = new BoardPanel(this);
        boardPanel = RegistryManager.getInstance().newPlayer(BoardPanel.class, new Class[]{SnakeGame.class}, new Object[]{this});
        statusPanel = new StatusPanel();
        statisticsPanel = new StatisticsPanel(this);

        add(boardPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        add(statisticsPanel, BorderLayout.NORTH);

        setResizable(false);
        pack();

        setTitle("EzSnake");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        start();
    }

    public void start(){
        router.setDirection(Router.DIRECTION_NONE);
        board.generateFood();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void reset(){
        router.reset();
        timer.setDelay(DELAY);
        speed = 0;
        timer.start();
    }

    public void pause(){
        router.setDirection(Router.DIRECTION_NONE);
    }

    public boolean isPause(){
        return router.getDirection() == Router.DIRECTION_NONE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        router.update();
        if(router.gameOver) timer.stop();
        boardPanel.repaint();
        statisticsPanel.repaint();
    }

    public int getFoodEaten() {
        //return foodEaten;
        return snake.getFoodEaten();
    }

    public int getSpeed() {
        return speed+1;
        //return timer.getDelay();
    }

    public void increaseSpeed(){
        if(speed<9) {
            speed++;
            timer.setDelay(timer.getDelay() - DELAY_STEP);
        }
    }

    public void decreaseSpeed(){
        if(speed>0) {
            speed--;
            timer.setDelay(timer.getDelay() + DELAY_STEP);
        }
    }

    public static void main(String[] args){

        //Board board = Player.initialize(Board.class, new Class[]{int.class, int.class}, new Object[]{SnakeGame.ROW_COUNT, SnakeGame.COL_COUNT});
        Board board = RegistryManager.getInstance().newPlayer(Board.class, new Class[]{int.class, int.class}, new Object[]{SnakeGame.ROW_COUNT, SnakeGame.COL_COUNT});

//        Snake snake = new Snake(new Cell(5, 6));
        Snake snake = new Snake(board, 5, 6);
        //Router router = new Router(snake, board);
        Router router = RegistryManager.getInstance().newPlayer(Router.class, new Class[]{Snake.class, Board.class}, new Object[]{snake, board});

        JFrame ex = new SnakeGame(board, snake, router);
        ex.setVisible(true);

//        EventQueue.invokeLater(() -> {
//            JFrame ex = new SnakeGame(board, snake, router);
//            ex.setVisible(true);
//        });

        //Test binding and dump
        Compartment comp = new Compartment();
        comp.activate();

        Thread watchService = new WatchService();
        watchService.start();

        do{
            System.out.println(":::: Console command :::: ");
            Scanner keyboard = new Scanner(System.in);
            String key = keyboard.nextLine();
            if(key.equalsIgnoreCase("dumpRelation")){
                DumpHelper.dumpRelation();
            }else if(key.equalsIgnoreCase("dumpCore")){
                DumpHelper.dumpCoreObjects();
            }else if(key.equalsIgnoreCase("dumpCompartment")) {
                DumpHelper.dumpCompartments();
            }else if(key.equalsIgnoreCase("dumpRole")){
                DumpHelper.dumpRoles();
            }else {
                System.out.println("Invalid command. Try: dumpCore, dumpCompartment, dumpRelation");
            }
        }while(true);
    }

    static class WatchService extends Thread{
        public void run(){
            try {
                String dir = System.getProperty("user.dir");
                Path p = Paths.get(dir + "/src/test/java/net/runtime/role/snake");
                FileWatcher fileWatcher = FileWatcher.getInstance();
                fileWatcher.register(p);
                fileWatcher.monitor("evolution.xml");
                fileWatcher.processEvents();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
