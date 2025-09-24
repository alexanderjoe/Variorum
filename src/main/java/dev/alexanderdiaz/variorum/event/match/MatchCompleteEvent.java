package dev.alexanderdiaz.variorum.event.match;

import dev.alexanderdiaz.variorum.match.Match;
import java.util.Collection;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

@Getter
public class MatchCompleteEvent extends MatchEvent {
  private static final HandlerList handlers = new HandlerList();

  private final Collection<? extends Player> competitors;
  private final Collection<Player> winners;

  public MatchCompleteEvent(
      Match match, Collection<? extends Player> competitors, Collection<Player> winners) {
    super(match);
    this.competitors = competitors;
    this.winners = winners;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
