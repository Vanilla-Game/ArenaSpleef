package org.battleplugins.arena.spleef.arena;

import org.battleplugins.arena.ArenaPlayer;
import org.battleplugins.arena.competition.map.LiveCompetitionMap;
import org.battleplugins.arena.competition.map.options.Bounds;
import org.battleplugins.arena.competition.phase.CompetitionPhaseType;
import org.battleplugins.arena.event.player.ArenaLeaveEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

class BoundaryExitCheckTest {
    private final SpleefCompetition competition = mock(SpleefCompetition.class);
    private final LiveCompetitionMap map = mock(LiveCompetitionMap.class);
    private final Bounds bounds = mock(Bounds.class);
    private final World world = mock(World.class);
    private final Set<ArenaPlayer> players = new LinkedHashSet<>();

    @BeforeEach
    void setup() {
        doReturn(CompetitionPhaseType.INGAME).when(competition).getPhase();
        when(competition.getMap()).thenReturn(map);
        when(map.getWorld()).thenReturn(world);
        when(map.getBounds()).thenReturn(bounds);
        when(competition.getPlayers()).thenReturn(players);
    }

    private ArenaPlayer participant(World destinationWorld, boolean inside) {
        ArenaPlayer participant = mock(ArenaPlayer.class);
        Player player = mock(Player.class);
        Location location = new Location(destinationWorld, 10, 100, 20);
        when(participant.getPlayer()).thenReturn(player);
        when(player.isOnline()).thenReturn(true);
        when(player.getLocation()).thenReturn(location);
        when(bounds.isInside(location)).thenReturn(inside);
        players.add(participant);
        return participant;
    }

    @Test
    void leavesAfterTeleportOutsideAndOnlyOnce() {
        ArenaPlayer player = participant(world, false);
        doAnswer(invocation -> { players.remove(player); return null; })
                .when(competition).leave(player, ArenaLeaveEvent.Cause.GAME);
        BoundaryExitCheck.check(competition);
        BoundaryExitCheck.check(competition);
        verify(competition, times(1)).leave(player, ArenaLeaveEvent.Cause.GAME);
    }

    @Test
    void differentWorldLeavesEvenAtSameCoordinates() {
        ArenaPlayer player = participant(mock(World.class), true);
        BoundaryExitCheck.check(competition);
        verify(competition).leave(player, ArenaLeaveEvent.Cause.GAME);
    }

    @Test
    void playerStillInsideAfterCancelledTeleportStays() {
        participant(world, true);
        BoundaryExitCheck.check(competition);
        verify(competition, never()).leave(any(ArenaPlayer.class), any());
    }

    @Test
    void ignoresWaitingCountdownAndVictory() {
        participant(world, false);
        for (var phase : new CompetitionPhaseType[]{CompetitionPhaseType.WAITING,
                CompetitionPhaseType.COUNTDOWN, CompetitionPhaseType.VICTORY}) {
            doReturn(phase).when(competition).getPhase();
            BoundaryExitCheck.check(competition);
        }
        verify(competition, never()).leave(any(ArenaPlayer.class), any());
    }

    @Test
    void stopsIfFirstDepartureEndsMatch() {
        ArenaPlayer first = participant(mock(World.class), false);
        ArenaPlayer second = participant(mock(World.class), false);
        doAnswer(invocation -> {
            players.remove(first);
            doReturn(CompetitionPhaseType.VICTORY).when(competition).getPhase();
            return null;
        }).when(competition).leave(first, ArenaLeaveEvent.Cause.GAME);
        BoundaryExitCheck.check(competition);
        verify(competition).leave(first, ArenaLeaveEvent.Cause.GAME);
        verify(competition, never()).leave(second, ArenaLeaveEvent.Cause.GAME);
    }

    @Test
    void doesNotProcessSpectators() {
        ArenaPlayer spectator = participant(world, false);
        players.remove(spectator);
        when(competition.getSpectators()).thenReturn(Set.of(spectator));
        BoundaryExitCheck.check(competition);
        verify(competition, never()).leave(any(ArenaPlayer.class), any());
    }

    @Test
    void skipsParticipantRemovedByAnotherLeaveCallback() {
        ArenaPlayer first = participant(mock(World.class), false);
        ArenaPlayer second = participant(mock(World.class), false);
        doAnswer(invocation -> { players.clear(); return null; })
                .when(competition).leave(first, ArenaLeaveEvent.Cause.GAME);
        BoundaryExitCheck.check(competition);
        verify(competition).leave(first, ArenaLeaveEvent.Cause.GAME);
        verify(competition, never()).leave(second, ArenaLeaveEvent.Cause.GAME);
    }

    @Test
    void ignoresMissingBoundsAndOfflinePlayers() {
        ArenaPlayer player = participant(world, false);
        when(map.getBounds()).thenReturn(null);
        BoundaryExitCheck.check(competition);
        when(map.getBounds()).thenReturn(bounds);
        when(player.getPlayer().isOnline()).thenReturn(false);
        BoundaryExitCheck.check(competition);
        verify(competition, never()).leave(any(ArenaPlayer.class), any());
    }
}
