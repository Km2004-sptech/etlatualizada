package school.sptech;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JiraService {

    private static final String EMAIL = "autobotics.sptech@gmail.com";
    private static final String API_TOKEN = "";

    private static final String BASE_URL =
            "https://autoboticssptech.atlassian.net/rest/api/3/search/jql";

    // JQL: abertos + concluídos hoje
    private static final String JQL = """
            (project = AAC AND statusCategory != Done)
            OR
            (project = AAC AND statusCategory = Done AND resolutiondate >= startOfDay())
            ORDER BY created DESC
            """;

    //Campos a serem retornados
    private static final String FIELDS = "id,key,summary,description,status,created,resolutiondate";

    private static String authHeader() {
        String auth = EMAIL + ":" + API_TOKEN;
        String encoded = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    public static JSONArray buscarTodosTicketsAAC() {
        JSONArray todos = new JSONArray();
        String nextPageToken = null;
        int maxResults = 100;

        try {
            while (true) {

                StringBuilder urlBuilder = new StringBuilder(BASE_URL);
                urlBuilder.append("?jql=")
                        .append(URLEncoder.encode(JQL, StandardCharsets.UTF_8));

                urlBuilder.append("&maxResults=").append(maxResults);

                urlBuilder.append("&fields=")
                        .append(URLEncoder.encode(FIELDS, StandardCharsets.UTF_8));

                if (nextPageToken != null && !nextPageToken.isBlank()) {
                    urlBuilder.append("&nextPageToken=")
                            .append(URLEncoder.encode(nextPageToken, StandardCharsets.UTF_8));
                }

                URL url = new URL(urlBuilder.toString());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", authHeader());
                con.setRequestProperty("Accept", "application/json");

                int status = con.getResponseCode();
                System.out.println("STATUS HTTP → " + status);

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        status >= 200 && status < 300
                                ? con.getInputStream()
                                : con.getErrorStream(),
                        StandardCharsets.UTF_8
                ));

                StringBuilder responseTxt = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    responseTxt.append(line);
                }

                String jsonStr = responseTxt.toString();
                System.out.println("RESPOSTA → " + jsonStr);

                if (status < 200 || status >= 300) {
                    break;
                }

                JSONObject json = new JSONObject(jsonStr);

                if (!json.has("issues")) {
                    System.out.println("⚠ Resposta não possui 'issues'");
                    break;
                }

                JSONArray issues = json.getJSONArray("issues");
                for (int i = 0; i < issues.length(); i++) {
                    todos.put(issues.getJSONObject(i));
                }

                boolean isLast = json.optBoolean("isLast", true);
                if (isLast || issues.length() == 0) {
                    break;
                }

                // próxima página
                if (json.has("nextPageToken")) {
                    nextPageToken = json.optString("nextPageToken", null);
                    if (nextPageToken == null || nextPageToken.isBlank()) {
                        break;
                    }
                } else {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return todos;
    }
}
