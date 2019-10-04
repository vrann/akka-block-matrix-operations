package com.vrann.dataformat;

import com.vrann.actormatrix.Position;
import com.vrann.blockedcholesky.operation.BlockMatrixType;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MatrixFileInputStreamFactory {

    private FileLocator fileNameLocator;

    public MatrixFileInputStreamFactory(FileLocator fileNameLocator) {
        //arguments which would help to resolve path to the file
        //Concern: single responsibility; this class should not know neither about the location of the file not how to
        // obtain that location
        // options are:
        //FileLocator -- encapsulates logic on both: passing reference to the ile to this class and constructing the path to the file
        // File -- passing reference to this class but somebody else ( the class invoking this class) should construct the path
        // question: should class invoking this class know how to construct the path?

        this.fileNameLocator = fileNameLocator;
    }

    private File getFile(Position position, BlockMatrixType blockMatrixType) {
        System.out.println(fileNameLocator.getFile(position, blockMatrixType));
        return new File(fileNameLocator.getFile(position, blockMatrixType).toString());
    }

    //constructing input stream after file path was located and File is instantiated
    public DataInputStream createMatrixBlockInputStream(Position position, BlockMatrixType blockMatrixType) throws FileNotFoundException {
        try {
            return new DataInputStream(new FileInputStream(getFile(position, blockMatrixType)));
        } catch (FileNotFoundException e) {
            throw new MatrixBlockException("Data for Matrix Block %d %d of type Original is not found in %s");
        }
    }
}
