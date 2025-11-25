package com.progressquest.model;

public class Quest {
    private final String title;
    private final String description;
    private final int requiredKills;
    private int currentKills = 0;
    private final long rewardXP;
    private final Item rewardItem;

    public Quest(String title, String description, int requiredKills, long rewardXP, Item rewardItem) {
        this.title = title;
        this.description = description;
        this.requiredKills = requiredKills;
        this.rewardXP = rewardXP;
        this.rewardItem = rewardItem;
    }

    public void registerKill() {currentKills++;}
    public boolean isCompleted() { return currentKills >= requiredKills;}

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getRewardXP() { return rewardXP; }
    public Item getRewardItem() { return rewardItem; }
    public int getRequiredKills() { return requiredKills; }
    public int getCurrentKills() { return currentKills; }
}