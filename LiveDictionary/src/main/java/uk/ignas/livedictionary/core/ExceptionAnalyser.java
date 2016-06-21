package uk.ignas.livedictionary.core;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.List;

class ExceptionAnalyser {
    static boolean isUniqueConstraintViolation(Exception e) {
        @SuppressWarnings("unchecked")
        List<Throwable> throwables = (List<Throwable>) ExceptionUtils.getThrowableList(e);
        boolean isUniqueConstrainViolation = false;
        for (Throwable t: throwables) {
            if (uniqueConstraintViolation(t.getMessage())) {
                isUniqueConstrainViolation = true;
            }
        }
        return isUniqueConstrainViolation;
    }

    static boolean uniqueConstraintViolation(String message) {
        return message.toLowerCase().contains("unique");
    }

}
