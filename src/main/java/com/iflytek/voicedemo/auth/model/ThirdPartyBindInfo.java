package com.iflytek.voicedemo.auth.model;

public class ThirdPartyBindInfo {
    public static final int TYPE_PHONE = 0;
    public static final int TYPE_QQ = 1;
    public static final int TYPE_WECHAT = 2;

    private int type;
    private String displayName;
    private String bindIdentifier;
    private boolean isBound;

    public ThirdPartyBindInfo(int type, String displayName, String bindIdentifier, boolean isBound) {
        this.type = type;
        this.displayName = displayName;
        this.bindIdentifier = bindIdentifier;
        this.isBound = isBound;
    }

    public int getType() { return type; }
    public String getDisplayName() { return displayName; }
    public String getBindIdentifier() { return bindIdentifier; }
    public boolean isBound() { return isBound; }
}
