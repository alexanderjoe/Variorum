package dev.alexanderdiaz.variorum.match.registry;

import dev.alexanderdiaz.variorum.match.Match;
import java.util.HashMap;
import java.util.Optional;
import lombok.Getter;

@Getter
public class MatchRegistry {

    private final Match match;
    private final HashMap<String, RegisterableObject> registry;

    public MatchRegistry(Match match) {
        this.match = match;
        this.registry = new HashMap<>();
    }

    public void register(RegisterableObject object) throws MatchRegistryException {
        Optional<Object> existing = get(Object.class, object.getId(), false);
        if (existing.isPresent()) {
            throw new MatchRegistryException("Object with id " + object.getId() + " already exists.");
        }

        registry.put(object.getId(), object);
    }

    public boolean has(String id) {
        return registry.containsKey(id);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Class<T> clazz, String id, boolean required) throws MatchRegistryException {
        RegisterableObject found = this.registry.get(id);

        if (found == null) {
            if (required) {
                throw new MatchRegistryException(
                        "Could not find required " + clazz.getSimpleName() + " with id " + id + " in MatchRegistry");
            }
            return Optional.empty();
        }

        if (clazz.isAssignableFrom(found.getObject().getClass())) {
            return Optional.of((T) found.getObject());
        }

        throw new MatchRegistryException("Mismatch! For id " + id + ": Expected to find " + clazz.getSimpleName()
                + " but found " + found.getObject().getClass().getSimpleName() + " instead.");
    }
}
