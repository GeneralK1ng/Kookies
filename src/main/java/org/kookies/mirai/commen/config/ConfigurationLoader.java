package org.kookies.mirai.commen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.GaodeAPIConstant;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.ConfigurationLoadException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.pojo.dto.PoiDTO;


import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.format.DateTimeFormatter;


public class ConfigurationLoader {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private static final File config = new File(DataPathInfo.CONFIG_PATH);

    private static final File eatPOI = new File(DataPathInfo.EAT_WHAT_POI_PATH);

    private static final DataFormatter dataFormatter = new DataFormatter();

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * 对外开放的方法
     * 用于加载配置文件
     */
    public static void init() {
        // 如果配置文件不存在，则按照 resources/template下的 configurationTemplate.json 创建一个配置文件
        try {
            if (!config.exists()){
                config.getParentFile().mkdirs();
                String templateFile = FileManager.readTemplateFile(DataPathInfo.CONFIG_TEMPLATE_PATH);
                FileManager.write(config.getPath(), templateFile);
            } else {
                update();
            }

            initEatPOI();

            // 读取配置文件并解析为 Config 对象
//            JsonObject jsonObject = FileManager.readJsonFile(config.getPath());
//            Config config = gson.fromJson(jsonObject, Config.class);
//            ConfigContext.setConfig(config);  // 设置到 ThreadLocal 中
        } catch (IOException e) {
            throw new ConfigurationLoadException(MsgConstant.CONFIG_LOAD_ERROR);
        }
    }

    /**
     * 用于更新配置文件
     */
    private static void update() {
        try {
            JsonObject jsonObject = FileManager.readJsonFile(config.getPath());
            FileManager.writeJsonFile(config.getPath(), jsonObject);
        } catch (IOException e) {
            throw new ConfigurationLoadException(MsgConstant.CONFIG_UPDATE_ERROR);
        }
    }

    private static void initEatPOI() {
        try {
            if (!eatPOI.exists()) {
                eatPOI.getParentFile().mkdirs();
                List<PoiDTO> poiList = getEatPOI();
                JsonArray jsonArray = gson.toJsonTree(poiList).getAsJsonArray();
                FileManager.write(eatPOI.getPath(), jsonArray.toString());
            } else {
                JsonArray jsonArray = FileManager.readJsonArray(eatPOI.getPath());
                FileManager.write(eatPOI.getPath(), jsonArray.toString());
            }
        } catch (IOException e) {
            throw new ConfigurationLoadException(MsgConstant.CONFIG_LOAD_ERROR);
        }
    }

    private static List<PoiDTO> getEatPOI() {
        String targetCategory = GaodeAPIConstant.TARGET_CATEGORY;

        List<PoiDTO> poiList = new ArrayList<>();
        try (InputStream is = Objects.requireNonNull(ConfigurationLoader.class.getResourceAsStream(DataPathInfo.MAP_POI_PATH));
             BufferedInputStream bis = new BufferedInputStream(is);
             Workbook workbook = new XSSFWorkbook(bis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                CellType cellType = row.getCell(1).getCellType();

                PoiDTO poiDTO = PoiDTO.builder()
                        .id(Long.parseLong(getCellValue(row.getCell(0))))
                        .newType(getCellValue(row.getCell(1)))
                        .bigCategory(getCellValue(row.getCell(2)))
                        .midCategory(getCellValue(row.getCell(3)))
                        .subCategory(getCellValue(row.getCell(4)))
                        .build();

                if (targetCategory.equals(poiDTO.getBigCategory())) {
                    poiList.add(poiDTO);
                }
            }

        } catch (IOException e) {
            throw new ConfigurationLoadException(MsgConstant.EXCEL_LOAD_ERROR);
        }

        return poiList;
    }

    private static String getCellValue(Cell cell) {
        String cellValue;
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                    return dateFormatter.format(date);
                } else {
                    return dataFormatter.formatCellValue(cell);
                }
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            case ERROR:
                return "ERROR";
            default:
                return "N/A";
        }
    }

}
