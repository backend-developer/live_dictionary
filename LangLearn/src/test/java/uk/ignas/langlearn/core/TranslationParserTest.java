package uk.ignas.langlearn.core;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.TranslationParser;


public class TranslationParserTest {

    private TranslationParser translationParser;

    @Before
    public void setup() {
        translationParser = new TranslationParser();
    }

    @Test
    public void shouldNotParseEmptyString() {
        Translation translation = translationParser.parse("");
        Assert.assertNull(translation);
    }

    @Test
    public void shouldParseHappyPathData() {
        Translation translation = translationParser.parse("Modo Feliz-happy Path");
        Assert.assertEquals(translation.getOriginalWord(), "happy Path");
        Assert.assertEquals(translation.getTranslatedWord(), "Modo Feliz");
    }

    @Test
    public void shouldTrimResultsAfterParsing() {
        Translation translation = translationParser.parse("  Modo Feliz -     happy Path  ");
        Assert.assertEquals(translation.getOriginalWord(), "happy Path");
        Assert.assertEquals(translation.getTranslatedWord(), "Modo Feliz");
    }

    @Test
    public void shouldNotParseStringWithoutTranslation() {
        Translation translation = translationParser.parse("no traducido-");
        Assert.assertNull(translation);
    }

    @Test
    public void shouldNotParseStringWithoutOrigWord() {
        Translation translation = translationParser.parse("-translation WithoutOriginalWord");
        Assert.assertNull(translation);
    }

    @Test
    public void shouldNotParseAmbiguousTranslation() {
        Translation translation = translationParser.parse("word1 - word2 - word3");
        Assert.assertNull(translation);
    }
}