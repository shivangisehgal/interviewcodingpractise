package com.conceptandcoding.LowLevelDesign.DesignRateLimiter.model;

import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.enums.UserTier;
import java.util.Objects;

public final class User {
    private final String userId;
    private final UserTier tier;

    public User(String userId, UserTier tier) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.tier = Objects.requireNonNull(tier, "tier");
    }

    public String getUserId() { return userId; }
    public UserTier getTier() { return tier; }
}
