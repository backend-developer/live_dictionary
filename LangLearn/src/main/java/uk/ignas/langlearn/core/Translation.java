package uk.ignas.langlearn.core;

public class Translation {
    private Integer id;
    private ForeignWord foreignWord;
    private NativeWord nativeWord;

    public Translation(ForeignWord foreignWord, NativeWord nativeWord) {
        this(null, foreignWord, nativeWord);
    }

    public Translation(Integer id, Translation translation) {
        this.id = id;
        this.foreignWord = translation.getForeignWord();
        this.nativeWord = translation.getNativeWord();
    }

    public Translation(Integer id, ForeignWord foreignWord, NativeWord nativeWord) {
        this.id = id;
        this.foreignWord = foreignWord;
        this.nativeWord = nativeWord;
    }

    public Integer getId() {
        return id;
    }

    public ForeignWord getForeignWord() {
        return foreignWord;
    }

    public NativeWord getNativeWord() {
        return nativeWord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Translation that = (Translation) o;

        if (foreignWord != null ? !foreignWord.equals(that.foreignWord) : that.foreignWord != null) return false;
        return nativeWord != null ? nativeWord.equals(that.nativeWord) : that.nativeWord == null;

    }

    @Override
    public int hashCode() {
        int result = foreignWord != null ? foreignWord.hashCode() : 0;
        result = 31 * result + (nativeWord != null ? nativeWord.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Translation{" +
                "id=" + id +
                ", foreignWord=" + foreignWord +
                ", nativeWord=" + nativeWord +
                '}';
    }
}
