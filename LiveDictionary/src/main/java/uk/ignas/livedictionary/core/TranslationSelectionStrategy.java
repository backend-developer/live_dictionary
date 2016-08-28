package uk.ignas.livedictionary.core;

import com.google.common.base.Optional;

import java.util.List;

public interface TranslationSelectionStrategy {
    void updateState(List<Translation> translations);

    Optional<Translation> selectTranslation();
}
