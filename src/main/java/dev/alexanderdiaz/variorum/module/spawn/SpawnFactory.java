package dev.alexanderdiaz.variorum.module.spawn;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleBuildException;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.List;
import java.util.Optional;

public class SpawnFactory implements ModuleFactory<SpawnModule> {
    @Override
    public Optional<SpawnModule> build(Match match, XmlElement root) throws ModuleBuildException {
        List<XmlElement> spawns = root.getChildren("spawns");

        if (spawns.isEmpty()) {
            throw new ModuleBuildException(this, "No spawns found in map file.");
        }

        return Optional.of(new SpawnModule(match));
    }
}
