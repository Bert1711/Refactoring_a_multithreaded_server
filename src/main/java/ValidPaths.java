import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ValidPaths {

    public static List<String> getValidPaths(String paths) {
        List<String> validPaths = null;
        try {
            String content = Files.readString(Paths.get(paths));
            validPaths = Arrays.asList(content.split("\\r?\\n"));
        } catch (IOException e) {
            e.printStackTrace();// обработка ошибки чтения файла
        }
        return validPaths;
    }
}
