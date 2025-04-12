package coursework_manager.http_server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import coursework_manager.http_server.dto.CourseworkDTO;
import coursework_manager.http_server.dto.CourseworkRecordDto;
import coursework_manager.repos.RecordRepo;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HttpServerCw {
    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/courseworks/add", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                    // Чтение JSON
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        requestBody.append(line);
                    }

                    // Парсинг JSON
                    ObjectMapper mapper = new ObjectMapper();
                    CourseworkRecordDto request = mapper.readValue(requestBody.toString(), CourseworkRecordDto.class);

                    // Обработка данных (здесь можно добавить сохранение в БД)
                    System.out.println("Received request for group: " + request.getGroupId());
                    for (CourseworkDTO cw : request.getCourseworks()) {
                        int cwRecordId = RecordRepo.addNewCw(cw.getTitle(), request.getTeacherId(), request.getGroupId());
                        if (cwRecordId == -1) {
                            throw new RuntimeException("error adding new cw");
                        }
                        cw.setId(cwRecordId);

                        System.out.println("Coursework: " + cw.getTitle());
                    }

                    try {
                        // Создаем ответ с обновленными данными
                        CourseworkRecordDto responseRecord = new CourseworkRecordDto();
                        responseRecord.setCourseworks(request.getCourseworks());
                        responseRecord.setGroupId(request.getGroupId());
                        responseRecord.setTeacherId(request.getTeacherId());

                        // Конвертируем в JSON
                         mapper = new ObjectMapper();
                        String responseJson = mapper.writeValueAsString(responseRecord);

                        // Отправляем ответ
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, responseJson.getBytes().length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(responseJson.getBytes());
                        }
                    } catch (Exception e) {
                        String error = "{\"error\":\"" + e.getMessage() + "\"}";
                        exchange.sendResponseHeaders(500, error.getBytes().length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(error.getBytes());
                        }
                    }
                } catch (Exception e) {
                    String error = "{\"error\":\"" + e.getMessage() + "\"}";
                    exchange.sendResponseHeaders(400, error.getBytes().length);
                    System.out.println(error.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(error.getBytes());
                    }
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        });
        server.start();
        System.out.println("Server started on port 8080");
    }

}