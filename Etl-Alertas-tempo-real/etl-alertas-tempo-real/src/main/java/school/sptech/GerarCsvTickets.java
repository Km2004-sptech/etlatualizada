package school.sptech;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class GerarCsvTickets {

    public static void gerarCsv(JSONArray tickets) {

        String nomeArquivo = "tickets_" +
                java.time.LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) +
                ".csv";

        try (FileWriter csv = new FileWriter(nomeArquivo)) {

            csv.append("ID;Key;Summary;Descricao;Status;CriadoEm;ConcluidoEm;ConcluidoHoje\n");

            for (int i = 0; i < tickets.length(); i++) {
                JSONObject issue = tickets.getJSONObject(i);
                escreverLinha(issue, csv);
            }

            System.out.println("CSV gerado: " + nomeArquivo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void escreverLinha(JSONObject issue, FileWriter csv) throws IOException {

        String id = issue.optString("id", "");
        String key = issue.optString("key", "");

        JSONObject fields = issue.optJSONObject("fields");
        if (fields == null) fields = new JSONObject();

        String summary = sanitize(fields.optString("summary", ""));
        String descricao = sanitize(extrairDescricao(fields));

        String status = "";
        JSONObject statusObj = fields.optJSONObject("status");
        if (statusObj != null) {
            status = sanitize(statusObj.optString("name", ""));
        }

        String criadoRaw = fields.optString("created", "");
        String concluidoRaw = fields.optString("resolutiondate", "");

        String criadoEm = formatarData(criadoRaw);
        String concluidoEm = formatarData(concluidoRaw);

        boolean concluidoHoje = isHoje(concluidoRaw);

        csv.append(id).append(";")
                .append(key).append(";")
                .append(summary).append(";")
                .append(descricao).append(";")
                .append(status).append(";")
                .append(criadoEm).append(";")
                .append(concluidoEm).append(";")
                .append(concluidoHoje ? "SIM" : "NAO")
                .append("\n");
    }

    private static String sanitize(String s) {
        if (s == null) return "";
        return s.replace(";", ",")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    private static String extrairDescricao(JSONObject fields) {

        Object descObj = fields.opt("description");
        if (descObj == null) return "";

        // simples string
        if (descObj instanceof String) {
            return (String) descObj;
        }

        // Atlassian Document Format (ADF)
        if (descObj instanceof JSONObject descJson) {
            try {
                StringBuilder sb = new StringBuilder();

                JSONArray contentLvl1 = descJson.optJSONArray("content");
                if (contentLvl1 != null) {
                    for (int i = 0; i < contentLvl1.length(); i++) {
                        JSONObject node1 = contentLvl1.getJSONObject(i);
                        JSONArray contentLvl2 = node1.optJSONArray("content");
                        if (contentLvl2 != null) {
                            for (int j = 0; j < contentLvl2.length(); j++) {
                                JSONObject node2 = contentLvl2.getJSONObject(j);
                                if (node2.has("text")) {
                                    sb.append(node2.getString("text")).append("\n");
                                }
                            }
                        }
                    }
                }

                String texto = sb.toString().trim();
                return texto.isEmpty() ? descJson.toString() : texto;

            } catch (Exception e) {
                return descObj.toString();
            }
        }

        return descObj.toString();
    }

    private static String formatarData(String raw) {
        if (raw == null || raw.isBlank()) return "";
        try {
            // Jira manda algo como 2025-11-24T18:23:45.123+0000
            OffsetDateTime odt = OffsetDateTime.parse(
                    raw.replace("+0000", "Z")
            );
            return odt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return raw;
        }
    }

    private static boolean isHoje(String raw) {
        if (raw == null || raw.isBlank()) return false;
        try {
            // só compara a parte da data
            String normalizado = raw.replace("+0000", "Z");
            OffsetDateTime odt = OffsetDateTime.parse(normalizado);
            LocalDate data = odt.toLocalDate();
            return data.equals(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
}
