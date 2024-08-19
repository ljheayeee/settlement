package com.settlement.project.ads.entity;

public enum AdStatusEnum {
    ACTIVE(AdStatusEnum.Authority.ACTIVE),
    INACTIVE(AdStatusEnum.Authority.INACTIVE),
    EXPIRED(AdStatusEnum.Authority.EXPIRED);

    private final String authority;

    AdStatusEnum(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return this.authority;
    }

    public static class Authority {
        public static final String ACTIVE = "STATUS_AD_ACTIVE";
        public static final String INACTIVE = "STATUS_AD_INACTIVE";
        public static final String EXPIRED = "STATUS_AD_EXPIRED";
    }
}