package uk.ignas.livedictionary.core;

public enum Label {
    A(1),
    B(2),
    C(3),
    D(4);

    private final int id;

    Label(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


}
