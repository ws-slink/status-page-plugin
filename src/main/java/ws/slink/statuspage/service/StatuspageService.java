package ws.slink.statuspage.service;

import org.apache.commons.lang3.StringUtils;
import ws.slink.statuspage.StatusPage;
import ws.slink.statuspage.model.*;
import ws.slink.statuspage.type.ComponentStatus;
import ws.slink.statuspage.type.IncidentSeverity;
import ws.slink.statuspage.type.IncidentStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatuspageService {

    private static class StatuspageServiceSingleton {
        private static final StatuspageService INSTANCE = new StatuspageService();
    }
    public static StatuspageService instance () {
        return StatuspageService.StatuspageServiceSingleton.INSTANCE;
    }

    private final Map<String, StatusPage> statusPages = new ConcurrentHashMap<>();

    private StatuspageService() {
        System.out.println("---- created statuspage service");
    }

    public void clear() {
        System.out.println("--- clear statusPage store");
        statusPages.clear();
    }
    public void init(String projectKey, String apiKey) {
        System.out.println("--- init statusPage for " + projectKey + " with ApiKey " + apiKey);
        if (StringUtils.isBlank(apiKey))
            return;
        statusPages.remove(projectKey);
        statusPages.put(projectKey, new StatusPage.Builder()
            .apiKey(apiKey)
            .bridgeErrors(true)
            .rateLimit(true)
            .rateLimitDelay(1000)
            .build())
        ;
    }
    public Optional<StatusPage> get(String projectKey) {
        return Optional.ofNullable(statusPages.get(projectKey));
    }
    public List<AffectedComponentStatus> componentStatusList() {
        return Arrays.asList(ComponentStatus.values())
            .stream()
            .sorted(Comparator.comparing(a -> Integer.valueOf(a.id())))
            .map(AffectedComponentStatus::of)
            .filter(s -> StringUtils.isNotBlank(s.title))
            .collect(Collectors.toList())
        ;
    }
    public List<IssueIncidentImpact> incidentImpactList() {
        return Arrays.asList(IncidentSeverity.values())
                .stream()
                .sorted(Comparator.comparing(a -> Integer.valueOf(a.id())))
                .map(IssueIncidentImpact::of)
                .filter(s -> StringUtils.isNotBlank(s.title))
                .collect(Collectors.toList())
                ;
    }
    public List<IssueIncidentStatus> incidentStatusList(boolean sheduledIncident) {
        Stream<IncidentStatus> is = Arrays.asList(IncidentStatus.values()).stream();
        if (sheduledIncident)
            is = is.filter(s -> s.id() >= IncidentStatus.SCHEDULED.id());
        else
            is = is.filter(s -> s.id() < IncidentStatus.SCHEDULED.id());
        return is
            .sorted(Comparator.comparing(a -> Integer.valueOf(a.id())))
            .map(IssueIncidentStatus::of)
            .filter(s -> StringUtils.isNotBlank(s.title))
            .collect(Collectors.toList())
        ;
    }
    public List<Component> nonAffectedComponentsList(List<Component> allComponents, Incident incident) {
        if (null == allComponents || null == incident) {
            return Collections.emptyList();
        } else {
            List<Component> result = new ArrayList<>(allComponents);
            result.removeAll(incident.components());
            return result;
        }
    }

}
