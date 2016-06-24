package uk.ignas.livedictionary.core;

interface Transactable<T> {
    T perform();
}
