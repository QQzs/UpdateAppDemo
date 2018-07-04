package com.zs.demo.updateappdemo.bean;

/**
 * Created by zs
 * Date：2018年 07月 04日
 * Time：14:52
 * —————————————————————————————————————
 * About:
 * —————————————————————————————————————
 */
public class UpdateBean {


    /**
     * appCode : string
     * downloadurl : string
     * id : string
     * mustbe : false
     * platform : string
     * platformversion : string
     * releasedate : 2018-06-30T13:33:54.571Z
     * releasenote : string
     * version : string
     */

    private String appCode;
    private String downloadurl;
    private String id;
    private boolean mustbe; // 是否强制升级
    private String platform;
    private String platformversion;
    private String releasedate;
    private String releasenote;
    private String version;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getDownloadurl() {
        return downloadurl;
    }

    public void setDownloadurl(String downloadurl) {
        this.downloadurl = downloadurl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isMustbe() {
        return mustbe;
    }

    public void setMustbe(boolean mustbe) {
        this.mustbe = mustbe;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformversion() {
        return platformversion;
    }

    public void setPlatformversion(String platformversion) {
        this.platformversion = platformversion;
    }

    public String getReleasedate() {
        return releasedate;
    }

    public void setReleasedate(String releasedate) {
        this.releasedate = releasedate;
    }

    public String getReleasenote() {
        return releasenote;
    }

    public void setReleasenote(String releasenote) {
        this.releasenote = releasenote;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "UpdateBean{" +
                "appCode='" + appCode + '\'' +
                ", downloadurl='" + downloadurl + '\'' +
                ", id='" + id + '\'' +
                ", mustbe=" + mustbe +
                ", platform='" + platform + '\'' +
                ", platformversion='" + platformversion + '\'' +
                ", releasedate='" + releasedate + '\'' +
                ", releasenote='" + releasenote + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
