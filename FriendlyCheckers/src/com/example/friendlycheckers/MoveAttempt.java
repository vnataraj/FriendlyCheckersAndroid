import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException.InvalidMoveException.PlayerMustJumpException.WrongMultiJumpPieceException.BadMoveNumberException.NoMovesLeftException.MoveAttempt;
import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException.InvalidMoveException.PlayerMustJumpException.WrongMultiJumpPieceException.BadMoveNumberException.NoMovesLeftException.Piece;
import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException.InvalidMoveException.PlayerMustJumpException.WrongMultiJumpPieceException.BadMoveNumberException.NoMovesLeftException.Vector;

 public class MoveAttempt {
        int yStart;
        int xStart;
        int yEnd;
        int xEnd;
        public MoveAttempt(int yStart, int xStart, int yEnd, int xEnd) {
            this.yStart = yStart;
            this.xStart = xStart;
            this.yEnd = yEnd;
            this.xEnd = xEnd;
        }
        public MoveAttempt(Vector v, Piece p)
            : this(p.getCoordinates().getY(), p.getCoordinates().getX(),
                v.getY() + p.getCoordinates().getY(), v.getX() + p.getCoordinates().getX()) { }
        public int getYStart() {
            return yStart;
        }
        public int getXStart() {
            return xStart;
        }
        public int getYEnd() {
            return yEnd;
        }
        public int getXEnd() {
            return xEnd;
        }
        public String toString() {
            return yStart + ":" + xStart + ":" + yEnd + ":" + xEnd;
        }
        public static MoveAttempt fromString(String ma) {
            String[] s = ma.Split(new string[] { ":" }, StringSplitOptions.None);
            return new MoveAttempt(Convert.ToInt32(s[0]), Convert.ToInt32(s[1]), Convert.ToInt32(s[2]), Convert.ToInt32(s[3]));
        }
    }