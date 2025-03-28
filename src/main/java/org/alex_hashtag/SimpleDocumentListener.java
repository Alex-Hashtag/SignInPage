package org.alex_hashtag;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@FunctionalInterface
public interface SimpleDocumentListener extends DocumentListener {
    void update();

    @Override
    default void insertUpdate(DocumentEvent e) {
        update();
    }

    @Override
    default void removeUpdate(DocumentEvent e) {
        update();
    }

    @Override
    default void changedUpdate(DocumentEvent e) {
        update();
    }
}
