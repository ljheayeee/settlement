package com.settlement.video.entity;

public enum VideoStatusEnum {
    ACTIVATE(VideoStatusEnum.Authority.ACTIVATE),
    INACTIVATE(VideoStatusEnum.Authority.INACTIVATE);

    private final String authority;
    VideoStatusEnum(String authority){
        this.authority = authority;
    }

    public String getAuthority(){
        return this.authority;
    }

    public static class Authority {
        public static final String ACTIVATE = "STATUS_ACTIVATE";
        public static final String INACTIVATE = "STATUS_INACTIVATE";

    }
}