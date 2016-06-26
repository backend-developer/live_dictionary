package uk.ignas.livedictionary.core.util;

public interface Transactable<T> {
    T perform();
}
