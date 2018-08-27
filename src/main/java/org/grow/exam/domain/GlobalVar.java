package org.grow.exam.domain;

/**
 * Author : xieweig
 * Date : 18-8-1
 * <p>
 * Description:管理员要一些全局变量来控制功能开关
 */
import lombok.Data;

import org.springframework.stereotype.Component;


@Component
@Data
public class GlobalVar {

    private Boolean enableDownload = true;

    private byte[] tempFile = null;

    private String tempFileName= "";


}
