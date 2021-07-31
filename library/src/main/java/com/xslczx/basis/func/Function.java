package com.xslczx.basis.func;

public interface Function<I, O> {
    /**
     * Applies this function to the given input.
     *
     * @param input the input
     * @return the function result.
     */
    O apply(I input);
}
