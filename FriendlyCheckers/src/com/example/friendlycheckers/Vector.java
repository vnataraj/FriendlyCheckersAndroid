import com.example.friendlycheckers.GameLogicException.CellAlreadyFilledException.CellEmptyException.CellFullException.PieceWrongColorException.UnreachableCodeException.CellOutOfBoundsException.InvalidMoveException.PlayerMustJumpException.WrongMultiJumpPieceException.BadMoveNumberException.NoMovesLeftException.Vector;

public class Vector {
        private int x;
        private int y; 

        // Constructor: 
        public Vector(int y, int x) {
            this.x = x;
            this.y = y;
        }

        @override
        public String ToString() {
            return ":" + getY() + "," + getX() + ":";
        }

        public int getX() { 
            return x; 
        }

        public int getY(){ 
            return y; 
        }

        //copy Constructor: 
        public Vector(Vector copyable) { 
            this.x = copyable.getX(); 
            this.y = copyable.getY(); 
        }

        public Vector add(Vector move) {
            return new Vector(this.y + move.y, 
                this.x + move.x);
        }

        public Vector subtract(Vector move) {
            return new Vector(this.y - move.y,
                this.x - move.x);
        }

        public Vector divideVector(int divisor) {
            return new Vector(this.getY() / divisor, this.getX() / divisor);
        }

        @override
        public boolean Equals(Object obj) {
            if (obj == null || GetType() != obj.GetType())
                return false;
            Vector v = (Vector)obj;
            return this.x == v.getX() && this.y == v.getY();
        }
        @override
        public int GetHashCode() {
            return x ^ y;
        }
    }