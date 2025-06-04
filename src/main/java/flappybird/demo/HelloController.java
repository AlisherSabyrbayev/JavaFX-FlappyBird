package flappybird.demo;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Random;

public class HelloController {

    @FXML
    private Canvas gameCanvas;
    private GraphicsContext gc;

    private final int boardWidth = 360;
    private final int boardHeight = 640;

    private Image backgroundImg;
    private Image birdImg;
    private Image topPipeImg;
    private Image bottomPipeImg;

    private double birdX = boardWidth / 8.0;
    private double birdY = boardHeight / 2.0;
    private final double birdWidth = 34;
    private final double birdHeight = 24;

    private double velocityY = 0;
    private final double gravity = 1000;
    private final double jumpForce = -400;
    private long lastTime = 0;

    private final ArrayList<Pipe> pipes = new ArrayList<>();
    private final Random random = new Random();
    private boolean gameOver = false;
    private double score = 0;

    private double pipeSpeed = 1.5;

    private AnimationTimer gameLoop;

    // Вспомогательный класс для труб
    private static class Pipe {
        double x;
        double y;
        double width;
        double height;
        Image img;
        boolean passed;

        Pipe(double x, double y, double width, double height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
            this.passed = false;
        }
    }

    @FXML
    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();

        backgroundImg = new Image(getClass().getResourceAsStream("/flappybirdbg.png"));
        birdImg = new Image(getClass().getResourceAsStream("/flappybird.png"));
        topPipeImg = new Image(getClass().getResourceAsStream("/toppipe.png"));
        bottomPipeImg = new Image(getClass().getResourceAsStream("/bottompipe.png"));

        startGameLoop();
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            long lastPipeTime = 0;

            @Override
            public void handle(long now) {
                // Добавление труб каждые 1.5 секунды
                if (now - lastPipeTime >= 2_100_000_000) {
                    placePipes();
                    lastPipeTime = now;
                }

                update();
                draw();
            }
        };
        gameLoop.start();
    }

    private void placePipes() {
        int pipeWidth = 64;
        int pipeHeight = 512;
        int openingSpace = boardHeight / 3;

        // Верхняя труба
        double randomPipeY = -pipeHeight / 3.0 - random.nextDouble() * (pipeHeight / 3.0);
        pipes.add(new Pipe(boardWidth, randomPipeY, pipeWidth, pipeHeight, topPipeImg));

        // Нижняя труба
        pipes.add(new Pipe(boardWidth, randomPipeY + pipeHeight + openingSpace, pipeWidth, pipeHeight, bottomPipeImg));
    }

    private void update() {
        if (gameOver) {
            return;
        }

        long currentTime = System.nanoTime();
        double deltaTime = (lastTime == 0) ? 0 : (currentTime - lastTime) / 1_000_000_000.0;
        lastTime = currentTime;

        if (deltaTime == 0) return;


        // Обновление положения птицы
        velocityY += gravity * deltaTime * 1.2; // Множитель 1.2 ускоряет падение
        birdY += velocityY * deltaTime * 1.1;   // Множитель 1.1 ускоряет движение

        // Обновление труб с постоянной скоростью (не зависящей от FPS)
        for (int i = pipes.size() - 1; i >= 0; i--) {
            Pipe pipe = pipes.get(i);
            pipe.x -= pipeSpeed * 80 * deltaTime; // 60 пикселей в секунду

            // Проверка на столкновение
            if (collision(birdX, birdY, birdWidth, birdHeight, pipe)) {
                gameOver = true;
                gameLoop.stop();
            }

            // Удаление труб за экраном
            if (pipe.x + pipe.width < 0) {
                pipes.remove(i);
            }

            // Увеличение счета
            if (!pipe.passed && birdX > pipe.x + pipe.width) {
                score += 0.5; // Каждая пара труб дает 1 очко (0.5 за каждую)
                pipe.passed = true;

                // Увеличение скорости при каждом 5-м очке
                if ((int) score % 5 == 0) {
                    pipeSpeed += 0.5; // Увеличиваем скорость труб
                }
            }
        }

        // Проверка на выход птицы за границы
        if (birdY > boardHeight || birdY < 0) {
            gameOver = true;
            gameLoop.stop();
        }
    }

    private boolean collision(double x1, double y1, double w1, double h1, Pipe pipe) {
        return x1 < pipe.x + pipe.width &&
                x1 + w1 > pipe.x &&
                y1 < pipe.y + pipe.height &&
                y1 + h1 > pipe.y;
    }

    private void draw() {
        // Фон
        gc.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight);

        // Птица
        gc.drawImage(birdImg, birdX, birdY, birdWidth, birdHeight);

        // Трубы
        for (Pipe pipe : pipes) {
            gc.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height);
        }

        // Счет
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(32));
        if (gameOver) {
            gc.fillText("Game Over: " + (int) score, 10, 35);
        } else {
            gc.fillText(String.valueOf((int) score), 10, 35);
        }
    }

    // Обработчик нажатий клавиш
    public void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case SPACE:
                if (gameOver) {
                    resetGame();
                } else {
                    velocityY = jumpForce * 1.1;
                }
                break;
        }
    }

    private void resetGame() {
        birdY = boardHeight / 2.0;
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        score = 0;
        pipeSpeed = 1.5 ; // Возвращаем начальную скорость
        lastTime = 0; // Сбрасываем время для delta time
        gameLoop.start();
    }
}
