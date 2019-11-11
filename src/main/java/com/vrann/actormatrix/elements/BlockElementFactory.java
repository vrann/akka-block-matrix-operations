package com.vrann.actormatrix.elements;

import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.handler.A11ReadyHander;
import com.vrann.dataformat.UnformattedMatrixReader;
import com.vrann.dataformat.UnformattedMatrixWriter;
import org.apache.spark.ml.linalg.DenseMatrix;

public class BlockElementFactory {

    private final LoggingAdapter log;
    private final ActorRef mediator;
    private final int sectionId;

    public BlockElementFactory(
            LoggingAdapter log,
            ActorRef mediator,
            int sectionId
    ) {
        this.log = log;
        this.mediator = mediator;
        this.sectionId = sectionId;
    }

    //        log.info("Section coordinator started allocation of actors for section of {} actors", sectionPositions.size());
//        for (Position pos: sectionPositions) {
//            log.info("Starting actor for {}", pos);
//            if (pos.getX() == pos.getY() && pos.getX() == 0) { // "first" block at 0,0 coordinates
//                ActorRef block = actorSystem.actorOf(
//                        FirstBlock.props(new Position(pos.getX(), pos.getY())), String.format("first-%d-%d-actor", pos.getX(), pos.getY()));
//            } else if (pos.getX() == pos.getY()) { //block on the diagonal of the matrix
//                ActorRef block = actorSystem.actorOf(
//                        DiagonalBlock.props(new Position(pos.getX(), pos.getY())), String.format("diagonal-%d-%d-actor", pos.getX(), pos.getY()));
//            } else if (pos.getX() == 0) {
//                ActorRef block = actorSystem.actorOf(
//                        FirstColumnBlock.props(new Position(pos.getX(), pos.getY())), String.format("firstcolumn-%d-%d-actor", pos.getX(), pos.getY()));
//            } else {
//                ActorRef block = actorSystem.actorOf(
//                        SubdiagonalBlock.props(new Position(pos.getX(), pos.getY())), String.format("subdiagonal-%d-%d-actor", pos.getX(), pos.getY()));
//            }
//        }

    public BlockElement createBlockElement(Position pos)
    {
        if (pos.getX() == pos.getY() && pos.getX() == 0) { // "first" block at 0,0 coordinates
            //return new FirstBlockElement(pos, mediator);
            return new DiagonalBlockElement(
                    pos,
                    mediator,
                    new A11ReadyHander(
                            log,
                            mediator,
                            sectionId
                    )
            );
        } else if (pos.getX() == pos.getY()) { //block on the diagonal of the matrix
            return new DiagonalBlockElement(pos, mediator,
                    new A11ReadyHander(
                            log,
                            mediator,
                            sectionId
                    ));
        } else if (pos.getX() == 0) {
            //return new FirstColumnBlockElement(pos, mediator);
            return new SubdiagonalBlockElement(pos, mediator);
        } else {
            return new SubdiagonalBlockElement(pos, mediator);
        }
    }

}
