package uk.ignas.langlearn;

public class Translation {
    private String originalWord;

    public Translation(String originalWord, String translatedWord) {
        this.originalWord = originalWord;
    }

    public String getOriginalWord() {
        return originalWord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Translation that = (Translation) o;

        if (originalWord != null ? !originalWord.equals(that.originalWord) : that.originalWord != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return originalWord != null ? originalWord.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Translation{" +
                "originalWord='" + originalWord + '\'' +
                '}';
    }
}
