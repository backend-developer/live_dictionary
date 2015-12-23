package uk.ignas.langlearn.core;

public class Translation {
    private Integer id;
    private String originalWord;
    private String translatedWord;

    public Translation(String originalWord, String translatedWord) {
        this(null, originalWord, translatedWord);
    }

    public Translation(Integer id, Translation translation) {
        this.id = id;
        this.originalWord = translation.getOriginalWord();
        this.translatedWord = translation.getTranslatedWord();
    }

    public Translation(Integer id, String originalWord, String translatedWord) {
        this.id = id;
        this.originalWord = originalWord;
        this.translatedWord = translatedWord;
    }

    public Integer getId() {
        return id;
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
