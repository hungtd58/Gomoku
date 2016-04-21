package smac.tna.tdh1710.gomoku_ai;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class GomokuActivity extends AppCompatActivity {

    private int WIDTH = 15;
    private int HEIGHT = 15;
    private Context context;
    private ImageView cell[][] = new ImageView[WIDTH][HEIGHT];
    private int index[][] = new int[WIDTH][HEIGHT];
    //index = 0 nếu ô đó chưa được đánh, không nằm trong stack
    //index = 1 nếu ô đó chứa quân X - quân của máy
    //index = 2 nếu ô đó chứa quân O - quân của người
    //index = 3 nếu ô đó chưa được đánh, nằm trong stack

    private int backupIndex[][] = new int[WIDTH][HEIGHT];
    //backup index

    private ArrayList<XY> history = new ArrayList<>();
    //luu thu tu cac buoc di

    private ArrayList<XY> stack = new ArrayList<>();
    //luu danh sach cac nuoc di se duoc xet den

    private ArrayList<XY> backupStack = new ArrayList<>();
    //backup stack

    private boolean endGame = false;
    //game chua ket thuc thi endGame = false

    private boolean turn;
    //turn = true - luot di cua nguoi
    //turn = false - luot di cua may

    private XY list_win[] = new XY[5];
    //danh sach 5 o chien thang

    private int number_move;
    //number_move = 5 -> nuoc di thu 5

    private Button newGame, undoMove;
    private TextView turnPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gomoku);

        control();
        init();
    }

    private void init() {
        history.clear();
        stack.clear();
        number_move = 0;
        turn = false;
        turnPlayer.setText("Computer Turn");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //nhan gia tri chieu rong cua man hinh
        int width_screen = displayMetrics.widthPixels - 20;

        LinearLayout.LayoutParams ly_cell_row = new LinearLayout.LayoutParams(width_screen, width_screen / 15);
        LinearLayout.LayoutParams ly_cell = new LinearLayout.LayoutParams(width_screen / 15, width_screen / 15);

        LinearLayout boardGame = (LinearLayout) findViewById(R.id.boardGame);
        boardGame.removeAllViews();
        for (int j = 0; j < HEIGHT; j++) {

            context = GomokuActivity.this;
            LinearLayout ln_row = new LinearLayout(context);

            for (int i = 0; i < WIDTH; i++) {
                cell[i][j] = new ImageView(context);
                cell[i][j].setImageResource(R.drawable.empty);
                ln_row.addView(cell[i][j], ly_cell);
            }
            boardGame.addView(ln_row, ly_cell_row);
        }

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < WIDTH; j++) {
                index[i][j] = 0;
                final int x = i;
                final int y = j;
                cell[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        move(x, y);
                    }
                });
            }
        }
        move(7, 7);
    }

    private void move(final int x, final int y) {
        if (index[x][y] == 0 || index[x][y] == 3) {
            if (turn) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        turnPlayer.setText("Computer Turn");
                        cell[x][y].setImageResource(R.drawable.o);
                        index[x][y] = 2;
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        turnPlayer.setText("Player Turn");
                        cell[x][y].setImageResource(R.drawable.x);
                        index[x][y] = 1;
                    }
                });
            }
            updateStack(x, y, stack, index);
            //displayStack();
            turn = !turn;
            number_move++;
            XY move_new = new XY(x, y);
            history.add(move_new);
            alertFinish(x, y);
            if(!turn && !endGame){
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        bestMove();
                    }
                });
            }
        }
    }


    private boolean checkWin(int x, int y) { //nhat dinh ham phai goi sau cau lenh turn = !turn
        int count = 0; //neu count = 5 thi co nguoi chien thang
        int value_is_checking;
        if (turn) {
            value_is_checking = 1;
        } else {
            value_is_checking = 2;
        }

        //kiem tra theo hang ngang
        for (int i = -4; i <= 4; i++) {
            if (x + i >= 0 && x + i < WIDTH) {
                if (index[x + i][y] == value_is_checking) {
                    list_win[count] = new XY(x + i, y);
                    count++;
                } else {
                    count = 0;
                }
            }
            if (count == 5) return true;
        }
        count = 0; //tra lai count = 0

        //kiem tra theo hang doc
        for (int i = -4; i <= 4; i++) {
            if (y + i >= 0 && y + i < HEIGHT) {
                if (index[x][y + i] == value_is_checking) {
                    list_win[count] = new XY(x, y + i);
                    count++;
                } else {
                    count = 0;
                }
            }
            if (count == 5) return true;
        }
        count = 0; //tra lai count = 0

        //kiem tra theo duong cheo tu trai sang phai
        for (int i = -4; i <= 4; i++) {
            if (x + i >= 0 && x + i < WIDTH && y + i >= 0 && y + i < HEIGHT) {
                if (index[x + i][y + i] == value_is_checking) {
                    list_win[count] = new XY(x + i, y + i);
                    count++;
                } else {
                    count = 0;
                }
            }
            if (count == 5) return true;
        }
        count = 0; //tra lai count = 0

        for (int i = -4; i <= 4; i++) {
            if (x - i >= 0 && x - i < WIDTH && y + i >= 0 && y + i < HEIGHT) {
                if (index[x - i][y + i] == value_is_checking) {
                    list_win[count] = new XY(x - i, y + i);
                    count++;
                } else {
                    count = 0;
                }
            }
            if (count == 5) return true;
        }

        return false;
    }

    private void alertFinish(int x, int y) { //nhat dinh ham phai goi sau cau lenh turn = !turn
        endGame = checkWin(x, y);
        if (endGame) {
            for (int index = 0; index < 5; index++) {
                final int w = list_win[index].x;
                final int h = list_win[index].y;
                if (turn) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cell[w][h].setImageResource(R.drawable.x_win);
                        }
                    });
                } else {runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cell[w][h].setImageResource(R.drawable.o_win);
                    }
                });
                }
            }
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Victory");
            if (turn) {
                builder.setMessage("Computer win");
            } else {
                builder.setMessage("Player win");
            }
            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    for (int i = 0; i < WIDTH; i++) {
                        for (int j = 0; j < HEIGHT; j++) {
                            cell[i][j].setClickable(false); //nhấn ok thì có thể xem màn hình game nhưng không thể đánh thêm
                        }
                    }
                }
            }).setPositiveButton("New Game", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    init();
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
    }

    private void control() {
        newGame = (Button) findViewById(R.id.newGame);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
            }
        });

        undoMove = (Button) findViewById(R.id.undoMove);
        undoMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        turnPlayer = (TextView) findViewById(R.id.turnPlayer);
    }

    private int total(int value_temp[][]) {
        int result = 0;
        //Quet theo chieu doc
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 10; j++) {

                String a = "";
                for (int k = 0; k < 6; k++) {
                    try {
                        if (value_temp[i][j + k] == 3) {
                            a += "0";
                        } else {
                            a += value_temp[i][j + k];
                        }
                    } catch (Exception e) {
                        break;
                    }
                }

                result += evalute(a);
            }
        }

        //Quet theo chieu ngang
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 10; j++) {

                String a = "";
                for (int k = 0; k < 6; k++) {
                    try {
                        if (value_temp[j + k][i] == 3) {
                            a += "0";
                        } else {
                            a += value_temp[j + k][i];
                        }
                    } catch (Exception e) {
                        break;
                    }
                }

                result += evalute(a);
            }
        }

        //Quet cheo trai sang phai
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {

                String a = "";
                for (int k = 0; k < 6; k++) {
                    try {
                        if (value_temp[i + k][j + k] == 3) {
                            a += "0";
                        } else {
                            a += value_temp[i + k][j + k];
                        }
                    } catch (Exception e) {
                        break;
                    }
                }

                result += evalute(a);
            }
        }

        //Quet cheo phai sang trai
        for (int i = 5; i < 15; i++) {
            for (int j = 0; j < 10; j++) {

                String a = "";
                for (int k = 0; k < 6; k++) {
                    try {
                        if (value_temp[i - k][j + k] == 3) {
                            a += "0";
                        } else {
                            a += value_temp[i - k][j + k];
                        }
                    } catch (Exception e) {
                        break;
                    }
                }

                result += evalute(a);
            }
        }

        return result;
    }

    private void updateStack(int x, int y, ArrayList<XY> stackTemp, int[][] indexT) {

        for (int i = -2; i < 3; i++) {
            if (i == 0) {
                for (int j = -2; j < 3; j++) {
                    if (y + j >= 0 && y + j < 15) {
                        if (indexT[x][y + j] == 0) {
                            stackTemp.add(new XY(x, y + j, number_move));
                            indexT[x][y + j] = 3;
                        }
                    }
                }
                continue;
            }
            for (int j = -1; j < 2; j++) {
                if (x + i >= 0 && x + i < 15 && y + i * j >= 0 && y + i * j < 15) {
                    if (indexT[x + i][y + i * j] == 0) {
                        stackTemp.add(new XY(x + i, y + i * j, number_move));
                        indexT[x + i][y + i * j] = 3;
                    }
                }
            }
        }//them cac o xung quanh (x,y) chua duoc danh o vua danh vao stack

        for (int i = 0; i < stackTemp.size(); i++) {
            XY t = stackTemp.get(i);
            if (t.x == x && t.y == y) {
                stackTemp.remove(i);
                break;
            }
        } //neu o vua danh nam trong stack thi remove o do khoi stack

    }

    private void displayStack() {
        for (int i = 0; i < stack.size(); i++) {
            final XY temp = stack.get(i);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cell[temp.x][temp.y].setImageResource(R.drawable.stack);
                }
            });

        }
        ;
    }

    //Ham danh gia
    //Diem cang lon, loi the cho may cang cao
    //Diem cang nho, loi the cho nguoi cang cao
    //nguoc lai voi phien ban Gomoku_Window da code lan truoc
    private int evalute(String t) {
        switch (t) {
            case "111110":
                return 100000000;
            case "011111":
                return 100000000;
            case "211111":
                return 100000000;
            case "111112":
                return 100000000;
            case "011110":
                return 10000000;
            case "101110":
                return 1002;
            case "011101":
                return 1002;
            case "011112":
                return 1000;
            case "211110":
                return 1000;
            case "011100":
                return 102;
            case "001110":
                return 102;
            case "210111":
                return 100;
            case "111012":
                return 100;
            case "211011":
                return 100;
            case "110112":
                return 100;
            case "211101":
                return 100;
            case "101112":
                return 100;
            case "010100":
                return 10;
            case "001010":
                return 10;
            case "011000":
                return 10;
            case "000110":
                return 10;
            case "211000":
                return 1;
            case "000112":
                return 1;
            case "201100":
                return 1;
            case "001102":
                return 1;
            case "200110":
                return 1;
            case "011002":
                return 1;
            case "200011":
                return 1;
            case "110002":
                return 1;

            case "222220":
                return -100000000;
            case "022222":
                return -100000000;
            case "122222":
                return -100000000;
            case "222221":
                return -100000000;
            case "022220":
                return -10000000;
            case "202220":
                return -1002;
            case "022202":
                return -1002;
            case "022221":
                return -1000;
            case "122220":
                return -1000;
            case "022200":
                return -102;
            case "002220":
                return -102;
            case "120222":
                return -100;
            case "222021":
                return -100;
            case "122022":
                return -100;
            case "220221":
                return -100;
            case "122202":
                return -100;
            case "202221":
                return -100;
            case "020200":
                return -10;
            case "002020":
                return -10;
            case "022000":
                return -10;
            case "000220":
                return -10;
            case "122000":
                return -1;
            case "000221":
                return -1;
            case "102200":
                return -1;
            case "002201":
                return -1;
            case "100220":
                return -1;
            case "022001":
                return -1;
            case "100022":
                return -1;
            case "220001":
                return -1;
            default:
                break;
        }
        return 0;
    }


    private void bestMove() {
        newGame.setClickable(false);
        for(int a = 0; a < 15; a++){
            for(int b = 0; b < 15; b++){
                cell[a][b].setClickable(false);
            }
        }
        XY best = new XY();
        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < stack.size(); i++) {
            XY step = stack.get(i);
            int min = Integer.MAX_VALUE;
            ArrayList<XY> stackTemp = new ArrayList<>(stack);
            int indexTemp[][] = new int[15][15];
            copyArray(index, indexTemp);
            indexTemp[step.x][step.y] = 1;
            updateStack(step.x, step.y, stackTemp, indexTemp);
            for (int j = 0; j < stackTemp.size(); j++) {
                XY step2 = stackTemp.get(j);
                indexTemp[step2.x][step2.y] = 2;
                int diem = total(indexTemp);
                if(min > diem){
                    min = diem;
                }
                indexTemp[step2.x][step2.y] = 3;
                if(min < bestScore) break;
            }
            if(bestScore < min){
                bestScore = min;
                best = step;
            }
        }
        move(best.x, best.y);
        newGame.setClickable(true);
        for(int a = 0; a < 15; a++){
            for(int b = 0; b < 15; b++){
                cell[a][b].setClickable(true);
            }
        }
    }

    private void copyArray(int[][] start, int[][] end) {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                end[i][j] = start[i][j];
            }
        }
    }
}
