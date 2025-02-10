package dev.alexanderdiaz.variorum.module.spawn;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleBuildException;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.util.xml.XmlList;
import java.util.Optional;
import org.w3c.dom.Element;

public class SpawnFactory implements ModuleFactory<SpawnModule> {
    @Override
    public Optional<SpawnModule> build(Match match, Element root) throws ModuleBuildException {
        XmlList spawns = XmlList.of(root.getElementsByTagName("spawns"));

        if (spawns.size() == 0) {
            throw new ModuleBuildException(this, "No spawns found in map file.");
        }

        return Optional.of(new SpawnModule(match));
    }
}
