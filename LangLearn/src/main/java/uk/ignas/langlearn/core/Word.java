package uk.ignas.langlearn.core;

public class Word {
    private final String word;

    public Word(String word) {
        this.word = word;
    }

    public String get() {
        return word;
    }

    @Override
    public String toString() {
        return word;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (word == null && o == null) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() == String.class) {
            return word.equals(o);
        }
        if (getClass() != o.getClass()) return false;

        Word word1 = (Word) o;

        return word != null ? word.equals(word1.word) : word1.word == null;

    }

    @Override
    public int hashCode() {
        return word != null ? word.hashCode() : 0;
    }
}
