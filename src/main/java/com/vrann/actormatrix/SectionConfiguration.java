package com.vrann.actormatrix;

import com.typesafe.config.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SectionConfiguration {

    private Config sectionConfig;

    private static final ConfigSyntax syntax = ConfigSyntax.CONF;

    public SectionConfiguration(
            File file,
            ConfigSyntax syntax
    ) {
        sectionConfig = ConfigFactory.parseFile(file,
                ConfigParseOptions.defaults().setSyntax(syntax));
    }

    public static SectionConfiguration createDefaultNodeConfiguration() {
        StringBuilder pathBuilder = (new StringBuilder())
                .append(System.getProperty("user.home"))
                .append("/.actorchoreography/node.conf");
        File file = new File(pathBuilder.toString());
        ;
        return new SectionConfiguration(file, syntax);
    }

    public static SectionConfiguration createCustomNodeConfiguration(File file) {
        return new SectionConfiguration(file, syntax);
    }

    public int getSectionId() {
        return sectionConfig.getInt("actors.section");
    }

    public List<Position> getSectionBlockPositions() {
        List<? extends ConfigObject> positionsArray = sectionConfig.getObjectList("actors.matrix-blocks");
        List<Position> positions = new ArrayList<>();
        for (ConfigObject position: positionsArray) {
            positions.add(new Position(position.get("x").render(), position.get("y").render()));
        }
        return positions;
    }
}
