import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;


public class sokoban extends JPanel implements KeyListener {
  // Box Player Goal Wall Dead
                      // B P G W
  int T_D    = 0x00;  // 0 0 0 0
  int T_W    = 0x01;  // 0 0 0 1
  int T_G    = 0x02;  // 0 0 1 0
  int T_P    = 0x04;  // 0 1 0 0
  int T_PG   = 0x06;  // 0 1 1 0
  int T_B    = 0x08;  // 1 0 0 0
  int T_BG   = 0x0A;  // 1 0 1 0

  static int BOARDWD = 400;
  static int BOARDHT = 400;
  int TILEWD  = 32;
  int TILEHT  = 32;
  int ROWS;
  int COLS;

  int pIndex = -1;

  sokoTile theTiles[] = new sokoTile[16];
  sokoBoard theBoard  = new sokoBoard();

  actor theActors[] = new actor[13];

  public sokoban() {
    super();
    this.setFocusable(true);
    this.addKeyListener(this);
    loadTiles();

    Point actorLocs[] = theBoard.getA();
    int n=0;
    Boolean pushable=false;
    Boolean collision=false;
    for(Point a : actorLocs) {
      int v = theBoard.getV(a);
      if(v==T_P) {
        pIndex = n;
        collision = true;
        pushable = false;
      } else if(v==T_G) {
        collision = false;
        pushable = false;
      } else if(v==T_B) {
        collision = true;
        pushable = true;
      }
      theActors[n] = new actor("Actor"+n,a,theBoard.getV(a), collision, pushable);
      theBoard.setV( (int)theActors[n].getX(), (int)theActors[n].getY(),0);
      n++;
    }
  }

  public void loadTiles() {
    theTiles[T_D]  = new sokoTile("tiles/dead.png");
    theTiles[T_W]  = new sokoTile("tiles/wall.png");
    theTiles[T_G]  = new sokoTile("tiles/goal.png");
    theTiles[T_P]  = new sokoTile("tiles/play.png");
    theTiles[T_PG] = new sokoTile("tiles/playgoal.png");
    theTiles[T_B]  = new sokoTile("tiles/box.png");
    theTiles[T_BG] = new sokoTile("tiles/boxgoal.png");
  }

  public void paintComponent(Graphics g){
    int ROWS = theBoard.rows();
    int COLS = theBoard.cols();
    for(int r=0;r<ROWS;r++) {
      for(int c=0;c<COLS;c++) {
        int t = theBoard.getV(c,r);
        g.drawImage(theTiles[t].getImg(),c*TILEWD,r*TILEHT,TILEWD,TILEHT,this);
      }
    }
    for(actor a : theActors) {
      g.drawImage(theTiles[a.getI()].getImg(),
                  TILEWD*(int)a.getX(),TILEHT*(int)a.getY(),
                  TILEWD,TILEHT,this);
    }

  }

  public static void main(String arg[]){
    JFrame sokoFrame = new JFrame("Sokoban");
    sokoFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    sokoban panel = new sokoban();
    sokoFrame.setSize(BOARDWD,BOARDHT);
    sokoFrame.setContentPane(panel);
    sokoFrame.setVisible(true);
    panel.requestFocus();
  }

  public void keyPressed(KeyEvent e) {
    Point _m = new Point(0,0);
    switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT:
        _m.translate(-1,0);
        break;
      case KeyEvent.VK_RIGHT:
        _m.translate(1,0);
        break;
      case KeyEvent.VK_UP:
        _m.translate(0,-1);
        break;
      case KeyEvent.VK_DOWN:
        _m.translate(0,1);
        break;
      default:
        break;
    }
    if(theBoard.tryMove(theActors[pIndex],_m)) {
      theBoard.moveActor(theActors[pIndex],_m);
      repaint();
    }
  }

  public void keyTyped(KeyEvent e)    { }
  public void keyReleased(KeyEvent e) { }

}

class sokoTile {
  private Image _i;

  sokoTile() {
    // empty constructor
  }

  sokoTile(String imgFN) {
    setImg(imgFN);
  }

  public void setImg(String imgFN) {
    _i = Toolkit.getDefaultToolkit().getImage(imgFN);
  }

  public Image getImg() {
    return _i;
  }

}

class actor extends Point{
  String  _name;      // actor's name
  int     _status;    // status
  int     _index;     // index to actor's image
  Boolean _collision; // actor is collidable?
  Boolean _pushable;  // actor can be pushed?

  actor() {
    //empty constructor
  }

  actor(String name) {
    _name = name;
  }

  actor(String n, Point p, int i, Boolean c, Boolean u) {
    //System.out.println("New Actor: " + n + " | " + p + " | " + i + " | " + c + " | " + u);
    setup(n,p,i,c,u);
  }

  public void setup(String n, Point p, int i, Boolean c, Boolean u) {
    setLocation(p);
    _name      = n;
    _index     = i;
    _collision = c;
    _pushable  = u;
  }

  public Boolean getCollision() { return _collision; }
  public void    setCollision(Boolean c) { _collision = c; }

  public Boolean getPushable() { return _pushable; }
  public void    setPushable(Boolean u) { _pushable = u; }

  public int getI() {
    //System.out.println("getI(): " + _index);
    return _index;
  }

}

class sokoBoard {
  int   _b[][];
  int   ROWS;
  int   COLS;
  Point _a[];

  sokoBoard() {
    _b = readB();
    ROWS = _b.length;
    COLS = _b[0].length;
    _a = findActors();
  }

  public Point[] getA() {
    return _a;
  }

  public Boolean tryMove(actor a, Point offset) {
    int _x = (int)a.getX();
    int _y = (int)a.getY();
    int _d = getV(_x+(int)offset.getX(),_y+(int)offset.getY() );
    if( (_d & 0x01) == 0x01 ) { return false; }
    return true;
  }

  public void moveActor(actor a, Point offset) {
    a.translate( (int)offset.getX(),(int)offset.getY() );
  }


  private int[][] readB() {
    int[][] _read = { {0,0,0,0,0,0,0,0,0,0,0},
                      {0,0,1,1,1,1,1,1,1,0,0},
                      {0,1,0,0,0,0,0,0,0,1,0},
                      {0,1,0,2,8,0,8,2,0,1,0},
                      {0,1,0,2,8,4,8,2,0,1,0},
                      {0,1,0,2,8,0,8,2,0,1,0},
                      {0,1,0,0,0,0,0,0,0,1,0},
                      {0,0,1,1,1,1,1,1,1,0,0},
                      {0,0,0,0,0,0,0,0,0,0,0} };
    return _read;
  }

  public Point findV(int _v) {
    for(int r=0;r<ROWS;r++) {
      for(int c=0;c<COLS;c++) {
        if(_b[r][c]==_v) {
          return(new Point(c,r));
        }
      }
    }
    return null;
  }

  public int countV(int _v) {
    int retval = 0;
    for(int r=0;r<ROWS;r++) {
      for(int c=0;c<COLS;c++) {
        if(_b[r][c]==_v) { retval++; }
      }
    }
    return(retval);
  }

  public Point[] findActors() {
    int a = 0;
    Point[] p = new Point[countActors()];
    for(int r=0;r<ROWS;r++) {
      for(int c=0;c<COLS;c++) {
        if(_b[r][c]>1) { p[a++]=new Point(c,r); }
      }
    }
    return p;
  }

  public int countActors() {
    int retval = 0;
    for(int r=0;r<ROWS;r++) {
      for(int c=0;c<COLS;c++) {
        if(_b[r][c]>1) { retval++; }
      }
    }
    return(retval);
  }

  public int getV(int _c, int _r) {
    return _b[_r][_c];
  }
  public int getV(Point _p) {
    int _r = (int)_p.getY(); int _c = (int)_p.getX();
    return _b[_r][_c];
  }

  public void setV(int _c, int _r, int _v) {
    _b[_r][_c] = _v;
  }
  public void setV(Point _p, int _v) {
    int _r = (int)_p.getY(); int _c = (int)_p.getX();
    _b[_r][_c] = _v;
  }

  public void transV(int _c, int _r, int _v) {
    _b[_r][_c] += _v;
  }
  
  public int rows() { return ROWS; }
  public int cols() { return COLS; }

  public void dumpBoard() {
    System.out.println();
    for(int r=0;r<ROWS;r++) {
      for(int c=0;c<COLS;c++) {
        System.out.print(_b[r][c] + " ");
      }
      System.out.println();
    }
  }

}
