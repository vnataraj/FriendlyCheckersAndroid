import java.util.ArrayList;

import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException.InvalidMoveException.PlayerMustJumpException.WrongMultiJumpPieceException.BadMoveNumberException.NoMovesLeftException.Piece;
import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException.InvalidMoveException.PlayerMustJumpException.WrongMultiJumpPieceException.BadMoveNumberException.NoMovesLeftException.PieceColor;

 public class Move { // this is the api to give data to networking (and maybe GUI)
        ArrayList<Piece> removals;
        ArrayList<Piece> additions;
        PieceColor player; 
        
        public Move(int turnNumber, List<Piece> removals, List<Piece> additions, PieceColor player) {
            //this.turnNumber = turnNumber;
            this.removals = removals;
            this.additions = additions;
            this.player = player; 
        }

        public PieceColor getPlayer() { 
            return this.player; 
        }

        public List<Piece> getRemovals(){
            return removals;
        }

        public List<Piece> getAdditions() { //does a deep copy of additions
            List<Piece> additionsCopy = new List<Piece>(); 
            foreach (Piece v in this.additions) {
                additionsCopy.Add(new Piece(v));
            }
            return additionsCopy;
        }

    }