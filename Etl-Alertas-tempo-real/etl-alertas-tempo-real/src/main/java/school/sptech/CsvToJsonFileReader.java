package school.sptech;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class CsvToJsonFileReader {

    public static JSONArray parseCsvToJson(String filePath) {
        JSONArray jsonArray = new JSONArray();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {


                if (firstLine) {
                    firstLine = false;
                    continue;
                }


                String[] parts = line.split(";", -1);
                if (parts.length < 7) continue;

                String id = parts[0];
                String key = parts[1];
                String summary = parts[2];
                String descricao = parts[3];
                String status = parts[4];
                String criadoEm = parts[5];
                String concluidoEm = parts[6];


                String setor = extract(descricao, "Setor:");
                String maquina = extract(descricao, "Máquina:");
                String componente = extract(descricao, "Componente:");
                String criticidade = extract(descricao, "Criticidade:");
                String valorAtual = extract(descricao, "Valor atual:");
                String minimo = extract(descricao, "Mínimo:");


                JSONObject obj = new JSONObject();
                obj.put("ID", id);
                obj.put("Key", key);
                obj.put("Summary", summary);

                JSONObject desc = new JSONObject();
                desc.put("Setor", setor);
                desc.put("Maquina", maquina);
                desc.put("Componente", componente);
                desc.put("Criticidade", criticidade);
                desc.put("ValorAtual", valorAtual);
                desc.put("Minimo", minimo);

                obj.put("Descricao", desc);
                obj.put("Status", status);
                obj.put("CriadoEm", criadoEm);
                obj.put("ConcluidoEm", concluidoEm.isEmpty() ? null : concluidoEm);

                jsonArray.put(obj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

    private static String extract(String text, String key) {
        try {
            int index = text.indexOf(key);
            if (index == -1) return "";

            String sub = text.substring(index + key.length()).trim();

            int end = sub.indexOf("\\n");
            if (end != -1) {
                sub = sub.substring(0, end).trim();
            }

            return sub;
        } catch (Exception e) {
            return "";
        }
    }

    public static void saveJsonToFile(JSONArray jsonArray, String outputPath) {
        try (FileWriter file = new FileWriter(outputPath, StandardCharsets.UTF_8)) {
            file.write(jsonArray.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}