package dev.alexanderdiaz.variorum.map.rotation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.alexanderdiaz.variorum.match.Match;
import java.util.List;
import org.junit.jupiter.api.Test;

class RotationTest {
  @Test
  void testThatRotationIsEmptyAndGetMatchThrows() {
    // given
    Rotation rotation = new Rotation(List.of());
    // when
    assertTrue(rotation.getMapQueue().isEmpty());
    // then
    assertThrows(IllegalStateException.class, rotation::getMatch);
  }

  @Test
  void testThatRotationWithAMatchReturnsMatch() {
    // given
    Match matchMock = mock(Match.class);
    Rotation rotation = new Rotation(List.of(matchMock));

    // when
    rotation.start();

    // then
    assertEquals(0, rotation.getCurrentIndex().intValue());
    assertEquals(matchMock, rotation.getMatch());
  }
}
