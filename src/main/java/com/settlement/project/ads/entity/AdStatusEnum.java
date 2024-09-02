package com.settlement.project.ads.entity;

public enum AdStatusEnum {
    ACTIVE(AdStatusEnum.Authority.ACTIVE),
    SCHEDULED(AdStatusEnum.Authority.SCHEDULED),
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
        public static final String SCHEDULED = "STATUS_AD_SCHEDULED";
        public static final String EXPIRED = "STATUS_AD_EXPIRED";
    }
}