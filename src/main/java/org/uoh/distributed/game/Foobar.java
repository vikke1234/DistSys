package org.uoh.distributed.game;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


/**
 * @brief Main class for the game
 */
public class Foobar extends JFrame {

    private final int gridSize = 10; // size of the grid
    private final int cellSize = 40; // pixel size of each grid cell

    private static class Game extends JPanel implements KeyListener {
        private final int cellSize;
        private final int gridSize;
        private int playerX = 0;
        private int playerY = 0;

        private Food food;

        public Game(int cellSize, int gridSize) {
            this.cellSize = cellSize;
            this.gridSize = gridSize;
            setFocusable(true);
            requestFocus();
            addKeyListener(this);
            food = Food.generateFood(gridSize, gridSize);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    g.drawRect(i * cellSize, j * cellSize, cellSize, cellSize);
                }
            }
            if (playerX == food.getX() && playerY == food.getY()) {
                food = Food.generateFood(gridSize, gridSize);
            }
            drawFood(g, food);
            g.fillOval(playerX * cellSize, playerY * cellSize, cellSize, cellSize);
        }

        private void drawFood(Graphics g, Food food) {
            g.fillRect(food.getX() * cellSize, food.getY() * cellSize, cellSize, cellSize);
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    playerY = Math.max(playerY - 1, 0);
                    break;
                case KeyEvent.VK_DOWN:
                    playerY = Math.min(playerY + 1, gridSize - 1);
                    break;
                case KeyEvent.VK_LEFT:
                    playerX = Math.max(playerX - 1, 0);
                    break;
                case KeyEvent.VK_RIGHT:
                    playerX = Math.min(playerX + 1, gridSize - 1);
                    break;
            }
            repaint();
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    public Foobar() {
        setTitle("Grid Game");
        setSize((gridSize + 1) * cellSize, (gridSize + 1) * cellSize+1);
        setLayout(new BorderLayout());
        add(new Game(cellSize, gridSize));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
    }
}
