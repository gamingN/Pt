package com.example.veeotech.postaltracking.warehouse.bean;

/**
 * Created by VeeoTech on 2018/5/4.
 */

public class VersionBean {

    /**
     * version : 2
     * download_url : http://easy-logistics.com.hk/postal/app/postaltracking.apk
     * content : 發現新版本2.0,請更新!
     */

    private String version;
    private String download_url;
    private String content;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
