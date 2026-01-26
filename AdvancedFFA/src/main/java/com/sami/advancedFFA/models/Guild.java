package com.sami.advancedFFA.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Guild {
    private final String name;
    private final String tag;
    private final UUID leader;
    private final List<UUID> officers;
    private final List<UUID> members;

    public Guild(String name, String tag, UUID leader) {
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        this.officers = new ArrayList<>();
        this.members = new ArrayList<>();
        this.members.add(leader);
    }

    public String getName() { return name; }
    public String getTag() { return tag; }
    public UUID getLeader() { return leader; }
    public List<UUID> getMembers() { return members; }
    public List<UUID> getOfficers() { return officers; }
}