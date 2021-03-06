package com.eip.cloud.dynamic.thread.pool.listener;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.eip.cloud.dynamic.thread.pool.config.ThreadPoolConstant;
import com.eip.cloud.dynamic.thread.pool.config.prop.DynamicThreadPoolProperties;
import com.eip.cloud.dynamic.thread.pool.core.DynamicThreadPoolManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import javax.annotation.PostConstruct;

/**
 * @Author: Barnett
 * @Date: 2021/11/8 17:53
 * @Description: Nacos配置修改监听
 */
@Slf4j
public class NacosConfigUpdateListener {

    @NacosInjected
    private ConfigService configService;

    @Autowired
    private DynamicThreadPoolManager dynamicThreadPoolManager;

    @Autowired
    private DynamicThreadPoolProperties poolProperties;

    @PostConstruct
    public void init() {
        initConfigUpdateListener();
    }

    public void initConfigUpdateListener() {
        Assert.hasText(poolProperties.getNacosDataId(), "property [eip.dynamic.thread.pool.nacosDataId] not allowed to be empty");
        Assert.hasText(poolProperties.getNacosGroup(), "property [eip.dynamic.thread.pool.nacosGroup] not allowed to be empty");

        try {
            configService.addListener(poolProperties.getNacosDataId(), poolProperties.getNacosGroup(), new AbstractListener() {


                @Override
                public void receiveConfigInfo(String configInfo) {

                    //等待Nacos配置刷新完成
                    waitConfigRefreshOver();

                    String nacosDataId = poolProperties.getNacosDataId();
                    if (nacosDataId.contains(ThreadPoolConstant.PROPERTIES_YAML)) {
                        poolProperties.refreshYaml(configInfo);
                    } else {
                        poolProperties.refresh(configInfo);
                    }

                    dynamicThreadPoolManager.refreshThreadPoolExecutor();
                    log.info("[dynamic pool] - nacos config change , refresh finished");
                }
            });
        } catch (NacosException e) {
            log.error("[dynamic pool] - nacos config refresh error , {}",e);
        }
        log.info("[dynamic pool] - nacos config listener init");
    }

    private void waitConfigRefreshOver() {
        if (poolProperties.isBoolWaitRefreshConfig()) {
            try {
                Thread.sleep(poolProperties.getWaitRefreshConfigSeconds() * 1000);
            } catch (InterruptedException e) {}
        }
    }

}
