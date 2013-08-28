package com.example.friendlycheckers;

import com.example.friendlycheckers.MainPage.GameState;

import android.graphics.Canvas;
import android.graphics.Color;

public class GamePage
{
        public static Color DarkRed;
        public static Color DarkGrey;
        public static Color HighlightRed;
        public static Color HighlightGrey;
        private static Color Brown;
        private static Color Sand;

        private static int checkerX, checkerY;
        public static int computerDelay = 400;
        private static Canvas mainCanvas;
        private static GameLogic logic;
        private static BoardSpace[][] spaces;

        private static Boolean wait_for_timer = false, player_turn = true, wait_for_computer = false, used_make_move = false, multi_jump = false;
        private static int row_W = 8;
        private static DispatcherTimer TURN_TIMER, COMPUTER_DELAY;
        private static GameState game_type;

        public GamePage()
        {
            InitializeColors();
            checkerX = checkerY = -1;

            if (MainPage.game_state == GameState.SINGLE_PLAYER)
            {
                Versus.Text = "Player 1 vs. Computer";
                game_type = GameState.SINGLE_PLAYER;
            }
            else
            {
                game_type = GameState.LOCAL_MULTI;
                Bottom.Children.Remove(Make_A_Move);
            }

            COMPUTER_DELAY = new DispatcherTimer();
            COMPUTER_DELAY.Tick += Computer_Delay_Tick;
            COMPUTER_DELAY.Interval = new TimeSpan(0, 0, 0, 0, computerDelay);

            TURN_TIMER = new DispatcherTimer();
            TURN_TIMER.Tick += timerTick;                           // Everytime timer ticks, timer_Tick will be called
            TURN_TIMER.Interval = new TimeSpan(0, 0, 0, 0, 200);  // Timer will tick in 800 milliseconds. This is the wait between moves.

            mainCanvas = new Canvas();
            mainCanvas.Width = MainPage.w;
            mainCanvas.Height = MainPage.h;
            Board.Children.Add(mainCanvas);
            createBoard();
            createPieces();
        }
        private void InitializeColors()
        {
            HighlightRed = new Color();
            HighlightRed.R = 255;
            HighlightRed.G = 100;
            HighlightRed.B = 100;
            HighlightRed.A = 255;

            HighlightGrey = new Color();
            HighlightGrey.R = 100;
            HighlightGrey.G = 100;
            HighlightGrey.B = 100;
            HighlightGrey.A = 255;

            DarkRed = new Color();
            DarkRed.R = 50;
            DarkRed.G = 0;
            DarkRed.B = 0;
            DarkRed.A = 255;

            DarkGrey = new Color();
            DarkGrey.R = DarkGrey.G = DarkGrey.B = 20;
            DarkGrey.A = 255;

            Brown = new Color();
            Brown.R = 120;
            Brown.G = 40;
            Brown.B = 10;
            Brown.A = 255;

            Sand = new Color();
            Sand.R = 200;
            Sand.G = 180;
            Sand.B = 90;
            Sand.A = 255;
        }
        private void createPieces()
        {
            logic = new GameLogic(row_W, row_W, MainPage.FORCE_JUMP);
            int row = 0, col = 0;
            for (int k = 0; k < 24; k++)
            {
                row = (k / 4) + ((k >= 12) ? 2 : 0);
                col = 2 * (k % 4) + (row % 2 == 0 ? 0 : 1);
                Checker c = new Checker(col, row, (k < 12) ? Colors.Red : DarkGrey,
                                                (k < 12) ? DarkRed : Colors.Black);
                Vector vect = new Vector(row, col);
                Piece piece = new Piece((k < 12) ? PieceColor.RED : PieceColor.BLACK, vect, PieceType.REGULAR);
                logic.addPiece(piece);
                spaces[col][row].setChecker(c);
                mainCanvas.Children.Add(spaces[col][row].getChecker().getEl2());
                mainCanvas.Children.Add(spaces[col][row].getChecker().getEl1());
                mainCanvas.Children.Add(spaces[col][row].getChecker().getCrown());
            }
        }
        private void createBoard()
        {
            spaces = new BoardSpace[row_W][row_W];
            int size = 60;
            for (int k = 0; k < row_W; k++)
            {
                for (int i = 0; i < row_W; i++)
                {
                    spaces[k][i] = new BoardSpace(k, i, size, ((i + k) % 2 == 0) ? Sand : Brown, null);
                    mainCanvas.Children.Add(spaces[k][i].getRect());
                }
            }
        }
        private void resetBoard()
        {
            for (int k = 0; k < row_W; k++)
            {
                for (int j = 0; j < row_W; j++)
                {
                    if (spaces[k, j].getChecker() == null) continue;
                    delete(k, j);
                }
            }
            createPieces(); //makes new GameLogic instance
            //Moves.Text = "Moves: 0";
            // WhoseTurn.Text = "Black to move next.";
        }
        private static Checker delete(int x, int y)
        {
            Checker temp = spaces[x][y].getChecker();
            mainCanvas.Children.Remove(temp.getEl2());
            mainCanvas.Children.Remove(temp.getEl1());
            mainCanvas.Children.Remove(temp.getCrown());
            spaces[x][y].setChecker(null);

            return temp;
        }
        //////////
        //// HANDLERS FOR BOARD, PIECES, LOGIC AND HIGHLIGHTING LOCATED BELOW HERE
        //////////
        public static void MakeMove(int boardX, int boardY)
        {
            if (wait_for_timer || !player_turn || wait_for_computer || !canMove()) return;
            if (MainPage.game_state == GameState.OUT_OF_GAME || MainPage.game_state == GameState.END_GAME || (checkerX == -1 && checkerY == -1)) return;

            if (game_type == GameState.SINGLE_PLAYER) player_turn = false;
            wait_for_timer = true;
            Move m;
            try
            {
                PieceColor whoseTurn = logic.whoseMove();
                int locX = checkerX;
                int locY = checkerY;
                // Unhighlight the selected piece
                handleHighlighting(checkerX, checkerY);
                m = logic.makeMove(locY, locX, boardY, boardX);
              
                handleMove(m);
                if (whoseTurn.Equals(logic.whoseMove()))
                {
                    checkerX = boardX;
                    checkerY = boardY;
                    multi_jump = true;
                }

                TURN_TIMER.Start();
            }
            catch (PlayerMustJumpException)
            {
                MessageBox.Show("You must take an available jump!");
                wait_for_timer = false;
                player_turn = true;
            }
            catch (WrongMultiJumpPieceException)
            {
                MessageBox.Show("You must finish the multijump!");
                wait_for_timer = false;
                player_turn = true;
            }
            catch (InvalidMoveException)
            {
                System.Diagnostics.Debug.WriteLine("invalid move");
                wait_for_timer = false;
                player_turn = true;
            }
        }
        private static bool canMove()
        {
            return !(MainPage.game_state == GameState.END_GAME);
        }
        private Boolean checkEndGame()
        {
            GameStatus status = logic.getGameStatus();
            if (status == GameStatus.BLACKWINS)
            {
                WhoseTurn.Text = "Black player wins!";
                System.Diagnostics.Debug.WriteLine("game status is BLACKWINS");
                return true;
            }
            else if (status == GameStatus.REDWINS)
            {
                WhoseTurn.Text = "Red player wins!";
                System.Diagnostics.Debug.WriteLine("game status is REDWINS");
                return true;
            }
            else if (status == GameStatus.DRAW)
            {
                WhoseTurn.Text = "Game is a draw.";
                System.Diagnostics.Debug.WriteLine("game status is DRAW");
                return true;
            }
            else
            {
                System.Diagnostics.Debug.WriteLine("game status is NOWINNER");
                return false;
            }
        }
        private void Make_Educated_Move(object sender, EventArgs e) // object sender, EventArgs e
        {
            if (!canMove() || !player_turn || (wait_for_computer && sender.Equals(Make_A_Move))) return;
            if(sender.Equals(Make_A_Move))used_make_move = true;

            if (game_type == GameState.SINGLE_PLAYER) player_turn = false;
            wait_for_timer = true;
            PieceColor whoseMove = logic.whoseMove();
            MoveAttempt a;
            if (MainPage.DIFFICULT && wait_for_computer)
                a = logic.getHardMove();
            else
                a = logic.getEasyMove();

            Move m = logic.makeMove(a);
            handleMove(m);
            if (!TURN_TIMER.IsEnabled)
                TURN_TIMER.Start();

            if (whoseMove.Equals(logic.whoseMove()))
            {
                checkerX = a.getXEnd();
                checkerY = a.getYEnd();
                multi_jump = true;
            }
        }
        private void timerTick(object o, EventArgs e)
        {
            TURN_TIMER.Stop();
            if (checkEndGame())
            {
                MainPage.game_state = GameState.END_GAME;
                return;
            }
            PieceColor last = logic.getWhoMovedLast();

            // if there is a double jump available and it isn't forced
            if (multi_jump)
            {
                // if black is making the jump
                if (!last.Equals(PieceColor.RED) || (MainPage.game_state != GameState.SINGLE_PLAYER))
                {
                    if (used_make_move)
                        Make_Educated_Move(o, e);
                    else
                    {
                        if (!MainPage.FORCE_JUMP)
                        {
                            if (MessageBox.Show("Double Jump Available!", "Take the double jump?", MessageBoxButton.OKCancel) == MessageBoxResult.Cancel)
                            {
                                logic.skipMultiJump();
                                multi_jump = false;
                            }
                            else
                            {
                                int x = checkerX;
                                int y = checkerY;
                                checkerX = checkerY = -1;
                                handleHighlighting(x, y);
                            }
                        }
                    }
                }
                else
                {
                    if (MainPage.game_state == GameState.SINGLE_PLAYER)
                        COMPUTER_DELAY.Start();
                }
            }
            else
            {
                used_make_move = false;
                checkerX = checkerY = -1;
            }
            multi_jump = false;
            WhoseTurn.Text = (logic.whoseMove().Equals(PieceColor.RED) ? "Red" : "Black") + " to move next.";
            Moves.Text = "Moves: "+logic.getMoveNumber();
            wait_for_timer = false;
            player_turn = true;
            if (MainPage.game_state == GameState.SINGLE_PLAYER)
            {
                //MessageBox.Show("Computer's turn.");
                wait_for_computer = !wait_for_computer;
                if (wait_for_computer && logic.whoseMove().Equals(PieceColor.RED))
                    COMPUTER_DELAY.Start();
                else
                    wait_for_computer = false;
            }
        }
        private void Computer_Delay_Tick(object o, EventArgs e)
        {
            COMPUTER_DELAY.Stop();
            Make_Educated_Move(o, e);
        }
        private static void handleMove(Move move)
        {
            List<Piece> added = move.getAdditions();
            List<Piece> removed = move.getRemovals();

            foreach (Piece p in removed)
            {
                Vector co = p.getCoordinates();
                delete(co.getX(), co.getY());
            }

            foreach (Piece p in added)
            {
                Vector co = p.getCoordinates();

                int col = co.getX();
                int row = co.getY();
                Checker c = new Checker(col, row, p.getColor() == PieceColor.BLACK ? DarkGrey : Colors.Red,
                                                 p.getColor() == PieceColor.BLACK ? Colors.Black : DarkRed);
                spaces[col, row].setChecker(c);
                mainCanvas.Children.Add(spaces[col, row].getChecker().getEl2());
                mainCanvas.Children.Add(spaces[col, row].getChecker().getEl1());
                mainCanvas.Children.Add(spaces[col, row].getChecker().getCrown());

                if (p.getType() == PieceType.KING)
                    c.king();
            }
        }
        public static void handleHighlighting(int x, int y)
        {
            if ((wait_for_timer && !multi_jump) || wait_for_computer || MainPage.game_state == GameState.END_GAME || !canMove()) return;
            if (!logic.isSelectable(y, x)) return;

            Checker HIGHLIGHTED_PIECE = spaces[x, y].getChecker();
            if (checkerX != -1 && checkerY != -1)
                spaces[checkerX, checkerY].getChecker().toggleHighlight();

            //if the already highlighted piece is the same as the one being clicked
            if (checkerX != -1 && checkerY != -1 && HIGHLIGHTED_PIECE.Equals(spaces[checkerX, checkerY].getChecker()))
            {
                checkerX = checkerY = -1;
                return;
            }
            else //otherwise, a piece is either being clicked for the first time or is switching highlights.
            {
                checkerX = x;
                checkerY = y;
                HIGHLIGHTED_PIECE.toggleHighlight();
            }
        }
        public void Menu_Setup(object sender, EventArgs e)
        {
            OnBackKeyPress(null);
        }
        protected override void OnBackKeyPress(System.ComponentModel.CancelEventArgs e)
        {
            if (canMove() && MessageBox.Show("The current game will end.", "Exit to main menu?", MessageBoxButton.OKCancel) == MessageBoxResult.Cancel)
            {
                if(e!=null)e.Cancel = true;
                return;
            }
            else
            {
                TURN_TIMER.Stop();
                COMPUTER_DELAY.Stop();
                wait_for_computer = false;
                wait_for_timer = false;
                player_turn = true;
                NavigationService.GoBack();
                MainPage.game_state = GameState.MENU;
            }
        }

    }
    public class BoardSpace
    {
        private Rectangle space;
        private Checker checker;
        private int gridx, gridy, size;
        private Color color;

        public BoardSpace(int x, int y, int size, Color c, Checker checker)
        {
            this.gridx = x;
            this.gridy = y;
            this.color = c;
            this.size = size;
            this.checker = checker;

            space = new Rectangle();
            space.Width = size;
            space.Height = size;
            space.MinWidth = size;
            space.MinHeight = size;
            space.MouseLeftButtonUp += Space_Action;
            space.Fill = new SolidColorBrush(color);
            int lm = (x * size) - 40;
            int tm = (y * size) - 150;
            space.Margin = new Thickness(lm, tm, MainPage.w - lm, MainPage.h - tm);
            if (checker != null)
                this.checker.setMargin(space.Margin);
        }
        public void setChecker(Checker c)
        {
            this.checker = c;
            if (c == null) return;
            checker.setMargin(space.Margin);
        }
        public Checker getChecker() { return checker; }
        public int getX() { return this.gridx; }
        public int getY() { return this.gridy; }
        public Rectangle getRect() { return space; }
        public Color getColor() { return color; }
        public void setColor(Color c)
        {
            this.color = c;
            space.Fill = new SolidColorBrush(c);
        }
        private void Space_Action(object o, MouseButtonEventArgs e)
        {
           GamePage.MakeMove(gridx, gridy);
        }
    }
    public class Checker
    {
        private Ellipse el1, el2;
        private Image crown;
        private int width, x, y;
        private Color color, bg, high;
        private bool col, lit;
        public Checker(int x, int y, Color color, Color bg)
        {
            el1 = new Ellipse();
            el2 = new Ellipse();
            crown = new Image();
            BitmapImage bi = new BitmapImage();
            bi.UriSource = new Uri("crown.png", UriKind.Relative);
            crown.Source = bi;
            crown.Visibility = Visibility.Collapsed;
            crown.MouseLeftButtonUp += ellipse_MouseUp;

            this.x = x;
            this.y = y;
            this.width = 55;
            this.lit = false;
            this.color = color;
            this.col = (color.Equals(GamePage.DarkGrey) ? false : true);
            this.bg = bg;
            this.high = !col ? GamePage.HighlightGrey : GamePage.HighlightRed;

            el1.Width = width;
            el1.MinWidth = width;
            el1.Height = width;
            el1.MinHeight = width;
            el1.Fill = new SolidColorBrush(color);

            el2.Width = width;
            el2.MinWidth = width;
            el2.Height = width;
            el2.MinHeight = width;
            el2.Fill = new SolidColorBrush(bg);
            el1.MouseLeftButtonUp += ellipse_MouseUp;
            el2.MouseLeftButtonUp += ellipse_MouseUp;
        }
        public void setMargin(Thickness t)
        {
            Thickness ct = new Thickness(t.Left + 2, t.Top + 2, t.Right - 2, t.Bottom - 2);
            this.el1.Margin = ct;
            this.crown.Margin = ct;
            Thickness ct2 = new Thickness(t.Left + 4, t.Top + 4, t.Right - 4, t.Bottom - 4);
            this.el2.Margin = ct2;
        }
        public Ellipse getEl1() { return el1; }
        public Ellipse getEl2() { return el2; }
        public Image getCrown() { return crown; }
        public int getX() { return this.x; }
        public int getY() { return this.y; }
        public Color getColor() { return this.color; }
        public Color getBG() { return this.bg; }
        public void king()
        {
            crown.Visibility = Visibility.Visible;
        }
        public Boolean Equals(Checker c) { return (c.getX() == this.getX() && c.getY() == this.getY()); }
        public void toggleHighlight()
        {
            if (!lit)
                el1.Fill = new SolidColorBrush(high);
            else
                el1.Fill = new SolidColorBrush(color);
            lit = !lit;
        }
        private void ellipse_MouseUp(object sender, MouseButtonEventArgs e)
        {
            if (MainPage.game_state == GameState.OUT_OF_GAME) return;
            GamePage.handleHighlighting(x, y);
        }
    }
}