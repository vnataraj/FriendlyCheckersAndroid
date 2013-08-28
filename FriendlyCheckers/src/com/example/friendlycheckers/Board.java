import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException;
import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException.InvalidMoveException.PlayerMustJumpException.WrongMultiJumpPieceException.BadMoveNumberException.NoMovesLeftException.Board;

 public class Board {
        //List<Piece> pieces;
        Cell[][] grid;
        int height;
        int width;

        public int getHeight() {
            return height;
        }
        public int getWidth() {
            return width;
        }

        public static Board deepCopy(Board b) {
            Board newB = new Board(b.getHeight(), b.getWidth());
            for (int y = 0; y < newB.getHeight(); y++) {
                for (int x = 0; x < newB.getWidth(); x++) {
                    Piece p = b.getCellContents(y, x);
                    if (p != null) {
                        newB.addPieceToCell(new Piece(p));
                    }
                }
            }
            return newB;
        }

        public Board(int height, int width) {
            this.grid = new Cell[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    this.grid[y][x] = new Cell();
                }
            }
            this.height = height;
            this.width = width;
        }

        public Piece getCellContents(Vector v) {
            return getCellContents(v.getY(), v.getX());
        }
        public Piece getCellContents(int y, int x) {
            if (!(x < width) || !(y < height) || !(x >= 0) || !(y >= 0)) {
                throw new CellOutOfBoundsException();
            }
            return grid[y,x].getPiece(); 
        }

        public void addPieceToCell(Piece piece) {
            grid[piece.getCoordinates().getY(), piece.getCoordinates().getX()].addPiece(piece);
            //pieces.Add(piece);
        }

        public void removePieceFromCell(Piece piece) {
            grid[piece.getCoordinates().getY(), piece.getCoordinates().getX()].removePiece();
            //pieces.Add(piece);
        }
    }