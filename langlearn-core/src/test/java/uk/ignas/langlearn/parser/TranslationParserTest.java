package uk.ignas.langlearn.parser;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import uk.ignas.langlearn.Translation;

@org.testng.annotations.Test
public class TranslationParserTest {

    private TranslationParser translationParser;

    @BeforeMethod
    public void setup() {
        translationParser = new TranslationParser();
    }

    public void shouldNotParseEmptyString() {
        Translation translation = translationParser.parse("");
        Assert.assertNull(translation);
    }

    public void shouldParseHappyPathData() {
        Translation translation = translationParser.parse("Modo Feliz-happy Path");
        Assert.assertEquals(translation.getOriginalWord(), "Modo Feliz");
        Assert.assertEquals(translation.getTranslatedWord(), "happy Path");
    }

    public void shouldTrimResultsAfterParsin() {
        Translation translation = translationParser.parse("  Modo Feliz -     happy Path  ");
        Assert.assertEquals(translation.getOriginalWord(), "Modo Feliz");
        Assert.assertEquals(translation.getTranslatedWord(), "happy Path");
    }

    public void shouldNotParseStringWithoutTranslation() {
        Translation translation = translationParser.parse("no traducido-");
        Assert.assertNull(translation);
    }

    public void shouldNotParseStringWithoutOrigWord() {
        Translation translation = translationParser.parse("-translation WithoutOriginal");
        Assert.assertNull(translation);
    }

    public void shouldNotParseAmbiguousTranslation() {
        Translation translation = translationParser.parse("word1 - word2 - word3");
        Assert.assertNull(translation);
    }
}