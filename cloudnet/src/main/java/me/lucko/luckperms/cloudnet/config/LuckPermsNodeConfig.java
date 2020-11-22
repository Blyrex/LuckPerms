package me.lucko.luckperms.cloudnet.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class LuckPermsNodeConfig {

    private final boolean enabled;
    private final List<String> excludedGroups;

}
