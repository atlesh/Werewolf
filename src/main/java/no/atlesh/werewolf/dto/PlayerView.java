package no.atlesh.werewolf.dto;

import no.atlesh.werewolf.model.role.Role;

public record PlayerView(String id, String name, boolean alive, Role role) {}
