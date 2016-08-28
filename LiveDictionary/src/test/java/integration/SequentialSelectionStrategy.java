package integration;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import uk.ignas.livedictionary.core.Translation;
import uk.ignas.livedictionary.core.TranslationSelectionStrategy;

import java.util.Iterator;
import java.util.List;

public class SequentialSelectionStrategy implements TranslationSelectionStrategy {
    private Iterator<Translation> iterator;
    private List<Translation> translations;

    @Override
    public void updateState(List<Translation> translations) {
        this.translations = translations;
        iterator = Iterables.cycle(translations).iterator();
    }

    @Override
    public Optional<Translation> selectTranslation() {
        return Optional.fromNullable(translations.isEmpty() ? null : iterator.next());
    }
}
