package dev.alexanderdiaz.variorum.map.rotation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.MapManager;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.MatchFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class JsonFileRotationProvider extends AbstractFileRotationProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<ArrayList<String>>() {}.getType();

    public JsonFileRotationProvider(File file, MapManager mm, MatchFactory factory) {
        super(file, mm, factory);
    }

    @Override
    protected Rotation createRotation() {
        List<Match> matches = new ArrayList<>();

        // If rotation file doesn't exist, use default rotation
        if (!file.exists()) {
            Variorum.get().getLogger().info("No rotation file found, using default rotation");
            throw new IllegalStateException("No rotation file found");
        }

        try (Reader reader = Files.newBufferedReader(this.file.toPath())) {
            List<String> mapNames = GSON.fromJson(reader, LIST_TYPE);

            // Validate that all maps exist and create matches
            for (String mapName : mapNames) {
                try {
                    Match match = this.createMatch(mapName);
                    matches.add(match);
                } catch (Exception e) {
                    Variorum.get().getLogger().warning("Failed to create match for map: " + mapName);
                }
            }

            if (matches.isEmpty()) {
                Variorum.get().getLogger().warning("No valid maps found in rotation file, using default rotation");
                throw new IllegalStateException("No valid maps found in rotation file");
            }

            Variorum.get().getLogger().info("Loaded " + matches.size() + " maps from rotation file");
            return new Rotation(matches);

        } catch (IOException e) {
            Variorum.get().getLogger().log(Level.SEVERE, "Failed to load rotation file", e);
            throw new IllegalStateException("Failed to load rotation from file", e);
        }
    }
}
