package smac.tna.tdh1710.gomoku_ai;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    private ArrayList<XY> history = new ArrayList<>();
    //luu thu tu cac buoc di

    private ArrayList<XY> stack = new ArrayList<>();
    //luu danh sach cac nuoc di se duoc xet den

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
    }

    private void move(int x, int y) {
        if (index[x][y] == 0 || index[x][y] == 3) {
            if (turn) {
                turnPlayer.setText("Computer Turn");
                cell[x][y].setImageResource(R.drawable.o);
                index[x][y] = 2;
                updateStack(x, y, stack, index);
            } else {
                turnPlayer.setText("Player Turn");
                cell[x][y].setImageResource(R.drawable.x);
                index[x][y] = 1;
            }
            turn = !turn;
            number_move++;
            XY move_new = new XY(x, y);
            history.add(move_new);
            checkEndGame(x, y);
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

    private void checkEndGame(int x, int y) { //nhat dinh ham phai goi sau cau lenh turn = !turn
        endGame = checkWin(x, y);
        if (endGame) {
            for (int index = 0; index < 5; index++) {
                int w = list_win[index].x;
                int h = list_win[index].y;
                if (turn) {
                    cell[w][h].setImageResource(R.drawable.x_win);
                } else {
                    cell[w][h].setImageResource(R.drawable.o_win);
                }
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

            AlertDialog dialog = builder.create();
            dialog.show();
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

    private ArrayList<XY> updateStack(int x, int y, ArrayList<XY> stackT, int[][] indexT) {
        ArrayList<XY> stackTemp = new ArrayList<XY>(stackT);

        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                if (indexT[x + i][y + j] == 0) {
                    stackTemp.add(new XY(x + i, y + j, number_move));
                    indexT[x + i][y + j] = 3;
                }
            }
        }
        for (int i = 0; i < stackTemp.size(); i++) {
            XY t = stackTemp.get(i);
            if (t.x == x && t.y == y) {
                stackTemp.remove(i);
                break;
            }
        }

        return stackTemp;
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

    //Chuyen khoi 6 o sang dang text va tinh diem
    private int block(int x, int y, int iT, int select, int isCheck) {
        int result = 0;
        //x, y toa do diem dinh danh quan
        //iT
        //select = 1,2,3,4
        //isCheck = 1,2 luot di cua may = 1, cua nguoi = 2

        //xet theo hang ngang
        if (select == 1) {
            if (x + iT + 5 >= 15) {
                return 0;
            } else {
                String ev_after = "";
                String ev_before = "";
                for (int i = 0; i < 6; i++) {
                    if (i == -iT) {
                        ev_after += isCheck;
                        ev_before += "0";
                    }else{

                    }
                }
                result = evalute(ev_after) - evalute(ev_before);
            }
        }
//        String test = "";
//        for (int i = 0; i < 6; i++) {
//            if (a[i] == 0) test += '0';
//            if (a[i] == 1) test += '1';
//            if (a[i] == 2) test += '2';
//        }
//        result = evalute(test);
        return result;
    }

    private void bestMove() {
        XY best = new XY();
        int bestScore = Integer.MAX_VALUE;

        ArrayList<XY> stackTemp = new ArrayList<>(stack);
        int[][] indexTemp = new int[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                indexTemp[i][j] = index[i][j];
            }
        }
        for (XY xy : stack) {

        }
    }

    private class XY {
        int x;
        int y;
        int id;

        public XY() {
        }

        public XY(int a, int b) {
            x = a;
            y = b;
            id = -1;
        }

        public XY(int a, int b, int c) {
            x = a;
            y = b;
            id = c;
        }
    }
}
