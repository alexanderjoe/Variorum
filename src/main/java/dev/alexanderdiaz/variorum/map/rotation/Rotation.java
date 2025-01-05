package dev.alexanderdiaz.variorum.map.rotation;

import lombok.Getter;
import java.util.LinkedList;
import java.util.List;

public class Rotation {
    @Getter
    private final LinkedList<String> mapQueue;
    private int currentIndex = 0;

    public Rotation(List<String> maps) {
        this.mapQueue = new LinkedList<>(maps);
    }

    public String getCurrentMap() {
        if (mapQueue.isEmpty()) {
            throw new IllegalStateException("No maps in rotation");
        }
        return mapQueue.get(currentIndex);
    }

    public String getNextMap() {
        if (mapQueue.isEmpty()) {
            throw new IllegalStateException("No maps in rotation");
        }
        currentIndex = (currentIndex + 1) % mapQueue.size();
        return mapQueue.get(currentIndex);
    }

    public void addMap(String mapName) {
        mapQueue.add(mapName);
    }

    public void removeMap(String mapName) {
        mapQueue.remove(mapName);
        if (currentIndex >= mapQueue.size()) {
            currentIndex = 0;
        }
    }

    public void shuffle() {
        List<String> currentMaps = new LinkedList<>(mapQueue);
        mapQueue.clear();
        while (!currentMaps.isEmpty()) {
            int index = (int) (Math.random() * currentMaps.size());
            mapQueue.add(currentMaps.remove(index));
        }
        currentIndex = 0;
    }
}
