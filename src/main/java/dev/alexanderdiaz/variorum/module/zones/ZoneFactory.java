package dev.alexanderdiaz.variorum.module.zones;

import static dev.alexanderdiaz.variorum.map.VariorumMapFactory.getElementContext;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.MapParseException;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.registry.RegisteredObject;
import dev.alexanderdiaz.variorum.module.FactoryUtil;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.module.zones.checks.EntryCheck;
import dev.alexanderdiaz.variorum.region.Region;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParser;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParsers;
import java.lang.reflect.Method;
import java.util.*;

public class ZoneFactory implements ModuleFactory<ZoneModule> {
    private static final Map<Method, Collection<String>> PARSERS = NamedParsers.getMethods(ZoneFactory.class);

    @Override
    public Optional<ZoneModule> build(Match match, XmlElement root) {
        List<XmlElement> zones = root.getChildren("zones");
        if (zones.isEmpty()) {
            return Optional.empty();
        }

        ZoneModule module = new ZoneModule(match);

        for (XmlElement zone : zones) {
            Zone parsedZone = parseZone(match, zone);
            match.getRegistry().register(new RegisteredObject<>(parsedZone.getId(), parsedZone));
            module.addZone(parsedZone);
        }

        return Optional.of(module);
    }

    private Zone parseZone(Match match, XmlElement element) {
        String id = element.getRequiredAttribute("id");
        XmlElement regionElement = element.getRequiredChild("region");

        Region region = FactoryUtil.resolveRequiredRegionAs(
                match, Region.class, element.getAttribute("ref"), Optional.of(regionElement));

        Variorum.get()
                .getLogger()
                .info("Found region " + region.getMin() + " " + region.getMax() + " c:" + region.getCenter());

        Zone zone = new Zone(match, id, region);

        List<XmlElement> checks = element.getChildren("checks");
        if (!checks.isEmpty()) {
            parseChecks(match, zone, checks);
        }

        return zone;
    }

    private void parseChecks(Match match, Zone zone, List<XmlElement> checksElement) {
        for (XmlElement checkElement : checksElement) {
            try {
                NamedParsers.invoke(
                        this, PARSERS, checkElement, "Unknown check type: " + checkElement.getName(), match, zone);
            } catch (Exception e) {
                throw new MapParseException(
                        "Failed to parse check: " + checkElement.getName(),
                        "zones",
                        getElementContext(checkElement.getElement()),
                        e);
            }
        }
    }

    @NamedParser("entry-check")
    private void parseEntryCheck(XmlElement element, Match match, Zone zone) {
        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);

        Set<Team> allowedTeams = new HashSet<>();
        Set<Team> deniedTeams = new HashSet<>();

        List<XmlElement> allowTeamNodes = element.getChildrenByTag("allow-team");
        for (XmlElement teamElement : allowTeamNodes) {
            String teamId = teamElement.getTextContent().trim();
            match.getRegistry().get(Team.class, teamId, true).ifPresent(allowedTeams::add);
            // teamsModule.getTeamById(teamId).ifPresent(allowedTeams::add);
        }

        List<XmlElement> denyTeamNodes = element.getChildrenByTag("deny-team");
        for (XmlElement teamElement : denyTeamNodes) {
            String teamId = teamElement.getTextContent().trim();
            match.getRegistry().get(Team.class, teamId, true).ifPresent(deniedTeams::add);
            // teamsModule.getTeamById(teamId).ifPresent(deniedTeams::add);
        }

        String message = null;
        Optional<XmlElement> messageElement = element.getFirstChildByTag("message");
        if (messageElement.isPresent()) {
            message = messageElement.get().getTextContent().trim();
        }

        boolean denySpectators = element.getBooleanAttribute("deny-spectators", false);

        EntryCheck check = new EntryCheck(zone, allowedTeams, deniedTeams, message, denySpectators);
        zone.addCheck(check);
    }
}
