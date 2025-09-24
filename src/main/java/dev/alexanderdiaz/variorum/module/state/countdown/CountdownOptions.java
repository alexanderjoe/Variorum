package dev.alexanderdiaz.variorum.module.state.countdown;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Builder
@Getter
public class CountdownOptions {
  /** The prefix to display before the countdown time in chat messages. */
  @Builder.Default
  private Component prefix = Component.text("Match starting in ", NamedTextColor.YELLOW);

  /** Whether to send chat announcements during the countdown. */
  @Builder.Default
  private boolean chatsEnabled = true;

  /** Whether to show title announcements during the countdown. */
  @Builder.Default
  private boolean titlesEnabled = true;

  /**
   * Whether to enable any announcements at all. If false, both chat and title announcements will be
   * disabled.
   */
  @Builder.Default
  private boolean announcementsEnabled = true;

  /** Creates a default options instance. */
  public static CountdownOptions defaults() {
    return CountdownOptions.builder().build();
  }

  /** Creates options for a match start countdown. */
  public static CountdownOptions matchStart() {
    return CountdownOptions.builder()
        .prefix(Component.text("Match starting in ", NamedTextColor.YELLOW))
        .build();
  }

  /** Creates options for a match end countdown. */
  public static CountdownOptions matchEnd() {
    return CountdownOptions.builder()
        .prefix(Component.text("Match ending in ", NamedTextColor.YELLOW))
        .build();
  }

  /** Creates silent options (no announcements). */
  public static CountdownOptions silent() {
    return CountdownOptions.builder().announcementsEnabled(false).build();
  }
}
