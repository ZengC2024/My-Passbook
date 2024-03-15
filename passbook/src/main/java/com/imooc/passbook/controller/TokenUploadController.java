package com.imooc.passbook.controller;



import com.imooc.passbook.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import  java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h1>PassTemplate Token Upload</h1>
 * 写Token 入redis*/
@Slf4j
@Controller  //以html页面返回，restController返回时JSON串
public class TokenUploadController {
    @Autowired
    public TokenUploadController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    private StringRedisTemplate redisTemplate;

    /** 1直接返回upload页面*/
    @GetMapping("/upload")
    public String upload(){
        return "upload";
    }
    @GetMapping("uploadStatus")
    public String uploadStatus(){
        return "uploadStatus";

    }
    /**过程：四个参数，前三个参数与前端模版文件相同，第四个为重定向参数
     * 首先判断是否合法
     * 然后写本地文件
     * 然后将token写入redis*/
    @PostMapping("/token")
    public String tokenFileUpload(@RequestParam("merchantsId") String merchantsId,
                                  @RequestParam("passtemplateId") String passTemplateId,
                                  @RequestParam("file")MultipartFile file,
                                  RedirectAttributes redirectAttributes){ //这个用于传递重定向的属性
        if(null==passTemplateId || file.isEmpty()){
            redirectAttributes.addFlashAttribute("message","passTemplateId is null" +
                    "or file is empty");
            return "redirect:uploadStatus"; //出错则重定向到这个页面
        }
        try{
            File cur =new File(Constants.TOKEN_DIR+merchantsId);
            if(!cur.exists()){
                log.info("create File:{}",cur.mkdir());
            }
            Path path = Paths.get(Constants.TOKEN_DIR,merchantsId,passTemplateId);
            /**写token入本地文件*/
            Files.write(path,file.getBytes());
            /**将token放入redis中
             * passTemplateId(也是PassTemplate在hbase中存储位置rowKey)作为token在redis中的key*/
            if(!writeTokenToRedis(path,passTemplateId)){
                redirectAttributes.addFlashAttribute("message","write token error");
            }else{
                redirectAttributes.addFlashAttribute("message","You successfully uploaded!"+
                        file.getOriginalFilename());
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return "redirect:/uploadStatus";
    }

    /**将token写入redis
     * path为token文件的路径对象，key为给token定义的redis key*/
    private boolean writeTokenToRedis(Path path, String key){
        Set<String> tokens;
        try(Stream<String> stream = Files.lines(path)){
            tokens =stream.collect(Collectors.toSet());
        }catch(IOException ex){
            ex.printStackTrace();
            return false;
        }

        /**写入redis*/
        if(!CollectionUtils.isEmpty(tokens)){
            /**Pipelined只有在单机时可以使用，集群时不可使用*/
            redisTemplate.executePipelined((RedisCallback<Object>)connection ->{
                for(String token:tokens){
                    connection.sAdd(key.getBytes(),token.getBytes());
                }
                return null;
            });
            return true;
        }
        return false;
    }
}
