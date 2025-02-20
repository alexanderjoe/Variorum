package dev.alexanderdiaz.variorum.module.chat;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.Optional;

public class ChatFactory implements ModuleFactory<ChatModule> {
    @Override
    public Optional<ChatModule> build(Match match, XmlElement root) {
        return Optional.of(new ChatModule(match));
    }
}
