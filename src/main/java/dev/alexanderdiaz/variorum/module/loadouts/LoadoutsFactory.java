package dev.alexanderdiaz.variorum.module.loadouts;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.module.loadouts.types.LoadoutArmor;
import dev.alexanderdiaz.variorum.module.loadouts.types.LoadoutEffect;
import dev.alexanderdiaz.variorum.module.loadouts.types.LoadoutItem;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

public class LoadoutsFactory implements ModuleFactory<LoadoutsModule> {
    @Override
    public Optional<LoadoutsModule> build(Match match, Element root) {
        NodeList loadoutsRoot = root.getElementsByTagName("loadouts");
        if (loadoutsRoot.getLength() == 0) {
            return Optional.empty();
        }

        Map<String, Loadout> loadouts = new HashMap<>();
        NodeList loadoutNodes = ((Element) loadoutsRoot.item(0)).getElementsByTagName("loadout");

        for (int i = 0; i < loadoutNodes.getLength(); i++) {
            Element loadoutElement = (Element) loadoutNodes.item(i);
            String id = loadoutElement.getAttribute("id");
            Loadout loadout = parseLoadout(id, loadoutElement);
            loadouts.put(id, loadout);
        }

        return Optional.of(new LoadoutsModule(match, loadouts));
    }

    private Loadout parseLoadout(String id, Element element) {
        LoadoutArmor helmet = parseArmor(getFirstElementByTag(element, "helmet"));
        LoadoutArmor chestplate = parseArmor(getFirstElementByTag(element, "chestplate"));
        LoadoutArmor leggings = parseArmor(getFirstElementByTag(element, "leggings"));
        LoadoutArmor boots = parseArmor(getFirstElementByTag(element, "boots"));

        List<LoadoutItem> items = parseItems(element);
        List<LoadoutEffect> effects = parseEffects(element);

        return new Loadout(id, helmet, chestplate, leggings, boots, items, effects);
    }

    private LoadoutArmor parseArmor(Element element) {
        if (element == null) return null;

        return new LoadoutArmor(
                element.getAttribute("material"),
                Boolean.parseBoolean(element.getAttribute("team-color")),
                Boolean.parseBoolean(element.getAttribute("unbreakable"))
        );
    }

    private List<LoadoutItem> parseItems(Element parent) {
        List<LoadoutItem> items = new ArrayList<>();
        NodeList itemNodes = parent.getElementsByTagName("item");

        for (int i = 0; i < itemNodes.getLength(); i++) {
            Element itemElement = (Element) itemNodes.item(i);

            String material = itemElement.getAttribute("material");
            int amount = getAttribute(itemElement, "amount", 1);
            int slot = Integer.parseInt(itemElement.getAttribute("slot"));
            boolean unbreakable = Boolean.parseBoolean(itemElement.getAttribute("unbreakable"));

            // Parse enchantments
            List<String> enchantments = new ArrayList<>();
            NodeList enchantNodes = itemElement.getElementsByTagName("enchantment");
            for (int j = 0; j < enchantNodes.getLength(); j++) {
                enchantments.add(enchantNodes.item(j).getTextContent());
            }

            items.add(new LoadoutItem(material, amount, slot, unbreakable, enchantments));
        }

        return items;
    }

    private List<LoadoutEffect> parseEffects(Element parent) {
        List<LoadoutEffect> effects = new ArrayList<>();
        NodeList effectNodes = parent.getElementsByTagName("effect");

        for (int i = 0; i < effectNodes.getLength(); i++) {
            Element effectElement = (Element) effectNodes.item(i);
            effects.add(new LoadoutEffect(
                    effectElement.getTextContent(),
                    effectElement.getAttribute("duration")
            ));
        }

        return effects;
    }

    private Element getFirstElementByTag(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }

    private int getAttribute(Element element, String attribute, int defaultValue) {
        String value = element.getAttribute(attribute);
        return value.isEmpty() ? defaultValue : Integer.parseInt(value);
    }
}