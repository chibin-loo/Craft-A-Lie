package com.artlu.craftalie.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Voting {
    private static final Map<UUID, UUID> VOTES = new HashMap<>(); // voter -> who they voted for
    private static final Set<UUID> CANDIDATES = new LinkedHashSet<>(); // who can be voted for
    private static boolean revote = false;

    private Voting() {
    }

    public static void begin(Collection<UUID> candidates, boolean isRevote) {
        VOTES.clear();
        CANDIDATES.clear();
        CANDIDATES.addAll(candidates);
        revote = isRevote;
    }

    public static void clear() {
        VOTES.clear();
        CANDIDATES.clear();
        revote = false;
    }

    public static Set<UUID> candidates() {
        return Collections.unmodifiableSet(CANDIDATES);
    }

    public static boolean isRevote() {
        return revote;
    }

    public static boolean isCandidate(UUID id) {
        return CANDIDATES.contains(id);
    }

    public static boolean hasVoted(UUID voter) {
        return VOTES.containsKey(voter);
    }

    public static void cast(UUID voter, UUID target) {
        VOTES.put(voter, target);
    }

    public static boolean everyoneVoted(Collection<UUID> voters) {
        return VOTES.keySet().containsAll(voters);
    }

    /** Vote count per candidate. */
    public static Map<UUID, Integer> tally() {
        Map<UUID, Integer> counts = new HashMap<>();
        for (UUID target : VOTES.values()) {
            counts.merge(target, 1, Integer::sum);
        }
        return counts;
    }

    /** Everyone tied for the most votes (empty if nobody voted). */
    public static List<UUID> mostVoted() {
        Map<UUID, Integer> counts = tally();
        int max = 0;
        for (int c : counts.values()) {
            max = Math.max(max, c);
        }
        List<UUID> top = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
            if (max > 0 && entry.getValue() == max) {
                top.add(entry.getKey());
            }
        }
        return top;
    }
}