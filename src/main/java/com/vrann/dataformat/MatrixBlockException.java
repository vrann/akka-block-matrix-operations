package com.vrann.dataformat;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MatrixBlockException extends FileNotFoundException {

    public MatrixBlockException(String message) {
        super(message);
    }
}
