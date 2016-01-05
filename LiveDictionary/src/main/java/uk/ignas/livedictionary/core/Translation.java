package uk.ignas.livedictionary.core;

public class Translation {
    private Integer id;
    private ForeignWord foreignWord;
    private NativeWord nativeWord;
    private TranslationMetadata metadata;

    public Translation(ForeignWord foreignWord, NativeWord nativeWord) {
        this(foreignWord, nativeWord, TranslationMetadata.createEmpty());
    }

    public Translation(ForeignWord foreignWord, NativeWord nativeWord, TranslationMetadata metadata) {
        this(null, foreignWord, nativeWord, metadata);
    }

    public Translation(Integer id, Translation translation) {
        this.id = id;
        this.foreignWord = translation.getForeignWord();
        this.nativeWord = translation.getNativeWord();
        this.metadata = translation.getMetadata();
    }

    public Translation(Integer id, ForeignWord foreignWord, NativeWord nativeWord) {
        this(id, foreignWord, nativeWord, TranslationMetadata.createEmpty());
    }

    public Translation(Integer id, ForeignWord foreignWord, NativeWord nativeWord, TranslationMetadata metadata) {
        this.id = id;
        this.foreignWord = foreignWord;
        this.nativeWord = nativeWord;
        this.metadata = metadata;
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

    public TranslationMetadata getMetadata() {
        return metadata;
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
                ", metadata=" + metadata +
                '}';
    }
}
