package smac.tna.tdh1710.gomoku_ai;

/**
 * Created by tdh1710 on 19/04/2016.
 */
public class XY {
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