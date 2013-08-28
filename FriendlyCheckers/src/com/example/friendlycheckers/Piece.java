import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException.InvalidMoveException.PlayerMustJumpException.WrongMultiJumpPieceException.BadMoveNumberException.NoMovesLeftException.Piece;
import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException.InvalidMoveException.PlayerMustJumpException.WrongMultiJumpPieceException.BadMoveNumberException.NoMovesLeftException.PieceColor;
import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException.InvalidMoveException.PlayerMustJumpException.WrongMultiJumpPieceException.BadMoveNumberException.NoMovesLeftException.PieceType;

public class Piece { // Piece cannot be changed after created; only copied or read
        PieceColor color;
        Vector coordinates;
        PieceType type;

        public Piece(PieceColor color, Vector coordinates, PieceType type) {
            this.color = color;
            this.coordinates = coordinates;
            this.type = type;
        }
        public Piece(Piece copyable) {
            this.color = copyable.color;
            this.coordinates = copyable.coordinates;
            this.type = copyable.type;
        }

        public Piece newLocation(Vector loc){ 
            return new Piece(this.color, loc, this.type);
        }

        public Piece newType(PieceType type) {
            return new Piece(this.color, this.coordinates, type);
        }

        public PieceType getType(){ 
            return type; 
        }
        public PieceColor getColor(){ 
            return color; 
        }
        public Vector getCoordinates(){ 
            return new Vector(coordinates); 
        }

    }