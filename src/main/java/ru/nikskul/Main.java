package ru.nikskul;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

public class Main {
    public static void main(String[] args) throws IOException, SAXException {
        var baseDir = System.getProperty("user.dir");
        var basePath = Path.of(baseDir);

        var console = System.console();

        // load config
        var config = basePath.resolve("config.txt");

        final Path schemaPath = basePath.resolve(Pattern.compile("(?<=schema=\").*(?=\")")
            .matcher(Files.readString(config))
            .results().findFirst().orElseThrow().group());

        final Path xmlDirPath = basePath.resolve(Pattern.compile("(?<=xmlDirectory=\").*(?=\")")
            .matcher(Files.readString(config))
            .results().findFirst().orElseThrow().group());

        // config validator
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.FALSE);

        var validator = factory.newSchema(new StreamSource(schemaPath.toFile())).newValidator();

        String resultsName = "ошибки-валидации";
        Path resultsDirPath = basePath.resolve(resultsName);
        if (!Files.isDirectory(xmlDirPath) || !resultsDirPath.toFile().exists()) {
            Files.createDirectory(resultsDirPath);
        }
        try (var dir = Files.newDirectoryStream(xmlDirPath)) {
            for (var xml : dir) {
                File file = xml.toFile();
                try {
                    validator.validate(new StreamSource(file));
                } catch (Exception e) {
                    Path resultsFilePath = resultsDirPath.resolve(file.getName());
                    Files.write(
                        resultsFilePath,
                        e.getMessage().getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE
                    );
                }
            }
        }
        console.printf("Данные помещены в папку \"ошибки валидации\"\n");
    }
}