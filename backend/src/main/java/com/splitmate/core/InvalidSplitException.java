package com.splitmate.core;

/** Thrown when a split request cannot be resolved into exact cent amounts. */
public class InvalidSplitException extends RuntimeException {
    public InvalidSplitException(String message) { super(message); }
}
