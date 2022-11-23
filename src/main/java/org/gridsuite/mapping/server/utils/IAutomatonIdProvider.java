package org.gridsuite.mapping.server.utils;

import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;

public interface IAutomatonIdProvider {
    default String getId(AbstractAutomaton automaton) {
        return automaton.getWatchedElement();
    }
}
