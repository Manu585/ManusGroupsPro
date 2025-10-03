package com.github.manu585.manusgroups.domain;

import java.util.Comparator;

public record Group(String name, String prefix, int weight, boolean isDefault) {
    public static final Comparator<Group> ORDER = Comparator.comparingInt(Group::weight).reversed().thenComparing(Group::name);
}
