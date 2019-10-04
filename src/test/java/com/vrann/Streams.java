package com.vrann;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.management.javadsl.AkkaManagement;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import com.typesafe.config.ConfigFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class Streams {

    public static void main(String[] args) {
        System.out.println("streams");

        final Source<Integer, NotUsed> source =
                Source.from(Arrays.asList(1, 2, 3, 4)).map(elem -> elem * 2);
        source.to(Sink.foreach(System.out::println));
        //source.mat





        ActorSystem system = ActorSystem.create("l11-actor-system", ConfigFactory.load("app.conf"));
        AkkaManagement.get(system).start();
        LoggingAdapter log = Logging.getLogger(system, system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);


        final Path file = Paths.get(Streams.class.getResource("/test/data-0-0.dat").getPath());
        Sink<ByteString, CompletionStage<Done>> printlnSink =
                Sink.<ByteString>foreach(chunk -> System.out.println(chunk.utf8String()));

        Sink<ByteString, CompletionStage<ByteString>> concatSink =
                Sink.<ByteString, ByteString>fold(ByteString.fromString(""), (akk, entry) -> {akk = akk.concat(entry);
                    System.out.println(akk.utf8String()); return akk;});

        CompletionStage<IOResult> result = FileIO.fromPath(file).to(printlnSink).run(materializer);
        CompletionStage<IOResult> ioResult = FileIO.fromPath(file).to(concatSink).run(materializer);
        //System.out.println(ioResult.thenAccept(res -> {res.});

//        final Source<Integer, NotUsed> source1 =
//                Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
//        source1.map(x -> 0); // has no effect on source, since it's immutable
//        source1.runWith(Sink.fold(0, (agg, next) -> agg + next), materializer); // 55
//
//
//
//        final Sink<Integer, CompletionStage<Integer>> sink =
//                Sink.<Integer, Integer>fold(0, (aggr, next) -> aggr + next);
//        final RunnableGraph<CompletionStage<Integer>> runnable =
//                Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).toMat(sink, Keep.right());
//        // materialize the flow
//        final CompletionStage<Integer> sum = runnable.run(materializer);
//        sum.thenRun(System.out::println);

//        final Source<String, NotUsed> source =
//                Source.repeat(NotUsed.getInstance()).map(elem -> builderFunction());
//
//        source.map(
//                elem -> {
//                    System.out.println(elem);
//                    return elem;
//                });



//        final Source<Integer, NotUsed> sourceInteger =
//                Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
//// note that the Future is scala.concurrent.Future
////        final Sink<Integer, CompletionStage<Integer>> sink =
////                Sink.<Integer, Integer>fold(0, (aggr, next) -> aggr + next);
//
//        // connect the Source to the Sink, obtaining a RunnableFlow
//        //
//
//// materialize the flow
//        //final CompletionStage<Integer> sum = runnable.run();
//
//        System.out.println(sink.toString());
    }

    private static String builderFunction() {
        return "test";
    }

}
