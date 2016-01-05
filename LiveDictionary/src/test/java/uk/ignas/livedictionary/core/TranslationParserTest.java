package uk.ignas.livedictionary.core;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


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
        Assert.assertEquals(translation.getNativeWord().get(), "happy Path");
        Assert.assertEquals(translation.getForeignWord().get(), "Modo Feliz");
    }

    @Test
    public void shouldTrimResultsAfterParsing() {
        Translation translation = translationParser.parse("  Modo Feliz -     happy Path  ");
        Assert.assertEquals(translation.getNativeWord().get(), "happy Path");
        Assert.assertEquals(translation.getForeignWord().get(), "Modo Feliz");
    }

    @Test
    public void shouldNotParseStringWithoutNativeWord() {
        Translation translation = translationParser.parse("no traducido-");
        Assert.assertNull(translation);
    }

    @Test
    public void shouldNotParseStringWithoutForeignWord() {
        Translation translation = translationParser.parse("-native not-translated word");
        Assert.assertNull(translation);
    }

    @Test
    public void shouldNotParseAmbiguousTranslation() {
        Translation translation = translationParser.parse("word1 - word2 - word3");
        Assert.assertNull(translation);
    }
}