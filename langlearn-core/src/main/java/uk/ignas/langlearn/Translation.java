package uk.ignas.langlearn;

public class Translation {
    private String originalWord;
    private String translatedWord;

    public Translation(String originalWord, String translatedWord) {
        this.originalWord = originalWord;
        this.translatedWord = translatedWord;
    }

    public String getOriginalWord() {
        return originalWord;
    }

    public String getTranslatedWord() {
        return translatedWord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Translation that = (Translation) o;

        if (!originalWord.equals(that.originalWord)) return false;
        if (!translatedWord.equals(that.translatedWord)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = originalWord.hashCode();
        result = 31 * result + translatedWord.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Translation{" +
                "originalWord='" + originalWord + '\'' +
                ", translatedWord='" + translatedWord + '\'' +
                '}';
    }
}
