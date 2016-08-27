package uk.ignas.livedictionary.core;

import java.util.Collection;
import java.util.List;

public class TranslationDaoStub implements TranslationDao {

    public TranslationDaoStub(LabelDaoStub dao, AnswewrDaoStub stub) {

    }

    @Override
    public void insert(List<Translation> translations) {

    }

    @Override
    public boolean insertSingle(Translation translation) {
        return false;
    }

    @Override
    public int update(Translation translation) {
        return 0;
    }

    @Override
    public void delete(Collection<Translation> translations) {

    }

    @Override
    public List<Translation> getAllTranslations() {
        return null;
    }
}
