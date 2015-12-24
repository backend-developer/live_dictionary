package uk.ignas.langlearn.util;

/**
 * Created by ignas on 12/24/15.
 */
public class MutableObject<E> {
    private E object;

    public MutableObject(E object) {
        this.object = object;
    }

    public void set(E object) {
        this.object = object;
    }

    public E get() {
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutableObject<?> that = (MutableObject<?>) o;

        return object != null ? object.equals(that.object) : that.object == null;

    }

    @Override
    public int hashCode() {
        return object != null ? object.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "MutableObject{" +
                "object=" + object +
                '}';
    }
}
