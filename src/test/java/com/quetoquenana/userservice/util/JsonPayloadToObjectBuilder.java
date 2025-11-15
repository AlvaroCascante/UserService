package com.quetoquenana.userservice.util;

import com.fasterxml.jackson.databind.JavaType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class JsonPayloadToObjectBuilder<T> {

    private final Class<?> model;

    public JsonPayloadToObjectBuilder(Class<T> model) {
        this.model = model;
    }

    public List<T> loadListJsonFile(String fileName) throws IOException{
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        JavaType type = DateUtil.mapper.getTypeFactory().constructCollectionType(List.class, model);
        return DateUtil.mapper.readValue(inputStream, type);
    }

    public String loadJsonData(String fileName) throws IOException, URISyntaxException {
        return Files.readString(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).toURI()));
    }

    public T loadJsonObject(String jsonString) throws IOException {
        return DateUtil.mapper.readValue(jsonString, (Class<T>) model);
    }
}
