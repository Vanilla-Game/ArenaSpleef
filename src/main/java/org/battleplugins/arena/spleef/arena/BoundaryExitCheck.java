package org.battleplugins.arena.spleef.arena;

import org.battleplugins.arena.ArenaPlayer;
import org.battleplugins.arena.competition.LiveCompetition;
import org.battleplugins.arena.competition.map.options.Bounds;
import org.battleplugins.arena.competition.phase.CompetitionPhaseType;
import org.battleplugins.arena.event.player.ArenaLeaveEvent;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

final class BoundaryExitCheck {
    private BoundaryExitCheck() {
    }

    static void check(LiveCompetition<?> competition) {
        if (!CompetitionPhaseType.INGAME.equals(competition.getPhase())) {
            return;
        }
        World world = competition.getMap().getWorld();
        Bounds bounds = competition.getMap().getBounds();
        if (world == null || bounds == null) {
            return;
        }

        // Leaving can change membership and end the match synchronously.
        for (ArenaPlayer participant : List.copyOf(competition.getPlayers())) {
            if (!CompetitionPhaseType.INGAME.equals(competition.getPhase())) {
                return;
            }
            if (!competition.getPlayers().contains(participant) || !participant.getPlayer().isOnline()) {
                continue;
            }
            Location location = participant.getPlayer().getLocation();
            if (!world.equals(location.getWorld()) || !bounds.isInside(location)) {
                competition.leave(participant, ArenaLeaveEvent.Cause.GAME);
            }
        }
    }
}
