package dev.alexanderdiaz.variorum.map.rotation;

import com.google.common.base.Preconditions;
import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.Match;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.mutable.MutableInt;

@Getter
@ToString
public class Rotation {
    private final List<Match> mapQueue;
    private final MutableInt currentIndex;

    public Rotation(List<Match> matches) {
        this.mapQueue = matches;
        this.currentIndex = new MutableInt(-1);
    }

    public void start() {
        this.currentIndex.increment();
        getMatch().load();
        getMatch().start();
    }

    public boolean remove(int index) {
        if (index <= this.currentIndex.intValue() || index > this.mapQueue.size()) {
            throw new IllegalArgumentException("Invalid index " + index + " for removal.");
        }

        this.mapQueue.remove(index);
        return true;
    }

    public boolean next(Match match) throws IllegalArgumentException {
        return insert(this.currentIndex.intValue() + 1, match);
    }

    public boolean append(Match match) {
        return insert(this.mapQueue.size(), match);
    }

    public boolean insert(int index, Match match) {
        if (index <= this.currentIndex.intValue() || index > this.mapQueue.size()) {
            throw new IllegalArgumentException("Invalid index " + index + " for insertion.");
        }

        this.mapQueue.add(index, match);
        return true;
    }

    public Match getMatch() {
        Optional<Match> match = getMatchAt(this.currentIndex.intValue());
        Preconditions.checkState(match.isPresent(), "No match at index " + this.currentIndex);
        return match.get();
    }

    public Optional<Match> getNextMatch() {
        return getMatchAt(this.currentIndex.intValue() + 1);
    }

    public Optional<Match> getMatchAt(int index) {
        if (index < 0 || index >= this.mapQueue.size()) {
            return Optional.empty();
        }
        return Optional.of(this.mapQueue.get(index));
    }

    public void cycle() {
        Match from = getMatch();
        Optional<Match> to = getNextMatch();

        if (to.isEmpty()) {
            throw new IllegalStateException("No match to cycle to.");
        }

        from.end();

        this.currentIndex.increment();

        to.get().load();
        to.get().start();

        Variorum.get()
                .getServer()
                .getScheduler()
                .runTaskLater(
                        Variorum.get(),
                        bukkitTask -> {
                            from.unload();
                        },
                        20L * 5); // 5 seconds
    }
}
