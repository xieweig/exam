package org.grow.exam.domain;

/**
 * Author : xieweig
 * Date : 18-8-1
 * <p>
 * Description:管理员要一些全局变量来控制功能开关
 */
import lombok.Data;

import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component

public class GlobalVar {

    private Boolean enableDownload = true;

    private byte[] tempFile = null;

    private String tempFileName= "";

    public Boolean getEnableDownload() {
        return enableDownload;
    }

    public synchronized void setEnableDownload(Boolean enableDownload) {
        this.enableDownload = enableDownload;
    }

    public byte[] getTempFile() {
        return tempFile;
    }

    public void setTempFile(byte[] tempFile) {
        this.tempFile = tempFile;
    }

    public String getTempFileName() {
        return tempFileName;
    }

    public void setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
    }

    @Override
    public String toString() {
        return "GlobalVar{" +
                "enableDownload=" + enableDownload +
                ", tempFile=" + Arrays.toString(tempFile) +
                ", tempFileName='" + tempFileName + '\'' +
                '}';
    }
}
