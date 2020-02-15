package com.example.demo.Tools;

import com.example.demo.Model.StepModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * json和yaml互转
 */
public class JsonYamlUntils {


    /**
     * 將json转化为yaml格式
     * @param jsonString
     * @return
     * @throws JsonProcessingException
     * @throws IOException
     */
    public List<StepModel> createYaml(String jsonString) throws JsonProcessingException, InvocationTargetException, IllegalAccessException {
        List<StepModel> stepModels = new ArrayList<>();
        // parse JSON
        JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonString);
        // save it as YAML
        String jsonAsYaml = new YAMLMapper().writeValueAsString(jsonNodeTree);
        // yaml to map
        Yaml yaml = new Yaml();
        Map<String,Object> map = (Map<String, Object>) yaml.load(jsonAsYaml);
        // Map to Bean
        BeanUtils.populate(stepModels,map);
        return stepModels;
    }

    /**
     * 讀取yaml生成json并返回
     *
     * @param file
     * @return
     */
    @Test
    public void yamlToJson() {
        String file = "F:\\project\\demo\\src\\main\\java\\com\\example\\demo\\Case\\post_temp.yaml";
        Gson gs = new Gson();
        Map<String, Object> loaded = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            Yaml yaml = new Yaml();
            loaded = (Map<String, Object>) yaml.load(fis);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        String string = gs.toJson(loaded);
        System.out.println(string);
    }


}
