package com.lld.im.tcp;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.feign.FeignMessageRpcService;
import com.lld.im.tcp.reciver.MessageReciver;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.register.RegistryZK;
import com.lld.im.tcp.register.ZKit;
import com.lld.im.tcp.server.LimServer;
import com.lld.im.tcp.server.LimWebSocketServer;
import com.lld.im.tcp.utils.MqFactory;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.I0Itec.zkclient.ZkClient;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @description:
 */
public class ImTcpApplication {

    public static void main(String[] args) throws IOException {
        start();
    }

    private static void start(){
        try {
            Yaml yaml = new Yaml();
            /* 使用yaml插件 加载配置文件信息 */
            String path =
                    ClassLoader.getSystemClassLoader().getResources("config.yml").nextElement().getPath();
            InputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);
            FeignMessageRpcService rpcService=new  Feign.Builder()
                    .decoder(new JacksonDecoder())
                    .encoder(new JacksonEncoder())
                    .target(FeignMessageRpcService.class,bootstrapConfig.getLim().getLogicUrl());
            /* 启动im连接 tcp 以及 websocket */
            new LimServer(bootstrapConfig.getLim(),rpcService).start();
            new LimWebSocketServer(bootstrapConfig.getLim(),rpcService).start();

            RedisManager.init(bootstrapConfig);
            MqFactory.init(bootstrapConfig.getLim().getRabbitmq());
            MessageReciver.init(bootstrapConfig.getLim().getBrokerId()+"");
            registerZK(bootstrapConfig);

        }catch (Exception e){
            //出现异常 直接退出
            e.printStackTrace();
            System.exit(500);
        }
    }

    public static void registerZK(BootstrapConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        ZkClient zkClient = new ZkClient(config.getLim().getZkConfig().getZkAddr(),
                config.getLim().getZkConfig().getZkConnectTimeOut());
        ZKit zKit = new ZKit(zkClient);
        RegistryZK registryZK = new RegistryZK(zKit, hostAddress, config.getLim());
        Thread thread = new Thread(registryZK);
        thread.start();
    }
}
