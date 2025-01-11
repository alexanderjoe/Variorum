package dev.alexanderdiaz.variorum.module.chat;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import java.util.Optional;
import org.w3c.dom.Element;

public class ChatFactory implements ModuleFactory<ChatModule> {
    @Override
    public Optional<ChatModule> build(Match match, Element root) {
        // Chat module is always created as it's a core feature
        return Optional.of(new ChatModule(match));
    }
}
