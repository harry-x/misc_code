package hsxin;

/**
 * @author harpreet.singh
 * @since 8/24/2017.
 */
public class Pair<X, Y> {
    protected final X x;
    protected final Y y;
    protected final int hashCode;

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
        int result;
        result = this.x != null ? this.x.hashCode() : 0;
        result = 31 * result + (this.y != null ? this.y.hashCode() : 0);
        hashCode = result;
    }

    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pair<?, ?> pair = (Pair<?, ?>) o;

        return pair.hashCode == hashCode
                && !(x != null ? !x.equals(pair.x) : pair.x != null)
                && !(y != null ? !y.equals(pair.y) : pair.y != null);
    }
}
