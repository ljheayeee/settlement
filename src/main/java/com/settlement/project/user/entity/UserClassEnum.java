package com.settlement.project.user.entity;

public enum UserClassEnum {

    NONE(Authority.NONE), // 사용자
    SILVER(Authority.SILVER), // 판매자 등급 실버
    GOLD(Authority.GOLD), // 판매자 등급 골드
    PLATINUM(Authority.PLATINUM); // 판매자 등급 플래티넘

    private final String authority;
    UserClassEnum(String authority){
        this.authority = authority;
    }

    public String getAuthority(){
        return this.authority;
    }

    public static class Authority {
        public static final String NONE = "CLASS_NONE";
        public static final String SILVER = "CLASS_SILVER";
        public static final String GOLD = "CLASS_GOLD";
        public static final String PLATINUM = "CLASS_PLATINUM";
    }
}
