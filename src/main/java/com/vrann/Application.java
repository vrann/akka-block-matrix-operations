package com.vrann;

import com.typesafe.config.ConfigFactory;
import com.vrann.actormatrix.*;
import com.vrann.actormatrix.block.BlockFactory;
import com.vrann.actormatrix.cholesky.handler.HandlerFactory;
import java.io.IOException;

public class Application {

    public static void main(String[] args) {

        ActorSystemContext context = ActorSystemContext.create(
                ConfigFactory.load("app1.conf")
        );

        SectionCoordinator sectionCoordinator = new SectionCoordinator(
                SectionConfiguration.createDefaultNodeConfiguration(),
                new BlockFactory(HandlerFactory.create(context)),
                context,
                new MatrixBlockFileLocator()
        );

        TopologyManager topologyManager = new TopologyManager(sectionCoordinator);

        try {
            topologyManager.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
