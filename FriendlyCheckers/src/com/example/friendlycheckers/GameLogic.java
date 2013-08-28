import java.util.ArrayList;

using System;
using System.Collections.Generic;

    public class GameLogicException : System.Exception { }
    public class CellAlreadyFilledException : GameLogicException { }
    public class CellEmptyException : GameLogicException { }
    public class CellFullException : GameLogicException { }
    public class PieceWrongColorException : GameLogicException { }
    public class UnreachableCodeException : GameLogicException { }
    public class CellOutOfBoundsException : GameLogicException { }
    public class InvalidMoveException : GameLogicException { }
    public class PlayerMustJumpException : InvalidMoveException { }
    public class WrongMultiJumpPieceException : InvalidMoveException {} 
    public class BadMoveNumberException : GameLogicException { }
    public class NoMovesLeftException : GameLogicException { }

    public enum PieceColor {RED, BLACK};
    public enum PieceType {REGULAR, KING};
    public enum GameStatus {NOWINNER, REDWINS, BLACKWINS, DRAW};

    public class GameLogic {
        Board board; 
        int moveNumber;
        bool forceJumps;
        int blackPieces = 0;
        int redPieces = 0; 
        int turnNumber = 0; 
        List<Move> movesMade;
        List<MoveAttempt> successMoves;
        PieceColor lastPlayer; 

        int lastAdvantage = 0; //the turnNumber of the last move that either
                               //made a king or took a piece.

        Vector multiJumpLoc = null; 

        static Vector [] kingMoves = new Vector[4]{new Vector(1,1), new Vector(1,-1), new Vector(-1,-1), new Vector(-1,1)}; // kings move anywhere
        static Vector [] blackMoves = new Vector[2]{new Vector(-1,1), new Vector(-1,-1)}; // black moves up
        static Vector [] redMoves = new Vector[2]{new Vector(1,1), new Vector(1,-1)}; // red moves down

        static Vector [] kingJumps = new Vector[4]{new Vector(2,2), new Vector(2,-2), new Vector(-2,-2), new Vector(-2,2)}; // kings move anywhere
        static Vector [] blackJumps = new Vector[2]{new Vector(-2,2), new Vector(-2,-2)}; // black moves up
        static Vector [] redJumps = new Vector[2]{new Vector(2,2), new Vector(2,-2)}; // red moves down

        public static PieceColor getOppositeColor(PieceColor color){ 
            if (color == PieceColor.BLACK) { 
                return PieceColor.RED; 
            } else if(color == PieceColor.RED) { 
                return PieceColor.BLACK; 
            } else {
                throw new UnreachableCodeException(); 
            }
        }
        public PieceColor getWhoMovedLast() { return this.lastPlayer; }
        public static Vector[] getPossibleJumps(PieceColor color, PieceType type) {
            if (type == PieceType.KING) {
                return kingJumps;
            } else if (type == PieceType.REGULAR) {
                if (color == PieceColor.BLACK) {
                    return blackJumps;
                } else if (color == PieceColor.RED) {
                    return redJumps;
                } else {
                    throw new UnreachableCodeException();
                }
            } else {
                throw new UnreachableCodeException();
            }
        }

        public static Vector[] getPossibleMoves(PieceColor color, PieceType type) {
            if (type == PieceType.KING) {
                return kingMoves;
            } else if (type == PieceType.REGULAR) {
                if (color == PieceColor.BLACK) {
                    return blackMoves;
                } else if (color == PieceColor.RED) {
                    return redMoves;
                } else {
                    throw new UnreachableCodeException();
                }
            } else {
                throw new UnreachableCodeException();
            }
        }

        public int getMoveNumber() {
            return moveNumber;
        }

        public int getTurnNumber() { 
            return turnNumber; 
        }

        public GameLogic(int boardHeight, int boardWidth) : this 
            (boardHeight, boardWidth, false){}

        public GameLogic(int boardHeight, int boardWidth, bool forceJumps) {
            this.forceJumps = forceJumps; 
            this.board = new Board(boardWidth, boardHeight);
            moveNumber = 0;
            turnNumber = 0;
            movesMade = new List<Move>();
            successMoves = new List<MoveAttempt>(); 
        }

        public GameLogic(GameLogic g) { //does a deep copy of GameLogic
            this.forceJumps = g.forceJumps;
            this.board = g.getBoardCopy();
            this.moveNumber = g.moveNumber;
            this.blackPieces = g.blackPieces;
            this.redPieces = g.redPieces;
            if (multiJumpLoc != null) {
                this.multiJumpLoc = new Vector(g.multiJumpLoc);
            } else {
                this.multiJumpLoc = null;
            }
            this.turnNumber = g.turnNumber; 
        }

        public Board getBoardCopy() {
            return Board.deepCopy(board);
        }

        public string getBoardText() {
            string t = "";
            for (int y = 0; y < board.getHeight(); y++) {
                for (int x = 0; x < board.getWidth(); x++) {
                    Piece p = board.getCellContents(y, x);
                    if (p == null) {
                        t += "n";
                    } else {
                        if (p.getColor() == PieceColor.BLACK) {
                            t += "b";
                        } else {
                            t += "r";
                        }
                    }
                }
                t += "\n"; 
            }
            return t;
        }

        public void removePiece(Piece p) {
            if (p.getColor() == PieceColor.BLACK) {
                this.blackPieces--;
            } else {
                this.redPieces--;
            }
            board.removePieceFromCell(p);
        }

        public void addPiece(Piece p){
            if (p.getColor() == PieceColor.BLACK) {
                this.blackPieces++;
            } else {
                this.redPieces++;
            }
            board.addPieceToCell(p); 
        }

        public bool isSelectable(int y, int x) {
            System.Diagnostics.Debug.WriteLine("y is " + y + " and x is " + x); 
            Piece p = board.getCellContents(y, x);
            System.Diagnostics.Debug.WriteLine("piece is "+p.ToString()); 
            return p.getColor() == this.whoseMove(); 
        }


        public PieceColor whoseMove() {
            if (turnNumber % 2 == 1) {
                return PieceColor.RED;
            } else {
                return PieceColor.BLACK;
            }
        }

        public void skipMultiJump() {
            if (this.forceJumps) {
                throw new PlayerMustJumpException();
            } else {
                turnNumber++;
                multiJumpLoc = null;
            }
        }

        public Piece givePieceNewLocationKingCheck(Piece currentP, Vector newLoc) { 
            Piece newP = currentP.newLocation(newLoc);
            if (newLoc.getY() == 0 && newP.getColor() == PieceColor.BLACK) {
                newP = newP.newType(PieceType.KING);
            } else if (newLoc.getY() == board.getHeight() - 1 && newP.getColor() == PieceColor.RED) {
                newP = newP.newType(PieceType.KING);
            }
            return newP; 
        }

        public GameStatus getGameStatus() { 

            System.Diagnostics.Debug.WriteLine("redPieces: " + redPieces + ". blackPieces: " + blackPieces); 

            if (turnNumber - lastAdvantage >= 40) {
                return GameStatus.DRAW;
            }

            if (!canJumpSomewhere() && !canMoveSomewhere()) {
                if (whoseMove() == PieceColor.BLACK) {
                    if (redPieces > 0) {
                        return GameStatus.REDWINS;
                    }
                } else {
                    if (blackPieces > 0) {
                        return GameStatus.BLACKWINS;
                    }
                }
            }
            if (blackPieces > 0) {
                if (redPieces == 0) {
                    return GameStatus.BLACKWINS;
                }
                return GameStatus.NOWINNER;
            } else if (redPieces > 0) {
                if (blackPieces == 0) {
                    return GameStatus.REDWINS;
                }
                return GameStatus.NOWINNER;
            }
            return GameStatus.DRAW;
        }

        public static List<List<MoveAttempt>> getFullTurns(List<MoveAttempt> possibility, GameLogic g) { 
            List<List<MoveAttempt>> fullturns = new List<List<MoveAttempt>>(); 
            g.makeMove(possibility[possibility.Count-1]); 
            if(g.multiJumpLoc == null) { 
                fullturns.Add(possibility);
            } else { 
                foreach(Vector v in g.getDoableJumps(g.board.getCellContents(g.multiJumpLoc))) { 
                    MoveAttempt move = new MoveAttempt(v, g.board.getCellContents(g.multiJumpLoc)); 
                    List<MoveAttempt> p = new List<MoveAttempt>(); 
                    p.AddRange(possibility); 
                    p.Add(move); 
                    GameLogic newG = new GameLogic(g);
                    g.makeMove(move); 
                    fullturns.AddRange(getFullTurns(p, newG)); 
                }
            }
            return fullturns; 
        }


        private static double getOptimizedHeuristic(int depth, int maxDepth, GameLogic g) {
            if (depth == maxDepth || g.getGameStatus() != GameStatus.NOWINNER) {
                return g.calculateHeuristic();
            } else {
                PieceColor currentPlayer = g.whoseMove();
                List<MoveAttempt> starting = g.getAllDoableMoveJumpAttempts();
                List<List<MoveAttempt>> poss = new List<List<MoveAttempt>>(); 
                foreach (MoveAttempt possibility in starting) {
                    List<MoveAttempt> p = new List<MoveAttempt>(); 
                    p.Add(possibility); 
                    foreach(List<MoveAttempt> fullturn in getFullTurns(p, new GameLogic(g))) { 
                        poss.Add(fullturn); 
                    }
                }

            }
            return .345;
        }

        private List<Vector> getDoableMoves(Piece p) {
            Vector[] moves = getPossibleMoves(p.getColor(), p.getType());
            List<Vector> doable = new List<Vector>();

            foreach (Vector move in moves) {
                Vector endLoc = move.add(p.getCoordinates());
                Piece endP;
                try {
                    endP = board.getCellContents(endLoc);
                } catch (CellOutOfBoundsException) {
                    continue;
                }
                if (endP != null) {
                    continue;
                }
                doable.Add(new Vector(move));
            }

            return doable;
        }

        private List<Vector> getDoableJumps(Piece p) { 
            Vector[] jumps = getPossibleJumps(p.getColor(), p.getType());
            List<Vector> doable = new List<Vector>();

            foreach (Vector jump in jumps) {
                Vector endLoc = jump.add(p.getCoordinates());
                Piece endP;
                try {
                    endP = board.getCellContents(endLoc);
                } catch (CellOutOfBoundsException) {
                    continue;
                }
                if (endP != null) {
                    continue;
                }
                Vector middleLoc = p.getCoordinates().add(jump.divideVector(2));
                Piece middleP; 
                try {
                    middleP = board.getCellContents(middleLoc);
                } catch (CellOutOfBoundsException) {
                    continue;
                }
                if (middleP == null) {
                    continue;
                }

                if (middleP.getColor() == p.getColor()) {
                    continue;
                }
                doable.Add(new Vector(jump)); 
            }

            return doable; 
        }

        public double calculateHeuristic() { //in terms of player who made last move
            GameStatus status = getGameStatus();
            if (status == GameStatus.DRAW) {
                return 0.5;
            } else if (status == GameStatus.BLACKWINS) {
                if (whoseMove() == PieceColor.BLACK) {
                    return 0.0;
                } else {
                    return 1.0;
                }
            } else if (status == GameStatus.REDWINS) {
                if (whoseMove() == PieceColor.RED) {
                    return 0.0;
                } else {
                    return 1.0;
                }
            } else if (status == GameStatus.NOWINNER) {
                double his;
                double mine;
                if (whoseMove() == PieceColor.BLACK) {
                    mine = (double)redPieces;
                    his = (double)blackPieces;
                } else {
                    mine = (double)blackPieces;
                    his = (double)redPieces;
                }
                return mine / (mine + his);
            } else {
                throw new UnreachableCodeException();
            }
        }

        public MoveAttempt getEasyMove() {
            List<MoveAttempt> ms = this.getAllDoableMoveJumpAttempts();
            Random r = new Random();
            MoveAttempt m = ms[r.Next(0,ms.Count)];

            return m;

            //if (!forceJumps && (m = this.getRandomDoableMoveAttempt()) != null) {
            //    return m;
            //} else {
            //    return getRandomDoableMoveJump();
            //}
        }
        public MoveAttempt getHardMove() {
            return getRandomDoableMoveJump();
        }

        public MoveAttempt getAnyDoableMoveJump() {
            if (multiJumpLoc != null) {
                Vector doable = getDoableJumps(board.getCellContents(multiJumpLoc))[0];
                return new MoveAttempt(multiJumpLoc.getY(), multiJumpLoc.getX(),
                    multiJumpLoc.getY() + doable.getY(), multiJumpLoc.getX() + doable.getX()); 
            }
            MoveAttempt jump = getAnyDoableJumpAttempt();
            if (jump != null) {
                return jump;
            }
            MoveAttempt move = getAnyDoableMoveAttempt();
            if (move != null) {
                return move;
            }
            throw new NoMovesLeftException(); 
        }
        public MoveAttempt getRandomDoableMoveJump()
        {
            if (multiJumpLoc != null)
            {
                Vector doable = getDoableJumps(board.getCellContents(multiJumpLoc))[0];
                return new MoveAttempt(multiJumpLoc.getY(), multiJumpLoc.getX(),
                    multiJumpLoc.getY() + doable.getY(), multiJumpLoc.getX() + doable.getX());
            }
            MoveAttempt jump = getRandomDoableJumpAttempt();
            if (jump != null)
            {
                return jump;
            }
            MoveAttempt move = getRandomDoableMoveAttempt();
            if (move != null)
            {
                return move;
            }
            throw new NoMovesLeftException();
        }

        public MoveAttempt getAnyDoableMoveAttempt() {

            if (multiJumpLoc != null) {
                return null;
            }

            PieceColor jumperColor = this.whoseMove();
            for (int y = 0; y < board.getHeight(); y++) {
                for (int x = 0; x < board.getHeight(); x++) {
                    Piece p = board.getCellContents(y, x);
                    if (p == null) {
                        continue;
                    }
                    if (p.getColor() != jumperColor) {
                        continue;
                    }
                    List<Vector> vectors = getDoableMoves(p); 
                    if (vectors.Count > 0) {
                        System.Diagnostics.Debug.WriteLine("can jump somewhere returning moveattempt for" +y+" "+x);
                        return new MoveAttempt(y, x, y+vectors[0].getY(), x+vectors[0].getX());
                    }
                }
            }
            System.Diagnostics.Debug.WriteLine("can move somewhere returning false");
            return null;
        }

        private bool canMoveSomewhere() {
            return getAnyDoableMoveAttempt() != null;
        }

        private bool canJumpSomewhere() {
            return getAnyDoableJumpAttempt() != null;
        }

        private bool moveIsJump(Vector start, int yEnd, int xEnd) {
            return (Math.Abs(start.getX() - xEnd) == 2 && Math.Abs(start.getY() - yEnd) == 2);
        }
        public List<MoveAttempt> getAllDoableMoveAttempts()
        {
            
            PieceColor jumperColor = this.whoseMove();
            List<MoveAttempt> allMoves = new List<MoveAttempt>();
            if (multiJumpLoc != null) {
                return allMoves;
            }
            for (int y = 0; y < board.getHeight(); y++)
            {
                for (int x = 0; x < board.getHeight(); x++)
                {
                    Piece p = board.getCellContents(y, x);
                    if (p == null || (p.getColor() != jumperColor)) continue;
                    List<Vector> doable = getDoableMoves(p);
                    if (doable != null && doable.Count != 0)
                    {
                        foreach (Vector v in doable)
                            allMoves.Add(new MoveAttempt(y, x, y + v.getY(), x + v.getX()));
                    }
                }
            }
            System.Diagnostics.Debug.WriteLine("all moves returning" + ((allMoves.Count > 0) ? "true" : "false"));
            return allMoves;
        }
        public List<MoveAttempt> getAllDoableJumpAttempts()
        {
            if (multiJumpLoc != null) {
                List<MoveAttempt> moves = new List<MoveAttempt>();
                foreach (Vector v in getDoableJumps(board.getCellContents(multiJumpLoc))) {
                    moves.Add(new MoveAttempt(v, board.getCellContents(multiJumpLoc)));
                }
                return moves;
            }
            PieceColor jumperColor = this.whoseMove();
            List<MoveAttempt> allMoves = new List<MoveAttempt>();
            for (int y = 0; y < board.getHeight(); y++)
            {
                for (int x = 0; x < board.getHeight(); x++)
                {
                    Piece p = board.getCellContents(y, x);
                    if (p == null || (p.getColor() != jumperColor)) continue;
                    List<Vector> doable = getDoableJumps(p);
                    if (doable == null || doable.Count == 0) continue;
                    foreach (Vector v in doable)
                        allMoves.Add(new MoveAttempt(y, x, y + v.getY(), x + v.getX()));
                }
            }
            System.Diagnostics.Debug.WriteLine("all jumps returning" + ((allMoves.Count > 0) ? "true" : "false"));
            return allMoves;
        }
        public List<MoveAttempt> getAllDoableMoveJumpAttempts()
        {
            List<MoveAttempt> moves;
            if (multiJumpLoc != null)
            {
                moves = new List<MoveAttempt>();
                foreach (Vector v in getDoableJumps(board.getCellContents(multiJumpLoc)))
                {
                    moves.Add(new MoveAttempt(v, board.getCellContents(multiJumpLoc)));
                }
                return moves;
            }

            PieceColor jumperColor = this.whoseMove();
            List<MoveAttempt> allMoves = new List<MoveAttempt>();
            List<MoveAttempt> jumps = getAllDoableJumpAttempts();
            moves = getAllDoableMoveAttempts();

            foreach (MoveAttempt move in jumps)
                allMoves.Add(move);
            if((forceJumps && allMoves.Count == 0) || (!this.forceJumps))
                foreach (MoveAttempt move in moves)
                    allMoves.Add(move);

            System.Diagnostics.Debug.WriteLine("all doable moves jumps returning " + (allMoves.Count > 0 ? "true" : "false"));
            return allMoves;
        }
        public MoveAttempt getRandomDoableMoveAttempt()
        {
            List<MoveAttempt> moves = getAllDoableMoveAttempts();
            Random random = new Random();
            int rand = random.Next(0, moves.Count);
            return (moves.Count==0 ? null : moves[rand]);
        }
        public MoveAttempt getRandomDoableJumpAttempt()
        {
            List<MoveAttempt> jumps = getAllDoableJumpAttempts();
            Random random = new Random();
            int rand = random.Next(0, jumps.Count);
            return (jumps.Count==0? null : jumps[rand]);
        }
        public MoveAttempt getAnyDoableJumpAttempt() {
            if (multiJumpLoc != null) {
                return new MoveAttempt(getDoableJumps(board.getCellContents(multiJumpLoc))[0], board.getCellContents(multiJumpLoc));
            }
            PieceColor jumperColor = this.whoseMove();
            for (int y = 0; y < board.getHeight(); y++) {
                for (int x = 0; x < board.getHeight(); x++) {
                    Piece p = board.getCellContents(y, x);
                    if (p == null) {
                        continue;
                    }
                    if (p.getColor() != jumperColor) {
                        continue;
                    }
                    List<Vector> doable = getDoableJumps(p);
                    if (doable.Count > 0) {
                        System.Diagnostics.Debug.WriteLine("can jump somewhere returning true. " + y + " " + x);
                        return new MoveAttempt(y, x, y + doable[0].getY(), x + doable[0].getX()); 
                    }
                }
            }
            System.Diagnostics.Debug.WriteLine("can jump somewhere returning false");
            return null;
        }


        public Move makeMove(MoveAttempt a) {
            return makeMove(a.getYStart(), a.getXStart(), a.getYEnd(), a.getXEnd());
        }

        public Move makeMove(int yStart, int xStart, int yEnd, int xEnd) {
            Piece start = board.getCellContents(yStart, xStart);
            Piece end = board.getCellContents(yEnd, xEnd);

            if (start == null) { //there is no piece here
                throw new CellEmptyException();
            }
            if (end != null) {
                throw new CellFullException();
            }
            if (start.getColor() != whoseMove()) {
                throw new PieceWrongColorException();
            }

            PieceType originalPieceType = start.getType();

            System.Diagnostics.Debug.WriteLine("makeMove called");
            Move myMove = getMove(start, yEnd, xEnd, this.whoseMove());

            doMove(myMove);
            successMoves.Add(new MoveAttempt(yStart, xStart, yEnd, xEnd));
            movesMade.Add(myMove); 

            Piece add = myMove.getAdditions()[0];
            if (originalPieceType != add.getType()) {
                lastAdvantage = turnNumber;
            }
            if (myMove.getRemovals().Count > 1) { //that means a piece has been taken
                lastAdvantage = turnNumber;
            }

            if (originalPieceType == add.getType() && myMove.getRemovals().Count == 2 && getDoableJumps(add).Count != 0) {
                this.multiJumpLoc = myMove.getAdditions()[0].getCoordinates();
                //don't change turnNumber
            } else {
                this.turnNumber++;
                this.multiJumpLoc = null;
            }
            this.moveNumber++;
            //getOptimizedHeuristic(0, 3, new GameLogic(this));
            return myMove;
        }

        private void doMove(Move move) {
            lastPlayer = move.getPlayer(); 
            foreach (Piece removal in move.getRemovals()) {
                removePiece(removal);
            }
            foreach (Piece addition in move.getAdditions()) {
                addPiece(addition);
            }
        }

        public List<MoveAttempt> getMoveAttemptsMade() {
            return this.successMoves;
        }

        public void makeMoves(List<Move> moves) {
            PieceColor lastMoveColor = whoseMove();
            foreach (Move move in moves) {
                if (move.getPlayer() != lastMoveColor) {
                    turnNumber++;
                    lastMoveColor = move.getPlayer();
                }
                if (move.getRemovals().Count >= 2 || move.getRemovals()[0].getType() != move.getAdditions()[0].getType()) { 
                    lastAdvantage = turnNumber;
                }
                moveNumber++;
                doMove(move);
            }
        }


        private Move getMove(Piece start, int yEnd, int xEnd, PieceColor player){
            List<Piece> removals = new List<Piece>();
            List<Piece> additions = new List<Piece>(); 

            if(multiJumpLoc != null) { 
                if(multiJumpLoc.Equals(start.getCoordinates()) && moveIsJump(start.getCoordinates(), yEnd, xEnd)) { 
                } else {
                    throw new WrongMultiJumpPieceException();
                }
            }

            System.Diagnostics.Debug.WriteLine("start vector is " + start.getCoordinates().ToString());

            Vector startLoc = start.getCoordinates();
            Vector endLoc = new Vector(yEnd, xEnd);  
            Vector myMove = endLoc.subtract(startLoc);

            //jump logic goes here
            if (this.forceJumps && canJumpSomewhere()) {
                List<Vector> jumps = getDoableJumps(start);
                bool found = false;
                foreach (Vector jump in jumps) {
                    if (jump.Equals(myMove)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new PlayerMustJumpException();
                }
            }

            
            System.Diagnostics.Debug.WriteLine("myMove is " + myMove.ToString()); 
            bool foundValid = false; 

            if(Math.Abs(myMove.getX()) == 1 && Math.Abs(myMove.getY()) == 1) { //move is not a jump
                System.Diagnostics.Debug.WriteLine("Move is not a jump.");
                Vector[] moves = getPossibleMoves(start.getColor(), start.getType());
                foreach(Vector move in moves) {
                    System.Diagnostics.Debug.WriteLine("testing possible move " + move.ToString()); 
                    if (myMove.Equals(move)) {
                        removals.Add(start);
                        additions.Add(givePieceNewLocationKingCheck(start, start.getCoordinates().add(myMove))); 
                        foundValid = true; 
                        break;
                    }
                }
            } else if (Math.Abs(myMove.getX()) == 2 && Math.Abs(myMove.getY()) == 2) { //move is a jump
                Vector[] moves = getPossibleJumps(start.getColor(), start.getType());
                foreach (Vector move in moves) {
                    if (myMove.Equals(move)) {
                        Vector jumpedLoc = start.getCoordinates().add(move.divideVector(2));
                        Piece jumpedPiece = board.getCellContents(jumpedLoc);
                        if (jumpedPiece == null) {
                            System.Diagnostics.Debug.WriteLine("cannot jump an empty square");
                            throw new InvalidMoveException();
                        }
                        if (jumpedPiece.getColor() == getOppositeColor(start.getColor())) {
                            removals.Add(start);
                            removals.Add(jumpedPiece);
                            additions.Add(givePieceNewLocationKingCheck(start, endLoc));
                            foundValid = true;
                        } else {
                            System.Diagnostics.Debug.WriteLine("cannot jump your own piece");
                            throw new InvalidMoveException();
                        }
                        break;
                    }
                }
            } else {
                System.Diagnostics.Debug.WriteLine("vector is wrong length"); 
                throw new InvalidMoveException();
            }

            if (!foundValid) {
                System.Diagnostics.Debug.WriteLine("Could not find match vector");
                throw new InvalidMoveException();
            }
            int myTurnNumber = turnNumber + 1; 
            return new Move(myTurnNumber, removals, additions, player); 
        }
    }
}
