package com.sonhoai.sonho.tiktak;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sonho on 3/15/2018.
 */

public class ChessBoard {
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    private int[][] board;//cac buoc đã đi -1 là chưa đi, 0 la nguoi choi, 1 la may
    private int player;//nguoi choi nào
    private Context context;
    private int bitmapWidth, bitmapHeight, colQty,rowQty;
    private List<Line> lines;
    private Minimax minimax;
    private int winQty;
    private Move previousMove;
    private int winner;
    public static boolean isGameOver = false;

    private Bitmap playerA, playerB;

    public ChessBoard(Context context, int bitmapWidth, int bitmapHeight, int colQty, int rowQty) {
        this.context = context;
        this.bitmapWidth = bitmapWidth;
        this.bitmapHeight = bitmapHeight;
        this.colQty = colQty;
        this.rowQty = rowQty;
    }

    public void initBoard2() {
        winner = -1;
        previousMove = null;

        if (colQty > 5) {
            winQty = 4;
        } else {
            winQty = 2;
        }
    }

    public void init() {
        winner = -1;
        previousMove = null;
        minimax = new Minimax();
        lines = new ArrayList<>();
        bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        board = new int[rowQty][colQty];

        playerA = BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_player_a);
        playerB = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_player_b);

        if (colQty > 5) {
            winQty = 4;
        } else {
            winQty = 2;
        }

        for(int i = 0; i<rowQty; i++){
            for(int j = 0; j < colQty;j++){
                board[i][j] = -1;//-1 là chưa đi
            }
        }

        player = 0;
        paint.setStrokeWidth(2);
        int celWidth = bitmapWidth/colQty;
        int celHeight = bitmapHeight/rowQty;

        for(int i = 0; i <= colQty; i++){
            lines.add(new Line(celWidth*i, 0, celWidth*i, bitmapHeight));
        }
        for(int i = 0; i <= rowQty; i++){
            lines.add(new Line(0, i*celHeight, bitmapWidth, i*celHeight));
        }
    }

    public Bitmap drawBoard(){
        for(int i = 0; i < lines.size(); i++) {
            canvas.drawLine(
                    lines.get(i).getX1(),
                    lines.get(i).getY1(),
                    lines.get(i).getX2(),
                    lines.get(i).getY2(),
                    paint
            );
        }

        return bitmap;
    }

    public boolean negaABMovee(final View view, MotionEvent motionEvent) {
        if (winner == 0 || winner == 1) {
            return true;
        }

        final int cellWidth = bitmapWidth / colQty;
        final int cellHeight = bitmapHeight / rowQty;
        final int colIndex = (int) (motionEvent.getX() / (view.getWidth() / colQty));
        final int rowIndex = (int) (motionEvent.getY() / (view.getHeight() / rowQty));

        int count = getCurrentDept();
        final int currentDetp = rowQty*colQty - count;

        Record record = minimax.minimaxRecode(
                ChessBoard.this,
                currentDetp,
                rowQty * colQty,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE
        );

        onDrawBoard(record.getMove().getRowIndex(), record.getMove().getColIndex() , cellWidth, cellHeight);

        makeMove(record.getMove());

        if (isGameOver()) {
            isGameOver = true;
            if (winner == 1) {
                Toast.makeText(context, "Ban thua roi", Toast.LENGTH_LONG).show();
            } else if (winner == 0) {
                Toast.makeText(context, "Ban thang roi", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Ban hoa", Toast.LENGTH_LONG).show();
            }
        }

        view.invalidate();
        return true;
    }

    public boolean onTouch(final View view, MotionEvent motionEvent){
        final int cellWidth = bitmapWidth / colQty;
        final int cellHeight = bitmapHeight / rowQty;
        final int colIndex = (int) (motionEvent.getX() / (view.getWidth() / colQty));
        final int rowIndex = (int) (motionEvent.getY() / (view.getHeight() / rowQty));

        if(board[rowIndex][colIndex] != -1){
            return true;
        }

        if (winner == 0 || winner == 1) {
            return true;
        }

        onDrawBoard(rowIndex, colIndex, cellWidth, cellHeight);
        view.invalidate();

        makeMove(new Move(rowIndex, colIndex));

        if(isGameOver()){
            isGameOver = true;
            if (winner == 1) {
                Toast.makeText(context, "Ban thua roi", Toast.LENGTH_LONG).show();
            } else if (winner == 0) {
                Toast.makeText(context, "Ban thang roi", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Ban hoa", Toast.LENGTH_LONG).show();
            }

            return true;
        }

        return true;
    }

    public void onDrawBoard(int rowIndex, int colIndex, int cellWidth, int cellHeight){
        int padding = 50;

        if(player == 0){
            canvas.drawBitmap(
                    playerA,
                    new Rect(0,0,playerA.getWidth(), playerA.getHeight()),
                    new Rect(colIndex*cellWidth+padding,rowIndex*cellHeight+padding,(colIndex+1)*cellWidth -padding, (rowIndex+1)*cellHeight -padding),
                    paint);
        } else {
            canvas.drawBitmap(
                    playerB,
                    new Rect(0, 0, playerB.getWidth(), playerB.getHeight()),
                    new Rect(colIndex * cellWidth, rowIndex * cellHeight, (colIndex + 1) * cellWidth, (rowIndex + 1) * cellHeight),
                    paint);
        }
    }

    public boolean isGameOver(){
        if (checkWin()) {
            return true;
        }

        int count = 0;
        for (int i = 0; i < rowQty; i++) {
            for (int j = 0; j < colQty; j++) {
                if (board[i][j] == -1) count++;
            }
        }
        if (count == 0){
            winner = -1;
            return true;
        }

        return false;
    }

    private boolean checkWin() {
        if (previousMove == null) return false;
        if (checkRow(previousMove.getRowIndex())
                || checkColumn(previousMove.getColIndex())
                || checkDiagonalFromTopLeft(previousMove.getRowIndex(), previousMove.getColIndex())
                || checkDiagonalFromTopRight(previousMove.getRowIndex(), previousMove.getColIndex())) {
            return true;
        }

        return false;
    }

    private Boolean checkRow (int row) {
        int count = 0;
        for (int i = 1; i < rowQty; i++) {
            if (board[row][i] == board[row][i-1] && board[row][i] != -1) {
                count++;

                if (count == winQty) {
                    winner = board[row][i];
                    return true;
                }
            } else {
                count = 0;
            }
        }

        return false;
    }

    private boolean checkColumn (int column) {
        int count = 0;
        for (int i = 1; i < colQty; i++) {
            if (board[i][column] == board[i-1][column] && board[i][column] != -1)  {
                count++;

                if (count == winQty) {
                    winner = board[i][column];
                    return true;
                }
            } else {
               count = 0;
            }
        }

        return false;
    }

    private Boolean checkDiagonalFromTopRight (int row, int col) {
        int rowStart, colStart;
        int i = 0;
        int count = 0;

        if (row + col < colQty - 1) {
            colStart = row + col;
            rowStart = 0;
        } else {
            colStart = colQty - 1;
            rowStart = col + row - (colQty - 1);
        }

        while (colStart - i - 1 >= 0 && rowStart + i + 1 < colQty) {
            if (board[rowStart + i][colStart - i] == board[rowStart + i + 1][colStart - i - 1] && board[rowStart + i][colStart - i] != -1) {
                count++;

                if (count == winQty) {
                    winner = board[rowStart + i][colStart - i];
                    return true;
                }
            } else {
                count = 0;
            }

            i++;
        }

        return false;
    }

    private Boolean checkDiagonalFromTopLeft (int row, int col) {
        int rowStart, colStart;
        int i = 0;
        int count = 0;

        if (row > col) {
            rowStart = row - col;
            colStart = 0;
        } else {
            rowStart = 0;
            colStart = col - row;
        }

        while (rowStart + i + 1 < colQty && colStart + i + 1 < rowQty) {
            if (board[rowStart + i][colStart + i] == board[rowStart + i + 1][colStart + i + 1] && board[rowStart + i][colStart + i] != -1) {
                count++;

                if (count == winQty) {
                    winner = board[rowStart + i][colStart + i];
                    return true;
                }
            } else {
                count = 0;
            }
            i++;
        }

        return false;
    }

    public List<Move> getMove() {
        List<Move> moves = new ArrayList<>();

        for (int i = 0; i < rowQty; i++) {
            for (int j = 0; j < colQty; j++) {
                if (board[i][j] == -1) moves.add(new Move(i, j));//có thể đi dc
            }
        }
        return moves;
    }

    public void makeMove(Move move) {
        previousMove = move;
        board[move.getRowIndex()][move.getColIndex()] = player;
        player = (player + 1) % 2;
    }

    //0 -1 1: quan sát trên ngườu chơi hiện tại
    //0 hoà
    //1 người chơi hiện tại thắng
    //winner: -1, 1, 0
    public int evaluate() {
        if (winner == -1) {
            return 0;
        }

        if (winner == player) {
            return 1;
        } else {
           return -1;
        }
    }


    public int[][] getNewBoard(){
        int[][] newBoard = new int[rowQty][colQty];
        for (int i = 0; i < rowQty; i++) {
            for (int j = 0; j < colQty; j++) {
                newBoard[i][j] = board[i][j];
            }
        }
        return newBoard;
    }
    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getBitmapWidth() {
        return bitmapWidth;
    }

    public void setBitmapWidth(int bitmapWidth) {
        this.bitmapWidth = bitmapWidth;
    }

    public int getBitmapHeight() {
        return bitmapHeight;
    }

    public void setBitmapHeight(int bitmapHeight) {
        this.bitmapHeight = bitmapHeight;
    }

    public int getColQty() {
        return colQty;
    }

    public void setColQty(int colQty) {
        this.colQty = colQty;
    }

    public int getRowQty() {
        return rowQty;
    }

    public void setRowQty(int rowQty) {
        this.rowQty = rowQty;
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public int getCurrentDept(){
        int count = 0;
        for (int i = 0; i < rowQty; i++) {
            for (int j = 0; j < colQty; j++) {
                if (board[i][j] == -1) count++;
            }
        }
        return count;
    }

    public int getWinner() {
        return winner;
    }
}








